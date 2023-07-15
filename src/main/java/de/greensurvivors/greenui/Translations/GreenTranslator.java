package de.greensurvivors.greenui.Translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

//Null <-- fallback <-- default(optional) <-- similar (optional)<-- start

/**
 * Registered Translations must be formatted in {@link MiniMessage#deserialize(Object)} readable input
 */
public class GreenTranslator extends GreenTranslationRegistry { //
    private boolean shouldUsePlayerLocale;

    public GreenTranslator(@NotNull Plugin plugin) {
        this(plugin, false, null);
    }

    public GreenTranslator(@NotNull Plugin plugin, boolean shouldUsePlayerLocale, @Nullable Locale defaultLocale) {
        super(plugin, defaultLocale);
        this.shouldUsePlayerLocale = shouldUsePlayerLocale;
    }

    public @Nullable MessageFormat translatePlayerLocal(@NotNull String key, @NotNull Locale locale) {
        return this.translate(shouldUsePlayerLocale ? locale : null, key);
    }

    /**
     * doesn't care about placeholders
     *
     * @param key
     * @return
     */
    public @NotNull MessageFormat simpleTranslate(@NotNull String key) {
        MessageFormat messageFormat = this.translate(null, key);

        return Objects.requireNonNullElseGet(messageFormat, () -> new MessageFormat(key));
    }

    public @NotNull Component translateToComponent(@NotNull String key) { //todo player locale
        MessageFormat messageFormat = simpleTranslate(key);

        return MiniMessage.miniMessage().deserialize(messageFormat.format(""));
    }

    /**
     * Gets a translated component from a translatable component and locale.
     *
     * @param component a translatable component
     * @param locale    a locale
     * @return a translated component or {@code null} to use {@link GreenTranslationRegistry#translate(Locale, String)} instead (if available)
     */
    @Deprecated
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        MessageFormat format = translate(locale, component.key());

        if (format != null) {
            String[] args = component.args().stream().map(s -> MiniMessage.miniMessage().serialize(s)).toArray(String[]::new);

            return MiniMessage.miniMessage().deserialize(format.format(args, new StringBuffer(), new FieldPosition(0)).toString());
        } else {
            if (component.fallback() != null) {
                return MiniMessage.miniMessage().deserialize(component.fallback());

            }
            return null;
        }
    }

    public boolean shouldUsePlayerLocale() {
        return shouldUsePlayerLocale;
    }

    public void setShouldUsePlayerLocale(boolean shouldUsePlayerLocale) {
        this.shouldUsePlayerLocale = shouldUsePlayerLocale;
    }

    public enum PlaceHolders {
    }
}
