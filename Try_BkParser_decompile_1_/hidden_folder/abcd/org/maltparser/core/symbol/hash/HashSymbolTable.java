/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.hash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.InputNullValues;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.symbol.nullvalue.OutputNullValues;

public final class HashSymbolTable
implements SymbolTable {
    private final String name;
    private final Map<String, Integer> symbolCodeMap;
    private final Map<Integer, String> codeSymbolMap;
    private final Map<String, Double> symbolValueMap;
    private final NullValues nullValues;
    private final int category;
    private final int type;
    private int valueCounter;

    public HashSymbolTable(String _name, int _category, int _type, String nullValueStrategy) throws MaltChainedException {
        this.name = _name;
        this.category = _category;
        this.type = _type;
        this.symbolCodeMap = new HashMap<String, Integer>();
        this.codeSymbolMap = new HashMap<Integer, String>();
        this.symbolValueMap = new HashMap<String, Double>();
        this.nullValues = this.category == 3 ? new OutputNullValues(nullValueStrategy, this) : new InputNullValues(nullValueStrategy, this);
        this.valueCounter = this.nullValues.getNextCode();
    }

    public HashSymbolTable(String _name) {
        this.name = _name;
        this.category = -1;
        this.type = 1;
        this.symbolCodeMap = new HashMap<String, Integer>();
        this.codeSymbolMap = new HashMap<Integer, String>();
        this.symbolValueMap = new HashMap<String, Double>();
        this.nullValues = new InputNullValues("one", this);
        this.valueCounter = 1;
    }

    @Override
    public int addSymbol(String symbol) throws MaltChainedException {
        if (this.nullValues == null || !this.nullValues.isNullValue(symbol)) {
            if (symbol == null || symbol.length() == 0) {
                throw new SymbolException("Symbol table error: empty string cannot be added to the symbol table");
            }
            if (this.type == 4) {
                this.addSymbolValue(symbol);
            }
            if (!this.symbolCodeMap.containsKey(symbol)) {
                int code = this.valueCounter++;
                this.symbolCodeMap.put(symbol, code);
                this.codeSymbolMap.put(code, symbol);
                return code;
            }
            return this.symbolCodeMap.get(symbol);
        }
        return this.nullValues.symbolToCode(symbol);
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
        if (code >= 0) {
            if (this.nullValues == null || !this.nullValues.isNullValue(code)) {
                return this.codeSymbolMap.get(code);
            }
            return this.nullValues.codeToSymbol(code);
        }
        throw new SymbolException("The symbol code '" + code + "' cannot be found in the symbol table. ");
    }

    @Override
    public int getSymbolStringToCode(String symbol) throws MaltChainedException {
        if (symbol != null) {
            if (this.nullValues == null || !this.nullValues.isNullValue(symbol)) {
                Integer value = this.symbolCodeMap.get(symbol);
                return value != null ? value : -1;
            }
            return this.nullValues.symbolToCode(symbol);
        }
        throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
    }

    @Override
    public double getSymbolStringToValue(String symbol) throws MaltChainedException {
        if (symbol != null) {
            if (this.type == 4 && this.nullValues == null || !this.nullValues.isNullValue(symbol)) {
                Double value = this.symbolValueMap.get(symbol);
                return value != null ? value : Double.parseDouble(symbol);
            }
            return 1.0;
        }
        throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
    }

    public void saveHeader(BufferedWriter out) throws MaltChainedException {
        try {
            out.append('\t');
            out.append(this.getName());
            out.append('\t');
            out.append(Integer.toString(this.getCategory()));
            out.append('\t');
            out.append(Integer.toString(this.getType()));
            out.append('\t');
            out.append(this.getNullValueStrategy());
            out.append('\n');
        }
        catch (IOException e) {
            throw new SymbolException("Could not save the symbol table. ", e);
        }
    }

    public int getCategory() {
        return this.category;
    }

    public int getType() {
        return this.type;
    }

    public String getNullValueStrategy() {
        if (this.nullValues == null) {
            return null;
        }
        return this.nullValues.getNullValueStrategy();
    }

    @Override
    public int size() {
        return this.symbolCodeMap.size();
    }

    @Override
    public void save(BufferedWriter out) throws MaltChainedException {
        try {
            out.write(this.name);
            out.write(10);
            if (this.type != 4) {
                for (Integer code : this.codeSymbolMap.keySet()) {
                    out.write(Integer.toString(code));
                    out.write(9);
                    out.write(this.codeSymbolMap.get(code));
                    out.write(10);
                }
            } else {
                for (String symbol : this.symbolValueMap.keySet()) {
                    out.write(1);
                    out.write(9);
                    out.write(symbol);
                    out.write(10);
                }
            }
            out.write(10);
        }
        catch (IOException e) {
            throw new SymbolException("Could not save the symbol table. ", e);
        }
    }

    @Override
    public void load(BufferedReader in) throws MaltChainedException {
        int max = 0;
        try {
            String fileLine;
            while ((fileLine = in.readLine()) != null) {
                int index;
                if (fileLine.length() == 0 || (index = fileLine.indexOf(9)) == -1) {
                    this.valueCounter = max + 1;
                    break;
                }
                if (this.type != 4) {
                    int code;
                    try {
                        code = Integer.parseInt(fileLine.substring(0, index));
                    }
                    catch (NumberFormatException e) {
                        throw new SymbolException("The symbol table file (.sym) contains a non-integer value in the first column. ", e);
                    }
                    String symbol = fileLine.substring(index + 1);
                    this.symbolCodeMap.put(symbol, code);
                    this.codeSymbolMap.put(code, symbol);
                    if (max >= code) continue;
                    max = code;
                    continue;
                }
                String symbol = fileLine.substring(index + 1);
                this.symbolValueMap.put(symbol, Double.parseDouble(symbol));
                max = 1;
            }
        }
        catch (IOException e) {
            throw new SymbolException("Could not load the symbol table. ", e);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getValueCounter() {
        return this.valueCounter;
    }

    @Override
    public int getNullValueCode(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
        if (this.nullValues == null) {
            throw new SymbolException("The symbol table does not have any null-values. ");
        }
        return this.nullValues.nullvalueToCode(nullValueIdentifier);
    }

    @Override
    public String getNullValueSymbol(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
        if (this.nullValues == null) {
            throw new SymbolException("The symbol table does not have any null-values. ");
        }
        return this.nullValues.nullvalueToSymbol(nullValueIdentifier);
    }

    @Override
    public boolean isNullValue(String symbol) throws MaltChainedException {
        if (this.nullValues != null) {
            return this.nullValues.isNullValue(symbol);
        }
        return false;
    }

    @Override
    public boolean isNullValue(int code) throws MaltChainedException {
        if (this.nullValues != null) {
            return this.nullValues.isNullValue(code);
        }
        return false;
    }

    public Set<Integer> getCodes() {
        return this.codeSymbolMap.keySet();
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
        HashSymbolTable other = (HashSymbolTable)obj;
        return this.name == null ? other.name == null : this.name.equals(other.name);
    }

    public int hashCode() {
        return 217 + (null == this.name ? 0 : this.name.hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(' ');
        sb.append(this.valueCounter);
        return sb.toString();
    }
}

