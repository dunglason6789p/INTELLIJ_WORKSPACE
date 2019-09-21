package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;

public interface Function {
   void initialize(Object[] var1) throws MaltChainedException;

   Class<?>[] getParameterTypes();

   void update() throws MaltChainedException;
}
