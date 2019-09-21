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
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.ParserState;
import org.maltparser.parser.Trainer;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.guide.SingleGuide;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.GuideUserAction;

public class BatchTrainer
extends Trainer {
    private final OracleGuide oracleGuide;
    private final FeatureModel featureModel;

    public BatchTrainer(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        super(manager, symbolTableHandler);
        this.registry.setAlgorithm(this);
        this.setGuide(new SingleGuide(this, ClassifierGuide.GuideMode.BATCH));
        String featureModelFileName = manager.getOptionValue("guide", "features").toString().trim();
        if (manager.isLoggerInfoEnabled()) {
            manager.logDebugMessage("  Feature model        : " + featureModelFileName + "\n");
            manager.logDebugMessage("  Learner              : " + manager.getOptionValueString("guide", "learner").toString() + "\n");
        }
        String dataSplitColumn = manager.getOptionValue("guide", "data_split_column").toString().trim();
        String dataSplitStructure = manager.getOptionValue("guide", "data_split_structure").toString().trim();
        this.featureModel = manager.getFeatureModelManager().getFeatureModel(SingleGuide.findURL(featureModelFileName, manager), 0, this.getParserRegistry(), dataSplitColumn, dataSplitStructure);
        manager.writeInfoToConfigFile("\nFEATURE MODEL\n");
        manager.writeInfoToConfigFile(this.featureModel.toString());
        this.oracleGuide = this.parserState.getFactory().makeOracleGuide(this.parserState.getHistory());
    }

    @Override
    public DependencyStructure parse(DependencyStructure goldDependencyGraph, DependencyStructure parseDependencyGraph) throws MaltChainedException {
        this.parserState.clear();
        this.parserState.initialize(parseDependencyGraph);
        this.currentParserConfiguration = this.parserState.getConfiguration();
        TransitionSystem transitionSystem = this.parserState.getTransitionSystem();
        while (!this.parserState.isTerminalState()) {
            GuideUserAction action = transitionSystem.getDeterministicAction(this.parserState.getHistory(), this.currentParserConfiguration);
            if (action == null) {
                action = this.oracleGuide.predict(goldDependencyGraph, this.currentParserConfiguration);
                try {
                    this.classifierGuide.addInstance(this.featureModel, (GuideDecision)((Object)action));
                }
                catch (NullPointerException e) {
                    throw new MaltChainedException("The guide cannot be found. ", e);
                }
            }
            this.parserState.apply(action);
        }
        this.copyEdges(this.currentParserConfiguration.getDependencyGraph(), parseDependencyGraph);
        parseDependencyGraph.linkAllTreesToRoot();
        this.oracleGuide.finalizeSentence(parseDependencyGraph);
        return parseDependencyGraph;
    }

    @Override
    public OracleGuide getOracleGuide() {
        return this.oracleGuide;
    }

    @Override
    public void train() throws MaltChainedException {
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

