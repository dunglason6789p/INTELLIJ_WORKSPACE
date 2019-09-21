package org.maltparser.core.feature.map;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.FeatureMapFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;

public final class MergeFeature implements FeatureMapFunction {
   public static final Class<?>[] paramTypes = new Class[]{FeatureFunction.class, FeatureFunction.class};
   private FeatureFunction firstFeature;
   private FeatureFunction secondFeature;
   private final SymbolTableHandler tableHandler;
   private SymbolTable table;
   private final SingleFeatureValue singleFeatureValue;
   private int type;

   public MergeFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
      this.tableHandler = tableHandler;
      this.singleFeatureValue = new SingleFeatureValue(this);
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 2) {
         throw new FeatureException("Could not initialize MergeFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof FeatureFunction)) {
         throw new FeatureException("Could not initialize MergeFeature: the first argument is not a feature. ");
      } else if (!(arguments[1] instanceof FeatureFunction)) {
         throw new FeatureException("Could not initialize MergeFeature: the second argument is not a feature. ");
      } else {
         this.setFirstFeature((FeatureFunction)arguments[0]);
         this.setSecondFeature((FeatureFunction)arguments[1]);
         if (this.firstFeature.getType() != this.secondFeature.getType()) {
            throw new FeatureException("Could not initialize MergeFeature: the first and the second arguments are not of the same type.");
         } else {
            this.type = this.firstFeature.getType();
            this.setSymbolTable(this.tableHandler.addSymbolTable("MERGE2_" + this.firstFeature.getMapIdentifier() + "_" + this.secondFeature.getMapIdentifier(), 1, 1, "One"));
         }
      }
   }

   public void update() throws MaltChainedException {
      this.singleFeatureValue.reset();
      this.firstFeature.update();
      this.secondFeature.update();
      FeatureValue firstValue = this.firstFeature.getFeatureValue();
      FeatureValue secondValue = this.secondFeature.getFeatureValue();
      if (!firstValue.isMultiple() && !secondValue.isMultiple()) {
         String firstSymbol = ((SingleFeatureValue)firstValue).getSymbol();
         if (firstValue.isNullValue() && secondValue.isNullValue()) {
            this.singleFeatureValue.setIndexCode(this.firstFeature.getSymbolTable().getSymbolStringToCode(firstSymbol));
            this.singleFeatureValue.setSymbol(firstSymbol);
            this.singleFeatureValue.setNullValue(true);
         } else {
            if (this.getType() == 1) {
               StringBuilder mergedValue = new StringBuilder();
               mergedValue.append(firstSymbol);
               mergedValue.append('~');
               mergedValue.append(((SingleFeatureValue)secondValue).getSymbol());
               this.singleFeatureValue.setIndexCode(this.table.addSymbol(mergedValue.toString()));
               this.singleFeatureValue.setSymbol(mergedValue.toString());
               this.singleFeatureValue.setNullValue(false);
               this.singleFeatureValue.setValue(1.0D);
            } else if (!firstValue.isNullValue() && !secondValue.isNullValue()) {
               String secondSymbol;
               if (this.getType() != 3) {
                  if (this.getType() == 2) {
                     Integer firstInt = 0;
                     Integer secondInt = 0;
                     int dotIndex = firstSymbol.indexOf(46);

                     try {
                        if (dotIndex == -1) {
                           firstInt = Integer.parseInt(firstSymbol);
                        } else {
                           firstInt = Integer.parseInt(firstSymbol.substring(0, dotIndex));
                        }
                     } catch (NumberFormatException var12) {
                        throw new FeatureException("Could not cast the feature value '" + firstSymbol + "' to integer value.", var12);
                     }

                     String secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();
                     dotIndex = secondSymbol.indexOf(46);

                     try {
                        if (dotIndex == -1) {
                           secondInt = Integer.parseInt(secondSymbol);
                        } else {
                           secondInt = Integer.parseInt(secondSymbol.substring(0, dotIndex));
                        }
                     } catch (NumberFormatException var11) {
                        throw new FeatureException("Could not cast the feature value '" + secondSymbol + "' to integer value.", var11);
                     }

                     Integer result = firstInt * secondInt;
                     this.singleFeatureValue.setValue((double)result);
                     this.table.addSymbol(result.toString());
                     this.singleFeatureValue.setSymbol(result.toString());
                  } else if (this.getType() == 4) {
                     Double firstReal = 0.0D;
                     Double secondReal = 0.0D;

                     try {
                        firstReal = Double.parseDouble(firstSymbol);
                     } catch (NumberFormatException var10) {
                        throw new FeatureException("Could not cast the feature value '" + firstSymbol + "' to real value.", var10);
                     }

                     secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();

                     try {
                        secondReal = Double.parseDouble(secondSymbol);
                     } catch (NumberFormatException var9) {
                        throw new FeatureException("Could not cast the feature value '" + secondSymbol + "' to real value.", var9);
                     }

                     Double result = firstReal * secondReal;
                     this.singleFeatureValue.setValue(result);
                     this.table.addSymbol(result.toString());
                     this.singleFeatureValue.setSymbol(result.toString());
                  }
               } else {
                  boolean result = false;
                  int dotIndex = firstSymbol.indexOf(46);
                  result = firstSymbol.equals("1") || firstSymbol.equals("true") || firstSymbol.equals("#true#") || dotIndex != -1 && firstSymbol.substring(0, dotIndex).equals("1");
                  if (result) {
                     secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();
                     dotIndex = secondSymbol.indexOf(46);
                     result = secondSymbol.equals("1") || secondSymbol.equals("true") || secondSymbol.equals("#true#") || dotIndex != -1 && secondSymbol.substring(0, dotIndex).equals("1");
                  }

                  if (result) {
                     this.singleFeatureValue.setValue(1.0D);
                     this.table.addSymbol("true");
                     this.singleFeatureValue.setSymbol("true");
                  } else {
                     this.singleFeatureValue.setValue(0.0D);
                     this.table.addSymbol("false");
                     this.singleFeatureValue.setSymbol("false");
                  }
               }

               this.singleFeatureValue.setNullValue(false);
               this.singleFeatureValue.setIndexCode(1);
            } else {
               this.singleFeatureValue.setValue(0.0D);
               this.table.addSymbol("#null#");
               this.singleFeatureValue.setSymbol("#null#");
               this.singleFeatureValue.setNullValue(true);
               this.singleFeatureValue.setIndexCode(1);
            }

         }
      } else {
         throw new FeatureException("It is not possible to merge Split-features. ");
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public FeatureValue getFeatureValue() {
      return this.singleFeatureValue;
   }

   public String getSymbol(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public int getCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public FeatureFunction getFirstFeature() {
      return this.firstFeature;
   }

   public void setFirstFeature(FeatureFunction firstFeature) {
      this.firstFeature = firstFeature;
   }

   public FeatureFunction getSecondFeature() {
      return this.secondFeature;
   }

   public void setSecondFeature(FeatureFunction secondFeature) {
      this.secondFeature = secondFeature;
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

   public int getType() {
      return this.type;
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
      sb.append("Merge(");
      sb.append(this.firstFeature.toString());
      sb.append(", ");
      sb.append(this.secondFeature.toString());
      sb.append(')');
      return sb.toString();
   }
}
