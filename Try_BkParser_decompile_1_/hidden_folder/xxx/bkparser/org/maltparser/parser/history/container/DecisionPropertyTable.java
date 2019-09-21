package org.maltparser.parser.history.container;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;

public interface DecisionPropertyTable {
   boolean continueWithNextDecision(int var1) throws MaltChainedException;

   boolean continueWithNextDecision(String var1) throws MaltChainedException;

   Table getTableForNextDecision(int var1) throws MaltChainedException;

   Table getTableForNextDecision(String var1) throws MaltChainedException;
}
