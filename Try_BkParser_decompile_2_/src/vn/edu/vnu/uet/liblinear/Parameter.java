/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import vn.edu.vnu.uet.liblinear.Linear;
import vn.edu.vnu.uet.liblinear.SolverType;

public final class Parameter {
    double C;
    double eps;
    int max_iters = 1000;
    SolverType solverType;
    double[] weight = null;
    int[] weightLabel = null;
    double p = 0.1;

    public Parameter(SolverType solver, double C, double eps) {
        this.setSolverType(solver);
        this.setC(C);
        this.setEps(eps);
    }

    public Parameter(SolverType solver, double C, int max_iters, double eps) {
        this.setSolverType(solver);
        this.setC(C);
        this.setEps(eps);
        this.setMaxIters(max_iters);
    }

    public Parameter(SolverType solverType, double C, double eps, double p) {
        this.setSolverType(solverType);
        this.setC(C);
        this.setEps(eps);
        this.setP(p);
    }

    public Parameter(SolverType solverType, double C, double eps, int max_iters, double p) {
        this.setSolverType(solverType);
        this.setC(C);
        this.setEps(eps);
        this.setMaxIters(max_iters);
        this.setP(p);
    }

    public void setWeights(double[] weights, int[] weightLabels) {
        if (weights == null) {
            throw new IllegalArgumentException("'weight' must not be null");
        }
        if (weightLabels == null || weightLabels.length != weights.length) {
            throw new IllegalArgumentException("'weightLabels' must have same length as 'weight'");
        }
        this.weightLabel = Linear.copyOf(weightLabels, weightLabels.length);
        this.weight = Linear.copyOf(weights, weights.length);
    }

    public double[] getWeights() {
        return Linear.copyOf(this.weight, this.weight.length);
    }

    public int[] getWeightLabels() {
        return Linear.copyOf(this.weightLabel, this.weightLabel.length);
    }

    public int getNumWeights() {
        if (this.weight == null) {
            return 0;
        }
        return this.weight.length;
    }

    public void setC(double C) {
        if (C <= 0.0) {
            throw new IllegalArgumentException("C must not be <= 0");
        }
        this.C = C;
    }

    public double getC() {
        return this.C;
    }

    public void setEps(double eps) {
        if (eps <= 0.0) {
            throw new IllegalArgumentException("eps must not be <= 0");
        }
        this.eps = eps;
    }

    public double getEps() {
        return this.eps;
    }

    public void setMaxIters(int iters) {
        if (iters <= 0) {
            throw new IllegalArgumentException("max iters not be <= 0");
        }
        this.max_iters = iters;
    }

    public int getMaxIters() {
        return this.max_iters;
    }

    public void setSolverType(SolverType solverType) {
        if (solverType == null) {
            throw new IllegalArgumentException("solver type must not be null");
        }
        this.solverType = solverType;
    }

    public SolverType getSolverType() {
        return this.solverType;
    }

    public void setP(double p) {
        if (p < 0.0) {
            throw new IllegalArgumentException("p must not be less than 0");
        }
        this.p = p;
    }

    public double getP() {
        return this.p;
    }
}

