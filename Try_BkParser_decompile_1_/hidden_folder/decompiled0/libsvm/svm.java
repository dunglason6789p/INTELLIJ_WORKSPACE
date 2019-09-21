/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Random;
import java.util.StringTokenizer;
import libsvm.Kernel;
import libsvm.ONE_CLASS_Q;
import libsvm.QMatrix;
import libsvm.SVC_Q;
import libsvm.SVR_Q;
import libsvm.Solver;
import libsvm.Solver_NU;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

public class svm {
    public static final int LIBSVM_VERSION = 310;
    public static final Random rand = new Random();
    private static svm_print_interface svm_print_stdout;
    private static svm_print_interface svm_print_string;
    static final String[] svm_type_table;
    static final String[] kernel_type_table;

    static void info(String s) {
        svm_print_string.print(s);
    }

    private static void solve_c_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si, double Cp, double Cn) {
        int i;
        int l = prob.l;
        double[] minus_ones = new double[l];
        byte[] y = new byte[l];
        for (i = 0; i < l; ++i) {
            alpha[i] = 0.0;
            minus_ones[i] = -1.0;
            y[i] = prob.y[i] > 0.0 ? 1 : -1;
        }
        Solver s = new Solver();
        s.Solve(l, new SVC_Q(prob, param, y), minus_ones, y, alpha, Cp, Cn, param.eps, si, param.shrinking);
        double sum_alpha = 0.0;
        for (i = 0; i < l; ++i) {
            sum_alpha += alpha[i];
        }
        if (Cp == Cn) {
            svm.info("nu = " + sum_alpha / (Cp * (double)prob.l) + "\n");
        }
        for (i = 0; i < l; ++i) {
            double[] arrd = alpha;
            int n = i;
            arrd[n] = arrd[n] * (double)y[i];
        }
    }

    private static void solve_nu_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int i;
        int l = prob.l;
        double nu = param.nu;
        byte[] y = new byte[l];
        for (i = 0; i < l; ++i) {
            y[i] = prob.y[i] > 0.0 ? 1 : -1;
        }
        double sum_pos = nu * (double)l / 2.0;
        double sum_neg = nu * (double)l / 2.0;
        for (i = 0; i < l; ++i) {
            if (y[i] == 1) {
                alpha[i] = Math.min(1.0, sum_pos);
                sum_pos -= alpha[i];
                continue;
            }
            alpha[i] = Math.min(1.0, sum_neg);
            sum_neg -= alpha[i];
        }
        double[] zeros = new double[l];
        for (i = 0; i < l; ++i) {
            zeros[i] = 0.0;
        }
        Solver_NU s = new Solver_NU();
        s.Solve(l, new SVC_Q(prob, param, y), zeros, y, alpha, 1.0, 1.0, param.eps, si, param.shrinking);
        double r = si.r;
        svm.info("C = " + 1.0 / r + "\n");
        for (i = 0; i < l; ++i) {
            double[] arrd = alpha;
            int n = i;
            arrd[n] = arrd[n] * ((double)y[i] / r);
        }
        si.rho /= r;
        si.obj /= r * r;
        si.upper_bound_p = 1.0 / r;
        si.upper_bound_n = 1.0 / r;
    }

    private static void solve_one_class(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int i;
        int l = prob.l;
        double[] zeros = new double[l];
        byte[] ones = new byte[l];
        int n = (int)(param.nu * (double)prob.l);
        for (i = 0; i < n; ++i) {
            alpha[i] = 1.0;
        }
        if (n < prob.l) {
            alpha[n] = param.nu * (double)prob.l - (double)n;
        }
        for (i = n + 1; i < l; ++i) {
            alpha[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            zeros[i] = 0.0;
            ones[i] = 1;
        }
        Solver s = new Solver();
        s.Solve(l, new ONE_CLASS_Q(prob, param), zeros, ones, alpha, 1.0, 1.0, param.eps, si, param.shrinking);
    }

    private static void solve_epsilon_svr(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int i;
        int l = prob.l;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        for (i = 0; i < l; ++i) {
            alpha2[i] = 0.0;
            linear_term[i] = param.p - prob.y[i];
            y[i] = 1;
            alpha2[i + l] = 0.0;
            linear_term[i + l] = param.p + prob.y[i];
            y[i + l] = -1;
        }
        Solver s = new Solver();
        s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y, alpha2, param.C, param.C, param.eps, si, param.shrinking);
        double sum_alpha = 0.0;
        for (i = 0; i < l; ++i) {
            alpha[i] = alpha2[i] - alpha2[i + l];
            sum_alpha += Math.abs(alpha[i]);
        }
        svm.info("nu = " + sum_alpha / (param.C * (double)l) + "\n");
    }

    private static void solve_nu_svr(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int i;
        int l = prob.l;
        double C = param.C;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        double sum = C * param.nu * (double)l / 2.0;
        for (i = 0; i < l; ++i) {
            double d = Math.min(sum, C);
            alpha2[i + l] = d;
            alpha2[i] = d;
            sum -= alpha2[i];
            linear_term[i] = -prob.y[i];
            y[i] = 1;
            linear_term[i + l] = prob.y[i];
            y[i + l] = -1;
        }
        Solver_NU s = new Solver_NU();
        s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y, alpha2, C, C, param.eps, si, param.shrinking);
        svm.info("epsilon = " + -si.r + "\n");
        for (i = 0; i < l; ++i) {
            alpha[i] = alpha2[i] - alpha2[i + l];
        }
    }

    static decision_function svm_train_one(svm_problem prob, svm_parameter param, double Cp, double Cn) {
        double[] alpha = new double[prob.l];
        Solver.SolutionInfo si = new Solver.SolutionInfo();
        switch (param.svm_type) {
            case 0: {
                svm.solve_c_svc(prob, param, alpha, si, Cp, Cn);
                break;
            }
            case 1: {
                svm.solve_nu_svc(prob, param, alpha, si);
                break;
            }
            case 2: {
                svm.solve_one_class(prob, param, alpha, si);
                break;
            }
            case 3: {
                svm.solve_epsilon_svr(prob, param, alpha, si);
                break;
            }
            case 4: {
                svm.solve_nu_svr(prob, param, alpha, si);
            }
        }
        svm.info("obj = " + si.obj + ", rho = " + si.rho + "\n");
        int nSV = 0;
        int nBSV = 0;
        for (int i = 0; i < prob.l; ++i) {
            if (!(Math.abs(alpha[i]) > 0.0)) continue;
            ++nSV;
            if (prob.y[i] > 0.0) {
                if (!(Math.abs(alpha[i]) >= si.upper_bound_p)) continue;
                ++nBSV;
                continue;
            }
            if (!(Math.abs(alpha[i]) >= si.upper_bound_n)) continue;
            ++nBSV;
        }
        svm.info("nSV = " + nSV + ", nBSV = " + nBSV + "\n");
        decision_function f = new decision_function();
        f.alpha = alpha;
        f.rho = si.rho;
        return f;
    }

    private static void sigmoid_train(int l, double[] dec_values, double[] labels, double[] probAB) {
        int iter;
        double fApB;
        int i;
        double prior1 = 0.0;
        double prior0 = 0.0;
        for (i = 0; i < l; ++i) {
            if (labels[i] > 0.0) {
                prior1 += 1.0;
                continue;
            }
            prior0 += 1.0;
        }
        int max_iter = 100;
        double min_step = 1.0E-10;
        double sigma = 1.0E-12;
        double eps = 1.0E-5;
        double hiTarget = (prior1 + 1.0) / (prior1 + 2.0);
        double loTarget = 1.0 / (prior0 + 2.0);
        double[] t = new double[l];
        double A = 0.0;
        double B = Math.log((prior0 + 1.0) / (prior1 + 1.0));
        double fval = 0.0;
        for (i = 0; i < l; ++i) {
            t[i] = labels[i] > 0.0 ? hiTarget : loTarget;
            fApB = dec_values[i] * A + B;
            if (fApB >= 0.0) {
                fval += t[i] * fApB + Math.log(1.0 + Math.exp(-fApB));
                continue;
            }
            fval += (t[i] - 1.0) * fApB + Math.log(1.0 + Math.exp(fApB));
        }
        for (iter = 0; iter < max_iter; ++iter) {
            double stepsize;
            double h11 = sigma;
            double h22 = sigma;
            double h21 = 0.0;
            double g1 = 0.0;
            double g2 = 0.0;
            for (i = 0; i < l; ++i) {
                double q;
                double p;
                fApB = dec_values[i] * A + B;
                if (fApB >= 0.0) {
                    p = Math.exp(-fApB) / (1.0 + Math.exp(-fApB));
                    q = 1.0 / (1.0 + Math.exp(-fApB));
                } else {
                    p = 1.0 / (1.0 + Math.exp(fApB));
                    q = Math.exp(fApB) / (1.0 + Math.exp(fApB));
                }
                double d2 = p * q;
                h11 += dec_values[i] * dec_values[i] * d2;
                h22 += d2;
                h21 += dec_values[i] * d2;
                double d1 = t[i] - p;
                g1 += dec_values[i] * d1;
                g2 += d1;
            }
            if (Math.abs(g1) < eps && Math.abs(g2) < eps) break;
            double det = h11 * h22 - h21 * h21;
            double dA = -(h22 * g1 - h21 * g2) / det;
            double dB = -(-h21 * g1 + h11 * g2) / det;
            double gd = g1 * dA + g2 * dB;
            for (stepsize = 1.0; stepsize >= min_step; stepsize /= 2.0) {
                double newA = A + stepsize * dA;
                double newB = B + stepsize * dB;
                double newf = 0.0;
                for (i = 0; i < l; ++i) {
                    fApB = dec_values[i] * newA + newB;
                    if (fApB >= 0.0) {
                        newf += t[i] * fApB + Math.log(1.0 + Math.exp(-fApB));
                        continue;
                    }
                    newf += (t[i] - 1.0) * fApB + Math.log(1.0 + Math.exp(fApB));
                }
                if (!(newf < fval + 1.0E-4 * stepsize * gd)) continue;
                A = newA;
                B = newB;
                fval = newf;
                break;
            }
            if (!(stepsize < min_step)) continue;
            svm.info("Line search fails in two-class probability estimates\n");
            break;
        }
        if (iter >= max_iter) {
            svm.info("Reaching maximal iterations in two-class probability estimates\n");
        }
        probAB[0] = A;
        probAB[1] = B;
    }

    private static double sigmoid_predict(double decision_value, double A, double B) {
        double fApB = decision_value * A + B;
        if (fApB >= 0.0) {
            return Math.exp(-fApB) / (1.0 + Math.exp(-fApB));
        }
        return 1.0 / (1.0 + Math.exp(fApB));
    }

    private static void multiclass_probability(int k, double[][] r, double[] p) {
        int j;
        int t;
        int iter = 0;
        int max_iter = Math.max(100, k);
        double[][] Q = new double[k][k];
        double[] Qp = new double[k];
        double eps = 0.005 / (double)k;
        for (t = 0; t < k; ++t) {
            p[t] = 1.0 / (double)k;
            Q[t][t] = 0.0;
            for (j = 0; j < t; ++j) {
                double[] arrd = Q[t];
                int n = t;
                arrd[n] = arrd[n] + r[j][t] * r[j][t];
                Q[t][j] = Q[j][t];
            }
            for (j = t + 1; j < k; ++j) {
                double[] arrd = Q[t];
                int n = t;
                arrd[n] = arrd[n] + r[j][t] * r[j][t];
                Q[t][j] = -r[j][t] * r[t][j];
            }
        }
        for (iter = 0; iter < max_iter; ++iter) {
            double pQp = 0.0;
            for (t = 0; t < k; ++t) {
                Qp[t] = 0.0;
                for (j = 0; j < k; ++j) {
                    double[] arrd = Qp;
                    int n = t;
                    arrd[n] = arrd[n] + Q[t][j] * p[j];
                }
                pQp += p[t] * Qp[t];
            }
            double max_error = 0.0;
            for (t = 0; t < k; ++t) {
                double error = Math.abs(Qp[t] - pQp);
                if (!(error > max_error)) continue;
                max_error = error;
            }
            if (max_error < eps) break;
            for (t = 0; t < k; ++t) {
                double diff = (-Qp[t] + pQp) / Q[t][t];
                double[] arrd = p;
                int n = t;
                arrd[n] = arrd[n] + diff;
                pQp = (pQp + diff * (diff * Q[t][t] + 2.0 * Qp[t])) / (1.0 + diff) / (1.0 + diff);
                j = 0;
                while (j < k) {
                    Qp[j] = (Qp[j] + diff * Q[t][j]) / (1.0 + diff);
                    double[] arrd2 = p;
                    int n2 = j++;
                    arrd2[n2] = arrd2[n2] / (1.0 + diff);
                }
            }
        }
        if (iter >= max_iter) {
            svm.info("Exceeds max_iter in multiclass_prob\n");
        }
    }

    private static void svm_binary_svc_probability(svm_problem prob, svm_parameter param, double Cp, double Cn, double[] probAB) {
        int i;
        int nr_fold = 5;
        int[] perm = new int[prob.l];
        double[] dec_values = new double[prob.l];
        for (i = 0; i < prob.l; ++i) {
            perm[i] = i;
        }
        for (i = 0; i < prob.l; ++i) {
            int j = i + rand.nextInt(prob.l - i);
            int _ = perm[i];
            perm[i] = perm[j];
            perm[j] = _;
        }
        for (i = 0; i < nr_fold; ++i) {
            int j;
            int begin = i * prob.l / nr_fold;
            int end = (i + 1) * prob.l / nr_fold;
            svm_problem subprob = new svm_problem();
            subprob.l = prob.l - (end - begin);
            subprob.x = new svm_node[subprob.l][];
            subprob.y = new double[subprob.l];
            int k = 0;
            for (j = 0; j < begin; ++j) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            for (j = end; j < prob.l; ++j) {
                subprob.x[k] = prob.x[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            int p_count = 0;
            int n_count = 0;
            for (j = 0; j < k; ++j) {
                if (subprob.y[j] > 0.0) {
                    ++p_count;
                    continue;
                }
                ++n_count;
            }
            if (p_count == 0 && n_count == 0) {
                for (j = begin; j < end; ++j) {
                    dec_values[perm[j]] = 0.0;
                }
                continue;
            }
            if (p_count > 0 && n_count == 0) {
                for (j = begin; j < end; ++j) {
                    dec_values[perm[j]] = 1.0;
                }
                continue;
            }
            if (p_count == 0 && n_count > 0) {
                for (j = begin; j < end; ++j) {
                    dec_values[perm[j]] = -1.0;
                }
                continue;
            }
            svm_parameter subparam = (svm_parameter)param.clone();
            subparam.probability = 0;
            subparam.C = 1.0;
            subparam.nr_weight = 2;
            subparam.weight_label = new int[2];
            subparam.weight = new double[2];
            subparam.weight_label[0] = 1;
            subparam.weight_label[1] = -1;
            subparam.weight[0] = Cp;
            subparam.weight[1] = Cn;
            svm_model submodel = svm.svm_train(subprob, subparam);
            for (j = begin; j < end; ++j) {
                double[] dec_value = new double[1];
                svm.svm_predict_values(submodel, prob.x[perm[j]], dec_value);
                dec_values[perm[j]] = dec_value[0];
                double[] arrd = dec_values;
                int n = perm[j];
                arrd[n] = arrd[n] * (double)submodel.label[0];
            }
        }
        svm.sigmoid_train(prob.l, dec_values, prob.y, probAB);
    }

    private static double svm_svr_probability(svm_problem prob, svm_parameter param) {
        int i;
        int nr_fold = 5;
        double[] ymv = new double[prob.l];
        double mae = 0.0;
        svm_parameter newparam = (svm_parameter)param.clone();
        newparam.probability = 0;
        svm.svm_cross_validation(prob, newparam, nr_fold, ymv);
        for (i = 0; i < prob.l; ++i) {
            ymv[i] = prob.y[i] - ymv[i];
            mae += Math.abs(ymv[i]);
        }
        double std = Math.sqrt(2.0 * (mae /= (double)prob.l) * mae);
        int count = 0;
        mae = 0.0;
        for (i = 0; i < prob.l; ++i) {
            if (Math.abs(ymv[i]) > 5.0 * std) {
                ++count;
                continue;
            }
            mae += Math.abs(ymv[i]);
        }
        svm.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + (mae /= (double)(prob.l - count)) + "\n");
        return mae;
    }

    private static void svm_group_classes(svm_problem prob, int[] nr_class_ret, int[][] label_ret, int[][] start_ret, int[][] count_ret, int[] perm) {
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
                int[] new_data = new int[max_nr_class *= 2];
                System.arraycopy(label, 0, new_data, 0, label.length);
                label = new_data;
                new_data = new int[max_nr_class];
                System.arraycopy(count, 0, new_data, 0, count.length);
                count = new_data;
            }
            label[nr_class] = this_label;
            count[nr_class] = 1;
            ++nr_class;
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
        nr_class_ret[0] = nr_class;
        label_ret[0] = label;
        start_ret[0] = start;
        count_ret[0] = count;
    }

    public static svm_model svm_train(svm_problem prob, svm_parameter param) {
        svm_model model = new svm_model();
        model.param = param;
        if (param.svm_type == 2 || param.svm_type == 3 || param.svm_type == 4) {
            int i;
            model.nr_class = 2;
            model.label = null;
            model.nSV = null;
            model.probA = null;
            model.probB = null;
            model.sv_coef = new double[1][];
            if (param.probability == 1 && (param.svm_type == 3 || param.svm_type == 4)) {
                model.probA = new double[1];
                model.probA[0] = svm.svm_svr_probability(prob, param);
            }
            decision_function f = svm.svm_train_one(prob, param, 0.0, 0.0);
            model.rho = new double[1];
            model.rho[0] = f.rho;
            int nSV = 0;
            for (i = 0; i < prob.l; ++i) {
                if (!(Math.abs(f.alpha[i]) > 0.0)) continue;
                ++nSV;
            }
            model.l = nSV;
            model.SV = new svm_node[nSV][];
            model.sv_coef[0] = new double[nSV];
            int j = 0;
            for (i = 0; i < prob.l; ++i) {
                if (!(Math.abs(f.alpha[i]) > 0.0)) continue;
                model.SV[j] = prob.x[i];
                model.sv_coef[0][j] = f.alpha[i];
                ++j;
            }
        } else {
            int i;
            int j;
            int l = prob.l;
            int[] tmp_nr_class = new int[1];
            int[][] tmp_label = new int[1][];
            int[][] tmp_start = new int[1][];
            int[][] tmp_count = new int[1][];
            int[] perm = new int[l];
            svm.svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);
            int nr_class = tmp_nr_class[0];
            int[] label = tmp_label[0];
            int[] start = tmp_start[0];
            int[] count = tmp_count[0];
            svm_node[][] x = new svm_node[l][];
            for (i = 0; i < l; ++i) {
                x[i] = prob.x[perm[i]];
            }
            double[] weighted_C = new double[nr_class];
            for (i = 0; i < nr_class; ++i) {
                weighted_C[i] = param.C;
            }
            for (i = 0; i < param.nr_weight; ++i) {
                int j2;
                for (j2 = 0; j2 < nr_class && param.weight_label[i] != label[j2]; ++j2) {
                }
                if (j2 == nr_class) {
                    System.err.print("warning: class label " + param.weight_label[i] + " specified in weight is not found\n");
                    continue;
                }
                double[] arrd = weighted_C;
                int n = j2;
                arrd[n] = arrd[n] * param.weight[i];
            }
            boolean[] nonzero = new boolean[l];
            for (i = 0; i < l; ++i) {
                nonzero[i] = false;
            }
            decision_function[] f = new decision_function[nr_class * (nr_class - 1) / 2];
            double[] probA = null;
            double[] probB = null;
            if (param.probability == 1) {
                probA = new double[nr_class * (nr_class - 1) / 2];
                probB = new double[nr_class * (nr_class - 1) / 2];
            }
            int p = 0;
            for (i = 0; i < nr_class; ++i) {
                for (int j3 = i + 1; j3 < nr_class; ++j3) {
                    int k;
                    svm_problem sub_prob = new svm_problem();
                    int si = start[i];
                    int sj = start[j3];
                    int ci = count[i];
                    int cj = count[j3];
                    sub_prob.l = ci + cj;
                    sub_prob.x = new svm_node[sub_prob.l][];
                    sub_prob.y = new double[sub_prob.l];
                    for (k = 0; k < ci; ++k) {
                        sub_prob.x[k] = x[si + k];
                        sub_prob.y[k] = 1.0;
                    }
                    for (k = 0; k < cj; ++k) {
                        sub_prob.x[ci + k] = x[sj + k];
                        sub_prob.y[ci + k] = -1.0;
                    }
                    if (param.probability == 1) {
                        double[] probAB = new double[2];
                        svm.svm_binary_svc_probability(sub_prob, param, weighted_C[i], weighted_C[j3], probAB);
                        probA[p] = probAB[0];
                        probB[p] = probAB[1];
                    }
                    f[p] = svm.svm_train_one(sub_prob, param, weighted_C[i], weighted_C[j3]);
                    for (k = 0; k < ci; ++k) {
                        if (nonzero[si + k] || !(Math.abs(f[p].alpha[k]) > 0.0)) continue;
                        nonzero[si + k] = true;
                    }
                    for (k = 0; k < cj; ++k) {
                        if (nonzero[sj + k] || !(Math.abs(f[p].alpha[ci + k]) > 0.0)) continue;
                        nonzero[sj + k] = true;
                    }
                    ++p;
                }
            }
            model.nr_class = nr_class;
            model.label = new int[nr_class];
            for (i = 0; i < nr_class; ++i) {
                model.label[i] = label[i];
            }
            model.rho = new double[nr_class * (nr_class - 1) / 2];
            for (i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
                model.rho[i] = f[i].rho;
            }
            if (param.probability == 1) {
                model.probA = new double[nr_class * (nr_class - 1) / 2];
                model.probB = new double[nr_class * (nr_class - 1) / 2];
                for (i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
                    model.probA[i] = probA[i];
                    model.probB[i] = probB[i];
                }
            } else {
                model.probA = null;
                model.probB = null;
            }
            int nnz = 0;
            int[] nz_count = new int[nr_class];
            model.nSV = new int[nr_class];
            for (i = 0; i < nr_class; ++i) {
                int nSV = 0;
                for (j = 0; j < count[i]; ++j) {
                    if (!nonzero[start[i] + j]) continue;
                    ++nSV;
                    ++nnz;
                }
                model.nSV[i] = nSV;
                nz_count[i] = nSV;
            }
            svm.info("Total nSV = " + nnz + "\n");
            model.l = nnz;
            model.SV = new svm_node[nnz][];
            p = 0;
            for (i = 0; i < l; ++i) {
                if (!nonzero[i]) continue;
                model.SV[p++] = x[i];
            }
            int[] nz_start = new int[nr_class];
            nz_start[0] = 0;
            for (i = 1; i < nr_class; ++i) {
                nz_start[i] = nz_start[i - 1] + nz_count[i - 1];
            }
            model.sv_coef = new double[nr_class - 1][];
            for (i = 0; i < nr_class - 1; ++i) {
                model.sv_coef[i] = new double[nnz];
            }
            p = 0;
            for (i = 0; i < nr_class; ++i) {
                for (j = i + 1; j < nr_class; ++j) {
                    int k;
                    int si = start[i];
                    int sj = start[j];
                    int ci = count[i];
                    int cj = count[j];
                    int q = nz_start[i];
                    for (k = 0; k < ci; ++k) {
                        if (!nonzero[si + k]) continue;
                        model.sv_coef[j - 1][q++] = f[p].alpha[k];
                    }
                    q = nz_start[j];
                    for (k = 0; k < cj; ++k) {
                        if (!nonzero[sj + k]) continue;
                        model.sv_coef[i][q++] = f[p].alpha[ci + k];
                    }
                    ++p;
                }
            }
        }
        return model;
    }

    public static void svm_cross_validation(svm_problem prob, svm_parameter param, int nr_fold, double[] target) {
        int i;
        int[] fold_start = new int[nr_fold + 1];
        int l = prob.l;
        int[] perm = new int[l];
        if ((param.svm_type == 0 || param.svm_type == 1) && nr_fold < l) {
            int c;
            int[] tmp_nr_class = new int[1];
            int[][] tmp_label = new int[1][];
            int[][] tmp_start = new int[1][];
            int[][] tmp_count = new int[1][];
            svm.svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);
            int nr_class = tmp_nr_class[0];
            int[] start = tmp_start[0];
            int[] count = tmp_count[0];
            int[] fold_count = new int[nr_fold];
            int[] index = new int[l];
            for (i = 0; i < l; ++i) {
                index[i] = perm[i];
            }
            for (c = 0; c < nr_class; ++c) {
                for (i = 0; i < count[c]; ++i) {
                    int j = i + rand.nextInt(count[c] - i);
                    int _ = index[start[c] + j];
                    index[start[c] + j] = index[start[c] + i];
                    index[start[c] + i] = _;
                }
            }
            for (i = 0; i < nr_fold; ++i) {
                fold_count[i] = 0;
                for (c = 0; c < nr_class; ++c) {
                    int[] arrn = fold_count;
                    int n = i;
                    arrn[n] = arrn[n] + ((i + 1) * count[c] / nr_fold - i * count[c] / nr_fold);
                }
            }
            fold_start[0] = 0;
            for (i = 1; i <= nr_fold; ++i) {
                fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
            }
            for (c = 0; c < nr_class; ++c) {
                for (i = 0; i < nr_fold; ++i) {
                    int begin = start[c] + i * count[c] / nr_fold;
                    int end = start[c] + (i + 1) * count[c] / nr_fold;
                    for (int j = begin; j < end; ++j) {
                        perm[fold_start[i]] = index[j];
                        int[] arrn = fold_start;
                        int n = i;
                        arrn[n] = arrn[n] + 1;
                    }
                }
            }
            fold_start[0] = 0;
            for (i = 1; i <= nr_fold; ++i) {
                fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
            }
        } else {
            for (i = 0; i < l; ++i) {
                perm[i] = i;
            }
            for (i = 0; i < l; ++i) {
                int j = i + rand.nextInt(l - i);
                int _ = perm[i];
                perm[i] = perm[j];
                perm[j] = _;
            }
            for (i = 0; i <= nr_fold; ++i) {
                fold_start[i] = i * l / nr_fold;
            }
        }
        for (i = 0; i < nr_fold; ++i) {
            int j;
            int begin = fold_start[i];
            int end = fold_start[i + 1];
            svm_problem subprob = new svm_problem();
            subprob.l = l - (end - begin);
            subprob.x = new svm_node[subprob.l][];
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
            svm_model submodel = svm.svm_train(subprob, param);
            if (param.probability == 1 && (param.svm_type == 0 || param.svm_type == 1)) {
                double[] prob_estimates = new double[svm.svm_get_nr_class(submodel)];
                for (j = begin; j < end; ++j) {
                    target[perm[j]] = svm.svm_predict_probability(submodel, prob.x[perm[j]], prob_estimates);
                }
                continue;
            }
            for (j = begin; j < end; ++j) {
                target[perm[j]] = svm.svm_predict(submodel, prob.x[perm[j]]);
            }
        }
    }

    public static int svm_get_svm_type(svm_model model) {
        return model.param.svm_type;
    }

    public static int svm_get_nr_class(svm_model model) {
        return model.nr_class;
    }

    public static void svm_get_labels(svm_model model, int[] label) {
        if (model.label != null) {
            for (int i = 0; i < model.nr_class; ++i) {
                label[i] = model.label[i];
            }
        }
    }

    public static double svm_get_svr_probability(svm_model model) {
        if ((model.param.svm_type == 3 || model.param.svm_type == 4) && model.probA != null) {
            return model.probA[0];
        }
        System.err.print("Model doesn't contain information for SVR probability inference\n");
        return 0.0;
    }

    public static double svm_predict_values(svm_model model, svm_node[] x, double[] dec_values) {
        int i;
        if (model.param.svm_type == 2 || model.param.svm_type == 3 || model.param.svm_type == 4) {
            double[] sv_coef = model.sv_coef[0];
            double sum = 0.0;
            for (int i2 = 0; i2 < model.l; ++i2) {
                sum += sv_coef[i2] * Kernel.k_function(x, model.SV[i2], model.param);
            }
            dec_values[0] = sum -= model.rho[0];
            if (model.param.svm_type == 2) {
                return sum > 0.0 ? 1.0 : -1.0;
            }
            return sum;
        }
        int nr_class = model.nr_class;
        int l = model.l;
        double[] kvalue = new double[l];
        for (i = 0; i < l; ++i) {
            kvalue[i] = Kernel.k_function(x, model.SV[i], model.param);
        }
        int[] start = new int[nr_class];
        start[0] = 0;
        for (i = 1; i < nr_class; ++i) {
            start[i] = start[i - 1] + model.nSV[i - 1];
        }
        int[] vote = new int[nr_class];
        for (i = 0; i < nr_class; ++i) {
            vote[i] = 0;
        }
        int p = 0;
        for (i = 0; i < nr_class; ++i) {
            for (int j = i + 1; j < nr_class; ++j) {
                int k;
                double sum = 0.0;
                int si = start[i];
                int sj = start[j];
                int ci = model.nSV[i];
                int cj = model.nSV[j];
                double[] coef1 = model.sv_coef[j - 1];
                double[] coef2 = model.sv_coef[i];
                for (k = 0; k < ci; ++k) {
                    sum += coef1[si + k] * kvalue[si + k];
                }
                for (k = 0; k < cj; ++k) {
                    sum += coef2[sj + k] * kvalue[sj + k];
                }
                dec_values[p] = sum -= model.rho[p];
                if (dec_values[p] > 0.0) {
                    int[] arrn = vote;
                    int n = i;
                    arrn[n] = arrn[n] + 1;
                } else {
                    int[] arrn = vote;
                    int n = j;
                    arrn[n] = arrn[n] + 1;
                }
                ++p;
            }
        }
        int vote_max_idx = 0;
        for (i = 1; i < nr_class; ++i) {
            if (vote[i] <= vote[vote_max_idx]) continue;
            vote_max_idx = i;
        }
        return model.label[vote_max_idx];
    }

    public static double svm_predict(svm_model model, svm_node[] x) {
        int nr_class = model.nr_class;
        double[] dec_values = model.param.svm_type == 2 || model.param.svm_type == 3 || model.param.svm_type == 4 ? new double[1] : new double[nr_class * (nr_class - 1) / 2];
        double pred_result = svm.svm_predict_values(model, x, dec_values);
        return pred_result;
    }

    public static double svm_predict_probability(svm_model model, svm_node[] x, double[] prob_estimates) {
        if ((model.param.svm_type == 0 || model.param.svm_type == 1) && model.probA != null && model.probB != null) {
            int i;
            int nr_class = model.nr_class;
            double[] dec_values = new double[nr_class * (nr_class - 1) / 2];
            svm.svm_predict_values(model, x, dec_values);
            double min_prob = 1.0E-7;
            double[][] pairwise_prob = new double[nr_class][nr_class];
            int k = 0;
            for (i = 0; i < nr_class; ++i) {
                for (int j = i + 1; j < nr_class; ++j) {
                    pairwise_prob[i][j] = Math.min(Math.max(svm.sigmoid_predict(dec_values[k], model.probA[k], model.probB[k]), min_prob), 1.0 - min_prob);
                    pairwise_prob[j][i] = 1.0 - pairwise_prob[i][j];
                    ++k;
                }
            }
            svm.multiclass_probability(nr_class, pairwise_prob, prob_estimates);
            int prob_max_idx = 0;
            for (i = 1; i < nr_class; ++i) {
                if (!(prob_estimates[i] > prob_estimates[prob_max_idx])) continue;
                prob_max_idx = i;
            }
            return model.label[prob_max_idx];
        }
        return svm.svm_predict(model, x);
    }

    public static void svm_save_model(String model_file_name, svm_model model) throws IOException {
        int i;
        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(model_file_name)));
        svm_parameter param = model.param;
        fp.writeBytes("svm_type " + svm_type_table[param.svm_type] + "\n");
        fp.writeBytes("kernel_type " + kernel_type_table[param.kernel_type] + "\n");
        if (param.kernel_type == 1) {
            fp.writeBytes("degree " + param.degree + "\n");
        }
        if (param.kernel_type == 1 || param.kernel_type == 2 || param.kernel_type == 3) {
            fp.writeBytes("gamma " + param.gamma + "\n");
        }
        if (param.kernel_type == 1 || param.kernel_type == 3) {
            fp.writeBytes("coef0 " + param.coef0 + "\n");
        }
        int nr_class = model.nr_class;
        int l = model.l;
        fp.writeBytes("nr_class " + nr_class + "\n");
        fp.writeBytes("total_sv " + l + "\n");
        fp.writeBytes("rho");
        for (i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
            fp.writeBytes(" " + model.rho[i]);
        }
        fp.writeBytes("\n");
        if (model.label != null) {
            fp.writeBytes("label");
            for (i = 0; i < nr_class; ++i) {
                fp.writeBytes(" " + model.label[i]);
            }
            fp.writeBytes("\n");
        }
        if (model.probA != null) {
            fp.writeBytes("probA");
            for (i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
                fp.writeBytes(" " + model.probA[i]);
            }
            fp.writeBytes("\n");
        }
        if (model.probB != null) {
            fp.writeBytes("probB");
            for (i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
                fp.writeBytes(" " + model.probB[i]);
            }
            fp.writeBytes("\n");
        }
        if (model.nSV != null) {
            fp.writeBytes("nr_sv");
            for (i = 0; i < nr_class; ++i) {
                fp.writeBytes(" " + model.nSV[i]);
            }
            fp.writeBytes("\n");
        }
        fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;
        for (int i2 = 0; i2 < l; ++i2) {
            for (int j = 0; j < nr_class - 1; ++j) {
                fp.writeBytes(sv_coef[j][i2] + " ");
            }
            svm_node[] p = SV[i2];
            if (param.kernel_type == 4) {
                fp.writeBytes("0:" + (int)p[0].value);
            } else {
                for (int j = 0; j < p.length; ++j) {
                    fp.writeBytes(p[j].index + ":" + p[j].value + " ");
                }
            }
            fp.writeBytes("\n");
        }
        fp.close();
    }

    private static double atof(String s) {
        return Double.valueOf(s);
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    public static svm_model svm_load_model(String model_file_name) throws IOException {
        return svm.svm_load_model(new BufferedReader(new FileReader(model_file_name)));
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static svm_model svm_load_model(BufferedReader fp) throws IOException {
        block25 : {
            model = new svm_model();
            model.param = param = new svm_parameter();
            model.rho = null;
            model.probA = null;
            model.probB = null;
            model.label = null;
            model.nSV = null;
            block0 : do {
                block26 : {
                    cmd = fp.readLine();
                    arg = cmd.substring(cmd.indexOf(32) + 1);
                    if (!cmd.startsWith("svm_type")) break block26;
                    ** GOTO lbl85
                }
                if (cmd.startsWith("kernel_type")) {
                } else {
                    if (cmd.startsWith("degree")) {
                        param.degree = svm.atoi(arg);
                        continue;
                    }
                    if (cmd.startsWith("gamma")) {
                        param.gamma = svm.atof(arg);
                        continue;
                    }
                    if (cmd.startsWith("coef0")) {
                        param.coef0 = svm.atof(arg);
                        continue;
                    }
                    if (cmd.startsWith("nr_class")) {
                        model.nr_class = svm.atoi(arg);
                        continue;
                    }
                    if (cmd.startsWith("total_sv")) {
                        model.l = svm.atoi(arg);
                        continue;
                    }
                    if (cmd.startsWith("rho")) {
                        n = model.nr_class * (model.nr_class - 1) / 2;
                        model.rho = new double[n];
                        st = new StringTokenizer(arg);
                        i = 0;
                        do {
                            if (i >= n) continue block0;
                            model.rho[i] = svm.atof(st.nextToken());
                            ++i;
                        } while (true);
                    }
                    if (cmd.startsWith("label")) {
                        n = model.nr_class;
                        model.label = new int[n];
                        st = new StringTokenizer(arg);
                        i = 0;
                        do {
                            if (i >= n) continue block0;
                            model.label[i] = svm.atoi(st.nextToken());
                            ++i;
                        } while (true);
                    }
                    if (cmd.startsWith("probA")) {
                        n = model.nr_class * (model.nr_class - 1) / 2;
                        model.probA = new double[n];
                        st = new StringTokenizer(arg);
                        i = 0;
                        do {
                            if (i >= n) continue block0;
                            model.probA[i] = svm.atof(st.nextToken());
                            ++i;
                        } while (true);
                    }
                    if (cmd.startsWith("probB")) {
                        n = model.nr_class * (model.nr_class - 1) / 2;
                        model.probB = new double[n];
                        st = new StringTokenizer(arg);
                        i = 0;
                        do {
                            if (i >= n) continue block0;
                            model.probB[i] = svm.atof(st.nextToken());
                            ++i;
                        } while (true);
                    }
                    if (cmd.startsWith("nr_sv")) {
                        n = model.nr_class;
                        model.nSV = new int[n];
                        st = new StringTokenizer(arg);
                        i = 0;
                        do {
                            if (i >= n) continue block0;
                            model.nSV[i] = svm.atoi(st.nextToken());
                            ++i;
                        } while (true);
                    }
                    if (!cmd.startsWith("SV")) {
                        System.err.print("unknown text in model file: [" + cmd + "]\n");
                        return null;
                    }
                    break block25;
lbl85: // 2 sources:
                    for (i = 0; i < svm.svm_type_table.length; ++i) {
                        if (arg.indexOf(svm.svm_type_table[i]) == -1) continue;
                        param.svm_type = i;
                        break;
                    }
                    if (i != svm.svm_type_table.length) continue;
                    System.err.print("unknown svm type.\n");
                    return null;
                }
                for (i = 0; i < svm.kernel_type_table.length; ++i) {
                    if (arg.indexOf(svm.kernel_type_table[i]) == -1) continue;
                    param.kernel_type = i;
                    break;
                }
                if (i == svm.kernel_type_table.length) break;
            } while (true);
            System.err.print("unknown kernel function.\n");
            return null;
        }
        m = model.nr_class - 1;
        l = model.l;
        model.sv_coef = new double[m][l];
        model.SV = new svm_node[l][];
        i = 0;
        do {
            if (i >= l) {
                fp.close();
                return model;
            }
            line = fp.readLine();
            st = new StringTokenizer(line, " \t\n\r\f:");
            for (k = 0; k < m; ++k) {
                model.sv_coef[k][i] = svm.atof(st.nextToken());
            }
            n = st.countTokens() / 2;
            model.SV[i] = new svm_node[n];
            for (j = 0; j < n; ++j) {
                model.SV[i][j] = new svm_node();
                model.SV[i][j].index = svm.atoi(st.nextToken());
                model.SV[i][j].value = svm.atof(st.nextToken());
            }
            ++i;
        } while (true);
    }

    public static String svm_check_parameter(svm_problem prob, svm_parameter param) {
        int svm_type = param.svm_type;
        if (svm_type != 0 && svm_type != 1 && svm_type != 2 && svm_type != 3 && svm_type != 4) {
            return "unknown svm type";
        }
        int kernel_type = param.kernel_type;
        if (kernel_type != 0 && kernel_type != 1 && kernel_type != 2 && kernel_type != 3 && kernel_type != 4) {
            return "unknown kernel type";
        }
        if (param.gamma < 0.0) {
            return "gamma < 0";
        }
        if (param.degree < 0) {
            return "degree of polynomial kernel < 0";
        }
        if (param.cache_size <= 0.0) {
            return "cache_size <= 0";
        }
        if (param.eps <= 0.0) {
            return "eps <= 0";
        }
        if ((svm_type == 0 || svm_type == 3 || svm_type == 4) && param.C <= 0.0) {
            return "C <= 0";
        }
        if ((svm_type == 1 || svm_type == 2 || svm_type == 4) && (param.nu <= 0.0 || param.nu > 1.0)) {
            return "nu <= 0 or nu > 1";
        }
        if (svm_type == 3 && param.p < 0.0) {
            return "p < 0";
        }
        if (param.shrinking != 0 && param.shrinking != 1) {
            return "shrinking != 0 and shrinking != 1";
        }
        if (param.probability != 0 && param.probability != 1) {
            return "probability != 0 and probability != 1";
        }
        if (param.probability == 1 && svm_type == 2) {
            return "one-class SVM probability output not supported yet";
        }
        if (svm_type == 1) {
            int i;
            int j;
            int l = prob.l;
            int max_nr_class = 16;
            int nr_class = 0;
            int[] label = new int[max_nr_class];
            int[] count = new int[max_nr_class];
            for (i = 0; i < l; ++i) {
                int this_label = (int)prob.y[i];
                for (j = 0; j < nr_class; ++j) {
                    if (this_label != label[j]) continue;
                    int[] arrn = count;
                    int n = j;
                    arrn[n] = arrn[n] + 1;
                    break;
                }
                if (j != nr_class) continue;
                if (nr_class == max_nr_class) {
                    int[] new_data = new int[max_nr_class *= 2];
                    System.arraycopy(label, 0, new_data, 0, label.length);
                    label = new_data;
                    new_data = new int[max_nr_class];
                    System.arraycopy(count, 0, new_data, 0, count.length);
                    count = new_data;
                }
                label[nr_class] = this_label;
                count[nr_class] = 1;
                ++nr_class;
            }
            for (i = 0; i < nr_class; ++i) {
                int n1 = count[i];
                for (j = i + 1; j < nr_class; ++j) {
                    int n2 = count[j];
                    if (!(param.nu * (double)(n1 + n2) / 2.0 > (double)Math.min(n1, n2))) continue;
                    return "specified nu is infeasible";
                }
            }
        }
        return null;
    }

    public static int svm_check_probability_model(svm_model model) {
        return (model.param.svm_type == 0 || model.param.svm_type == 1) && model.probA != null && model.probB != null || (model.param.svm_type == 3 || model.param.svm_type == 4) && model.probA != null;
    }

    public static void svm_set_print_string_function(svm_print_interface print_func) {
        svm_print_string = print_func == null ? svm_print_stdout : print_func;
    }

    static {
        svm_print_string = svm_print_stdout = new svm_print_interface(){

            public void print(String s) {
                System.out.print(s);
                System.out.flush();
            }
        };
        svm_type_table = new String[]{"c_svc", "nu_svc", "one_class", "epsilon_svr", "nu_svr"};
        kernel_type_table = new String[]{"linear", "polynomial", "rbf", "sigmoid", "precomputed"};
    }

    static class decision_function {
        double[] alpha;
        double rho;

        decision_function() {
        }
    }

}

