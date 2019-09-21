/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;

public interface AbstractFeatureFactory {
    public Function makeFunction(String var1, FeatureRegistry var2) throws MaltChainedException;
}

