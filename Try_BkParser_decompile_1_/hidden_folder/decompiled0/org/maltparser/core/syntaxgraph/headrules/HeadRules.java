/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.headrules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.headrules.Direction;
import org.maltparser.core.syntaxgraph.headrules.HeadRule;
import org.maltparser.core.syntaxgraph.headrules.HeadRuleException;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public class HeadRules
extends HashMap<String, HeadRule> {
    public static final long serialVersionUID = 8045568022124826323L;
    protected Logger logger;
    protected String name;
    private final SymbolTableHandler symbolTableHandler;
    private final DataFormatInstance dataFormatInstance;
    protected SymbolTable nonTerminalSymbolTable;
    protected SymbolTable edgelabelSymbolTable;

    public HeadRules(Logger logger, DataFormatInstance dataFormatInstance, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        this.setLogger(logger);
        this.dataFormatInstance = dataFormatInstance;
        this.symbolTableHandler = symbolTableHandler;
        this.nonTerminalSymbolTable = symbolTableHandler.addSymbolTable("CAT");
        this.edgelabelSymbolTable = symbolTableHandler.addSymbolTable("LABEL");
    }

    public void parseHeadRules(String fileName) throws MaltChainedException {
        URLFinder f = new URLFinder();
        this.parseHeadRules(f.findURL(fileName));
    }

    public void parseHeadRules(URL url) throws MaltChainedException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {
            throw new HeadRuleException("Could not read the head rules from file '" + url.toString() + "'. ", e);
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.debug("Loading the head rule specification '" + url.toString() + "' ...\n");
        }
        do {
            String fileLine;
            try {
                fileLine = br.readLine();
            }
            catch (IOException e) {
                throw new HeadRuleException("Could not read the head rules from file '" + url.toString() + "'. ", e);
            }
            if (fileLine == null) break;
            if (fileLine.length() <= 1 && fileLine.trim().substring(0, 2).trim().equals("--")) continue;
            int index = fileLine.indexOf(9);
            if (index == -1) {
                throw new HeadRuleException("The specification of the head rule is not correct '" + fileLine + "'. ");
            }
            HeadRule rule = new HeadRule(this, fileLine);
            this.put(fileLine.substring(0, index), rule);
        } while (true);
    }

    public PhraseStructureNode getHeadChild(NonTerminalNode nt) throws MaltChainedException {
        HeadRule rule = null;
        if (nt.hasLabel(this.nonTerminalSymbolTable)) {
            rule = (HeadRule)this.get(this.nonTerminalSymbolTable.getName() + ":" + nt.getLabelSymbol(this.nonTerminalSymbolTable));
        }
        if (rule == null && nt.hasParentEdgeLabel(this.edgelabelSymbolTable)) {
            rule = (HeadRule)this.get(this.edgelabelSymbolTable.getName() + ":" + nt.getParentEdgeLabelSymbol(this.edgelabelSymbolTable));
        }
        if (rule != null) {
            return rule.getHeadChild(nt);
        }
        return null;
    }

    public Direction getDefaultDirection(NonTerminalNode nt) throws MaltChainedException {
        HeadRule rule = null;
        if (nt.hasLabel(this.nonTerminalSymbolTable)) {
            rule = (HeadRule)this.get(this.nonTerminalSymbolTable.getName() + ":" + nt.getLabelSymbol(this.nonTerminalSymbolTable));
        }
        if (rule == null && nt.hasParentEdgeLabel(this.edgelabelSymbolTable)) {
            rule = (HeadRule)this.get(this.edgelabelSymbolTable.getName() + ":" + nt.getParentEdgeLabelSymbol(this.edgelabelSymbolTable));
        }
        if (rule != null) {
            return rule.getDefaultDirection();
        }
        return Direction.LEFT;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    public SymbolTableHandler getSymbolTableHandler() {
        return this.symbolTableHandler;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (HeadRule rule : this.values()) {
            sb.append(rule);
            sb.append('\n');
        }
        return sb.toString();
    }
}

