package vn.edu.vnu.uet.nlp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileUtils {
   public static final Charset UNICODE = Charset.forName("utf-8");

   public FileUtils() {
   }

   public static void appendFile(List<String> lines, String pathname) {
      try {
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathname, true)));
         Throwable var3 = null;

         try {
            Iterator var4 = lines.iterator();

            while(var4.hasNext()) {
               String line = (String)var4.next();
               if (!line.isEmpty() && line != null) {
                  out.println(line);
               }
            }
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (out != null) {
               if (var3 != null) {
                  try {
                     out.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  out.close();
               }
            }

         }
      } catch (IOException var16) {
         System.out.println(var16.getMessage());
      }

   }

   public static void truncateFile(String filename) throws IOException {
      Path file = Paths.get(filename);
      Files.newBufferedWriter(file, UNICODE, StandardOpenOption.CREATE);
      Files.newBufferedWriter(file, UNICODE, StandardOpenOption.WRITE);
      BufferedWriter bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.TRUNCATE_EXISTING);
      bf.close();
   }

   public static BufferedWriter newUTF8BufferedWriterFromNewFile(String filename) throws IOException {
      BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
      bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.WRITE);
      bw = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
      return bw;
   }

   public static BufferedReader newUTF8BufferedReaderFromFile(String filename) throws IOException {
      return Files.newBufferedReader(Paths.get(filename), Constants.cs);
   }

   public static List<String> readFile(String filename) throws IOException {
      List<String> list = new ArrayList();
      BufferedReader br = Files.newBufferedReader(Paths.get(filename), Constants.cs);

      String line;
      while((line = br.readLine()) != null) {
         if (!line.trim().isEmpty()) {
            list.add(line.trim());
         }
      }

      return list;
   }
}
