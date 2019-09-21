package org.maltparser.ml.liblinear;

public class XNode implements Comparable<XNode> {
   private int index;
   private double value;

   public XNode(int index, double value) {
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
         XNode other = (XNode)obj;
         if (this.index != other.index) {
            return false;
         } else {
            return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
         }
      }
   }

   public int compareTo(XNode aThat) {
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
      return "XNode [index=" + this.index + ", value=" + this.value + "]";
   }
}
