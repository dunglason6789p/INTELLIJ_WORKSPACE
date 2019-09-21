package org.maltparser.core.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.Function;

public interface AbstractFeatureFactory {
   Function makeFunction(String var1, FeatureRegistry var2) throws MaltChainedException;
}
