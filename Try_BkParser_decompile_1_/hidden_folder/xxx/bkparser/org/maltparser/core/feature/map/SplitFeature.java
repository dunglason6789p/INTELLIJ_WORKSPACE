package org.maltparser.core.feature.map;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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

public final class SplitFeature implements FeatureMapFunction {
   public static final Class<?>[] paramTypes = new Class[]{FeatureFunction.class, String.class};
   private FeatureFunction parentFeature;
   private final MultipleFeatureValue multipleFeatureValue;
   private final DataFormatInstance dataFormatInstance;
   private final SymbolTableHandler tableHandler;
   private ColumnDescription column;
   private SymbolTable table;
   private String separators;
   private Pattern separatorsPattern;

   public SplitFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      this.dataFormatInstance = dataFormatInstance;
      this.tableHandler = tableHandler;
      this.multipleFeatureValue = new MultipleFeatureValue(this);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 2) {
         throw new FeatureException("Could not initialize SplitFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof FeatureFunction)) {
         throw new FeatureException("Could not initialize SplitFeature: the first argument is not a feature. ");
      } else if (!(arguments[1] instanceof String)) {
         throw new FeatureException("Could not initialize SplitFeature: the second argument is not a string. ");
      } else {
         this.setParentFeature((FeatureFunction)arguments[0]);
         this.setSeparators((String)arguments[1]);
         ColumnDescription parentColumn = this.dataFormatInstance.getColumnDescriptionByName(this.parentFeature.getSymbolTable().getName());
         if (parentColumn.getType() != 1) {
            throw new FeatureException("Could not initialize SplitFeature: the first argument must be a string. ");
         } else {
            this.setColumn(this.dataFormatInstance.addInternalColumnDescription(this.tableHandler, "SPLIT_" + this.parentFeature.getSymbolTable().getName(), parentColumn));
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

   public String getSymbol(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public int getCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public void update() throws MaltChainedException {
      this.multipleFeatureValue.reset();
      this.parentFeature.update();
      FunctionValue value = this.parentFeature.getFeatureValue();
      if (value instanceof SingleFeatureValue) {
         String symbol = ((SingleFeatureValue)value).getSymbol();
         if (((FeatureValue)value).isNullValue()) {
            this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(symbol), symbol);
            this.multipleFeatureValue.setNullValue(true);
         } else {
            String[] items;
            try {
               items = this.separatorsPattern.split(symbol);
            } catch (PatternSyntaxException var7) {
               throw new FeatureException("The split feature '" + this.toString() + "' could not split the value using the following separators '" + this.separators + "'", var7);
            }

            for(int i = 0; i < items.length; ++i) {
               if (items[i].length() > 0) {
                  this.multipleFeatureValue.addFeatureValue(this.table.addSymbol(items[i]), items[i]);
               }
            }

            this.multipleFeatureValue.setNullValue(false);
         }
      } else if (value instanceof MultipleFeatureValue) {
         if (((MultipleFeatureValue)value).isNullValue()) {
            this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(((MultipleFeatureValue)value).getFirstSymbol()), ((MultipleFeatureValue)value).getFirstSymbol());
            this.multipleFeatureValue.setNullValue(true);
         } else {
            Iterator i$ = ((MultipleFeatureValue)value).getSymbols().iterator();

            while(i$.hasNext()) {
               String symbol = (String)i$.next();

               String[] items;
               try {
                  items = this.separatorsPattern.split(symbol);
               } catch (PatternSyntaxException var6) {
                  throw new FeatureException("The split feature '" + this.toString() + "' could not split the value using the following separators '" + this.separators + "'", var6);
               }

               for(int i = 0; i < items.length; ++i) {
                  this.multipleFeatureValue.addFeatureValue(this.table.addSymbol(items[i]), items[i]);
               }

               this.multipleFeatureValue.setNullValue(false);
            }
         }
      }

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

   public FeatureFunction getParentFeature() {
      return this.parentFeature;
   }

   public void setParentFeature(FeatureFunction parentFeature) {
      this.parentFeature = parentFeature;
   }

   public String getSeparators() {
      return this.separators;
   }

   public void setSeparators(String separators) {
      this.separators = separators;
      this.separatorsPattern = Pattern.compile(separators);
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

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Split(");
      sb.append(this.parentFeature.toString());
      sb.append(", ");
      sb.append(this.separators);
      sb.append(')');
      return sb.toString();
   }
}
