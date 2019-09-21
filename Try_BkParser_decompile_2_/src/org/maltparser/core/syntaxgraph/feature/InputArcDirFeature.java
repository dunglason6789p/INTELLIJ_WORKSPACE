/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class InputArcDirFeature
implements FeatureFunction {
    public static final Class<?>[] paramTypes = new Class[]{String.class, AddressFunction.class};
    private ColumnDescription column;
    private final DataFormatInstance dataFormatInstance;
    private final SymbolTableHandler tableHandler;
    private SymbolTable table;
    private final SingleFeatureValue featureValue;
    private AddressFunction addressFunction;

    public InputArcDirFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
        this.dataFormatInstance = dataFormatInstance;
        this.tableHandler = tableHandler;
        this.featureValue = new SingleFeatureValue(this);
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 2) {
            throw new FeatureException("Could not initialize InputArcDirFeature: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof String)) {
            throw new FeatureException("Could not initialize InputArcDirFeature: the first argument is not a string. ");
        }
        if (!(arguments[1] instanceof AddressFunction)) {
            throw new FeatureException("Could not initialize InputArcDirFeature: the second argument is not an address function. ");
        }
        this.setColumn(this.dataFormatInstance.getColumnDescriptionByName((String)arguments[0]));
        this.setSymbolTable(this.tableHandler.addSymbolTable("ARCDIR_" + this.column.getName(), 1, 1, "one"));
        this.table.addSymbol("LEFT");
        this.table.addSymbol("RIGHT");
        this.table.addSymbol("ROOT");
        this.setAddressFunction((AddressFunction)arguments[1]);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public int getCode(String symbol) throws MaltChainedException {
        return this.table.getSymbolStringToCode(symbol);
    }

    @Override
    public String getSymbol(int code) throws MaltChainedException {
        return this.table.getSymbolCodeToString(code);
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.featureValue;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    @Override
    public void update() throws MaltChainedException {
        a = this.addressFunction.getAddressValue();
        if (a.getAddress() != null && a.getAddressClass() == DependencyNode.class) {
            node = (DependencyNode)a.getAddress();
            try {
                index = Integer.parseInt(node.getLabelSymbol(this.tableHandler.getSymbolTable(this.column.getName())));
                if (node.isRoot()) {
                    this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.ROOT_NODE));
                    this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.ROOT_NODE));
                    this.featureValue.setNullValue(true);
                }
                if (index == 0) {
                    this.featureValue.setIndexCode(this.table.getSymbolStringToCode("ROOT"));
                    this.featureValue.setSymbol("ROOT");
                    this.featureValue.setNullValue(false);
                }
                if (index < node.getIndex()) {
                    this.featureValue.setIndexCode(this.table.getSymbolStringToCode("LEFT"));
                    this.featureValue.setSymbol("LEFT");
                    this.featureValue.setNullValue(false);
                }
                if (index <= node.getIndex()) ** GOTO lbl31
                this.featureValue.setIndexCode(this.table.getSymbolStringToCode("RIGHT"));
                this.featureValue.setSymbol("RIGHT");
                this.featureValue.setNullValue(false);
            }
            catch (NumberFormatException e) {
                throw new FeatureException("The index of the feature must be an integer value. ", e);
            }
        } else {
            this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
            this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
            this.featureValue.setNullValue(true);
        }
lbl31: // 6 sources:
        this.featureValue.setValue(1.0);
    }

    public AddressFunction getAddressFunction() {
        return this.addressFunction;
    }

    public void setAddressFunction(AddressFunction addressFunction) {
        this.addressFunction = addressFunction;
    }

    public ColumnDescription getColumn() {
        return this.column;
    }

    public void setColumn(ColumnDescription column) throws MaltChainedException {
        if (column.getType() != 2) {
            throw new FeatureException("InputArc feature column must be of type integer. ");
        }
        this.column = column;
    }

    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
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

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getMapIdentifier() {
        return this.getSymbolTable().getName();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputArcDirFeature)) {
            return false;
        }
        return obj.toString().equals(this.toString());
    }

    public String toString() {
        return "InputArcDir(" + this.column.getName() + ", " + this.addressFunction.toString() + ")";
    }
}

