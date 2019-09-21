package de.bwaldvogel.liblinear;

class L2R_L2_SvcFunction implements Function {
   private final Problem prob;
   private final double[] C;
   private final int[] I;
   private final double[] z;
   private int sizeI;

   public L2R_L2_SvcFunction(Problem prob, double Cp, double Cn) {
      int l = prob.l;
      int[] y = prob.y;
      this.prob = prob;
      this.z = new double[l];
      this.C = new double[l];
      this.I = new int[l];

      for(int i = 0; i < l; ++i) {
         if (y[i] == 1) {
            this.C[i] = Cp;
         } else {
            this.C[i] = Cn;
         }
      }

   }

   public double fun(double[] w) {
      double f = 0.0D;
      int[] y = this.prob.y;
      int l = this.prob.l;
      int w_size = this.get_nr_variable();
      this.Xv(w, this.z);

      int i;
      for(i = 0; i < l; ++i) {
         this.z[i] = (double)y[i] * this.z[i];
         double d = 1.0D - this.z[i];
         if (d > 0.0D) {
            f += this.C[i] * d * d;
         }
      }

      f = 2.0D * f;

      for(i = 0; i < w_size; ++i) {
         f += w[i] * w[i];
      }

      f /= 2.0D;
      return f;
   }

   public int get_nr_variable() {
      return this.prob.n;
   }

   public void grad(double[] w, double[] g) {
      int[] y = this.prob.y;
      int l = this.prob.l;
      int w_size = this.get_nr_variable();
      this.sizeI = 0;

      int i;
      for(i = 0; i < l; ++i) {
         if (this.z[i] < 1.0D) {
            this.z[this.sizeI] = this.C[i] * (double)y[i] * (this.z[i] - 1.0D);
            this.I[this.sizeI] = i;
            ++this.sizeI;
         }
      }

      this.subXTv(this.z, g);

      for(i = 0; i < w_size; ++i) {
         g[i] = w[i] + 2.0D * g[i];
      }

   }

   public void Hv(double[] s, double[] Hs) {
      int l = this.prob.l;
      int w_size = this.get_nr_variable();
      double[] wa = new double[l];
      this.subXv(s, wa);

      int i;
      for(i = 0; i < this.sizeI; ++i) {
         wa[i] = this.C[this.I[i]] * wa[i];
      }

      this.subXTv(wa, Hs);

      for(i = 0; i < w_size; ++i) {
         Hs[i] = s[i] + 2.0D * Hs[i];
      }

   }

   private void subXTv(double[] v, double[] XTv) {
      int w_size = this.get_nr_variable();

      int i;
      for(i = 0; i < w_size; ++i) {
         XTv[i] = 0.0D;
      }

      for(i = 0; i < this.sizeI; ++i) {
         FeatureNode[] arr$ = this.prob.x[this.I[i]];
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FeatureNode s = arr$[i$];
            int var10001 = s.index - 1;
            XTv[var10001] += v[i] * s.value;
         }
      }

   }

   private void subXv(double[] v, double[] Xv) {
      for(int i = 0; i < this.sizeI; ++i) {
         Xv[i] = 0.0D;
         FeatureNode[] arr$ = this.prob.x[this.I[i]];
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FeatureNode s = arr$[i$];
            Xv[i] += v[s.index - 1] * s.value;
         }
      }

   }

   private void Xv(double[] v, double[] Xv) {
      for(int i = 0; i < this.prob.l; ++i) {
         Xv[i] = 0.0D;
         FeatureNode[] arr$ = this.prob.x[i];
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FeatureNode s = arr$[i$];
            Xv[i] += v[s.index - 1] * s.value;
         }
      }

   }
}
