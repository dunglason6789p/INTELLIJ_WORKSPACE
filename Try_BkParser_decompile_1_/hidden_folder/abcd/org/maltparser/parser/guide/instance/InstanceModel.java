/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide.instance;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.SingleDecision;

public interface InstanceModel
extends Model {
    public void addInstance(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

    public boolean predict(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

    public FeatureVector predictExtract(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

    public FeatureVector extract(FeatureVector var1) throws MaltChainedException;

    public void train() throws MaltChainedException;

    public void increaseFrequency();

    public void decreaseFrequency();
}

