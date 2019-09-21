package libsvm;

class ONE_CLASS_Q extends Kernel {
   private final Cache cache;
   private final double[] QD;

   ONE_CLASS_Q(svm_problem prob, svm_parameter param) {
      super(prob.l, prob.x, param);
      this.cache = new Cache(prob.l, (long)(param.cache_size * 1048576.0D));
      this.QD = new double[prob.l];

      for(int i = 0; i < prob.l; ++i) {
         this.QD[i] = this.kernel_function(i, i);
      }

   }

   float[] get_Q(int i, int len) {
      float[][] data = new float[1][];
      int start;
      if ((start = this.cache.get_data(i, data, len)) < len) {
         for(int j = start; j < len; ++j) {
            data[0][j] = (float)this.kernel_function(i, j);
         }
      }

      return data[0];
   }

   double[] get_QD() {
      return this.QD;
   }

   void swap_index(int i, int j) {
      this.cache.swap_index(i, j);
      super.swap_index(i, j);
      double _ = this.QD[i];
      this.QD[i] = this.QD[j];
      this.QD[j] = _;
   }
}
