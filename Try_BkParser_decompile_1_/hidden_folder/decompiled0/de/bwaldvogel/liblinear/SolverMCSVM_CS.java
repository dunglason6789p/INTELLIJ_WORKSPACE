/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

import de.bwaldvogel.liblinear.ArraySorter;
import de.bwaldvogel.liblinear.DoubleArrayPointer;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.IntArrayPointer;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Problem;
import java.util.Random;

class SolverMCSVM_CS {
    private final double[] B;
    private final double[] C;
    private final double eps;
    private final double[] G;
    private final int max_iter;
    private final int w_size;
    private final int l;
    private final int nr_class;
    private final Problem prob;

    public SolverMCSVM_CS(Problem prob, int nr_class, double[] C) {
        this(prob, nr_class, C, 0.1);
    }

    public SolverMCSVM_CS(Problem prob, int nr_class, double[] C, double eps) {
        this(prob, nr_class, C, eps, 100000);
    }

    public SolverMCSVM_CS(Problem prob, int nr_class, double[] weighted_C, double eps, int max_iter) {
        this.w_size = prob.n;
        this.l = prob.l;
        this.nr_class = nr_class;
        this.eps = eps;
        this.max_iter = max_iter;
        this.prob = prob;
        this.C = weighted_C;
        this.B = new double[nr_class];
        this.G = new double[nr_class];
    }

    private int GETI(int i) {
        return this.prob.y[i];
    }

    private boolean be_shrunk(int i, int m, int yi, double alpha_i, double minG) {
        double bound = 0.0;
        if (m == yi) {
            bound = this.C[this.GETI(i)];
        }
        return alpha_i == bound && this.G[m] < minG;
    }

    public void solve(double[] w) {
        int i;
        int m;
        int iter = 0;
        double[] alpha = new double[this.l * this.nr_class];
        double[] alpha_new = new double[this.nr_class];
        int[] index = new int[this.l];
        double[] QD = new double[this.l];
        int[] d_ind = new int[this.nr_class];
        double[] d_val = new double[this.nr_class];
        int[] alpha_index = new int[this.nr_class * this.l];
        int[] y_index = new int[this.l];
        int active_size = this.l;
        int[] active_size_i = new int[this.l];
        double eps_shrink = Math.max(10.0 * this.eps, 1.0);
        boolean start_from_all = true;
        for (i = 0; i < this.l * this.nr_class; ++i) {
            alpha[i] = 0.0;
        }
        for (i = 0; i < this.w_size * this.nr_class; ++i) {
            w[i] = 0.0;
        }
        for (i = 0; i < this.l; ++i) {
            for (m = 0; m < this.nr_class; ++m) {
                alpha_index[i * this.nr_class + m] = m;
            }
            QD[i] = 0.0;
            for (FeatureNode xi : this.prob.x[i]) {
                double[] arrd = QD;
                int n = i;
                arrd[n] = arrd[n] + xi.value * xi.value;
            }
            active_size_i[i] = this.nr_class;
            y_index[i] = this.prob.y[i];
            index[i] = i;
        }
        DoubleArrayPointer alpha_i = new DoubleArrayPointer(alpha, 0);
        IntArrayPointer alpha_index_i = new IntArrayPointer(alpha_index, 0);
        while (iter < this.max_iter) {
            double stopping = Double.NEGATIVE_INFINITY;
            for (i = 0; i < active_size; ++i) {
                int j = i + Linear.random.nextInt(active_size - i);
                Linear.swap(index, i, j);
            }
            for (int s = 0; s < active_size; ++s) {
                i = index[s];
                double Ai = QD[i];
                alpha_i.setOffset(i * this.nr_class);
                alpha_index_i.setOffset(i * this.nr_class);
                if (!(Ai > 0.0)) continue;
                for (m = 0; m < active_size_i[i]; ++m) {
                    this.G[m] = 1.0;
                }
                if (y_index[i] < active_size_i[i]) {
                    this.G[y_index[i]] = 0.0;
                }
                for (FeatureNode xi : this.prob.x[i]) {
                    int w_offset = (xi.index - 1) * this.nr_class;
                    for (m = 0; m < active_size_i[i]; ++m) {
                        double[] arrd = this.G;
                        int n = m;
                        arrd[n] = arrd[n] + w[w_offset + alpha_index_i.get(m)] * xi.value;
                    }
                }
                double minG = Double.POSITIVE_INFINITY;
                double maxG = Double.NEGATIVE_INFINITY;
                for (m = 0; m < active_size_i[i]; ++m) {
                    if (alpha_i.get(alpha_index_i.get(m)) < 0.0 && this.G[m] < minG) {
                        minG = this.G[m];
                    }
                    if (!(this.G[m] > maxG)) continue;
                    maxG = this.G[m];
                }
                if (y_index[i] < active_size_i[i] && alpha_i.get(this.prob.y[i]) < this.C[this.GETI(i)] && this.G[y_index[i]] < minG) {
                    minG = this.G[y_index[i]];
                }
                block12 : for (m = 0; m < active_size_i[i]; ++m) {
                    if (!this.be_shrunk(i, m, y_index[i], alpha_i.get(alpha_index_i.get(m)), minG)) continue;
                    int[] arrn = active_size_i;
                    int n = i;
                    arrn[n] = arrn[n] - 1;
                    while (active_size_i[i] > m) {
                        if (!this.be_shrunk(i, active_size_i[i], y_index[i], alpha_i.get(alpha_index_i.get(active_size_i[i])), minG)) {
                            Linear.swap(alpha_index_i, m, active_size_i[i]);
                            Linear.swap(this.G, m, active_size_i[i]);
                            if (y_index[i] == active_size_i[i]) {
                                y_index[i] = m;
                                continue block12;
                            }
                            if (y_index[i] != m) continue block12;
                            y_index[i] = active_size_i[i];
                            continue block12;
                        }
                        int[] arrn2 = active_size_i;
                        int n2 = i;
                        arrn2[n2] = arrn2[n2] - 1;
                    }
                }
                if (active_size_i[i] <= 1) {
                    Linear.swap(index, s, --active_size);
                    --s;
                    continue;
                }
                if (maxG - minG <= 1.0E-12) continue;
                stopping = Math.max(maxG - minG, stopping);
                for (m = 0; m < active_size_i[i]; ++m) {
                    this.B[m] = this.G[m] - Ai * alpha_i.get(alpha_index_i.get(m));
                }
                this.solve_sub_problem(Ai, y_index[i], this.C[this.GETI(i)], active_size_i[i], alpha_new);
                int nz_d = 0;
                for (m = 0; m < active_size_i[i]; ++m) {
                    double d = alpha_new[m] - alpha_i.get(alpha_index_i.get(m));
                    alpha_i.set(alpha_index_i.get(m), alpha_new[m]);
                    if (!(Math.abs(d) >= 1.0E-12)) continue;
                    d_ind[nz_d] = alpha_index_i.get(m);
                    d_val[nz_d] = d;
                    ++nz_d;
                }
                for (FeatureNode xi : this.prob.x[i]) {
                    int w_offset = (xi.index - 1) * this.nr_class;
                    for (m = 0; m < nz_d; ++m) {
                        double[] arrd = w;
                        int n = w_offset + d_ind[m];
                        arrd[n] = arrd[n] + d_val[m] * xi.value;
                    }
                }
            }
            if (++iter % 10 == 0) {
                Linear.info(".");
            }
            if (stopping < eps_shrink) {
                if (stopping < this.eps && start_from_all) break;
                active_size = this.l;
                for (i = 0; i < this.l; ++i) {
                    active_size_i[i] = this.nr_class;
                }
                Linear.info("*");
                eps_shrink = Math.max(eps_shrink / 2.0, this.eps);
                start_from_all = true;
                continue;
            }
            start_from_all = false;
        }
        Linear.info("%noptimization finished, #iter = %d%n", iter);
        if (iter >= this.max_iter) {
            Linear.info("%nWARNING: reaching max number of iterations%n");
        }
        double v = 0.0;
        int nSV = 0;
        for (i = 0; i < this.w_size * this.nr_class; ++i) {
            v += w[i] * w[i];
        }
        v = 0.5 * v;
        for (i = 0; i < this.l * this.nr_class; ++i) {
            v += alpha[i];
            if (!(Math.abs(alpha[i]) > 0.0)) continue;
            ++nSV;
        }
        for (i = 0; i < this.l; ++i) {
            v -= alpha[i * this.nr_class + this.prob.y[i]];
        }
        Linear.info("Objective value = %f%n", v);
        Linear.info("nSV = %d%n", nSV);
    }

    private void solve_sub_problem(double A_i, int yi, double C_yi, int active_i, double[] alpha_new) {
        int r;
        assert (active_i <= this.B.length);
        double[] D = Linear.copyOf(this.B, active_i);
        if (yi < active_i) {
            double[] arrd = D;
            int n = yi;
            arrd[n] = arrd[n] + A_i * C_yi;
        }
        ArraySorter.reversedMergesort(D);
        double beta = D[0] - A_i * C_yi;
        for (r = 1; r < active_i && beta < (double)r * D[r]; beta += D[r], ++r) {
        }
        beta /= (double)r;
        for (r = 0; r < active_i; ++r) {
            alpha_new[r] = r == yi ? Math.min(C_yi, (beta - this.B[r]) / A_i) : Math.min(0.0, (beta - this.B[r]) / A_i);
        }
    }
}

