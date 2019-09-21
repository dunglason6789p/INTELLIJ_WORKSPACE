/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.trie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.InputNullValues;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.symbol.nullvalue.OutputNullValues;
import org.maltparser.core.symbol.trie.Trie;
import org.maltparser.core.symbol.trie.TrieNode;

public class TrieSymbolTable
implements SymbolTable {
    private final String name;
    private final Trie trie;
    private final SortedMap<Integer, TrieNode> codeTable;
    private int category;
    private final NullValues nullValues;
    private int valueCounter;
    private int cachedHash;

    public TrieSymbolTable(String _name, Trie _trie, int _category, String nullValueStrategy) throws MaltChainedException {
        this.name = _name;
        this.trie = _trie;
        this.category = _category;
        this.codeTable = new TreeMap<Integer, TrieNode>();
        this.nullValues = this.category != 3 ? new OutputNullValues(nullValueStrategy, this) : new InputNullValues(nullValueStrategy, this);
        this.valueCounter = this.nullValues.getNextCode();
    }

    public TrieSymbolTable(String _name, Trie trie) {
        this.name = _name;
        this.trie = trie;
        this.codeTable = new TreeMap<Integer, TrieNode>();
        this.nullValues = new InputNullValues("one", this);
        this.valueCounter = 1;
    }

    @Override
    public int addSymbol(String symbol) throws MaltChainedException {
        if (this.nullValues == null || !this.nullValues.isNullValue(symbol)) {
            if (symbol == null || symbol.length() == 0) {
                throw new SymbolException("Symbol table error: empty string cannot be added to the symbol table");
            }
            TrieNode node = this.trie.addValue(symbol, this, -1);
            int code = node.getEntry(this);
            if (!this.codeTable.containsKey(code)) {
                this.codeTable.put(code, node);
            }
            return code;
        }
        return this.nullValues.symbolToCode(symbol);
    }

    @Override
    public String getSymbolCodeToString(int code) throws MaltChainedException {
        if (code >= 0) {
            if (this.nullValues == null || !this.nullValues.isNullValue(code)) {
                TrieNode node = (TrieNode)this.codeTable.get(code);
                if (node != null) {
                    return this.trie.getValue(node, this);
                }
                return null;
            }
            return this.nullValues.codeToSymbol(code);
        }
        throw new SymbolException("The symbol code '" + code + "' cannot be found in the symbol table. ");
    }

    @Override
    public int getSymbolStringToCode(String symbol) throws MaltChainedException {
        if (symbol != null) {
            if (this.nullValues == null || !this.nullValues.isNullValue(symbol)) {
                Integer entry = this.trie.getEntry(symbol, this);
                if (entry != null) {
                    return entry;
                }
                return -1;
            }
            return this.nullValues.symbolToCode(symbol);
        }
        throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
    }

    @Override
    public double getSymbolStringToValue(String symbol) throws MaltChainedException {
        if (symbol == null) {
            throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
        }
        return 1.0;
    }

    public void clearTmpStorage() {
    }

    public String getNullValueStrategy() {
        if (this.nullValues == null) {
            return null;
        }
        return this.nullValues.getNullValueStrategy();
    }

    public int getCategory() {
        return this.category;
    }

    public void saveHeader(BufferedWriter out) throws MaltChainedException {
        try {
            out.append('\t');
            out.append(this.getName());
            out.append('\t');
            out.append(Integer.toString(this.getCategory()));
            out.append('\t');
            out.append(Integer.toString(1));
            out.append('\t');
            out.append(this.getNullValueStrategy());
            out.append('\n');
        }
        catch (IOException e) {
            throw new SymbolException("Could not save the symbol table. ", e);
        }
    }

    @Override
    public int size() {
        return this.codeTable.size();
    }

    @Override
    public void save(BufferedWriter out) throws MaltChainedException {
        try {
            out.write(this.name);
            out.write(10);
            for (Integer code : this.codeTable.keySet()) {
                out.write(code + "");
                out.write(9);
                out.write(this.trie.getValue((TrieNode)this.codeTable.get(code), this));
                out.write(10);
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
        int index = 0;
        try {
            String fileLine;
            while ((fileLine = in.readLine()) != null) {
                if (fileLine.length() == 0 || (index = fileLine.indexOf(9)) == -1) {
                    this.setValueCounter(max + 1);
                    break;
                }
                int code = Integer.parseInt(fileLine.substring(0, index));
                String str = fileLine.substring(index + 1);
                TrieNode node = this.trie.addValue(str, this, code);
                this.codeTable.put(node.getEntry(this), node);
                if (max >= code) continue;
                max = code;
            }
        }
        catch (NumberFormatException e) {
            throw new SymbolException("The symbol table file (.sym) contains a non-integer value in the first column. ", e);
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

    private void setValueCounter(int valueCounter) {
        this.valueCounter = valueCounter;
    }

    protected void updateValueCounter(int code) {
        if (code > this.valueCounter) {
            this.valueCounter = code;
        }
    }

    protected int increaseValueCounter() {
        return this.valueCounter++;
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

    public SortedMap<Integer, TrieNode> getCodeTable() {
        return this.codeTable;
    }

    public Set<Integer> getCodes() {
        return this.codeTable.keySet();
    }

    protected Trie getTrie() {
        return this.trie;
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
        TrieSymbolTable other = (TrieSymbolTable)obj;
        return this.name == null ? other.name == null : this.name.equals(other.name);
    }

    public int hashCode() {
        if (this.cachedHash == 0) {
            this.cachedHash = 217 + (null == this.name ? 0 : this.name.hashCode());
        }
        return this.cachedHash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(' ');
        sb.append(this.valueCounter);
        return sb.toString();
    }
}

