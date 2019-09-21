package org.maltparser.parser.history.action;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.container.TableContainer;
import org.maltparser.parser.history.kbest.KBestList;

public interface SingleDecision extends GuideDecision {
   void addDecision(int var1) throws MaltChainedException;

   void addDecision(String var1) throws MaltChainedException;

   int getDecisionCode() throws MaltChainedException;

   String getDecisionSymbol() throws MaltChainedException;

   int getDecisionCode(String var1) throws MaltChainedException;

   KBestList getKBestList() throws MaltChainedException;

   boolean updateFromKBestList() throws MaltChainedException;

   boolean continueWithNextDecision() throws MaltChainedException;

   TableContainer getTableContainer();

   TableContainer.RelationToNextDecision getRelationToNextDecision();
}
