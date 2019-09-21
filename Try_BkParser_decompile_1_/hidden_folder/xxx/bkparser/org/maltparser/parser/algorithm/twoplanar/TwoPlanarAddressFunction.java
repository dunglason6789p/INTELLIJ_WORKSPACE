package org.maltparser.parser.algorithm.twoplanar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.ParsingException;

public final class TwoPlanarAddressFunction extends AddressFunction {
   public static final Class<?>[] paramTypes = new Class[]{Integer.class};
   private final String subFunctionName;
   private final TwoPlanarAddressFunction.TwoPlanarSubFunction subFunction;
   private final AlgoritmInterface parsingAlgorithm;
   private int index;

   public TwoPlanarAddressFunction(String _subFunctionName, AlgoritmInterface _parsingAlgorithm) {
      this.subFunctionName = _subFunctionName;
      this.subFunction = TwoPlanarAddressFunction.TwoPlanarSubFunction.valueOf(this.subFunctionName.toUpperCase());
      this.parsingAlgorithm = _parsingAlgorithm;
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 1) {
         throw new ParsingException("Could not initialize " + this.getClass().getName() + ": number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof Integer)) {
         throw new ParsingException("Could not initialize " + this.getClass().getName() + ": the first argument is not an integer. ");
      } else {
         this.setIndex((Integer)arguments[0]);
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public void update() throws MaltChainedException {
      this.update((TwoPlanarConfig)this.parsingAlgorithm.getCurrentParserConfiguration());
   }

   public void update(Object[] arguments) throws MaltChainedException {
      if (arguments.length == 1 && arguments[0] instanceof TwoPlanarConfig) {
         this.update((TwoPlanarConfig)arguments[0]);
      } else {
         throw new ParsingException("Arguments to the two-planar address function are not correct. ");
      }
   }

   private void update(TwoPlanarConfig config) throws MaltChainedException {
      if (this.subFunction == TwoPlanarAddressFunction.TwoPlanarSubFunction.ACTIVESTACK) {
         this.address.setAddress(config.getActiveStackNode(this.index));
      } else if (this.subFunction == TwoPlanarAddressFunction.TwoPlanarSubFunction.INACTIVESTACK) {
         this.address.setAddress(config.getInactiveStackNode(this.index));
      } else if (this.subFunction == TwoPlanarAddressFunction.TwoPlanarSubFunction.INPUT) {
         this.address.setAddress(config.getInputNode(this.index));
      } else {
         this.address.setAddress((Object)null);
      }

   }

   public String getSubFunctionName() {
      return this.subFunctionName;
   }

   public TwoPlanarAddressFunction.TwoPlanarSubFunction getSubFunction() {
      return this.subFunction;
   }

   public AddressValue getAddressValue() {
      return this.address;
   }

   public int getIndex() {
      return this.index;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         TwoPlanarAddressFunction other = (TwoPlanarAddressFunction)obj;
         if (this.index != other.index) {
            return false;
         } else {
            if (this.parsingAlgorithm == null) {
               if (other.parsingAlgorithm != null) {
                  return false;
               }
            } else if (!this.parsingAlgorithm.equals(other.parsingAlgorithm)) {
               return false;
            }

            if (this.subFunction == null) {
               if (other.subFunction != null) {
                  return false;
               }
            } else if (!this.subFunction.equals(other.subFunction)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      return this.subFunctionName + "[" + this.index + "]";
   }

   public static enum TwoPlanarSubFunction {
      ACTIVESTACK,
      INACTIVESTACK,
      INPUT;

      private TwoPlanarSubFunction() {
      }
   }
}
