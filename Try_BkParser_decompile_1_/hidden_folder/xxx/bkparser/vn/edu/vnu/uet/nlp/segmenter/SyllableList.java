package vn.edu.vnu.uet.nlp.segmenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

public class SyllableList {
   private static Set<String> sylList;
   private static String path = "dictionary/VNsylObject";

   public SyllableList() {
   }

   private static void getInstance() {
      sylList = new HashSet();
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
         sylList = (Set)ois.readObject();
      } catch (IOException | ClassNotFoundException var4) {
         var4.printStackTrace();
      }

      try {
         ois.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public static boolean isVNsyl(String syl) {
      if (sylList == null) {
         getInstance();
      }

      return sylList.contains(syl.trim().toLowerCase());
   }

   public static void setPath(String _path) {
      path = _path;
   }
}
