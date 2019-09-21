package org.maltparser.parser.history;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.TableContainer;

public interface GuideUserHistory {
   GuideUserAction getEmptyGuideUserAction() throws MaltChainedException;

   ArrayList<ActionContainer> getActionContainers();

   ActionContainer[] getActionContainerArray();

   int getNumberOfDecisions();

   void clear() throws MaltChainedException;

   int getKBestSize();

   ArrayList<TableContainer> getDecisionTables();

   ArrayList<TableContainer> getActionTables();
}
