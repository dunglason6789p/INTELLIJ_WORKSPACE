/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

interface Function {
    public double fun(double[] var1);

    public void grad(double[] var1, double[] var2);

    public void Hv(double[] var1, double[] var2);

    public int get_nr_variable();
}

