/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide.decision;

import java.util.Collection;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.guide.decision.DecisionModel;
import org.maltparser.parser.guide.decision.OneDecisionModel;
import org.maltparser.parser.guide.decision.SeqDecisionModel;
import org.maltparser.parser.guide.instance.AtomicModel;
import org.maltparser.parser.guide.instance.FeatureDivideModel;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.MultipleDecision;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.container.TableContainer;

public class BranchedDecisionModel
implements DecisionModel {
    private final ClassifierGuide guide;
    private final String modelName;
    private InstanceModel instanceModel;
    private final int decisionIndex;
    private final DecisionModel parentDecisionModel;
    private final HashMap<Integer, DecisionModel> children;
    private final String branchedDecisionSymbols;

    public BranchedDecisionModel(ClassifierGuide _guide) throws MaltChainedException {
        this.guide = _guide;
        this.branchedDecisionSymbols = "";
        this.decisionIndex = 0;
        this.modelName = "bdm0";
        this.parentDecisionModel = null;
        this.children = new HashMap();
    }

    public BranchedDecisionModel(ClassifierGuide _guide, DecisionModel _parentDecisionModel, String _branchedDecisionSymbol) throws MaltChainedException {
        this.guide = _guide;
        this.parentDecisionModel = _parentDecisionModel;
        this.decisionIndex = this.parentDecisionModel.getDecisionIndex() + 1;
        if (_branchedDecisionSymbol != null && _branchedDecisionSymbol.length() > 0) {
            this.branchedDecisionSymbols = _branchedDecisionSymbol;
            this.modelName = "bdm" + this.decisionIndex + this.branchedDecisionSymbols;
        } else {
            this.branchedDecisionSymbols = "";
            this.modelName = "bdm" + this.decisionIndex;
        }
        this.children = new HashMap();
    }

    private void initInstanceModel(FeatureModel featureModel, String subModelName) throws MaltChainedException {
        this.instanceModel = featureModel.hasDivideFeatureFunction() ? new FeatureDivideModel(this) : new AtomicModel(-1, this);
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
        if (this.instanceModel != null) {
            this.instanceModel.finalizeSentence(dependencyGraph);
        }
        if (this.children != null) {
            for (DecisionModel child : this.children.values()) {
                child.finalizeSentence(dependencyGraph);
            }
        }
    }

    @Override
    public void noMoreInstances(FeatureModel featureModel) throws MaltChainedException {
        if (this.guide.getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
            throw new GuideException("The decision model could not create it's model. ");
        }
        if (this.instanceModel != null) {
            this.instanceModel.noMoreInstances(featureModel);
            this.instanceModel.train();
        }
        if (this.children != null) {
            for (DecisionModel child : this.children.values()) {
                child.noMoreInstances(featureModel);
            }
        }
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.instanceModel != null) {
            this.instanceModel.terminate();
            this.instanceModel = null;
        }
        if (this.children != null) {
            for (DecisionModel child : this.children.values()) {
                child.terminate();
            }
        }
    }

    @Override
    public void addInstance(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        if (decision instanceof SingleDecision) {
            throw new GuideException("A branched decision model expect more than one decisions. ");
        }
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        this.instanceModel.addInstance(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (this.decisionIndex + 1 < decision.numberOfDecisions() && singleDecision.continueWithNextDecision()) {
            DecisionModel child = this.children.get(singleDecision.getDecisionCode());
            if (child == null) {
                child = this.initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols + (this.branchedDecisionSymbols.length() == 0 ? "" : "_") + singleDecision.getDecisionSymbol());
                this.children.put(singleDecision.getDecisionCode(), child);
            }
            child.addInstance(featureModel, decision);
        }
    }

    @Override
    public boolean predict(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        this.instanceModel.predict(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (this.decisionIndex + 1 < decision.numberOfDecisions() && singleDecision.continueWithNextDecision()) {
            DecisionModel child = this.children.get(singleDecision.getDecisionCode());
            if (child == null) {
                child = this.initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols + (this.branchedDecisionSymbols.length() == 0 ? "" : "_") + singleDecision.getDecisionSymbol());
                this.children.put(singleDecision.getDecisionCode(), child);
            }
            child.predict(featureModel, decision);
        }
        return true;
    }

    @Override
    public FeatureVector predictExtract(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        if (decision instanceof SingleDecision) {
            throw new GuideException("A branched decision model expect more than one decisions. ");
        }
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        FeatureVector fv = this.instanceModel.predictExtract(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (this.decisionIndex + 1 < decision.numberOfDecisions() && singleDecision.continueWithNextDecision()) {
            DecisionModel child = this.children.get(singleDecision.getDecisionCode());
            if (child == null) {
                child = this.initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols + (this.branchedDecisionSymbols.length() == 0 ? "" : "_") + singleDecision.getDecisionSymbol());
                this.children.put(singleDecision.getDecisionCode(), child);
            }
            child.predictExtract(featureModel, decision);
        }
        return fv;
    }

    @Override
    public FeatureVector extract(FeatureModel featureModel) throws MaltChainedException {
        featureModel.update();
        return null;
    }

    @Override
    public boolean predictFromKBestList(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        DecisionModel child;
        if (decision instanceof SingleDecision) {
            throw new GuideException("A branched decision model expect more than one decisions. ");
        }
        boolean success = false;
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.decisionIndex + 1 < decision.numberOfDecisions() && singleDecision.continueWithNextDecision() && (child = this.children.get(singleDecision.getDecisionCode())) != null) {
            success = child.predictFromKBestList(featureModel, decision);
        }
        if (!success) {
            success = singleDecision.updateFromKBestList();
            if (this.decisionIndex + 1 < decision.numberOfDecisions() && singleDecision.continueWithNextDecision()) {
                child = this.children.get(singleDecision.getDecisionCode());
                if (child == null) {
                    child = this.initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols + (this.branchedDecisionSymbols.length() == 0 ? "" : "_") + singleDecision.getDecisionSymbol());
                    this.children.put(singleDecision.getDecisionCode(), child);
                }
                child.predict(featureModel, decision);
            }
        }
        return success;
    }

    @Override
    public ClassifierGuide getGuide() {
        return this.guide;
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public int getDecisionIndex() {
        return this.decisionIndex;
    }

    public DecisionModel getParentDecisionModel() {
        return this.parentDecisionModel;
    }

    private DecisionModel initChildDecisionModel(SingleDecision decision, String branchedDecisionSymbol) throws MaltChainedException {
        if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.SEQUANTIAL) {
            return new SeqDecisionModel(this.guide, this, branchedDecisionSymbol);
        }
        if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.BRANCHED) {
            return new BranchedDecisionModel(this.guide, this, branchedDecisionSymbol);
        }
        if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.NONE) {
            return new OneDecisionModel(this.guide, this, branchedDecisionSymbol);
        }
        throw new GuideException("Could not find an appropriate decision model for the relation to the next decision");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.modelName + ", ");
        for (DecisionModel model : this.children.values()) {
            sb.append(model.toString() + ", ");
        }
        return sb.toString();
    }
}

