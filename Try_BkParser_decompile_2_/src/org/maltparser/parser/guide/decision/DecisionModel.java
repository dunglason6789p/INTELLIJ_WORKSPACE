/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide.decision;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.GuideDecision;

public interface DecisionModel
extends Model {
    public void addInstance(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

    public boolean predict(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

    public FeatureVector predictExtract(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

    public FeatureVector extract(FeatureModel var1) throws MaltChainedException;

    public boolean predictFromKBestList(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

    public int getDecisionIndex();
}

