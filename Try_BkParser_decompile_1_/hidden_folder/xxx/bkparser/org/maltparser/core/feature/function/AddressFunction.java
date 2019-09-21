package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.value.AddressValue;

public abstract class AddressFunction implements Function {
   protected final AddressValue address = new AddressValue(this);

   public AddressFunction() {
   }

   public abstract void update(Object[] var1) throws MaltChainedException;

   public AddressValue getAddressValue() {
      return this.address;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : this.address.equals(((AddressFunction)obj).getAddressValue());
      }
   }

   public String toString() {
      return this.address.toString();
   }
}
