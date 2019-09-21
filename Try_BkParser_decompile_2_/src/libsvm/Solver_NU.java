/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import libsvm.QMatrix;
import libsvm.Solver;

final class Solver_NU
extends Solver {
    private Solver.SolutionInfo si;

    Solver_NU() {
    }

    void Solve(int l, QMatrix Q, double[] p, byte[] y, double[] alpha, double Cp, double Cn, double eps, Solver.SolutionInfo si, int shrinking) {
        this.si = si;
        super.Solve(l, Q, p, y, alpha, Cp, Cn, eps, si, shrinking);
    }

    int select_working_set(int[] working_set) {
        double Gmaxp = Double.NEGATIVE_INFINITY;
        double Gmaxp2 = Double.NEGATIVE_INFINITY;
        int Gmaxp_idx = -1;
        double Gmaxn = Double.NEGATIVE_INFINITY;
        double Gmaxn2 = Double.NEGATIVE_INFINITY;
        int Gmaxn_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = Double.POSITIVE_INFINITY;
        for (int t = 0; t < this.active_size; ++t) {
            if (this.y[t] == 1) {
                if (this.is_upper_bound(t) || !(-this.G[t] >= Gmaxp)) continue;
                Gmaxp = -this.G[t];
                Gmaxp_idx = t;
                continue;
            }
            if (this.is_lower_bound(t) || !(this.G[t] >= Gmaxn)) continue;
            Gmaxn = this.G[t];
            Gmaxn_idx = t;
        }
        int ip = Gmaxp_idx;
        int in = Gmaxn_idx;
        float[] Q_ip = null;
        float[] Q_in = null;
        if (ip != -1) {
            Q_ip = this.Q.get_Q(ip, this.active_size);
        }
        if (in != -1) {
            Q_in = this.Q.get_Q(in, this.active_size);
        }
        for (int j = 0; j < this.active_size; ++j) {
            double obj_diff;
            double grad_diff;
            double quad_coef;
            if (this.y[j] == 1) {
                if (this.is_lower_bound(j)) continue;
                grad_diff = Gmaxp + this.G[j];
                if (this.G[j] >= Gmaxp2) {
                    Gmaxp2 = this.G[j];
                }
                if (!(grad_diff > 0.0) || !((obj_diff = (quad_coef = this.QD[ip] + this.QD[j] - (double)(2.0f * Q_ip[j])) > 0.0 ? -(grad_diff * grad_diff) / quad_coef : -(grad_diff * grad_diff) / 1.0E-12) <= obj_diff_min)) continue;
                Gmin_idx = j;
                obj_diff_min = obj_diff;
                continue;
            }
            if (this.is_upper_bound(j)) continue;
            grad_diff = Gmaxn - this.G[j];
            if (-this.G[j] >= Gmaxn2) {
                Gmaxn2 = -this.G[j];
            }
            if (!(grad_diff > 0.0) || !((obj_diff = (quad_coef = this.QD[in] + this.QD[j] - (double)(2.0f * Q_in[j])) > 0.0 ? -(grad_diff * grad_diff) / quad_coef : -(grad_diff * grad_diff) / 1.0E-12) <= obj_diff_min)) continue;
            Gmin_idx = j;
            obj_diff_min = obj_diff;
        }
        if (Math.max(Gmaxp + Gmaxp2, Gmaxn + Gmaxn2) < this.eps) {
            return 1;
        }
        working_set[0] = this.y[Gmin_idx] == 1 ? Gmaxp_idx : Gmaxn_idx;
        working_set[1] = Gmin_idx;
        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2, double Gmax3, double Gmax4) {
        if (this.is_upper_bound(i)) {
            if (this.y[i] == 1) {
                return -this.G[i] > Gmax1;
            }
            return -this.G[i] > Gmax4;
        }
        if (this.is_lower_bound(i)) {
            if (this.y[i] == 1) {
                return this.G[i] > Gmax2;
            }
            return this.G[i] > Gmax3;
        }
        return false;
    }

    void do_shrinking() {
        int i;
        double Gmax1 = Double.NEGATIVE_INFINITY;
        double Gmax2 = Double.NEGATIVE_INFINITY;
        double Gmax3 = Double.NEGATIVE_INFINITY;
        double Gmax4 = Double.NEGATIVE_INFINITY;
        for (i = 0; i < this.active_size; ++i) {
            if (!this.is_upper_bound(i)) {
                if (this.y[i] == 1) {
                    if (-this.G[i] > Gmax1) {
                        Gmax1 = -this.G[i];
                    }
                } else if (-this.G[i] > Gmax4) {
                    Gmax4 = -this.G[i];
                }
            }
            if (this.is_lower_bound(i)) continue;
            if (this.y[i] == 1) {
                if (!(this.G[i] > Gmax2)) continue;
                Gmax2 = this.G[i];
                continue;
            }
            if (!(this.G[i] > Gmax3)) continue;
            Gmax3 = this.G[i];
        }
        if (!this.unshrink && Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= this.eps * 10.0) {
            this.unshrink = true;
            this.reconstruct_gradient();
            this.active_size = this.l;
        }
        block1 : for (i = 0; i < this.active_size; ++i) {
            if (!this.be_shrunk(i, Gmax1, Gmax2, Gmax3, Gmax4)) continue;
            --this.active_size;
            while (this.active_size > i) {
                if (!this.be_shrunk(this.active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                    this.swap_index(i, this.active_size);
                    continue block1;
                }
                --this.active_size;
            }
        }
    }

    double calculate_rho() {
        int nr_free1 = 0;
        int nr_free2 = 0;
        double ub1 = Double.POSITIVE_INFINITY;
        double ub2 = Double.POSITIVE_INFINITY;
        double lb1 = Double.NEGATIVE_INFINITY;
        double lb2 = Double.NEGATIVE_INFINITY;
        double sum_free1 = 0.0;
        double sum_free2 = 0.0;
        for (int i = 0; i < this.active_size; ++i) {
            if (this.y[i] == 1) {
                if (this.is_lower_bound(i)) {
                    ub1 = Math.min(ub1, this.G[i]);
                    continue;
                }
                if (this.is_upper_bound(i)) {
                    lb1 = Math.max(lb1, this.G[i]);
                    continue;
                }
                ++nr_free1;
                sum_free1 += this.G[i];
                continue;
            }
            if (this.is_lower_bound(i)) {
                ub2 = Math.min(ub2, this.G[i]);
                continue;
            }
            if (this.is_upper_bound(i)) {
                lb2 = Math.max(lb2, this.G[i]);
                continue;
            }
            ++nr_free2;
            sum_free2 += this.G[i];
        }
        double r1 = nr_free1 > 0 ? sum_free1 / (double)nr_free1 : (ub1 + lb1) / 2.0;
        double r2 = nr_free2 > 0 ? sum_free2 / (double)nr_free2 : (ub2 + lb2) / 2.0;
        this.si.r = (r1 + r2) / 2.0;
        return (r1 - r2) / 2.0;
    }
}

