/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml;

import java.io.BufferedWriter;
import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.history.action.SingleDecision;

public interface LearningMethod {
    public static final int BATCH = 0;
    public static final int CLASSIFY = 1;

    public void addInstance(SingleDecision var1, FeatureVector var2) throws MaltChainedException;

    public void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

    public void noMoreInstances() throws MaltChainedException;

    public void train() throws MaltChainedException;

    public void moveAllInstances(LearningMethod var1, FeatureFunction var2, ArrayList<Integer> var3) throws MaltChainedException;

    public void terminate() throws MaltChainedException;

    public boolean predict(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

    public BufferedWriter getInstanceWriter();

    public void increaseNumberOfInstances();

    public void decreaseNumberOfInstances();
}

