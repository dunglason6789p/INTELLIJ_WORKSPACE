package com.github.jcrfsuite.util;

public class OSInfo {
   public OSInfo() {
   }

   public static void main(String[] args) {
      if (args.length >= 1) {
         if ("--os".equals(args[0])) {
            System.out.print(getOSName());
            return;
         }

         if ("--arch".equals(args[0])) {
            System.out.print(getArchName());
            return;
         }
      }

      System.out.print(getNativeLibFolderPathForCurrentOS());
   }

   public static String getNativeLibFolderPathForCurrentOS() {
      return getOSName() + "/" + getArchName();
   }

   public static String getOSName() {
      return translateOSNameToFolderName(System.getProperty("os.name"));
   }

   public static String getArchName() {
      return translateArchNameToFolderName(System.getProperty("os.arch"));
   }

   public static String translateOSNameToFolderName(String osName) {
      if (osName.contains("Windows")) {
         return "Windows";
      } else if (osName.contains("Mac")) {
         return "Mac";
      } else {
         return osName.contains("Linux") ? "Linux" : osName.replaceAll("\\W", "");
      }
   }

   public static String translateArchNameToFolderName(String archName) {
      return archName.replaceAll("\\W", "");
   }
}
