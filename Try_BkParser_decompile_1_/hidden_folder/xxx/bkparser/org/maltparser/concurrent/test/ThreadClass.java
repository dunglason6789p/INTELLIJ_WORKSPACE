package org.maltparser.concurrent.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.core.exception.MaltChainedException;

public class ThreadClass extends Thread {
   private URL inURL;
   private File outFile;
   private String charSet;
   private ConcurrentMaltParserModel model;

   public ThreadClass(String _charSet, String _inFile, String _outFile, ConcurrentMaltParserModel _model) throws MalformedURLException {
      this.charSet = _charSet;
      this.inURL = (new File(_inFile)).toURI().toURL();
      this.outFile = new File(_outFile);
      this.model = _model;
   }

   public ThreadClass(String _charSet, URL _inUrl, File _outFile, ConcurrentMaltParserModel _model) {
      this.charSet = _charSet;
      this.inURL = _inUrl;
      this.outFile = _outFile;
      this.model = _model;
   }

   public void run() {
      BufferedReader reader = null;
      BufferedWriter writer = null;

      try {
         reader = new BufferedReader(new InputStreamReader(this.inURL.openStream(), this.charSet));
         writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outFile), this.charSet));
         int diffCount = 0;
         int sentenceCount = 0;

         while(true) {
            String[] goldTokens = ConcurrentUtils.readSentence(reader);
            if (goldTokens.length == 0) {
               System.out.println("DiffCount: " + diffCount + "/" + sentenceCount + "(ThreadID:" + Thread.currentThread().getId() + ")");
               break;
            }

            String[] inputTokens = ConcurrentUtils.stripGold(goldTokens);
            String[] outputTokens = this.model.parseTokens(inputTokens);
            diffCount = ConcurrentUtils.diffSentences(goldTokens, outputTokens) ? diffCount + 1 : diffCount;
            ++sentenceCount;
            ConcurrentUtils.writeSentence(outputTokens, writer);
         }
      } catch (IOException var23) {
         var23.printStackTrace();
      } catch (MaltChainedException var24) {
         var24.printStackTrace();
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException var22) {
               var22.printStackTrace();
            }
         }

         if (writer != null) {
            try {
               writer.close();
            } catch (IOException var21) {
               var21.printStackTrace();
            }
         }

      }

   }
}
