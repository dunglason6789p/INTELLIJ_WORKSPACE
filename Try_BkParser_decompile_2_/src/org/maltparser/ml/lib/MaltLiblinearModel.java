/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.maltparser.core.helper.Util;
import org.maltparser.ml.lib.MaltFeatureNode;
import org.maltparser.ml.lib.MaltLibModel;

public class MaltLiblinearModel
implements Serializable,
MaltLibModel {
    private static final long serialVersionUID = 7526471155622776147L;
    private static final Charset FILE_CHARSET = Charset.forName("ISO-8859-1");
    private double bias;
    private int[] labels;
    private int nr_class;
    private int nr_feature;
    private SolverType solverType;
    private double[][] w;

    public MaltLiblinearModel(int[] labels, int nr_class, int nr_feature, double[][] w, SolverType solverType) {
        this.labels = labels;
        this.nr_class = nr_class;
        this.nr_feature = nr_feature;
        this.w = w;
        this.solverType = solverType;
    }

    public MaltLiblinearModel(Reader inputReader) throws IOException {
        this.loadModel(inputReader);
    }

    public MaltLiblinearModel(File modelFile) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(modelFile), FILE_CHARSET));
        this.loadModel(inputReader);
    }

    public int getNrClass() {
        return this.nr_class;
    }

    public int getNrFeature() {
        return this.nr_feature;
    }

    public int[] getLabels() {
        return Util.copyOf(this.labels, this.nr_class);
    }

    public boolean isProbabilityModel() {
        return this.solverType == SolverType.L2R_LR || this.solverType == SolverType.L2R_LR_DUAL || this.solverType == SolverType.L1R_LR;
    }

    public double getBias() {
        return this.bias;
    }

    @Override
    public int[] predict(MaltFeatureNode[] x) {
        double[] dec_values = new double[this.nr_class];
        int n = this.bias >= 0.0 ? this.nr_feature + 1 : this.nr_feature;
        int xlen = x.length;
        for (int i = 0; i < xlen; ++i) {
            int t;
            if (x[i].index > n || this.w[t = x[i].index - 1] == null) continue;
            for (int j = 0; j < this.w[t].length; ++j) {
                double[] arrd = dec_values;
                int n2 = j;
                arrd[n2] = arrd[n2] + this.w[t][j] * x[i].value;
            }
        }
        int[] predictionList = new int[this.nr_class];
        System.arraycopy(this.labels, 0, predictionList, 0, this.nr_class);
        int nc = this.nr_class - 1;
        for (int i = 0; i < nc; ++i) {
            int iMax = i;
            for (int j = i + 1; j < this.nr_class; ++j) {
                if (!(dec_values[j] > dec_values[iMax])) continue;
                iMax = j;
            }
            if (iMax == i) continue;
            double tmpDec = dec_values[iMax];
            dec_values[iMax] = dec_values[i];
            dec_values[i] = tmpDec;
            int tmpObj = predictionList[iMax];
            predictionList[iMax] = predictionList[i];
            predictionList[i] = tmpObj;
        }
        return predictionList;
    }

    @Override
    public int predict_one(MaltFeatureNode[] x) {
        double[] dec_values = new double[this.nr_class];
        int n = this.bias >= 0.0 ? this.nr_feature + 1 : this.nr_feature;
        int xlen = x.length;
        for (int i = 0; i < xlen; ++i) {
            int t;
            if (x[i].index > n || this.w[t = x[i].index - 1] == null) continue;
            for (int j = 0; j < this.w[t].length; ++j) {
                double[] arrd = dec_values;
                int n2 = j;
                arrd[n2] = arrd[n2] + this.w[t][j] * x[i].value;
            }
        }
        double max = dec_values[0];
        int max_index = 0;
        for (int i = 1; i < dec_values.length; ++i) {
            if (!(dec_values[i] > max)) continue;
            max = dec_values[i];
            max_index = i;
        }
        return this.labels[max_index];
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.defaultWriteObject();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadModel(Reader inputReader) throws IOException {
        this.labels = null;
        Pattern whitespace = Pattern.compile("\\s+");
        BufferedReader reader = null;
        reader = inputReader instanceof BufferedReader ? (BufferedReader)inputReader : new BufferedReader(inputReader);
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = whitespace.split(line);
                if (split[0].equals("solver_type")) {
                    SolverType solver = SolverType.valueOf(split[1]);
                    if (solver == null) {
                        throw new RuntimeException("unknown solver type");
                    }
                    this.solverType = solver;
                    continue;
                }
                if (split[0].equals("nr_class")) {
                    this.nr_class = Util.atoi(split[1]);
                    Integer.parseInt(split[1]);
                    continue;
                }
                if (split[0].equals("nr_feature")) {
                    this.nr_feature = Util.atoi(split[1]);
                    continue;
                }
                if (split[0].equals("bias")) {
                    this.bias = Util.atof(split[1]);
                    continue;
                }
                if (split[0].equals("w")) break;
                if (split[0].equals("label")) {
                    this.labels = new int[this.nr_class];
                    for (int i = 0; i < this.nr_class; ++i) {
                        this.labels[i] = Util.atoi(split[i + 1]);
                    }
                    continue;
                }
                throw new RuntimeException("unknown text in model file: [" + line + "]");
            }
            int w_size = this.nr_feature;
            if (this.bias >= 0.0) {
                ++w_size;
            }
            int nr_w = this.nr_class;
            if (this.nr_class == 2 && this.solverType != SolverType.MCSVM_CS) {
                nr_w = 1;
            }
            this.w = new double[w_size][nr_w];
            int[] buffer = new int[128];
            for (int i = 0; i < w_size; ++i) {
                for (int j = 0; j < nr_w; ++j) {
                    int b = 0;
                    do {
                        int ch;
                        if ((ch = reader.read()) == -1) {
                            throw new EOFException("unexpected EOF");
                        }
                        if (ch == 32) break;
                        buffer[b++] = ch;
                    } while (true);
                    this.w[i][j] = Util.atof(new String(buffer, 0, b));
                }
            }
        }
        finally {
            Util.closeQuietly(reader);
        }
    }

    public int hashCode() {
        int prime = 31;
        long temp = Double.doubleToLongBits(this.bias);
        int result = 31 + (int)(temp ^ temp >>> 32);
        result = 31 * result + Arrays.hashCode(this.labels);
        result = 31 * result + this.nr_class;
        result = 31 * result + this.nr_feature;
        result = 31 * result + (this.solverType == null ? 0 : this.solverType.hashCode());
        for (int i = 0; i < this.w.length; ++i) {
            result = 31 * result + Arrays.hashCode(this.w[i]);
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        MaltLiblinearModel other = (MaltLiblinearModel)obj;
        if (Double.doubleToLongBits(this.bias) != Double.doubleToLongBits(other.bias)) {
            return false;
        }
        if (!Arrays.equals(this.labels, other.labels)) {
            return false;
        }
        if (this.nr_class != other.nr_class) {
            return false;
        }
        if (this.nr_feature != other.nr_feature) {
            return false;
        }
        if (this.solverType == null ? other.solverType != null : !this.solverType.equals((Object)other.solverType)) {
            return false;
        }
        for (int i = 0; i < this.w.length; ++i) {
            if (other.w.length <= i) {
                return false;
            }
            if (Util.equals(this.w[i], other.w[i])) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Model");
        sb.append(" bias=").append(this.bias);
        sb.append(" nr_class=").append(this.nr_class);
        sb.append(" nr_feature=").append(this.nr_feature);
        sb.append(" solverType=").append((Object)this.solverType);
        return sb.toString();
    }
}

