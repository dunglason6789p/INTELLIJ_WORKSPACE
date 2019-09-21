/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraph;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class Sentence
extends SyntaxGraph
implements TokenStructure {
    protected final ObjectPoolList<Token> terminalPool;
    protected final SortedMap<Integer, Token> terminalNodes = new TreeMap<Integer, Token>();
    protected final HashMap<Integer, ArrayList<String>> comments;
    protected int sentenceID;

    public Sentence(SymbolTableHandler symbolTables) throws MaltChainedException {
        super(symbolTables);
        this.terminalPool = new ObjectPoolList<Token>(){

            @Override
            protected Token create() throws MaltChainedException {
                return new Token();
            }

            @Override
            public void resetObject(Token o) throws MaltChainedException {
                o.clear();
            }
        };
        this.comments = new HashMap();
    }

    @Override
    public TokenNode addTokenNode(int index) throws MaltChainedException {
        if (index > 0) {
            return this.getOrAddTerminalNode(index);
        }
        return null;
    }

    @Override
    public TokenNode addTokenNode() throws MaltChainedException {
        int index = this.getHighestTokenIndex();
        if (index > 0) {
            return this.getOrAddTerminalNode(index + 1);
        }
        return this.getOrAddTerminalNode(1);
    }

    @Override
    public void addComment(String comment, int at_index) {
        ArrayList<String> commentList = this.comments.get(at_index);
        if (commentList == null) {
            commentList = new ArrayList();
            this.comments.put(at_index, commentList);
        }
        commentList.add(comment);
    }

    @Override
    public ArrayList<String> getComment(int at_index) {
        return this.comments.get(at_index);
    }

    @Override
    public boolean hasComments() {
        return this.comments.size() > 0;
    }

    @Override
    public int nTokenNode() {
        return this.terminalNodes.size();
    }

    @Override
    public boolean hasTokens() {
        return !this.terminalNodes.isEmpty();
    }

    protected Token getOrAddTerminalNode(int index) throws MaltChainedException {
        Token node = (Token)this.terminalNodes.get(index);
        if (node == null) {
            if (index > 0) {
                node = this.terminalPool.checkOut();
                node.setIndex(index);
                node.setBelongsToGraph(this);
                if (index > 1) {
                    Token prev = (Token)this.terminalNodes.get(index - 1);
                    if (prev == null) {
                        try {
                            prev = (Token)this.terminalNodes.get(this.terminalNodes.headMap(index).lastKey());
                        }
                        catch (NoSuchElementException e) {
                            // empty catch block
                        }
                    }
                    if (prev != null) {
                        prev.setSuccessor(node);
                        node.setPredecessor(prev);
                    }
                    if (this.terminalNodes.lastKey() > index) {
                        Token succ = (Token)this.terminalNodes.get(index + 1);
                        if (succ == null) {
                            try {
                                succ = (Token)this.terminalNodes.get(this.terminalNodes.tailMap(index).firstKey());
                            }
                            catch (NoSuchElementException e) {
                                // empty catch block
                            }
                        }
                        if (succ != null) {
                            succ.setPredecessor(node);
                            node.setSuccessor(succ);
                        }
                    }
                }
            }
            this.terminalNodes.put(index, node);
            ++this.numberOfComponents;
        }
        return node;
    }

    @Override
    public SortedSet<Integer> getTokenIndices() {
        return new TreeSet<Integer>(this.terminalNodes.keySet());
    }

    @Override
    public int getHighestTokenIndex() {
        try {
            return this.terminalNodes.lastKey();
        }
        catch (NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public TokenNode getTokenNode(int index) {
        if (index > 0) {
            return (TokenNode)this.terminalNodes.get(index);
        }
        return null;
    }

    @Override
    public int getSentenceID() {
        return this.sentenceID;
    }

    @Override
    public void setSentenceID(int sentenceID) {
        this.sentenceID = sentenceID;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.terminalPool.checkInAll();
        this.terminalNodes.clear();
        this.comments.clear();
        this.sentenceID = 0;
        super.clear();
    }

    @Override
    public void update(Observable o, Object str) {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int index : this.terminalNodes.keySet()) {
            sb.append(((Token)this.terminalNodes.get(index)).toString().trim());
            sb.append('\n');
        }
        sb.append("\n");
        return sb.toString();
    }

}

