/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.hust.nlp.preprocess;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isAlpha(String s) {
        Pattern p = Pattern.compile("^[\\p{Alpha}\\s]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isNumeric(String s) {
        Pattern p = Pattern.compile("^[\\p{Digit}\\s\\.]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isAlphanumeric(String s) {
        Pattern p = Pattern.compile("^[\\p{Alnum}\\s\\.]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isPunct(String s) {
        Pattern p = Pattern.compile("^[\\p{Punct}]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isAcronym(String s) {
        Pattern p = Pattern.compile("^[\\p{Upper}]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static boolean isCapitalized(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.equals("");
    }

    public static String join(String[] items, String glue) {
        return StringUtils.join(Arrays.asList(items), glue);
    }

    public static <X> String join(Iterable<X> l, String glue) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (X o : l) {
            if (!first) {
                sb.append(glue);
            } else {
                first = false;
            }
            sb.append(o);
        }
        return sb.toString();
    }
}

