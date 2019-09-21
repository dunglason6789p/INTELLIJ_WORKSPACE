package org.maltparser.ml.lib;

import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.maltparser.core.helper.Util;

public class MaltLiblinearModel implements Serializable, MaltLibModel {
   private static final long serialVersionUID = 7526471155622776147L;
   private static final Charset FILE_CHARSET = Charset.forName("ISO-8859-1");
   private double bias;
   private int[] labels;
   private int nr_class;
   private int nr_feature;
   private SolverType solverType;
   private double[][] w;

   public MaltLiblinearModel(int[] labels, int nr_class, int nr_feature, double[][] w, SolverType solverType) {
      this.labels = labels;
      this.nr_class = nr_class;
      this.nr_feature = nr_feature;
      this.w = w;
      this.solverType = solverType;
   }

   public MaltLiblinearModel(Reader inputReader) throws IOException {
      this.loadModel(inputReader);
   }

   public MaltLiblinearModel(File modelFile) throws IOException {
      BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), FILE_CHARSET));
      this.loadModel(inputReader);
   }

   public int getNrClass() {
      return this.nr_class;
   }

   public int getNrFeature() {
      return this.nr_feature;
   }

   public int[] getLabels() {
      return Util.copyOf(this.labels, this.nr_class);
   }

   public boolean isProbabilityModel() {
      return this.solverType == SolverType.L2R_LR || this.solverType == SolverType.L2R_LR_DUAL || this.solverType == SolverType.L1R_LR;
   }

   public double getBias() {
      return this.bias;
   }

   public int[] predict(MaltFeatureNode[] x) {
      double[] dec_values = new double[this.nr_class];
      int n = this.bias >= 0.0D ? this.nr_feature + 1 : this.nr_feature;
      int xlen = x.length;

      int tmpObj;
      for(int i = 0; i < xlen; ++i) {
         if (x[i].index <= n) {
            int t = x[i].index - 1;
            if (this.w[t] != null) {
               for(tmpObj = 0; tmpObj < this.w[t].length; ++tmpObj) {
                  dec_values[tmpObj] += this.w[t][tmpObj] * x[i].value;
               }
            }
         }
      }

      int[] predictionList = new int[this.nr_class];
      System.arraycopy(this.labels, 0, predictionList, 0, this.nr_class);
      int nc = this.nr_class - 1;

      for(int i = 0; i < nc; ++i) {
         int iMax = i;

         for(int j = i + 1; j < this.nr_class; ++j) {
            if (dec_values[j] > dec_values[iMax]) {
               iMax = j;
            }
         }

         if (iMax != i) {
            double tmpDec = dec_values[iMax];
            dec_values[iMax] = dec_values[i];
            dec_values[i] = tmpDec;
            tmpObj = predictionList[iMax];
            predictionList[iMax] = predictionList[i];
            predictionList[i] = tmpObj;
         }
      }

      return predictionList;
   }

   public int predict_one(MaltFeatureNode[] x) {
      double[] dec_values = new double[this.nr_class];
      int n = this.bias >= 0.0D ? this.nr_feature + 1 : this.nr_feature;
      int xlen = x.length;

      int j;
      for(int i = 0; i < xlen; ++i) {
         if (x[i].index <= n) {
            int t = x[i].index - 1;
            if (this.w[t] != null) {
               for(j = 0; j < this.w[t].length; ++j) {
                  dec_values[j] += this.w[t][j] * x[i].value;
               }
            }
         }
      }

      double max = dec_values[0];
      j = 0;

      for(int i = 1; i < dec_values.length; ++i) {
         if (dec_values[i] > max) {
            max = dec_values[i];
            j = i;
         }
      }

      return this.labels[j];
   }

   private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
      is.defaultReadObject();
   }

   private void writeObject(ObjectOutputStream os) throws IOException {
      os.defaultWriteObject();
   }

   private void loadModel(Reader inputReader) throws IOException {
      this.labels = null;
      Pattern whitespace = Pattern.compile("\\s+");
      BufferedReader reader = null;
      if (inputReader instanceof BufferedReader) {
         reader = (BufferedReader)inputReader;
      } else {
         reader = new BufferedReader(inputReader);
      }

      try {
         String line = null;

         int i;
         label179:
         while(true) {
            while(true) {
               if ((line = reader.readLine()) == null) {
                  break label179;
               }

               String[] split = whitespace.split(line);
               if (split[0].equals("solver_type")) {
                  SolverType solver = SolverType.valueOf(split[1]);
                  if (solver == null) {
                     throw new RuntimeException("unknown solver type");
                  }

                  this.solverType = solver;
               } else if (split[0].equals("nr_class")) {
                  this.nr_class = Util.atoi(split[1]);
                  Integer.parseInt(split[1]);
               } else if (split[0].equals("nr_feature")) {
                  this.nr_feature = Util.atoi(split[1]);
               } else if (split[0].equals("bias")) {
                  this.bias = Util.atof(split[1]);
               } else {
                  if (split[0].equals("w")) {
                     break label179;
                  }

                  if (!split[0].equals("label")) {
                     throw new RuntimeException("unknown text in model file: [" + line + "]");
                  }

                  this.labels = new int[this.nr_class];

                  for(i = 0; i < this.nr_class; ++i) {
                     this.labels[i] = Util.atoi(split[i + 1]);
                  }
               }
            }
         }

         int w_size = this.nr_feature;
         if (this.bias >= 0.0D) {
            ++w_size;
         }

         i = this.nr_class;
         if (this.nr_class == 2 && this.solverType != SolverType.MCSVM_CS) {
            i = 1;
         }

         this.w = new double[w_size][i];
         int[] buffer = new int[128];

         for(int i = 0; i < w_size; ++i) {
            for(int j = 0; j < i; ++j) {
               int b = 0;

               while(true) {
                  int ch = reader.read();
                  if (ch == -1) {
                     throw new EOFException("unexpected EOF");
                  }

                  if (ch == 32) {
                     this.w[i][j] = Util.atof(new String(buffer, 0, b));
                     break;
                  }

                  buffer[b++] = ch;
               }
            }
         }
      } finally {
         Util.closeQuietly(reader);
      }

   }

   public int hashCode() {
      int prime = true;
      long temp = Double.doubleToLongBits(this.bias);
      int result = 31 + (int)(temp ^ temp >>> 32);
      result = 31 * result + Arrays.hashCode(this.labels);
      result = 31 * result + this.nr_class;
      result = 31 * result + this.nr_feature;
      result = 31 * result + (this.solverType == null ? 0 : this.solverType.hashCode());

      for(int i = 0; i < this.w.length; ++i) {
         result = 31 * result + Arrays.hashCode(this.w[i]);
      }

      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MaltLiblinearModel other = (MaltLiblinearModel)obj;
         if (Double.doubleToLongBits(this.bias) != Double.doubleToLongBits(other.bias)) {
            return false;
         } else if (!Arrays.equals(this.labels, other.labels)) {
            return false;
         } else if (this.nr_class != other.nr_class) {
            return false;
         } else if (this.nr_feature != other.nr_feature) {
            return false;
         } else {
            if (this.solverType == null) {
               if (other.solverType != null) {
                  return false;
               }
            } else if (!this.solverType.equals(other.solverType)) {
               return false;
            }

            for(int i = 0; i < this.w.length; ++i) {
               if (other.w.length <= i) {
                  return false;
               }

               if (!Util.equals(this.w[i], other.w[i])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("Model");
      sb.append(" bias=").append(this.bias);
      sb.append(" nr_class=").append(this.nr_class);
      sb.append(" nr_feature=").append(this.nr_feature);
      sb.append(" solverType=").append(this.solverType);
      return sb.toString();
   }
}
