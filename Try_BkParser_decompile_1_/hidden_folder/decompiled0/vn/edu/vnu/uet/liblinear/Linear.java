/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;
import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.FeatureNode;
import vn.edu.vnu.uet.liblinear.Function;
import vn.edu.vnu.uet.liblinear.IntArrayPointer;
import vn.edu.vnu.uet.liblinear.L2R_L2_SvcFunction;
import vn.edu.vnu.uet.liblinear.L2R_L2_SvrFunction;
import vn.edu.vnu.uet.liblinear.L2R_LrFunction;
import vn.edu.vnu.uet.liblinear.Model;
import vn.edu.vnu.uet.liblinear.Parameter;
import vn.edu.vnu.uet.liblinear.Problem;
import vn.edu.vnu.uet.liblinear.SolverMCSVM_CS;
import vn.edu.vnu.uet.liblinear.SolverType;
import vn.edu.vnu.uet.liblinear.Tron;

public class Linear {
    static final Charset FILE_CHARSET = Charset.forName("ISO-8859-1");
    static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static Object OUTPUT_MUTEX = new Object();
    private static PrintStream DEBUG_OUTPUT = System.out;
    private static final long DEFAULT_RANDOM_SEED = 0L;
    static Random random = new Random(0L);

    public static void crossValidation(Problem prob, Parameter param, int nr_fold, double[] target) {
        int i;
        int l = prob.l;
        int[] perm = new int[l];
        if (nr_fold > l) {
            nr_fold = l;
            System.err.println("WARNING: # folds > # data. Will use # folds = # data instead (i.e., leave-one-out cross validation)");
        }
        int[] fold_start = new int[nr_fold + 1];
        for (i = 0; i < l; ++i) {
            perm[i] = i;
        }
        for (i = 0; i < l; ++i) {
            int j = i + random.nextInt(l - i);
            Linear.swap(perm, i, j);
        }
        for (i = 0; i <= nr_fold; ++i) {
            fold_start[i] = i * l / nr_fold;
        }
        for (i = 0; i < nr_fold; ++i) {
            int j;
            int begin = fold_start[i];
            int end = fold_start[i + 1];
            Problem subprob = new Problem();
            subprob.bias = prob.bias;
            subprob.n = prob.n;
            subprob.l = l - (end - begin);
            subprob.x = new Feature[subprob.l][];
            subprob.y = new double[subprob.l];
            int k = 0;
            for (j = 0; j < begin; ++j) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            for (j = end; j < l; ++j) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            Model submodel = Linear.train(subprob, param);
            for (j = begin; j < end; ++j) {
                target[perm[j]] = Linear.predict(submodel, prob.x[perm[j]]);
            }
        }
    }

    private static GroupClassesReturn groupClasses(Problem prob, int[] perm) {
        int i;
        int l = prob.l;
        int max_nr_class = 16;
        int nr_class = 0;
        int[] label = new int[max_nr_class];
        int[] count = new int[max_nr_class];
        int[] data_label = new int[l];
        for (i = 0; i < l; ++i) {
            int j;
            int this_label = (int)prob.y[i];
            for (j = 0; j < nr_class; ++j) {
                if (this_label != label[j]) continue;
                int[] arrn = count;
                int n = j;
                arrn[n] = arrn[n] + 1;
                break;
            }
            data_label[i] = j;
            if (j != nr_class) continue;
            if (nr_class == max_nr_class) {
                label = Linear.copyOf(label, max_nr_class *= 2);
                count = Linear.copyOf(count, max_nr_class);
            }
            label[nr_class] = this_label;
            count[nr_class] = 1;
            ++nr_class;
        }
        if (nr_class == 2 && label[0] == -1 && label[1] == 1) {
            Linear.swap(label, 0, 1);
            Linear.swap(count, 0, 1);
            for (i = 0; i < l; ++i) {
                data_label[i] = data_label[i] == 0 ? 1 : 0;
            }
        }
        int[] start = new int[nr_class];
        start[0] = 0;
        for (i = 1; i < nr_class; ++i) {
            start[i] = start[i - 1] + count[i - 1];
        }
        for (i = 0; i < l; ++i) {
            perm[start[data_label[i]]] = i;
            int[] arrn = start;
            int n = data_label[i];
            arrn[n] = arrn[n] + 1;
        }
        start[0] = 0;
        for (i = 1; i < nr_class; ++i) {
            start[i] = start[i - 1] + count[i - 1];
        }
        return new GroupClassesReturn(nr_class, label, start, count);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void info(String message) {
        Object object = OUTPUT_MUTEX;
        synchronized (object) {
            if (DEBUG_OUTPUT == null) {
                return;
            }
            DEBUG_OUTPUT.printf(message, new Object[0]);
            DEBUG_OUTPUT.flush();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void info(String format, Object ... args) {
        Object object = OUTPUT_MUTEX;
        synchronized (object) {
            if (DEBUG_OUTPUT == null) {
                return;
            }
            DEBUG_OUTPUT.printf(format, args);
            DEBUG_OUTPUT.flush();
        }
    }

    static double atof(String s) {
        if (s == null || s.length() < 1) {
            throw new IllegalArgumentException("Can't convert empty string to integer");
        }
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("NaN or Infinity in input: " + s);
        }
        return d;
    }

    static int atoi(String s) throws NumberFormatException {
        if (s == null || s.length() < 1) {
            throw new IllegalArgumentException("Can't convert empty string to integer");
        }
        if (s.charAt(0) == '+') {
            s = s.substring(1);
        }
        return Integer.parseInt(s);
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
        reader = inputReader instanceof BufferedReader ? (BufferedReader)inputReader : new BufferedReader(inputReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] split = whitespace.split(line);
            if (split[0].equals("solver_type")) {
                SolverType solver = SolverType.valueOf(split[1]);
                if (solver == null) {
                    throw new RuntimeException("unknown solver type");
                }
                model.solverType = solver;
                continue;
            }
            if (split[0].equals("nr_class")) {
                model.nr_class = Linear.atoi(split[1]);
                Integer.parseInt(split[1]);
                continue;
            }
            if (split[0].equals("nr_feature")) {
                model.nr_feature = Linear.atoi(split[1]);
                continue;
            }
            if (split[0].equals("bias")) {
                model.bias = Linear.atof(split[1]);
                continue;
            }
            if (split[0].equals("w")) break;
            if (split[0].equals("label")) {
                model.label = new int[model.nr_class];
                for (int i = 0; i < model.nr_class; ++i) {
                    model.label[i] = Linear.atoi(split[i + 1]);
                }
                continue;
            }
            throw new RuntimeException("unknown text in model file: [" + line + "]");
        }
        int w_size = model.nr_feature;
        if (model.bias >= 0.0) {
            ++w_size;
        }
        int nr_w = model.nr_class;
        if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) {
            nr_w = 1;
        }
        model.w = new double[w_size * nr_w];
        int[] buffer = new int[128];
        for (int i = 0; i < w_size; ++i) {
            for (int j = 0; j < nr_w; ++j) {
                int b = 0;
                do {
                    int ch;
                    if ((ch = reader.read()) == -1) {
                        throw new EOFException("unexpected EOF");
                    }
                    if (ch == 32) break;
                    buffer[b++] = ch;
                } while (true);
                model.w[i * nr_w + j] = Linear.atof(new String(buffer, 0, b));
            }
        }
        return model;
    }

    public static Model loadModel(File modelFile) throws IOException {
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(modelFile), FILE_CHARSET));){
            Model model = Linear.loadModel(inputReader);
            return model;
        }
    }

    static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static double predict(Model model, Feature[] x) {
        double[] dec_values = new double[model.nr_class];
        return Linear.predictValues(model, x, dec_values);
    }

    public static double predict(Model model, Feature[] x, double[] dec_values) {
        return Linear.predictValues(model, x, dec_values);
    }

    public static double predictProbability(Model model, Feature[] x, double[] prob_estimates) throws IllegalArgumentException {
        if (!model.isProbabilityModel()) {
            StringBuilder sb = new StringBuilder("probability output is only supported for logistic regression");
            sb.append(". This is currently only supported by the following solvers: ");
            int i = 0;
            for (SolverType solverType : SolverType.values()) {
                if (!solverType.isLogisticRegressionSolver()) continue;
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(solverType.name());
            }
            throw new IllegalArgumentException(sb.toString());
        }
        int nr_class = model.nr_class;
        int nr_w = nr_class == 2 ? 1 : nr_class;
        double label = Linear.predictValues(model, x, prob_estimates);
        for (int i = 0; i < nr_w; ++i) {
            prob_estimates[i] = 1.0 / (1.0 + Math.exp(-prob_estimates[i]));
        }
        if (nr_class == 2) {
            prob_estimates[1] = 1.0 - prob_estimates[0];
        } else {
            int i;
            double sum = 0.0;
            for (i = 0; i < nr_class; ++i) {
                sum += prob_estimates[i];
            }
            for (i = 0; i < nr_class; ++i) {
                prob_estimates[i] = prob_estimates[i] / sum;
            }
        }
        return label;
    }

    public static double predictValues(Model model, Feature[] x, double[] dec_values) {
        int n = model.bias >= 0.0 ? model.nr_feature + 1 : model.nr_feature;
        double[] w = model.w;
        int nr_w = model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS ? 1 : model.nr_class;
        for (int i = 0; i < nr_w; ++i) {
            dec_values[i] = 0.0;
        }
        for (Feature lx : x) {
            int idx = lx.getIndex();
            if (idx > n) continue;
            for (int i = 0; i < nr_w; ++i) {
                double[] arrd = dec_values;
                int n2 = i;
                arrd[n2] = arrd[n2] + w[(idx - 1) * nr_w + i] * lx.getValue();
            }
        }
        if (model.nr_class == 2) {
            if (model.solverType.isSupportVectorRegression()) {
                return dec_values[0];
            }
            return dec_values[0] > 0.0 ? (double)model.label[0] : (double)model.label[1];
        }
        int dec_max_idx = 0;
        for (int i = 1; i < model.nr_class; ++i) {
            if (!(dec_values[i] > dec_values[dec_max_idx])) continue;
            dec_max_idx = i;
        }
        return model.label[dec_max_idx];
    }

    static void printf(Formatter formatter, String format, Object ... args) throws IOException {
        formatter.format(format, args);
        IOException ioException = formatter.ioException();
        if (ioException != null) {
            throw ioException;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveModel(Writer modelOutput, Model model) throws IOException {
        int nr_feature;
        int w_size = nr_feature = model.nr_feature;
        if (model.bias >= 0.0) {
            ++w_size;
        }
        int nr_w = model.nr_class;
        if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) {
            nr_w = 1;
        }
        try (Formatter formatter = new Formatter(modelOutput, DEFAULT_LOCALE);){
            int i;
            Linear.printf(formatter, "solver_type %s\n", model.solverType.name());
            Linear.printf(formatter, "nr_class %d\n", model.nr_class);
            if (model.label != null) {
                Linear.printf(formatter, "label", new Object[0]);
                for (i = 0; i < model.nr_class; ++i) {
                    Linear.printf(formatter, " %d", model.label[i]);
                }
                Linear.printf(formatter, "\n", new Object[0]);
            }
            Linear.printf(formatter, "nr_feature %d\n", nr_feature);
            Linear.printf(formatter, "bias %.16g\n", model.bias);
            Linear.printf(formatter, "w\n", new Object[0]);
            for (i = 0; i < w_size; ++i) {
                for (int j = 0; j < nr_w; ++j) {
                    double value = model.w[i * nr_w + j];
                    if (value == 0.0) {
                        Linear.printf(formatter, "%d ", 0);
                        continue;
                    }
                    Linear.printf(formatter, "%.16g ", value);
                }
                Linear.printf(formatter, "\n", new Object[0]);
            }
            formatter.flush();
            IOException ioException = formatter.ioException();
            if (ioException != null) {
                throw ioException;
            }
        }
    }

    public static void saveModel(File modelFile, Model model) throws IOException {
        BufferedWriter modelOutput = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(modelFile), FILE_CHARSET));
        Linear.saveModel(modelOutput, model);
    }

    private static int GETI(byte[] y, int i) {
        return y[i] + 1;
    }

    private static void solve_l2r_l1l2_svc(Problem prob, double[] w, double eps, double Cp, double Cn, SolverType solver_type) {
        int i;
        int l = prob.l;
        int w_size = prob.n;
        int iter = 0;
        double[] QD = new double[l];
        int max_iter = 1000;
        int[] index = new int[l];
        double[] alpha = new double[l];
        byte[] y = new byte[l];
        int active_size = l;
        double PGmax_old = Double.POSITIVE_INFINITY;
        double PGmin_old = Double.NEGATIVE_INFINITY;
        double[] diag = new double[]{0.5 / Cn, 0.0, 0.5 / Cp};
        double[] upper_bound = new double[]{Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY};
        if (solver_type == SolverType.L2R_L1LOSS_SVC_DUAL) {
            diag[0] = 0.0;
            diag[2] = 0.0;
            upper_bound[0] = Cn;
            upper_bound[2] = Cp;
        }
        for (i = 0; i < l; ++i) {
            y[i] = prob.y[i] > 0.0 ? 1 : -1;
        }
        for (i = 0; i < l; ++i) {
            alpha[i] = 0.0;
        }
        for (i = 0; i < w_size; ++i) {
            w[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            QD[i] = diag[Linear.GETI(y, i)];
            for (reference xi : prob.x[i]) {
                double val = xi.getValue();
                double[] arrd = QD;
                int n = i;
                arrd[n] = arrd[n] + val * val;
                double[] arrd2 = w;
                int n2 = xi.getIndex() - 1;
                arrd2[n2] = arrd2[n2] + (double)y[i] * alpha[i] * val;
            }
            index[i] = i;
        }
        while (iter < max_iter) {
            double PGmax_new = Double.NEGATIVE_INFINITY;
            double PGmin_new = Double.POSITIVE_INFINITY;
            for (i = 0; i < active_size; ++i) {
                int j = i + random.nextInt(active_size - i);
                Linear.swap(index, i, j);
            }
            for (int s = 0; s < active_size; ++s) {
                i = index[s];
                double G = 0.0;
                byte yi = y[i];
                Feature[] arrfeature = prob.x[i];
                int n = arrfeature.length;
                for (reference xi = (reference)false; xi < n; ++xi) {
                    Feature xi2 = arrfeature[xi];
                    G += w[xi2.getIndex() - 1] * xi2.getValue();
                }
                G = G * (double)yi - 1.0;
                double C = upper_bound[Linear.GETI(y, i)];
                G += alpha[i] * diag[Linear.GETI(y, i)];
                double PG = 0.0;
                if (alpha[i] == 0.0) {
                    if (G > PGmax_old) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                    if (G < 0.0) {
                        PG = G;
                    }
                } else if (alpha[i] == C) {
                    if (G < PGmin_old) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                    if (G > 0.0) {
                        PG = G;
                    }
                } else {
                    PG = G;
                }
                PGmax_new = Math.max(PGmax_new, PG);
                PGmin_new = Math.min(PGmin_new, PG);
                if (!(Math.abs(PG) > 1.0E-12)) continue;
                double alpha_old = alpha[i];
                alpha[i] = Math.min(Math.max(alpha[i] - G / QD[i], 0.0), C);
                double d = (alpha[i] - alpha_old) * (double)yi;
                for (Feature xi : prob.x[i]) {
                    double[] arrd = w;
                    int n3 = xi.getIndex() - 1;
                    arrd[n3] = arrd[n3] + d * xi.getValue();
                }
            }
            if (++iter % 10 == 0) {
                Linear.info(".");
            }
            if (PGmax_new - PGmin_new <= eps) {
                if (active_size == l) break;
                active_size = l;
                Linear.info("*");
                PGmax_old = Double.POSITIVE_INFINITY;
                PGmin_old = Double.NEGATIVE_INFINITY;
                continue;
            }
            PGmax_old = PGmax_new;
            PGmin_old = PGmin_new;
            if (PGmax_old <= 0.0) {
                PGmax_old = Double.POSITIVE_INFINITY;
            }
            if (!(PGmin_old >= 0.0)) continue;
            PGmin_old = Double.NEGATIVE_INFINITY;
        }
        Linear.info("%noptimization finished, #iter = %d%n", iter);
        if (iter >= max_iter) {
            Linear.info("%nWARNING: reaching max number of iterations%nUsing -s 2 may be faster (also see FAQ)%n%n");
        }
        double v = 0.0;
        int nSV = 0;
        for (i = 0; i < w_size; ++i) {
            v += w[i] * w[i];
        }
        for (i = 0; i < l; ++i) {
            v += alpha[i] * (alpha[i] * diag[Linear.GETI(y, i)] - 2.0);
            if (!(alpha[i] > 0.0)) continue;
            ++nSV;
        }
        Linear.info("Objective value = %g%n", v / 2.0);
        Linear.info("nSV = %d%n", nSV);
    }

    private static int GETI_SVR(int i) {
        return 0;
    }

    private static void solve_l2r_l1l2_svr(Problem prob, double[] w, Parameter param) {
        int i;
        int l = prob.l;
        double C = param.C;
        double p = param.p;
        int w_size = prob.n;
        double eps = param.eps;
        int iter = 0;
        int max_iter = param.getMaxIters();
        int active_size = l;
        int[] index = new int[l];
        double Gmax_old = Double.POSITIVE_INFINITY;
        double Gnorm1_init = -1.0;
        double[] beta = new double[l];
        double[] QD = new double[l];
        double[] y = prob.y;
        double[] lambda = new double[]{0.5 / C};
        double[] upper_bound = new double[]{Double.POSITIVE_INFINITY};
        if (param.solverType == SolverType.L2R_L1LOSS_SVR_DUAL) {
            lambda[0] = 0.0;
            upper_bound[0] = C;
        }
        for (i = 0; i < l; ++i) {
            beta[i] = 0.0;
        }
        for (i = 0; i < w_size; ++i) {
            w[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            QD[i] = 0.0;
            for (Feature xi : prob.x[i]) {
                double val = xi.getValue();
                double[] arrd = QD;
                int n = i;
                arrd[n] = arrd[n] + val * val;
                double[] arrd2 = w;
                int n2 = xi.getIndex() - 1;
                arrd2[n2] = arrd2[n2] + beta[i] * val;
            }
            index[i] = i;
        }
        while (iter < max_iter) {
            double Gmax_new = 0.0;
            double Gnorm1_new = 0.0;
            for (i = 0; i < active_size; ++i) {
                int j = i + random.nextInt(active_size - i);
                Linear.swap(index, i, j);
            }
            for (int s = 0; s < active_size; ++s) {
                i = index[s];
                double G = -y[i] + lambda[Linear.GETI_SVR(i)] * beta[i];
                double H = QD[i] + lambda[Linear.GETI_SVR(i)];
                for (Feature xi : prob.x[i]) {
                    int ind = xi.getIndex() - 1;
                    double val = xi.getValue();
                    G += val * w[ind];
                }
                double Gp = G + p;
                double Gn = G - p;
                double violation = 0.0;
                if (beta[i] == 0.0) {
                    if (Gp < 0.0) {
                        violation = -Gp;
                    } else if (Gn > 0.0) {
                        violation = Gn;
                    } else if (Gp > Gmax_old && Gn < -Gmax_old) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                } else if (beta[i] >= upper_bound[Linear.GETI_SVR(i)]) {
                    if (Gp > 0.0) {
                        violation = Gp;
                    } else if (Gp < -Gmax_old) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                } else if (beta[i] <= -upper_bound[Linear.GETI_SVR(i)]) {
                    if (Gn < 0.0) {
                        violation = -Gn;
                    } else if (Gn > Gmax_old) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                } else {
                    violation = beta[i] > 0.0 ? Math.abs(Gp) : Math.abs(Gn);
                }
                Gmax_new = Math.max(Gmax_new, violation);
                Gnorm1_new += violation;
                double d = Gp < H * beta[i] ? -Gp / H : (Gn > H * beta[i] ? -Gn / H : -beta[i]);
                if (Math.abs(d) < 1.0E-12) continue;
                double beta_old = beta[i];
                beta[i] = Math.min(Math.max(beta[i] + d, -upper_bound[Linear.GETI_SVR(i)]), upper_bound[Linear.GETI_SVR(i)]);
                d = beta[i] - beta_old;
                if (d == 0.0) continue;
                for (Feature xi : prob.x[i]) {
                    double[] arrd = w;
                    int n = xi.getIndex() - 1;
                    arrd[n] = arrd[n] + d * xi.getValue();
                }
            }
            if (iter == 0) {
                Gnorm1_init = Gnorm1_new;
            }
            if (++iter % 10 == 0) {
                Linear.info(".");
            }
            if (Gnorm1_new <= eps * Gnorm1_init) {
                if (active_size == l) break;
                active_size = l;
                Linear.info("*");
                Gmax_old = Double.POSITIVE_INFINITY;
                continue;
            }
            Gmax_old = Gmax_new;
        }
        Linear.info("%noptimization finished, #iter = %d%n", iter);
        if (iter >= max_iter) {
            Linear.info("%nWARNING: reaching max number of iterations%nUsing -s 11 may be faster%n%n");
        }
        double v = 0.0;
        int nSV = 0;
        for (i = 0; i < w_size; ++i) {
            v += w[i] * w[i];
        }
        v = 0.5 * v;
        for (i = 0; i < l; ++i) {
            v += p * Math.abs(beta[i]) - y[i] * beta[i] + 0.5 * lambda[Linear.GETI_SVR(i)] * beta[i] * beta[i];
            if (beta[i] == 0.0) continue;
            ++nSV;
        }
        Linear.info("Objective value = %g%n", v);
        Linear.info("nSV = %d%n", nSV);
    }

    private static void solve_l2r_lr_dual(Problem prob, double[] w, double eps, double Cp, double Cn) {
        int i;
        int l = prob.l;
        int w_size = prob.n;
        int iter = 0;
        double[] xTx = new double[l];
        int max_iter = 1000;
        int[] index = new int[l];
        double[] alpha = new double[2 * l];
        byte[] y = new byte[l];
        int max_inner_iter = 100;
        double innereps = 0.01;
        double innereps_min = Math.min(1.0E-8, eps);
        double[] upper_bound = new double[]{Cn, 0.0, Cp};
        for (i = 0; i < l; ++i) {
            y[i] = prob.y[i] > 0.0 ? 1 : -1;
        }
        for (i = 0; i < l; ++i) {
            alpha[2 * i] = Math.min(0.001 * upper_bound[Linear.GETI(y, i)], 1.0E-8);
            alpha[2 * i + 1] = upper_bound[Linear.GETI(y, i)] - alpha[2 * i];
        }
        for (i = 0; i < w_size; ++i) {
            w[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            xTx[i] = 0.0;
            for (Feature xi : prob.x[i]) {
                double val = xi.getValue();
                double[] arrd = xTx;
                int n = i;
                arrd[n] = arrd[n] + val * val;
                double[] arrd2 = w;
                int n2 = xi.getIndex() - 1;
                arrd2[n2] = arrd2[n2] + (double)y[i] * alpha[2 * i] * val;
            }
            index[i] = i;
        }
        while (iter < max_iter) {
            for (i = 0; i < l; ++i) {
                int j = i + random.nextInt(l - i);
                Linear.swap(index, i, j);
            }
            int newton_iter = 0;
            double Gmax = 0.0;
            for (int s = 0; s < l; ++s) {
                double tmpz;
                double gpp;
                int inner_iter;
                double alpha_old;
                double z;
                i = index[s];
                byte yi = y[i];
                double C = upper_bound[Linear.GETI(y, i)];
                double ywTx = 0.0;
                double xisq = xTx[i];
                for (Feature xi : prob.x[i]) {
                    ywTx += w[xi.getIndex() - 1] * xi.getValue();
                }
                double a = xisq;
                double b = ywTx *= (double)y[i];
                int ind1 = 2 * i;
                int ind2 = 2 * i + 1;
                int sign = 1;
                if (0.5 * a * (alpha[ind2] - alpha[ind1]) + b < 0.0) {
                    ind1 = 2 * i + 1;
                    ind2 = 2 * i;
                    sign = -1;
                }
                if (C - (z = (alpha_old = alpha[ind1])) < 0.5 * C) {
                    z = 0.1 * z;
                }
                double gp = a * (z - alpha_old) + (double)sign * b + Math.log(z / (C - z));
                Gmax = Math.max(Gmax, Math.abs(gp));
                double eta = 0.1;
                for (inner_iter = 0; inner_iter <= max_inner_iter && !(Math.abs(gp) < innereps); ++inner_iter) {
                    gpp = a + C / (C - z) / z;
                    tmpz = z - gp / gpp;
                    z = tmpz <= 0.0 ? (z *= 0.1) : tmpz;
                    gp = a * (z - alpha_old) + (double)sign * b + Math.log(z / (C - z));
                    ++newton_iter;
                }
                if (inner_iter <= 0) continue;
                alpha[ind1] = z;
                alpha[ind2] = C - z;
                gpp = (double)prob.x[i];
                int n = ((double)gpp).length;
                for (tmpz = (double)false; tmpz < n; ++tmpz) {
                    void xi = gpp[tmpz];
                    double[] arrd = w;
                    int n3 = xi.getIndex() - 1;
                    arrd[n3] = arrd[n3] + (double)sign * (z - alpha_old) * (double)yi * xi.getValue();
                }
            }
            if (++iter % 10 == 0) {
                Linear.info(".");
            }
            if (Gmax < eps) break;
            if (newton_iter > l / 10) continue;
            innereps = Math.max(innereps_min, 0.1 * innereps);
        }
        Linear.info("%noptimization finished, #iter = %d%n", iter);
        if (iter >= max_iter) {
            Linear.info("%nWARNING: reaching max number of iterations%nUsing -s 0 may be faster (also see FAQ)%n%n");
        }
        double v = 0.0;
        for (i = 0; i < w_size; ++i) {
            v += w[i] * w[i];
        }
        v *= 0.5;
        for (i = 0; i < l; ++i) {
            v += alpha[2 * i] * Math.log(alpha[2 * i]) + alpha[2 * i + 1] * Math.log(alpha[2 * i + 1]) - upper_bound[Linear.GETI(y, i)] * Math.log(upper_bound[Linear.GETI(y, i)]);
        }
        Linear.info("Objective value = %g%n", v);
    }

    private static void solve_l1r_l2_svc(Problem prob_col, double[] w, double eps, double Cp, double Cn) {
        int val;
        int j;
        int l = prob_col.l;
        int w_size = prob_col.n;
        int iter = 0;
        int max_iter = 1000;
        int active_size = w_size;
        int max_num_linesearch = 20;
        double sigma = 0.01;
        double Gmax_old = Double.POSITIVE_INFINITY;
        double Gnorm1_init = -1.0;
        double loss_old = 0.0;
        int[] index = new int[w_size];
        byte[] y = new byte[l];
        double[] b = new double[l];
        double[] xj_sq = new double[w_size];
        double[] C = new double[]{Cn, 0.0, Cp};
        for (j = 0; j < w_size; ++j) {
            w[j] = 0.0;
        }
        for (j = 0; j < l; ++j) {
            b[j] = 1.0;
            y[j] = prob_col.y[j] > 0.0 ? 1 : -1;
        }
        for (j = 0; j < w_size; ++j) {
            index[j] = j;
            xj_sq[j] = 0.0;
            for (Feature xi : prob_col.x[j]) {
                int ind = xi.getIndex() - 1;
                xi.setValue(xi.getValue() * (double)y[ind]);
                val = (int)xi.getValue();
                double[] arrd = b;
                int n = ind;
                arrd[n] = arrd[n] - w[j] * val;
                double[] arrd2 = xj_sq;
                int n2 = j;
                arrd2[n2] = arrd2[n2] + C[Linear.GETI(y, ind)] * val * val;
            }
        }
        while (iter < max_iter) {
            double Gmax_new = 0.0;
            double Gnorm1_new = 0.0;
            for (j = 0; j < active_size; ++j) {
                int i = j + random.nextInt(active_size - j);
                Linear.swap(index, i, j);
            }
            for (int s = 0; s < active_size; ++s) {
                int num_linesearch;
                int i;
                j = index[s];
                double G_loss = 0.0;
                double H = 0.0;
                for (Feature xi : prob_col.x[j]) {
                    int ind = xi.getIndex() - 1;
                    if (!(b[ind] > 0.0)) continue;
                    val = (int)xi.getValue();
                    double tmp = C[Linear.GETI(y, ind)] * val;
                    G_loss -= tmp * b[ind];
                    H += tmp * val;
                }
                double G = G_loss *= 2.0;
                H *= 2.0;
                H = Math.max(H, 1.0E-12);
                double Gp = G + 1.0;
                double Gn = G - 1.0;
                double violation = 0.0;
                if (w[j] == 0.0) {
                    if (Gp < 0.0) {
                        violation = -Gp;
                    } else if (Gn > 0.0) {
                        violation = Gn;
                    } else if (Gp > Gmax_old / (double)l && Gn < -Gmax_old / (double)l) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                } else {
                    violation = w[j] > 0.0 ? Math.abs(Gp) : Math.abs(Gn);
                }
                Gmax_new = Math.max(Gmax_new, violation);
                Gnorm1_new += violation;
                double d = Gp < H * w[j] ? -Gp / H : (Gn > H * w[j] ? -Gn / H : -w[j]);
                if (Math.abs(d) < 1.0E-12) continue;
                double delta = Math.abs(w[j] + d) - Math.abs(w[j]) + G * d;
                double d_old = 0.0;
                for (num_linesearch = 0; num_linesearch < max_num_linesearch; ++num_linesearch) {
                    double b_new;
                    double loss_new;
                    int ind;
                    double d_diff = d_old - d;
                    double cond = Math.abs(w[j] + d) - Math.abs(w[j]) - sigma * delta;
                    double appxcond = xj_sq[j] * d * d + G_loss * d + cond;
                    if (appxcond <= 0.0) {
                        for (Feature x : prob_col.x[j]) {
                            double[] arrd = b;
                            int n = x.getIndex() - 1;
                            arrd[n] = arrd[n] + d_diff * x.getValue();
                        }
                        break;
                    }
                    if (num_linesearch == 0) {
                        loss_old = 0.0;
                        loss_new = 0.0;
                        for (Feature x : prob_col.x[j]) {
                            ind = x.getIndex() - 1;
                            if (b[ind] > 0.0) {
                                loss_old += C[Linear.GETI(y, ind)] * b[ind] * b[ind];
                            }
                            b[ind] = b_new = b[ind] + d_diff * x.getValue();
                            if (!(b_new > 0.0)) continue;
                            loss_new += C[Linear.GETI(y, ind)] * b_new * b_new;
                        }
                    } else {
                        loss_new = 0.0;
                        for (Feature x : prob_col.x[j]) {
                            ind = x.getIndex() - 1;
                            b[ind] = b_new = b[ind] + d_diff * x.getValue();
                            if (!(b_new > 0.0)) continue;
                            loss_new += C[Linear.GETI(y, ind)] * b_new * b_new;
                        }
                    }
                    if ((cond = cond + loss_new - loss_old) <= 0.0) break;
                    d_old = d;
                    d *= 0.5;
                    delta *= 0.5;
                }
                double[] arrd = w;
                int n = j;
                arrd[n] = arrd[n] + d;
                if (num_linesearch < max_num_linesearch) continue;
                Linear.info("#");
                for (i = 0; i < l; ++i) {
                    b[i] = 1.0;
                }
                for (i = 0; i < w_size; ++i) {
                    if (w[i] == 0.0) continue;
                    for (Feature x : prob_col.x[i]) {
                        double[] arrd3 = b;
                        int n3 = x.getIndex() - 1;
                        arrd3[n3] = arrd3[n3] - w[i] * x.getValue();
                    }
                }
            }
            if (iter == 0) {
                Gnorm1_init = Gnorm1_new;
            }
            if (++iter % 10 == 0) {
                Linear.info(".");
            }
            if (Gmax_new <= eps * Gnorm1_init) {
                if (active_size == w_size) break;
                active_size = w_size;
                Linear.info("*");
                Gmax_old = Double.POSITIVE_INFINITY;
                continue;
            }
            Gmax_old = Gmax_new;
        }
        Linear.info("%noptimization finished, #iter = %d%n", iter);
        if (iter >= max_iter) {
            Linear.info("%nWARNING: reaching max number of iterations%n");
        }
        double v = 0.0;
        int nnz = 0;
        for (j = 0; j < w_size; ++j) {
            for (Feature x : prob_col.x[j]) {
                x.setValue(x.getValue() * prob_col.y[x.getIndex() - 1]);
            }
            if (w[j] == 0.0) continue;
            v += Math.abs(w[j]);
            ++nnz;
        }
        for (j = 0; j < l; ++j) {
            if (!(b[j] > 0.0)) continue;
            v += C[Linear.GETI(y, j)] * b[j] * b[j];
        }
        Linear.info("Objective value = %g%n", v);
        Linear.info("#nonzeros/#features = %d/%d%n", nnz, w_size);
    }

    private static void solve_l1r_lr(Problem prob_col, double[] w, double eps, double Cp, double Cn) {
        int j;
        int l = prob_col.l;
        int w_size = prob_col.n;
        int newton_iter = 0;
        int iter = 0;
        int max_newton_iter = 100;
        int max_iter = 1000;
        int max_num_linesearch = 20;
        double nu = 1.0E-12;
        double inner_eps = 1.0;
        double sigma = 0.01;
        double Gnorm1_init = -1.0;
        double Gmax_old = Double.POSITIVE_INFINITY;
        double QP_Gmax_old = Double.POSITIVE_INFINITY;
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
        double[] C = new double[]{Cn, 0.0, Cp};
        for (j = 0; j < w_size; ++j) {
            w[j] = 0.0;
        }
        for (j = 0; j < l; ++j) {
            y[j] = prob_col.y[j] > 0.0 ? 1 : -1;
            exp_wTx[j] = 0.0;
        }
        double w_norm = 0.0;
        for (j = 0; j < w_size; ++j) {
            w_norm += Math.abs(w[j]);
            wpd[j] = w[j];
            index[j] = j;
            xjneg_sum[j] = 0.0;
            for (Object x : prob_col.x[j]) {
                int ind = x.getIndex() - 1;
                double val = x.getValue();
                double[] arrd = exp_wTx;
                int n = ind;
                arrd[n] = arrd[n] + w[j] * val;
                if (y[ind] != -1) continue;
                double[] arrd2 = xjneg_sum;
                int n2 = j;
                arrd2[n2] = arrd2[n2] + C[Linear.GETI(y, ind)] * val;
            }
        }
        for (j = 0; j < l; ++j) {
            exp_wTx[j] = Math.exp(exp_wTx[j]);
            double tau_tmp = 1.0 / (1.0 + exp_wTx[j]);
            tau[j] = C[Linear.GETI(y, j)] * tau_tmp;
            D[j] = C[Linear.GETI(y, j)] * exp_wTx[j] * tau_tmp * tau_tmp;
        }
        while (newton_iter < max_newton_iter) {
            int s;
            int i;
            int i2;
            int num_linesearch;
            double Gmax_new = 0.0;
            double Gnorm1_new = 0.0;
            int active_size = w_size;
            for (s = 0; s < active_size; ++s) {
                j = index[s];
                Hdiag[j] = nu;
                Grad[j] = 0.0;
                double tmp = 0.0;
                for (Feature x : prob_col.x[j]) {
                    int ind = x.getIndex() - 1;
                    double[] arrd = Hdiag;
                    int n = j;
                    arrd[n] = arrd[n] + x.getValue() * x.getValue() * D[ind];
                    tmp += x.getValue() * tau[ind];
                }
                Grad[j] = -tmp + xjneg_sum[j];
                double Gp = Grad[j] + 1.0;
                double Gn = Grad[j] - 1.0;
                double violation = 0.0;
                if (w[j] == 0.0) {
                    if (Gp < 0.0) {
                        violation = -Gp;
                    } else if (Gn > 0.0) {
                        violation = Gn;
                    } else if (Gp > Gmax_old / (double)l && Gn < -Gmax_old / (double)l) {
                        Linear.swap(index, s, --active_size);
                        --s;
                        continue;
                    }
                } else {
                    violation = w[j] > 0.0 ? Math.abs(Gp) : Math.abs(Gn);
                }
                Gmax_new = Math.max(Gmax_new, violation);
                Gnorm1_new += violation;
            }
            if (newton_iter == 0) {
                Gnorm1_init = Gnorm1_new;
            }
            if (Gnorm1_new <= eps * Gnorm1_init) break;
            iter = 0;
            QP_Gmax_old = Double.POSITIVE_INFINITY;
            int QP_active_size = active_size;
            for (int i3 = 0; i3 < l; ++i3) {
                xTd[i3] = 0.0;
            }
            while (iter < max_iter) {
                double QP_Gmax_new = 0.0;
                double QP_Gnorm1_new = 0.0;
                for (j = 0; j < QP_active_size; ++j) {
                    i = random.nextInt(QP_active_size - j);
                    Linear.swap(index, i, j);
                }
                for (s = 0; s < QP_active_size; ++s) {
                    j = index[s];
                    double H = Hdiag[j];
                    double G = Grad[j] + (wpd[j] - w[j]) * nu;
                    for (Feature x : prob_col.x[j]) {
                        int ind = x.getIndex() - 1;
                        G += x.getValue() * D[ind] * xTd[ind];
                    }
                    double Gp = G + 1.0;
                    double Gn = G - 1.0;
                    double violation = 0.0;
                    if (wpd[j] == 0.0) {
                        if (Gp < 0.0) {
                            violation = -Gp;
                        } else if (Gn > 0.0) {
                            violation = Gn;
                        } else if (Gp > QP_Gmax_old / (double)l && Gn < -QP_Gmax_old / (double)l) {
                            Linear.swap(index, s, --QP_active_size);
                            --s;
                            continue;
                        }
                    } else {
                        violation = wpd[j] > 0.0 ? Math.abs(Gp) : Math.abs(Gn);
                    }
                    QP_Gmax_new = Math.max(QP_Gmax_new, violation);
                    QP_Gnorm1_new += violation;
                    double z = Gp < H * wpd[j] ? -Gp / H : (Gn > H * wpd[j] ? -Gn / H : -wpd[j]);
                    if (Math.abs(z) < 1.0E-12) continue;
                    z = Math.min(Math.max(z, -10.0), 10.0);
                    double[] arrd = wpd;
                    int n = j;
                    arrd[n] = arrd[n] + z;
                    for (Feature x : prob_col.x[j]) {
                        int ind = x.getIndex() - 1;
                        double[] arrd3 = xTd;
                        int n3 = ind;
                        arrd3[n3] = arrd3[n3] + x.getValue() * z;
                    }
                }
                ++iter;
                if (QP_Gnorm1_new <= inner_eps * Gnorm1_init) {
                    if (QP_active_size == active_size) break;
                    QP_active_size = active_size;
                    QP_Gmax_old = Double.POSITIVE_INFINITY;
                    continue;
                }
                QP_Gmax_old = QP_Gmax_new;
            }
            if (iter >= max_iter) {
                Linear.info("WARNING: reaching max number of inner iterations%n");
            }
            double delta = 0.0;
            double w_norm_new = 0.0;
            for (j = 0; j < w_size; ++j) {
                delta += Grad[j] * (wpd[j] - w[j]);
                if (wpd[j] == 0.0) continue;
                w_norm_new += Math.abs(wpd[j]);
            }
            delta += w_norm_new - w_norm;
            double negsum_xTd = 0.0;
            for (i = 0; i < l; ++i) {
                if (y[i] != -1) continue;
                negsum_xTd += C[Linear.GETI(y, i)] * xTd[i];
            }
            for (num_linesearch = 0; num_linesearch < max_num_linesearch; ++num_linesearch) {
                double cond = w_norm_new - w_norm + negsum_xTd - sigma * delta;
                for (i2 = 0; i2 < l; ++i2) {
                    double exp_xTd = Math.exp(xTd[i2]);
                    exp_wTx_new[i2] = exp_wTx[i2] * exp_xTd;
                    cond += C[Linear.GETI(y, i2)] * Math.log((1.0 + exp_wTx_new[i2]) / (exp_xTd + exp_wTx_new[i2]));
                }
                if (cond <= 0.0) {
                    w_norm = w_norm_new;
                    for (j = 0; j < w_size; ++j) {
                        w[j] = wpd[j];
                    }
                    for (i2 = 0; i2 < l; ++i2) {
                        exp_wTx[i2] = exp_wTx_new[i2];
                        double tau_tmp = 1.0 / (1.0 + exp_wTx[i2]);
                        tau[i2] = C[Linear.GETI(y, i2)] * tau_tmp;
                        D[i2] = C[Linear.GETI(y, i2)] * exp_wTx[i2] * tau_tmp * tau_tmp;
                    }
                    break;
                }
                w_norm_new = 0.0;
                for (j = 0; j < w_size; ++j) {
                    wpd[j] = (w[j] + wpd[j]) * 0.5;
                    if (wpd[j] == 0.0) continue;
                    w_norm_new += Math.abs(wpd[j]);
                }
                delta *= 0.5;
                negsum_xTd *= 0.5;
                i2 = 0;
                while (i2 < l) {
                    double[] arrd = xTd;
                    int n = i2++;
                    arrd[n] = arrd[n] * 0.5;
                }
            }
            if (num_linesearch >= max_num_linesearch) {
                for (i2 = 0; i2 < l; ++i2) {
                    exp_wTx[i2] = 0.0;
                }
                for (i2 = 0; i2 < w_size; ++i2) {
                    if (w[i2] == 0.0) continue;
                    for (Feature x : prob_col.x[i2]) {
                        double[] arrd = exp_wTx;
                        int n = x.getIndex() - 1;
                        arrd[n] = arrd[n] + w[i2] * x.getValue();
                    }
                }
                for (i2 = 0; i2 < l; ++i2) {
                    exp_wTx[i2] = Math.exp(exp_wTx[i2]);
                }
            }
            if (iter == 1) {
                inner_eps *= 0.25;
            }
            Gmax_old = Gmax_new;
            Linear.info("iter %3d  #CD cycles %d%n", ++newton_iter, iter);
        }
        Linear.info("=========================%n");
        Linear.info("optimization finished, #iter = %d%n", newton_iter);
        if (newton_iter >= max_newton_iter) {
            Linear.info("WARNING: reaching max number of iterations%n");
        }
        double v = 0.0;
        int nnz = 0;
        for (j = 0; j < w_size; ++j) {
            if (w[j] == 0.0) continue;
            v += Math.abs(w[j]);
            ++nnz;
        }
        for (j = 0; j < l; ++j) {
            if (y[j] == 1) {
                v += C[Linear.GETI(y, j)] * Math.log(1.0 + 1.0 / exp_wTx[j]);
                continue;
            }
            v += C[Linear.GETI(y, j)] * Math.log(1.0 + exp_wTx[j]);
        }
        Linear.info("Objective value = %g%n", v);
        Linear.info("#nonzeros/#features = %d/%d%n", nnz, w_size);
    }

    static Problem transpose(Problem prob) {
        int i;
        int l = prob.l;
        int n = prob.n;
        int[] col_ptr = new int[n + 1];
        Problem prob_col = new Problem();
        prob_col.l = l;
        prob_col.n = n;
        prob_col.y = new double[l];
        prob_col.x = new Feature[n][];
        for (i = 0; i < l; ++i) {
            prob_col.y[i] = prob.y[i];
        }
        for (i = 0; i < l; ++i) {
            for (Feature x : prob.x[i]) {
                int[] arrn = col_ptr;
                int n2 = x.getIndex();
                arrn[n2] = arrn[n2] + 1;
            }
        }
        for (i = 0; i < n; ++i) {
            prob_col.x[i] = new Feature[col_ptr[i + 1]];
            col_ptr[i] = 0;
        }
        for (i = 0; i < l; ++i) {
            for (int j = 0; j < prob.x[i].length; ++j) {
                Feature x = prob.x[i][j];
                int index = x.getIndex() - 1;
                prob_col.x[index][col_ptr[index]] = new FeatureNode(i + 1, x.getValue());
                int[] arrn = col_ptr;
                int n3 = index;
                arrn[n3] = arrn[n3] + 1;
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
        }
        if (param == null) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        if (prob.n == 0) {
            throw new IllegalArgumentException("problem has zero features");
        }
        if (prob.l == 0) {
            throw new IllegalArgumentException("problem has zero instances");
        }
        for (Feature[] nodes : prob.x) {
            int indexBefore = 0;
            for (Feature n : nodes) {
                if (n.getIndex() <= indexBefore) {
                    throw new IllegalArgumentException("feature nodes must be sorted by index in ascending order");
                }
                indexBefore = n.getIndex();
            }
        }
        int l = prob.l;
        int n = prob.n;
        int w_size = prob.n;
        Model model = new Model();
        model.nr_feature = prob.bias >= 0.0 ? n - 1 : n;
        model.solverType = param.solverType;
        model.bias = prob.bias;
        if (param.solverType.isSupportVectorRegression()) {
            model.w = new double[w_size];
            model.nr_class = 2;
            model.label = null;
            Linear.checkProblemSize(n, model.nr_class);
            Linear.train_one(prob, param, model.w, 0.0, 0.0);
        } else {
            int i;
            int[] perm = new int[l];
            GroupClassesReturn rv = Linear.groupClasses(prob, perm);
            int nr_class = rv.nr_class;
            int[] label = rv.label;
            int[] start = rv.start;
            int[] count = rv.count;
            Linear.checkProblemSize(n, nr_class);
            model.nr_class = nr_class;
            model.label = new int[nr_class];
            for (int i2 = 0; i2 < nr_class; ++i2) {
                model.label[i2] = label[i2];
            }
            double[] weighted_C = new double[nr_class];
            for (i = 0; i < nr_class; ++i) {
                weighted_C[i] = param.C;
            }
            for (i = 0; i < param.getNumWeights(); ++i) {
                int j;
                for (j = 0; j < nr_class && param.weightLabel[i] != label[j]; ++j) {
                }
                if (j == nr_class) {
                    throw new IllegalArgumentException("class label " + param.weightLabel[i] + " specified in weight is not found");
                }
                double[] arrd = weighted_C;
                int n2 = j;
                arrd[n2] = arrd[n2] * param.weight[i];
            }
            Feature[][] x = new Feature[l][];
            for (int i3 = 0; i3 < l; ++i3) {
                x[i3] = prob.x[perm[i3]];
            }
            Problem sub_prob = new Problem();
            sub_prob.l = l;
            sub_prob.n = n;
            sub_prob.x = new Feature[sub_prob.l][];
            sub_prob.y = new double[sub_prob.l];
            for (int k = 0; k < sub_prob.l; ++k) {
                sub_prob.x[k] = x[k];
            }
            if (param.solverType == SolverType.MCSVM_CS) {
                model.w = new double[n * nr_class];
                for (int i4 = 0; i4 < nr_class; ++i4) {
                    for (int j = start[i4]; j < start[i4] + count[i4]; ++j) {
                        sub_prob.y[j] = i4;
                    }
                }
                SolverMCSVM_CS solver = new SolverMCSVM_CS(sub_prob, nr_class, weighted_C, param.eps);
                solver.solve(model.w);
            } else if (nr_class == 2) {
                int k;
                model.w = new double[w_size];
                int e0 = start[0] + count[0];
                for (k = 0; k < e0; ++k) {
                    sub_prob.y[k] = 1.0;
                }
                while (k < sub_prob.l) {
                    sub_prob.y[k] = -1.0;
                    ++k;
                }
                Linear.train_one(sub_prob, param, model.w, weighted_C[0], weighted_C[1]);
            } else {
                model.w = new double[w_size * nr_class];
                double[] w = new double[w_size];
                for (int i5 = 0; i5 < nr_class; ++i5) {
                    int k;
                    int si = start[i5];
                    int ei = si + count[i5];
                    for (k = 0; k < si; ++k) {
                        sub_prob.y[k] = -1.0;
                    }
                    while (k < ei) {
                        sub_prob.y[k] = 1.0;
                        ++k;
                    }
                    while (k < sub_prob.l) {
                        sub_prob.y[k] = -1.0;
                        ++k;
                    }
                    Linear.train_one(sub_prob, param, w, weighted_C[i5], param.C);
                    for (int j = 0; j < n; ++j) {
                        model.w[j * nr_class + i5] = w[j];
                    }
                }
            }
        }
        return model;
    }

    private static void checkProblemSize(int n, int nr_class) {
        if (n >= Integer.MAX_VALUE / nr_class || n * nr_class < 0) {
            throw new IllegalArgumentException("'number of classes' * 'number of instances' is too large: " + nr_class + "*" + n);
        }
    }

    private static void train_one(Problem prob, Parameter param, double[] w, double Cp, double Cn) {
        double eps = param.eps;
        int pos = 0;
        for (int i = 0; i < prob.l; ++i) {
            if (!(prob.y[i] > 0.0)) continue;
            ++pos;
        }
        int neg = prob.l - pos;
        double primal_solver_tol = eps * (double)Math.max(Math.min(pos, neg), 1) / (double)prob.l;
        Function fun_obj = null;
        switch (param.solverType) {
            case L2R_LR: {
                double[] C = new double[prob.l];
                for (int i = 0; i < prob.l; ++i) {
                    C[i] = prob.y[i] > 0.0 ? Cp : Cn;
                }
                fun_obj = new L2R_LrFunction(prob, C);
                Tron tron_obj = new Tron(fun_obj, primal_solver_tol);
                tron_obj.tron(w);
                break;
            }
            case L2R_L2LOSS_SVC: {
                double[] C = new double[prob.l];
                for (int i = 0; i < prob.l; ++i) {
                    C[i] = prob.y[i] > 0.0 ? Cp : Cn;
                }
                fun_obj = new L2R_L2_SvcFunction(prob, C);
                Tron tron_obj = new Tron(fun_obj, primal_solver_tol);
                tron_obj.tron(w);
                break;
            }
            case L2R_L2LOSS_SVC_DUAL: {
                Linear.solve_l2r_l1l2_svc(prob, w, eps, Cp, Cn, SolverType.L2R_L2LOSS_SVC_DUAL);
                break;
            }
            case L2R_L1LOSS_SVC_DUAL: {
                Linear.solve_l2r_l1l2_svc(prob, w, eps, Cp, Cn, SolverType.L2R_L1LOSS_SVC_DUAL);
                break;
            }
            case L1R_L2LOSS_SVC: {
                Problem prob_col = Linear.transpose(prob);
                Linear.solve_l1r_l2_svc(prob_col, w, primal_solver_tol, Cp, Cn);
                break;
            }
            case L1R_LR: {
                Problem prob_col = Linear.transpose(prob);
                Linear.solve_l1r_lr(prob_col, w, primal_solver_tol, Cp, Cn);
                break;
            }
            case L2R_LR_DUAL: {
                Linear.solve_l2r_lr_dual(prob, w, eps, Cp, Cn);
                break;
            }
            case L2R_L2LOSS_SVR: {
                double[] C = new double[prob.l];
                for (int i = 0; i < prob.l; ++i) {
                    C[i] = param.C;
                }
                fun_obj = new L2R_L2_SvrFunction(prob, C, param.p);
                Tron tron_obj = new Tron(fun_obj, param.eps);
                tron_obj.tron(w);
                break;
            }
            case L2R_L1LOSS_SVR_DUAL: 
            case L2R_L2LOSS_SVR_DUAL: {
                Linear.solve_l2r_l1l2_svr(prob, w, param);
                break;
            }
            default: {
                throw new IllegalStateException("unknown solver type: " + (Object)((Object)param.solverType));
            }
        }
    }

    public static void disableDebugOutput() {
        Linear.setDebugOutput(null);
    }

    public static void enableDebugOutput() {
        Linear.setDebugOutput(System.out);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setDebugOutput(PrintStream debugOutput) {
        Object object = OUTPUT_MUTEX;
        synchronized (object) {
            DEBUG_OUTPUT = debugOutput;
        }
    }

    public static void resetRandom() {
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

