/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;

public interface TableHandler {
    public Table getSymbolTable(String var1) throws MaltChainedException;

    public Table addSymbolTable(String var1) throws MaltChainedException;
}

