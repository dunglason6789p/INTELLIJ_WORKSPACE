package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public interface HistoryNode {
   HistoryNode getPreviousNode();

   GuideUserAction getAction();

   void setAction(GuideUserAction var1);

   void setPreviousNode(HistoryNode var1);

   int getPosition();

   void clear() throws MaltChainedException;
}
