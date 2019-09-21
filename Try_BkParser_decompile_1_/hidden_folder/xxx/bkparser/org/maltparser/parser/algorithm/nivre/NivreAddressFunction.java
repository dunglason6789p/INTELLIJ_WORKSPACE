package org.maltparser.parser.algorithm.nivre;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.ParsingException;

public final class NivreAddressFunction extends AddressFunction {
   public static final Class<?>[] paramTypes = new Class[]{Integer.class};
   private final String subFunctionName;
   private final NivreAddressFunction.NivreSubFunction subFunction;
   private final AlgoritmInterface parsingAlgorithm;
   private int index;

   public NivreAddressFunction(String _subFunctionName, AlgoritmInterface _parsingAlgorithm) {
      this.subFunctionName = _subFunctionName;
      this.subFunction = NivreAddressFunction.NivreSubFunction.valueOf(this.subFunctionName.toUpperCase());
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
      this.update((NivreConfig)this.parsingAlgorithm.getCurrentParserConfiguration());
   }

   public void update(Object[] arguments) throws MaltChainedException {
      if (this.subFunction == NivreAddressFunction.NivreSubFunction.STACK) {
         this.address.setAddress(((NivreConfig)arguments[0]).getStackNode(this.index));
      } else if (this.subFunction == NivreAddressFunction.NivreSubFunction.INPUT) {
         this.address.setAddress(((NivreConfig)arguments[0]).getInputNode(this.index));
      } else {
         this.address.setAddress((Object)null);
      }

   }

   private void update(NivreConfig config) throws MaltChainedException {
      if (this.subFunction == NivreAddressFunction.NivreSubFunction.STACK) {
         this.address.setAddress(config.getStackNode(this.index));
      } else if (this.subFunction == NivreAddressFunction.NivreSubFunction.INPUT) {
         this.address.setAddress(config.getInputNode(this.index));
      } else {
         this.address.setAddress((Object)null);
      }

   }

   public String getSubFunctionName() {
      return this.subFunctionName;
   }

   public NivreAddressFunction.NivreSubFunction getSubFunction() {
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
         NivreAddressFunction other = (NivreAddressFunction)obj;
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
      StringBuilder sb = new StringBuilder();
      sb.append(this.subFunctionName);
      sb.append('[');
      sb.append(this.index);
      sb.append(']');
      return sb.toString();
   }

   public static enum NivreSubFunction {
      STACK,
      INPUT;

      private NivreSubFunction() {
      }
   }
}
