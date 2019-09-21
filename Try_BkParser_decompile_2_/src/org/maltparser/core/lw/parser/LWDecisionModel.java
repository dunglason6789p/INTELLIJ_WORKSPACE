/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.parser;

import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.spec.SpecificationSubModel;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.lw.parser.LWClassifier;
import org.maltparser.core.lw.parser.McoModel;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.container.TableContainer;

public final class LWDecisionModel {
    private final String classifierName;
    private final HashMap<String, LWClassifier> classifiers;

    public LWDecisionModel(McoModel mcoModel, boolean _excludeNullValues, String _classifierName) {
        this.classifierName = _classifierName;
        this.classifiers = new HashMap();
        Set<String> mcoEntryObjectKeys = mcoModel.getMcoEntryObjectKeys();
        for (String key : mcoEntryObjectKeys) {
            if (!key.endsWith(".moo")) continue;
            String prefixFileName = key.substring(0, key.length() - 4);
            this.classifiers.put(prefixFileName, new LWClassifier(mcoModel, prefixFileName, _excludeNullValues));
        }
    }

    public boolean predict(FeatureModel featureModel, ComplexDecisionAction decision, boolean one_prediction) throws MaltChainedException {
        if (decision.numberOfDecisions() > 2) {
            throw new MaltChainedException("Number of decisions is greater than two,  which is unsupported in the light-weight parser (lw.parser)");
        }
        featureModel.update();
        boolean success = true;
        for (int i = 0; i < decision.numberOfDecisions(); ++i) {
            LWClassifier classifier = null;
            SingleDecision singleDecision = decision.getSingleDecision(i);
            StringBuilder classifierString = new StringBuilder();
            StringBuilder decisionModelString = new StringBuilder();
            if (singleDecision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.BRANCHED) {
                decisionModelString.append("bdm");
            } else if (singleDecision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.SEQUANTIAL) {
                decisionModelString.append("sdm");
            } else {
                decisionModelString.append("odm");
            }
            decisionModelString.append(i);
            String decisionSymbol = "";
            if (i == 1 && singleDecision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.BRANCHED) {
                decisionSymbol = singleDecision.getDecisionSymbol();
                decisionModelString.append(decisionSymbol);
            }
            decisionModelString.append('.');
            FeatureVector featureVector = featureModel.getFeatureVector(decisionSymbol, singleDecision.getTableContainer().getTableContainerName());
            if (featureModel.hasDivideFeatureFunction()) {
                SingleFeatureValue featureValue = (SingleFeatureValue)featureModel.getDivideFeatureFunction().getFeatureValue();
                classifierString.append(decisionModelString);
                classifierString.append(String.format("%03d", featureValue.getIndexCode()));
                classifierString.append('.');
                classifierString.append(this.classifierName);
                classifier = this.classifiers.get(classifierString.toString());
                if (classifier != null) {
                    FeatureVector dividefeatureVector = featureModel.getFeatureVector("/" + featureVector.getSpecSubModel().getSubModelName());
                    success = classifier.predict(dividefeatureVector, singleDecision, one_prediction) && success;
                    continue;
                }
                classifierString.setLength(0);
            }
            classifierString.append(decisionModelString);
            classifierString.append(this.classifierName);
            classifier = this.classifiers.get(classifierString.toString());
            if (classifier != null) {
                success = classifier.predict(featureVector, singleDecision, one_prediction) && success;
            } else {
                singleDecision.addDecision(1);
            }
            if (!singleDecision.continueWithNextDecision()) break;
        }
        return success;
    }

    public boolean predictFromKBestList(FeatureModel featureModel, ComplexDecisionAction decision) throws MaltChainedException {
        this.predict(featureModel, decision, false);
        if (decision.numberOfDecisions() == 1) {
            return decision.getSingleDecision(0).updateFromKBestList();
        }
        if (decision.numberOfDecisions() > 2) {
            throw new MaltChainedException("Number of decisions is greater than two,  which is unsupported in the light-weight parser (lw.parser)");
        }
        boolean success = false;
        if (decision.getSingleDecision(0).continueWithNextDecision()) {
            success = decision.getSingleDecision(1).updateFromKBestList();
        }
        return success;
    }
}

