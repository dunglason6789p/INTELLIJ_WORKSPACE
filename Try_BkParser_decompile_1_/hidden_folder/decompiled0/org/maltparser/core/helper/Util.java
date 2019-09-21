/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;

public class Util {
    private static final int BUFFER = 4096;
    private static final char AMP_CHAR = '&';
    private static final char LT_CHAR = '<';
    private static final char GT_CHAR = '>';
    private static final char QUOT_CHAR = '\"';
    private static final char APOS_CHAR = '\'';

    public static String xmlEscape(String str) {
        char c;
        boolean needEscape = false;
        for (int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);
            if (c != '&' && c != '<' && c != '>' && c != '\"' && c != '\'') continue;
            needEscape = true;
            break;
        }
        if (!needEscape) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);
            if (str.charAt(i) == '&') {
                sb.append("&amp;");
                continue;
            }
            if (str.charAt(i) == '<') {
                sb.append("&lt;");
                continue;
            }
            if (str.charAt(i) == '>') {
                sb.append("&gt;");
                continue;
            }
            if (str.charAt(i) == '\"') {
                sb.append("&quot;");
                continue;
            }
            if (str.charAt(i) == '\'') {
                sb.append("&apos;");
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static int simpleTicer(Logger logger, long startTime, int nTicxRow, int inTic, int subject) {
        logger.info(".");
        int tic = inTic + 1;
        if (tic >= nTicxRow) {
            Util.ticInfo(logger, startTime, subject);
            tic = 0;
        }
        return tic;
    }

    public static void startTicer(Logger logger, long startTime, int nTicxRow, int subject) {
        logger.info(".");
        for (int i = 1; i <= nTicxRow; ++i) {
            logger.info(" ");
        }
        Util.ticInfo(logger, startTime, subject);
    }

    public static void endTicer(Logger logger, long startTime, int nTicxRow, int inTic, int subject) {
        for (int i = inTic; i <= nTicxRow; ++i) {
            logger.info(" ");
        }
        Util.ticInfo(logger, startTime, subject);
    }

    private static void ticInfo(Logger logger, long startTime, int subject) {
        logger.info("\t");
        int a = 1000000;
        if (subject != 0) {
            while (subject / a == 0) {
                logger.info(" ");
                a /= 10;
            }
        } else {
            logger.info("      ");
        }
        logger.info(subject);
        logger.info("\t");
        long time = (System.currentTimeMillis() - startTime) / 1000L;
        a = 1000000;
        if (time != 0L) {
            while (time / (long)a == 0L) {
                logger.info(" ");
                a /= 10;
            }
            logger.info(time);
            logger.info("s");
        } else {
            logger.info("      0s");
        }
        logger.info("\t");
        long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000L;
        a = 1000000;
        if (memory != 0L) {
            while (memory / (long)a == 0L) {
                logger.info(" ");
                a /= 10;
            }
            logger.info(memory);
            logger.info("MB\n");
        } else {
            logger.info("      0MB\n");
        }
    }

    public static void copyfile(String source, String destination) throws MaltChainedException {
        try {
            byte[] readBuffer = new byte[4096];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination), 4096);
            int n = 0;
            while ((n = bis.read(readBuffer, 0, 4096)) != -1) {
                bos.write(readBuffer, 0, n);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
        catch (FileNotFoundException e) {
            throw new MaltChainedException("The destination file '" + destination + "' cannot be created when coping the file. ", e);
        }
        catch (IOException e) {
            throw new MaltChainedException("The source file '" + source + "' cannot be copied to destination '" + destination + "'. ", e);
        }
    }

    public static double atof(String s) {
        if (s == null || s.length() < 1) {
            throw new IllegalArgumentException("Can't convert empty string to integer");
        }
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("NaN or Infinity in input: " + s);
        }
        return d;
    }

    public static int atoi(String s) throws NumberFormatException {
        if (s == null || s.length() < 1) {
            throw new IllegalArgumentException("Can't convert empty string to integer");
        }
        if (s.charAt(0) == '+') {
            s = s.substring(1);
        }
        return Integer.parseInt(s);
    }

    public static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        }
        catch (Throwable t) {
            // empty catch block
        }
    }

    public static double[] copyOf(double[] original, int newLength) {
        double[] copy = new double[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    public static int[] copyOf(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    public static boolean equals(double[] a, double[] a2) {
        if (a == a2) {
            return true;
        }
        if (a == null || a2 == null) {
            return false;
        }
        int length = a.length;
        if (a2.length != length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (a[i] == a2[i]) continue;
            return false;
        }
        return true;
    }
}

