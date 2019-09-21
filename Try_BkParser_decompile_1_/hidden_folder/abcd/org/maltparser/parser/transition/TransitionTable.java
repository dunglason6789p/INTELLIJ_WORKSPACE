/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.transition;

import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.Table;
import org.maltparser.parser.history.container.DecisionPropertyTable;
import org.maltparser.parser.transition.Transition;

public class TransitionTable
implements Table,
DecisionPropertyTable {
    private final String name;
    private final SortedMap<Integer, Transition> code2transitionMap;
    private final HashMap<String, Transition> symbol2transitionMap;
    private final HashMap<Transition, TransitionTable> childrenTables;

    public TransitionTable(String tableName) {
        this.name = tableName;
        this.code2transitionMap = new TreeMap<Integer, Transition>();
        this.symbol2transitionMap = new HashMap();
        this.childrenTables = new HashMap();
    }

    public void addTransition(int code, String symbol, boolean labeled, TransitionTable childrenTable) {
        Transition transition = new Transition(code, symbol, labeled);
        this.code2transitionMap.put(code, transition);
        this.symbol2transitionMap.put(symbol, transition);
        this.childrenTables.put(transition, childrenTable);
    }

    @Override
    public boolean continueWithNextDecision(int code) throws MaltChainedException {
        if (this.code2transitionMap.containsKey(code)) {
            return ((Transition)this.code2transitionMap.get(code)).isLabeled();
        }
        return true;
    }

    @Override
    public boolean continueWithNextDecision(String symbol) throws MaltChainedException {
        if (this.symbol2transitionMap.containsKey(symbol)) {
            return this.symbol2transitionMap.get(symbol).isLabeled();
        }
        return true;
    }

    @Override
    public Table getTableForNextDecision(int code) throws MaltChainedException {
        if (this.code2transitionMap.containsKey(code)) {
            return this.childrenTables.get(this.code2transitionMap.get(code));
        }
        return null;
    }

    @Override
    public Table getTableForNextDecision(String symbol) throws MaltChainedException {
        if (this.symbol2transitionMap.containsKey(symbol)) {
            return this.childrenTables.get(this.symbol2transitionMap.get(symbol));
        }
        return null;
    }

    public Transition getTransition(String symbol) {
        return this.symbol2transitionMap.get(symbol);
    }

    public Transition getTransition(int code) {
        return (Transition)this.code2transitionMap.get(code);
    }

    @Override
    public int addSymbol(String symbol) throws MaltChainedException {
        return -1;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbolCodeToString(int code) throws MaltChainedException {
        if (code < 0) {
            return null;
        }
        return ((Transition)this.code2transitionMap.get(code)).getSymbol();
    }

    @Override
    public int getSymbolStringToCode(String symbol) throws MaltChainedException {
        if (symbol == null) {
            return -1;
        }
        return this.symbol2transitionMap.get(symbol).getCode();
    }

    @Override
    public double getSymbolStringToValue(String symbol) throws MaltChainedException {
        return 1.0;
    }

    @Override
    public int size() {
        return this.code2transitionMap.size();
    }
}

