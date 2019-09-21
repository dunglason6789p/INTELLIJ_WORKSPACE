/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Parser;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.ParserState;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.SingleGuide;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.GuideUserAction;

public class DeterministicParser
extends Parser {
    private final FeatureModel featureModel;

    public DeterministicParser(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        super(manager, symbolTableHandler);
        this.registry.setAlgorithm(this);
        this.setGuide(new SingleGuide(this, ClassifierGuide.GuideMode.CLASSIFY));
        String featureModelFileName = manager.getOptionValue("guide", "features").toString().trim();
        if (manager.isLoggerInfoEnabled()) {
            manager.logDebugMessage("  Feature model        : " + featureModelFileName + "\n");
            manager.logDebugMessage("  Classifier           : " + manager.getOptionValueString("guide", "learner") + "\n");
        }
        String dataSplitColumn = manager.getOptionValue("guide", "data_split_column").toString().trim();
        String dataSplitStructure = manager.getOptionValue("guide", "data_split_structure").toString().trim();
        this.featureModel = manager.getFeatureModelManager().getFeatureModel(SingleGuide.findURL(featureModelFileName, manager), 0, this.getParserRegistry(), dataSplitColumn, dataSplitStructure);
    }

    @Override
    public DependencyStructure parse(DependencyStructure parseDependencyGraph) throws MaltChainedException {
        this.parserState.clear();
        this.parserState.initialize(parseDependencyGraph);
        this.currentParserConfiguration = this.parserState.getConfiguration();
        TransitionSystem ts = this.parserState.getTransitionSystem();
        while (!this.parserState.isTerminalState()) {
            GuideUserAction action = ts.getDeterministicAction(this.parserState.getHistory(), this.currentParserConfiguration);
            if (action == null) {
                action = this.predict();
            }
            this.parserState.apply(action);
        }
        parseDependencyGraph.linkAllTreesToRoot();
        return parseDependencyGraph;
    }

    private GuideUserAction predict() throws MaltChainedException {
        GuideUserAction currentAction = this.parserState.getHistory().getEmptyGuideUserAction();
        try {
            this.classifierGuide.predict(this.featureModel, (GuideDecision)((Object)currentAction));
            while (!this.parserState.permissible(currentAction)) {
                if (this.classifierGuide.predictFromKBestList(this.featureModel, (GuideDecision)((Object)currentAction))) continue;
                currentAction = this.getParserState().getTransitionSystem().defaultAction(this.parserState.getHistory(), this.currentParserConfiguration);
                break;
            }
        }
        catch (NullPointerException e) {
            throw new MaltChainedException("The guide cannot be found. ", e);
        }
        return currentAction;
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

