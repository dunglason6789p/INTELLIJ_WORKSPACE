package vn.edu.vnu.uet.nlp.helper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import vn.edu.vnu.uet.nlp.utils.FileUtils;

public class ConvertHieuPXFormat {
   public ConvertHieuPXFormat() {
   }

   public static void main(String[] args) throws IOException {
      for(int i = 1; i <= 5; ++i) {
         String filename = "data/hieupx_data/test" + i + ".iob2.cvt";
         BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile(filename);
         List<String> dataLines = FileUtils.readFile("data/hieupx_data/test" + i + ".iob2");
         StringBuffer sb = new StringBuffer();
         Iterator var6 = dataLines.iterator();

         while(var6.hasNext()) {
            String line = (String)var6.next();
            if (line.isEmpty()) {
               bw.write(sb.toString().trim());
               bw.newLine();
               bw.flush();
               sb = new StringBuffer();
            } else {
               String[] tokens = line.split("\\s+");
               if (!tokens[1].equals("I_W")) {
                  sb.append(" ");
                  sb.append(tokens[0]);
               } else {
                  sb.append("_");
                  sb.append(tokens[0]);
               }
            }
         }
      }

   }
}
