package de.bwaldvogel.liblinear;

import java.io.File;
import java.io.IOException;

public class Problem {
   public int l;
   public int n;
   public int[] y;
   public FeatureNode[][] x;
   public double bias;

   public Problem() {
   }

   public static Problem readFromFile(File file, double bias) throws IOException, InvalidInputDataException {
      return Train.readProblem(file, bias);
   }
}
