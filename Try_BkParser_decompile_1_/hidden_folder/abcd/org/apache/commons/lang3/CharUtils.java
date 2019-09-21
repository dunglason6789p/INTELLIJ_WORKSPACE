/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.StringUtils;

public class CharUtils {
    private static final String CHAR_STRING = "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f";
    private static final String[] CHAR_STRING_ARRAY = new String[128];
    private static final Character[] CHAR_ARRAY = new Character[128];
    public static final char LF = '\n';
    public static final char CR = '\r';

    public static Character toCharacterObject(char ch) {
        if (ch < CHAR_ARRAY.length) {
            return CHAR_ARRAY[ch];
        }
        return new Character(ch);
    }

    public static Character toCharacterObject(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return CharUtils.toCharacterObject(str.charAt(0));
    }

    public static char toChar(Character ch) {
        if (ch == null) {
            throw new IllegalArgumentException("The Character must not be null");
        }
        return ch.charValue();
    }

    public static char toChar(Character ch, char defaultValue) {
        if (ch == null) {
            return defaultValue;
        }
        return ch.charValue();
    }

    public static char toChar(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("The String must not be empty");
        }
        return str.charAt(0);
    }

    public static char toChar(String str, char defaultValue) {
        if (StringUtils.isEmpty(str)) {
            return defaultValue;
        }
        return str.charAt(0);
    }

    public static int toIntValue(char ch) {
        if (!CharUtils.isAsciiNumeric(ch)) {
            throw new IllegalArgumentException("The character " + ch + " is not in the range '0' - '9'");
        }
        return ch - 48;
    }

    public static int toIntValue(char ch, int defaultValue) {
        if (!CharUtils.isAsciiNumeric(ch)) {
            return defaultValue;
        }
        return ch - 48;
    }

    public static int toIntValue(Character ch) {
        if (ch == null) {
            throw new IllegalArgumentException("The character must not be null");
        }
        return CharUtils.toIntValue(ch.charValue());
    }

    public static int toIntValue(Character ch, int defaultValue) {
        if (ch == null) {
            return defaultValue;
        }
        return CharUtils.toIntValue(ch.charValue(), defaultValue);
    }

    public static String toString(char ch) {
        if (ch < '\u0080') {
            return CHAR_STRING_ARRAY[ch];
        }
        return new String(new char[]{ch});
    }

    public static String toString(Character ch) {
        if (ch == null) {
            return null;
        }
        return CharUtils.toString(ch.charValue());
    }

    public static String unicodeEscaped(char ch) {
        if (ch < '\u0010') {
            return "\\u000" + Integer.toHexString(ch);
        }
        if (ch < '\u0100') {
            return "\\u00" + Integer.toHexString(ch);
        }
        if (ch < '\u1000') {
            return "\\u0" + Integer.toHexString(ch);
        }
        return "\\u" + Integer.toHexString(ch);
    }

    public static String unicodeEscaped(Character ch) {
        if (ch == null) {
            return null;
        }
        return CharUtils.unicodeEscaped(ch.charValue());
    }

    public static boolean isAscii(char ch) {
        return ch < '\u0080';
    }

    public static boolean isAsciiPrintable(char ch) {
        return ch >= ' ' && ch < '\u007f';
    }

    public static boolean isAsciiControl(char ch) {
        return ch < ' ' || ch == '\u007f';
    }

    public static boolean isAsciiAlpha(char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiAlphaUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isAsciiAlphaLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAsciiAlphanumeric(char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9';
    }

    static {
        for (int i = 127; i >= 0; --i) {
            CharUtils.CHAR_STRING_ARRAY[i] = CHAR_STRING.substring(i, i + 1);
            CharUtils.CHAR_ARRAY[i] = new Character((char)i);
        }
    }
}

