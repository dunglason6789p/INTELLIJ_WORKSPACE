/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.map;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.FeatureMapFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;

public final class SplitFeature
implements FeatureMapFunction {
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

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 2) {
            throw new FeatureException("Could not initialize SplitFeature: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof FeatureFunction)) {
            throw new FeatureException("Could not initialize SplitFeature: the first argument is not a feature. ");
        }
        if (!(arguments[1] instanceof String)) {
            throw new FeatureException("Could not initialize SplitFeature: the second argument is not a string. ");
        }
        this.setParentFeature((FeatureFunction)arguments[0]);
        this.setSeparators((String)arguments[1]);
        ColumnDescription parentColumn = this.dataFormatInstance.getColumnDescriptionByName(this.parentFeature.getSymbolTable().getName());
        if (parentColumn.getType() != 1) {
            throw new FeatureException("Could not initialize SplitFeature: the first argument must be a string. ");
        }
        this.setColumn(this.dataFormatInstance.addInternalColumnDescription(this.tableHandler, "SPLIT_" + this.parentFeature.getSymbolTable().getName(), parentColumn));
        this.setSymbolTable(this.tableHandler.getSymbolTable(this.column.getName()));
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.multipleFeatureValue;
    }

    @Override
    public String getSymbol(int code) throws MaltChainedException {
        return this.table.getSymbolCodeToString(code);
    }

    @Override
    public int getCode(String symbol) throws MaltChainedException {
        return this.table.getSymbolStringToCode(symbol);
    }

    @Override
    public void update() throws MaltChainedException {
        this.multipleFeatureValue.reset();
        this.parentFeature.update();
        FeatureValue value = this.parentFeature.getFeatureValue();
        if (value instanceof SingleFeatureValue) {
            String symbol = ((SingleFeatureValue)value).getSymbol();
            if (value.isNullValue()) {
                this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(symbol), symbol);
                this.multipleFeatureValue.setNullValue(true);
            } else {
                String[] items;
                try {
                    items = this.separatorsPattern.split(symbol);
                }
                catch (PatternSyntaxException e) {
                    throw new FeatureException("The split feature '" + this.toString() + "' could not split the value using the following separators '" + this.separators + "'", e);
                }
                for (int i = 0; i < items.length; ++i) {
                    if (items[i].length() <= 0) continue;
                    this.multipleFeatureValue.addFeatureValue(this.table.addSymbol(items[i]), items[i]);
                }
                this.multipleFeatureValue.setNullValue(false);
            }
        } else if (value instanceof MultipleFeatureValue) {
            if (((MultipleFeatureValue)value).isNullValue()) {
                this.multipleFeatureValue.addFeatureValue(this.parentFeature.getSymbolTable().getSymbolStringToCode(((MultipleFeatureValue)value).getFirstSymbol()), ((MultipleFeatureValue)value).getFirstSymbol());
                this.multipleFeatureValue.setNullValue(true);
            } else {
                for (String symbol : ((MultipleFeatureValue)value).getSymbols()) {
                    String[] items;
                    try {
                        items = this.separatorsPattern.split(symbol);
                    }
                    catch (PatternSyntaxException e) {
                        throw new FeatureException("The split feature '" + this.toString() + "' could not split the value using the following separators '" + this.separators + "'", e);
                    }
                    for (int i = 0; i < items.length; ++i) {
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
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return obj.toString().equals(this.toString());
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

    @Override
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

    @Override
    public int getType() {
        return this.column.getType();
    }

    @Override
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

