/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.utils;

import java.io.PrintStream;
import java.util.Date;

public class OldLogging {
    private static void log(String mes, String type) {
        String[] lines;
        for (String line : lines = mes.split("\\r?\\n")) {
            if (line.isEmpty()) continue;
            if (type.equals("error")) {
                System.err.println(new Date() + " : " + type.toUpperCase() + " : " + line);
                continue;
            }
            System.out.println(new Date() + " : " + type.toUpperCase() + " : " + line);
        }
    }

    public static void info(String mes) {
        OldLogging.log(mes, "info");
    }

    public static void error(String mes) {
        OldLogging.log(mes, "error");
    }
}

