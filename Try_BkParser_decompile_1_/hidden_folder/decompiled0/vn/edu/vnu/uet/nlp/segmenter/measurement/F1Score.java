/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter.measurement;

public class F1Score {
    private double P;
    private double R;
    private double F;

    public F1Score() {
        this.P = 0.0;
        this.R = 0.0;
        this.F = 0.0;
    }

    public F1Score(int N1, int N2, int N3) {
        if (N1 == 0 || N2 == 0) {
            new F1Score();
        } else {
            this.P = (double)N3 / (double)N1 * 100.0;
            this.R = (double)N3 / (double)N2 * 100.0;
            this.F = 2.0 * this.P * this.R / (this.P + this.R);
        }
    }

    public double getPrecision() {
        return this.P;
    }

    public double getRecall() {
        return this.R;
    }

    public double getF1Score() {
        return this.F;
    }

    public String toString() {
        return this.P + "%\t" + this.R + "%\t" + this.F + "%";
    }
}

