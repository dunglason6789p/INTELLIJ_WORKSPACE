/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.trie;

import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.trie.TrieSymbolTable;

public class TrieNode {
    private final char character;
    private HashMap<TrieSymbolTable, Integer> entries;
    private TrieSymbolTable cachedKeyEntry;
    private Integer cachedValueEntry;
    private HashMap<Character, TrieNode> children;
    private char cachedKeyChar;
    private TrieNode cachedValueTrieNode;
    private final TrieNode parent;

    public TrieNode(char character, TrieNode parent) {
        this.character = character;
        this.parent = parent;
    }

    public TrieNode getOrAddChild(boolean isWord, char c, TrieSymbolTable table, int code) throws SymbolException {
        if (this.cachedValueTrieNode == null) {
            this.cachedValueTrieNode = new TrieNode(c, this);
            this.cachedKeyChar = c;
            if (isWord) {
                this.cachedValueTrieNode.addEntry(table, code);
            }
            return this.cachedValueTrieNode;
        }
        if (this.cachedKeyChar == c) {
            if (isWord) {
                this.cachedValueTrieNode.addEntry(table, code);
            }
            return this.cachedValueTrieNode;
        }
        TrieNode child = null;
        if (this.children == null) {
            this.children = new HashMap();
            child = new TrieNode(c, this);
            this.children.put(Character.valueOf(c), child);
        } else {
            child = this.children.get(Character.valueOf(c));
            if (child == null) {
                child = new TrieNode(c, this);
                this.children.put(Character.valueOf(c), child);
            }
        }
        if (isWord) {
            child.addEntry(table, code);
        }
        return child;
    }

    private void addEntry(TrieSymbolTable table, int code) throws SymbolException {
        if (table == null) {
            throw new SymbolException("Symbol table cannot be found. ");
        }
        if (this.cachedValueEntry == null) {
            if (code != -1) {
                this.cachedValueEntry = code;
                table.updateValueCounter(code);
            } else {
                this.cachedValueEntry = table.increaseValueCounter();
            }
            this.cachedKeyEntry = table;
        } else if (!table.equals(this.cachedKeyEntry)) {
            if (this.entries == null) {
                this.entries = new HashMap();
            }
            if (!this.entries.containsKey(table)) {
                if (code != -1) {
                    this.entries.put(table, code);
                    table.updateValueCounter(code);
                } else {
                    this.entries.put(table, table.increaseValueCounter());
                }
            }
        }
    }

    public TrieNode getChild(char c) {
        if (this.cachedKeyChar == c) {
            return this.cachedValueTrieNode;
        }
        if (this.children != null) {
            return this.children.get(Character.valueOf(c));
        }
        return null;
    }

    public Integer getEntry(TrieSymbolTable table) {
        if (table != null) {
            if (table.equals(this.cachedKeyEntry)) {
                return this.cachedValueEntry;
            }
            if (this.entries != null) {
                return this.entries.get(table);
            }
        }
        return null;
    }

    public char getCharacter() {
        return this.character;
    }

    public TrieNode getParent() {
        return this.parent;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.character);
        return sb.toString();
    }
}

