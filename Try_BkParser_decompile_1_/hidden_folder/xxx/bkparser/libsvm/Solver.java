package libsvm;

class Solver {
   int active_size;
   byte[] y;
   double[] G;
   static final byte LOWER_BOUND = 0;
   static final byte UPPER_BOUND = 1;
   static final byte FREE = 2;
   byte[] alpha_status;
   double[] alpha;
   QMatrix Q;
   double[] QD;
   double eps;
   double Cp;
   double Cn;
   double[] p;
   int[] active_set;
   double[] G_bar;
   int l;
   boolean unshrink;
   static final double INF = 1.0D / 0.0;

   Solver() {
   }

   double get_C(int i) {
      return this.y[i] > 0 ? this.Cp : this.Cn;
   }

   void update_alpha_status(int i) {
      if (this.alpha[i] >= this.get_C(i)) {
         this.alpha_status[i] = 1;
      } else if (this.alpha[i] <= 0.0D) {
         this.alpha_status[i] = 0;
      } else {
         this.alpha_status[i] = 2;
      }

   }

   boolean is_upper_bound(int i) {
      return this.alpha_status[i] == 1;
   }

   boolean is_lower_bound(int i) {
      return this.alpha_status[i] == 0;
   }

   boolean is_free(int i) {
      return this.alpha_status[i] == 2;
   }

   void swap_index(int i, int j) {
      this.Q.swap_index(i, j);
      byte _ = this.y[i];
      this.y[i] = this.y[j];
      this.y[j] = _;
      double _ = this.G[i];
      this.G[i] = this.G[j];
      this.G[j] = _;
      _ = this.alpha_status[i];
      this.alpha_status[i] = this.alpha_status[j];
      this.alpha_status[j] = _;
      _ = this.alpha[i];
      this.alpha[i] = this.alpha[j];
      this.alpha[j] = _;
      _ = this.p[i];
      this.p[i] = this.p[j];
      this.p[j] = _;
      int _ = this.active_set[i];
      this.active_set[i] = this.active_set[j];
      this.active_set[j] = _;
      _ = this.G_bar[i];
      this.G_bar[i] = this.G_bar[j];
      this.G_bar[j] = _;
   }

   void reconstruct_gradient() {
      if (this.active_size != this.l) {
         int nr_free = 0;

         int j;
         for(j = this.active_size; j < this.l; ++j) {
            this.G[j] = this.G_bar[j] + this.p[j];
         }

         for(j = 0; j < this.active_size; ++j) {
            if (this.is_free(j)) {
               ++nr_free;
            }
         }

         if (2 * nr_free < this.active_size) {
            svm.info("\nWarning: using -h 0 may be faster\n");
         }

         double[] var10000;
         int i;
         float[] Q_i;
         if (nr_free * this.l > 2 * this.active_size * (this.l - this.active_size)) {
            for(i = this.active_size; i < this.l; ++i) {
               Q_i = this.Q.get_Q(i, this.active_size);

               for(j = 0; j < this.active_size; ++j) {
                  if (this.is_free(j)) {
                     var10000 = this.G;
                     var10000[i] += this.alpha[j] * (double)Q_i[j];
                  }
               }
            }
         } else {
            for(i = 0; i < this.active_size; ++i) {
               if (this.is_free(i)) {
                  Q_i = this.Q.get_Q(i, this.l);
                  double alpha_i = this.alpha[i];

                  for(j = this.active_size; j < this.l; ++j) {
                     var10000 = this.G;
                     var10000[j] += alpha_i * (double)Q_i[j];
                  }
               }
            }
         }

      }
   }

   void Solve(int l, QMatrix Q, double[] p_, byte[] y_, double[] alpha_, double Cp, double Cn, double eps, Solver.SolutionInfo si, int shrinking) {
      this.l = l;
      this.Q = Q;
      this.QD = Q.get_QD();
      this.p = (double[])((double[])p_.clone());
      this.y = (byte[])((byte[])y_.clone());
      this.alpha = (double[])((double[])alpha_.clone());
      this.Cp = Cp;
      this.Cn = Cn;
      this.eps = eps;
      this.unshrink = false;
      this.alpha_status = new byte[l];

      int iter;
      for(iter = 0; iter < l; ++iter) {
         this.update_alpha_status(iter);
      }

      this.active_set = new int[l];

      for(iter = 0; iter < l; this.active_set[iter] = iter++) {
      }

      this.active_size = l;
      this.G = new double[l];
      this.G_bar = new double[l];

      for(iter = 0; iter < l; ++iter) {
         this.G[iter] = this.p[iter];
         this.G_bar[iter] = 0.0D;
      }

      double[] var10000;
      int j;
      for(iter = 0; iter < l; ++iter) {
         if (!this.is_lower_bound(iter)) {
            float[] Q_i = Q.get_Q(iter, l);
            double alpha_i = this.alpha[iter];

            for(j = 0; j < l; ++j) {
               var10000 = this.G;
               var10000[j] += alpha_i * (double)Q_i[j];
            }

            if (this.is_upper_bound(iter)) {
               for(j = 0; j < l; ++j) {
                  var10000 = this.G_bar;
                  var10000[j] += this.get_C(iter) * (double)Q_i[j];
               }
            }
         }
      }

      iter = 0;
      int counter = Math.min(l, 1000) + 1;
      int[] working_set = new int[2];

      while(true) {
         while(true) {
            float[] Q_j;
            double C_j;
            boolean uj;
            int k;
            do {
               --counter;
               if (counter == 0) {
                  counter = Math.min(l, 1000);
                  if (shrinking != 0) {
                     this.do_shrinking();
                  }

                  svm.info(".");
               }

               int i;
               if (this.select_working_set(working_set) != 0) {
                  this.reconstruct_gradient();
                  this.active_size = l;
                  svm.info("*");
                  if (this.select_working_set(working_set) != 0) {
                     si.rho = this.calculate_rho();
                     double v = 0.0D;

                     for(int i = 0; i < l; ++i) {
                        v += this.alpha[i] * (this.G[i] + this.p[i]);
                     }

                     si.obj = v / 2.0D;

                     for(i = 0; i < l; ++i) {
                        alpha_[this.active_set[i]] = this.alpha[i];
                     }

                     si.upper_bound_p = Cp;
                     si.upper_bound_n = Cn;
                     svm.info("\noptimization finished, #iter = " + iter + "\n");
                     return;
                  }

                  counter = 1;
               }

               i = working_set[0];
               j = working_set[1];
               ++iter;
               float[] Q_i = Q.get_Q(i, this.active_size);
               Q_j = Q.get_Q(j, this.active_size);
               double C_i = this.get_C(i);
               C_j = this.get_C(j);
               double old_alpha_i = this.alpha[i];
               double old_alpha_j = this.alpha[j];
               double delta_alpha_i;
               double delta_alpha_j;
               double diff;
               if (this.y[i] != this.y[j]) {
                  delta_alpha_i = this.QD[i] + this.QD[j] + (double)(2.0F * Q_i[j]);
                  if (delta_alpha_i <= 0.0D) {
                     delta_alpha_i = 1.0E-12D;
                  }

                  delta_alpha_j = (-this.G[i] - this.G[j]) / delta_alpha_i;
                  diff = this.alpha[i] - this.alpha[j];
                  var10000 = this.alpha;
                  var10000[i] += delta_alpha_j;
                  var10000 = this.alpha;
                  var10000[j] += delta_alpha_j;
                  if (diff > 0.0D) {
                     if (this.alpha[j] < 0.0D) {
                        this.alpha[j] = 0.0D;
                        this.alpha[i] = diff;
                     }
                  } else if (this.alpha[i] < 0.0D) {
                     this.alpha[i] = 0.0D;
                     this.alpha[j] = -diff;
                  }

                  if (diff > C_i - C_j) {
                     if (this.alpha[i] > C_i) {
                        this.alpha[i] = C_i;
                        this.alpha[j] = C_i - diff;
                     }
                  } else if (this.alpha[j] > C_j) {
                     this.alpha[j] = C_j;
                     this.alpha[i] = C_j + diff;
                  }
               } else {
                  delta_alpha_i = this.QD[i] + this.QD[j] - (double)(2.0F * Q_i[j]);
                  if (delta_alpha_i <= 0.0D) {
                     delta_alpha_i = 1.0E-12D;
                  }

                  delta_alpha_j = (this.G[i] - this.G[j]) / delta_alpha_i;
                  diff = this.alpha[i] + this.alpha[j];
                  var10000 = this.alpha;
                  var10000[i] -= delta_alpha_j;
                  var10000 = this.alpha;
                  var10000[j] += delta_alpha_j;
                  if (diff > C_i) {
                     if (this.alpha[i] > C_i) {
                        this.alpha[i] = C_i;
                        this.alpha[j] = diff - C_i;
                     }
                  } else if (this.alpha[j] < 0.0D) {
                     this.alpha[j] = 0.0D;
                     this.alpha[i] = diff;
                  }

                  if (diff > C_j) {
                     if (this.alpha[j] > C_j) {
                        this.alpha[j] = C_j;
                        this.alpha[i] = diff - C_j;
                     }
                  } else if (this.alpha[i] < 0.0D) {
                     this.alpha[i] = 0.0D;
                     this.alpha[j] = diff;
                  }
               }

               delta_alpha_i = this.alpha[i] - old_alpha_i;
               delta_alpha_j = this.alpha[j] - old_alpha_j;

               for(int k = 0; k < this.active_size; ++k) {
                  var10000 = this.G;
                  var10000[k] += (double)Q_i[k] * delta_alpha_i + (double)Q_j[k] * delta_alpha_j;
               }

               boolean ui = this.is_upper_bound(i);
               uj = this.is_upper_bound(j);
               this.update_alpha_status(i);
               this.update_alpha_status(j);
               if (ui != this.is_upper_bound(i)) {
                  Q_i = Q.get_Q(i, l);
                  if (ui) {
                     for(k = 0; k < l; ++k) {
                        var10000 = this.G_bar;
                        var10000[k] -= C_i * (double)Q_i[k];
                     }
                  } else {
                     for(k = 0; k < l; ++k) {
                        var10000 = this.G_bar;
                        var10000[k] += C_i * (double)Q_i[k];
                     }
                  }
               }
            } while(uj == this.is_upper_bound(j));

            Q_j = Q.get_Q(j, l);
            if (uj) {
               for(k = 0; k < l; ++k) {
                  var10000 = this.G_bar;
                  var10000[k] -= C_j * (double)Q_j[k];
               }
            } else {
               for(k = 0; k < l; ++k) {
                  var10000 = this.G_bar;
                  var10000[k] += C_j * (double)Q_j[k];
               }
            }
         }
      }
   }

   int select_working_set(int[] working_set) {
      double Gmax = -1.0D / 0.0;
      double Gmax2 = -1.0D / 0.0;
      int Gmax_idx = -1;
      int Gmin_idx = -1;
      double obj_diff_min = 1.0D / 0.0;

      int i;
      for(i = 0; i < this.active_size; ++i) {
         if (this.y[i] == 1) {
            if (!this.is_upper_bound(i) && -this.G[i] >= Gmax) {
               Gmax = -this.G[i];
               Gmax_idx = i;
            }
         } else if (!this.is_lower_bound(i) && this.G[i] >= Gmax) {
            Gmax = this.G[i];
            Gmax_idx = i;
         }
      }

      i = Gmax_idx;
      float[] Q_i = null;
      if (Gmax_idx != -1) {
         Q_i = this.Q.get_Q(Gmax_idx, this.active_size);
      }

      for(int j = 0; j < this.active_size; ++j) {
         double grad_diff;
         double obj_diff;
         double quad_coef;
         if (this.y[j] == 1) {
            if (!this.is_lower_bound(j)) {
               grad_diff = Gmax + this.G[j];
               if (this.G[j] >= Gmax2) {
                  Gmax2 = this.G[j];
               }

               if (grad_diff > 0.0D) {
                  quad_coef = this.QD[i] + this.QD[j] - 2.0D * (double)this.y[i] * (double)Q_i[j];
                  if (quad_coef > 0.0D) {
                     obj_diff = -(grad_diff * grad_diff) / quad_coef;
                  } else {
                     obj_diff = -(grad_diff * grad_diff) / 1.0E-12D;
                  }

                  if (obj_diff <= obj_diff_min) {
                     Gmin_idx = j;
                     obj_diff_min = obj_diff;
                  }
               }
            }
         } else if (!this.is_upper_bound(j)) {
            grad_diff = Gmax - this.G[j];
            if (-this.G[j] >= Gmax2) {
               Gmax2 = -this.G[j];
            }

            if (grad_diff > 0.0D) {
               quad_coef = this.QD[i] + this.QD[j] + 2.0D * (double)this.y[i] * (double)Q_i[j];
               if (quad_coef > 0.0D) {
                  obj_diff = -(grad_diff * grad_diff) / quad_coef;
               } else {
                  obj_diff = -(grad_diff * grad_diff) / 1.0E-12D;
               }

               if (obj_diff <= obj_diff_min) {
                  Gmin_idx = j;
                  obj_diff_min = obj_diff;
               }
            }
         }
      }

      if (Gmax + Gmax2 < this.eps) {
         return 1;
      } else {
         working_set[0] = Gmax_idx;
         working_set[1] = Gmin_idx;
         return 0;
      }
   }

   private boolean be_shrunk(int i, double Gmax1, double Gmax2) {
      if (this.is_upper_bound(i)) {
         if (this.y[i] == 1) {
            return -this.G[i] > Gmax1;
         } else {
            return -this.G[i] > Gmax2;
         }
      } else if (this.is_lower_bound(i)) {
         if (this.y[i] == 1) {
            return this.G[i] > Gmax2;
         } else {
            return this.G[i] > Gmax1;
         }
      } else {
         return false;
      }
   }

   void do_shrinking() {
      double Gmax1 = -1.0D / 0.0;
      double Gmax2 = -1.0D / 0.0;

      int i;
      for(i = 0; i < this.active_size; ++i) {
         if (this.y[i] == 1) {
            if (!this.is_upper_bound(i) && -this.G[i] >= Gmax1) {
               Gmax1 = -this.G[i];
            }

            if (!this.is_lower_bound(i) && this.G[i] >= Gmax2) {
               Gmax2 = this.G[i];
            }
         } else {
            if (!this.is_upper_bound(i) && -this.G[i] >= Gmax2) {
               Gmax2 = -this.G[i];
            }

            if (!this.is_lower_bound(i) && this.G[i] >= Gmax1) {
               Gmax1 = this.G[i];
            }
         }
      }

      if (!this.unshrink && Gmax1 + Gmax2 <= this.eps * 10.0D) {
         this.unshrink = true;
         this.reconstruct_gradient();
         this.active_size = this.l;
      }

      for(i = 0; i < this.active_size; ++i) {
         if (this.be_shrunk(i, Gmax1, Gmax2)) {
            --this.active_size;

            while(this.active_size > i) {
               if (!this.be_shrunk(this.active_size, Gmax1, Gmax2)) {
                  this.swap_index(i, this.active_size);
                  break;
               }

               --this.active_size;
            }
         }
      }

   }

   double calculate_rho() {
      int nr_free = 0;
      double ub = 1.0D / 0.0;
      double lb = -1.0D / 0.0;
      double sum_free = 0.0D;

      for(int i = 0; i < this.active_size; ++i) {
         double yG = (double)this.y[i] * this.G[i];
         if (this.is_lower_bound(i)) {
            if (this.y[i] > 0) {
               ub = Math.min(ub, yG);
            } else {
               lb = Math.max(lb, yG);
            }
         } else if (this.is_upper_bound(i)) {
            if (this.y[i] < 0) {
               ub = Math.min(ub, yG);
            } else {
               lb = Math.max(lb, yG);
            }
         } else {
            ++nr_free;
            sum_free += yG;
         }
      }

      double r;
      if (nr_free > 0) {
         r = sum_free / (double)nr_free;
      } else {
         r = (ub + lb) / 2.0D;
      }

      return r;
   }

   static class SolutionInfo {
      double obj;
      double rho;
      double upper_bound_p;
      double upper_bound_n;
      double r;

      SolutionInfo() {
      }
   }
}
