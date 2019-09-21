package com.github.jcrfsuite.example;

import com.github.jcrfsuite.CrfTrainer;
import java.io.IOException;

public class Train {
   public Train() {
   }

   public static void main(String[] args) throws IOException {
      if (args.length != 2) {
         System.out.println("Usage: " + Train.class.getCanonicalName() + " <train file> <model file>");
         System.exit(1);
      }

      String trainFile = args[0];
      String modelFile = args[1];
      CrfTrainer.train(trainFile, modelFile);
   }
}
