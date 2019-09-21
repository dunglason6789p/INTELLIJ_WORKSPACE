package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import vn.edu.vnu.uet.nlp.tokenizer.Tokenizer;

public class UETSegmenter {
   private SegmentationSystem machine;

   public UETSegmenter() {
      this("models");
   }

   public UETSegmenter(String modelpath) {
      this.machine = null;
      if (this.machine == null) {
         try {
            this.machine = new SegmentationSystem(modelpath);
         } catch (IOException | ClassNotFoundException var3) {
            var3.printStackTrace();
         }
      }

   }

   public String segmentTokenizedText(String str) {
      StringBuffer sb = new StringBuffer();
      List<String> tokens = new ArrayList();
      new ArrayList();
      tokens.addAll(Arrays.asList(str.split("\\s+")));
      List<String> sentences = Tokenizer.joinSentences(tokens);
      Iterator var5 = sentences.iterator();

      while(var5.hasNext()) {
         String sentence = (String)var5.next();
         sb.append(this.machine.segment(sentence));
         sb.append(" ");
      }

      tokens.clear();
      sentences.clear();
      return sb.toString().trim();
   }

   public String segment(String str) {
      StringBuffer sb = new StringBuffer();
      List<String> tokens = new ArrayList();
      Object sentences = new ArrayList();

      try {
         tokens = Tokenizer.tokenize(str);
         sentences = Tokenizer.joinSentences((List)tokens);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      Iterator var5 = ((List)sentences).iterator();

      while(var5.hasNext()) {
         String sentence = (String)var5.next();
         sb.append(this.machine.segment(sentence));
         sb.append(" ");
      }

      ((List)tokens).clear();
      ((List)sentences).clear();
      return sb.toString().trim();
   }

   public List<String> segmentSentences(String corpus) {
      List<String> result = new ArrayList();
      List<String> tokens = new ArrayList();
      Object sentences = new ArrayList();

      try {
         tokens = Tokenizer.tokenize(corpus);
         sentences = Tokenizer.joinSentences((List)tokens);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      Iterator var5 = ((List)sentences).iterator();

      while(var5.hasNext()) {
         String sentence = (String)var5.next();
         result.add(this.machine.segment(sentence));
      }

      ((List)tokens).clear();
      ((List)sentences).clear();
      return result;
   }

   public void setR(double r) {
      this.machine.setR(r);
   }
}
