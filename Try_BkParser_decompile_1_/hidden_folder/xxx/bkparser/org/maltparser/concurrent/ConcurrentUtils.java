package org.maltparser.concurrent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ConcurrentUtils {
   public ConcurrentUtils() {
   }

   public static String[] readSentence(BufferedReader reader) throws IOException {
      ArrayList tokens = new ArrayList();

      String line;
      while((line = reader.readLine()) != null && line.trim().length() != 0) {
         tokens.add(line.trim());
      }

      return (String[])tokens.toArray(new String[tokens.size()]);
   }

   public static void writeSentence(String[] inTokens, BufferedWriter writer) throws IOException {
      for(int i = 0; i < inTokens.length; ++i) {
         writer.write(inTokens[i]);
         writer.newLine();
      }

      writer.newLine();
      writer.flush();
   }

   public static String[] stripGold(String[] inTokens) {
      return stripGold(inTokens, 2);
   }

   public static String[] stripGold(String[] inTokens, int stripNumberOfEndingColumns) {
      String[] outTokens = new String[inTokens.length];

      for(int i = 0; i < inTokens.length; ++i) {
         int tabCounter = 0;

         for(int j = inTokens[i].length() - 1; j >= 0; --j) {
            if (inTokens[i].charAt(j) == '\t') {
               ++tabCounter;
            }

            if (tabCounter == stripNumberOfEndingColumns) {
               outTokens[i] = inTokens[i].substring(0, j);
               break;
            }
         }
      }

      return outTokens;
   }

   public static void printTokens(String[] inTokens) {
      printTokens(inTokens, System.out);
   }

   public static void printTokens(String[] inTokens, PrintStream stream) {
      for(int i = 0; i < inTokens.length; ++i) {
         stream.println(inTokens[i]);
      }

      stream.println();
   }

   public static boolean diffSentences(String[] goldTokens, String[] outputTokens) {
      if (goldTokens.length != outputTokens.length) {
         return true;
      } else {
         for(int i = 0; i < goldTokens.length; ++i) {
            if (!goldTokens[i].equals(outputTokens[i])) {
               return true;
            }
         }

         return false;
      }
   }

   public static void simpleEvaluation(List<String[]> goldSentences, List<String[]> parsedSentences, int headColumn, int dependencyLabelColumn, PrintStream stream) {
      if (goldSentences.size() != parsedSentences.size()) {
         stream.println("Number of sentences in gold and output differs");
      } else {
         int nTokens = 0;
         int nCorrectHead = 0;
         int nCorrectLabel = 0;
         int nCorrectBoth = 0;

         for(int i = 0; i < goldSentences.size(); ++i) {
            String[] goldTokens = (String[])goldSentences.get(i);
            String[] parsedTokens = (String[])parsedSentences.get(i);
            if (goldTokens.length != parsedTokens.length) {
               stream.println("Number of tokens in gold and output differs in sentence " + i);
               return;
            }

            for(int j = 0; j < goldTokens.length; ++j) {
               ++nTokens;
               String[] goldColumns = goldTokens[j].split("\t");
               String[] parsedColumns = parsedTokens[j].split("\t");
               if (goldColumns[headColumn].equals(parsedColumns[headColumn])) {
                  ++nCorrectHead;
               }

               if (goldColumns[dependencyLabelColumn].equals(parsedColumns[dependencyLabelColumn])) {
                  ++nCorrectLabel;
               }

               if (goldColumns[headColumn].equals(parsedColumns[headColumn]) && goldColumns[dependencyLabelColumn].equals(parsedColumns[dependencyLabelColumn])) {
                  ++nCorrectBoth;
               }
            }
         }

         stream.format("Labeled   attachment score: %d / %d * 100 = %.2f %%\n", nCorrectBoth, nTokens, (double)((float)nCorrectBoth / (float)nTokens) * 100.0D);
         stream.format("Unlabeled attachment score: %d / %d * 100 = %.2f %%\n", nCorrectHead, nTokens, (double)((float)nCorrectHead / (float)nTokens) * 100.0D);
         stream.format("Label accuracy score:       %d / %d * 100 = %.2f %%\n", nCorrectLabel, nTokens, (double)((float)nCorrectLabel / (float)nTokens) * 100.0D);
      }
   }
}
