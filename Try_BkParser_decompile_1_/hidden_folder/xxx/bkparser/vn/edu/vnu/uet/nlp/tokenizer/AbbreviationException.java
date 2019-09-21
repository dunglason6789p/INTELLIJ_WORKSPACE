package vn.edu.vnu.uet.nlp.tokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import vn.edu.vnu.uet.nlp.utils.FileUtils;

public class AbbreviationException {
   private static HashSet<String> abbreviation = null;
   private static HashSet<String> exception = null;
   private static String abbPath = "dictionary/abbreviation.dic";
   private static String excPath = "dictionary/exception.dic";

   public AbbreviationException() {
   }

   public static HashSet<String> getAbbreviation() throws IOException {
      if (abbreviation == null) {
         abbreviation = new HashSet();
         List<String> abbreviationList = FileUtils.readFile(abbPath);
         Iterator var1 = abbreviationList.iterator();

         while(var1.hasNext()) {
            String s = (String)var1.next();
            abbreviation.add(s);
         }
      }

      return abbreviation;
   }

   public static HashSet<String> getException() throws IOException {
      if (exception == null) {
         exception = new HashSet();
         List<String> exceptionList = FileUtils.readFile(excPath);
         Iterator var1 = exceptionList.iterator();

         while(var1.hasNext()) {
            String s = (String)var1.next();
            exception.add(s);
         }
      }

      return exception;
   }

   public static void setPath(String path) {
      if (!path.endsWith("/")) {
         path = path + "/";
      }

      abbPath = path + "abbreviation.dic";
      excPath = path + "exception.dic";
   }
}
