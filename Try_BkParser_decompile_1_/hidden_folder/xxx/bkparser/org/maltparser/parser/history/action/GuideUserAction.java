package org.maltparser.parser.history.action;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.container.ActionContainer;

public interface GuideUserAction {
   void addAction(ArrayList<ActionContainer> var1) throws MaltChainedException;

   void addAction(ActionContainer[] var1) throws MaltChainedException;

   void getAction(ArrayList<ActionContainer> var1) throws MaltChainedException;

   void getAction(ActionContainer[] var1) throws MaltChainedException;

   int numberOfActions();
}
