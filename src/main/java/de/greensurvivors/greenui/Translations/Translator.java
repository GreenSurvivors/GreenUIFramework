package de.greensurvivors.greenui.Translations;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
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
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Must be formatted in {@link MiniMessage#deserialize(Object)} readable input
 */
public class Translator {
    private final @Nullable FileConfiguration fallbackConfig;
    private final @NotNull Locale fallbackLocale;
    private final @NotNull TranslationRegistry registry;
    private final @NotNull Plugin plugin;

    private @Nullable Locale defaultLocale;
    private boolean shouldUsePlayerLocale;

    public Translator(@NotNull Plugin plugin) {
        this(plugin, false, null);
    }

    public Translator(@NotNull Plugin plugin, boolean shouldUsePlayerLocale, @Nullable Locale defaultLocale) {
        this.plugin = plugin;
        this.shouldUsePlayerLocale = shouldUsePlayerLocale;
        this.defaultLocale = defaultLocale;

        this.fallbackLocale = new Locale.Builder().setLanguage("cow").setScript("Latn").setRegion("GS").build();

        @Subst("test") final String pluginName = plugin.getName().toLowerCase(); // please use simple latin and no one gets hurt
        this.registry = TranslationRegistry.create(Key.key(pluginName + ":greenui_framework"));

        this.fallbackConfig = getFallbackConfig(); // todo load
    }

    /**
     * Checks if any translations are explicitly registered for the specified key.
     *
     * @param key a translation key
     * @return whether the registry contains a value for the translation key
     */
    public boolean contains(@NotNull String key) {
        return this.registry.contains(key);
    }


    /**
     * Gets a message format from a key and locale.
     *
     * <p>If a translation for {@code locale} is not found, we will then try {@code locale} without a country code, and then finally fallback to a default locale.</p>
     *
     * @param key    a translation key
     * @param locale a locale
     * @return a message format or {@code null} to skip translation
     * @since 4.0.0
     */
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return this.registry.translate(key, locale);
    }

    public @NotNull MessageFormat simpleTranslate(@NotNull String key) {
        MessageFormat messageFormat = this.translate(key, defaultLocale == null ? fallbackLocale : defaultLocale);

        return Objects.requireNonNullElseGet(messageFormat, () -> new MessageFormat(key));
    }

    public @NotNull Component translateToComponent(@NotNull String key) { //todo player locale
        MessageFormat messageFormat = this.translate(key, defaultLocale == null ? fallbackLocale : defaultLocale);

        if (messageFormat != null) {
            return MiniMessage.miniMessage().deserialize(messageFormat.format(""));
        } else {
            return Component.text(key);
        }
    }

    /**
     * Gets a translated component from a translatable component and locale.
     *
     * @param component a translatable component
     * @param locale    a locale
     * @return a translated component or {@code null} to use {@link #translate(String, Locale)} instead (if available)
     * @since 4.13.0
     */
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        MessageFormat format = translate(component.key(), locale);

        if (format != null) {
            String[] test = component.args().stream().map(s -> MiniMessage.miniMessage().serialize(s)).toArray(String[]::new);


            return MiniMessage.miniMessage().deserialize(format.format(test, new StringBuffer(), new FieldPosition(0)).toString());
        } else {
            if (component.fallback() != null) {
                return MiniMessage.miniMessage().deserialize(component.fallback());

            }
            return null;
        }
    }


    /**
     * Sets the default locale used by this registry.
     *
     * @param locale the locale to use a default
     */
    public void defaultLocale(@NotNull Locale locale) {
        this.defaultLocale = locale;
    }

    /**
     * Registers a translation.
     * final TranslationRegistry registry;
     * registry.register("example.hello", Locale.US, new MessageFormat("Hi, {0}. How are you?"));
     *
     * @param key    – a translation key
     * @param locale – a locale
     * @param format – a translation format
     *               Throws:
     *               IllegalArgumentException – if the translation key is already exists
     */
    public void register(final @NotNull String key, final @NotNull Locale locale, final @NotNull MessageFormat format) {
        this.registry.register(key, locale, format);
    }

    /**
     * Registers a map of translations.
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
     * @param locale  a locale
     * @param formats a map of translation keys to formats
     * @throws IllegalArgumentException if a translation key is already exists
     * @see #register(String, Locale, MessageFormat)
     */
    public void registerAll(@NotNull Locale locale, @NotNull Map<String, MessageFormat> formats) {
        this.registerAll(locale, formats.keySet(), formats::get);
    }

    /**
     * Registers a resource bundle of translations.
     *
     * @param locale             a locale
     * @param path               a path to the resource bundle
     * @param escapeSingleQuotes whether to escape single quotes
     * @throws IllegalArgumentException if a translation key is already exists
     * @see #registerAll(Locale, ResourceBundle, boolean)
     */
    public void registerAll(@NotNull Locale locale, @NotNull Path path, boolean escapeSingleQuotes) {
        try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.registerAll(locale, new PropertyResourceBundle(reader), escapeSingleQuotes);
        } catch (final IOException e) {
            // ignored
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
     * @throws IllegalArgumentException if a translation key is already exists
     * @see net.kyori.adventure.util.UTF8ResourceBundleControl
     */
    public void registerAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean escapeSingleQuotes) {
        this.registerAll(locale, bundle.keySet(), key -> {
            final String format = bundle.getString(key);

            return new MessageFormat(
                    escapeSingleQuotes
                            ? TranslationRegistry.SINGLE_QUOTE_PATTERN.matcher(format).replaceAll("''")
                            : format,
                    locale
            );
        });
    }

    /**
     * Registers a resource bundle of translations.
     *
     * @param locale   a locale
     * @param keys     the translation keys to register
     * @param function a function to transform a key into a message format
     * @throws IllegalArgumentException if a translation key is already exists
     */
    public void registerAll(@NotNull Locale locale, @NotNull Set<String> keys, Function<String, MessageFormat> function) {
        IllegalArgumentException firstError = null;
        int errorCount = 0;
        for (final String key : keys) {
            try {
                this.register(key, locale, function.apply(key));
            } catch (final IllegalArgumentException e) {
                if (firstError == null) {
                    firstError = e;
                }
                errorCount++;
            }
        }
        if (firstError != null) {
            if (errorCount == 1) {
                throw firstError;
            } else if (errorCount > 1) {
                throw new IllegalArgumentException(String.format("Invalid key (and %d more)", errorCount - 1), firstError);
            }
        }
    }

    private @Nullable YamlConfiguration getFallbackConfig() {
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

    public boolean isShouldUsePlayerLocale() {
        return shouldUsePlayerLocale;
    }

    public void setShouldUsePlayerLocale(boolean shouldUsePlayerLocale) {
        this.shouldUsePlayerLocale = shouldUsePlayerLocale;
    }

    public enum PlaceHolders {

    }
}
