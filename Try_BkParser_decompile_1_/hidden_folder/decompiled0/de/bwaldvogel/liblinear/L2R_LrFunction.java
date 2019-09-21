/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Function;
import de.bwaldvogel.liblinear.Problem;

class L2R_LrFunction
implements Function {
    private final double[] C;
    private final double[] z;
    private final double[] D;
    private final Problem prob;

    public L2R_LrFunction(Problem prob, double Cp, double Cn) {
        int l = prob.l;
        int[] y = prob.y;
        this.prob = prob;
        this.z = new double[l];
        this.D = new double[l];
        this.C = new double[l];
        for (int i = 0; i < l; ++i) {
            this.C[i] = y[i] == 1 ? Cp : Cn;
        }
    }

    private void Xv(double[] v, double[] Xv) {
        for (int i = 0; i < this.prob.l; ++i) {
            Xv[i] = 0.0;
            for (FeatureNode s : this.prob.x[i]) {
                double[] arrd = Xv;
                int n = i;
                arrd[n] = arrd[n] + v[s.index - 1] * s.value;
            }
        }
    }

    private void XTv(double[] v, double[] XTv) {
        int i;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        FeatureNode[][] x = this.prob.x;
        for (i = 0; i < w_size; ++i) {
            XTv[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            for (FeatureNode s : x[i]) {
                double[] arrd = XTv;
                int n = s.index - 1;
                arrd[n] = arrd[n] + v[i] * s.value;
            }
        }
    }

    @Override
    public double fun(double[] w) {
        int i;
        double f = 0.0;
        int[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        this.Xv(w, this.z);
        for (i = 0; i < l; ++i) {
            double yz = (double)y[i] * this.z[i];
            if (yz >= 0.0) {
                f += this.C[i] * Math.log(1.0 + Math.exp(-yz));
                continue;
            }
            f += this.C[i] * (-yz + Math.log(1.0 + Math.exp(yz)));
        }
        f = 2.0 * f;
        for (i = 0; i < w_size; ++i) {
            f += w[i] * w[i];
        }
        return f /= 2.0;
    }

    @Override
    public void grad(double[] w, double[] g) {
        int i;
        int[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        for (i = 0; i < l; ++i) {
            this.z[i] = 1.0 / (1.0 + Math.exp((double)(-y[i]) * this.z[i]));
            this.D[i] = this.z[i] * (1.0 - this.z[i]);
            this.z[i] = this.C[i] * (this.z[i] - 1.0) * (double)y[i];
        }
        this.XTv(this.z, g);
        for (i = 0; i < w_size; ++i) {
            g[i] = w[i] + g[i];
        }
    }

    @Override
    public void Hv(double[] s, double[] Hs) {
        int i;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        double[] wa = new double[l];
        this.Xv(s, wa);
        for (i = 0; i < l; ++i) {
            wa[i] = this.C[i] * this.D[i] * wa[i];
        }
        this.XTv(wa, Hs);
        for (i = 0; i < w_size; ++i) {
            Hs[i] = s[i] + Hs[i];
        }
    }

    @Override
    public int get_nr_variable() {
        return this.prob.n;
    }
}

