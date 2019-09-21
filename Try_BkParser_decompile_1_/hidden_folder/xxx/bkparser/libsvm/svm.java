package libsvm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

public class svm {
   public static final int LIBSVM_VERSION = 310;
   public static final Random rand = new Random();
   private static svm_print_interface svm_print_stdout = new svm_print_interface() {
      public void print(String s) {
         System.out.print(s);
         System.out.flush();
      }
   };
   private static svm_print_interface svm_print_string;
   static final String[] svm_type_table;
   static final String[] kernel_type_table;

   public svm() {
   }

   static void info(String s) {
      svm_print_string.print(s);
   }

   private static void solve_c_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si, double Cp, double Cn) {
      int l = prob.l;
      double[] minus_ones = new double[l];
      byte[] y = new byte[l];

      int i;
      for(i = 0; i < l; ++i) {
         alpha[i] = 0.0D;
         minus_ones[i] = -1.0D;
         if (prob.y[i] > 0.0D) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }
      }

      Solver s = new Solver();
      s.Solve(l, new SVC_Q(prob, param, y), minus_ones, y, alpha, Cp, Cn, param.eps, si, param.shrinking);
      double sum_alpha = 0.0D;

      for(i = 0; i < l; ++i) {
         sum_alpha += alpha[i];
      }

      if (Cp == Cn) {
         info("nu = " + sum_alpha / (Cp * (double)prob.l) + "\n");
      }

      for(i = 0; i < l; ++i) {
         alpha[i] *= (double)y[i];
      }

   }

   private static void solve_nu_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
      int l = prob.l;
      double nu = param.nu;
      byte[] y = new byte[l];

      int i;
      for(i = 0; i < l; ++i) {
         if (prob.y[i] > 0.0D) {
            y[i] = 1;
         } else {
            y[i] = -1;
         }
      }

      double sum_pos = nu * (double)l / 2.0D;
      double sum_neg = nu * (double)l / 2.0D;

      for(i = 0; i < l; ++i) {
         if (y[i] == 1) {
            alpha[i] = Math.min(1.0D, sum_pos);
            sum_pos -= alpha[i];
         } else {
            alpha[i] = Math.min(1.0D, sum_neg);
            sum_neg -= alpha[i];
         }
      }

      double[] zeros = new double[l];

      for(i = 0; i < l; ++i) {
         zeros[i] = 0.0D;
      }

      Solver_NU s = new Solver_NU();
      s.Solve(l, new SVC_Q(prob, param, y), zeros, y, alpha, 1.0D, 1.0D, param.eps, si, param.shrinking);
      double r = si.r;
      info("C = " + 1.0D / r + "\n");

      for(i = 0; i < l; ++i) {
         alpha[i] *= (double)y[i] / r;
      }

      si.rho /= r;
      si.obj /= r * r;
      si.upper_bound_p = 1.0D / r;
      si.upper_bound_n = 1.0D / r;
   }

   private static void solve_one_class(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
      int l = prob.l;
      double[] zeros = new double[l];
      byte[] ones = new byte[l];
      int n = (int)(param.nu * (double)prob.l);

      int i;
      for(i = 0; i < n; ++i) {
         alpha[i] = 1.0D;
      }

      if (n < prob.l) {
         alpha[n] = param.nu * (double)prob.l - (double)n;
      }

      for(i = n + 1; i < l; ++i) {
         alpha[i] = 0.0D;
      }

      for(i = 0; i < l; ++i) {
         zeros[i] = 0.0D;
         ones[i] = 1;
      }

      Solver s = new Solver();
      s.Solve(l, new ONE_CLASS_Q(prob, param), zeros, ones, alpha, 1.0D, 1.0D, param.eps, si, param.shrinking);
   }

   private static void solve_epsilon_svr(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
      int l = prob.l;
      double[] alpha2 = new double[2 * l];
      double[] linear_term = new double[2 * l];
      byte[] y = new byte[2 * l];

      int i;
      for(i = 0; i < l; ++i) {
         alpha2[i] = 0.0D;
         linear_term[i] = param.p - prob.y[i];
         y[i] = 1;
         alpha2[i + l] = 0.0D;
         linear_term[i + l] = param.p + prob.y[i];
         y[i + l] = -1;
      }

      Solver s = new Solver();
      s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y, alpha2, param.C, param.C, param.eps, si, param.shrinking);
      double sum_alpha = 0.0D;

      for(i = 0; i < l; ++i) {
         alpha[i] = alpha2[i] - alpha2[i + l];
         sum_alpha += Math.abs(alpha[i]);
      }

      info("nu = " + sum_alpha / (param.C * (double)l) + "\n");
   }

   private static void solve_nu_svr(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
      int l = prob.l;
      double C = param.C;
      double[] alpha2 = new double[2 * l];
      double[] linear_term = new double[2 * l];
      byte[] y = new byte[2 * l];
      double sum = C * param.nu * (double)l / 2.0D;

      int i;
      for(i = 0; i < l; ++i) {
         alpha2[i] = alpha2[i + l] = Math.min(sum, C);
         sum -= alpha2[i];
         linear_term[i] = -prob.y[i];
         y[i] = 1;
         linear_term[i + l] = prob.y[i];
         y[i + l] = -1;
      }

      Solver_NU s = new Solver_NU();
      s.Solve(2 * l, new SVR_Q(prob, param), linear_term, y, alpha2, C, C, param.eps, si, param.shrinking);
      info("epsilon = " + -si.r + "\n");

      for(i = 0; i < l; ++i) {
         alpha[i] = alpha2[i] - alpha2[i + l];
      }

   }

   static svm.decision_function svm_train_one(svm_problem prob, svm_parameter param, double Cp, double Cn) {
      double[] alpha = new double[prob.l];
      Solver.SolutionInfo si = new Solver.SolutionInfo();
      switch(param.svm_type) {
      case 0:
         solve_c_svc(prob, param, alpha, si, Cp, Cn);
         break;
      case 1:
         solve_nu_svc(prob, param, alpha, si);
         break;
      case 2:
         solve_one_class(prob, param, alpha, si);
         break;
      case 3:
         solve_epsilon_svr(prob, param, alpha, si);
         break;
      case 4:
         solve_nu_svr(prob, param, alpha, si);
      }

      info("obj = " + si.obj + ", rho = " + si.rho + "\n");
      int nSV = 0;
      int nBSV = 0;

      for(int i = 0; i < prob.l; ++i) {
         if (Math.abs(alpha[i]) > 0.0D) {
            ++nSV;
            if (prob.y[i] > 0.0D) {
               if (Math.abs(alpha[i]) >= si.upper_bound_p) {
                  ++nBSV;
               }
            } else if (Math.abs(alpha[i]) >= si.upper_bound_n) {
               ++nBSV;
            }
         }
      }

      info("nSV = " + nSV + ", nBSV = " + nBSV + "\n");
      svm.decision_function f = new svm.decision_function();
      f.alpha = alpha;
      f.rho = si.rho;
      return f;
   }

   private static void sigmoid_train(int l, double[] dec_values, double[] labels, double[] probAB) {
      double prior1 = 0.0D;
      double prior0 = 0.0D;

      int i;
      for(i = 0; i < l; ++i) {
         if (labels[i] > 0.0D) {
            ++prior1;
         } else {
            ++prior0;
         }
      }

      int max_iter = 100;
      double min_step = 1.0E-10D;
      double sigma = 1.0E-12D;
      double eps = 1.0E-5D;
      double hiTarget = (prior1 + 1.0D) / (prior1 + 2.0D);
      double loTarget = 1.0D / (prior0 + 2.0D);
      double[] t = new double[l];
      double A = 0.0D;
      double B = Math.log((prior0 + 1.0D) / (prior1 + 1.0D));
      double fval = 0.0D;

      double fApB;
      for(i = 0; i < l; ++i) {
         if (labels[i] > 0.0D) {
            t[i] = hiTarget;
         } else {
            t[i] = loTarget;
         }

         fApB = dec_values[i] * A + B;
         if (fApB >= 0.0D) {
            fval += t[i] * fApB + Math.log(1.0D + Math.exp(-fApB));
         } else {
            fval += (t[i] - 1.0D) * fApB + Math.log(1.0D + Math.exp(fApB));
         }
      }

      int iter;
      for(iter = 0; iter < max_iter; ++iter) {
         double h11 = sigma;
         double h22 = sigma;
         double h21 = 0.0D;
         double g1 = 0.0D;
         double g2 = 0.0D;

         for(i = 0; i < l; ++i) {
            fApB = dec_values[i] * A + B;
            double p;
            double q;
            if (fApB >= 0.0D) {
               p = Math.exp(-fApB) / (1.0D + Math.exp(-fApB));
               q = 1.0D / (1.0D + Math.exp(-fApB));
            } else {
               p = 1.0D / (1.0D + Math.exp(fApB));
               q = Math.exp(fApB) / (1.0D + Math.exp(fApB));
            }

            double d2 = p * q;
            h11 += dec_values[i] * dec_values[i] * d2;
            h22 += d2;
            h21 += dec_values[i] * d2;
            double d1 = t[i] - p;
            g1 += dec_values[i] * d1;
            g2 += d1;
         }

         if (Math.abs(g1) < eps && Math.abs(g2) < eps) {
            break;
         }

         double det = h11 * h22 - h21 * h21;
         double dA = -(h22 * g1 - h21 * g2) / det;
         double dB = -(-h21 * g1 + h11 * g2) / det;
         double gd = g1 * dA + g2 * dB;

         double stepsize;
         for(stepsize = 1.0D; stepsize >= min_step; stepsize /= 2.0D) {
            double newA = A + stepsize * dA;
            double newB = B + stepsize * dB;
            double newf = 0.0D;

            for(i = 0; i < l; ++i) {
               fApB = dec_values[i] * newA + newB;
               if (fApB >= 0.0D) {
                  newf += t[i] * fApB + Math.log(1.0D + Math.exp(-fApB));
               } else {
                  newf += (t[i] - 1.0D) * fApB + Math.log(1.0D + Math.exp(fApB));
               }
            }

            if (newf < fval + 1.0E-4D * stepsize * gd) {
               A = newA;
               B = newB;
               fval = newf;
               break;
            }
         }

         if (stepsize < min_step) {
            info("Line search fails in two-class probability estimates\n");
            break;
         }
      }

      if (iter >= max_iter) {
         info("Reaching maximal iterations in two-class probability estimates\n");
      }

      probAB[0] = A;
      probAB[1] = B;
   }

   private static double sigmoid_predict(double decision_value, double A, double B) {
      double fApB = decision_value * A + B;
      return fApB >= 0.0D ? Math.exp(-fApB) / (1.0D + Math.exp(-fApB)) : 1.0D / (1.0D + Math.exp(fApB));
   }

   private static void multiclass_probability(int k, double[][] r, double[] p) {
      int iter = false;
      int max_iter = Math.max(100, k);
      double[][] Q = new double[k][k];
      double[] Qp = new double[k];
      double eps = 0.005D / (double)k;

      int t;
      int j;
      for(t = 0; t < k; ++t) {
         p[t] = 1.0D / (double)k;
         Q[t][t] = 0.0D;

         for(j = 0; j < t; ++j) {
            Q[t][t] += r[j][t] * r[j][t];
            Q[t][j] = Q[j][t];
         }

         for(j = t + 1; j < k; ++j) {
            Q[t][t] += r[j][t] * r[j][t];
            Q[t][j] = -r[j][t] * r[t][j];
         }
      }

      int iter;
      for(iter = 0; iter < max_iter; ++iter) {
         double pQp = 0.0D;

         for(t = 0; t < k; ++t) {
            Qp[t] = 0.0D;

            for(j = 0; j < k; ++j) {
               Qp[t] += Q[t][j] * p[j];
            }

            pQp += p[t] * Qp[t];
         }

         double max_error = 0.0D;

         double diff;
         for(t = 0; t < k; ++t) {
            diff = Math.abs(Qp[t] - pQp);
            if (diff > max_error) {
               max_error = diff;
            }
         }

         if (max_error < eps) {
            break;
         }

         for(t = 0; t < k; ++t) {
            diff = (-Qp[t] + pQp) / Q[t][t];
            p[t] += diff;
            pQp = (pQp + diff * (diff * Q[t][t] + 2.0D * Qp[t])) / (1.0D + diff) / (1.0D + diff);

            for(j = 0; j < k; ++j) {
               Qp[j] = (Qp[j] + diff * Q[t][j]) / (1.0D + diff);
               p[j] /= 1.0D + diff;
            }
         }
      }

      if (iter >= max_iter) {
         info("Exceeds max_iter in multiclass_prob\n");
      }

   }

   private static void svm_binary_svc_probability(svm_problem prob, svm_parameter param, double Cp, double Cn, double[] probAB) {
      int nr_fold = 5;
      int[] perm = new int[prob.l];
      double[] dec_values = new double[prob.l];

      int i;
      for(i = 0; i < prob.l; perm[i] = i++) {
      }

      int begin;
      int end;
      for(i = 0; i < prob.l; ++i) {
         begin = i + rand.nextInt(prob.l - i);
         end = perm[i];
         perm[i] = perm[begin];
         perm[begin] = end;
      }

      for(i = 0; i < nr_fold; ++i) {
         begin = i * prob.l / nr_fold;
         end = (i + 1) * prob.l / nr_fold;
         svm_problem subprob = new svm_problem();
         subprob.l = prob.l - (end - begin);
         subprob.x = new svm_node[subprob.l][];
         subprob.y = new double[subprob.l];
         int k = 0;

         int j;
         for(j = 0; j < begin; ++j) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }

         for(j = end; j < prob.l; ++j) {
            subprob.x[k] = prob.x[perm[j]];
            subprob.y[k] = prob.y[perm[j]];
            ++k;
         }

         int p_count = 0;
         int n_count = 0;

         for(j = 0; j < k; ++j) {
            if (subprob.y[j] > 0.0D) {
               ++p_count;
            } else {
               ++n_count;
            }
         }

         if (p_count == 0 && n_count == 0) {
            for(j = begin; j < end; ++j) {
               dec_values[perm[j]] = 0.0D;
            }
         } else if (p_count > 0 && n_count == 0) {
            for(j = begin; j < end; ++j) {
               dec_values[perm[j]] = 1.0D;
            }
         } else if (p_count == 0 && n_count > 0) {
            for(j = begin; j < end; ++j) {
               dec_values[perm[j]] = -1.0D;
            }
         } else {
            svm_parameter subparam = (svm_parameter)param.clone();
            subparam.probability = 0;
            subparam.C = 1.0D;
            subparam.nr_weight = 2;
            subparam.weight_label = new int[2];
            subparam.weight = new double[2];
            subparam.weight_label[0] = 1;
            subparam.weight_label[1] = -1;
            subparam.weight[0] = Cp;
            subparam.weight[1] = Cn;
            svm_model submodel = svm_train(subprob, subparam);

            for(j = begin; j < end; ++j) {
               double[] dec_value = new double[1];
               svm_predict_values(submodel, prob.x[perm[j]], dec_value);
               dec_values[perm[j]] = dec_value[0];
               dec_values[perm[j]] *= (double)submodel.label[0];
            }
         }
      }

      sigmoid_train(prob.l, dec_values, prob.y, probAB);
   }

   private static double svm_svr_probability(svm_problem prob, svm_parameter param) {
      int nr_fold = 5;
      double[] ymv = new double[prob.l];
      double mae = 0.0D;
      svm_parameter newparam = (svm_parameter)param.clone();
      newparam.probability = 0;
      svm_cross_validation(prob, newparam, nr_fold, ymv);

      int i;
      for(i = 0; i < prob.l; ++i) {
         ymv[i] = prob.y[i] - ymv[i];
         mae += Math.abs(ymv[i]);
      }

      mae /= (double)prob.l;
      double std = Math.sqrt(2.0D * mae * mae);
      int count = 0;
      mae = 0.0D;

      for(i = 0; i < prob.l; ++i) {
         if (Math.abs(ymv[i]) > 5.0D * std) {
            ++count;
         } else {
            mae += Math.abs(ymv[i]);
         }
      }

      mae /= (double)(prob.l - count);
      info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + mae + "\n");
      return mae;
   }

   private static void svm_group_classes(svm_problem prob, int[] nr_class_ret, int[][] label_ret, int[][] start_ret, int[][] count_ret, int[] perm) {
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
               int[] new_data = new int[max_nr_class];
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

      nr_class_ret[0] = nr_class;
      label_ret[0] = label;
      start_ret[0] = start;
      count_ret[0] = count;
   }

   public static svm_model svm_train(svm_problem prob, svm_parameter param) {
      svm_model model = new svm_model();
      model.param = param;
      if (param.svm_type != 2 && param.svm_type != 3 && param.svm_type != 4) {
         int l = prob.l;
         int[] tmp_nr_class = new int[1];
         int[][] tmp_label = new int[1][];
         int[][] tmp_start = new int[1][];
         int[][] tmp_count = new int[1][];
         int[] perm = new int[l];
         svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);
         int nr_class = tmp_nr_class[0];
         int[] label = tmp_label[0];
         int[] start = tmp_start[0];
         int[] count = tmp_count[0];
         svm_node[][] x = new svm_node[l][];

         int i;
         for(i = 0; i < l; ++i) {
            x[i] = prob.x[perm[i]];
         }

         double[] weighted_C = new double[nr_class];

         for(i = 0; i < nr_class; ++i) {
            weighted_C[i] = param.C;
         }

         for(i = 0; i < param.nr_weight; ++i) {
            int j;
            for(j = 0; j < nr_class && param.weight_label[i] != label[j]; ++j) {
            }

            if (j == nr_class) {
               System.err.print("warning: class label " + param.weight_label[i] + " specified in weight is not found\n");
            } else {
               weighted_C[j] *= param.weight[i];
            }
         }

         boolean[] nonzero = new boolean[l];

         for(i = 0; i < l; ++i) {
            nonzero[i] = false;
         }

         svm.decision_function[] f = new svm.decision_function[nr_class * (nr_class - 1) / 2];
         double[] probA = null;
         double[] probB = null;
         if (param.probability == 1) {
            probA = new double[nr_class * (nr_class - 1) / 2];
            probB = new double[nr_class * (nr_class - 1) / 2];
         }

         int p = 0;

         int nnz;
         int nSV;
         int j;
         int si;
         int sj;
         int ci;
         for(i = 0; i < nr_class; ++i) {
            for(nnz = i + 1; nnz < nr_class; ++nnz) {
               svm_problem sub_prob = new svm_problem();
               nSV = start[i];
               j = start[nnz];
               si = count[i];
               sj = count[nnz];
               sub_prob.l = si + sj;
               sub_prob.x = new svm_node[sub_prob.l][];
               sub_prob.y = new double[sub_prob.l];

               for(ci = 0; ci < si; ++ci) {
                  sub_prob.x[ci] = x[nSV + ci];
                  sub_prob.y[ci] = 1.0D;
               }

               for(ci = 0; ci < sj; ++ci) {
                  sub_prob.x[si + ci] = x[j + ci];
                  sub_prob.y[si + ci] = -1.0D;
               }

               if (param.probability == 1) {
                  double[] probAB = new double[2];
                  svm_binary_svc_probability(sub_prob, param, weighted_C[i], weighted_C[nnz], probAB);
                  probA[p] = probAB[0];
                  probB[p] = probAB[1];
               }

               f[p] = svm_train_one(sub_prob, param, weighted_C[i], weighted_C[nnz]);

               for(ci = 0; ci < si; ++ci) {
                  if (!nonzero[nSV + ci] && Math.abs(f[p].alpha[ci]) > 0.0D) {
                     nonzero[nSV + ci] = true;
                  }
               }

               for(ci = 0; ci < sj; ++ci) {
                  if (!nonzero[j + ci] && Math.abs(f[p].alpha[si + ci]) > 0.0D) {
                     nonzero[j + ci] = true;
                  }
               }

               ++p;
            }
         }

         model.nr_class = nr_class;
         model.label = new int[nr_class];

         for(i = 0; i < nr_class; ++i) {
            model.label[i] = label[i];
         }

         model.rho = new double[nr_class * (nr_class - 1) / 2];

         for(i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
            model.rho[i] = f[i].rho;
         }

         if (param.probability == 1) {
            model.probA = new double[nr_class * (nr_class - 1) / 2];
            model.probB = new double[nr_class * (nr_class - 1) / 2];

            for(i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
               model.probA[i] = probA[i];
               model.probB[i] = probB[i];
            }
         } else {
            model.probA = null;
            model.probB = null;
         }

         nnz = 0;
         int[] nz_count = new int[nr_class];
         model.nSV = new int[nr_class];

         for(i = 0; i < nr_class; ++i) {
            nSV = 0;

            for(j = 0; j < count[i]; ++j) {
               if (nonzero[start[i] + j]) {
                  ++nSV;
                  ++nnz;
               }
            }

            model.nSV[i] = nSV;
            nz_count[i] = nSV;
         }

         info("Total nSV = " + nnz + "\n");
         model.l = nnz;
         model.SV = new svm_node[nnz][];
         p = 0;

         for(i = 0; i < l; ++i) {
            if (nonzero[i]) {
               model.SV[p++] = x[i];
            }
         }

         int[] nz_start = new int[nr_class];
         nz_start[0] = 0;

         for(i = 1; i < nr_class; ++i) {
            nz_start[i] = nz_start[i - 1] + nz_count[i - 1];
         }

         model.sv_coef = new double[nr_class - 1][];

         for(i = 0; i < nr_class - 1; ++i) {
            model.sv_coef[i] = new double[nnz];
         }

         p = 0;

         for(i = 0; i < nr_class; ++i) {
            for(j = i + 1; j < nr_class; ++j) {
               si = start[i];
               sj = start[j];
               ci = count[i];
               int cj = count[j];
               int q = nz_start[i];

               int k;
               for(k = 0; k < ci; ++k) {
                  if (nonzero[si + k]) {
                     model.sv_coef[j - 1][q++] = f[p].alpha[k];
                  }
               }

               q = nz_start[j];

               for(k = 0; k < cj; ++k) {
                  if (nonzero[sj + k]) {
                     model.sv_coef[i][q++] = f[p].alpha[ci + k];
                  }
               }

               ++p;
            }
         }
      } else {
         model.nr_class = 2;
         model.label = null;
         model.nSV = null;
         model.probA = null;
         model.probB = null;
         model.sv_coef = new double[1][];
         if (param.probability == 1 && (param.svm_type == 3 || param.svm_type == 4)) {
            model.probA = new double[1];
            model.probA[0] = svm_svr_probability(prob, param);
         }

         svm.decision_function f = svm_train_one(prob, param, 0.0D, 0.0D);
         model.rho = new double[1];
         model.rho[0] = f.rho;
         int nSV = 0;

         int i;
         for(i = 0; i < prob.l; ++i) {
            if (Math.abs(f.alpha[i]) > 0.0D) {
               ++nSV;
            }
         }

         model.l = nSV;
         model.SV = new svm_node[nSV][];
         model.sv_coef[0] = new double[nSV];
         int j = 0;

         for(i = 0; i < prob.l; ++i) {
            if (Math.abs(f.alpha[i]) > 0.0D) {
               model.SV[j] = prob.x[i];
               model.sv_coef[0][j] = f.alpha[i];
               ++j;
            }
         }
      }

      return model;
   }

   public static void svm_cross_validation(svm_problem prob, svm_parameter param, int nr_fold, double[] target) {
      int[] fold_start = new int[nr_fold + 1];
      int l = prob.l;
      int[] perm = new int[l];
      int i;
      int begin;
      int end;
      if ((param.svm_type == 0 || param.svm_type == 1) && nr_fold < l) {
         int[] tmp_nr_class = new int[1];
         int[][] tmp_label = new int[1][];
         int[][] tmp_start = new int[1][];
         int[][] tmp_count = new int[1][];
         svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);
         int nr_class = tmp_nr_class[0];
         int[] start = tmp_start[0];
         int[] count = tmp_count[0];
         int[] fold_count = new int[nr_fold];
         int[] index = new int[l];

         for(i = 0; i < l; ++i) {
            index[i] = perm[i];
         }

         int c;
         int begin;
         int end;
         for(c = 0; c < nr_class; ++c) {
            for(i = 0; i < count[c]; ++i) {
               begin = i + rand.nextInt(count[c] - i);
               end = index[start[c] + begin];
               index[start[c] + begin] = index[start[c] + i];
               index[start[c] + i] = end;
            }
         }

         for(i = 0; i < nr_fold; ++i) {
            fold_count[i] = 0;

            for(c = 0; c < nr_class; ++c) {
               fold_count[i] += (i + 1) * count[c] / nr_fold - i * count[c] / nr_fold;
            }
         }

         fold_start[0] = 0;

         for(i = 1; i <= nr_fold; ++i) {
            fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
         }

         for(c = 0; c < nr_class; ++c) {
            for(i = 0; i < nr_fold; ++i) {
               begin = start[c] + i * count[c] / nr_fold;
               end = start[c] + (i + 1) * count[c] / nr_fold;

               for(int j = begin; j < end; ++j) {
                  perm[fold_start[i]] = index[j];
                  int var10002 = fold_start[i]++;
               }
            }
         }

         fold_start[0] = 0;

         for(i = 1; i <= nr_fold; ++i) {
            fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
         }
      } else {
         for(i = 0; i < l; perm[i] = i++) {
         }

         for(i = 0; i < l; ++i) {
            begin = i + rand.nextInt(l - i);
            end = perm[i];
            perm[i] = perm[begin];
            perm[begin] = end;
         }

         for(i = 0; i <= nr_fold; ++i) {
            fold_start[i] = i * l / nr_fold;
         }
      }

      for(i = 0; i < nr_fold; ++i) {
         begin = fold_start[i];
         end = fold_start[i + 1];
         svm_problem subprob = new svm_problem();
         subprob.l = l - (end - begin);
         subprob.x = new svm_node[subprob.l][];
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

         svm_model submodel = svm_train(subprob, param);
         if (param.probability != 1 || param.svm_type != 0 && param.svm_type != 1) {
            for(j = begin; j < end; ++j) {
               target[perm[j]] = svm_predict(submodel, prob.x[perm[j]]);
            }
         } else {
            double[] prob_estimates = new double[svm_get_nr_class(submodel)];

            for(j = begin; j < end; ++j) {
               target[perm[j]] = svm_predict_probability(submodel, prob.x[perm[j]], prob_estimates);
            }
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
         for(int i = 0; i < model.nr_class; ++i) {
            label[i] = model.label[i];
         }
      }

   }

   public static double svm_get_svr_probability(svm_model model) {
      if ((model.param.svm_type == 3 || model.param.svm_type == 4) && model.probA != null) {
         return model.probA[0];
      } else {
         System.err.print("Model doesn't contain information for SVR probability inference\n");
         return 0.0D;
      }
   }

   public static double svm_predict_values(svm_model model, svm_node[] x, double[] dec_values) {
      if (model.param.svm_type != 2 && model.param.svm_type != 3 && model.param.svm_type != 4) {
         int nr_class = model.nr_class;
         int l = model.l;
         double[] kvalue = new double[l];

         int i;
         for(i = 0; i < l; ++i) {
            kvalue[i] = Kernel.k_function(x, model.SV[i], model.param);
         }

         int[] start = new int[nr_class];
         start[0] = 0;

         for(i = 1; i < nr_class; ++i) {
            start[i] = start[i - 1] + model.nSV[i - 1];
         }

         int[] vote = new int[nr_class];

         for(i = 0; i < nr_class; ++i) {
            vote[i] = 0;
         }

         int p = 0;

         int j;
         for(i = 0; i < nr_class; ++i) {
            for(j = i + 1; j < nr_class; ++j) {
               double sum = 0.0D;
               int si = start[i];
               int sj = start[j];
               int ci = model.nSV[i];
               int cj = model.nSV[j];
               double[] coef1 = model.sv_coef[j - 1];
               double[] coef2 = model.sv_coef[i];

               int k;
               for(k = 0; k < ci; ++k) {
                  sum += coef1[si + k] * kvalue[si + k];
               }

               for(k = 0; k < cj; ++k) {
                  sum += coef2[sj + k] * kvalue[sj + k];
               }

               sum -= model.rho[p];
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

         j = 0;

         for(i = 1; i < nr_class; ++i) {
            if (vote[i] > vote[j]) {
               j = i;
            }
         }

         return (double)model.label[j];
      } else {
         double[] sv_coef = model.sv_coef[0];
         double sum = 0.0D;

         for(int i = 0; i < model.l; ++i) {
            sum += sv_coef[i] * Kernel.k_function(x, model.SV[i], model.param);
         }

         sum -= model.rho[0];
         dec_values[0] = sum;
         if (model.param.svm_type == 2) {
            return sum > 0.0D ? 1.0D : -1.0D;
         } else {
            return sum;
         }
      }
   }

   public static double svm_predict(svm_model model, svm_node[] x) {
      int nr_class = model.nr_class;
      double[] dec_values;
      if (model.param.svm_type != 2 && model.param.svm_type != 3 && model.param.svm_type != 4) {
         dec_values = new double[nr_class * (nr_class - 1) / 2];
      } else {
         dec_values = new double[1];
      }

      double pred_result = svm_predict_values(model, x, dec_values);
      return pred_result;
   }

   public static double svm_predict_probability(svm_model model, svm_node[] x, double[] prob_estimates) {
      if ((model.param.svm_type == 0 || model.param.svm_type == 1) && model.probA != null && model.probB != null) {
         int nr_class = model.nr_class;
         double[] dec_values = new double[nr_class * (nr_class - 1) / 2];
         svm_predict_values(model, x, dec_values);
         double min_prob = 1.0E-7D;
         double[][] pairwise_prob = new double[nr_class][nr_class];
         int k = 0;

         int i;
         int prob_max_idx;
         for(i = 0; i < nr_class; ++i) {
            for(prob_max_idx = i + 1; prob_max_idx < nr_class; ++prob_max_idx) {
               pairwise_prob[i][prob_max_idx] = Math.min(Math.max(sigmoid_predict(dec_values[k], model.probA[k], model.probB[k]), min_prob), 1.0D - min_prob);
               pairwise_prob[prob_max_idx][i] = 1.0D - pairwise_prob[i][prob_max_idx];
               ++k;
            }
         }

         multiclass_probability(nr_class, pairwise_prob, prob_estimates);
         prob_max_idx = 0;

         for(i = 1; i < nr_class; ++i) {
            if (prob_estimates[i] > prob_estimates[prob_max_idx]) {
               prob_max_idx = i;
            }
         }

         return (double)model.label[prob_max_idx];
      } else {
         return svm_predict(model, x);
      }
   }

   public static void svm_save_model(String model_file_name, svm_model model) throws IOException {
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

      int i;
      for(i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
         fp.writeBytes(" " + model.rho[i]);
      }

      fp.writeBytes("\n");
      if (model.label != null) {
         fp.writeBytes("label");

         for(i = 0; i < nr_class; ++i) {
            fp.writeBytes(" " + model.label[i]);
         }

         fp.writeBytes("\n");
      }

      if (model.probA != null) {
         fp.writeBytes("probA");

         for(i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
            fp.writeBytes(" " + model.probA[i]);
         }

         fp.writeBytes("\n");
      }

      if (model.probB != null) {
         fp.writeBytes("probB");

         for(i = 0; i < nr_class * (nr_class - 1) / 2; ++i) {
            fp.writeBytes(" " + model.probB[i]);
         }

         fp.writeBytes("\n");
      }

      if (model.nSV != null) {
         fp.writeBytes("nr_sv");

         for(i = 0; i < nr_class; ++i) {
            fp.writeBytes(" " + model.nSV[i]);
         }

         fp.writeBytes("\n");
      }

      fp.writeBytes("SV\n");
      double[][] sv_coef = model.sv_coef;
      svm_node[][] SV = model.SV;

      for(int i = 0; i < l; ++i) {
         for(int j = 0; j < nr_class - 1; ++j) {
            fp.writeBytes(sv_coef[j][i] + " ");
         }

         svm_node[] p = SV[i];
         if (param.kernel_type == 4) {
            fp.writeBytes("0:" + (int)p[0].value);
         } else {
            for(int j = 0; j < p.length; ++j) {
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
      return svm_load_model(new BufferedReader(new FileReader(model_file_name)));
   }

   public static svm_model svm_load_model(BufferedReader fp) throws IOException {
      svm_model model = new svm_model();
      svm_parameter param = new svm_parameter();
      model.param = param;
      model.rho = null;
      model.probA = null;
      model.probB = null;
      model.label = null;
      model.nSV = null;

      int i;
      label155:
      do {
         while(true) {
            String cmd = fp.readLine();
            String arg = cmd.substring(cmd.indexOf(32) + 1);
            if (cmd.startsWith("svm_type")) {
               for(i = 0; i < svm_type_table.length; ++i) {
                  if (arg.indexOf(svm_type_table[i]) != -1) {
                     param.svm_type = i;
                     continue label155;
                  }
               }
               break;
            }

            if (!cmd.startsWith("kernel_type")) {
               if (cmd.startsWith("degree")) {
                  param.degree = atoi(arg);
               } else if (cmd.startsWith("gamma")) {
                  param.gamma = atof(arg);
               } else if (cmd.startsWith("coef0")) {
                  param.coef0 = atof(arg);
               } else if (cmd.startsWith("nr_class")) {
                  model.nr_class = atoi(arg);
               } else if (cmd.startsWith("total_sv")) {
                  model.l = atoi(arg);
               } else {
                  StringTokenizer st;
                  int i;
                  if (cmd.startsWith("rho")) {
                     i = model.nr_class * (model.nr_class - 1) / 2;
                     model.rho = new double[i];
                     st = new StringTokenizer(arg);

                     for(i = 0; i < i; ++i) {
                        model.rho[i] = atof(st.nextToken());
                     }
                  } else if (cmd.startsWith("label")) {
                     i = model.nr_class;
                     model.label = new int[i];
                     st = new StringTokenizer(arg);

                     for(i = 0; i < i; ++i) {
                        model.label[i] = atoi(st.nextToken());
                     }
                  } else if (cmd.startsWith("probA")) {
                     i = model.nr_class * (model.nr_class - 1) / 2;
                     model.probA = new double[i];
                     st = new StringTokenizer(arg);

                     for(i = 0; i < i; ++i) {
                        model.probA[i] = atof(st.nextToken());
                     }
                  } else if (cmd.startsWith("probB")) {
                     i = model.nr_class * (model.nr_class - 1) / 2;
                     model.probB = new double[i];
                     st = new StringTokenizer(arg);

                     for(i = 0; i < i; ++i) {
                        model.probB[i] = atof(st.nextToken());
                     }
                  } else {
                     if (!cmd.startsWith("nr_sv")) {
                        if (!cmd.startsWith("SV")) {
                           System.err.print("unknown text in model file: [" + cmd + "]\n");
                           return null;
                        }

                        int m = model.nr_class - 1;
                        int l = model.l;
                        model.sv_coef = new double[m][l];
                        model.SV = new svm_node[l][];

                        for(i = 0; i < l; ++i) {
                           String line = fp.readLine();
                           StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

                           int n;
                           for(n = 0; n < m; ++n) {
                              model.sv_coef[n][i] = atof(st.nextToken());
                           }

                           n = st.countTokens() / 2;
                           model.SV[i] = new svm_node[n];

                           for(int j = 0; j < n; ++j) {
                              model.SV[i][j] = new svm_node();
                              model.SV[i][j].index = atoi(st.nextToken());
                              model.SV[i][j].value = atof(st.nextToken());
                           }
                        }

                        fp.close();
                        return model;
                     }

                     i = model.nr_class;
                     model.nSV = new int[i];
                     st = new StringTokenizer(arg);

                     for(i = 0; i < i; ++i) {
                        model.nSV[i] = atoi(st.nextToken());
                     }
                  }
               }
            } else {
               for(i = 0; i < kernel_type_table.length; ++i) {
                  if (arg.indexOf(kernel_type_table[i]) != -1) {
                     param.kernel_type = i;
                     break;
                  }
               }

               if (i == kernel_type_table.length) {
                  System.err.print("unknown kernel function.\n");
                  return null;
               }
            }
         }
      } while(i != svm_type_table.length);

      System.err.print("unknown svm type.\n");
      return null;
   }

   public static String svm_check_parameter(svm_problem prob, svm_parameter param) {
      int svm_type = param.svm_type;
      if (svm_type != 0 && svm_type != 1 && svm_type != 2 && svm_type != 3 && svm_type != 4) {
         return "unknown svm type";
      } else {
         int kernel_type = param.kernel_type;
         if (kernel_type != 0 && kernel_type != 1 && kernel_type != 2 && kernel_type != 3 && kernel_type != 4) {
            return "unknown kernel type";
         } else if (param.gamma < 0.0D) {
            return "gamma < 0";
         } else if (param.degree < 0) {
            return "degree of polynomial kernel < 0";
         } else if (param.cache_size <= 0.0D) {
            return "cache_size <= 0";
         } else if (param.eps <= 0.0D) {
            return "eps <= 0";
         } else if ((svm_type == 0 || svm_type == 3 || svm_type == 4) && param.C <= 0.0D) {
            return "C <= 0";
         } else if ((svm_type == 1 || svm_type == 2 || svm_type == 4) && (param.nu <= 0.0D || param.nu > 1.0D)) {
            return "nu <= 0 or nu > 1";
         } else if (svm_type == 3 && param.p < 0.0D) {
            return "p < 0";
         } else if (param.shrinking != 0 && param.shrinking != 1) {
            return "shrinking != 0 and shrinking != 1";
         } else if (param.probability != 0 && param.probability != 1) {
            return "probability != 0 and probability != 1";
         } else if (param.probability == 1 && svm_type == 2) {
            return "one-class SVM probability output not supported yet";
         } else {
            if (svm_type == 1) {
               int l = prob.l;
               int max_nr_class = 16;
               int nr_class = 0;
               int[] label = new int[max_nr_class];
               int[] count = new int[max_nr_class];

               int i;
               int this_label;
               int j;
               for(i = 0; i < l; ++i) {
                  this_label = (int)prob.y[i];

                  for(j = 0; j < nr_class; ++j) {
                     if (this_label == label[j]) {
                        int var10002 = count[j]++;
                        break;
                     }
                  }

                  if (j == nr_class) {
                     if (nr_class == max_nr_class) {
                        max_nr_class *= 2;
                        int[] new_data = new int[max_nr_class];
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
               }

               for(i = 0; i < nr_class; ++i) {
                  this_label = count[i];

                  for(j = i + 1; j < nr_class; ++j) {
                     int n2 = count[j];
                     if (param.nu * (double)(this_label + n2) / 2.0D > (double)Math.min(this_label, n2)) {
                        return "specified nu is infeasible";
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static int svm_check_probability_model(svm_model model) {
      return (model.param.svm_type != 0 && model.param.svm_type != 1 || model.probA == null || model.probB == null) && (model.param.svm_type != 3 && model.param.svm_type != 4 || model.probA == null) ? 0 : 1;
   }

   public static void svm_set_print_string_function(svm_print_interface print_func) {
      if (print_func == null) {
         svm_print_string = svm_print_stdout;
      } else {
         svm_print_string = print_func;
      }

   }

   static {
      svm_print_string = svm_print_stdout;
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
