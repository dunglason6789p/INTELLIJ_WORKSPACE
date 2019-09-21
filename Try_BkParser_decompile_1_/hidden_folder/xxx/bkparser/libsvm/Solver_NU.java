package libsvm;

final class Solver_NU extends Solver {
   private Solver.SolutionInfo si;

   Solver_NU() {
   }

   void Solve(int l, QMatrix Q, double[] p, byte[] y, double[] alpha, double Cp, double Cn, double eps, Solver.SolutionInfo si, int shrinking) {
      this.si = si;
      super.Solve(l, Q, p, y, alpha, Cp, Cn, eps, si, shrinking);
   }

   int select_working_set(int[] working_set) {
      double Gmaxp = -1.0D / 0.0;
      double Gmaxp2 = -1.0D / 0.0;
      int Gmaxp_idx = -1;
      double Gmaxn = -1.0D / 0.0;
      double Gmaxn2 = -1.0D / 0.0;
      int Gmaxn_idx = -1;
      int Gmin_idx = -1;
      double obj_diff_min = 1.0D / 0.0;

      int ip;
      for(ip = 0; ip < this.active_size; ++ip) {
         if (this.y[ip] == 1) {
            if (!this.is_upper_bound(ip) && -this.G[ip] >= Gmaxp) {
               Gmaxp = -this.G[ip];
               Gmaxp_idx = ip;
            }
         } else if (!this.is_lower_bound(ip) && this.G[ip] >= Gmaxn) {
            Gmaxn = this.G[ip];
            Gmaxn_idx = ip;
         }
      }

      ip = Gmaxp_idx;
      int in = Gmaxn_idx;
      float[] Q_ip = null;
      float[] Q_in = null;
      if (Gmaxp_idx != -1) {
         Q_ip = this.Q.get_Q(Gmaxp_idx, this.active_size);
      }

      if (Gmaxn_idx != -1) {
         Q_in = this.Q.get_Q(Gmaxn_idx, this.active_size);
      }

      for(int j = 0; j < this.active_size; ++j) {
         double grad_diff;
         double obj_diff;
         double quad_coef;
         if (this.y[j] == 1) {
            if (!this.is_lower_bound(j)) {
               grad_diff = Gmaxp + this.G[j];
               if (this.G[j] >= Gmaxp2) {
                  Gmaxp2 = this.G[j];
               }

               if (grad_diff > 0.0D) {
                  quad_coef = this.QD[ip] + this.QD[j] - (double)(2.0F * Q_ip[j]);
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
            grad_diff = Gmaxn - this.G[j];
            if (-this.G[j] >= Gmaxn2) {
               Gmaxn2 = -this.G[j];
            }

            if (grad_diff > 0.0D) {
               quad_coef = this.QD[in] + this.QD[j] - (double)(2.0F * Q_in[j]);
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

      if (Math.max(Gmaxp + Gmaxp2, Gmaxn + Gmaxn2) < this.eps) {
         return 1;
      } else {
         if (this.y[Gmin_idx] == 1) {
            working_set[0] = Gmaxp_idx;
         } else {
            working_set[0] = Gmaxn_idx;
         }

         working_set[1] = Gmin_idx;
         return 0;
      }
   }

   private boolean be_shrunk(int i, double Gmax1, double Gmax2, double Gmax3, double Gmax4) {
      if (this.is_upper_bound(i)) {
         if (this.y[i] == 1) {
            return -this.G[i] > Gmax1;
         } else {
            return -this.G[i] > Gmax4;
         }
      } else if (this.is_lower_bound(i)) {
         if (this.y[i] == 1) {
            return this.G[i] > Gmax2;
         } else {
            return this.G[i] > Gmax3;
         }
      } else {
         return false;
      }
   }

   void do_shrinking() {
      double Gmax1 = -1.0D / 0.0;
      double Gmax2 = -1.0D / 0.0;
      double Gmax3 = -1.0D / 0.0;
      double Gmax4 = -1.0D / 0.0;

      int i;
      for(i = 0; i < this.active_size; ++i) {
         if (!this.is_upper_bound(i)) {
            if (this.y[i] == 1) {
               if (-this.G[i] > Gmax1) {
                  Gmax1 = -this.G[i];
               }
            } else if (-this.G[i] > Gmax4) {
               Gmax4 = -this.G[i];
            }
         }

         if (!this.is_lower_bound(i)) {
            if (this.y[i] == 1) {
               if (this.G[i] > Gmax2) {
                  Gmax2 = this.G[i];
               }
            } else if (this.G[i] > Gmax3) {
               Gmax3 = this.G[i];
            }
         }
      }

      if (!this.unshrink && Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= this.eps * 10.0D) {
         this.unshrink = true;
         this.reconstruct_gradient();
         this.active_size = this.l;
      }

      for(i = 0; i < this.active_size; ++i) {
         if (this.be_shrunk(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
            --this.active_size;

            while(this.active_size > i) {
               if (!this.be_shrunk(this.active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                  this.swap_index(i, this.active_size);
                  break;
               }

               --this.active_size;
            }
         }
      }

   }

   double calculate_rho() {
      int nr_free1 = 0;
      int nr_free2 = 0;
      double ub1 = 1.0D / 0.0;
      double ub2 = 1.0D / 0.0;
      double lb1 = -1.0D / 0.0;
      double lb2 = -1.0D / 0.0;
      double sum_free1 = 0.0D;
      double sum_free2 = 0.0D;

      for(int i = 0; i < this.active_size; ++i) {
         if (this.y[i] == 1) {
            if (this.is_lower_bound(i)) {
               ub1 = Math.min(ub1, this.G[i]);
            } else if (this.is_upper_bound(i)) {
               lb1 = Math.max(lb1, this.G[i]);
            } else {
               ++nr_free1;
               sum_free1 += this.G[i];
            }
         } else if (this.is_lower_bound(i)) {
            ub2 = Math.min(ub2, this.G[i]);
         } else if (this.is_upper_bound(i)) {
            lb2 = Math.max(lb2, this.G[i]);
         } else {
            ++nr_free2;
            sum_free2 += this.G[i];
         }
      }

      double r1;
      if (nr_free1 > 0) {
         r1 = sum_free1 / (double)nr_free1;
      } else {
         r1 = (ub1 + lb1) / 2.0D;
      }

      double r2;
      if (nr_free2 > 0) {
         r2 = sum_free2 / (double)nr_free2;
      } else {
         r2 = (ub2 + lb2) / 2.0D;
      }

      this.si.r = (r1 + r2) / 2.0D;
      return (r1 - r2) / 2.0D;
   }
}
