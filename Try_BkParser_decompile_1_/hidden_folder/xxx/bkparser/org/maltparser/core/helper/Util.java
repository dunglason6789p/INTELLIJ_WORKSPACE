package org.maltparser.core.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;

public class Util {
   private static final int BUFFER = 4096;
   private static final char AMP_CHAR = '&';
   private static final char LT_CHAR = '<';
   private static final char GT_CHAR = '>';
   private static final char QUOT_CHAR = '"';
   private static final char APOS_CHAR = '\'';

   public Util() {
   }

   public static String xmlEscape(String str) {
      boolean needEscape = false;

      char c;
      for(int i = 0; i < str.length(); ++i) {
         c = str.charAt(i);
         if (c == '&' || c == '<' || c == '>' || c == '"' || c == '\'') {
            needEscape = true;
            break;
         }
      }

      if (!needEscape) {
         return str;
      } else {
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);
            if (str.charAt(i) == '&') {
               sb.append("&amp;");
            } else if (str.charAt(i) == '<') {
               sb.append("&lt;");
            } else if (str.charAt(i) == '>') {
               sb.append("&gt;");
            } else if (str.charAt(i) == '"') {
               sb.append("&quot;");
            } else if (str.charAt(i) == '\'') {
               sb.append("&apos;");
            } else {
               sb.append(c);
            }
         }

         return sb.toString();
      }
   }

   public static int simpleTicer(Logger logger, long startTime, int nTicxRow, int inTic, int subject) {
      logger.info(".");
      int tic = inTic + 1;
      if (tic >= nTicxRow) {
         ticInfo(logger, startTime, subject);
         tic = 0;
      }

      return tic;
   }

   public static void startTicer(Logger logger, long startTime, int nTicxRow, int subject) {
      logger.info(".");

      for(int i = 1; i <= nTicxRow; ++i) {
         logger.info(" ");
      }

      ticInfo(logger, startTime, subject);
   }

   public static void endTicer(Logger logger, long startTime, int nTicxRow, int inTic, int subject) {
      for(int i = inTic; i <= nTicxRow; ++i) {
         logger.info(" ");
      }

      ticInfo(logger, startTime, subject);
   }

   private static void ticInfo(Logger logger, long startTime, int subject) {
      logger.info("\t");
      int a = 1000000;
      if (subject != 0) {
         while(subject / a == 0) {
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
      if (time == 0L) {
         logger.info("      0s");
      } else {
         while(time / (long)a == 0L) {
            logger.info(" ");
            a /= 10;
         }

         logger.info(time);
         logger.info("s");
      }

      logger.info("\t");
      long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000L;
      a = 1000000;
      if (memory == 0L) {
         logger.info("      0MB\n");
      } else {
         while(memory / (long)a == 0L) {
            logger.info(" ");
            a /= 10;
         }

         logger.info(memory);
         logger.info("MB\n");
      }

   }

   public static void copyfile(String source, String destination) throws MaltChainedException {
      try {
         byte[] readBuffer = new byte[4096];
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination), 4096);
         boolean var5 = false;

         int n;
         while((n = bis.read(readBuffer, 0, 4096)) != -1) {
            bos.write(readBuffer, 0, n);
         }

         bos.flush();
         bos.close();
         bis.close();
      } catch (FileNotFoundException var6) {
         throw new MaltChainedException("The destination file '" + destination + "' cannot be created when coping the file. ", var6);
      } catch (IOException var7) {
         throw new MaltChainedException("The source file '" + source + "' cannot be copied to destination '" + destination + "'. ", var7);
      }
   }

   public static double atof(String s) {
      if (s != null && s.length() >= 1) {
         double d = Double.parseDouble(s);
         if (!Double.isNaN(d) && !Double.isInfinite(d)) {
            return d;
         } else {
            throw new IllegalArgumentException("NaN or Infinity in input: " + s);
         }
      } else {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
   }

   public static int atoi(String s) throws NumberFormatException {
      if (s != null && s.length() >= 1) {
         if (s.charAt(0) == '+') {
            s = s.substring(1);
         }

         return Integer.parseInt(s);
      } else {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
   }

   public static void closeQuietly(Closeable c) {
      if (c != null) {
         try {
            c.close();
         } catch (Throwable var2) {
         }

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
      } else if (a != null && a2 != null) {
         int length = a.length;
         if (a2.length != length) {
            return false;
         } else {
            for(int i = 0; i < length; ++i) {
               if (a[i] != a2[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
