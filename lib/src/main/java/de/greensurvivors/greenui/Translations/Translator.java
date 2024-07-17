package de.greensurvivors.greenui.Translations;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * manages all translatable and placeholders used by this plugin.
 */
public class Translator {
    private final @NotNull ConcurrentHashMap<@NotNull Locale, @NotNull ResourceBundle> propertiesHashMap = new ConcurrentHashMap<>();
    private final @NotNull ConcurrentHashMap<@NotNull Locale, @NotNull Locale> localeCache = new ConcurrentHashMap<>();
    private final @NotNull String fullBundlePath;
    private final @NotNull Plugin plugin;
    /**
     * caches every component without placeholder for faster access in future and loads missing values automatically
     */
    private final LoadingCache<PluginLocalePath, Component> langCache = Caffeine.newBuilder().maximumSize(1000000).build(
            path -> MiniMessage.miniMessage().deserialize(getStringFromLang(path.locale(), path.langPath())));
    private @NotNull Locale defaultLocale;


    public Translator(@NotNull Plugin plugin, @NotNull Locale defaultLocale, @NotNull String fullBundlePath) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale;
        this.fullBundlePath = fullBundlePath.replaceAll("[\\\\/]", File.separator);
        initLangFiles();
    }

    private void ensureBundleWasLoaded(@NotNull Plugin plugin, @NotNull Locale locale) throws IllegalStateException {
        File langDictionary = new File(plugin.getDataFolder(), fullBundlePath);

        int separatorIndex = fullBundlePath.lastIndexOf(File.pathSeparatorChar);
        String bundleName;
        if (separatorIndex != -1) {
            bundleName = fullBundlePath.substring(separatorIndex + 1);
        } else {
            bundleName = fullBundlePath;
        }

        URL[] urls;
        @Nullable ResourceBundle bundle = null;
        try {
            urls = new URL[]{langDictionary.toURI().toURL()};
            bundle = ResourceBundle.getBundle(bundleName, locale, new URLClassLoader(urls), UTF8ResourceBundleControl.get());

        } catch (SecurityException | MalformedURLException e) {
            plugin.getLogger().log(Level.WARNING, "Exception while reading lang bundle. Using internal", e);
        } catch (MissingResourceException ignored) { // how? missing write access?
            plugin.getLogger().log(Level.WARNING, "No translation file for " + UTF8ResourceBundleControl.get().toBundleName(bundleName, locale) + " found on disc. Using internal");
        }

        if (bundle == null) { // fallback, since we are always trying to save defaults this never should happen
            try {
                bundle = PropertyResourceBundle.getBundle(bundleName, locale, plugin.getClass().getClassLoader(), new UTF8ResourceBundleControl());
            } catch (MissingResourceException e) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't get Resource bundle \"" + UTF8ResourceBundleControl.get().toBundleName(bundleName, locale) + "\". Messages WILL be broken!", e);
            }
        }

        if (bundle == null) {
            throw new IllegalStateException("Couldn't find any resource bundle");
        }

        if (localeCache.containsKey(bundle.getLocale())) {
            localeCache.put(locale, bundle.getLocale());
        } else {
            localeCache.put(locale, locale);
            propertiesHashMap.put(locale, bundle);
        }
    }

    private @NotNull String getStringFromLang(@NotNull Locale locale, @NotNull LangPath langPath) throws IllegalStateException {
        if (!localeCache.containsKey(locale)) {
            ensureBundleWasLoaded(plugin, locale); // throws the exception
        }

        try {
            return propertiesHashMap.get(locale).getString(langPath.getPath());
        } catch (MissingResourceException | ClassCastException e) {
            plugin.getLogger().log(Level.WARNING, "couldn't find langPath: \"" + langPath.getPath() + "\" in lang files using fallback.", e);
            return langPath.getDefaultValue();
        }
    }

    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder convertedStrBuilder = new StringBuilder(bufLen);

        for (int i = 0; i < theString.length(); i++) {
            char aChar = theString.charAt(i);
            // Handle common case first
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    if (i + 1 < theString.length()) {
                        final char bChar = theString.charAt(i + 1);
                        if (bChar == ' ' || bChar == 't' || bChar == 'n' || bChar == 'r' ||
                                bChar == 'f' || bChar == '\\' || bChar == 'u' || bChar == '=' ||
                                bChar == ':' || bChar == '#' || bChar == '!') {
                            // don't double escape already escaped chars
                            convertedStrBuilder.append(aChar);
                            convertedStrBuilder.append(bChar);
                            i++;
                            continue;
                        } else {
                            // any other char following
                            convertedStrBuilder.append('\\');
                        }
                    } else {
                        // last char was a backslash. escape!
                        convertedStrBuilder.append('\\');
                    }
                }
                convertedStrBuilder.append(aChar);
                continue;
            }

            // escape non escaped chars that have to get escaped
            switch (aChar) {
                case ' ' -> {
                    if (escapeSpace) {
                        convertedStrBuilder.append('\\');
                    }
                    convertedStrBuilder.append(' ');
                }
                case '\t' -> convertedStrBuilder.append("\\t");
                case '\n' -> convertedStrBuilder.append("\\n");
                case '\r' -> convertedStrBuilder.append("\\r");
                case '\f' -> convertedStrBuilder.append("\\f");
                case '=', ':', '#', '!' -> {
                    convertedStrBuilder.append('\\');
                    convertedStrBuilder.append(aChar);
                }
                default -> convertedStrBuilder.append(aChar);
            }
        }

        return convertedStrBuilder.toString();
    }

    /**
     * saves all missing lang files from resources to the plugins data folder
     */
    private void initLangFiles() {
        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();

        if (src != null) {
            final Pattern bundleFileNamePattern = Pattern.compile(fullBundlePath + "(?:_.*)?.properties");
            URL jarUrl = src.getLocation();

            try (ZipInputStream zipStream = new ZipInputStream(jarUrl.openStream())) {
                ZipEntry zipEntry;
                while ((zipEntry = zipStream.getNextEntry()) != null) {
                    if (zipEntry.isDirectory()) {
                        continue;
                    }

                    String entryName = zipEntry.getName();

                    if (bundleFileNamePattern.matcher(entryName).matches()) {
                        File langFile = new File(new File(plugin.getDataFolder(), fullBundlePath), entryName);
                        if (!langFile.exists()) { // don't overwrite existing files
                            // write all defaults into a file
                            FileUtils.copyToFile(zipStream, langFile);

                            // now add our fallback values - if they are missing
                            Properties current = new Properties();
                            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8)) {
                                current.load(reader);
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "couldn't get current properties file for " + entryName + "!", e);
                                continue;
                            }

                            try (FileWriter fw = new FileWriter(langFile, StandardCharsets.UTF_8, true);
                                 // we are NOT using Properties#store since it gets rid of comments and doesn't guarantee ordering
                                 BufferedWriter bw = new BufferedWriter(fw)) {
                                boolean updated = false; // only write comment once

                                for (BuildInLangPaths langPath : BuildInLangPaths.values()) {
                                    if (current.get(langPath.getPath()) == null) {
                                        if (!updated) {
                                            // most likely this will generate an empty line, since the last line should be empty.
                                            // however this is NOT guaranteed, and therefore we could write the comment onto an existing line and ruin the translation there, if this new line would be missing.
                                            bw.newLine();
                                            bw.write("# New Values where added. Is everything else up to date? Time of update: " + new Date());
                                            bw.newLine();

                                            plugin.getLogger().fine("Updated langfile \"" + entryName + "\" for plugin " + plugin.getName() + ". Might want to check the new translation strings out!");

                                            updated = true;
                                        }

                                        String key = saveConvert(langPath.getPath(), true);
                                        /* No need to escape embedded and trailing spaces for value, hence
                                         * pass false to flag.
                                         */
                                        String val;
                                        if (langPath == BuildInLangPaths.PLUGIN_PREFIX) {
                                            val = saveConvert("[" + plugin.getName() + "]", false);
                                        } else {
                                            val = saveConvert(langPath.getDefaultValue(), false);
                                        }

                                        bw.write((key + "=" + val));
                                        bw.newLine();
                                    } // current already knows the key
                                } // end of fallback
                            } // end of try
                        } else { // add defaults to file to expand in case there are key-value pairs missing
                            Properties defaults = new Properties();
                            // no try with since we need to keep the ZipStream open
                            try {
                                defaults.load(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "couldn't get default properties file for " + entryName + "!", e);
                                continue;
                            }

                            Properties current = new Properties();
                            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8)) {
                                current.load(reader);
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "couldn't get current properties file for " + entryName + "!", e);
                                continue;
                            }

                            try (FileWriter fw = new FileWriter(langFile, StandardCharsets.UTF_8, true);
                                 // we are NOT using Properties#store since it gets rid of comments and doesn't guarantee ordering
                                 BufferedWriter bw = new BufferedWriter(fw)) {
                                boolean updated = false; // only write comment once
                                for (Map.Entry<Object, Object> translationPair : defaults.entrySet()) {
                                    if (current.get(translationPair.getKey()) == null) {
                                        if (!updated) {
                                            // most likely this will generate an empty line, since the last line should be empty.
                                            // however this is NOT guaranteed, and therefore we could write the comment onto an existing line and ruin the translation there, if this new line would be missing.
                                            bw.newLine();
                                            bw.write("# New Values where added. Is everything else up to date? Time of update: " + new Date());
                                            bw.newLine();

                                            plugin.getLogger().fine("Updated langfile \"" + entryName + "\" for plugin " + plugin.getName() + ". Might want to check the new translation strings out!");

                                            updated = true;
                                        }

                                        String key = saveConvert((String) translationPair.getKey(), true);
                                        /* No need to escape embedded and trailing spaces for value, hence
                                         * pass false to flag.
                                         */
                                        String val = saveConvert((String) translationPair.getValue(), false);
                                        bw.write((key + "=" + val));
                                        bw.newLine();
                                    } // current already knows the key
                                } // end of defaults for

                                for (BuildInLangPaths langPath : BuildInLangPaths.values()) {
                                    if (current.get(langPath.getPath()) == null) {
                                        if (!updated) {
                                            // most likely this will generate an empty line, since the last line should be empty.
                                            // however this is NOT guaranteed, and therefore we could write the comment onto an existing line and ruin the translation there, if this new line would be missing.
                                            bw.newLine();
                                            bw.write("# New Values where added. Is everything else up to date? Time of update: " + new Date());
                                            bw.newLine();

                                            plugin.getLogger().fine("Updated langfile \"" + entryName + "\" for plugin " + plugin.getName() + ". Might want to check the new translation strings out!");

                                            updated = true;
                                        }

                                        String key = saveConvert(langPath.getPath(), true);
                                        /* No need to escape embedded and trailing spaces for value, hence
                                         * pass false to flag.
                                         */
                                        String val;
                                        if (langPath == BuildInLangPaths.PLUGIN_PREFIX) {
                                            val = saveConvert("[" + plugin.getName() + "]", false);
                                        } else {
                                            val = saveConvert(langPath.getDefaultValue(), false);
                                        }
                                        bw.write((key + "=" + val));
                                        bw.newLine();
                                    } // current already knows the key
                                } // end of fallback
                            } // end of try
                        } // end of else (file exists)
                    } // doesn't match
                } // end of elements
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Couldn't save lang files", e);
            }
        } else {
            plugin.getLogger().warning("Couldn't save lang files: no CodeSource!");
        }
    }

    /**
     * prepend the message with the plugins prefix before sending it to the audience.
     */
    public void sendMessageWithPrefix(@NotNull Locale locale, @NotNull Audience audience, @NotNull Component messages) {
        audience.sendMessage(langCache.get(new PluginLocalePath(locale, BuildInLangPaths.PLUGIN_PREFIX)).appendSpace().append(messages));
    }

    /**
     * get a component from lang file and apply the given tag resolver.
     * Note: might be slightly slower than {@link #getLang(Locale, LangPath)} since this can not use cache.
     */
    public @NotNull Component getLang(@NotNull Locale locale, @NotNull LangPath path, @NotNull TagResolver resolver) {
        return MiniMessage.miniMessage().deserialize(getStringFromLang(locale, path), resolver);
    }

    /**
     * get a component from lang file
     */
    public @NotNull Component getLang(@NotNull Locale locale, @NotNull LangPath path) {
        return langCache.get(new PluginLocalePath(locale, path));
    }

    /**
     * send a component from the lang file to the audience, prefixed with this plugins prefix.
     */
    public void sendLang(@NotNull Plugin plugin, @NotNull Locale locale, @NotNull Audience audience, @NotNull LangPath path) {
        audience.sendMessage(langCache.get(new PluginLocalePath(locale, BuildInLangPaths.PLUGIN_PREFIX)).appendSpace().append(langCache.get(new PluginLocalePath(locale, path))));
    }

    /**
     * send a component from the lang file to the audience, prefixed with this plugins prefix and applying the given tag resolver.
     * Note: might be slightly slower than {@link #sendLang(Plugin, Locale, Audience, LangPath)} since this can not use cache.
     */
    public void sendLang(@NotNull Locale locale, @NotNull Audience audience, @NotNull LangPath path, @NotNull TagResolver resolver) {
        audience.sendMessage(langCache.get(new PluginLocalePath(locale, BuildInLangPaths.PLUGIN_PREFIX)).appendSpace().append(
                MiniMessage.miniMessage().deserialize(getStringFromLang(locale, path), resolver)));
    }

    public @NotNull Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(@NotNull Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    private record PluginLocalePath(@NotNull Locale locale, @NotNull LangPath langPath) {
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof PluginLocalePath that) {
                return this.locale.equals(that.locale) &&
                        this.langPath.equals(that.langPath);
            } else {
                return false;
            }
        }
    }
}
