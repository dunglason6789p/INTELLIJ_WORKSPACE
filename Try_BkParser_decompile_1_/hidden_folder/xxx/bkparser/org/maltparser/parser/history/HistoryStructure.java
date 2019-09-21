package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public abstract class HistoryStructure {
   public HistoryStructure() {
   }

   public abstract HistoryNode getNewHistoryNode(HistoryNode var1, GuideUserAction var2) throws MaltChainedException;

   public abstract void clear() throws MaltChainedException;
}
