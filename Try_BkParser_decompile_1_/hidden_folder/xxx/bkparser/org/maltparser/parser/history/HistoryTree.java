package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryTree extends HistoryStructure {
   private final HistoryTreeNode root = new HistoryTreeNode((HistoryNode)null, (GuideUserAction)null);
   protected final ObjectPoolList<HistoryNode> nodePool = new ObjectPoolList<HistoryNode>() {
      protected HistoryNode create() throws MaltChainedException {
         return new HistoryTreeNode((HistoryNode)null, (GuideUserAction)null);
      }

      public void resetObject(HistoryNode o) throws MaltChainedException {
         o.clear();
      }
   };

   public HistoryTree() {
   }

   public HistoryNode getNewHistoryNode(HistoryNode previousNode, GuideUserAction action) throws MaltChainedException {
      HistoryNode node = (HistoryNode)this.nodePool.checkOut();
      node.setAction(action);
      if (previousNode == null) {
         node.setPreviousNode(this.root);
      } else {
         node.setPreviousNode(previousNode);
      }

      return node;
   }

   public void clear() throws MaltChainedException {
      this.nodePool.checkInAll();
      this.root.clear();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.root.toString());
      return sb.toString();
   }
}
