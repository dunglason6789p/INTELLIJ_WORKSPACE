package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Modifiable;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.nullvalue.NullValues;

public abstract class TableFeature implements FeatureFunction, Modifiable {
   protected final SingleFeatureValue featureValue;
   protected SymbolTable table;
   protected String tableName;
   protected SymbolTableHandler tableHandler;
   protected int type;

   public TableFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
      this.tableHandler = tableHandler;
      this.featureValue = new SingleFeatureValue(this);
   }

   public abstract void update() throws MaltChainedException;

   public abstract void initialize(Object[] var1) throws MaltChainedException;

   public abstract Class<?>[] getParameterTypes();

   public String getSymbol(int value) throws MaltChainedException {
      return this.table.getSymbolCodeToString(value);
   }

   public int getCode(String value) throws MaltChainedException {
      return this.table.getSymbolStringToCode(value);
   }

   public SymbolTable getSymbolTable() {
      return this.table;
   }

   public void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public void setFeatureValue(int indexCode) throws MaltChainedException {
      if (this.table.getSymbolCodeToString(indexCode) == null) {
         this.featureValue.setIndexCode(indexCode);
         this.featureValue.setValue(1.0D);
         this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
         this.featureValue.setNullValue(true);
      } else {
         this.featureValue.setIndexCode(indexCode);
         this.featureValue.setValue(1.0D);
         this.featureValue.setSymbol(this.table.getSymbolCodeToString(indexCode));
         this.featureValue.setNullValue(this.table.isNullValue(indexCode));
      }

   }

   public void setFeatureValue(String symbol) throws MaltChainedException {
      if (this.table.getSymbolStringToCode(symbol) < 0) {
         this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
         this.featureValue.setValue(1.0D);
         this.featureValue.setSymbol(symbol);
         this.featureValue.setNullValue(true);
      } else {
         this.featureValue.setIndexCode(this.table.getSymbolStringToCode(symbol));
         this.featureValue.setValue(1.0D);
         this.featureValue.setSymbol(symbol);
         this.featureValue.setNullValue(this.table.isNullValue(symbol));
      }

   }

   public FeatureValue getFeatureValue() {
      return this.featureValue;
   }

   public SymbolTableHandler getTableHandler() {
      return this.tableHandler;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof TableFeature)) {
         return false;
      } else {
         return obj.toString().equals(this.toString());
      }
   }

   public void setTableName(String name) {
      this.tableName = name;
   }

   public String getTableName() {
      return this.tableName;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getMapIdentifier() {
      return this.getSymbolTable().getName();
   }

   public String toString() {
      return this.tableName;
   }
}
