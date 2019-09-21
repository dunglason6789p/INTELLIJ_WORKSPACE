/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide.decision;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.guide.decision.BranchedDecisionModel;
import org.maltparser.parser.guide.decision.DecisionModel;
import org.maltparser.parser.guide.decision.OneDecisionModel;
import org.maltparser.parser.guide.instance.AtomicModel;
import org.maltparser.parser.guide.instance.FeatureDivideModel;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.MultipleDecision;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.container.TableContainer;

public class SeqDecisionModel
implements DecisionModel {
    private final ClassifierGuide guide;
    private final String modelName;
    private InstanceModel instanceModel;
    private final int decisionIndex;
    private final DecisionModel prevDecisionModel;
    private DecisionModel nextDecisionModel;
    private final String branchedDecisionSymbols;

    public SeqDecisionModel(ClassifierGuide _guide) throws MaltChainedException {
        this.guide = _guide;
        this.branchedDecisionSymbols = "";
        this.decisionIndex = 0;
        this.modelName = "sdm" + this.decisionIndex;
        this.prevDecisionModel = null;
    }

    public SeqDecisionModel(ClassifierGuide _guide, DecisionModel _prevDecisionModel, String _branchedDecisionSymbol) throws MaltChainedException {
        this.guide = _guide;
        this.decisionIndex = _prevDecisionModel.getDecisionIndex() + 1;
        if (_branchedDecisionSymbol != null && _branchedDecisionSymbol.length() > 0) {
            this.branchedDecisionSymbols = _branchedDecisionSymbol;
            this.modelName = "sdm" + this.decisionIndex + this.branchedDecisionSymbols;
        } else {
            this.branchedDecisionSymbols = "";
            this.modelName = "sdm" + this.decisionIndex;
        }
        this.prevDecisionModel = _prevDecisionModel;
    }

    private void initInstanceModel(FeatureModel featureModel, String subModelName) throws MaltChainedException {
        this.instanceModel = featureModel.hasDivideFeatureFunction() ? new FeatureDivideModel(this) : new AtomicModel(-1, this);
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
        if (this.instanceModel != null) {
            this.instanceModel.finalizeSentence(dependencyGraph);
        }
        if (this.nextDecisionModel != null) {
            this.nextDecisionModel.finalizeSentence(dependencyGraph);
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
        if (this.nextDecisionModel != null) {
            this.nextDecisionModel.noMoreInstances(featureModel);
        }
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.instanceModel != null) {
            this.instanceModel.terminate();
            this.instanceModel = null;
        }
        if (this.nextDecisionModel != null) {
            this.nextDecisionModel.terminate();
            this.nextDecisionModel = null;
        }
    }

    @Override
    public void addInstance(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        if (decision instanceof SingleDecision) {
            throw new GuideException("A sequantial decision model expect a sequence of decisions, not a single decision. ");
        }
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        this.instanceModel.addInstance(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (singleDecision.continueWithNextDecision() && this.decisionIndex + 1 < decision.numberOfDecisions()) {
            if (this.nextDecisionModel == null) {
                this.initNextDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols);
            }
            this.nextDecisionModel.addInstance(featureModel, decision);
        }
    }

    @Override
    public boolean predict(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        if (decision instanceof SingleDecision) {
            throw new GuideException("A sequantial decision model expect a sequence of decisions, not a single decision. ");
        }
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        boolean success = this.instanceModel.predict(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (singleDecision.continueWithNextDecision() && this.decisionIndex + 1 < decision.numberOfDecisions()) {
            if (this.nextDecisionModel == null) {
                this.initNextDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols);
            }
            success = this.nextDecisionModel.predict(featureModel, decision) && success;
        }
        return success;
    }

    @Override
    public FeatureVector predictExtract(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
        if (decision instanceof SingleDecision) {
            throw new GuideException("A sequantial decision model expect a sequence of decisions, not a single decision. ");
        }
        featureModel.update();
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.instanceModel == null) {
            this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
        }
        FeatureVector fv = this.instanceModel.predictExtract(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
        if (singleDecision.continueWithNextDecision() && this.decisionIndex + 1 < decision.numberOfDecisions()) {
            if (this.nextDecisionModel == null) {
                this.initNextDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols);
            }
            this.nextDecisionModel.predictExtract(featureModel, decision);
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
        if (decision instanceof SingleDecision) {
            throw new GuideException("A sequantial decision model expect a sequence of decisions, not a single decision. ");
        }
        boolean success = false;
        SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
        if (this.nextDecisionModel != null && singleDecision.continueWithNextDecision()) {
            success = this.nextDecisionModel.predictFromKBestList(featureModel, decision);
        }
        if (!success && (success = singleDecision.updateFromKBestList()) && singleDecision.continueWithNextDecision() && this.decisionIndex + 1 < decision.numberOfDecisions()) {
            if (this.nextDecisionModel == null) {
                this.initNextDecisionModel(((MultipleDecision)decision).getSingleDecision(this.decisionIndex + 1), this.branchedDecisionSymbols);
            }
            this.nextDecisionModel.predict(featureModel, decision);
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

    public DecisionModel getPrevDecisionModel() {
        return this.prevDecisionModel;
    }

    public DecisionModel getNextDecisionModel() {
        return this.nextDecisionModel;
    }

    private void initNextDecisionModel(SingleDecision decision, String branchedDecisionSymbol) throws MaltChainedException {
        if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.SEQUANTIAL) {
            this.nextDecisionModel = new SeqDecisionModel(this.guide, this, branchedDecisionSymbol);
        } else if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.BRANCHED) {
            this.nextDecisionModel = new BranchedDecisionModel(this.guide, this, branchedDecisionSymbol);
        } else if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.NONE) {
            this.nextDecisionModel = new OneDecisionModel(this.guide, this, branchedDecisionSymbol);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.modelName + ", ");
        sb.append(this.nextDecisionModel.toString());
        return sb.toString();
    }
}

