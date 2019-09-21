package vn.edu.vnu.uet.nlp.utils;

import java.util.Date;

public class OldLogging {
   public OldLogging() {
   }

   private static void log(String mes, String type) {
      String[] lines = mes.split("\\r?\\n");
      String[] var3 = lines;
      int var4 = lines.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String line = var3[var5];
         if (!line.isEmpty()) {
            if (type.equals("error")) {
               System.err.println(new Date() + " : " + type.toUpperCase() + " : " + line);
            } else {
               System.out.println(new Date() + " : " + type.toUpperCase() + " : " + line);
            }
         }
      }

   }

   public static void info(String mes) {
      log(mes, "info");
   }

   public static void error(String mes) {
      log(mes, "error");
   }
}
