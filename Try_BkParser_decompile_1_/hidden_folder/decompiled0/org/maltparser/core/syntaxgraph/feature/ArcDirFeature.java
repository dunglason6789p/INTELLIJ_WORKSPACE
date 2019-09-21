/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class ArcDirFeature
implements FeatureFunction {
    public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class};
    private AddressFunction addressFunction;
    private final SymbolTableHandler tableHandler;
    private SymbolTable table;
    private final SingleFeatureValue featureValue;

    public ArcDirFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
        this.tableHandler = tableHandler;
        this.featureValue = new SingleFeatureValue(this);
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 1) {
            throw new SyntaxGraphException("Could not initialize ArcDirFeature: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof AddressFunction)) {
            throw new SyntaxGraphException("Could not initialize ArcDirFeature: the first argument is not an address function. ");
        }
        this.setAddressFunction((AddressFunction)arguments[0]);
        this.setSymbolTable(this.tableHandler.addSymbolTable("ARCDIR", 1, 1, "one"));
        this.table.addSymbol("LEFT");
        this.table.addSymbol("RIGHT");
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
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
        if (this.addressFunction.getAddressValue().getAddress() != null) {
            DependencyNode node = (DependencyNode)this.addressFunction.getAddressValue().getAddress();
            if (!node.isRoot()) {
                if (node.getHead().getIndex() < node.getIndex()) {
                    this.featureValue.setIndexCode(this.table.getSymbolStringToCode("LEFT"));
                    this.featureValue.setValue(1.0);
                    this.featureValue.setSymbol("LEFT");
                    this.featureValue.setNullValue(false);
                } else {
                    this.featureValue.setIndexCode(this.table.getSymbolStringToCode("RIGHT"));
                    this.featureValue.setValue(1.0);
                    this.featureValue.setSymbol("RIGHT");
                    this.featureValue.setNullValue(false);
                }
            } else {
                this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.ROOT_NODE));
                this.featureValue.setValue(1.0);
                this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.ROOT_NODE));
                this.featureValue.setNullValue(true);
            }
        } else {
            this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
            this.featureValue.setValue(1.0);
            this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
            this.featureValue.setNullValue(true);
        }
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.featureValue;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.table;
    }

    public AddressFunction getAddressFunction() {
        return this.addressFunction;
    }

    public void setAddressFunction(AddressFunction addressFunction) {
        this.addressFunction = addressFunction;
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

    public void setSymbolTable(SymbolTable table) {
        this.table = table;
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

    public int hashCode() {
        return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArcDir(");
        sb.append(this.addressFunction.toString());
        sb.append(')');
        return sb.toString();
    }
}
