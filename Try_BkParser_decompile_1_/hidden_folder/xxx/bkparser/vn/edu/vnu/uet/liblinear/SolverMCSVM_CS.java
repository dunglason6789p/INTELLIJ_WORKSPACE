package vn.edu.vnu.uet.liblinear;

class SolverMCSVM_CS {
   private final double[] B;
   private final double[] C;
   private final double eps;
   private final double[] G;
   private final int max_iter;
   private final int w_size;
   private final int l;
   private final int nr_class;
   private final Problem prob;

   public SolverMCSVM_CS(Problem prob, int nr_class, double[] C) {
      this(prob, nr_class, C, 0.1D);
   }

   public SolverMCSVM_CS(Problem prob, int nr_class, double[] C, double eps) {
      this(prob, nr_class, C, eps, 100000);
   }

   public SolverMCSVM_CS(Problem prob, int nr_class, double[] weighted_C, double eps, int max_iter) {
      this.w_size = prob.n;
      this.l = prob.l;
      this.nr_class = nr_class;
      this.eps = eps;
      this.max_iter = max_iter;
      this.prob = prob;
      this.C = weighted_C;
      this.B = new double[nr_class];
      this.G = new double[nr_class];
   }

   private int GETI(int i) {
      return (int)this.prob.y[i];
   }

   private boolean be_shrunk(int i, int m, int yi, double alpha_i, double minG) {
      double bound = 0.0D;
      if (m == yi) {
         bound = this.C[this.GETI(i)];
      }

      return alpha_i == bound && this.G[m] < minG;
   }

   public void solve(double[] w) {
      int iter = 0;
      double[] alpha = new double[this.l * this.nr_class];
      double[] alpha_new = new double[this.nr_class];
      int[] index = new int[this.l];
      double[] QD = new double[this.l];
      int[] d_ind = new int[this.nr_class];
      double[] d_val = new double[this.nr_class];
      int[] alpha_index = new int[this.nr_class * this.l];
      int[] y_index = new int[this.l];
      int active_size = this.l;
      int[] active_size_i = new int[this.l];
      double eps_shrink = Math.max(10.0D * this.eps, 1.0D);
      boolean start_from_all = true;

      int i;
      for(i = 0; i < this.l * this.nr_class; ++i) {
         alpha[i] = 0.0D;
      }

      for(i = 0; i < this.w_size * this.nr_class; ++i) {
         w[i] = 0.0D;
      }

      int m;
      double Ai;
      for(i = 0; i < this.l; index[i] = i++) {
         for(m = 0; m < this.nr_class; alpha_index[i * this.nr_class + m] = m++) {
         }

         QD[i] = 0.0D;
         Feature[] var19 = this.prob.x[i];
         int var20 = var19.length;

         for(int var21 = 0; var21 < var20; ++var21) {
            Feature xi = var19[var21];
            Ai = xi.getValue();
            QD[i] += Ai * Ai;
         }

         active_size_i[i] = this.nr_class;
         y_index[i] = (int)this.prob.y[i];
      }

      DoubleArrayPointer alpha_i = new DoubleArrayPointer(alpha, 0);
      IntArrayPointer alpha_index_i = new IntArrayPointer(alpha_index, 0);

      double stopping;
      int nSV;
      while(iter < this.max_iter) {
         stopping = -1.0D / 0.0;

         for(i = 0; i < active_size; ++i) {
            nSV = i + Linear.random.nextInt(active_size - i);
            Linear.swap(index, i, nSV);
         }

         for(int s = 0; s < active_size; ++s) {
            i = index[s];
            Ai = QD[i];
            alpha_i.setOffset(i * this.nr_class);
            alpha_index_i.setOffset(i * this.nr_class);
            if (Ai > 0.0D) {
               for(m = 0; m < active_size_i[i]; ++m) {
                  this.G[m] = 1.0D;
               }

               if (y_index[i] < active_size_i[i]) {
                  this.G[y_index[i]] = 0.0D;
               }

               Feature[] var25 = this.prob.x[i];
               int var26 = var25.length;

               int nz_d;
               for(int var27 = 0; var27 < var26; ++var27) {
                  Feature xi = var25[var27];
                  nz_d = (xi.getIndex() - 1) * this.nr_class;

                  for(m = 0; m < active_size_i[i]; ++m) {
                     double[] var10000 = this.G;
                     var10000[m] += w[nz_d + alpha_index_i.get(m)] * xi.getValue();
                  }
               }

               double minG = 1.0D / 0.0;
               double maxG = -1.0D / 0.0;

               for(m = 0; m < active_size_i[i]; ++m) {
                  if (alpha_i.get(alpha_index_i.get(m)) < 0.0D && this.G[m] < minG) {
                     minG = this.G[m];
                  }

                  if (this.G[m] > maxG) {
                     maxG = this.G[m];
                  }
               }

               if (y_index[i] < active_size_i[i] && alpha_i.get((int)this.prob.y[i]) < this.C[this.GETI(i)] && this.G[y_index[i]] < minG) {
                  minG = this.G[y_index[i]];
               }

               for(m = 0; m < active_size_i[i]; ++m) {
                  if (this.be_shrunk(i, m, y_index[i], alpha_i.get(alpha_index_i.get(m)), minG)) {
                     for(int var10002 = active_size_i[i]--; active_size_i[i] > m; var10002 = active_size_i[i]--) {
                        if (!this.be_shrunk(i, active_size_i[i], y_index[i], alpha_i.get(alpha_index_i.get(active_size_i[i])), minG)) {
                           Linear.swap(alpha_index_i, m, active_size_i[i]);
                           Linear.swap(this.G, m, active_size_i[i]);
                           if (y_index[i] == active_size_i[i]) {
                              y_index[i] = m;
                           } else if (y_index[i] == m) {
                              y_index[i] = active_size_i[i];
                           }
                           break;
                        }
                     }
                  }
               }

               if (active_size_i[i] <= 1) {
                  --active_size;
                  Linear.swap(index, s, active_size);
                  --s;
               } else if (maxG - minG > 1.0E-12D) {
                  stopping = Math.max(maxG - minG, stopping);

                  for(m = 0; m < active_size_i[i]; ++m) {
                     this.B[m] = this.G[m] - Ai * alpha_i.get(alpha_index_i.get(m));
                  }

                  this.solve_sub_problem(Ai, y_index[i], this.C[this.GETI(i)], active_size_i[i], alpha_new);
                  nz_d = 0;

                  for(m = 0; m < active_size_i[i]; ++m) {
                     double d = alpha_new[m] - alpha_i.get(alpha_index_i.get(m));
                     alpha_i.set(alpha_index_i.get(m), alpha_new[m]);
                     if (Math.abs(d) >= 1.0E-12D) {
                        d_ind[nz_d] = alpha_index_i.get(m);
                        d_val[nz_d] = d;
                        ++nz_d;
                     }
                  }

                  Feature[] var41 = this.prob.x[i];
                  int var31 = var41.length;

                  for(int var32 = 0; var32 < var31; ++var32) {
                     Feature xi = var41[var32];
                     int w_offset = (xi.getIndex() - 1) * this.nr_class;

                     for(m = 0; m < nz_d; ++m) {
                        w[w_offset + d_ind[m]] += d_val[m] * xi.getValue();
                     }
                  }
               }
            }
         }

         ++iter;
         if (iter % 10 == 0) {
            Linear.info(".");
         }

         if (stopping >= eps_shrink) {
            start_from_all = false;
         } else {
            if (stopping < this.eps && start_from_all) {
               break;
            }

            active_size = this.l;

            for(i = 0; i < this.l; ++i) {
               active_size_i[i] = this.nr_class;
            }

            Linear.info("*");
            eps_shrink = Math.max(eps_shrink / 2.0D, this.eps);
            start_from_all = true;
         }
      }

      Linear.info("%noptimization finished, #iter = %d%n", iter);
      if (iter >= this.max_iter) {
         Linear.info("%nWARNING: reaching max number of iterations%n");
      }

      stopping = 0.0D;
      nSV = 0;

      for(i = 0; i < this.w_size * this.nr_class; ++i) {
         stopping += w[i] * w[i];
      }

      stopping = 0.5D * stopping;

      for(i = 0; i < this.l * this.nr_class; ++i) {
         stopping += alpha[i];
         if (Math.abs(alpha[i]) > 0.0D) {
            ++nSV;
         }
      }

      for(i = 0; i < this.l; ++i) {
         stopping -= alpha[i * this.nr_class + (int)this.prob.y[i]];
      }

      Linear.info("Objective value = %f%n", stopping);
      Linear.info("nSV = %d%n", nSV);
   }

   private void solve_sub_problem(double A_i, int yi, double C_yi, int active_i, double[] alpha_new) {
      assert active_i <= this.B.length;

      double[] D = Linear.copyOf(this.B, active_i);
      if (yi < active_i) {
         D[yi] += A_i * C_yi;
      }

      ArraySorter.reversedMergesort(D);
      double beta = D[0] - A_i * C_yi;

      int r;
      for(r = 1; r < active_i && beta < (double)r * D[r]; ++r) {
         beta += D[r];
      }

      beta /= (double)r;

      for(r = 0; r < active_i; ++r) {
         if (r == yi) {
            alpha_new[r] = Math.min(C_yi, (beta - this.B[r]) / A_i);
         } else {
            alpha_new[r] = Math.min(0.0D, (beta - this.B[r]) / A_i);
         }
      }

   }
}
