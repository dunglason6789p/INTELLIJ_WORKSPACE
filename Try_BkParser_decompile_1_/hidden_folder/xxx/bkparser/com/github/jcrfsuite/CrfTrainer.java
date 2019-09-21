package com.github.jcrfsuite;

import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Trainer;

public class CrfTrainer {
   private static final String DEFAULT_ALGORITHM = "lbfgs";
   private static final String DEFAULT_GRAPHICAL_MODEL_TYPE = "crf1d";
   public static final String DEFAULT_ENCODING = "UTF-8";

   public CrfTrainer() {
   }

   public static Pair<List<ItemSequence>, List<StringList>> loadTrainingInstances(String fileName, String encoding) throws IOException {
      List<ItemSequence> xseqs = new ArrayList();
      List<StringList> yseqs = new ArrayList();
      ItemSequence xseq = new ItemSequence();
      StringList yseq = new StringList();
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
      Throwable var7 = null;

      try {
         String line;
         while((line = br.readLine()) != null) {
            if (line.length() <= 0) {
               xseqs.add(xseq);
               yseqs.add(yseq);
               xseq = new ItemSequence();
               yseq = new StringList();
            } else {
               String[] fields = line.split("\t");
               yseq.add(fields[0]);
               Item item = new Item();

               for(int i = 1; i < fields.length; ++i) {
                  String field = fields[i];
                  String[] colonSplit = field.split(":", 2);
                  if (colonSplit.length == 2) {
                     try {
                        double val = Double.valueOf(colonSplit[1]);
                        item.add(new Attribute(colonSplit[0], val));
                     } catch (NumberFormatException var24) {
                        item.add(new Attribute(field));
                     }
                  } else {
                     item.add(new Attribute(field));
                  }
               }

               xseq.add(item);
            }
         }

         if (!xseq.isEmpty()) {
            xseqs.add(xseq);
            yseqs.add(yseq);
         }
      } catch (Throwable var25) {
         var7 = var25;
         throw var25;
      } finally {
         if (br != null) {
            if (var7 != null) {
               try {
                  br.close();
               } catch (Throwable var23) {
                  var7.addSuppressed(var23);
               }
            } else {
               br.close();
            }
         }

      }

      return new Pair(xseqs, yseqs);
   }

   public static void train(String fileName, String modelFile) throws IOException {
      train(fileName, modelFile, "lbfgs", "crf1d", "UTF-8");
   }

   public static void train(String fileName, String modelFile, String encoding) throws IOException {
      train(fileName, modelFile, "lbfgs", "crf1d", encoding);
   }

   public static void train(String fileName, String modelFile, String algorithm, String graphicalModelType, String encoding, Pair<String, String>... parameters) throws IOException {
      Pair<List<ItemSequence>, List<StringList>> trainingData = loadTrainingInstances(fileName, encoding);
      List<ItemSequence> xseqs = (List)trainingData.first;
      List<StringList> yseqs = (List)trainingData.second;
      train(xseqs, yseqs, modelFile, algorithm, graphicalModelType, parameters);
   }

   public static void train(List<ItemSequence> xseqs, List<StringList> yseqs, String modelFile) {
      train(xseqs, yseqs, modelFile, "lbfgs", "crf1d");
   }

   public static void train(List<ItemSequence> xseqs, List<StringList> yseqs, String modelFile, String algorithm, String graphicalModelType, Pair<String, String>... parameters) {
      Trainer trainer = new Trainer();
      int n = xseqs.size();

      for(int i = 0; i < n; ++i) {
         trainer.append((ItemSequence)xseqs.get(i), (StringList)yseqs.get(i), 0);
      }

      trainer.select(algorithm, graphicalModelType);
      int i;
      if (parameters != null) {
         Pair[] var12 = parameters;
         i = parameters.length;

         for(int var10 = 0; var10 < i; ++var10) {
            Pair<String, String> attribute = var12[var10];
            trainer.set((String)attribute.first, (String)attribute.second);
         }
      }

      StringList params = trainer.params();

      for(i = 0; (long)i < params.size(); ++i) {
         String param = params.get(i);
         System.out.printf("%s, %s, %s\n", param, trainer.get(param), trainer.help(param));
      }

      trainer.train(modelFile, -1);
   }

   static {
      try {
         CrfSuiteLoader.load();
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }
}
