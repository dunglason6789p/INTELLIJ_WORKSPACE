package libsvm;

class SVR_Q extends Kernel {
   private final int l;
   private final Cache cache;
   private final byte[] sign;
   private final int[] index;
   private int next_buffer;
   private float[][] buffer;
   private final double[] QD;

   SVR_Q(svm_problem prob, svm_parameter param) {
      super(prob.l, prob.x, param);
      this.l = prob.l;
      this.cache = new Cache(this.l, (long)(param.cache_size * 1048576.0D));
      this.QD = new double[2 * this.l];
      this.sign = new byte[2 * this.l];
      this.index = new int[2 * this.l];

      for(int k = 0; k < this.l; ++k) {
         this.sign[k] = 1;
         this.sign[k + this.l] = -1;
         this.index[k] = k;
         this.index[k + this.l] = k;
         this.QD[k] = this.kernel_function(k, k);
         this.QD[k + this.l] = this.QD[k];
      }

      this.buffer = new float[2][2 * this.l];
      this.next_buffer = 0;
   }

   void swap_index(int i, int j) {
      byte _ = this.sign[i];
      this.sign[i] = this.sign[j];
      this.sign[j] = _;
      int _ = this.index[i];
      this.index[i] = this.index[j];
      this.index[j] = _;
      double _ = this.QD[i];
      this.QD[i] = this.QD[j];
      this.QD[j] = _;
   }

   float[] get_Q(int i, int len) {
      float[][] data = new float[1][];
      int real_i = this.index[i];
      int j;
      if (this.cache.get_data(real_i, data, this.l) < this.l) {
         for(j = 0; j < this.l; ++j) {
            data[0][j] = (float)this.kernel_function(real_i, j);
         }
      }

      float[] buf = this.buffer[this.next_buffer];
      this.next_buffer = 1 - this.next_buffer;
      byte si = this.sign[i];

      for(j = 0; j < len; ++j) {
         buf[j] = (float)si * (float)this.sign[j] * data[0][this.index[j]];
      }

      return buf;
   }

   double[] get_QD() {
      return this.QD;
   }
}
