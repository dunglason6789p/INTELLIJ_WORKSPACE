/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.map;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.FeatureMapFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;

public final class Merge3Feature
implements FeatureMapFunction {
    public static final Class<?>[] paramTypes = new Class[]{FeatureFunction.class, FeatureFunction.class, FeatureFunction.class};
    private FeatureFunction firstFeature;
    private FeatureFunction secondFeature;
    private FeatureFunction thirdFeature;
    private final SymbolTableHandler tableHandler;
    private SymbolTable table;
    private final SingleFeatureValue singleFeatureValue;
    private int type;

    public Merge3Feature(SymbolTableHandler tableHandler) throws MaltChainedException {
        this.tableHandler = tableHandler;
        this.singleFeatureValue = new SingleFeatureValue(this);
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 3) {
            throw new FeatureException("Could not initialize Merge3Feature: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof FeatureFunction)) {
            throw new FeatureException("Could not initialize Merge3Feature: the first argument is not a feature. ");
        }
        if (!(arguments[1] instanceof FeatureFunction)) {
            throw new FeatureException("Could not initialize Merge3Feature: the second argument is not a feature. ");
        }
        if (!(arguments[2] instanceof FeatureFunction)) {
            throw new FeatureException("Could not initialize Merge3Feature: the third argument is not a feature. ");
        }
        this.setFirstFeature((FeatureFunction)arguments[0]);
        this.setSecondFeature((FeatureFunction)arguments[1]);
        this.setThirdFeature((FeatureFunction)arguments[2]);
        if (this.firstFeature.getType() != this.secondFeature.getType() || this.firstFeature.getType() != this.thirdFeature.getType()) {
            throw new FeatureException("Could not initialize MergeFeature: the arguments are not of the same type.");
        }
        this.type = this.firstFeature.getType();
        String name = "MERGE3_" + this.firstFeature.getMapIdentifier() + "_" + this.secondFeature.getMapIdentifier() + "_" + this.thirdFeature.getMapIdentifier();
        this.setSymbolTable(this.tableHandler.addSymbolTable(name, 1, 1, "One"));
    }

    @Override
    public void update() throws MaltChainedException {
        this.singleFeatureValue.reset();
        this.firstFeature.update();
        this.secondFeature.update();
        this.thirdFeature.update();
        FeatureValue firstValue = this.firstFeature.getFeatureValue();
        FeatureValue secondValue = this.secondFeature.getFeatureValue();
        FeatureValue thirdValue = this.thirdFeature.getFeatureValue();
        if (firstValue.isMultiple() || secondValue.isMultiple() || thirdValue.isMultiple()) {
            throw new FeatureException("It is not possible to merge Split-features. ");
        }
        String firstSymbol = ((SingleFeatureValue)firstValue).getSymbol();
        if (firstValue.isNullValue() && secondValue.isNullValue() && thirdValue.isNullValue()) {
            this.singleFeatureValue.setIndexCode(this.firstFeature.getSymbolTable().getSymbolStringToCode(firstSymbol));
            this.singleFeatureValue.setSymbol(firstSymbol);
            this.singleFeatureValue.setNullValue(true);
        } else if (this.getType() == 1) {
            StringBuilder mergedValue = new StringBuilder();
            mergedValue.append(((SingleFeatureValue)firstValue).getSymbol());
            mergedValue.append('~');
            mergedValue.append(((SingleFeatureValue)secondValue).getSymbol());
            mergedValue.append('~');
            mergedValue.append(((SingleFeatureValue)thirdValue).getSymbol());
            this.singleFeatureValue.setIndexCode(this.table.addSymbol(mergedValue.toString()));
            this.singleFeatureValue.setSymbol(mergedValue.toString());
            this.singleFeatureValue.setNullValue(false);
            this.singleFeatureValue.setValue(1.0);
        } else if (firstValue.isNullValue() || secondValue.isNullValue() || thirdValue.isNullValue()) {
            this.singleFeatureValue.setValue(0.0);
            this.table.addSymbol("#null#");
            this.singleFeatureValue.setSymbol("#null#");
            this.singleFeatureValue.setNullValue(true);
            this.singleFeatureValue.setIndexCode(1);
        } else {
            if (this.getType() == 3) {
                boolean result = false;
                int dotIndex = firstSymbol.indexOf(46);
                boolean bl = result = firstSymbol.equals("1") || firstSymbol.equals("true") || firstSymbol.equals("#true#") || dotIndex != -1 && firstSymbol.substring(0, dotIndex).equals("1");
                if (result) {
                    String secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();
                    dotIndex = secondSymbol.indexOf(46);
                    boolean bl2 = result = secondSymbol.equals("1") || secondSymbol.equals("true") || secondSymbol.equals("#true#") || dotIndex != -1 && secondSymbol.substring(0, dotIndex).equals("1");
                }
                if (result) {
                    String thirdSymbol = ((SingleFeatureValue)thirdValue).getSymbol();
                    dotIndex = thirdSymbol.indexOf(46);
                    boolean bl3 = result = thirdSymbol.equals("1") || thirdSymbol.equals("true") || thirdSymbol.equals("#true#") || dotIndex != -1 && thirdSymbol.substring(0, dotIndex).equals("1");
                }
                if (result) {
                    this.singleFeatureValue.setValue(1.0);
                    this.table.addSymbol("true");
                    this.singleFeatureValue.setSymbol("true");
                } else {
                    this.singleFeatureValue.setValue(0.0);
                    this.table.addSymbol("false");
                    this.singleFeatureValue.setSymbol("false");
                }
            } else if (this.getType() == 2) {
                Integer firstInt = 0;
                Integer secondInt = 0;
                Integer thirdInt = 0;
                try {
                    int dotIndex = firstSymbol.indexOf(46);
                    firstInt = dotIndex == -1 ? Integer.valueOf(Integer.parseInt(firstSymbol)) : Integer.valueOf(Integer.parseInt(firstSymbol.substring(0, dotIndex)));
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + firstSymbol + "' to integer value.", e);
                }
                String secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();
                try {
                    int dotIndex = secondSymbol.indexOf(46);
                    secondInt = dotIndex == -1 ? Integer.valueOf(Integer.parseInt(secondSymbol)) : Integer.valueOf(Integer.parseInt(secondSymbol.substring(0, dotIndex)));
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + secondSymbol + "' to integer value.", e);
                }
                String thirdSymbol = ((SingleFeatureValue)thirdValue).getSymbol();
                try {
                    int dotIndex = thirdSymbol.indexOf(46);
                    secondInt = dotIndex == -1 ? Integer.valueOf(Integer.parseInt(thirdSymbol)) : Integer.valueOf(Integer.parseInt(thirdSymbol.substring(0, dotIndex)));
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + thirdSymbol + "' to integer value.", e);
                }
                Integer result = firstInt * secondInt * thirdInt;
                this.singleFeatureValue.setValue(result.intValue());
                this.table.addSymbol(result.toString());
                this.singleFeatureValue.setSymbol(result.toString());
            } else if (this.getType() == 4) {
                Double firstReal = 0.0;
                Double secondReal = 0.0;
                Double thirdReal = 0.0;
                try {
                    firstReal = Double.parseDouble(firstSymbol);
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + firstSymbol + "' to real value.", e);
                }
                String secondSymbol = ((SingleFeatureValue)secondValue).getSymbol();
                try {
                    secondReal = Double.parseDouble(secondSymbol);
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + secondSymbol + "' to real value.", e);
                }
                String thirdSymbol = ((SingleFeatureValue)thirdValue).getSymbol();
                try {
                    thirdReal = Double.parseDouble(thirdSymbol);
                }
                catch (NumberFormatException e) {
                    throw new FeatureException("Could not cast the feature value '" + thirdSymbol + "' to real value.", e);
                }
                Double result = firstReal * secondReal * thirdReal;
                this.singleFeatureValue.setValue(result);
                this.table.addSymbol(result.toString());
                this.singleFeatureValue.setSymbol(result.toString());
            }
            this.singleFeatureValue.setNullValue(false);
            this.singleFeatureValue.setIndexCode(1);
        }
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.singleFeatureValue;
    }

    @Override
    public String getSymbol(int code) throws MaltChainedException {
        return this.table.getSymbolCodeToString(code);
    }

    @Override
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

    public FeatureFunction getThirdFeature() {
        return this.thirdFeature;
    }

    public void setThirdFeature(FeatureFunction thirdFeature) {
        this.thirdFeature = thirdFeature;
    }

    public SymbolTableHandler getTableHandler() {
        return this.tableHandler;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.table;
    }

    public void setSymbolTable(SymbolTable table) {
        this.table = table;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public String getMapIdentifier() {
        return this.getSymbolTable().getName();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return obj.toString().equals(this.toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Merge3(");
        sb.append(this.firstFeature.toString());
        sb.append(", ");
        sb.append(this.secondFeature.toString());
        sb.append(", ");
        sb.append(this.thirdFeature.toString());
        sb.append(')');
        return sb.toString();
    }
}

