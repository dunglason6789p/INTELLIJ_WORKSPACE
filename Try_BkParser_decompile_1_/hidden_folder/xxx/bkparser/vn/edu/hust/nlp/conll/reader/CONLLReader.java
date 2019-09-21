package vn.edu.hust.nlp.conll.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.conll.model.Sentence;

public class CONLLReader {
   private BufferedReader reader;

   public CONLLReader(BufferedReader reader) {
      this.reader = reader;
   }

   public void close() throws IOException {
      this.reader.close();
   }

   public Sentence readSentence() throws IOException {
      ArrayList tokens = new ArrayList();

      String line;
      while((line = this.reader.readLine()) != null) {
         String[] parts = StringUtils.split(line.trim(), '\t');
         if (parts.length == 0) {
            if (!tokens.isEmpty()) {
               return this.constructSentence(tokens);
            }
         } else {
            if (parts.length < 2) {
               throw new IOException(String.format("Line has fewer than two columns: %s", line));
            }

            String id = parts[0];
            String form = this.valueForColumn(parts, 1);
            String lemma = this.valueForColumn(parts, 2);
            String uPOSTag = this.valueForColumn(parts, 3);
            String xPOSTag = this.valueForColumn(parts, 4);
            String feats = this.valueForColumn(parts, 5);
            String head = this.valueForColumn(parts, 6);
            String depRel = this.valueForColumn(parts, 7);
            String deps = this.valueForColumn(parts, 8);
            String misc = this.valueForColumn(parts, 9);
            CONLLToken token = new CONLLToken(id, form, lemma, uPOSTag, xPOSTag, feats, head, depRel, deps, misc);
            tokens.add(token);
         }
      }

      if (!tokens.isEmpty()) {
         return this.constructSentence(tokens);
      } else {
         return null;
      }
   }

   private Sentence constructSentence(List<CONLLToken> tokens) throws IOException {
      try {
         Sentence sentence = new Sentence(tokens);
         return sentence;
      } catch (IllegalArgumentException var4) {
         throw new IOException(var4.getMessage());
      }
   }

   private String valueForColumn(String[] columns, int column) {
      return column >= columns.length ? "_" : columns[column];
   }

   public BufferedReader getReader() {
      return this.reader;
   }

   public void setReader(BufferedReader reader) {
      this.reader = reader;
   }
}
