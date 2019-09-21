/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import java.io.Serializable;
import libsvm.svm_node;
import libsvm.svm_parameter;

public class svm_model
implements Serializable {
    public svm_parameter param;
    public int nr_class;
    public int l;
    public svm_node[][] SV;
    public double[][] sv_coef;
    public double[] rho;
    public double[] probA;
    public double[] probB;
    public int[] label;
    public int[] nSV;
}

