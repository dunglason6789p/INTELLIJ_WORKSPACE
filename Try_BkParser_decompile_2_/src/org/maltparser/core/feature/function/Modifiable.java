/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.FeatureFunction;

public interface Modifiable
extends FeatureFunction {
    public void setFeatureValue(int var1) throws MaltChainedException;

    public void setFeatureValue(String var1) throws MaltChainedException;
}

