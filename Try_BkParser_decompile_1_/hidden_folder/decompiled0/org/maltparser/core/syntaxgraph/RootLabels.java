/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;

public class RootLabels {
    public static final String DEFAULT_ROOTSYMBOL = "ROOT";
    private final LabelSet rootLabelCodes = new LabelSet();

    public void setRootLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
        block10 : {
            block11 : {
                int index;
                block12 : {
                    block9 : {
                        if (edgeSymbolTables == null) {
                            return;
                        }
                        if (rootLabelOption != null && rootLabelOption.trim().length() != 0) break block9;
                        for (SymbolTable table : edgeSymbolTables.values()) {
                            this.rootLabelCodes.put(table, table.addSymbol(DEFAULT_ROOTSYMBOL));
                        }
                        break block10;
                    }
                    if (rootLabelOption.trim().indexOf(44) != -1) break block11;
                    index = rootLabelOption.trim().indexOf(61);
                    if (index != -1) break block12;
                    for (SymbolTable table : edgeSymbolTables.values()) {
                        this.rootLabelCodes.put(table, table.addSymbol(rootLabelOption.trim()));
                    }
                    break block10;
                }
                String name = rootLabelOption.trim().substring(0, index);
                if (edgeSymbolTables.get(name) == null) {
                    throw new SyntaxGraphException("The symbol table '" + name + "' cannot be found when defining the root symbol. ");
                }
                this.rootLabelCodes.put(edgeSymbolTables.get(name), ((SymbolTable)edgeSymbolTables.get(name)).addSymbol(rootLabelOption.trim().substring(index + 1)));
                if (edgeSymbolTables.size() <= 1) break block10;
                for (SymbolTable table : edgeSymbolTables.values()) {
                    if (table.getName().equals(name)) continue;
                    this.rootLabelCodes.put(table, table.addSymbol(DEFAULT_ROOTSYMBOL));
                }
                break block10;
            }
            String[] items = rootLabelOption.trim().split(",");
            for (int i = 0; i < items.length; ++i) {
                int index = items[i].trim().indexOf(61);
                if (index == -1) {
                    throw new SyntaxGraphException("The root symbol is undefinied. ");
                }
                String name = items[i].trim().substring(0, index);
                if (edgeSymbolTables.get(name) == null) {
                    throw new SyntaxGraphException("The symbol table'" + name + "' cannot be found when defining the root symbol. ");
                }
                this.rootLabelCodes.put(edgeSymbolTables.get(name), ((SymbolTable)edgeSymbolTables.get(name)).addSymbol(items[i].trim().substring(index + 1)));
            }
            for (SymbolTable table : edgeSymbolTables.values()) {
                if (this.rootLabelCodes.containsKey(table)) continue;
                this.rootLabelCodes.put(table, table.addSymbol(DEFAULT_ROOTSYMBOL));
            }
        }
    }

    public void setDefaultRootLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
        this.rootLabelCodes.put(table, table.addSymbol(defaultRootSymbol));
    }

    public Integer getDefaultRootLabelCode(SymbolTable table) throws MaltChainedException {
        Integer res = (Integer)this.rootLabelCodes.get(table);
        if (res == null) {
            return table.addSymbol(DEFAULT_ROOTSYMBOL);
        }
        return res;
    }

    public LabelSet getDefaultRootLabels() throws MaltChainedException {
        return new LabelSet(this.rootLabelCodes);
    }

    public String getDefaultRootLabelSymbol(SymbolTable table) throws MaltChainedException {
        return table.getSymbolCodeToString(this.getDefaultRootLabelCode(table));
    }

    public boolean checkRootLabelCodes(LabelSet rlc) {
        if (rlc == null && this.rootLabelCodes == null) {
            return true;
        }
        if (rlc == null && this.rootLabelCodes != null || rlc != null && this.rootLabelCodes == null) {
            return false;
        }
        if (rlc.size() != this.rootLabelCodes.size()) {
            return false;
        }
        for (SymbolTable table : this.rootLabelCodes.keySet()) {
            if (((Integer)this.rootLabelCodes.get(table)).equals(rlc.get(table))) continue;
            return false;
        }
        return true;
    }
}

