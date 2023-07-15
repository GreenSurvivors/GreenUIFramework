package de.greensurvivors.greenui.Translations;

/*
This file was made with code from adventure, licensed under the MIT License.

Copyright (c) 2017-2023 KyoriPowered

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class GreenTranslationRegistry {
    private final @NotNull Locale fallbackLocale;
    private final Map<@NotNull Locale, @Nullable ConcurrentHashMap<String, MessageFormat>> translations = new ConcurrentHashMap<>();
    private @Nullable Locale defaultLocale;

    public GreenTranslationRegistry(@NotNull Plugin plugin, @Nullable Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        this.fallbackLocale = new Locale.Builder().setLanguage("cow").setScript("Latn").setRegion("GS").build();

        // register fallback translations
        FileConfiguration cfg = getFallbackConfig(plugin);
        for (TranslationData data : TranslationData.values()) {
            String message = cfg.getString(data.getKey());

            if (message != null) {
                this.registerLocaleKey(fallbackLocale, data.getKey(), new MessageFormat(message));
            }
        }
    }

    private @Nullable YamlConfiguration getFallbackConfig(Plugin plugin) {
        try {
            URL url = getClass().getClassLoader().getResource("fallback_lang");

            if (url == null) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't get fallback language file (missing). Your Menus may be broken!");
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);

            return YamlConfiguration.loadConfiguration(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't get fallback language file (error). Your Menus may be broken!", ex);
            return null;
        }
    }

    /**
     * Gets the default locale used by this registry.
     */
    public @Nullable Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Sets the default locale used by this registry.
     *
     * @param locale the locale to use a default
     */
    public void setDefaultLocale(@Nullable Locale locale) {
        this.defaultLocale = locale;
    }

    /**
     * Registers a translation.
     * final GreenTranslationRegistry registry;
     * registry.register(Locale.US, "example.hello", new MessageFormat("Hi, {0}. How are you?"));
     * replaces an already registered translation of the same key of the same locale
     *
     * @param key    – a translation key
     * @param locale – a locale
     * @param format – a translation format
     */
    public void registerLocaleKey(final @NotNull Locale locale, final @NotNull String key, final @NotNull MessageFormat format) {
        requireNonNull(locale, "locale");
        requireNonNull(key, "message key");
        requireNonNull(format, "message format");

        this.translations.putIfAbsent(locale, new ConcurrentHashMap<>());
        requireNonNull(this.translations.get(locale)).put(key, format);
    }

    /**
     * Registers a map of translations.
     * replaces already registered translations of the same key of the same locale
     *
     * <pre>
     *   final TranslationRegistry registry;
     *   final Map&#60;String, MessageFormat&#62; translations;
     *
     *   translations.put("example.greeting", new MessageFormat("Greetings {0}. Doing ok?));
     *   translations.put("example.goodbye", new MessageFormat("Goodbye {0}. Have a nice day!));
     *
     *   registry.registerAll(Locale.US, translations);
     * </pre>
     *
     * @param locale       a locale
     * @param translations a map of translation keys to formats
     * @see #registerLocaleKey(Locale, String, MessageFormat)
     */
    public void registerLocale(final @NotNull Locale locale, final @NotNull Map<@NotNull String, @NotNull MessageFormat> translations) {
        requireNonNull(locale, "locale");
        requireNonNull(translations, "messages map");

        this.translations.putIfAbsent(locale, new ConcurrentHashMap<>());
        requireNonNull(this.translations.get(locale)).putAll(translations);
    }

    /**
     * Registers a resource bundle of translations.
     *
     * @param locale             a locale
     * @param path               a path to the resource bundle
     * @param escapeSingleQuotes whether to escape single quotes
     * @see #registerLocaleAll(Locale, ResourceBundle, boolean)
     */
    public void registerLocaleAll(@NotNull Locale locale, @NotNull Path path, boolean escapeSingleQuotes) {
        try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.registerLocaleAll(locale, new PropertyResourceBundle(reader), escapeSingleQuotes);
        } catch (final IOException ignored) {
        }
    }

    /**
     * Registers a resource bundle of translations.
     *
     * <p>It is highly recommended to create your bundle using {@link net.kyori.adventure.util.UTF8ResourceBundleControl} as your bundle control for UTF-8 support - for example:</p>
     *
     * <pre>
     *   final ResourceBundle bundle = ResourceBundle.getBundle("my_bundle", Locale.GERMANY, UTF8ResourceBundleControl.get());
     *   registry.registerAll(Locale.GERMANY, bundle, false);
     * </pre>
     *
     * @param locale             a locale
     * @param bundle             a resource bundle
     * @param escapeSingleQuotes whether to escape single quotes
     * @see net.kyori.adventure.util.UTF8ResourceBundleControl
     */
    public void registerLocaleAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean escapeSingleQuotes) {
        for (String key : bundle.keySet()) {
            String bundleStr = bundle.getString(key);

            if (escapeSingleQuotes) {
                bundleStr = TranslationRegistry.SINGLE_QUOTE_PATTERN.matcher(bundleStr).replaceAll("''");
            }

            this.registerLocaleKey(locale, key, new MessageFormat(bundleStr));
        }
    }

    public void unregisterLocale(final @NotNull Locale locale) {
        this.translations.remove(locale);
    }

    public void unregisterKey(final @NotNull String key) {
        Iterator<Map.Entry<Locale, ConcurrentHashMap<String, MessageFormat>>> iterator = translations.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Locale, ConcurrentHashMap<String, MessageFormat>> entry = iterator.next();

            if (entry.getValue() != null) {
                entry.getValue().remove(key);
            } else {
                // however that might happen to be
                iterator.remove();
            }
        }
    }

    private @Nullable MessageFormat getLocaleFormat(final @Nullable Locale locale, final @NotNull String key) {
        if (locale != null) {
            ConcurrentHashMap<String, MessageFormat> localeTranslations = this.translations.get(locale);

            if (localeTranslations == null) {
                localeTranslations = this.translations.get(new Locale(locale.getLanguage()));
            }

            if (localeTranslations != null) {
                return localeTranslations.get(key);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets a message format from a key and locale.
     *
     * <p>If a translation for {@code locale} is not found, we will then try {@code locale} without a country code, and then finally fallback to a default locale.</p>
     *
     * @param key    a translation key
     * @param locale a locale
     * @return a message format or {@code null} to skip translation
     */
    public @Nullable MessageFormat translate(final @Nullable Locale locale, final @NotNull String key) {
        @Nullable MessageFormat result = getLocaleFormat(locale, key);

        if (result == null) {
            result = getLocaleFormat(defaultLocale, key);

            if (result == null) {
                result = getLocaleFormat(fallbackLocale, key);
            }
        }

        return result;
    }
}