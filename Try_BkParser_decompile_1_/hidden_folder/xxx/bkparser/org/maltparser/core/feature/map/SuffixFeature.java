package org.maltparser.core.feature.map;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.FeatureMapFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.FunctionValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.feature.InputColumnFeature;

public final class SuffixFeature implements FeatureMapFunction {
   public static final Class<?>[] paramTypes = new Class[]{InputColumnFeature.class, Integer.class};
   private FeatureFunction parentFeature;
   private final MultipleFeatureValue multipleFeatureValue;
   private final SymbolTableHandler tableHandler;
   private SymbolTable table;
   private final DataFormatInstance dataFormatInstance;
   private ColumnDescription column;
   private int suffixLength;

   public SuffixFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      this.dataFormatInstance = dataFormatInstance;
      this.tableHandler = tableHandler;
      this.multipleFeatureValue = new MultipleFeatureValue(this);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 2) {
         throw new FeatureException("Could not initialize SuffixFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof FeatureFunction)) {
         throw new FeatureException("Could not initialize SuffixFeature: the first argument is not a feature. ");
      } else if (!(arguments[1] instanceof Integer)) {
         throw new FeatureException("Could not initialize SuffixFeature: the second argument is not a string. ");
      } else {
         this.setParentFeature((FeatureFunction)arguments[0]);
         this.setSuffixLength((Integer)arguments[1]);
         ColumnDescription parentColumn = this.dataFormatInstance.getColumnDescriptionByName(this.parentFeature.getSymbolTable().getName());
         if (parentColumn.getType() != 1) {
            throw new FeatureException("Could not initialize SuffixFeature: the first argument must be a string. ");
         } else {
            this.setColumn(this.dataFormatInstance.addInternalColumnDescription(this.tableHandler, "SUFFIX_" + this.suffixLength + "_" + this.parentFeature.getSymbolTable().getName(), parentColumn));
            this.setSymbolTable(this.tableHandler.getSymbolTable(this.column.getName()));
         }
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public FeatureValue getFeatureValue() {
      return this.multipleFeatureValue;
   }

   public int getCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public String getSymbol(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public void update() throws MaltChainedException {
      this.parentFeature.update();
      FunctionValue value = this.parentFeature.getFeatureValue();
      String symbol;
      if (value instanceof SingleFeatureValue) {
         String symbol = ((SingleFeatureValue)value).getSymbol();
         if (((FeatureValue)value).isNullValue()) {
            this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(symbol), symbol);
            this.multipleFeatureValue.setNullValue(true);
         } else {
            if (symbol.length() - this.suffixLength > 0) {
               symbol = symbol.substring(symbol.length() - this.suffixLength);
            } else {
               symbol = symbol;
            }

            int code = this.table.addSymbol(symbol);
            this.multipleFeatureValue.addFeatureValue(code, symbol);
            this.multipleFeatureValue.setNullValue(false);
         }
      } else if (value instanceof MultipleFeatureValue) {
         this.multipleFeatureValue.reset();
         if (((MultipleFeatureValue)value).isNullValue()) {
            this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(((MultipleFeatureValue)value).getFirstSymbol()), ((MultipleFeatureValue)value).getFirstSymbol());
            this.multipleFeatureValue.setNullValue(true);
         } else {
            Iterator i$ = ((MultipleFeatureValue)value).getSymbols().iterator();

            while(i$.hasNext()) {
               symbol = (String)i$.next();
               String suffixStr;
               if (symbol.length() - this.suffixLength > 0) {
                  suffixStr = symbol.substring(symbol.length() - this.suffixLength);
               } else {
                  suffixStr = symbol;
               }

               int code = this.table.addSymbol(suffixStr);
               this.multipleFeatureValue.addFeatureValue(code, suffixStr);
               this.multipleFeatureValue.setNullValue(true);
            }
         }
      }

   }

   public FeatureFunction getParentFeature() {
      return this.parentFeature;
   }

   public void setParentFeature(FeatureFunction feature) {
      this.parentFeature = feature;
   }

   public int getSuffixLength() {
      return this.suffixLength;
   }

   public void setSuffixLength(int suffixLength) {
      this.suffixLength = suffixLength;
   }

   public SymbolTableHandler getTableHandler() {
      return this.tableHandler;
   }

   public SymbolTable getSymbolTable() {
      return this.table;
   }

   public void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public ColumnDescription getColumn() {
      return this.column;
   }

   protected void setColumn(ColumnDescription column) {
      this.column = column;
   }

   public int getType() {
      return this.column.getType();
   }

   public String getMapIdentifier() {
      return this.getSymbolTable().getName();
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

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Suffix(");
      sb.append(this.parentFeature.toString());
      sb.append(", ");
      sb.append(this.suffixLength);
      sb.append(')');
      return sb.toString();
   }
}
