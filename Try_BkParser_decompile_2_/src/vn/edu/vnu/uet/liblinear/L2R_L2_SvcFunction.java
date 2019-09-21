/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.Function;
import vn.edu.vnu.uet.liblinear.Problem;

class L2R_L2_SvcFunction
implements Function {
    protected final Problem prob;
    protected final double[] C;
    protected final int[] I;
    protected final double[] z;
    protected int sizeI;

    public L2R_L2_SvcFunction(Problem prob, double[] C) {
        int l = prob.l;
        this.prob = prob;
        this.z = new double[l];
        this.I = new int[l];
        this.C = C;
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
            this.z[i] = y[i] * this.z[i];
            double d = 1.0 - this.z[i];
            if (!(d > 0.0)) continue;
            f += this.C[i] * d * d;
        }
        return f;
    }

    @Override
    public int get_nr_variable() {
        return this.prob.n;
    }

    @Override
    public void grad(double[] w, double[] g) {
        int i;
        double[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        this.sizeI = 0;
        for (i = 0; i < l; ++i) {
            if (!(this.z[i] < 1.0)) continue;
            this.z[this.sizeI] = this.C[i] * y[i] * (this.z[i] - 1.0);
            this.I[this.sizeI] = i;
            ++this.sizeI;
        }
        this.subXTv(this.z, g);
        for (i = 0; i < w_size; ++i) {
            g[i] = w[i] + 2.0 * g[i];
        }
    }

    @Override
    public void Hv(double[] s, double[] Hs) {
        int i;
        int w_size = this.get_nr_variable();
        double[] wa = new double[this.sizeI];
        this.subXv(s, wa);
        for (i = 0; i < this.sizeI; ++i) {
            wa[i] = this.C[this.I[i]] * wa[i];
        }
        this.subXTv(wa, Hs);
        for (i = 0; i < w_size; ++i) {
            Hs[i] = s[i] + 2.0 * Hs[i];
        }
    }

    protected void subXTv(double[] v, double[] XTv) {
        int i;
        int w_size = this.get_nr_variable();
        for (i = 0; i < w_size; ++i) {
            XTv[i] = 0.0;
        }
        for (i = 0; i < this.sizeI; ++i) {
            for (Feature s : this.prob.x[this.I[i]]) {
                double[] arrd = XTv;
                int n = s.getIndex() - 1;
                arrd[n] = arrd[n] + v[i] * s.getValue();
            }
        }
    }

    private void subXv(double[] v, double[] Xv) {
        for (int i = 0; i < this.sizeI; ++i) {
            Xv[i] = 0.0;
            for (Feature s : this.prob.x[this.I[i]]) {
                double[] arrd = Xv;
                int n = i;
                arrd[n] = arrd[n] + v[s.getIndex() - 1] * s.getValue();
            }
        }
    }

    protected void Xv(double[] v, double[] Xv) {
        for (int i = 0; i < this.prob.l; ++i) {
            Xv[i] = 0.0;
            for (Feature s : this.prob.x[i]) {
                double[] arrd = Xv;
                int n = i;
                arrd[n] = arrd[n] + v[s.getIndex() - 1] * s.getValue();
            }
        }
    }
}

