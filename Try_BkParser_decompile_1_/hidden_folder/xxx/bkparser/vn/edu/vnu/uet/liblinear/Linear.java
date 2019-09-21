package vn.edu.vnu.uet.liblinear;

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
   private static final long DEFAULT_RANDOM_SEED = 0L;
   static Random random;

   public Linear() {
   }

   public static void crossValidation(Problem prob, Parameter param, int nr_fold, double[] target) {
      int l = prob.l;
      int[] perm = new int[l];
      if (nr_fold > l) {
         nr_fold = l;
         System.err.println("WARNING: # folds > # data. Will use # folds = # data instead (i.e., leave-one-out cross validation)");
      }

      int[] fold_start = new int[nr_fold + 1];

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
         subprob.x = new Feature[subprob.l][];
         subprob.y = new double[subprob.l];
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
         int this_label = (int)prob.y[i];

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

      if (nr_class == 2 && label[0] == -1 && label[1] == 1) {
         swap((int[])label, 0, 1);
         swap((int[])count, 0, 1);

         for(i = 0; i < l; ++i) {
            if (data_label[i] == 0) {
               data_label[i] = 1;
            } else {
               data_label[i] = 0;
            }
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

      String line = null;

      int i;
      while((line = reader.readLine()) != null) {
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
               break;
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

      return model;
   }

   public static Model loadModel(File modelFile) throws IOException {
      BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), FILE_CHARSET));

      Model var2;
      try {
         var2 = loadModel((Reader)inputReader);
      } finally {
         inputReader.close();
      }

      return var2;
   }

   static void closeQuietly(Closeable c) {
      if (c != null) {
         try {
            c.close();
         } catch (Throwable var2) {
         }

      }
   }

   public static double predict(Model model, Feature[] x) {
      double[] dec_values = new double[model.nr_class];
      return predictValues(model, x, dec_values);
   }

   public static double predict(Model model, Feature[] x, double[] dec_values) {
      return predictValues(model, x, dec_values);
   }

   public static double predictProbability(Model model, Feature[] x, double[] prob_estimates) throws IllegalArgumentException {
      int nr_w;
      int i;
      if (!model.isProbabilityModel()) {
         StringBuilder sb = new StringBuilder("probability output is only supported for logistic regression");
         sb.append(". This is currently only supported by the following solvers: ");
         nr_w = 0;
         SolverType[] var11 = SolverType.values();
         int var6 = var11.length;

         for(i = 0; i < var6; ++i) {
            SolverType solverType = var11[i];
            if (solverType.isLogisticRegressionSolver()) {
               if (nr_w++ > 0) {
                  sb.append(", ");
               }

               sb.append(solverType.name());
            }
         }

         throw new IllegalArgumentException(sb.toString());
      } else {
         int nr_class = model.nr_class;
         if (nr_class == 2) {
            nr_w = 1;
         } else {
            nr_w = nr_class;
         }

         double label = predictValues(model, x, prob_estimates);

         for(i = 0; i < nr_w; ++i) {
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

   public static double predictValues(Model model, Feature[] x, double[] dec_values) {
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

      Feature[] var12 = x;
      int i = x.length;

      for(int var8 = 0; var8 < i; ++var8) {
         Feature lx = var12[var8];
         int idx = lx.getIndex();
         if (idx <= n) {
            for(int i = 0; i < nr_w; ++i) {
               dec_values[i] += w[(idx - 1) * nr_w + i] * lx.getValue();
            }
         }
      }

      if (model.nr_class == 2) {
         if (model.solverType.isSupportVectorRegression()) {
            return dec_values[0];
         } else {
            return dec_values[0] > 0.0D ? (double)model.label[0] : (double)model.label[1];
         }
      } else {
         dec_max_idx = 0;

         for(i = 1; i < model.nr_class; ++i) {
            if (dec_values[i] > dec_values[dec_max_idx]) {
               dec_max_idx = i;
            }
         }

         return (double)model.label[dec_max_idx];
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
         int i;
         if (model.label != null) {
            printf(formatter, "label");

            for(i = 0; i < model.nr_class; ++i) {
               printf(formatter, " %d", model.label[i]);
            }

            printf(formatter, "\n");
         }

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
      for(i = 0; i < l; ++i) {
         if (prob.y[i] > 0.0D) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }
      }

      for(i = 0; i < l; ++i) {
         alpha[i] = 0.0D;
      }

      for(i = 0; i < w_size; ++i) {
         w[i] = 0.0D;
      }

      int var10001;
      int nSV;
      for(i = 0; i < l; index[i] = i++) {
         QD[i] = diag[GETI(y, i)];
         Feature[] var38 = prob.x[i];
         int var39 = var38.length;

         for(nSV = 0; nSV < var39; ++nSV) {
            Feature xi = var38[nSV];
            double val = xi.getValue();
            QD[i] += val * val;
            var10001 = xi.getIndex() - 1;
            w[var10001] += (double)y[i] * alpha[i] * val;
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
            Feature[] var48 = prob.x[i];
            nSV = var48.length;

            for(int var50 = 0; var50 < nSV; ++var50) {
               Feature xi = var48[var50];
               G += w[xi.getIndex() - 1] * xi.getValue();
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
               Feature[] var51 = prob.x[i];
               int var53 = var51.length;

               for(int var43 = 0; var43 < var53; ++var43) {
                  Feature xi = var51[var43];
                  var10001 = xi.getIndex() - 1;
                  w[var10001] += d * xi.getValue();
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

      info("%noptimization finished, #iter = %d%n", iter);
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

      info("Objective value = %g%n", v / 2.0D);
      info("nSV = %d%n", nSV);
   }

   private static int GETI_SVR(int i) {
      return 0;
   }

   private static void solve_l2r_l1l2_svr(Problem prob, double[] w, Parameter param) {
      int l = prob.l;
      double C = param.C;
      double p = param.p;
      int w_size = prob.n;
      double eps = param.eps;
      int iter = 0;
      int max_iter = param.getMaxIters();
      int active_size = l;
      int[] index = new int[l];
      double Gmax_old = 1.0D / 0.0;
      double Gnorm1_init = -1.0D;
      double[] beta = new double[l];
      double[] QD = new double[l];
      double[] y = prob.y;
      double[] lambda = new double[]{0.5D / C};
      double[] upper_bound = new double[]{1.0D / 0.0};
      if (param.solverType == SolverType.L2R_L1LOSS_SVR_DUAL) {
         lambda[0] = 0.0D;
         upper_bound[0] = C;
      }

      int i;
      for(i = 0; i < l; ++i) {
         beta[i] = 0.0D;
      }

      for(i = 0; i < w_size; ++i) {
         w[i] = 0.0D;
      }

      int var10001;
      Feature[] var36;
      int var37;
      int nSV;
      Feature xi;
      double violation;
      for(i = 0; i < l; index[i] = i++) {
         QD[i] = 0.0D;
         var36 = prob.x[i];
         var37 = var36.length;

         for(nSV = 0; nSV < var37; ++nSV) {
            xi = var36[nSV];
            violation = xi.getValue();
            QD[i] += violation * violation;
            var10001 = xi.getIndex() - 1;
            w[var10001] += beta[i] * violation;
         }
      }

      double Gp;
      while(iter < max_iter) {
         double Gmax_new = 0.0D;
         double Gnorm1_new = 0.0D;

         for(i = 0; i < active_size; ++i) {
            int j = i + random.nextInt(active_size - i);
            swap(index, i, j);
         }

         for(int s = 0; s < active_size; ++s) {
            i = index[s];
            double G = -y[i] + lambda[GETI_SVR(i)] * beta[i];
            double H = QD[i] + lambda[GETI_SVR(i)];
            var36 = prob.x[i];
            var37 = var36.length;

            for(nSV = 0; nSV < var37; ++nSV) {
               xi = var36[nSV];
               int ind = xi.getIndex() - 1;
               double val = xi.getValue();
               G += val * w[ind];
            }

            Gp = G + p;
            double Gn = G - p;
            violation = 0.0D;
            if (beta[i] == 0.0D) {
               if (Gp < 0.0D) {
                  violation = -Gp;
               } else if (Gn > 0.0D) {
                  violation = Gn;
               } else if (Gp > Gmax_old && Gn < -Gmax_old) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }
            } else if (beta[i] >= upper_bound[GETI_SVR(i)]) {
               if (Gp > 0.0D) {
                  violation = Gp;
               } else if (Gp < -Gmax_old) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }
            } else if (beta[i] <= -upper_bound[GETI_SVR(i)]) {
               if (Gn < 0.0D) {
                  violation = -Gn;
               } else if (Gn > Gmax_old) {
                  --active_size;
                  swap(index, s, active_size);
                  --s;
                  continue;
               }
            } else if (beta[i] > 0.0D) {
               violation = Math.abs(Gp);
            } else {
               violation = Math.abs(Gn);
            }

            Gmax_new = Math.max(Gmax_new, violation);
            Gnorm1_new += violation;
            double d;
            if (Gp < H * beta[i]) {
               d = -Gp / H;
            } else if (Gn > H * beta[i]) {
               d = -Gn / H;
            } else {
               d = -beta[i];
            }

            if (Math.abs(d) >= 1.0E-12D) {
               double beta_old = beta[i];
               beta[i] = Math.min(Math.max(beta[i] + d, -upper_bound[GETI_SVR(i)]), upper_bound[GETI_SVR(i)]);
               d = beta[i] - beta_old;
               if (d != 0.0D) {
                  Feature[] var44 = prob.x[i];
                  int var45 = var44.length;

                  for(int var46 = 0; var46 < var45; ++var46) {
                     Feature xi = var44[var46];
                     var10001 = xi.getIndex() - 1;
                     w[var10001] += d * xi.getValue();
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

         if (Gnorm1_new <= eps * Gnorm1_init) {
            if (active_size == l) {
               break;
            }

            active_size = l;
            info("*");
            Gmax_old = 1.0D / 0.0;
         } else {
            Gmax_old = Gmax_new;
         }
      }

      info("%noptimization finished, #iter = %d%n", iter);
      if (iter >= max_iter) {
         info("%nWARNING: reaching max number of iterations%nUsing -s 11 may be faster%n%n");
      }

      Gp = 0.0D;
      nSV = 0;

      for(i = 0; i < w_size; ++i) {
         Gp += w[i] * w[i];
      }

      Gp = 0.5D * Gp;

      for(i = 0; i < l; ++i) {
         Gp += p * Math.abs(beta[i]) - y[i] * beta[i] + 0.5D * lambda[GETI_SVR(i)] * beta[i] * beta[i];
         if (beta[i] != 0.0D) {
            ++nSV;
         }
      }

      info("Objective value = %g%n", Gp);
      info("nSV = %d%n", nSV);
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
      for(i = 0; i < l; ++i) {
         if (prob.y[i] > 0.0D) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }
      }

      for(i = 0; i < l; ++i) {
         alpha[2 * i] = Math.min(0.001D * upper_bound[GETI(y, i)], 1.0E-8D);
         alpha[2 * i + 1] = upper_bound[GETI(y, i)] - alpha[2 * i];
      }

      for(i = 0; i < w_size; ++i) {
         w[i] = 0.0D;
      }

      int var10001;
      double C;
      for(i = 0; i < l; index[i] = i++) {
         xTx[i] = 0.0D;
         Feature[] var24 = prob.x[i];
         int var25 = var24.length;

         for(int var26 = 0; var26 < var25; ++var26) {
            Feature xi = var24[var26];
            C = xi.getValue();
            xTx[i] += C * C;
            var10001 = xi.getIndex() - 1;
            w[var10001] += (double)y[i] * alpha[2 * i] * C;
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
            C = upper_bound[GETI(y, i)];
            double ywTx = 0.0D;
            double xisq = xTx[i];
            Feature[] var34 = prob.x[i];
            int var35 = var34.length;

            for(int var36 = 0; var36 < var35; ++var36) {
               Feature xi = var34[var36];
               ywTx += w[xi.getIndex() - 1] * xi.getValue();
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
               Feature[] var60 = prob.x[i];
               int var51 = var60.length;

               for(int var61 = 0; var61 < var51; ++var61) {
                  Feature xi = var60[var61];
                  var10001 = xi.getIndex() - 1;
                  w[var10001] += (double)sign * (z - alpha_old) * (double)yi * xi.getValue();
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

      info("Objective value = %g%n", v);
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
      double Gnorm1_init = -1.0D;
      double loss_old = 0.0D;
      int[] index = new int[w_size];
      byte[] y = new byte[l];
      double[] b = new double[l];
      double[] xj_sq = new double[w_size];
      double[] C = new double[]{Cn, 0.0D, Cp};

      int j;
      for(j = 0; j < w_size; ++j) {
         w[j] = 0.0D;
      }

      for(j = 0; j < l; ++j) {
         b[j] = 1.0D;
         if (prob_col.y[j] > 0.0D) {
            y[j] = 1;
         } else {
            y[j] = -1;
         }
      }

      Feature[] var51;
      int var52;
      int nnz;
      Feature xi;
      int ind;
      double val;
      for(j = 0; j < w_size; ++j) {
         index[j] = j;
         xj_sq[j] = 0.0D;
         var51 = prob_col.x[j];
         var52 = var51.length;

         for(nnz = 0; nnz < var52; ++nnz) {
            xi = var51[nnz];
            ind = xi.getIndex() - 1;
            xi.setValue(xi.getValue() * (double)y[ind]);
            val = xi.getValue();
            b[ind] -= w[j] * val;
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
            var51 = prob_col.x[j];
            var52 = var51.length;

            for(nnz = 0; nnz < var52; ++nnz) {
               xi = var51[nnz];
               ind = xi.getIndex() - 1;
               if (b[ind] > 0.0D) {
                  val = xi.getValue();
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
            if (Gp < H * w[j]) {
               d = -Gp / H;
            } else if (Gn > H * w[j]) {
               d = -Gn / H;
            } else {
               d = -w[j];
            }

            if (Math.abs(d) >= 1.0E-12D) {
               double delta = Math.abs(w[j] + d) - Math.abs(w[j]) + G_loss * d;
               double d_old = 0.0D;

               int var10001;
               int num_linesearch;
               int var62;
               label216:
               for(num_linesearch = 0; num_linesearch < max_num_linesearch; ++num_linesearch) {
                  double d_diff = d_old - d;
                  double cond = Math.abs(w[j] + d) - Math.abs(w[j]) - sigma * delta;
                  double appxcond = xj_sq[j] * d * d + G_loss * d + cond;
                  Feature[] var60;
                  int var61;
                  Feature x;
                  if (appxcond <= 0.0D) {
                     var60 = prob_col.x[j];
                     var61 = var60.length;
                     var62 = 0;

                     while(true) {
                        if (var62 >= var61) {
                           break label216;
                        }

                        x = var60[var62];
                        var10001 = x.getIndex() - 1;
                        b[var10001] += d_diff * x.getValue();
                        ++var62;
                     }
                  }

                  double loss_new;
                  int ind;
                  double b_new;
                  if (num_linesearch == 0) {
                     loss_old = 0.0D;
                     loss_new = 0.0D;
                     var60 = prob_col.x[j];
                     var61 = var60.length;

                     for(var62 = 0; var62 < var61; ++var62) {
                        x = var60[var62];
                        ind = x.getIndex() - 1;
                        if (b[ind] > 0.0D) {
                           loss_old += C[GETI(y, ind)] * b[ind] * b[ind];
                        }

                        b_new = b[ind] + d_diff * x.getValue();
                        b[ind] = b_new;
                        if (b_new > 0.0D) {
                           loss_new += C[GETI(y, ind)] * b_new * b_new;
                        }
                     }
                  } else {
                     loss_new = 0.0D;
                     var60 = prob_col.x[j];
                     var61 = var60.length;

                     for(var62 = 0; var62 < var61; ++var62) {
                        x = var60[var62];
                        ind = x.getIndex() - 1;
                        b_new = b[ind] + d_diff * x.getValue();
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
                        Feature[] var75 = prob_col.x[i];
                        var62 = var75.length;

                        for(int var76 = 0; var76 < var62; ++var76) {
                           Feature x = var75[var76];
                           var10001 = x.getIndex() - 1;
                           b[var10001] -= w[i] * x.getValue();
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
      nnz = 0;

      for(j = 0; j < w_size; ++j) {
         Feature[] var70 = prob_col.x[j];
         ind = var70.length;

         for(int var72 = 0; var72 < ind; ++var72) {
            Feature x = var70[var72];
            x.setValue(x.getValue() * prob_col.y[x.getIndex() - 1]);
         }

         if (w[j] != 0.0D) {
            Gp += Math.abs(w[j]);
            ++nnz;
         }
      }

      for(j = 0; j < l; ++j) {
         if (b[j] > 0.0D) {
            Gp += C[GETI(y, j)] * b[j] * b[j];
         }
      }

      info("Objective value = %g%n", Gp);
      info("#nonzeros/#features = %d/%d%n", nnz, w_size);
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
      double Gnorm1_init = -1.0D;
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
      for(j = 0; j < w_size; ++j) {
         w[j] = 0.0D;
      }

      for(j = 0; j < l; ++j) {
         if (prob_col.y[j] > 0.0D) {
            y[j] = 1;
         } else {
            y[j] = -1;
         }

         exp_wTx[j] = 0.0D;
      }

      double w_norm = 0.0D;

      Feature[] var67;
      int i;
      int nnz;
      Feature x;
      int ind;
      for(j = 0; j < w_size; ++j) {
         w_norm += Math.abs(w[j]);
         wpd[j] = w[j];
         index[j] = j;
         xjneg_sum[j] = 0.0D;
         var67 = prob_col.x[j];
         i = var67.length;

         for(nnz = 0; nnz < i; ++nnz) {
            x = var67[nnz];
            ind = x.getIndex() - 1;
            double val = x.getValue();
            exp_wTx[ind] += w[j] * val;
            if (y[ind] == -1) {
               xjneg_sum[j] += C[GETI(y, ind)] * val;
            }
         }
      }

      double Gp;
      for(j = 0; j < l; ++j) {
         exp_wTx[j] = Math.exp(exp_wTx[j]);
         Gp = 1.0D / (1.0D + exp_wTx[j]);
         tau[j] = C[GETI(y, j)] * Gp;
         D[j] = C[GETI(y, j)] * exp_wTx[j] * Gp * Gp;
      }

      while(newton_iter < max_newton_iter) {
         double Gmax_new = 0.0D;
         double Gnorm1_new = 0.0D;
         int active_size = w_size;

         int s;
         Feature[] var81;
         double tau_tmp;
         int var83;
         double violation;
         Feature x;
         for(s = 0; s < active_size; ++s) {
            j = index[s];
            Hdiag[j] = nu;
            Grad[j] = 0.0D;
            Gp = 0.0D;
            var81 = prob_col.x[j];
            var83 = var81.length;

            for(ind = 0; ind < var83; ++ind) {
               x = var81[ind];
               int ind = x.getIndex() - 1;
               Hdiag[j] += x.getValue() * x.getValue() * D[ind];
               Gp += x.getValue() * tau[ind];
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
               var67 = prob_col.x[j];
               i = var67.length;

               for(nnz = 0; nnz < i; ++nnz) {
                  x = var67[nnz];
                  ind = x.getIndex() - 1;
                  G += x.getValue() * D[ind] * xTd[ind];
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
               if (Gp < H * wpd[j]) {
                  z = -Gp / H;
               } else if (tau_tmp > H * wpd[j]) {
                  z = -tau_tmp / H;
               } else {
                  z = -wpd[j];
               }

               if (Math.abs(z) >= 1.0E-12D) {
                  z = Math.min(Math.max(z, -10.0D), 10.0D);
                  wpd[j] += z;
                  Feature[] var87 = prob_col.x[j];
                  int var74 = var87.length;

                  for(int var75 = 0; var75 < var74; ++var75) {
                     Feature x = var87[var75];
                     int ind = x.getIndex() - 1;
                     xTd[ind] += x.getValue() * z;
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
            info("WARNING: reaching max number of inner iterations%n");
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

         label252:
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
                     break label252;
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
                  var81 = prob_col.x[i];
                  var83 = var81.length;

                  for(ind = 0; ind < var83; ++ind) {
                     x = var81[ind];
                     int var10001 = x.getIndex() - 1;
                     exp_wTx[var10001] += w[i] * x.getValue();
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
      nnz = 0;

      for(j = 0; j < w_size; ++j) {
         if (w[j] != 0.0D) {
            Gp += Math.abs(w[j]);
            ++nnz;
         }
      }

      for(j = 0; j < l; ++j) {
         if (y[j] == 1) {
            Gp += C[GETI(y, j)] * Math.log(1.0D + 1.0D / exp_wTx[j]);
         } else {
            Gp += C[GETI(y, j)] * Math.log(1.0D + exp_wTx[j]);
         }
      }

      info("Objective value = %g%n", Gp);
      info("#nonzeros/#features = %d/%d%n", nnz, w_size);
   }

   static Problem transpose(Problem prob) {
      int l = prob.l;
      int n = prob.n;
      int[] col_ptr = new int[n + 1];
      Problem prob_col = new Problem();
      prob_col.l = l;
      prob_col.n = n;
      prob_col.y = new double[l];
      prob_col.x = new Feature[n][];

      int i;
      for(i = 0; i < l; ++i) {
         prob_col.y[i] = prob.y[i];
      }

      int index;
      for(i = 0; i < l; ++i) {
         Feature[] var6 = prob.x[i];
         int var7 = var6.length;

         for(index = 0; index < var7; ++index) {
            Feature x = var6[index];
            ++col_ptr[x.getIndex()];
         }
      }

      for(i = 0; i < n; ++i) {
         prob_col.x[i] = new Feature[col_ptr[i + 1]];
         col_ptr[i] = 0;
      }

      for(i = 0; i < l; ++i) {
         for(int j = 0; j < prob.x[i].length; ++j) {
            Feature x = prob.x[i][j];
            index = x.getIndex() - 1;
            prob_col.x[index][col_ptr[index]] = new FeatureNode(i + 1, x.getValue());
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
      } else if (prob.n == 0) {
         throw new IllegalArgumentException("problem has zero features");
      } else if (prob.l == 0) {
         throw new IllegalArgumentException("problem has zero instances");
      } else {
         Feature[][] var2 = prob.x;
         int n = var2.length;

         int w_size;
         int nr_class;
         for(w_size = 0; w_size < n; ++w_size) {
            Feature[] nodes = var2[w_size];
            int indexBefore = 0;
            Feature[] var7 = nodes;
            nr_class = nodes.length;

            for(int var9 = 0; var9 < nr_class; ++var9) {
               Feature n = var7[var9];
               if (n.getIndex() <= indexBefore) {
                  throw new IllegalArgumentException("feature nodes must be sorted by index in ascending order");
               }

               indexBefore = n.getIndex();
            }
         }

         int l = prob.l;
         n = prob.n;
         w_size = prob.n;
         Model model = new Model();
         if (prob.bias >= 0.0D) {
            model.nr_feature = n - 1;
         } else {
            model.nr_feature = n;
         }

         model.solverType = param.solverType;
         model.bias = prob.bias;
         if (param.solverType.isSupportVectorRegression()) {
            model.w = new double[w_size];
            model.nr_class = 2;
            model.label = null;
            checkProblemSize(n, model.nr_class);
            train_one(prob, param, model.w, 0.0D, 0.0D);
         } else {
            int[] perm = new int[l];
            Linear.GroupClassesReturn rv = groupClasses(prob, perm);
            nr_class = rv.nr_class;
            int[] label = rv.label;
            int[] start = rv.start;
            int[] count = rv.count;
            checkProblemSize(n, nr_class);
            model.nr_class = nr_class;
            model.label = new int[nr_class];

            for(int i = 0; i < nr_class; ++i) {
               model.label[i] = label[i];
            }

            double[] weighted_C = new double[nr_class];

            int i;
            for(i = 0; i < nr_class; ++i) {
               weighted_C[i] = param.C;
            }

            int j;
            for(i = 0; i < param.getNumWeights(); ++i) {
               for(j = 0; j < nr_class && param.weightLabel[i] != label[j]; ++j) {
               }

               if (j == nr_class) {
                  throw new IllegalArgumentException("class label " + param.weightLabel[i] + " specified in weight is not found");
               }

               weighted_C[j] *= param.weight[i];
            }

            Feature[][] x = new Feature[l][];

            for(j = 0; j < l; ++j) {
               x[j] = prob.x[perm[j]];
            }

            Problem sub_prob = new Problem();
            sub_prob.l = l;
            sub_prob.n = n;
            sub_prob.x = new Feature[sub_prob.l][];
            sub_prob.y = new double[sub_prob.l];

            int i;
            for(i = 0; i < sub_prob.l; ++i) {
               sub_prob.x[i] = x[i];
            }

            int i;
            if (param.solverType == SolverType.MCSVM_CS) {
               model.w = new double[n * nr_class];

               for(i = 0; i < nr_class; ++i) {
                  for(i = start[i]; i < start[i] + count[i]; ++i) {
                     sub_prob.y[i] = (double)i;
                  }
               }

               SolverMCSVM_CS solver = new SolverMCSVM_CS(sub_prob, nr_class, weighted_C, param.eps);
               solver.solve(model.w);
            } else if (nr_class == 2) {
               model.w = new double[w_size];
               i = start[0] + count[0];

               for(i = 0; i < i; ++i) {
                  sub_prob.y[i] = 1.0D;
               }

               while(i < sub_prob.l) {
                  sub_prob.y[i] = -1.0D;
                  ++i;
               }

               train_one(sub_prob, param, model.w, weighted_C[0], weighted_C[1]);
            } else {
               model.w = new double[w_size * nr_class];
               double[] w = new double[w_size];

               for(i = 0; i < nr_class; ++i) {
                  int si = start[i];
                  int ei = si + count[i];

                  int k;
                  for(k = 0; k < si; ++k) {
                     sub_prob.y[k] = -1.0D;
                  }

                  while(k < ei) {
                     sub_prob.y[k] = 1.0D;
                     ++k;
                  }

                  while(k < sub_prob.l) {
                     sub_prob.y[k] = -1.0D;
                     ++k;
                  }

                  train_one(sub_prob, param, w, weighted_C[i], param.C);

                  for(int j = 0; j < n; ++j) {
                     model.w[j * nr_class + i] = w[j];
                  }
               }
            }
         }

         return model;
      }
   }

   private static void checkProblemSize(int n, int nr_class) {
      if (n >= 2147483647 / nr_class || n * nr_class < 0) {
         throw new IllegalArgumentException("'number of classes' * 'number of instances' is too large: " + nr_class + "*" + n);
      }
   }

   private static void train_one(Problem prob, Parameter param, double[] w, double Cp, double Cn) {
      double eps = param.eps;
      int pos = 0;

      int i;
      for(i = 0; i < prob.l; ++i) {
         if (prob.y[i] > 0.0D) {
            ++pos;
         }
      }

      i = prob.l - pos;
      double primal_solver_tol = eps * (double)Math.max(Math.min(pos, i), 1) / (double)prob.l;
      Function fun_obj = null;
      double[] C;
      int i;
      Problem prob_col;
      Tron tron_obj;
      switch(param.solverType) {
      case L2R_LR:
         C = new double[prob.l];

         for(i = 0; i < prob.l; ++i) {
            if (prob.y[i] > 0.0D) {
               C[i] = Cp;
            } else {
               C[i] = Cn;
            }
         }

         Function fun_obj = new L2R_LrFunction(prob, C);
         tron_obj = new Tron(fun_obj, primal_solver_tol);
         tron_obj.tron(w);
         break;
      case L2R_L2LOSS_SVC:
         C = new double[prob.l];

         for(i = 0; i < prob.l; ++i) {
            if (prob.y[i] > 0.0D) {
               C[i] = Cp;
            } else {
               C[i] = Cn;
            }
         }

         Function fun_obj = new L2R_L2_SvcFunction(prob, C);
         tron_obj = new Tron(fun_obj, primal_solver_tol);
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
         solve_l1r_l2_svc(prob_col, w, primal_solver_tol, Cp, Cn);
         break;
      case L1R_LR:
         prob_col = transpose(prob);
         solve_l1r_lr(prob_col, w, primal_solver_tol, Cp, Cn);
         break;
      case L2R_LR_DUAL:
         solve_l2r_lr_dual(prob, w, eps, Cp, Cn);
         break;
      case L2R_L2LOSS_SVR:
         C = new double[prob.l];

         for(i = 0; i < prob.l; ++i) {
            C[i] = param.C;
         }

         fun_obj = new L2R_L2_SvrFunction(prob, C, param.p);
         tron_obj = new Tron(fun_obj, param.eps);
         tron_obj.tron(w);
         break;
      case L2R_L1LOSS_SVR_DUAL:
      case L2R_L2LOSS_SVR_DUAL:
         solve_l2r_l1l2_svr(prob, w, param);
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
