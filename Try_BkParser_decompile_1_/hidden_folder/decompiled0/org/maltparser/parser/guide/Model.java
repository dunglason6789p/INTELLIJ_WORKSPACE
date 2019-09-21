/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;

public interface Model {
    public void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

    public void noMoreInstances(FeatureModel var1) throws MaltChainedException;

    public void terminate() throws MaltChainedException;

    public ClassifierGuide getGuide();

    public String getModelName() throws MaltChainedException;
}

