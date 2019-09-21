/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Train;
import java.io.File;
import java.io.IOException;

public class Problem {
    public int l;
    public int n;
    public int[] y;
    public FeatureNode[][] x;
    public double bias;

    public static Problem readFromFile(File file, double bias) throws IOException, InvalidInputDataException {
        return Train.readProblem(file, bias);
    }
}

