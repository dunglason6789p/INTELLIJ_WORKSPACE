package vn.edu.vnu.uet.nlp.segmenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

public class FamilyName {
   private static Set<String> nameList;
   private static String path = "dictionary/VNFamilyNameObject";

   public FamilyName() {
   }

   private static void getInstance() {
      nameList = new HashSet();
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
         nameList = (Set)ois.readObject();
      } catch (IOException | ClassNotFoundException var4) {
         var4.printStackTrace();
      }

      try {
         ois.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public static boolean isVNFamilyName(String syl) {
      if (nameList == null) {
         getInstance();
      }

      return nameList.contains(syl.trim().toLowerCase());
   }

   public static void setPath(String _path) {
      path = _path;
   }
}
