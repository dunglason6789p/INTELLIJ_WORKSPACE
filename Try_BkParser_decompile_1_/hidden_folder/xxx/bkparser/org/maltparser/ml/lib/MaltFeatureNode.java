package org.maltparser.ml.lib;

public class MaltFeatureNode implements Comparable<MaltFeatureNode> {
   int index;
   double value;

   public MaltFeatureNode() {
      this.index = -1;
      this.value = 0.0D;
   }

   public MaltFeatureNode(int index, double value) {
      this.setIndex(index);
      this.setValue(value);
   }

   public int getIndex() {
      return this.index;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   public void clear() {
      this.index = -1;
      this.value = 0.0D;
   }

   public int hashCode() {
      int prime = true;
      long temp = Double.doubleToLongBits(this.value);
      return 31 * (31 + this.index) + (int)(temp ^ temp >>> 32);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MaltFeatureNode other = (MaltFeatureNode)obj;
         if (this.index != other.index) {
            return false;
         } else {
            return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
         }
      }
   }

   public int compareTo(MaltFeatureNode aThat) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == aThat) {
         return 0;
      } else if (this.index < aThat.index) {
         return -1;
      } else if (this.index > aThat.index) {
         return 1;
      } else if (this.value < aThat.value) {
         return -1;
      } else {
         return this.value > aThat.value ? 1 : 0;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("MaltFeatureNode [index=");
      sb.append(this.index);
      sb.append(", value=");
      sb.append(this.value);
      sb.append("]");
      return sb.toString();
   }
}
