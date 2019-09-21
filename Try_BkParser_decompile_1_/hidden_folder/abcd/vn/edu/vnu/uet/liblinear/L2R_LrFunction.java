/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.Function;
import vn.edu.vnu.uet.liblinear.Problem;

class L2R_LrFunction
implements Function {
    private final double[] C;
    private final double[] z;
    private final double[] D;
    private final Problem prob;

    public L2R_LrFunction(Problem prob, double[] C) {
        int l = prob.l;
        this.prob = prob;
        this.z = new double[l];
        this.D = new double[l];
        this.C = C;
    }

    private void Xv(double[] v, double[] Xv) {
        for (int i = 0; i < this.prob.l; ++i) {
            Xv[i] = 0.0;
            for (Feature s : this.prob.x[i]) {
                double[] arrd = Xv;
                int n = i;
                arrd[n] = arrd[n] + v[s.getIndex() - 1] * s.getValue();
            }
        }
    }

    private void XTv(double[] v, double[] XTv) {
        int i;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        Feature[][] x = this.prob.x;
        for (i = 0; i < w_size; ++i) {
            XTv[i] = 0.0;
        }
        for (i = 0; i < l; ++i) {
            for (Feature s : x[i]) {
                double[] arrd = XTv;
                int n = s.getIndex() - 1;
                arrd[n] = arrd[n] + v[i] * s.getValue();
            }
        }
    }

    @Override
    public double fun(double[] w) {
        int i;
        double f = 0.0;
        double[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        this.Xv(w, this.z);
        for (i = 0; i < w_size; ++i) {
            f += w[i] * w[i];
        }
        f /= 2.0;
        for (i = 0; i < l; ++i) {
            double yz = y[i] * this.z[i];
            if (yz >= 0.0) {
                f += this.C[i] * Math.log(1.0 + Math.exp(-yz));
                continue;
            }
            f += this.C[i] * (-yz + Math.log(1.0 + Math.exp(yz)));
        }
        return f;
    }

    @Override
    public void grad(double[] w, double[] g) {
        int i;
        double[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        for (i = 0; i < l; ++i) {
            this.z[i] = 1.0 / (1.0 + Math.exp(-y[i] * this.z[i]));
            this.D[i] = this.z[i] * (1.0 - this.z[i]);
            this.z[i] = this.C[i] * (this.z[i] - 1.0) * y[i];
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

