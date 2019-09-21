package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class InputArcFeature implements FeatureFunction {
   public static final Class<?>[] paramTypes = new Class[]{String.class, AddressFunction.class, AddressFunction.class};
   private AddressFunction addressFunction1;
   private AddressFunction addressFunction2;
   private ColumnDescription column;
   private final DataFormatInstance dataFormatInstance;
   private final SymbolTableHandler tableHandler;
   private SymbolTable table;
   private final SingleFeatureValue featureValue;

   public InputArcFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      this.dataFormatInstance = dataFormatInstance;
      this.tableHandler = tableHandler;
      this.featureValue = new SingleFeatureValue(this);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 3) {
         throw new FeatureException("Could not initialize InputArcFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof String)) {
         throw new FeatureException("Could not initialize InputArcFeature: the first argument is not a string. ");
      } else if (!(arguments[1] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize InputArcFeature: the second argument is not an address function. ");
      } else if (!(arguments[2] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize InputArcFeature: the third argument is not an address function. ");
      } else {
         this.setAddressFunction1((AddressFunction)arguments[1]);
         this.setAddressFunction2((AddressFunction)arguments[2]);
         this.setColumn(this.dataFormatInstance.getColumnDescriptionByName((String)arguments[0]));
         this.setSymbolTable(this.tableHandler.addSymbolTable("ARC_" + this.column.getName(), 1, 1, "one"));
         this.table.addSymbol("LEFT");
         this.table.addSymbol("RIGHT");
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public int getCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public FeatureValue getFeatureValue() {
      return this.featureValue;
   }

   public String getSymbol(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public void updateCardinality() throws MaltChainedException {
   }

   public void update() throws MaltChainedException {
      AddressValue arg1 = this.addressFunction1.getAddressValue();
      AddressValue arg2 = this.addressFunction2.getAddressValue();
      if (arg1.getAddress() != null && arg1.getAddressClass() == DependencyNode.class && arg2.getAddress() != null && arg2.getAddressClass() == DependencyNode.class) {
         DependencyNode node1 = (DependencyNode)arg1.getAddress();
         DependencyNode node2 = (DependencyNode)arg2.getAddress();

         try {
            SymbolTable symbolTable = this.tableHandler.getSymbolTable(this.column.getName());
            int head1 = Integer.parseInt(node1.getLabelSymbol(symbolTable));
            int head2 = Integer.parseInt(node2.getLabelSymbol(symbolTable));
            if (!node1.isRoot() && head1 == node2.getIndex()) {
               this.featureValue.setIndexCode(this.table.getSymbolStringToCode("LEFT"));
               this.featureValue.setSymbol("LEFT");
               this.featureValue.setNullValue(false);
            } else if (!node2.isRoot() && head2 == node1.getIndex()) {
               this.featureValue.setIndexCode(this.table.getSymbolStringToCode("RIGHT"));
               this.featureValue.setSymbol("RIGHT");
               this.featureValue.setNullValue(false);
            } else {
               this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
               this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
               this.featureValue.setNullValue(true);
            }
         } catch (NumberFormatException var8) {
            throw new FeatureException("The index of the feature must be an integer value. ", var8);
         }
      } else {
         this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
         this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
         this.featureValue.setNullValue(true);
      }

      this.featureValue.setValue(1.0D);
   }

   public ColumnDescription getColumn() {
      return this.column;
   }

   public void setColumn(ColumnDescription column) throws MaltChainedException {
      if (column.getType() != 2) {
         throw new FeatureException("InputArc feature column must be of type integer. ");
      } else {
         this.column = column;
      }
   }

   public AddressFunction getAddressFunction1() {
      return this.addressFunction1;
   }

   public void setAddressFunction1(AddressFunction addressFunction1) {
      this.addressFunction1 = addressFunction1;
   }

   public AddressFunction getAddressFunction2() {
      return this.addressFunction2;
   }

   public void setAddressFunction2(AddressFunction addressFunction2) {
      this.addressFunction2 = addressFunction2;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public SymbolTable getSymbolTable() {
      return this.table;
   }

   public void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public SymbolTableHandler getTableHandler() {
      return this.tableHandler;
   }

   public int getType() {
      return 1;
   }

   public String getMapIdentifier() {
      return this.getSymbolTable().getName();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof InputArcFeature)) {
         return false;
      } else {
         return obj.toString().equals(this.toString());
      }
   }

   public String toString() {
      return "InputArc(" + this.column.getName() + ")";
   }
}
