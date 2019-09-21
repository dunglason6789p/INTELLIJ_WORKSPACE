/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Map;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;

public class ParseSymbolTable
implements SymbolTable {
    private final String name;
    private final SymbolTable parentSymbolTable;
    private final int type;
    private final Map<String, Integer> symbolCodeMap;
    private final Map<Integer, String> codeSymbolMap;
    private final Map<String, Double> symbolValueMap;
    private int valueCounter;

    public ParseSymbolTable(String _name, int _category, int _type, String nullValueStrategy, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
        this.name = _name;
        this.type = _type;
        this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(this.name, _category, _type, nullValueStrategy);
        this.symbolCodeMap = new HashMap<String, Integer>();
        this.codeSymbolMap = new HashMap<Integer, String>();
        this.symbolValueMap = new HashMap<String, Double>();
        this.valueCounter = -1;
    }

    public ParseSymbolTable(String _name, SymbolTable parentTable, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
        this.name = _name;
        this.type = 1;
        this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(this.name, parentTable);
        this.symbolCodeMap = new HashMap<String, Integer>();
        this.codeSymbolMap = new HashMap<Integer, String>();
        this.symbolValueMap = new HashMap<String, Double>();
        this.valueCounter = -1;
    }

    public ParseSymbolTable(String name, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
        this.name = name;
        this.type = 1;
        this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(name);
        this.symbolCodeMap = new HashMap<String, Integer>();
        this.codeSymbolMap = new HashMap<Integer, String>();
        this.symbolValueMap = new HashMap<String, Double>();
        this.valueCounter = -1;
    }

    @Override
    public int addSymbol(String symbol) throws MaltChainedException {
        if (!this.parentSymbolTable.isNullValue(symbol)) {
            if (symbol == null || symbol.length() == 0) {
                throw new SymbolException("Symbol table error: empty string cannot be added to the symbol table");
            }
            int code = this.parentSymbolTable.getSymbolStringToCode(symbol);
            if (code > -1) {
                return code;
            }
            if (this.type == 4) {
                this.addSymbolValue(symbol);
            }
            if (!this.symbolCodeMap.containsKey(symbol)) {
                this.valueCounter = this.valueCounter == -1 ? this.parentSymbolTable.getValueCounter() + 1 : ++this.valueCounter;
                this.symbolCodeMap.put(symbol, this.valueCounter);
                this.codeSymbolMap.put(this.valueCounter, symbol);
                return this.valueCounter;
            }
            return this.symbolCodeMap.get(symbol);
        }
        return this.parentSymbolTable.getSymbolStringToCode(symbol);
    }

    public double addSymbolValue(String symbol) throws MaltChainedException {
        if (!this.symbolValueMap.containsKey(symbol)) {
            Double value = Double.valueOf(symbol);
            this.symbolValueMap.put(symbol, value);
            return value;
        }
        return this.symbolValueMap.get(symbol);
    }

    @Override
    public String getSymbolCodeToString(int code) throws MaltChainedException {
        if (code < 0) {
            throw new SymbolException("The symbol code '" + code + "' cannot be found in the symbol table. ");
        }
        String symbol = this.parentSymbolTable.getSymbolCodeToString(code);
        if (symbol != null) {
            return symbol;
        }
        return this.codeSymbolMap.get(code);
    }

    @Override
    public int getSymbolStringToCode(String symbol) throws MaltChainedException {
        if (symbol == null) {
            throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
        }
        int code = this.parentSymbolTable.getSymbolStringToCode(symbol);
        if (code > -1) {
            return code;
        }
        Integer item = this.symbolCodeMap.get(symbol);
        if (item == null) {
            throw new SymbolException("Could not find the symbol '" + symbol + "' in the symbol table. ");
        }
        return item;
    }

    @Override
    public double getSymbolStringToValue(String symbol) throws MaltChainedException {
        if (symbol == null) {
            throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
        }
        double value = this.parentSymbolTable.getSymbolStringToValue(symbol);
        if (value != Double.NaN) {
            return value;
        }
        Double item = this.symbolValueMap.get(symbol);
        if (item == null) {
            throw new SymbolException("Could not find the symbol '" + symbol + "' in the symbol table. ");
        }
        return item;
    }

    public void clearTmpStorage() {
        this.symbolCodeMap.clear();
        this.codeSymbolMap.clear();
        this.symbolValueMap.clear();
        this.valueCounter = -1;
    }

    @Override
    public int size() {
        return this.parentSymbolTable.size();
    }

    @Override
    public void save(BufferedWriter out) throws MaltChainedException {
        this.parentSymbolTable.save(out);
    }

    @Override
    public void load(BufferedReader in) throws MaltChainedException {
        this.parentSymbolTable.load(in);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getValueCounter() {
        return this.parentSymbolTable.getValueCounter();
    }

    @Override
    public int getNullValueCode(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
        return this.parentSymbolTable.getNullValueCode(nullValueIdentifier);
    }

    @Override
    public String getNullValueSymbol(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
        return this.parentSymbolTable.getNullValueSymbol(nullValueIdentifier);
    }

    @Override
    public boolean isNullValue(String symbol) throws MaltChainedException {
        return this.parentSymbolTable.isNullValue(symbol);
    }

    @Override
    public boolean isNullValue(int code) throws MaltChainedException {
        return this.parentSymbolTable.isNullValue(code);
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
        ParseSymbolTable other = (ParseSymbolTable)obj;
        return this.name == null ? other.name == null : this.name.equals(other.name);
    }

    public int hashCode() {
        return 217 + (null == this.name ? 0 : this.name.hashCode());
    }

    public String toString() {
        return this.parentSymbolTable.toString();
    }
}

