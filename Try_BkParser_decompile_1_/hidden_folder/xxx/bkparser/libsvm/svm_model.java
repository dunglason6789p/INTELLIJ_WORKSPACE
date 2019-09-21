package libsvm;

import java.io.Serializable;

public class svm_model implements Serializable {
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

   public svm_model() {
   }
}
