package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;

public interface Modifiable extends FeatureFunction {
   void setFeatureValue(int var1) throws MaltChainedException;

   void setFeatureValue(String var1) throws MaltChainedException;
}
