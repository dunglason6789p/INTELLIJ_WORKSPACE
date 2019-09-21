package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;

public class AddressValue extends FunctionValue {
   private Object address;

   public AddressValue(Function function) {
      super(function);
      this.setAddress((Object)null);
   }

   public void reset() {
      this.setAddress((Object)null);
   }

   public Class<?> getAddressClass() {
      return this.address != null ? this.address.getClass() : null;
   }

   public Object getAddress() {
      return this.address;
   }

   public void setAddress(Object address) {
      this.address = address;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         AddressValue other = (AddressValue)obj;
         if (this.address == null) {
            if (other.address != null) {
               return false;
            }
         } else if (!this.address.equals(other.address)) {
            return false;
         }

         return super.equals(obj);
      }
   }

   public int hashCode() {
      return 31 + (this.address == null ? 0 : this.address.hashCode());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append(this.address.toString());
      return sb.toString();
   }
}
