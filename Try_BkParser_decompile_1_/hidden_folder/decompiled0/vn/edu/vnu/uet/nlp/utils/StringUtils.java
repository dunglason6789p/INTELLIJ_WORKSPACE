/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.utils;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static void testFoundByRegex(String s, String regex) {
        System.out.println("Test string: " + s);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            System.out.println(s.substring(0, matcher.start()));
            System.out.println(s.substring(matcher.start(), matcher.end()));
            System.out.println(s.substring(matcher.end()));
        }
    }

    public static String char2Hex(Character c) {
        return String.format("\\u%04x", c.charValue());
    }

    public static Character hex2Char(String hex) {
        int hexToInt = Integer.parseInt(hex.substring(2), 16);
        return Character.valueOf((char)hexToInt);
    }

    public static boolean hasPunctuation(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isLetterOrDigit(s.charAt(i))) continue;
            return true;
        }
        return false;
    }

    public static boolean isBrace(String string) {
        return string.equals("\"") || string.equals("\ufffd") || string.equals("'") || string.equals(")") || string.equals("}") || string.equals("]");
    }
}

