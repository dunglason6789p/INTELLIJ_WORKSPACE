/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import java.io.File;
import java.io.IOException;
import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.InvalidInputDataException;
import vn.edu.vnu.uet.liblinear.Train;

public class Problem {
    public int l;
    public int n;
    public double[] y;
    public Feature[][] x;
    public double bias;

    public static Problem readFromFile(File file, double bias) throws IOException, InvalidInputDataException {
        return Train.readProblem(file, bias);
    }
}

