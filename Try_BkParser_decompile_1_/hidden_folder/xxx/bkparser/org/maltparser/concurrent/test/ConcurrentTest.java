package org.maltparser.concurrent.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public final class ConcurrentTest {
   public ConcurrentTest() {
   }

   public static String getMessageWithElapsed(String message, long startTime) {
      StringBuilder sb = new StringBuilder();
      long elapsed = (System.nanoTime() - startTime) / 1000000L;
      sb.append(message);
      sb.append(" : ");
      sb.append(String.format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L));
      sb.append(" ( ");
      sb.append(elapsed);
      sb.append(" ms)");
      return sb.toString();
   }

   public static void main(String[] args) {
      long startTime = System.nanoTime();
      if (args.length != 1) {
         System.out.println("No experiment file.");
      }

      List experiments = null;

      try {
         experiments = Experiment.loadExperiments(args[0]);
      } catch (IOException var14) {
         var14.printStackTrace();
      } catch (ExperimentException var15) {
         var15.printStackTrace();
      }

      if (experiments == null) {
         System.out.println("No experiments to process.");
         System.exit(0);
      }

      try {
         ConcurrentMaltParserModel[] models = new ConcurrentMaltParserModel[experiments.size()];
         int nThreads = 0;

         for(int i = 0; i < experiments.size(); ++i) {
            Experiment experiment = (Experiment)experiments.get(i);
            nThreads += experiment.nInURLs();
            models[i] = ConcurrentMaltParserService.initializeParserModel(experiment.getModelURL());
         }

         System.out.println(getMessageWithElapsed("Finished loading models", startTime));
         Thread[] threads = new Thread[nThreads];
         int t = 0;

         int i;
         for(i = 0; i < experiments.size(); ++i) {
            Experiment experiment = (Experiment)experiments.get(i);
            List<URL> inUrls = experiment.getInURLs();
            List<File> outFiles = experiment.getOutFiles();

            for(int j = 0; j < inUrls.size(); ++j) {
               threads[t] = new ThreadClass(experiment.getCharSet(), (URL)inUrls.get(j), (File)outFiles.get(j), models[i]);
               ++t;
            }
         }

         System.out.println(getMessageWithElapsed("Finished init threads", startTime));

         for(i = 0; i < threads.length; ++i) {
            if (threads[i] != null) {
               threads[i].start();
            } else {
               System.out.println("Thread " + i + " is null");
            }
         }

         for(i = 0; i < threads.length; ++i) {
            try {
               if (threads[i] != null) {
                  threads[i].join();
               } else {
                  System.out.println("Thread " + i + " is null");
               }
            } catch (InterruptedException var13) {
            }
         }

         System.out.println(getMessageWithElapsed("Finished parsing", startTime));
      } catch (MaltChainedException var16) {
         var16.printStackTrace();
      }

   }
}
