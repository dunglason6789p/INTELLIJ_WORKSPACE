/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import libsvm.QMatrix;
import libsvm.svm;

class Solver {
    int active_size;
    byte[] y;
    double[] G;
    static final byte LOWER_BOUND = 0;
    static final byte UPPER_BOUND = 1;
    static final byte FREE = 2;
    byte[] alpha_status;
    double[] alpha;
    QMatrix Q;
    double[] QD;
    double eps;
    double Cp;
    double Cn;
    double[] p;
    int[] active_set;
    double[] G_bar;
    int l;
    boolean unshrink;
    static final double INF = Double.POSITIVE_INFINITY;

    Solver() {
    }

    double get_C(int i) {
        return this.y[i] > 0 ? this.Cp : this.Cn;
    }

    void update_alpha_status(int i) {
        this.alpha_status[i] = this.alpha[i] >= this.get_C(i) ? 1 : (this.alpha[i] <= 0.0 ? 0 : 2);
    }

    boolean is_upper_bound(int i) {
        return this.alpha_status[i] == 1;
    }

    boolean is_lower_bound(int i) {
        return this.alpha_status[i] == 0;
    }

    boolean is_free(int i) {
        return this.alpha_status[i] == 2;
    }

    void swap_index(int i, int j) {
        this.Q.swap_index(i, j);
        byte _ = this.y[i];
        this.y[i] = this.y[j];
        this.y[j] = _;
        double _2 = this.G[i];
        this.G[i] = this.G[j];
        this.G[j] = _2;
        _ = this.alpha_status[i];
        this.alpha_status[i] = this.alpha_status[j];
        this.alpha_status[j] = _;
        double _3 = this.alpha[i];
        this.alpha[i] = this.alpha[j];
        this.alpha[j] = _3;
        _3 = this.p[i];
        this.p[i] = this.p[j];
        this.p[j] = _3;
        int _4 = this.active_set[i];
        this.active_set[i] = this.active_set[j];
        this.active_set[j] = _4;
        double _5 = this.G_bar[i];
        this.G_bar[i] = this.G_bar[j];
        this.G_bar[j] = _5;
    }

    void reconstruct_gradient() {
        int j;
        if (this.active_size == this.l) {
            return;
        }
        int nr_free = 0;
        for (j = this.active_size; j < this.l; ++j) {
            this.G[j] = this.G_bar[j] + this.p[j];
        }
        for (j = 0; j < this.active_size; ++j) {
            if (!this.is_free(j)) continue;
            ++nr_free;
        }
        if (2 * nr_free < this.active_size) {
            svm.info("\nWarning: using -h 0 may be faster\n");
        }
        if (nr_free * this.l > 2 * this.active_size * (this.l - this.active_size)) {
            for (int i = this.active_size; i < this.l; ++i) {
                float[] Q_i = this.Q.get_Q(i, this.active_size);
                for (j = 0; j < this.active_size; ++j) {
                    if (!this.is_free(j)) continue;
                    double[] arrd = this.G;
                    int n = i;
                    arrd[n] = arrd[n] + this.alpha[j] * (double)Q_i[j];
                }
            }
        } else {
            for (int i = 0; i < this.active_size; ++i) {
                if (!this.is_free(i)) continue;
                float[] Q_i = this.Q.get_Q(i, this.l);
                double alpha_i = this.alpha[i];
                for (j = this.active_size; j < this.l; ++j) {
                    double[] arrd = this.G;
                    int n = j;
                    arrd[n] = arrd[n] + alpha_i * (double)Q_i[j];
                }
            }
        }
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    void Solve(int l, QMatrix Q, double[] p_, byte[] y_, double[] alpha_, double Cp, double Cn, double eps, SolutionInfo si, int shrinking) {
        this.l = l;
        this.Q = Q;
        this.QD = Q.get_QD();
        this.p = (double[])p_.clone();
        this.y = (byte[])y_.clone();
        this.alpha = (double[])alpha_.clone();
        this.Cp = Cp;
        this.Cn = Cn;
        this.eps = eps;
        this.unshrink = false;
        this.alpha_status = new byte[l];
        for (i = 0; i < l; ++i) {
            this.update_alpha_status(i);
        }
        this.active_set = new int[l];
        for (i = 0; i < l; ++i) {
            this.active_set[i] = i;
        }
        this.active_size = l;
        this.G = new double[l];
        this.G_bar = new double[l];
        for (i = 0; i < l; ++i) {
            this.G[i] = this.p[i];
            this.G_bar[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            if (this.is_lower_bound(i)) continue;
            Q_i = Q.get_Q(i, l);
            alpha_i = this.alpha[i];
            for (j = 0; j < l; ++j) {
                v0 = this.G;
                v1 = j;
                v0[v1] = v0[v1] + alpha_i * (double)Q_i[j];
            }
            if (!this.is_upper_bound(i)) continue;
            for (j = 0; j < l; ++j) {
                v2 = this.G_bar;
                v3 = j;
                v2[v3] = v2[v3] + this.get_C(i) * (double)Q_i[j];
            }
        }
        iter = 0;
        counter = Math.min(l, 1000) + 1;
        working_set = new int[2];
        block6 : do lbl-1000: // 4 sources:
        {
            block42 : {
                if (--counter == 0) {
                    counter = Math.min(l, 1000);
                    if (shrinking != 0) {
                        this.do_shrinking();
                    }
                    svm.info(".");
                }
                if (this.select_working_set(working_set) != 0) {
                    this.reconstruct_gradient();
                    this.active_size = l;
                    svm.info("*");
                    if (this.select_working_set(working_set) != 0) {
                        si.rho = this.calculate_rho();
                        v = 0.0;
                        break;
                    }
                    counter = 1;
                }
                i = working_set[0];
                j = working_set[1];
                ++iter;
                Q_i = Q.get_Q(i, this.active_size);
                Q_j = Q.get_Q(j, this.active_size);
                C_i = this.get_C(i);
                C_j = this.get_C(j);
                old_alpha_i = this.alpha[i];
                old_alpha_j = this.alpha[j];
                if (this.y[i] != this.y[j]) {
                    quad_coef = this.QD[i] + this.QD[j] + (double)(2.0f * Q_i[j]);
                    if (quad_coef <= 0.0) {
                        quad_coef = 1.0E-12;
                    }
                    delta = (-this.G[i] - this.G[j]) / quad_coef;
                    diff = this.alpha[i] - this.alpha[j];
                    v4 = this.alpha;
                    v5 = i;
                    v4[v5] = v4[v5] + delta;
                    v6 = this.alpha;
                    v7 = j;
                    v6[v7] = v6[v7] + delta;
                    if (diff > 0.0) {
                        if (this.alpha[j] < 0.0) {
                            this.alpha[j] = 0.0;
                            this.alpha[i] = diff;
                        }
                    } else if (this.alpha[i] < 0.0) {
                        this.alpha[i] = 0.0;
                        this.alpha[j] = -diff;
                    }
                    if (diff > C_i - C_j) {
                        if (this.alpha[i] > C_i) {
                            this.alpha[i] = C_i;
                            this.alpha[j] = C_i - diff;
                        }
                    } else if (this.alpha[j] > C_j) {
                        this.alpha[j] = C_j;
                        this.alpha[i] = C_j + diff;
                    }
                } else {
                    quad_coef = this.QD[i] + this.QD[j] - (double)(2.0f * Q_i[j]);
                    if (quad_coef <= 0.0) {
                        quad_coef = 1.0E-12;
                    }
                    delta = (this.G[i] - this.G[j]) / quad_coef;
                    sum = this.alpha[i] + this.alpha[j];
                    v8 = this.alpha;
                    v9 = i;
                    v8[v9] = v8[v9] - delta;
                    v10 = this.alpha;
                    v11 = j;
                    v10[v11] = v10[v11] + delta;
                    if (sum > C_i) {
                        if (this.alpha[i] > C_i) {
                            this.alpha[i] = C_i;
                            this.alpha[j] = sum - C_i;
                        }
                    } else if (this.alpha[j] < 0.0) {
                        this.alpha[j] = 0.0;
                        this.alpha[i] = sum;
                    }
                    if (sum > C_j) {
                        if (this.alpha[j] > C_j) {
                            this.alpha[j] = C_j;
                            this.alpha[i] = sum - C_j;
                        }
                    } else if (this.alpha[i] < 0.0) {
                        this.alpha[i] = 0.0;
                        this.alpha[j] = sum;
                    }
                }
                delta_alpha_i = this.alpha[i] - old_alpha_i;
                delta_alpha_j = this.alpha[j] - old_alpha_j;
                for (k = 0; k < this.active_size; ++k) {
                    v12 = this.G;
                    v13 = k;
                    v12[v13] = v12[v13] + ((double)Q_i[k] * delta_alpha_i + (double)Q_j[k] * delta_alpha_j);
                }
                ui = this.is_upper_bound(i);
                uj = this.is_upper_bound(j);
                this.update_alpha_status(i);
                this.update_alpha_status(j);
                if (ui != this.is_upper_bound(i)) {
                    Q_i = Q.get_Q(i, l);
                    if (ui) {
                        for (k = 0; k < l; ++k) {
                            v14 = this.G_bar;
                            v15 = k;
                            v14[v15] = v14[v15] - C_i * (double)Q_i[k];
                        }
                    } else {
                        for (k = 0; k < l; ++k) {
                            v16 = this.G_bar;
                            v17 = k;
                            v16[v17] = v16[v17] + C_i * (double)Q_i[k];
                        }
                    }
                }
                if (uj == this.is_upper_bound(j)) ** GOTO lbl-1000
                Q_j = Q.get_Q(j, l);
                if (!uj) break block42;
                k = 0;
                do {
                    if (k >= l) ** GOTO lbl-1000
                    v18 = this.G_bar;
                    v19 = k;
                    v18[v19] = v18[v19] - C_j * (double)Q_j[k];
                    ++k;
                } while (true);
            }
            k = 0;
            do {
                if (k >= l) continue block6;
                v20 = this.G_bar;
                v21 = k;
                v20[v21] = v20[v21] + C_j * (double)Q_j[k];
                ++k;
            } while (true);
            break;
        } while (true);
        for (i = 0; i < l; v += this.alpha[i] * (this.G[i] + this.p[i]), ++i) {
        }
        si.obj = v / 2.0;
        i = 0;
        do {
            if (i >= l) {
                si.upper_bound_p = Cp;
                si.upper_bound_n = Cn;
                svm.info("\noptimization finished, #iter = " + iter + "\n");
                return;
            }
            alpha_[this.active_set[i]] = this.alpha[i];
            ++i;
        } while (true);
    }

    int select_working_set(int[] working_set) {
        double Gmax = Double.NEGATIVE_INFINITY;
        double Gmax2 = Double.NEGATIVE_INFINITY;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = Double.POSITIVE_INFINITY;
        for (int t = 0; t < this.active_size; ++t) {
            if (this.y[t] == 1) {
                if (this.is_upper_bound(t) || !(-this.G[t] >= Gmax)) continue;
                Gmax = -this.G[t];
                Gmax_idx = t;
                continue;
            }
            if (this.is_lower_bound(t) || !(this.G[t] >= Gmax)) continue;
            Gmax = this.G[t];
            Gmax_idx = t;
        }
        int i = Gmax_idx;
        float[] Q_i = null;
        if (i != -1) {
            Q_i = this.Q.get_Q(i, this.active_size);
        }
        for (int j = 0; j < this.active_size; ++j) {
            double quad_coef;
            double grad_diff;
            double obj_diff;
            if (this.y[j] == 1) {
                if (this.is_lower_bound(j)) continue;
                grad_diff = Gmax + this.G[j];
                if (this.G[j] >= Gmax2) {
                    Gmax2 = this.G[j];
                }
                if (!(grad_diff > 0.0) || !((obj_diff = (quad_coef = this.QD[i] + this.QD[j] - 2.0 * (double)this.y[i] * (double)Q_i[j]) > 0.0 ? -(grad_diff * grad_diff) / quad_coef : -(grad_diff * grad_diff) / 1.0E-12) <= obj_diff_min)) continue;
                Gmin_idx = j;
                obj_diff_min = obj_diff;
                continue;
            }
            if (this.is_upper_bound(j)) continue;
            grad_diff = Gmax - this.G[j];
            if (-this.G[j] >= Gmax2) {
                Gmax2 = -this.G[j];
            }
            if (!(grad_diff > 0.0) || !((obj_diff = (quad_coef = this.QD[i] + this.QD[j] + 2.0 * (double)this.y[i] * (double)Q_i[j]) > 0.0 ? -(grad_diff * grad_diff) / quad_coef : -(grad_diff * grad_diff) / 1.0E-12) <= obj_diff_min)) continue;
            Gmin_idx = j;
            obj_diff_min = obj_diff;
        }
        if (Gmax + Gmax2 < this.eps) {
            return 1;
        }
        working_set[0] = Gmax_idx;
        working_set[1] = Gmin_idx;
        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2) {
        if (this.is_upper_bound(i)) {
            if (this.y[i] == 1) {
                return -this.G[i] > Gmax1;
            }
            return -this.G[i] > Gmax2;
        }
        if (this.is_lower_bound(i)) {
            if (this.y[i] == 1) {
                return this.G[i] > Gmax2;
            }
            return this.G[i] > Gmax1;
        }
        return false;
    }

    void do_shrinking() {
        int i;
        double Gmax1 = Double.NEGATIVE_INFINITY;
        double Gmax2 = Double.NEGATIVE_INFINITY;
        for (i = 0; i < this.active_size; ++i) {
            if (this.y[i] == 1) {
                if (!this.is_upper_bound(i) && -this.G[i] >= Gmax1) {
                    Gmax1 = -this.G[i];
                }
                if (this.is_lower_bound(i) || !(this.G[i] >= Gmax2)) continue;
                Gmax2 = this.G[i];
                continue;
            }
            if (!this.is_upper_bound(i) && -this.G[i] >= Gmax2) {
                Gmax2 = -this.G[i];
            }
            if (this.is_lower_bound(i) || !(this.G[i] >= Gmax1)) continue;
            Gmax1 = this.G[i];
        }
        if (!this.unshrink && Gmax1 + Gmax2 <= this.eps * 10.0) {
            this.unshrink = true;
            this.reconstruct_gradient();
            this.active_size = this.l;
        }
        block1 : for (i = 0; i < this.active_size; ++i) {
            if (!this.be_shrunk(i, Gmax1, Gmax2)) continue;
            --this.active_size;
            while (this.active_size > i) {
                if (!this.be_shrunk(this.active_size, Gmax1, Gmax2)) {
                    this.swap_index(i, this.active_size);
                    continue block1;
                }
                --this.active_size;
            }
        }
    }

    double calculate_rho() {
        int nr_free = 0;
        double ub = Double.POSITIVE_INFINITY;
        double lb = Double.NEGATIVE_INFINITY;
        double sum_free = 0.0;
        for (int i = 0; i < this.active_size; ++i) {
            double yG = (double)this.y[i] * this.G[i];
            if (this.is_lower_bound(i)) {
                if (this.y[i] > 0) {
                    ub = Math.min(ub, yG);
                    continue;
                }
                lb = Math.max(lb, yG);
                continue;
            }
            if (this.is_upper_bound(i)) {
                if (this.y[i] < 0) {
                    ub = Math.min(ub, yG);
                    continue;
                }
                lb = Math.max(lb, yG);
                continue;
            }
            ++nr_free;
            sum_free += yG;
        }
        double r = nr_free > 0 ? sum_free / (double)nr_free : (ub + lb) / 2.0;
        return r;
    }

    static class SolutionInfo {
        double obj;
        double rho;
        double upper_bound_p;
        double upper_bound_n;
        double r;

        SolutionInfo() {
        }
    }

}

