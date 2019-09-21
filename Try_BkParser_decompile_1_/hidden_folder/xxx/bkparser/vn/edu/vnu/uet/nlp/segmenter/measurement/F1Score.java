package vn.edu.vnu.uet.nlp.segmenter.measurement;

public class F1Score {
   private double P;
   private double R;
   private double F;

   public F1Score() {
      this.P = 0.0D;
      this.R = 0.0D;
      this.F = 0.0D;
   }

   public F1Score(int N1, int N2, int N3) {
      if (N1 != 0 && N2 != 0) {
         this.P = (double)N3 / (double)N1 * 100.0D;
         this.R = (double)N3 / (double)N2 * 100.0D;
         this.F = 2.0D * this.P * this.R / (this.P + this.R);
      } else {
         new F1Score();
      }

   }

   public double getPrecision() {
      return this.P;
   }

   public double getRecall() {
      return this.R;
   }

   public double getF1Score() {
      return this.F;
   }

   public String toString() {
      return this.P + "%\t" + this.R + "%\t" + this.F + "%";
   }
}
