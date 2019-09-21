/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import vn.edu.vnu.uet.liblinear.Function;
import vn.edu.vnu.uet.liblinear.Linear;

class Tron {
    private final Function fun_obj;
    private final double eps;
    private final int max_iter;

    public Tron(Function fun_obj) {
        this(fun_obj, 0.1);
    }

    public Tron(Function fun_obj, double eps) {
        this(fun_obj, eps, 1000);
    }

    public Tron(Function fun_obj, double eps, int max_iter) {
        this.fun_obj = fun_obj;
        this.eps = eps;
        this.max_iter = max_iter;
    }

    void tron(double[] w) {
        double delta;
        double gnorm1;
        double eta0 = 1.0E-4;
        double eta1 = 0.25;
        double eta2 = 0.75;
        double sigma1 = 0.25;
        double sigma2 = 0.5;
        double sigma3 = 4.0;
        int n = this.fun_obj.get_nr_variable();
        double one = 1.0;
        boolean search = true;
        int iter = 1;
        double[] s = new double[n];
        double[] r = new double[n];
        double[] w_new = new double[n];
        double[] g = new double[n];
        for (int i = 0; i < n; ++i) {
            w[i] = 0.0;
        }
        double f = this.fun_obj.fun(w);
        this.fun_obj.grad(w, g);
        double gnorm = gnorm1 = (delta = Tron.euclideanNorm(g));
        if (gnorm <= this.eps * gnorm1) {
            search = false;
        }
        iter = 1;
        while (iter <= this.max_iter && search) {
            int cg_iter = this.trcg(delta, g, s, r);
            System.arraycopy(w, 0, w_new, 0, n);
            Tron.daxpy(one, s, w_new);
            double gs = Tron.dot(g, s);
            double prered = -0.5 * (gs - Tron.dot(s, r));
            double fnew = this.fun_obj.fun(w_new);
            double actred = f - fnew;
            double snorm = Tron.euclideanNorm(s);
            if (iter == 1) {
                delta = Math.min(delta, snorm);
            }
            double alpha = fnew - f - gs <= 0.0 ? sigma3 : Math.max(sigma1, -0.5 * (gs / (fnew - f - gs)));
            delta = actred < eta0 * prered ? Math.min(Math.max(alpha, sigma1) * snorm, sigma2 * delta) : (actred < eta1 * prered ? Math.max(sigma1 * delta, Math.min(alpha * snorm, sigma2 * delta)) : (actred < eta2 * prered ? Math.max(sigma1 * delta, Math.min(alpha * snorm, sigma3 * delta)) : Math.max(delta, Math.min(alpha * snorm, sigma3 * delta))));
            Linear.info("iter %2d act %5.3e pre %5.3e delta %5.3e f %5.3e |g| %5.3e CG %3d%n", iter, actred, prered, delta, f, gnorm, cg_iter);
            if (actred > eta0 * prered) {
                ++iter;
                System.arraycopy(w_new, 0, w, 0, n);
                f = fnew;
                this.fun_obj.grad(w, g);
                gnorm = Tron.euclideanNorm(g);
                if (gnorm <= this.eps * gnorm1) break;
            }
            if (f < -1.0E32) {
                Linear.info("WARNING: f < -1.0e+32%n");
                break;
            }
            if (Math.abs(actred) <= 0.0 && prered <= 0.0) {
                Linear.info("WARNING: actred and prered <= 0%n");
                break;
            }
            if (!(Math.abs(actred) <= 1.0E-12 * Math.abs(f)) || !(Math.abs(prered) <= 1.0E-12 * Math.abs(f))) continue;
            Linear.info("WARNING: actred and prered too small%n");
            break;
        }
    }

    private int trcg(double delta, double[] g, double[] s, double[] r) {
        int n = this.fun_obj.get_nr_variable();
        double one = 1.0;
        double[] d = new double[n];
        double[] Hd = new double[n];
        for (int i = 0; i < n; ++i) {
            s[i] = 0.0;
            r[i] = -g[i];
            d[i] = r[i];
        }
        double cgtol = 0.1 * Tron.euclideanNorm(g);
        int cg_iter = 0;
        double rTr = Tron.dot(r, r);
        while (!(Tron.euclideanNorm(r) <= cgtol)) {
            ++cg_iter;
            this.fun_obj.Hv(d, Hd);
            double alpha = rTr / Tron.dot(d, Hd);
            Tron.daxpy(alpha, d, s);
            if (Tron.euclideanNorm(s) > delta) {
                Linear.info("cg reaches trust region boundary%n");
                alpha = -alpha;
                Tron.daxpy(alpha, d, s);
                double std = Tron.dot(s, d);
                double sts = Tron.dot(s, s);
                double dtd = Tron.dot(d, d);
                double dsq = delta * delta;
                double rad = Math.sqrt(std * std + dtd * (dsq - sts));
                alpha = std >= 0.0 ? (dsq - sts) / (std + rad) : (rad - std) / dtd;
                Tron.daxpy(alpha, d, s);
                alpha = -alpha;
                Tron.daxpy(alpha, Hd, r);
                break;
            }
            alpha = -alpha;
            Tron.daxpy(alpha, Hd, r);
            double rnewTrnew = Tron.dot(r, r);
            double beta = rnewTrnew / rTr;
            Tron.scale(beta, d);
            Tron.daxpy(one, r, d);
            rTr = rnewTrnew;
        }
        return cg_iter;
    }

    private static void daxpy(double constant, double[] vector1, double[] vector2) {
        if (constant == 0.0) {
            return;
        }
        assert (vector1.length == vector2.length);
        for (int i = 0; i < vector1.length; ++i) {
            double[] arrd = vector2;
            int n = i;
            arrd[n] = arrd[n] + constant * vector1[i];
        }
    }

    private static double dot(double[] vector1, double[] vector2) {
        double product = 0.0;
        assert (vector1.length == vector2.length);
        for (int i = 0; i < vector1.length; ++i) {
            product += vector1[i] * vector2[i];
        }
        return product;
    }

    private static double euclideanNorm(double[] vector) {
        int n = vector.length;
        if (n < 1) {
            return 0.0;
        }
        if (n == 1) {
            return Math.abs(vector[0]);
        }
        double scale = 0.0;
        double sum = 1.0;
        for (int i = 0; i < n; ++i) {
            double t;
            if (vector[i] == 0.0) continue;
            double abs = Math.abs(vector[i]);
            if (scale < abs) {
                t = scale / abs;
                sum = 1.0 + sum * (t * t);
                scale = abs;
                continue;
            }
            t = abs / scale;
            sum += t * t;
        }
        return scale * Math.sqrt(sum);
    }

    private static void scale(double constant, double[] vector) {
        if (constant == 1.0) {
            return;
        }
        int i = 0;
        while (i < vector.length) {
            double[] arrd = vector;
            int n = i++;
            arrd[n] = arrd[n] * constant;
        }
    }
}

