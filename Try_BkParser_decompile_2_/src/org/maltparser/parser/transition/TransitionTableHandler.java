/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.transition;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;
import org.maltparser.parser.transition.TransitionTable;

public class TransitionTableHandler
implements TableHandler {
    private final HashMap<String, TransitionTable> transitionTables = new HashMap();

    @Override
    public Table addSymbolTable(String tableName) throws MaltChainedException {
        TransitionTable table = this.transitionTables.get(tableName);
        if (table == null) {
            table = new TransitionTable(tableName);
            this.transitionTables.put(tableName, table);
        }
        return table;
    }

    @Override
    public Table getSymbolTable(String tableName) throws MaltChainedException {
        return this.transitionTables.get(tableName);
    }
}

