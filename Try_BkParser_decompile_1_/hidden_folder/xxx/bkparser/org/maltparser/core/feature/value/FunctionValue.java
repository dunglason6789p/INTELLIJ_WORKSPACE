package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;

public abstract class FunctionValue {
   protected Function function;

   public FunctionValue(Function function) {
      this.setFunction(function);
   }

   public Function getFunction() {
      return this.function;
   }

   public void setFunction(Function function) {
      this.function = function;
   }

   public abstract void reset();

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : this.function.equals(((FunctionValue)obj).function);
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.function.toString());
      sb.append(':');
      return sb.toString();
   }
}
