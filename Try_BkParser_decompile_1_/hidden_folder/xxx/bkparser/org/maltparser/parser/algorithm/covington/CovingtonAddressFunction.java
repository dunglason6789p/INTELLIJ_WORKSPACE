package org.maltparser.parser.algorithm.covington;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.ParsingException;

public final class CovingtonAddressFunction extends AddressFunction {
   public static final Class<?>[] paramTypes = new Class[]{Integer.class};
   private final String subFunctionName;
   private final CovingtonAddressFunction.CovingtonSubFunction subFunction;
   private final AlgoritmInterface parsingAlgorithm;
   private int index;

   public CovingtonAddressFunction(String _subFunctionName, AlgoritmInterface _parsingAlgorithm) {
      this.subFunctionName = _subFunctionName;
      this.subFunction = CovingtonAddressFunction.CovingtonSubFunction.valueOf(this.subFunctionName.toUpperCase());
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
      this.update((CovingtonConfig)this.parsingAlgorithm.getCurrentParserConfiguration());
   }

   public void update(Object[] arguments) throws MaltChainedException {
      this.update((CovingtonConfig)arguments[0]);
   }

   private void update(CovingtonConfig config) throws MaltChainedException {
      if (this.subFunction == CovingtonAddressFunction.CovingtonSubFunction.LEFT) {
         this.address.setAddress(config.getLeftNode(this.index));
      } else if (this.subFunction == CovingtonAddressFunction.CovingtonSubFunction.RIGHT) {
         this.address.setAddress(config.getRightNode(this.index));
      } else if (this.subFunction == CovingtonAddressFunction.CovingtonSubFunction.LEFTCONTEXT) {
         this.address.setAddress(config.getLeftContextNode(this.index));
      } else if (this.subFunction == CovingtonAddressFunction.CovingtonSubFunction.RIGHTCONTEXT) {
         this.address.setAddress(config.getRightContextNode(this.index));
      } else {
         this.address.setAddress((Object)null);
      }

   }

   public String getSubFunctionName() {
      return this.subFunctionName;
   }

   public CovingtonAddressFunction.CovingtonSubFunction getSubFunction() {
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
         CovingtonAddressFunction other = (CovingtonAddressFunction)obj;
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

   public static enum CovingtonSubFunction {
      LEFT,
      RIGHT,
      LEFTCONTEXT,
      RIGHTCONTEXT;

      private CovingtonSubFunction() {
      }
   }
}
