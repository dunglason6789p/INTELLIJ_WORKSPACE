package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.GraphElement;
import org.maltparser.core.syntaxgraph.node.Node;

public class GraphEdge extends GraphElement implements Edge, Comparable<GraphEdge> {
   private Node source = null;
   private Node target = null;
   private int type;

   public GraphEdge() {
   }

   public GraphEdge(Node source, Node target, int type) throws MaltChainedException {
      this.clear();
      this.setEdge(source, target, type);
   }

   public void setEdge(Node source, Node target, int type) throws MaltChainedException {
      this.source = source;
      this.target = target;
      if (type >= 1 && type <= 3) {
         this.type = type;
      }

      this.source.addOutgoingEdge(this);
      this.target.addIncomingEdge(this);
      this.setChanged();
      this.notifyObservers(this);
   }

   public void clear() throws MaltChainedException {
      super.clear();
      if (this.source != null) {
         this.source.removeOutgoingEdge(this);
      }

      if (this.target != null) {
         this.target.removeIncomingEdge(this);
      }

      this.source = null;
      this.target = null;
      this.type = -1;
   }

   public Node getSource() {
      return this.source;
   }

   public Node getTarget() {
      return this.target;
   }

   public int getType() {
      return this.type;
   }

   public int compareTo(GraphEdge that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.target.getCompareToIndex() < that.target.getCompareToIndex()) {
         return -1;
      } else if (this.target.getCompareToIndex() > that.target.getCompareToIndex()) {
         return 1;
      } else if (this.source.getCompareToIndex() < that.source.getCompareToIndex()) {
         return -1;
      } else if (this.source.getCompareToIndex() > that.source.getCompareToIndex()) {
         return 1;
      } else if (this.type < that.type) {
         return -1;
      } else {
         return this.type > that.type ? 1 : super.compareTo(that);
      }
   }

   public boolean equals(Object obj) {
      GraphEdge e = (GraphEdge)obj;
      return this.type == e.getType() && this.source.equals(e.getSource()) && this.target.equals(e.getTarget()) && super.equals(obj);
   }

   public int hashCode() {
      int hash = 7;
      int hash = 31 * hash + this.type;
      hash = 31 * hash + (null == this.source ? 0 : this.source.hashCode());
      hash = 31 * hash + (null == this.target ? 0 : this.target.hashCode());
      return 31 * hash + super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.source.getIndex());
      sb.append("->");
      sb.append(this.target.getIndex());
      sb.append(' ');
      sb.append(super.toString());
      return sb.toString();
   }
}
