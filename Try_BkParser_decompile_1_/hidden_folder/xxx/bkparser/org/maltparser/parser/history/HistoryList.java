package org.maltparser.parser.history;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryList extends HistoryStructure {
   protected final ArrayList<HistoryNode> list = new ArrayList();
   protected final ObjectPoolList<HistoryNode> nodePool = new ObjectPoolList<HistoryNode>() {
      protected HistoryNode create() throws MaltChainedException {
         return new HistoryListNode((HistoryNode)null, (GuideUserAction)null);
      }

      public void resetObject(HistoryNode o) throws MaltChainedException {
         o.clear();
      }
   };

   public HistoryList() throws MaltChainedException {
   }

   public HistoryNode getNewHistoryNode(HistoryNode previousNode, GuideUserAction action) throws MaltChainedException {
      HistoryNode node = (HistoryNode)this.nodePool.checkOut();
      node.setAction(action);
      node.setPreviousNode(previousNode);
      this.list.add(node);
      return node;
   }

   public void clear() throws MaltChainedException {
      this.nodePool.checkInAll();
      this.list.clear();
   }

   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   public int hashCode() {
      return super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.list.size(); ++i) {
         sb.append(this.list.get(i));
         if (i < this.list.size() - 1) {
            sb.append(", ");
         }
      }

      return sb.toString();
   }
}
