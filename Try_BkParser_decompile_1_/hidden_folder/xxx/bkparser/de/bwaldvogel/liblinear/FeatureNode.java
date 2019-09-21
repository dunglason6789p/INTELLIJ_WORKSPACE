package de.bwaldvogel.liblinear;

public class FeatureNode {
   public final int index;
   public double value;

   public FeatureNode(int index, double value) {
      if (index < 0) {
         throw new IllegalArgumentException("index must be >= 0");
      } else {
         this.index = index;
         this.value = value;
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.index;
      long temp = Double.doubleToLongBits(this.value);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         FeatureNode other = (FeatureNode)obj;
         if (this.index != other.index) {
            return false;
         } else {
            return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
         }
      }
   }

   public String toString() {
      return "FeatureNode(idx=" + this.index + ", value=" + this.value + ")";
   }
}
