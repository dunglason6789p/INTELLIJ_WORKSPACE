package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.TableFeature;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class OutputTableFeature extends TableFeature {
   public static final Class<?>[] paramTypes = new Class[]{String.class, AddressFunction.class};
   private AddressFunction addressFunction;

   public OutputTableFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      super(tableHandler);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 2) {
         throw new SyntaxGraphException("Could not initialize OutputTableFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof String)) {
         throw new SyntaxGraphException("Could not initialize OutputTableFeature: the first argument is not a string. ");
      } else if (!(arguments[1] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize OutputTableFeature: the second argument is not an address function. ");
      } else {
         this.setSymbolTable(this.tableHandler.getSymbolTable((String)arguments[0]));
         this.setAddressFunction((AddressFunction)arguments[1]);
         this.setType(1);
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public void update() throws MaltChainedException {
      AddressValue a = this.addressFunction.getAddressValue();
      if (a.getAddress() == null) {
         this.featureValue.setIndexCode(this.getSymbolTable().getNullValueCode(NullValues.NullValueId.NO_NODE));
         this.featureValue.setSymbol(this.getSymbolTable().getNullValueSymbol(NullValues.NullValueId.NO_NODE));
         this.featureValue.setNullValue(true);
      } else {
         DependencyNode node = (DependencyNode)a.getAddress();
         if (!node.isRoot()) {
            if (node.hasHead()) {
               this.featureValue.setIndexCode(node.getHeadEdge().getLabelCode(this.getSymbolTable()));
               this.featureValue.setSymbol(this.getSymbolTable().getSymbolCodeToString(node.getHeadEdge().getLabelCode(this.getSymbolTable())));
               this.featureValue.setNullValue(false);
            } else {
               this.featureValue.setIndexCode(this.getSymbolTable().getNullValueCode(NullValues.NullValueId.NO_VALUE));
               this.featureValue.setSymbol(this.getSymbolTable().getNullValueSymbol(NullValues.NullValueId.NO_VALUE));
               this.featureValue.setNullValue(true);
            }
         } else {
            this.featureValue.setIndexCode(this.getSymbolTable().getNullValueCode(NullValues.NullValueId.ROOT_NODE));
            this.featureValue.setSymbol(this.getSymbolTable().getNullValueSymbol(NullValues.NullValueId.ROOT_NODE));
            this.featureValue.setNullValue(true);
         }
      }

      this.featureValue.setValue(1.0D);
   }

   public AddressFunction getAddressFunction() {
      return this.addressFunction;
   }

   public void setAddressFunction(AddressFunction addressFunction) {
      this.addressFunction = addressFunction;
   }

   public SymbolTableHandler getTableHandler() {
      return this.tableHandler;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : obj.toString().equals(this.toString());
      }
   }

   public int hashCode() {
      return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("OutputTable(");
      sb.append(super.toString());
      sb.append(", ");
      sb.append(this.addressFunction.toString());
      sb.append(")");
      return sb.toString();
   }
}
