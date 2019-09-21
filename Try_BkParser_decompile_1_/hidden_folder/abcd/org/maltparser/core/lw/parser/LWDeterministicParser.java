/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.lw.parser.LWDecisionModel;
import org.maltparser.core.lw.parser.LWSingleMalt;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.CombinedTableContainer;
import org.maltparser.parser.history.container.TableContainer;

public final class LWDeterministicParser
implements AlgoritmInterface,
GuideUserHistory {
    private final LWSingleMalt manager;
    private final ParserRegistry registry;
    private final TransitionSystem transitionSystem;
    private final ParserConfiguration config;
    private final FeatureModel featureModel;
    private final ComplexDecisionAction currentAction;
    private final int kBestSize;
    private final ArrayList<TableContainer> decisionTables;
    private final ArrayList<TableContainer> actionTables;
    private final HashMap<String, TableHandler> tableHandlers;

    public LWDeterministicParser(LWSingleMalt lwSingleMalt, SymbolTableHandler symbolTableHandler, FeatureModel _featureModel) throws MaltChainedException {
        this.manager = lwSingleMalt;
        this.registry = new ParserRegistry();
        this.registry.setSymbolTableHandler(symbolTableHandler);
        this.registry.setDataFormatInstance(this.manager.getDataFormatInstance());
        this.registry.setAbstractParserFeatureFactory(this.manager.getParserFactory());
        this.registry.setAlgorithm(this);
        this.transitionSystem = this.manager.getParserFactory().makeTransitionSystem();
        this.transitionSystem.initTableHandlers(lwSingleMalt.getDecisionSettings(), symbolTableHandler);
        this.tableHandlers = this.transitionSystem.getTableHandlers();
        this.kBestSize = lwSingleMalt.getkBestSize();
        this.decisionTables = new ArrayList();
        this.actionTables = new ArrayList();
        this.initDecisionSettings(lwSingleMalt.getDecisionSettings(), lwSingleMalt.getClassitem_separator());
        this.transitionSystem.initTransitionSystem(this);
        this.config = this.manager.getParserFactory().makeParserConfiguration();
        this.featureModel = _featureModel;
        this.currentAction = new ComplexDecisionAction(this);
    }

    public LWDeterministicParser(LWSingleMalt lwSingleMalt, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        this.manager = lwSingleMalt;
        this.registry = new ParserRegistry();
        this.registry.setSymbolTableHandler(symbolTableHandler);
        this.registry.setDataFormatInstance(this.manager.getDataFormatInstance());
        this.registry.setAbstractParserFeatureFactory(this.manager.getParserFactory());
        this.registry.setAlgorithm(this);
        this.transitionSystem = this.manager.getParserFactory().makeTransitionSystem();
        this.transitionSystem.initTableHandlers(lwSingleMalt.getDecisionSettings(), symbolTableHandler);
        this.tableHandlers = this.transitionSystem.getTableHandlers();
        this.kBestSize = lwSingleMalt.getkBestSize();
        this.decisionTables = new ArrayList();
        this.actionTables = new ArrayList();
        this.initDecisionSettings(lwSingleMalt.getDecisionSettings(), lwSingleMalt.getClassitem_separator());
        this.transitionSystem.initTransitionSystem(this);
        this.config = this.manager.getParserFactory().makeParserConfiguration();
        this.featureModel = this.manager.getFeatureModelManager().getFeatureModel(lwSingleMalt.getFeatureModelURL(), 0, this.registry, this.manager.getDataSplitColumn(), this.manager.getDataSplitStructure());
        this.currentAction = new ComplexDecisionAction(this);
    }

    public DependencyStructure parse(DependencyStructure parseDependencyGraph) throws MaltChainedException {
        this.config.clear();
        this.config.setDependencyGraph(parseDependencyGraph);
        this.config.initialize();
        while (!this.config.isTerminalState()) {
            GuideUserAction action = this.transitionSystem.getDeterministicAction(this, this.config);
            if (action == null) {
                action = this.predict();
            }
            this.transitionSystem.apply(action, this.config);
        }
        parseDependencyGraph.linkAllTreesToRoot();
        return parseDependencyGraph;
    }

    private GuideUserAction predict() throws MaltChainedException {
        this.currentAction.clear();
        try {
            this.manager.getDecisionModel().predict(this.featureModel, this.currentAction, true);
            while (!this.transitionSystem.permissible(this.currentAction, this.config)) {
                if (this.manager.getDecisionModel().predictFromKBestList(this.featureModel, this.currentAction)) continue;
                GuideUserAction defaultAction = this.transitionSystem.defaultAction(this, this.config);
                ActionContainer[] actionContainers = this.getActionContainerArray();
                defaultAction.getAction(actionContainers);
                this.currentAction.addAction(actionContainers);
                break;
            }
        }
        catch (NullPointerException e) {
            throw new MaltChainedException("The guide cannot be found. ", e);
        }
        return this.currentAction;
    }

    @Override
    public ParserRegistry getParserRegistry() {
        return this.registry;
    }

    @Override
    public ParserConfiguration getCurrentParserConfiguration() {
        return this.config;
    }

    @Override
    public DependencyParserConfig getManager() {
        return this.manager;
    }

    public String getGuideName() {
        return null;
    }

    public void setGuideName(String guideName) {
    }

    @Override
    public GuideUserAction getEmptyGuideUserAction() throws MaltChainedException {
        return new ComplexDecisionAction(this);
    }

    @Override
    public ArrayList<ActionContainer> getActionContainers() {
        ArrayList<ActionContainer> actionContainers = new ArrayList<ActionContainer>();
        for (int i = 0; i < this.actionTables.size(); ++i) {
            actionContainers.add(new ActionContainer(this.actionTables.get(i)));
        }
        return actionContainers;
    }

    @Override
    public ActionContainer[] getActionContainerArray() {
        ActionContainer[] actionContainers = new ActionContainer[this.actionTables.size()];
        for (int i = 0; i < this.actionTables.size(); ++i) {
            actionContainers[i] = new ActionContainer(this.actionTables.get(i));
        }
        return actionContainers;
    }

    @Override
    public void clear() throws MaltChainedException {
    }

    @Override
    public int getNumberOfDecisions() {
        return this.decisionTables.size();
    }

    @Override
    public int getKBestSize() {
        return this.kBestSize;
    }

    public int getNumberOfActions() {
        return this.actionTables.size();
    }

    @Override
    public ArrayList<TableContainer> getDecisionTables() {
        return this.decisionTables;
    }

    @Override
    public ArrayList<TableContainer> getActionTables() {
        return this.actionTables;
    }

    private void initDecisionSettings(String decisionSettings, String separator) throws MaltChainedException {
        if (decisionSettings.equals("T.TRANS+A.DEPREL")) {
            this.actionTables.add(new TableContainer(this.tableHandlers.get("T").getSymbolTable("TRANS"), "T.TRANS", '+'));
            this.actionTables.add(new TableContainer(this.tableHandlers.get("A").getSymbolTable("DEPREL"), "A.DEPREL", ' '));
            this.decisionTables.add(new CombinedTableContainer(this.tableHandlers.get("A"), separator, this.actionTables, ' '));
        } else if (decisionSettings.equals("T.TRANS,A.DEPREL")) {
            TableContainer transTableContainer = new TableContainer(this.tableHandlers.get("T").getSymbolTable("TRANS"), "T.TRANS", ',');
            TableContainer deprelTableContainer = new TableContainer(this.tableHandlers.get("A").getSymbolTable("DEPREL"), "A.DEPREL", ',');
            this.actionTables.add(transTableContainer);
            this.actionTables.add(deprelTableContainer);
            this.decisionTables.add(transTableContainer);
            this.decisionTables.add(deprelTableContainer);
        } else if (decisionSettings.equals("T.TRANS#A.DEPREL") || decisionSettings.equals("T.TRANS;A.DEPREL")) {
            TableContainer transTableContainer = new TableContainer(this.tableHandlers.get("T").getSymbolTable("TRANS"), "T.TRANS", '#');
            TableContainer deprelTableContainer = new TableContainer(this.tableHandlers.get("A").getSymbolTable("DEPREL"), "A.DEPREL", '#');
            this.actionTables.add(transTableContainer);
            this.actionTables.add(deprelTableContainer);
            this.decisionTables.add(transTableContainer);
            this.decisionTables.add(deprelTableContainer);
        } else {
            int start = 0;
            int k = 0;
            char prevDecisionSeparator = ' ';
            TableContainer tmp = null;
            StringBuilder sbTableHandler = new StringBuilder();
            StringBuilder sbTable = new StringBuilder();
            int state = 0;
            for (int i = 0; i < decisionSettings.length(); ++i) {
                switch (decisionSettings.charAt(i)) {
                    case '.': {
                        state = 1;
                        break;
                    }
                    case '+': {
                        tmp = new TableContainer(this.tableHandlers.get(sbTableHandler.toString()).getSymbolTable(sbTable.toString()), sbTableHandler.toString() + "." + sbTable.toString(), '+');
                        this.actionTables.add(tmp);
                        ++k;
                        sbTableHandler.setLength(0);
                        sbTable.setLength(0);
                        state = 0;
                        break;
                    }
                    case '#': {
                        state = 2;
                        break;
                    }
                    case ';': {
                        state = 2;
                        break;
                    }
                    case ',': {
                        state = 2;
                        break;
                    }
                    default: {
                        if (state == 0) {
                            sbTableHandler.append(decisionSettings.charAt(i));
                            break;
                        }
                        if (state != 1) break;
                        sbTable.append(decisionSettings.charAt(i));
                    }
                }
                if (state != 2 && i != decisionSettings.length() - 1) continue;
                char decisionSeparator = decisionSettings.charAt(i);
                if (i == decisionSettings.length() - 1) {
                    decisionSeparator = prevDecisionSeparator;
                }
                tmp = new TableContainer(this.tableHandlers.get(sbTableHandler.toString()).getSymbolTable(sbTable.toString()), sbTableHandler.toString() + "." + sbTable.toString(), decisionSeparator);
                this.actionTables.add(tmp);
                if (++k - start > 1) {
                    this.decisionTables.add(new CombinedTableContainer(this.tableHandlers.get("A"), separator, this.actionTables.subList(start, k), decisionSeparator));
                } else {
                    this.decisionTables.add(tmp);
                }
                sbTableHandler.setLength(0);
                sbTable.setLength(0);
                state = 0;
                start = k;
                prevDecisionSeparator = decisionSeparator;
            }
        }
    }
}

