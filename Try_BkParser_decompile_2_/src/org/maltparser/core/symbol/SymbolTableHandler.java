/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.TableHandler;

public interface SymbolTableHandler
extends TableHandler {
    @Override
    public SymbolTable addSymbolTable(String var1) throws MaltChainedException;

    public SymbolTable addSymbolTable(String var1, SymbolTable var2) throws MaltChainedException;

    public SymbolTable addSymbolTable(String var1, int var2, int var3, String var4) throws MaltChainedException;

    @Override
    public SymbolTable getSymbolTable(String var1) throws MaltChainedException;

    public Set<String> getSymbolTableNames();

    public void cleanUp();

    public void save(OutputStreamWriter var1) throws MaltChainedException;

    public void save(String var1, String var2) throws MaltChainedException;

    public void load(InputStreamReader var1) throws MaltChainedException;

    public void load(String var1, String var2) throws MaltChainedException;

    public SymbolTable loadTagset(String var1, String var2, String var3, int var4, int var5, String var6) throws MaltChainedException;
}

