package vn.edu.vnu.uet.nlp.segmenter.measurement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GiapEvaluation {
   private static final String TEXT_DIR = "independent_test";
   private static final String SEG_MODEL_DIR = "independent_test/seg_model";
   private static final String SEG_HUMAN_DIR = "independent_test/seg_human";
   private static int totalHumanCount;
   private static int totalModelCount;
   private static int totalMatchCount;
   private static int totalLines;
   private static int humanCount;
   private static int modelCount;
   private static int matchCount;

   public GiapEvaluation() {
   }

   public static void main(String[] args) {
      long initTime = init();
      System.out.println("Initialize time: " + initTime);
      System.out.println("--------------------\n");
      System.out.println("File\tLines\tHumanCount\tSystemCount\tMatchCount\tPre\tRecall\tF1");
      File textDir = new File("independent_test");
      String[] var4 = textDir.list();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];

         String fileName;
         try {
            fileName = s.substring(0, s.lastIndexOf(46));
         } catch (Exception var10) {
            continue;
         }

         process(fileName);
      }

      double precision = (double)totalMatchCount / (double)totalModelCount;
      double recall = (double)totalMatchCount / (double)totalHumanCount;
      double f1 = 2.0D * precision * recall / (precision + recall);
      System.out.println("Total\t" + totalLines + "\t" + totalHumanCount + "\t" + totalModelCount + "\t" + totalMatchCount + "\t" + precision + "\t" + recall + "\t" + f1);
   }

   private static long init() {
      long start = System.currentTimeMillis();
      totalMatchCount = 0;
      totalModelCount = 0;
      totalHumanCount = 0;
      totalLines = 0;
      return System.currentTimeMillis() - start;
   }

   private static void process(String fileName) {
      try {
         String textPath = "independent_test/seg_model/" + fileName + ".seg";
         BufferedReader reader = new BufferedReader(new FileReader(textPath));
         int lineCount = 0;

         String line;
         ArrayList segmentRes;
         for(segmentRes = new ArrayList(); (line = reader.readLine()) != null; ++lineCount) {
            segmentRes.add(Arrays.asList(line.split("\\s+")));
         }

         reader.close();
         evaluate(segmentRes, fileName);
         double precision = (double)matchCount / (double)modelCount;
         double recall = (double)matchCount / (double)humanCount;
         double f1 = 2.0D * precision * recall / (precision + recall);
         System.out.println(fileName + "\t" + lineCount + "\t" + humanCount + "\t" + modelCount + "\t" + matchCount + "\t" + precision + "\t" + recall + "\t" + f1);
         totalLines += lineCount;
         totalHumanCount += humanCount;
         totalModelCount += modelCount;
         totalMatchCount += matchCount;
      } catch (Exception var12) {
         var12.printStackTrace();
      }

   }

   private static void saveSegment(List<List<String>> seg, String fileName) {
      try {
         StringBuilder res = new StringBuilder();
         Iterator var3 = seg.iterator();

         while(var3.hasNext()) {
            List<String> line = (List)var3.next();
            StringBuilder sb = new StringBuilder();
            Iterator var6 = line.iterator();

            while(var6.hasNext()) {
               String word = (String)var6.next();
               sb.append(word + " ");
            }

            res.append(sb.toString().trim() + "\n");
         }

         String outFilePath = "independent_test/seg_model/" + fileName + ".seg";
         FileWriter writer = new FileWriter(outFilePath);
         writer.write(res.toString());
         writer.close();
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   private static void evaluate(List<List<String>> modelSeg, String fileName) {
      try {
         matchCount = 0;
         modelCount = 0;
         humanCount = 0;
         String humanSeg = "independent_test/seg_human/" + fileName + ".seg";
         BufferedReader reader = new BufferedReader(new FileReader(humanSeg));

         List modelWords;
         String[] humanWords;
         for(Iterator var4 = modelSeg.iterator(); var4.hasNext(); matchCount += getMatchCount(humanWords, modelWords)) {
            modelWords = (List)var4.next();
            String line = reader.readLine();
            if (line == null) {
               break;
            }

            humanWords = line.split("\\s+");
            humanCount += humanWords.length;
            modelCount += modelWords.size();
         }

         reader.close();
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   private static int[] calculateIndex(String[] words) {
      int[] indexArr = new int[words.length];
      int index = 0;

      for(int i = 0; i < words.length; ++i) {
         indexArr[i] = index;
         index += words[i].replace("_", "").length();
      }

      return indexArr;
   }

   public static int[] calculateIndex(List<String> words) {
      int[] indexArr = new int[words.size()];
      int index = 0;

      for(int i = 0; i < words.size(); ++i) {
         indexArr[i] = index;
         index += ((String)words.get(i)).replace("_", "").length();
      }

      return indexArr;
   }

   private static int getMatchCount(String[] humanWords, List<String> modelWords) {
      int[] humanIndex = calculateIndex(humanWords);
      int[] modelIndex = calculateIndex(modelWords);
      int matchCount = 0;
      int i = 0;

      for(int j = 0; i < humanWords.length; ++i) {
         while(j < modelIndex.length - 1 && humanIndex[i] > modelIndex[j]) {
            ++j;
         }

         if (humanIndex[i] == modelIndex[j] && humanWords[i].equals(modelWords.get(j))) {
            ++matchCount;
         }
      }

      return matchCount;
   }
}
