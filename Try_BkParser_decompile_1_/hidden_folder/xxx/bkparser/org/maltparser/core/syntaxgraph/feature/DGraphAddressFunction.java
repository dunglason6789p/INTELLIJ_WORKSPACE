package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class DGraphAddressFunction extends AddressFunction {
   public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class};
   private AddressFunction addressFunction;
   private final String subFunctionName;
   private final DGraphAddressFunction.DGraphSubFunction subFunction;

   public DGraphAddressFunction(String _subFunctionName) {
      this.subFunctionName = _subFunctionName;
      this.subFunction = DGraphAddressFunction.DGraphSubFunction.valueOf(this.subFunctionName.toUpperCase());
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 1) {
         throw new SyntaxGraphException("Could not initialize DGraphAddressFunction: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize DGraphAddressFunction: the second argument is not an addres function. ");
      } else {
         this.addressFunction = (AddressFunction)arguments[0];
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public void update() throws MaltChainedException {
      AddressValue a = this.addressFunction.getAddressValue();
      if (a.getAddress() == null) {
         this.address.setAddress((Object)null);
      } else {
         DependencyNode node = (DependencyNode)a.getAddress();
         if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.HEAD && !node.isRoot()) {
            this.address.setAddress(node.getHead());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.LDEP) {
            this.address.setAddress(node.getLeftmostDependent());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.RDEP) {
            this.address.setAddress(node.getRightmostDependent());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.RDEP2) {
            if (!node.isRoot()) {
               this.address.setAddress(node.getRightmostDependent());
            } else {
               this.address.setAddress((Object)null);
            }
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.LSIB) {
            this.address.setAddress(node.getSameSideLeftSibling());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.RSIB) {
            this.address.setAddress(node.getSameSideRightSibling());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.PRED && !node.isRoot()) {
            this.address.setAddress(node.getPredecessor());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.SUCC && !node.isRoot()) {
            this.address.setAddress(node.getSuccessor());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.ANC) {
            this.address.setAddress(node.getAncestor());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.PANC) {
            this.address.setAddress(node.getProperAncestor());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.LDESC) {
            this.address.setAddress(node.getLeftmostDescendant());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.PLDESC) {
            this.address.setAddress(node.getLeftmostProperDescendant());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.RDESC) {
            this.address.setAddress(node.getRightmostDescendant());
         } else if (this.subFunction == DGraphAddressFunction.DGraphSubFunction.PRDESC) {
            this.address.setAddress(node.getRightmostProperDescendant());
         } else {
            this.address.setAddress((Object)null);
         }
      }

   }

   public void update(Object[] arguments) throws MaltChainedException {
      this.update();
   }

   public AddressFunction getAddressFunction() {
      return this.addressFunction;
   }

   public String getSubFunctionName() {
      return this.subFunctionName;
   }

   public DGraphAddressFunction.DGraphSubFunction getSubFunction() {
      return this.subFunction;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else if (!this.addressFunction.equals(((DGraphAddressFunction)obj).getAddressFunction())) {
         return false;
      } else {
         return this.subFunction.equals(((DGraphAddressFunction)obj).getSubFunction());
      }
   }

   public String toString() {
      return this.subFunctionName + "(" + this.addressFunction.toString() + ")";
   }

   public static enum DGraphSubFunction {
      HEAD,
      LDEP,
      RDEP,
      RDEP2,
      LSIB,
      RSIB,
      PRED,
      SUCC,
      ANC,
      PANC,
      LDESC,
      PLDESC,
      RDESC,
      PRDESC;

      private DGraphSubFunction() {
      }
   }
}
