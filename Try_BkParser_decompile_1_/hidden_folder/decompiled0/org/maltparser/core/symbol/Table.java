/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;

public interface Table {
    public int addSymbol(String var1) throws MaltChainedException;

    public String getSymbolCodeToString(int var1) throws MaltChainedException;

    public int getSymbolStringToCode(String var1) throws MaltChainedException;

    public double getSymbolStringToValue(String var1) throws MaltChainedException;

    public String getName();

    public int size();
}

