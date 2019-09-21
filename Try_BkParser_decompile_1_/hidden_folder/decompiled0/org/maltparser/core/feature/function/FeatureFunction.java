/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.symbol.SymbolTable;

public interface FeatureFunction
extends Function {
    public String getSymbol(int var1) throws MaltChainedException;

    public int getCode(String var1) throws MaltChainedException;

    public SymbolTable getSymbolTable();

    public FeatureValue getFeatureValue();

    public int getType();

    public String getMapIdentifier();
}

