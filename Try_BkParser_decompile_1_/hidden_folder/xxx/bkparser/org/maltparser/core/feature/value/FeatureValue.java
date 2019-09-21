package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;

public abstract class FeatureValue extends FunctionValue {
   protected boolean nullValue;

   public FeatureValue(Function function) {
      super(function);
      this.setNullValue(true);
   }

   public void reset() {
      this.setNullValue(true);
   }

   public boolean isNullValue() {
      return this.nullValue;
   }

   public void setNullValue(boolean nullValue) {
      this.nullValue = nullValue;
   }

   public abstract boolean isMultiple();

   public abstract int nFeatureValues();

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : super.equals(obj);
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append("[null=");
      sb.append(this.nullValue);
      sb.append("]");
      return sb.toString();
   }
}
