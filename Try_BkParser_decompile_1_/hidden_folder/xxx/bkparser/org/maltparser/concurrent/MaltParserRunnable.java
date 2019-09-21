package org.maltparser.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;

public class MaltParserRunnable implements Runnable {
   private final List<String[]> inputSentences;
   private List<String[]> outputSentences;
   private final ConcurrentMaltParserModel model;

   public MaltParserRunnable(List<String[]> sentences, ConcurrentMaltParserModel _model) {
      this.inputSentences = new ArrayList(sentences);
      this.outputSentences = null;
      this.model = _model;
   }

   public void run() {
      try {
         this.outputSentences = this.model.parseSentences(this.inputSentences);
      } catch (MaltChainedException var2) {
         var2.printStackTrace();
      }

   }

   public List<String[]> getOutputSentences() {
      return this.outputSentences == null ? Collections.synchronizedList(new ArrayList()) : Collections.synchronizedList(new ArrayList(this.outputSentences));
   }
}
