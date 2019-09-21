/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.ParserState;
import org.maltparser.parser.guide.ClassifierGuide;

public abstract class ParsingAlgorithm
implements AlgoritmInterface {
    protected final DependencyParserConfig manager;
    protected final ParserRegistry registry;
    protected ClassifierGuide classifierGuide;
    protected final ParserState parserState;
    protected ParserConfiguration currentParserConfiguration;

    public ParsingAlgorithm(DependencyParserConfig _manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        this.manager = _manager;
        this.registry = new ParserRegistry();
        this.registry.setSymbolTableHandler(symbolTableHandler);
        this.registry.setDataFormatInstance(this.manager.getDataFormatInstance());
        this.registry.setAbstractParserFeatureFactory(this.manager.getParserFactory());
        this.parserState = new ParserState(this.manager, symbolTableHandler, this.manager.getParserFactory());
    }

    public abstract void terminate() throws MaltChainedException;

    @Override
    public ParserRegistry getParserRegistry() {
        return this.registry;
    }

    public ClassifierGuide getGuide() {
        return this.classifierGuide;
    }

    public void setGuide(ClassifierGuide guide) {
        this.classifierGuide = guide;
    }

    @Override
    public ParserConfiguration getCurrentParserConfiguration() {
        return this.currentParserConfiguration;
    }

    protected void setCurrentParserConfiguration(ParserConfiguration currentParserConfiguration) {
        this.currentParserConfiguration = currentParserConfiguration;
    }

    public ParserState getParserState() {
        return this.parserState;
    }

    @Override
    public DependencyParserConfig getManager() {
        return this.manager;
    }

    protected void copyEdges(DependencyStructure source, DependencyStructure target) throws MaltChainedException {
        Iterator<E> i$ = source.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            DependencyNode snode = source.getDependencyNode(index);
            if (!snode.hasHead()) continue;
            Edge s = snode.getHeadEdge();
            Edge t = target.addDependencyEdge(s.getSource().getIndex(), s.getTarget().getIndex());
            for (SymbolTable table : s.getLabelTypes()) {
                t.addLabel(table, s.getLabelSymbol(table));
            }
        }
    }

    protected void copyDynamicInput(DependencyStructure source, DependencyStructure target) throws MaltChainedException {
        Iterator<E> i$ = source.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            DependencyNode snode = source.getDependencyNode(index);
            DependencyNode tnode = target.getDependencyNode(index);
            for (SymbolTable table : snode.getLabelTypes()) {
                if (tnode.hasLabel(table)) continue;
                tnode.addLabel(table, snode.getLabelSymbol(table));
            }
        }
    }
}

