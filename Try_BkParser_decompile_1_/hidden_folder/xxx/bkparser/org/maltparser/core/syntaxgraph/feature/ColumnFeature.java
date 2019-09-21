package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Modifiable;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.NullValues;

public abstract class ColumnFeature implements FeatureFunction, Modifiable {
   protected ColumnDescription column;
   protected SymbolTable symbolTable;
   protected final SingleFeatureValue featureValue = new SingleFeatureValue(this);

   public ColumnFeature() throws MaltChainedException {
   }

   public abstract void update() throws MaltChainedException;

   public abstract void initialize(Object[] var1) throws MaltChainedException;

   public abstract Class<?>[] getParameterTypes();

   public String getSymbol(int value) throws MaltChainedException {
      return this.symbolTable.getSymbolCodeToString(value);
   }

   public int getCode(String value) throws MaltChainedException {
      return this.symbolTable.getSymbolStringToCode(value);
   }

   public ColumnDescription getColumn() {
      return this.column;
   }

   protected void setColumn(ColumnDescription column) {
      this.column = column;
   }

   public SymbolTable getSymbolTable() {
      return this.symbolTable;
   }

   protected void setSymbolTable(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
   }

   public void setFeatureValue(int indexCode) throws MaltChainedException {
      String symbol = this.symbolTable.getSymbolCodeToString(indexCode);
      if (symbol == null) {
         this.featureValue.update(indexCode, this.symbolTable.getNullValueSymbol(NullValues.NullValueId.NO_NODE), true, 1.0D);
      } else {
         boolean nullValue = this.symbolTable.isNullValue(indexCode);
         if (this.column.getType() != 1 && !nullValue) {
            this.castFeatureValue(symbol);
         } else {
            this.featureValue.update(indexCode, symbol, nullValue, 1.0D);
         }
      }

   }

   public void setFeatureValue(String symbol) throws MaltChainedException {
      int indexCode = this.symbolTable.getSymbolStringToCode(symbol);
      if (indexCode < 0) {
         this.featureValue.update(this.symbolTable.getNullValueCode(NullValues.NullValueId.NO_NODE), symbol, true, 1.0D);
      } else {
         boolean nullValue = this.symbolTable.isNullValue(symbol);
         if (this.column.getType() != 1 && !nullValue) {
            this.castFeatureValue(symbol);
         } else {
            this.featureValue.update(indexCode, symbol, nullValue, 1.0D);
         }
      }

   }

   protected void castFeatureValue(String symbol) throws MaltChainedException {
      int dotIndex;
      if (this.column.getType() == 2) {
         try {
            dotIndex = symbol.indexOf(46);
            if (dotIndex == -1) {
               this.featureValue.setValue((double)Integer.parseInt(symbol));
               this.featureValue.setSymbol(symbol);
            } else {
               this.featureValue.setValue((double)Integer.parseInt(symbol.substring(0, dotIndex)));
               this.featureValue.setSymbol(symbol.substring(0, dotIndex));
            }

            this.featureValue.setNullValue(false);
            this.featureValue.setIndexCode(1);
         } catch (NumberFormatException var4) {
            throw new FeatureException("Could not cast the feature value '" + symbol + "' to integer value.", var4);
         }
      } else if (this.column.getType() != 3) {
         if (this.column.getType() == 4) {
            try {
               this.featureValue.setValue(Double.parseDouble(symbol));
               this.featureValue.setSymbol(symbol);
            } catch (NumberFormatException var3) {
               throw new FeatureException("Could not cast the feature value '" + symbol + "' to real value.", var3);
            }

            this.featureValue.setNullValue(false);
            this.featureValue.setIndexCode(1);
         }
      } else {
         dotIndex = symbol.indexOf(46);
         if (!symbol.equals("1") && !symbol.equals("true") && !symbol.equals("#true#") && (dotIndex == -1 || !symbol.substring(0, dotIndex).equals("1"))) {
            if (!symbol.equals("false") && !symbol.equals("0") && (dotIndex == -1 || !symbol.substring(0, dotIndex).equals("0"))) {
               throw new FeatureException("Could not cast the feature value '" + symbol + "' to boolean value.");
            }

            this.featureValue.setValue(0.0D);
            this.featureValue.setSymbol("false");
         } else {
            this.featureValue.setValue(1.0D);
            this.featureValue.setSymbol("true");
         }

         this.featureValue.setNullValue(false);
         this.featureValue.setIndexCode(1);
      }

   }

   public FeatureValue getFeatureValue() {
      return this.featureValue;
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

   public String getColumnName() {
      return this.column.getName();
   }

   public int getType() {
      return this.column.getType();
   }

   public String getMapIdentifier() {
      return this.getSymbolTable().getName();
   }

   public String toString() {
      return this.column.getName();
   }
}
