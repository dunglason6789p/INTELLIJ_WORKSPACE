package de.bwaldvogel.liblinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public class Linear {
   static final Charset FILE_CHARSET = Charset.forName("ISO-8859-1");
   static final Locale DEFAULT_LOCALE;
   private static Object OUTPUT_MUTEX;
   private static PrintStream DEBUG_OUTPUT;
   static final String NL;
   private static final long DEFAULT_RANDOM_SEED = 0L;
   static Random random;

   public Linear() {
   }

   public static void crossValidation(Problem prob, Parameter param, int nr_fold, int[] target) {
      int[] fold_start = new int[nr_fold + 1];
      int l = prob.l;
      int[] perm = new int[l];

      int i;
      for(i = 0; i < l; perm[i] = i++) {
      }

      int begin;
      for(i = 0; i < l; ++i) {
         begin = i + random.nextInt(l - i);
         swap(perm, i, begin);
      }

      for(i = 0; i <= nr_fold; ++i) {
         fold_start[i] = i * l / nr_fold;
      }

      for(i = 0; i < nr_fold; ++i) {
         begin = fold_start[i];
         int end = fold_start[i + 1];
         Problem subprob = new Problem();
         subprob.bias = prob.bias;
         subprob.n = prob.n;
         subprob.l = l - (end - begin);
         subprob.x = new FeatureNode[subprob.l][];
         subprob.y = new int[subprob.l];
         int k = 0;

         int j;
         for(j = 0; j < begin; ++j) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }

         for(j = end; j < l; ++j) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }

         Model submodel = train(subprob, param);

         for(j = begin; j < end; ++j) {
            target[perm[j]] = predict(submodel, prob.x[perm[j]]);
         }
      }

   }

   private static Linear.GroupClassesReturn groupClasses(Problem prob, int[] perm) {
      int l = prob.l;
      int max_nr_class = 16;
      int nr_class = 0;
      int[] label = new int[max_nr_class];
      int[] count = new int[max_nr_class];
      int[] data_label = new int[l];

      int i;
      for(i = 0; i < l; ++i) {
         int this_label = prob.y[i];

         int j;
         for(j = 0; j < nr_class; ++j) {
            if (this_label == label[j]) {
               int var10002 = count[j]++;
               break;
            }
         }

         data_label[i] = j;
         if (j == nr_class) {
            if (nr_class == max_nr_class) {
               max_nr_class *= 2;
               label = copyOf(label, max_nr_class);
               count = copyOf(count, max_nr_class);
            }

            label[nr_class] = this_label;
            count[nr_class] = 1;
            ++nr_class;
         }
      }

      int[] start = new int[nr_class];
      start[0] = 0;

      for(i = 1; i < nr_class; ++i) {
         start[i] = start[i - 1] + count[i - 1];
      }

      for(i = 0; i < l; ++i) {
         perm[start[data_label[i]]] = i;
         ++start[data_label[i]];
      }

      start[0] = 0;

      for(i = 1; i < nr_class; ++i) {
         start[i] = start[i - 1] + count[i - 1];
      }

      return new Linear.GroupClassesReturn(nr_class, label, start, count);
   }

   static void info(String message) {
      synchronized(OUTPUT_MUTEX) {
         if (DEBUG_OUTPUT != null) {
            DEBUG_OUTPUT.printf(message);
            DEBUG_OUTPUT.flush();
         }
      }
   }

   static void info(String format, Object... args) {
      synchronized(OUTPUT_MUTEX) {
         if (DEBUG_OUTPUT != null) {
            DEBUG_OUTPUT.printf(format, args);
            DEBUG_OUTPUT.flush();
         }
      }
   }

   static double atof(String s) {
      if (s != null && s.length() >= 1) {
         double d = Double.parseDouble(s);
         if (!Double.isNaN(d) && !Double.isInfinite(d)) {
            return d;
         } else {
            throw new IllegalArgumentException("NaN or Infinity in input: " + s);
         }
      } else {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
   }

   static int atoi(String s) throws NumberFormatException {
      if (s != null && s.length() >= 1) {
         if (s.charAt(0) == '+') {
            s = s.substring(1);
         }

         return Integer.parseInt(s);
      } else {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
   }

   public static double[] copyOf(double[] original, int newLength) {
      double[] copy = new double[newLength];
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
      return copy;
   }

   public static int[] copyOf(int[] original, int newLength) {
      int[] copy = new int[newLength];
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
      return copy;
   }

   public static Model loadModel(Reader inputReader) throws IOException {
      Model model = new Model();
      model.label = null;
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

                  model.solverType = solver;
               } else if (split[0].equals("nr_class")) {
                  model.nr_class = atoi(split[1]);
                  Integer.parseInt(split[1]);
               } else if (split[0].equals("nr_feature")) {
                  model.nr_feature = atoi(split[1]);
               } else if (split[0].equals("bias")) {
                  model.bias = atof(split[1]);
               } else {
                  if (split[0].equals("w")) {
                     break label179;
                  }

                  if (!split[0].equals("label")) {
                     throw new RuntimeException("unknown text in model file: [" + line + "]");
                  }

                  model.label = new int[model.nr_class];

                  for(i = 0; i < model.nr_class; ++i) {
                     model.label[i] = atoi(split[i + 1]);
                  }
               }
            }
         }

         int w_size = model.nr_feature;
         if (model.bias >= 0.0D) {
            ++w_size;
         }

         i = model.nr_class;
         if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) {
            i = 1;
         }

         model.w = new double[w_size * i];
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
                     model.w[i * i + j] = atof(new String(buffer, 0, b));
                     break;
                  }

                  buffer[b++] = ch;
               }
            }
         }
      } finally {
         closeQuietly(reader);
      }

      return model;
   }

   public static Model loadModel(File modelFile) throws IOException {
      BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), FILE_CHARSET));
      return loadModel((Reader)inputReader);
   }

   static void closeQuietly(Closeable c) {
      if (c != null) {
         try {
            c.close();
         } catch (Throwable var2) {
         }

      }
   }

   public static int predict(Model model, FeatureNode[] x) {
      double[] dec_values = new double[model.nr_class];
      return predictValues(model, x, dec_values);
   }

   public static int predictProbability(Model model, FeatureNode[] x, double[] prob_estimates) throws IllegalArgumentException {
      if (!model.isProbabilityModel()) {
         throw new IllegalArgumentException("probability output is only supported for logistic regression");
      } else {
         int nr_class = model.nr_class;
         int nr_w;
         if (nr_class == 2) {
            nr_w = 1;
         } else {
            nr_w = nr_class;
         }

         int label = predictValues(model, x, prob_estimates);

         for(int i = 0; i < nr_w; ++i) {
            prob_estimates[i] = 1.0D / (1.0D + Math.exp(-prob_estimates[i]));
         }

         if (nr_class == 2) {
            prob_estimates[1] = 1.0D - prob_estimates[0];
         } else {
            double sum = 0.0D;

            int i;
            for(i = 0; i < nr_class; ++i) {
               sum += prob_estimates[i];
            }

            for(i = 0; i < nr_class; ++i) {
               prob_estimates[i] /= sum;
            }
         }

         return label;
      }
   }

   public static int predictValues(Model model, FeatureNode[] x, double[] dec_values) {
      int n;
      if (model.bias >= 0.0D) {
         n = model.nr_feature + 1;
      } else {
         n = model.nr_feature;
      }

      double[] w = model.w;
      int nr_w;
      if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) {
         nr_w = 1;
      } else {
         nr_w = model.nr_class;
      }

      int dec_max_idx;
      for(dec_max_idx = 0; dec_max_idx < nr_w; ++dec_max_idx) {
         dec_values[dec_max_idx] = 0.0D;
      }

      FeatureNode[] arr$ = x;
      int i = x.length;

      for(int i$ = 0; i$ < i; ++i$) {
         FeatureNode lx = arr$[i$];
         int idx = lx.index;
         if (idx <= n) {
            for(int i = 0; i < nr_w; ++i) {
               dec_values[i] += w[(idx - 1) * nr_w + i] * lx.value;
            }
         }
      }

      if (model.nr_class == 2) {
         return dec_values[0] > 0.0D ? model.label[0] : model.label[1];
      } else {
         dec_max_idx = 0;

         for(i = 1; i < model.nr_class; ++i) {
            if (dec_values[i] > dec_values[dec_max_idx]) {
               dec_max_idx = i;
            }
         }

         return model.label[dec_max_idx];
      }
   }

   static void printf(Formatter formatter, String format, Object... args) throws IOException {
      formatter.format(format, args);
      IOException ioException = formatter.ioException();
      if (ioException != null) {
         throw ioException;
      }
   }

   public static void saveModel(Writer modelOutput, Model model) throws IOException {
      int nr_feature = model.nr_feature;
      int w_size = nr_feature;
      if (model.bias >= 0.0D) {
         w_size = nr_feature + 1;
      }

      int nr_w = model.nr_class;
      if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) {
         nr_w = 1;
      }

      Formatter formatter = new Formatter(modelOutput, DEFAULT_LOCALE);

      try {
         printf(formatter, "solver_type %s\n", model.solverType.name());
         printf(formatter, "nr_class %d\n", model.nr_class);
         printf(formatter, "label");

         int i;
         for(i = 0; i < model.nr_class; ++i) {
            printf(formatter, " %d", model.label[i]);
         }

         printf(formatter, "\n");
         printf(formatter, "nr_feature %d\n", nr_feature);
         printf(formatter, "bias %.16g\n", model.bias);
         printf(formatter, "w\n");

         for(i = 0; i < w_size; ++i) {
            for(int j = 0; j < nr_w; ++j) {
               double value = model.w[i * nr_w + j];
               if (value == 0.0D) {
                  printf(formatter, "%d ", 0);
               } else {
                  printf(formatter, "%.16g ", value);
               }
            }

            printf(formatter, "\n");
         }

         formatter.flush();
         IOException ioException = formatter.ioException();
         if (ioException != null) {
            throw ioException;
         }
      } finally {
         formatter.close();
      }

   }

   public static void saveModel(File modelFile, Model model) throws IOException {
      BufferedWriter modelOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFile), FILE_CHARSET));
      saveModel((Writer)modelOutput, model);
   }

   private static int GETI(byte[] y, int i) {
      return y[i] + 1;
   }

   private static void solve_l2r_l1l2_svc(Problem prob, double[] w, double eps, double Cp, double Cn, SolverType solver_type) {
      int l = prob.l;
      int w_size = prob.n;
      int iter = 0;
      double[] QD = new double[l];
      int max_iter = 1000;
      int[] index = new int[l];
      double[] alpha = new double[l];
      byte[] y = new byte[l];
      int active_size = l;
      double PGmax_old = 1.0D / 0.0;
      double PGmin_old = -1.0D / 0.0;
      double[] diag = new double[]{0.5D / Cn, 0.0D, 0.5D / Cp};
      double[] upper_bound = new double[]{1.0D / 0.0, 0.0D, 1.0D / 0.0};
      if (solver_type == SolverType.L2R_L1LOSS_SVC_DUAL) {
         diag[0] = 0.0D;
         diag[2] = 0.0D;
         upper_bound[0] = Cn;
         upper_bound[2] = Cp;
      }

      int i;
      for(i = 0; i < w_size; ++i) {
         w[i] = 0.0D;
      }

      int nSV;
      for(i = 0; i < l; index[i] = i++) {
         alpha[i] = 0.0D;
         if (prob.y[i] > 0) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }

         QD[i] = diag[GETI(y, i)];
         FeatureNode[] arr$ = prob.x[i];
         int len$ = arr$.length;

         for(nSV = 0; nSV < len$; ++nSV) {
            FeatureNode xi = arr$[nSV];
            QD[i] += xi.value * xi.value;
         }
      }

      while(iter < max_iter) {
         double PGmax_new = -1.0D / 0.0;
         double PGmin_new = 1.0D / 0.0;

         for(i = 0; i < active_size; ++i) {
            int j = i + random.nextInt(active_size - i);
            swap(index, i, j);
         }

         for(int s = 0; s < active_size; ++s) {
            i = index[s];
            double G = 0.0D;
            byte yi = y[i];
            FeatureNode[] arr$ = prob.x[i];
            nSV = arr$.length;

            for(int i$ = 0; i$ < nSV; ++i$) {
               FeatureNode xi = arr$[i$];
               G += w[xi.index - 1] * xi.value;
            }

            G = G * (double)yi - 1.0D;
            double C = upper_bound[GETI(y, i)];
            G += alpha[i] * diag[GETI(y, i)];
            double PG = 0.0D;
            if (alpha[i] == 0.0D) {
               if (G > PGmax_old) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }

               if (G < 0.0D) {
                  PG = G;
               }
            } else if (alpha[i] == C) {
               if (G < PGmin_old) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }

               if (G > 0.0D) {
                  PG = G;
               }
            } else {
               PG = G;
            }

            PGmax_new = Math.max(PGmax_new, PG);
            PGmin_new = Math.min(PGmin_new, PG);
            if (Math.abs(PG) > 1.0E-12D) {
               double alpha_old = alpha[i];
               alpha[i] = Math.min(Math.max(alpha[i] - G / QD[i], 0.0D), C);
               double d = (alpha[i] - alpha_old) * (double)yi;
               FeatureNode[] arr$ = prob.x[i];
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  FeatureNode xi = arr$[i$];
                  int var10001 = xi.index - 1;
                  w[var10001] += d * xi.value;
               }
            }
         }

         ++iter;
         if (iter % 10 == 0) {
            info(".");
         }

         if (PGmax_new - PGmin_new <= eps) {
            if (active_size == l) {
               break;
            }

            active_size = l;
            info("*");
            PGmax_old = 1.0D / 0.0;
            PGmin_old = -1.0D / 0.0;
         } else {
            PGmax_old = PGmax_new;
            PGmin_old = PGmin_new;
            if (PGmax_new <= 0.0D) {
               PGmax_old = 1.0D / 0.0;
            }

            if (PGmin_new >= 0.0D) {
               PGmin_old = -1.0D / 0.0;
            }
         }
      }

      info(NL + "optimization finished, #iter = %d" + NL, iter);
      if (iter >= max_iter) {
         info("%nWARNING: reaching max number of iterations%nUsing -s 2 may be faster (also see FAQ)%n%n");
      }

      double v = 0.0D;
      nSV = 0;

      for(i = 0; i < w_size; ++i) {
         v += w[i] * w[i];
      }

      for(i = 0; i < l; ++i) {
         v += alpha[i] * (alpha[i] * diag[GETI(y, i)] - 2.0D);
         if (alpha[i] > 0.0D) {
            ++nSV;
         }
      }

      info("Objective value = %f" + NL, v / 2.0D);
      info("nSV = %d" + NL, nSV);
   }

   private static void solve_l2r_lr_dual(Problem prob, double[] w, double eps, double Cp, double Cn) {
      int l = prob.l;
      int w_size = prob.n;
      int iter = 0;
      double[] xTx = new double[l];
      int max_iter = 1000;
      int[] index = new int[l];
      double[] alpha = new double[2 * l];
      byte[] y = new byte[l];
      int max_inner_iter = 100;
      double innereps = 0.01D;
      double innereps_min = Math.min(1.0E-8D, eps);
      double[] upper_bound = new double[]{Cn, 0.0D, Cp};

      int i;
      for(i = 0; i < w_size; ++i) {
         w[i] = 0.0D;
      }

      int var10001;
      for(i = 0; i < l; index[i] = i++) {
         if (prob.y[i] > 0) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }

         alpha[2 * i] = Math.min(0.001D * upper_bound[GETI(y, i)], 1.0E-8D);
         alpha[2 * i + 1] = upper_bound[GETI(y, i)] - alpha[2 * i];
         xTx[i] = 0.0D;
         FeatureNode[] arr$ = prob.x[i];
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FeatureNode xi = arr$[i$];
            xTx[i] += xi.value * xi.value;
            var10001 = xi.index - 1;
            w[var10001] += (double)y[i] * alpha[2 * i] * xi.value;
         }
      }

      while(iter < max_iter) {
         int newton_iter;
         for(i = 0; i < l; ++i) {
            newton_iter = i + random.nextInt(l - i);
            swap(index, i, newton_iter);
         }

         newton_iter = 0;
         double Gmax = 0.0D;

         for(int s = 0; s < l; ++s) {
            i = index[s];
            byte yi = y[i];
            double C = upper_bound[GETI(y, i)];
            double ywTx = 0.0D;
            double xisq = xTx[i];
            FeatureNode[] arr$ = prob.x[i];
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               FeatureNode xi = arr$[i$];
               ywTx += w[xi.index - 1] * xi.value;
            }

            ywTx *= (double)y[i];
            double a = xisq;
            double b = ywTx;
            int ind1 = 2 * i;
            int ind2 = 2 * i + 1;
            int sign = 1;
            if (0.5D * xisq * (alpha[ind2] - alpha[ind1]) + ywTx < 0.0D) {
               ind1 = 2 * i + 1;
               ind2 = 2 * i;
               sign = -1;
            }

            double alpha_old = alpha[ind1];
            double z = alpha_old;
            if (C - alpha_old < 0.5D * C) {
               z = 0.1D * alpha_old;
            }

            double gp = xisq * (z - alpha_old) + (double)sign * ywTx + Math.log(z / (C - z));
            Gmax = Math.max(Gmax, Math.abs(gp));
            double eta = 0.1D;

            int inner_iter;
            for(inner_iter = 0; inner_iter <= max_inner_iter && Math.abs(gp) >= innereps; ++inner_iter) {
               double gpp = a + C / (C - z) / z;
               double tmpz = z - gp / gpp;
               if (tmpz <= 0.0D) {
                  z *= 0.1D;
               } else {
                  z = tmpz;
               }

               gp = a * (z - alpha_old) + (double)sign * b + Math.log(z / (C - z));
               ++newton_iter;
            }

            if (inner_iter > 0) {
               alpha[ind1] = z;
               alpha[ind2] = C - z;
               FeatureNode[] arr$ = prob.x[i];
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  FeatureNode xi = arr$[i$];
                  var10001 = xi.index - 1;
                  w[var10001] += (double)sign * (z - alpha_old) * (double)yi * xi.value;
               }
            }
         }

         ++iter;
         if (iter % 10 == 0) {
            info(".");
         }

         if (Gmax < eps) {
            break;
         }

         if (newton_iter <= l / 10) {
            innereps = Math.max(innereps_min, 0.1D * innereps);
         }
      }

      info("%noptimization finished, #iter = %d%n", iter);
      if (iter >= max_iter) {
         info("%nWARNING: reaching max number of iterations%nUsing -s 0 may be faster (also see FAQ)%n%n");
      }

      double v = 0.0D;

      for(i = 0; i < w_size; ++i) {
         v += w[i] * w[i];
      }

      v *= 0.5D;

      for(i = 0; i < l; ++i) {
         v += alpha[2 * i] * Math.log(alpha[2 * i]) + alpha[2 * i + 1] * Math.log(alpha[2 * i + 1]) - upper_bound[GETI(y, i)] * Math.log(upper_bound[GETI(y, i)]);
      }

      info("Objective value = %f%n", v);
   }

   private static void solve_l1r_l2_svc(Problem prob_col, double[] w, double eps, double Cp, double Cn) {
      int l = prob_col.l;
      int w_size = prob_col.n;
      int iter = 0;
      int max_iter = 1000;
      int active_size = w_size;
      int max_num_linesearch = 20;
      double sigma = 0.01D;
      double Gmax_old = 1.0D / 0.0;
      double Gnorm1_init = 0.0D;
      double loss_old = 0.0D;
      int[] index = new int[w_size];
      byte[] y = new byte[l];
      double[] b = new double[l];
      double[] xj_sq = new double[w_size];
      double[] C = new double[]{Cn, 0.0D, Cp};

      int j;
      for(j = 0; j < l; ++j) {
         b[j] = 1.0D;
         if (prob_col.y[j] > 0) {
            y[j] = 1;
         } else {
            y[j] = -1;
         }
      }

      FeatureNode[] arr$;
      int len$;
      int i$;
      FeatureNode xi;
      int ind;
      double val;
      for(j = 0; j < w_size; ++j) {
         w[j] = 0.0D;
         index[j] = j;
         xj_sq[j] = 0.0D;
         arr$ = prob_col.x[j];
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            xi = arr$[i$];
            ind = xi.index - 1;
            val = xi.value;
            xi.value *= (double)y[ind];
            xj_sq[j] += C[GETI(y, ind)] * val * val;
         }
      }

      double Gp;
      while(iter < max_iter) {
         double Gmax_new = 0.0D;
         double Gnorm1_new = 0.0D;

         for(j = 0; j < active_size; ++j) {
            int i = j + random.nextInt(active_size - j);
            swap(index, i, j);
         }

         for(int s = 0; s < active_size; ++s) {
            j = index[s];
            double G_loss = 0.0D;
            double H = 0.0D;
            arr$ = prob_col.x[j];
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               xi = arr$[i$];
               ind = xi.index - 1;
               if (b[ind] > 0.0D) {
                  val = xi.value;
                  double tmp = C[GETI(y, ind)] * val;
                  G_loss -= tmp * b[ind];
                  H += tmp * val;
               }
            }

            G_loss *= 2.0D;
            H *= 2.0D;
            H = Math.max(H, 1.0E-12D);
            Gp = G_loss + 1.0D;
            double Gn = G_loss - 1.0D;
            double violation = 0.0D;
            if (w[j] == 0.0D) {
               if (Gp < 0.0D) {
                  violation = -Gp;
               } else if (Gn > 0.0D) {
                  violation = Gn;
               } else if (Gp > Gmax_old / (double)l && Gn < -Gmax_old / (double)l) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }
            } else if (w[j] > 0.0D) {
               violation = Math.abs(Gp);
            } else {
               violation = Math.abs(Gn);
            }

            Gmax_new = Math.max(Gmax_new, violation);
            Gnorm1_new += violation;
            double d;
            if (Gp <= H * w[j]) {
               d = -Gp / H;
            } else if (Gn >= H * w[j]) {
               d = -Gn / H;
            } else {
               d = -w[j];
            }

            if (Math.abs(d) >= 1.0E-12D) {
               double delta = Math.abs(w[j] + d) - Math.abs(w[j]) + G_loss * d;
               double d_old = 0.0D;

               int var10001;
               int num_linesearch;
               int len$;
               label213:
               for(num_linesearch = 0; num_linesearch < max_num_linesearch; ++num_linesearch) {
                  double d_diff = d_old - d;
                  double cond = Math.abs(w[j] + d) - Math.abs(w[j]) - sigma * delta;
                  double appxcond = xj_sq[j] * d * d + G_loss * d + cond;
                  FeatureNode[] arr$;
                  int len$;
                  FeatureNode x;
                  if (appxcond <= 0.0D) {
                     arr$ = prob_col.x[j];
                     len$ = arr$.length;
                     len$ = 0;

                     while(true) {
                        if (len$ >= len$) {
                           break label213;
                        }

                        x = arr$[len$];
                        var10001 = x.index - 1;
                        b[var10001] += d_diff * x.value;
                        ++len$;
                     }
                  }

                  double loss_new;
                  int ind;
                  double b_new;
                  if (num_linesearch == 0) {
                     loss_old = 0.0D;
                     loss_new = 0.0D;
                     arr$ = prob_col.x[j];
                     len$ = arr$.length;

                     for(len$ = 0; len$ < len$; ++len$) {
                        x = arr$[len$];
                        ind = x.index - 1;
                        if (b[ind] > 0.0D) {
                           loss_old += C[GETI(y, ind)] * b[ind] * b[ind];
                        }

                        b_new = b[ind] + d_diff * x.value;
                        b[ind] = b_new;
                        if (b_new > 0.0D) {
                           loss_new += C[GETI(y, ind)] * b_new * b_new;
                        }
                     }
                  } else {
                     loss_new = 0.0D;
                     arr$ = prob_col.x[j];
                     len$ = arr$.length;

                     for(len$ = 0; len$ < len$; ++len$) {
                        x = arr$[len$];
                        ind = x.index - 1;
                        b_new = b[ind] + d_diff * x.value;
                        b[ind] = b_new;
                        if (b_new > 0.0D) {
                           loss_new += C[GETI(y, ind)] * b_new * b_new;
                        }
                     }
                  }

                  cond = cond + loss_new - loss_old;
                  if (cond <= 0.0D) {
                     break;
                  }

                  d_old = d;
                  d *= 0.5D;
                  delta *= 0.5D;
               }

               w[j] += d;
               if (num_linesearch >= max_num_linesearch) {
                  info("#");

                  int i;
                  for(i = 0; i < l; ++i) {
                     b[i] = 1.0D;
                  }

                  for(i = 0; i < w_size; ++i) {
                     if (w[i] != 0.0D) {
                        FeatureNode[] arr$ = prob_col.x[i];
                        len$ = arr$.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           FeatureNode x = arr$[i$];
                           var10001 = x.index - 1;
                           b[var10001] -= w[i] * x.value;
                        }
                     }
                  }
               }
            }
         }

         if (iter == 0) {
            Gnorm1_init = Gnorm1_new;
         }

         ++iter;
         if (iter % 10 == 0) {
            info(".");
         }

         if (Gmax_new <= eps * Gnorm1_init) {
            if (active_size == w_size) {
               break;
            }

            active_size = w_size;
            info("*");
            Gmax_old = 1.0D / 0.0;
         } else {
            Gmax_old = Gmax_new;
         }
      }

      info("%noptimization finished, #iter = %d%n", iter);
      if (iter >= max_iter) {
         info("%nWARNING: reaching max number of iterations%n");
      }

      Gp = 0.0D;
      i$ = 0;

      for(j = 0; j < w_size; ++j) {
         FeatureNode[] arr$ = prob_col.x[j];
         ind = arr$.length;

         for(int i$ = 0; i$ < ind; ++i$) {
            FeatureNode x = arr$[i$];
            x.value *= (double)prob_col.y[x.index - 1];
         }

         if (w[j] != 0.0D) {
            Gp += Math.abs(w[j]);
            ++i$;
         }
      }

      for(j = 0; j < l; ++j) {
         if (b[j] > 0.0D) {
            Gp += C[GETI(y, j)] * b[j] * b[j];
         }
      }

      info("Objective value = %f%n", Gp);
      info("#nonzeros/#features = %d/%d%n", i$, w_size);
   }

   private static void solve_l1r_lr(Problem prob_col, double[] w, double eps, double Cp, double Cn) {
      int l = prob_col.l;
      int w_size = prob_col.n;
      int newton_iter = 0;
      int iter = false;
      int max_newton_iter = 100;
      int max_iter = 1000;
      int max_num_linesearch = 20;
      double nu = 1.0E-12D;
      double inner_eps = 1.0D;
      double sigma = 0.01D;
      double w_norm = 0.0D;
      double Gnorm1_init = 0.0D;
      double Gmax_old = 1.0D / 0.0;
      double QP_Gmax_old = 1.0D / 0.0;
      int[] index = new int[w_size];
      byte[] y = new byte[l];
      double[] Hdiag = new double[w_size];
      double[] Grad = new double[w_size];
      double[] wpd = new double[w_size];
      double[] xjneg_sum = new double[w_size];
      double[] xTd = new double[l];
      double[] exp_wTx = new double[l];
      double[] exp_wTx_new = new double[l];
      double[] tau = new double[l];
      double[] D = new double[l];
      double[] C = new double[]{Cn, 0.0D, Cp};

      int j;
      for(j = 0; j < l; ++j) {
         if (prob_col.y[j] > 0) {
            y[j] = 1;
         } else {
            y[j] = -1;
         }

         exp_wTx[j] = 1.0D;
         tau[j] = C[GETI(y, j)] * 0.5D;
         D[j] = C[GETI(y, j)] * 0.25D;
      }

      FeatureNode[] arr$;
      int i;
      int i$;
      FeatureNode x;
      int i$;
      for(j = 0; j < w_size; ++j) {
         w[j] = 0.0D;
         wpd[j] = w[j];
         index[j] = j;
         xjneg_sum[j] = 0.0D;
         arr$ = prob_col.x[j];
         i = arr$.length;

         for(i$ = 0; i$ < i; ++i$) {
            x = arr$[i$];
            i$ = x.index - 1;
            if (y[i$] == -1) {
               xjneg_sum[j] += C[GETI(y, i$)] * x.value;
            }
         }
      }

      double Gp;
      while(newton_iter < max_newton_iter) {
         double Gmax_new = 0.0D;
         double Gnorm1_new = 0.0D;
         int active_size = w_size;

         int s;
         FeatureNode x;
         FeatureNode[] arr$;
         double tau_tmp;
         int len$;
         double violation;
         for(s = 0; s < active_size; ++s) {
            j = index[s];
            Hdiag[j] = nu;
            Grad[j] = 0.0D;
            Gp = 0.0D;
            arr$ = prob_col.x[j];
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               x = arr$[i$];
               int ind = x.index - 1;
               Hdiag[j] += x.value * x.value * D[ind];
               Gp += x.value * tau[ind];
            }

            Grad[j] = -Gp + xjneg_sum[j];
            tau_tmp = Grad[j] + 1.0D;
            violation = Grad[j] - 1.0D;
            double violation = 0.0D;
            if (w[j] == 0.0D) {
               if (tau_tmp < 0.0D) {
                  violation = -tau_tmp;
               } else if (violation > 0.0D) {
                  violation = violation;
               } else if (tau_tmp > Gmax_old / (double)l && violation < -Gmax_old / (double)l) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }
            } else if (w[j] > 0.0D) {
               violation = Math.abs(tau_tmp);
            } else {
               violation = Math.abs(violation);
            }

            Gmax_new = Math.max(Gmax_new, violation);
            Gnorm1_new += violation;
         }

         if (newton_iter == 0) {
            Gnorm1_init = Gnorm1_new;
         }

         if (Gnorm1_new <= eps * Gnorm1_init) {
            break;
         }

         int iter = 0;
         QP_Gmax_old = 1.0D / 0.0;
         int QP_active_size = active_size;

         int num_linesearch;
         for(num_linesearch = 0; num_linesearch < l; ++num_linesearch) {
            xTd[num_linesearch] = 0.0D;
         }

         while(iter < max_iter) {
            double QP_Gmax_new = 0.0D;
            double QP_Gnorm1_new = 0.0D;

            for(j = 0; j < QP_active_size; ++j) {
               num_linesearch = random.nextInt(QP_active_size - j);
               swap(index, num_linesearch, j);
            }

            for(s = 0; s < QP_active_size; ++s) {
               j = index[s];
               double H = Hdiag[j];
               double G = Grad[j] + (wpd[j] - w[j]) * nu;
               arr$ = prob_col.x[j];
               i = arr$.length;

               for(i$ = 0; i$ < i; ++i$) {
                  x = arr$[i$];
                  i$ = x.index - 1;
                  G += x.value * D[i$] * xTd[i$];
               }

               Gp = G + 1.0D;
               tau_tmp = G - 1.0D;
               violation = 0.0D;
               if (wpd[j] == 0.0D) {
                  if (Gp < 0.0D) {
                     violation = -Gp;
                  } else if (tau_tmp > 0.0D) {
                     violation = tau_tmp;
                  } else if (Gp > QP_Gmax_old / (double)l && tau_tmp < -QP_Gmax_old / (double)l) {
                     --QP_active_size;
                     swap(index, s, QP_active_size);
                     --s;
                     continue;
                  }
               } else if (wpd[j] > 0.0D) {
                  violation = Math.abs(Gp);
               } else {
                  violation = Math.abs(tau_tmp);
               }

               QP_Gmax_new = Math.max(QP_Gmax_new, violation);
               QP_Gnorm1_new += violation;
               double z;
               if (Gp <= H * wpd[j]) {
                  z = -Gp / H;
               } else if (tau_tmp >= H * wpd[j]) {
                  z = -tau_tmp / H;
               } else {
                  z = -wpd[j];
               }

               if (Math.abs(z) >= 1.0E-12D) {
                  z = Math.min(Math.max(z, -10.0D), 10.0D);
                  wpd[j] += z;
                  FeatureNode[] arr$ = prob_col.x[j];
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     FeatureNode x = arr$[i$];
                     int ind = x.index - 1;
                     xTd[ind] += x.value * z;
                  }
               }
            }

            ++iter;
            if (QP_Gnorm1_new <= inner_eps * Gnorm1_init) {
               if (QP_active_size == active_size) {
                  break;
               }

               QP_active_size = active_size;
               QP_Gmax_old = 1.0D / 0.0;
            } else {
               QP_Gmax_old = QP_Gmax_new;
            }
         }

         if (iter >= max_iter) {
            info("WARNING: reaching max number of inner iterations\n");
         }

         double delta = 0.0D;
         double w_norm_new = 0.0D;

         for(j = 0; j < w_size; ++j) {
            delta += Grad[j] * (wpd[j] - w[j]);
            if (wpd[j] != 0.0D) {
               w_norm_new += Math.abs(wpd[j]);
            }
         }

         delta += w_norm_new - w_norm;
         double negsum_xTd = 0.0D;

         for(num_linesearch = 0; num_linesearch < l; ++num_linesearch) {
            if (y[num_linesearch] == -1) {
               negsum_xTd += C[GETI(y, num_linesearch)] * xTd[num_linesearch];
            }
         }

         label246:
         for(num_linesearch = 0; num_linesearch < max_num_linesearch; ++num_linesearch) {
            double cond = w_norm_new - w_norm + negsum_xTd - sigma * delta;

            for(i = 0; i < l; ++i) {
               tau_tmp = Math.exp(xTd[i]);
               exp_wTx_new[i] = exp_wTx[i] * tau_tmp;
               cond += C[GETI(y, i)] * Math.log((1.0D + exp_wTx_new[i]) / (tau_tmp + exp_wTx_new[i]));
            }

            if (cond <= 0.0D) {
               w_norm = w_norm_new;

               for(j = 0; j < w_size; ++j) {
                  w[j] = wpd[j];
               }

               i = 0;

               while(true) {
                  if (i >= l) {
                     break label246;
                  }

                  exp_wTx[i] = exp_wTx_new[i];
                  tau_tmp = 1.0D / (1.0D + exp_wTx[i]);
                  tau[i] = C[GETI(y, i)] * tau_tmp;
                  D[i] = C[GETI(y, i)] * exp_wTx[i] * tau_tmp * tau_tmp;
                  ++i;
               }
            }

            w_norm_new = 0.0D;

            for(j = 0; j < w_size; ++j) {
               wpd[j] = (w[j] + wpd[j]) * 0.5D;
               if (wpd[j] != 0.0D) {
                  w_norm_new += Math.abs(wpd[j]);
               }
            }

            delta *= 0.5D;
            negsum_xTd *= 0.5D;

            for(i = 0; i < l; ++i) {
               xTd[i] *= 0.5D;
            }
         }

         if (num_linesearch >= max_num_linesearch) {
            for(i = 0; i < l; ++i) {
               exp_wTx[i] = 0.0D;
            }

            for(i = 0; i < w_size; ++i) {
               if (w[i] != 0.0D) {
                  arr$ = prob_col.x[i];
                  len$ = arr$.length;

                  for(i$ = 0; i$ < len$; ++i$) {
                     x = arr$[i$];
                     int var10001 = x.index - 1;
                     exp_wTx[var10001] += w[i] * x.value;
                  }
               }
            }

            for(i = 0; i < l; ++i) {
               exp_wTx[i] = Math.exp(exp_wTx[i]);
            }
         }

         if (iter == 1) {
            inner_eps *= 0.25D;
         }

         ++newton_iter;
         Gmax_old = Gmax_new;
         info("iter %3d  #CD cycles %d%n", newton_iter, iter);
      }

      info("=========================%n");
      info("optimization finished, #iter = %d%n", newton_iter);
      if (newton_iter >= max_newton_iter) {
         info("WARNING: reaching max number of iterations%n");
      }

      Gp = 0.0D;
      i$ = 0;

      for(j = 0; j < w_size; ++j) {
         if (w[j] != 0.0D) {
            Gp += Math.abs(w[j]);
            ++i$;
         }
      }

      for(j = 0; j < l; ++j) {
         if (y[j] == 1) {
            Gp += C[GETI(y, j)] * Math.log(1.0D + 1.0D / exp_wTx[j]);
         } else {
            Gp += C[GETI(y, j)] * Math.log(1.0D + exp_wTx[j]);
         }
      }

      info("Objective value = %f%n", Gp);
      info("#nonzeros/#features = %d/%d%n", i$, w_size);
   }

   static Problem transpose(Problem prob) {
      int l = prob.l;
      int n = prob.n;
      int[] col_ptr = new int[n + 1];
      Problem prob_col = new Problem();
      prob_col.l = l;
      prob_col.n = n;
      prob_col.y = new int[l];
      prob_col.x = new FeatureNode[n][];

      int i;
      for(i = 0; i < l; ++i) {
         prob_col.y[i] = prob.y[i];
      }

      int index;
      for(i = 0; i < l; ++i) {
         FeatureNode[] arr$ = prob.x[i];
         int len$ = arr$.length;

         for(index = 0; index < len$; ++index) {
            FeatureNode x = arr$[index];
            ++col_ptr[x.index];
         }
      }

      for(i = 0; i < n; ++i) {
         prob_col.x[i] = new FeatureNode[col_ptr[i + 1]];
         col_ptr[i] = 0;
      }

      for(i = 0; i < l; ++i) {
         for(int j = 0; j < prob.x[i].length; ++j) {
            FeatureNode x = prob.x[i][j];
            index = x.index - 1;
            prob_col.x[index][col_ptr[index]] = new FeatureNode(i + 1, x.value);
            int var10002 = col_ptr[index]++;
         }
      }

      return prob_col;
   }

   static void swap(double[] array, int idxA, int idxB) {
      double temp = array[idxA];
      array[idxA] = array[idxB];
      array[idxB] = temp;
   }

   static void swap(int[] array, int idxA, int idxB) {
      int temp = array[idxA];
      array[idxA] = array[idxB];
      array[idxB] = temp;
   }

   static void swap(IntArrayPointer array, int idxA, int idxB) {
      int temp = array.get(idxA);
      array.set(idxA, array.get(idxB));
      array.set(idxB, temp);
   }

   public static Model train(Problem prob, Parameter param) {
      if (prob == null) {
         throw new IllegalArgumentException("problem must not be null");
      } else if (param == null) {
         throw new IllegalArgumentException("parameter must not be null");
      } else {
         FeatureNode[][] arr$ = prob.x;
         int j = arr$.length;

         int l;
         int w_size;
         for(l = 0; l < j; ++l) {
            FeatureNode[] nodes = arr$[l];
            w_size = 0;
            FeatureNode[] arr$ = nodes;
            int len$ = nodes.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               FeatureNode n = arr$[i$];
               if (n.index <= w_size) {
                  throw new IllegalArgumentException("feature nodes must be sorted by index in ascending order");
               }

               w_size = n.index;
            }
         }

         l = prob.l;
         int n = prob.n;
         w_size = prob.n;
         Model model = new Model();
         if (prob.bias >= 0.0D) {
            model.nr_feature = n - 1;
         } else {
            model.nr_feature = n;
         }

         model.solverType = param.solverType;
         model.bias = prob.bias;
         int[] perm = new int[l];
         Linear.GroupClassesReturn rv = groupClasses(prob, perm);
         int nr_class = rv.nr_class;
         int[] label = rv.label;
         int[] start = rv.start;
         int[] count = rv.count;
         model.nr_class = nr_class;
         model.label = new int[nr_class];

         int i;
         for(i = 0; i < nr_class; ++i) {
            model.label[i] = label[i];
         }

         double[] weighted_C = new double[nr_class];

         for(i = 0; i < nr_class; ++i) {
            weighted_C[i] = param.C;
         }

         for(i = 0; i < param.getNumWeights(); ++i) {
            for(j = 0; j < nr_class && param.weightLabel[i] != label[j]; ++j) {
            }

            if (j == nr_class) {
               throw new IllegalArgumentException("class label " + param.weightLabel[i] + " specified in weight is not found");
            }

            weighted_C[j] *= param.weight[i];
         }

         FeatureNode[][] x = new FeatureNode[l][];

         for(i = 0; i < l; ++i) {
            x[i] = prob.x[perm[i]];
         }

         Problem sub_prob = new Problem();
         sub_prob.l = l;
         sub_prob.n = n;
         sub_prob.x = new FeatureNode[sub_prob.l][];
         sub_prob.y = new int[sub_prob.l];

         int k;
         for(k = 0; k < sub_prob.l; ++k) {
            sub_prob.x[k] = x[k];
         }

         if (param.solverType == SolverType.MCSVM_CS) {
            model.w = new double[n * nr_class];

            for(i = 0; i < nr_class; ++i) {
               for(j = start[i]; j < start[i] + count[i]; ++j) {
                  sub_prob.y[j] = i;
               }
            }

            SolverMCSVM_CS solver = new SolverMCSVM_CS(sub_prob, nr_class, weighted_C, param.eps);
            solver.solve(model.w);
         } else if (nr_class == 2) {
            model.w = new double[w_size];
            int e0 = start[0] + count[0];

            for(k = 0; k < e0; ++k) {
               sub_prob.y[k] = 1;
            }

            while(k < sub_prob.l) {
               sub_prob.y[k] = -1;
               ++k;
            }

            train_one(sub_prob, param, model.w, weighted_C[0], weighted_C[1]);
         } else {
            model.w = new double[w_size * nr_class];
            double[] w = new double[w_size];

            for(i = 0; i < nr_class; ++i) {
               int si = start[i];
               int ei = si + count[i];

               for(k = 0; k < si; ++k) {
                  sub_prob.y[k] = -1;
               }

               while(k < ei) {
                  sub_prob.y[k] = 1;
                  ++k;
               }

               while(k < sub_prob.l) {
                  sub_prob.y[k] = -1;
                  ++k;
               }

               train_one(sub_prob, param, w, weighted_C[i], param.C);

               for(j = 0; j < n; ++j) {
                  model.w[j * nr_class + i] = w[j];
               }
            }
         }

         return model;
      }
   }

   private static void train_one(Problem prob, Parameter param, double[] w, double Cp, double Cn) {
      double eps = param.eps;
      int pos = 0;

      int neg;
      for(neg = 0; neg < prob.l; ++neg) {
         if (prob.y[neg] == 1) {
            ++pos;
         }
      }

      neg = prob.l - pos;
      Function fun_obj = null;
      Problem prob_col;
      Tron tron_obj;
      switch(param.solverType) {
      case L2R_LR:
         Function fun_obj = new L2R_LrFunction(prob, Cp, Cn);
         tron_obj = new Tron(fun_obj, eps * (double)Math.min(pos, neg) / (double)prob.l);
         tron_obj.tron(w);
         break;
      case L2R_L2LOSS_SVC:
         fun_obj = new L2R_L2_SvcFunction(prob, Cp, Cn);
         tron_obj = new Tron(fun_obj, eps * (double)Math.min(pos, neg) / (double)prob.l);
         tron_obj.tron(w);
         break;
      case L2R_L2LOSS_SVC_DUAL:
         solve_l2r_l1l2_svc(prob, w, eps, Cp, Cn, SolverType.L2R_L2LOSS_SVC_DUAL);
         break;
      case L2R_L1LOSS_SVC_DUAL:
         solve_l2r_l1l2_svc(prob, w, eps, Cp, Cn, SolverType.L2R_L1LOSS_SVC_DUAL);
         break;
      case L1R_L2LOSS_SVC:
         prob_col = transpose(prob);
         solve_l1r_l2_svc(prob_col, w, eps * (double)Math.min(pos, neg) / (double)prob.l, Cp, Cn);
         break;
      case L1R_LR:
         prob_col = transpose(prob);
         solve_l1r_lr(prob_col, w, eps * (double)Math.min(pos, neg) / (double)prob.l, Cp, Cn);
         break;
      case L2R_LR_DUAL:
         solve_l2r_lr_dual(prob, w, eps, Cp, Cn);
         break;
      default:
         throw new IllegalStateException("unknown solver type: " + param.solverType);
      }

   }

   public static void disableDebugOutput() {
      setDebugOutput((PrintStream)null);
   }

   public static void enableDebugOutput() {
      setDebugOutput(System.out);
   }

   public static void setDebugOutput(PrintStream debugOutput) {
      synchronized(OUTPUT_MUTEX) {
         DEBUG_OUTPUT = debugOutput;
      }
   }

   public static void resetRandom() {
      random = new Random(0L);
   }

   static {
      DEFAULT_LOCALE = Locale.ENGLISH;
      OUTPUT_MUTEX = new Object();
      DEBUG_OUTPUT = System.out;
      NL = System.getProperty("line.separator");
      random = new Random(0L);
   }

   private static class GroupClassesReturn {
      final int[] count;
      final int[] label;
      final int nr_class;
      final int[] start;

      GroupClassesReturn(int nr_class, int[] label, int[] start, int[] count) {
         this.nr_class = nr_class;
         this.label = label;
         this.start = start;
         this.count = count;
      }
   }
}
