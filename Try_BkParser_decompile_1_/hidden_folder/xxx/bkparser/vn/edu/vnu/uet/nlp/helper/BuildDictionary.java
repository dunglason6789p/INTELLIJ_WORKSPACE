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
import java.util.HashSet;

public class BuildDictionary {
   public BuildDictionary() {
   }

   public static void main(String[] args) throws IOException {
      Path file = Paths.get("/home/tuanphong94/workspace/dictionary/original_word_list.txt");
      BufferedReader br = Files.newBufferedReader(file, Charset.forName("utf-8"));
      HashSet set = new HashSet();

      String line;
      while((line = br.readLine()) != null) {
         line = line.replace(" ", " ");
         String[] tokens = line.split("\\s+");
         line = "";
         String[] var6 = tokens;
         int var7 = tokens.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String token = var6[var8];
            line = line + BuildSyllableSet.normalize(token.toLowerCase()) + " ";
         }

         set.add(line.trim().toLowerCase());
      }

      System.out.println(set.size());
      String path = "dictionary/VNDictObject";
      Path filePath = Paths.get(path);
      BufferedWriter obj = Files.newBufferedWriter(filePath, Charset.forName("utf-8"), StandardOpenOption.CREATE);
      obj.close();
      FileOutputStream fout = new FileOutputStream(path);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(set);
      oos.close();
   }
}
