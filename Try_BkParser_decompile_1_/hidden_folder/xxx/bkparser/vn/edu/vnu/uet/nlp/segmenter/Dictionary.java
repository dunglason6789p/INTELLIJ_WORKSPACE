package vn.edu.vnu.uet.nlp.segmenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

public class Dictionary {
   private static Set<String> dict;
   private static String path = "dictionary/VNDictObject";

   public Dictionary() {
   }

   private static void getInstance() {
      dict = new HashSet();
      FileInputStream fin = null;

      try {
         fin = new FileInputStream(path);
      } catch (FileNotFoundException var6) {
         var6.printStackTrace();
      }

      ObjectInputStream ois = null;

      try {
         ois = new ObjectInputStream(fin);
      } catch (IOException var5) {
         var5.printStackTrace();
      }

      try {
         dict = (Set)ois.readObject();
      } catch (IOException | ClassNotFoundException var4) {
         var4.printStackTrace();
      }

      try {
         ois.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public static boolean inVNDict(String word) {
      if (word != null && !word.isEmpty()) {
         if (dict == null) {
            getInstance();
         }

         return dict.contains(word.trim().toLowerCase());
      } else {
         return false;
      }
   }

   public static void setPath(String _path) {
      path = _path;
   }
}
