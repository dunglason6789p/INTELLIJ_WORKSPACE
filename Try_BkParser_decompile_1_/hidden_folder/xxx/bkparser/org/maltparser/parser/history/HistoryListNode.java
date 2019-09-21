package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryListNode implements HistoryNode {
   private HistoryNode previousNode;
   private GuideUserAction action;
   private int position;

   public HistoryListNode(HistoryNode _previousNode, GuideUserAction _action) {
      this.previousNode = _previousNode;
      this.action = _action;
      if (this.previousNode != null) {
         this.position = this.previousNode.getPosition() + 1;
      } else {
         this.position = 1;
      }

   }

   public HistoryNode getPreviousNode() {
      return this.previousNode;
   }

   public GuideUserAction getAction() {
      return this.action;
   }

   public void setPreviousNode(HistoryNode node) {
      this.previousNode = node;
   }

   public void setAction(GuideUserAction action) {
      this.action = action;
   }

   public int getPosition() {
      return this.position;
   }

   public void clear() throws MaltChainedException {
      this.previousNode = null;
      this.action = null;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.action);
      return sb.toString();
   }
}
