package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public interface Guidable {
   void setInstance(GuideUserAction var1) throws MaltChainedException;

   void predict(GuideUserAction var1) throws MaltChainedException;

   boolean predictFromKBestList(GuideUserAction var1) throws MaltChainedException;
}
