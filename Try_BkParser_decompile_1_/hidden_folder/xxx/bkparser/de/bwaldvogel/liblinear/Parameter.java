package de.bwaldvogel.liblinear;

public final class Parameter {
   double C;
   double eps;
   SolverType solverType;
   double[] weight = null;
   int[] weightLabel = null;

   public Parameter(SolverType solverType, double C, double eps) {
      this.setSolverType(solverType);
      this.setC(C);
      this.setEps(eps);
   }

   public void setWeights(double[] weights, int[] weightLabels) {
      if (weights == null) {
         throw new IllegalArgumentException("'weight' must not be null");
      } else if (weightLabels != null && weightLabels.length == weights.length) {
         this.weightLabel = Linear.copyOf(weightLabels, weightLabels.length);
         this.weight = Linear.copyOf(weights, weights.length);
      } else {
         throw new IllegalArgumentException("'weightLabels' must have same length as 'weight'");
      }
   }

   public double[] getWeights() {
      return Linear.copyOf(this.weight, this.weight.length);
   }

   public int[] getWeightLabels() {
      return Linear.copyOf(this.weightLabel, this.weightLabel.length);
   }

   public int getNumWeights() {
      return this.weight == null ? 0 : this.weight.length;
   }

   public void setC(double C) {
      if (C <= 0.0D) {
         throw new IllegalArgumentException("C must not be <= 0");
      } else {
         this.C = C;
      }
   }

   public double getC() {
      return this.C;
   }

   public void setEps(double eps) {
      if (eps <= 0.0D) {
         throw new IllegalArgumentException("eps must not be <= 0");
      } else {
         this.eps = eps;
      }
   }

   public double getEps() {
      return this.eps;
   }

   public void setSolverType(SolverType solverType) {
      if (solverType == null) {
         throw new IllegalArgumentException("solver type must not be null");
      } else {
         this.solverType = solverType;
      }
   }

   public SolverType getSolverType() {
      return this.solverType;
   }
}
