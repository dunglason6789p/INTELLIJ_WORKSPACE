package vn.edu.vnu.uet.liblinear;

import java.io.File;
import java.io.IOException;

public class Problem {
   public int l;
   public int n;
   public double[] y;
   public Feature[][] x;
   public double bias;

   public Problem() {
   }

   public static Problem readFromFile(File file, double bias) throws IOException, InvalidInputDataException {
      return Train.readProblem(file, bias);
   }
}
