package de.bwaldvogel.liblinear;

class L2R_LrFunction implements Function {
   private final double[] C;
   private final double[] z;
   private final double[] D;
   private final Problem prob;

   public L2R_LrFunction(Problem prob, double Cp, double Cn) {
      int l = prob.l;
      int[] y = prob.y;
      this.prob = prob;
      this.z = new double[l];
      this.D = new double[l];
      this.C = new double[l];

      for(int i = 0; i < l; ++i) {
         if (y[i] == 1) {
            this.C[i] = Cp;
         } else {
            this.C[i] = Cn;
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

   private void XTv(double[] v, double[] XTv) {
      int l = this.prob.l;
      int w_size = this.get_nr_variable();
      FeatureNode[][] x = this.prob.x;

      int i;
      for(i = 0; i < w_size; ++i) {
         XTv[i] = 0.0D;
      }

      for(i = 0; i < l; ++i) {
         FeatureNode[] arr$ = x[i];
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FeatureNode s = arr$[i$];
            int var10001 = s.index - 1;
            XTv[var10001] += v[i] * s.value;
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
         double yz = (double)y[i] * this.z[i];
         if (yz >= 0.0D) {
            f += this.C[i] * Math.log(1.0D + Math.exp(-yz));
         } else {
            f += this.C[i] * (-yz + Math.log(1.0D + Math.exp(yz)));
         }
      }

      f = 2.0D * f;

      for(i = 0; i < w_size; ++i) {
         f += w[i] * w[i];
      }

      f /= 2.0D;
      return f;
   }

   public void grad(double[] w, double[] g) {
      int[] y = this.prob.y;
      int l = this.prob.l;
      int w_size = this.get_nr_variable();

      int i;
      for(i = 0; i < l; ++i) {
         this.z[i] = 1.0D / (1.0D + Math.exp((double)(-y[i]) * this.z[i]));
         this.D[i] = this.z[i] * (1.0D - this.z[i]);
         this.z[i] = this.C[i] * (this.z[i] - 1.0D) * (double)y[i];
      }

      this.XTv(this.z, g);

      for(i = 0; i < w_size; ++i) {
         g[i] += w[i];
      }

   }

   public void Hv(double[] s, double[] Hs) {
      int l = this.prob.l;
      int w_size = this.get_nr_variable();
      double[] wa = new double[l];
      this.Xv(s, wa);

      int i;
      for(i = 0; i < l; ++i) {
         wa[i] = this.C[i] * this.D[i] * wa[i];
      }

      this.XTv(wa, Hs);

      for(i = 0; i < w_size; ++i) {
         Hs[i] += s[i];
      }

   }

   public int get_nr_variable() {
      return this.prob.n;
   }
}
