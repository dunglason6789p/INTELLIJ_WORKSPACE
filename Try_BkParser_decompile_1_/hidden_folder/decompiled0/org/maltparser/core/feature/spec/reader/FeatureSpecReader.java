/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.spec.reader;

import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.spec.SpecificationModels;

public interface FeatureSpecReader {
    public void load(URL var1, SpecificationModels var2) throws MaltChainedException;
}
