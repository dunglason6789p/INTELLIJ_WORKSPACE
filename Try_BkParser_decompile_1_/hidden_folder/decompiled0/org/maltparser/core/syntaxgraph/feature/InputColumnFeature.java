/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.feature.ColumnFeature;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class InputColumnFeature
extends ColumnFeature {
    public static final Class<?>[] paramTypes = new Class[]{String.class, AddressFunction.class};
    private final DataFormatInstance dataFormatInstance;
    private final SymbolTableHandler tableHandler;
    private AddressFunction addressFunction;

    public InputColumnFeature(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
        this.dataFormatInstance = dataFormatInstance;
        this.tableHandler = tableHandler;
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 2) {
            throw new SyntaxGraphException("Could not initialize InputColumnFeature: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof String)) {
            throw new SyntaxGraphException("Could not initialize InputColumnFeature: the first argument is not a string. ");
        }
        if (!(arguments[1] instanceof AddressFunction)) {
            throw new SyntaxGraphException("Could not initialize InputColumnFeature: the second argument is not an address function. ");
        }
        ColumnDescription column = this.dataFormatInstance.getColumnDescriptionByName((String)arguments[0]);
        if (column == null) {
            throw new SyntaxGraphException("Could not initialize InputColumnFeature: the input column type '" + (String)arguments[0] + "' could not be found in the data format specification. ' ");
        }
        this.setColumn(column);
        this.setSymbolTable(this.tableHandler.getSymbolTable(column.getName()));
        this.setAddressFunction((AddressFunction)arguments[1]);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public void update() throws MaltChainedException {
        AddressValue a = this.addressFunction.getAddressValue();
        if (a.getAddress() == null) {
            this.featureValue.update(this.symbolTable.getNullValueCode(NullValues.NullValueId.NO_NODE), this.symbolTable.getNullValueSymbol(NullValues.NullValueId.NO_NODE), true, 1.0);
        } else {
            DependencyNode node = (DependencyNode)a.getAddress();
            if (!node.isRoot()) {
                int indexCode = node.getLabelCode(this.symbolTable);
                if (this.column.getType() == 1) {
                    this.featureValue.update(indexCode, this.symbolTable.getSymbolCodeToString(indexCode), false, 1.0);
                } else {
                    this.castFeatureValue(this.symbolTable.getSymbolCodeToString(indexCode));
                }
            } else {
                this.featureValue.update(this.symbolTable.getNullValueCode(NullValues.NullValueId.ROOT_NODE), this.symbolTable.getNullValueSymbol(NullValues.NullValueId.ROOT_NODE), true, 1.0);
            }
        }
    }

    public AddressFunction getAddressFunction() {
        return this.addressFunction;
    }

    public void setAddressFunction(AddressFunction addressFunction) {
        this.addressFunction = addressFunction;
    }

    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
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

    public int hashCode() {
        return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InputColumn(");
        sb.append(super.toString());
        sb.append(", ");
        sb.append(this.addressFunction.toString());
        sb.append(")");
        return sb.toString();
    }
}

