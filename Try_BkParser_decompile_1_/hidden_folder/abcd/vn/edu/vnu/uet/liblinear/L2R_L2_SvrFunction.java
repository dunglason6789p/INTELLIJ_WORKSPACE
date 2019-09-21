/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import vn.edu.vnu.uet.liblinear.L2R_L2_SvcFunction;
import vn.edu.vnu.uet.liblinear.Problem;

public class L2R_L2_SvrFunction
extends L2R_L2_SvcFunction {
    private double p;

    public L2R_L2_SvrFunction(Problem prob, double[] C, double p) {
        super(prob, C);
        this.p = p;
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
            double d = this.z[i] - y[i];
            if (d < -this.p) {
                f += this.C[i] * (d + this.p) * (d + this.p);
                continue;
            }
            if (!(d > this.p)) continue;
            f += this.C[i] * (d - this.p) * (d - this.p);
        }
        return f;
    }

    @Override
    public void grad(double[] w, double[] g) {
        int i;
        double[] y = this.prob.y;
        int l = this.prob.l;
        int w_size = this.get_nr_variable();
        this.sizeI = 0;
        for (i = 0; i < l; ++i) {
            double d = this.z[i] - y[i];
            if (d < -this.p) {
                this.z[this.sizeI] = this.C[i] * (d + this.p);
                this.I[this.sizeI] = i;
                ++this.sizeI;
                continue;
            }
            if (!(d > this.p)) continue;
            this.z[this.sizeI] = this.C[i] * (d - this.p);
            this.I[this.sizeI] = i;
            ++this.sizeI;
        }
        this.subXTv(this.z, g);
        for (i = 0; i < w_size; ++i) {
            g[i] = w[i] + 2.0 * g[i];
        }
    }
}

