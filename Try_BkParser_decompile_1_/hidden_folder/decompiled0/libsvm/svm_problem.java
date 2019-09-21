/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import java.io.Serializable;
import libsvm.svm_node;

public class svm_problem
implements Serializable {
    public int l;
    public double[] y;
    public svm_node[][] x;
}

