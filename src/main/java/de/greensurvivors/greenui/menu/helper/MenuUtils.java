package de.greensurvivors.greenui.menu.helper;

import org.bukkit.Material;

import java.util.regex.Pattern;

public class MenuUtils {
    // the pattern below is taken from Double.valueOf()
    final static String Digits = "(\\p{Digit}+)";
    final static String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final static String Exp = "[eE][+-]?" + Digits;
    final static String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    //"NaN|" +           // "NaN" string
                    //"Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");// Optional trailing "whitespace"
    private static final Pattern FLOAT_PATTERN = Pattern.compile(fpRegex);

    private static final int MAX_INT_CHARS = (int) (Math.log10(Integer.MAX_VALUE) + 1);

    /**
     * Material, if you want to use a save button
     */
    public static Material getSaveMaterial() {
        return Material.CHEST;
    }

    /**
     * Material to indicate additional information
     */
    public static Material getInfoMaterial() {
        return Material.LIGHT;
    }

    /**
     * Material to fill slots without doing anything
     */
    public static Material getFillerMaterial() {
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Material to indicate something or someone gets deleted.
     * Like if you want to remove a value from a string
     */
    public static Material getDestroyMaterial() {
        return Material.TNT;
    }

    /**
     * Material something gets created. like creating a new page
     */
    public static Material getCreateMaterial() {
        return Material.CRAFTING_TABLE;
    }

    /**
     * Material to indicate this MenuItem is currently unnavigable
     */
    public static Material getDisabledMaterial() {
        return Material.BARRIER;
    }

    /**
     * Next / last page and scrolling
     */
    public static Material getPageMaterial() {
        return Material.ARROW;
    }

    /**
     * you want to search for something or filter your input?
     */
    public static Material getFilterMaterial() {
        return Material.SPYGLASS;
    }

    /**
     * Test if a String can safely convert into an integer
     *
     * @param toTest String input
     * @return boolean, is only true if the Sting has the right size and is entirely made of numbers (first char might be a '-')
     */
    public static boolean isInt(String toTest) {
        if (toTest == null) {
            return false;
        }

        int length = toTest.length();
        if (length > MAX_INT_CHARS) { //protection against overflow
            return false;
        } else if (length == 0) { //empty
            return false;
        }

        int i = 0;
        if (toTest.charAt(0) == '-' || toTest.charAt(0) == '+') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = toTest.charAt(i);

            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if a String can safely convert into a double
     *
     * @param toTest String input
     */
    public static boolean isDouble(String toTest) {
        if (toTest == null) {
            return false;
        }

        if (toTest.length() == 0) { //empty
            return false;
        }

        return FLOAT_PATTERN.matcher(toTest).find();
    }

    public enum TwoCraftSlots {
        LEFT(0),
        RIGHT(1),
        RESULT(2);

        private final int id;

        TwoCraftSlots(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum MenuClosingResult {
        STAY_OPEN,
        CLOSE,
        REOPEN_LATER
    }
}
