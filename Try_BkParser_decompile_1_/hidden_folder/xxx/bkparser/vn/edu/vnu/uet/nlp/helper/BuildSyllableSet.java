package vn.edu.vnu.uet.nlp.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BuildSyllableSet {
   private static Map<String, String> normalizationMap = new HashMap();
   private static Set<String> normalizationSet;

   public BuildSyllableSet() {
   }

   public static void main(String[] args) throws IOException {
      Path file = Paths.get("/home/tuanphong94/workspace/dictionary/VNsyl.txt");
      BufferedReader br = Files.newBufferedReader(file, Charset.forName("utf-8"));
      HashSet set = new HashSet();

      String line;
      while((line = br.readLine()) != null) {
         line = line.replace(" ", " ");
         line = normalize(line.toLowerCase());
         set.add(line.trim());
      }

      System.out.println(set.size());
      String path = "dictionary/VNsylObject";
      Path filePath = Paths.get(path);
      BufferedWriter obj = Files.newBufferedWriter(filePath, Charset.forName("utf-8"), StandardOpenOption.CREATE);
      obj.close();
      FileOutputStream fout = new FileOutputStream(path);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(set);
      oos.close();
   }

   public static String normalize(String token) {
      Iterator var1 = normalizationSet.iterator();

      while(var1.hasNext()) {
         String regex = (String)var1.next();
         if (token.contains(regex)) {
            token = token.replace(regex, (CharSequence)normalizationMap.get(regex));
            break;
         }
      }

      return token;
   }

   static {
      normalizationMap.put("òa", "oà");
      normalizationMap.put("óa", "oá");
      normalizationMap.put("ỏa", "oả");
      normalizationMap.put("õa", "oã");
      normalizationMap.put("ọa", "oạ");
      normalizationMap.put("òe", "oè");
      normalizationMap.put("óe", "oé");
      normalizationMap.put("ỏe", "oẻ");
      normalizationMap.put("õe", "oẽ");
      normalizationMap.put("ọe", "oẹ");
      normalizationMap.put("ùy", "uỳ");
      normalizationMap.put("úy", "uý");
      normalizationMap.put("ủy", "uỷ");
      normalizationMap.put("ũy", "uỹ");
      normalizationMap.put("ụy", "uỵ");
      normalizationMap.put("Ủy", "Uỷ");
      normalizationSet = normalizationMap.keySet();
   }
}
