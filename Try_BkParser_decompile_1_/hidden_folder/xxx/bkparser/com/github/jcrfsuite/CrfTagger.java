package com.github.jcrfsuite;

import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Tagger;

public class CrfTagger {
   private final Tagger tagger = new Tagger();

   public CrfTagger(String modelFile) {
      this.tagger.open(modelFile);
   }

   public synchronized List<Pair<String, Double>> tag(ItemSequence xseq) {
      List<Pair<String, Double>> predicted = new ArrayList();
      this.tagger.set(xseq);
      StringList labels = this.tagger.viterbi();

      for(int i = 0; (long)i < labels.size(); ++i) {
         String label = labels.get(i);
         predicted.add(new Pair(label, this.tagger.marginal(label, i)));
      }

      return predicted;
   }

   public List<List<Pair<String, Double>>> tag(String fileName) throws IOException {
      List<List<Pair<String, Double>>> taggedSentences = new ArrayList();
      Pair<List<ItemSequence>, List<StringList>> taggingSequences = CrfTrainer.loadTrainingInstances(fileName, "UTF-8");
      Iterator var4 = ((List)taggingSequences.getFirst()).iterator();

      while(var4.hasNext()) {
         ItemSequence xseq = (ItemSequence)var4.next();
         taggedSentences.add(this.tag(xseq));
      }

      return taggedSentences;
   }

   public List<List<Pair<String, Double>>> tag(String fileName, String encoding) throws IOException {
      List<List<Pair<String, Double>>> taggedSentences = new ArrayList();
      Pair<List<ItemSequence>, List<StringList>> taggingSequences = CrfTrainer.loadTrainingInstances(fileName, encoding);
      Iterator var5 = ((List)taggingSequences.getFirst()).iterator();

      while(var5.hasNext()) {
         ItemSequence xseq = (ItemSequence)var5.next();
         taggedSentences.add(this.tag(xseq));
      }

      return taggedSentences;
   }

   public List<String> getlabels() {
      StringList labels = this.tagger.labels();
      int numLabels = (int)labels.size();
      List<String> result = new ArrayList(numLabels);

      for(int labelIndex = 0; labelIndex < numLabels; ++labelIndex) {
         result.add(labels.get(labelIndex));
      }

      return result;
   }

   static {
      try {
         CrfSuiteLoader.load();
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }
}
