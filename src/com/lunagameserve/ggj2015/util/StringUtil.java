package com.lunagameserve.ggj2015.util;

/**
 * Created by Six on 1/24/2015.
 */
public class StringUtil {

    /**
     * Reads all characters from the start of a {@link String} until
     * a specified {@code char} is reached, then returns a {@link String}
     * consisting of all {@code char}s read before the specified {@code char}.
     * <br>
     * For example:
     * <p>
     *     {@code readUntil("abcde", 'd') => "abc"}
     * </p>
     * @param s The {@link String} to read {@code char}s from.
     * @param c The {@code char} to stop reading at.
     * @return A {@link String} containing all {@code char}s from the
     *         beginning of {@code s} until the first {@code char} matching
     *         {@code c}.
     */
    public static String readUntil(String s, char c) {
        return s.substring(0, s.indexOf(c));
    }

    /**
     * Reads all characters from the first occurrence of the instance of a
     * specified {@code char} from a {@link String} until the end of the
     * {@link String}.
     * <br>
     * For example:
     * <p>
     *     {@code readAfter("abcde", 'c') => "de"}
     * </p>
     * @param s The {@link String} to read {@code char}s from.
     * @param c The {@code char} to start reading at.
     * @return A {@link String} containing all {@code char}s from the
     *         first occurrence of {@code c} until the end of {@code s}.
     */
    public static String readAfter(String s, char c) {
        return s.substring(s.indexOf(c) + 1, s.length());
    }
}
