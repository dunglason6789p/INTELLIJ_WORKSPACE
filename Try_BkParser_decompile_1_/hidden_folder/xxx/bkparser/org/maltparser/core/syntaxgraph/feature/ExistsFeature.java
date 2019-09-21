package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;

public final class ExistsFeature implements FeatureFunction {
   public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class};
   private AddressFunction addressFunction;
   private final SymbolTableHandler tableHandler;
   private SymbolTable table;
   private final SingleFeatureValue featureValue;

   public ExistsFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
      this.tableHandler = tableHandler;
      this.featureValue = new SingleFeatureValue(this);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 1) {
         throw new SyntaxGraphException("Could not initialize ExistsFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize ExistsFeature: the first argument is not an address function. ");
      } else {
         this.setAddressFunction((AddressFunction)arguments[0]);
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public String getSymbol(int code) throws MaltChainedException {
      return code == 1 ? "true" : "false";
   }

   public int getCode(String symbol) throws MaltChainedException {
      return symbol.equals("true") ? 1 : 0;
   }

   public void update() throws MaltChainedException {
      this.featureValue.setIndexCode(1);
      this.featureValue.setNullValue(false);
      if (this.addressFunction.getAddressValue().getAddress() != null) {
         this.featureValue.setSymbol("true");
         this.featureValue.setValue(1.0D);
      } else {
         this.featureValue.setSymbol("false");
         this.featureValue.setValue(0.0D);
      }

   }

   public FeatureValue getFeatureValue() {
      return this.featureValue;
   }

   public SymbolTable getSymbolTable() {
      return this.table;
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

   public void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public int getType() {
      return 3;
   }

   public String getMapIdentifier() {
      return "EXISTS";
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
      sb.append("Exists(");
      sb.append(this.addressFunction.toString());
      sb.append(')');
      return sb.toString();
   }
}
