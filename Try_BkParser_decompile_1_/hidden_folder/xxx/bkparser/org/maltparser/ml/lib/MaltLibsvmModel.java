package org.maltparser.ml.lib;

import java.io.Serializable;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class MaltLibsvmModel implements Serializable, MaltLibModel {
   private static final long serialVersionUID = 7526471155622776147L;
   public svm_parameter param;
   public int nr_class;
   public int l;
   public svm_node[][] SV;
   public double[][] sv_coef;
   public double[] rho;
   public int[] label;
   public int[] nSV;
   public int[] start;

   public MaltLibsvmModel(svm_model model, svm_problem problem) {
      this.param = model.param;
      this.nr_class = model.nr_class;
      this.l = model.l;
      this.SV = model.SV;
      this.sv_coef = model.sv_coef;
      this.rho = model.rho;
      this.label = model.label;
      this.nSV = model.nSV;
      this.start = new int[this.nr_class];
      this.start[0] = 0;

      for(int i = 1; i < this.nr_class; ++i) {
         this.start[i] = this.start[i - 1] + this.nSV[i - 1];
      }

   }

   public int[] predict(MaltFeatureNode[] x) {
      double[] dec_values = new double[this.nr_class * (this.nr_class - 1) / 2];
      double[] kvalue = new double[this.l];
      int[] vote = new int[this.nr_class];

      int i;
      for(i = 0; i < this.l; ++i) {
         kvalue[i] = k_function(x, this.SV[i], this.param);
      }

      for(i = 0; i < this.nr_class; ++i) {
         vote[i] = 0;
      }

      int p = 0;

      int si;
      int j;
      for(i = 0; i < this.nr_class; ++i) {
         for(int j = i + 1; j < this.nr_class; ++j) {
            double sum = 0.0D;
            si = this.start[i];
            j = this.start[j];
            int ci = this.nSV[i];
            int cj = this.nSV[j];
            double[] coef1 = this.sv_coef[j - 1];
            double[] coef2 = this.sv_coef[i];

            int k;
            for(k = 0; k < ci; ++k) {
               sum += coef1[si + k] * kvalue[si + k];
            }

            for(k = 0; k < cj; ++k) {
               sum += coef2[j + k] * kvalue[j + k];
            }

            sum -= this.rho[p];
            dec_values[p] = sum;
            int var10002;
            if (dec_values[p] > 0.0D) {
               var10002 = vote[i]++;
            } else {
               var10002 = vote[j]++;
            }

            ++p;
         }
      }

      int[] predictionList = new int[this.nr_class];
      System.arraycopy(this.label, 0, predictionList, 0, this.nr_class);
      si = this.nr_class - 1;

      for(i = 0; i < si; ++i) {
         int iMax = i;

         for(j = i + 1; j < this.nr_class; ++j) {
            if (vote[j] > vote[iMax]) {
               iMax = j;
            }
         }

         if (iMax != i) {
            int tmp = vote[iMax];
            vote[iMax] = vote[i];
            vote[i] = tmp;
            tmp = predictionList[iMax];
            predictionList[iMax] = predictionList[i];
            predictionList[i] = tmp;
         }
      }

      return predictionList;
   }

   public int predict_one(MaltFeatureNode[] x) {
      double[] dec_values = new double[this.nr_class * (this.nr_class - 1) / 2];
      double[] kvalue = new double[this.l];
      int[] vote = new int[this.nr_class];

      int i;
      for(i = 0; i < this.l; ++i) {
         kvalue[i] = k_function(x, this.SV[i], this.param);
      }

      for(i = 0; i < this.nr_class; ++i) {
         vote[i] = 0;
      }

      int p = 0;

      int j;
      for(i = 0; i < this.nr_class; ++i) {
         for(j = i + 1; j < this.nr_class; ++j) {
            double sum = 0.0D;
            int si = this.start[i];
            int sj = this.start[j];
            int ci = this.nSV[i];
            int cj = this.nSV[j];
            double[] coef1 = this.sv_coef[j - 1];
            double[] coef2 = this.sv_coef[i];

            int k;
            for(k = 0; k < ci; ++k) {
               sum += coef1[si + k] * kvalue[si + k];
            }

            for(k = 0; k < cj; ++k) {
               sum += coef2[sj + k] * kvalue[sj + k];
            }

            sum -= this.rho[p];
            dec_values[p] = sum;
            int var10002;
            if (dec_values[p] > 0.0D) {
               var10002 = vote[i]++;
            } else {
               var10002 = vote[j]++;
            }

            ++p;
         }
      }

      j = vote[0];
      int max_index = 0;

      for(i = 1; i < vote.length; ++i) {
         if (vote[i] > j) {
            j = vote[i];
            max_index = i;
         }
      }

      return this.label[max_index];
   }

   static double dot(MaltFeatureNode[] x, svm_node[] y) {
      double sum = 0.0D;
      int xlen = x.length;
      int ylen = y.length;
      int i = 0;
      int j = 0;

      while(i < xlen && j < ylen) {
         if (x[i].index == y[j].index) {
            sum += x[i++].value * y[j++].value;
         } else if (x[i].index > y[j].index) {
            ++j;
         } else {
            ++i;
         }
      }

      return sum;
   }

   static double powi(double base, int times) {
      double tmp = base;
      double ret = 1.0D;

      for(int t = times; t > 0; t /= 2) {
         if (t % 2 == 1) {
            ret *= tmp;
         }

         tmp *= tmp;
      }

      return ret;
   }

   static double k_function(MaltFeatureNode[] x, svm_node[] y, svm_parameter param) {
      switch(param.kernel_type) {
      case 0:
         return dot(x, y);
      case 1:
         return powi(param.gamma * dot(x, y) + param.coef0, param.degree);
      case 2:
         double sum = 0.0D;
         int xlen = x.length;
         int ylen = y.length;
         int i = 0;
         int j = 0;

         while(i < xlen && j < ylen) {
            if (x[i].index == y[j].index) {
               double d = x[i++].value - y[j++].value;
               sum += d * d;
            } else if (x[i].index > y[j].index) {
               sum += y[j].value * y[j].value;
               ++j;
            } else {
               sum += x[i].value * x[i].value;
               ++i;
            }
         }

         while(i < xlen) {
            sum += x[i].value * x[i].value;
            ++i;
         }

         while(j < ylen) {
            sum += y[j].value * y[j].value;
            ++j;
         }

         return Math.exp(-param.gamma * sum);
      case 3:
         return Math.tanh(param.gamma * dot(x, y) + param.coef0);
      case 4:
         return x[(int)y[0].value].value;
      default:
         return 0.0D;
      }
   }

   public int[] getLabels() {
      if (this.label == null) {
         return null;
      } else {
         int[] labels = new int[this.nr_class];

         for(int i = 0; i < this.nr_class; ++i) {
            labels[i] = this.label[i];
         }

         return labels;
      }
   }
}
