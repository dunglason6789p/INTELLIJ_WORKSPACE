package org.maltparser.core.syntaxgraph.node;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.GraphElement;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;

public abstract class GraphNode extends GraphElement implements Node {
   protected SortedSet<Edge> incomingEdges = new TreeSet();
   protected SortedSet<Edge> outgoingEdges = new TreeSet();

   public GraphNode() throws MaltChainedException {
   }

   public void addIncomingEdge(Edge in) throws MaltChainedException {
      if (in.getTarget() != this) {
         throw new SyntaxGraphException("The incoming edge's 'to' reference is not correct.");
      } else {
         this.incomingEdges.add(in);
      }
   }

   public void addOutgoingEdge(Edge out) throws MaltChainedException {
      if (out.getSource() != this) {
         throw new SyntaxGraphException("The outgoing edge's 'from' reference is not correct");
      } else {
         this.outgoingEdges.add(out);
      }
   }

   public void removeIncomingEdge(Edge in) throws MaltChainedException {
      if (in.getTarget() != this) {
         throw new SyntaxGraphException("The incoming edge's 'to' reference is not correct");
      } else {
         this.incomingEdges.remove(in);
      }
   }

   public void removeOutgoingEdge(Edge out) throws MaltChainedException {
      if (out.getSource() != this) {
         throw new SyntaxGraphException("The outgoing edge's 'from' reference is not correct");
      } else {
         this.outgoingEdges.remove(out);
      }
   }

   public int getLeftmostProperDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getLeftmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public int getRightmostProperDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getRightmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public int getLeftmostDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getLeftmostProperDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public int getRightmostDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getRightmostProperDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public Iterator<Edge> getIncomingEdgeIterator() {
      return this.incomingEdges.iterator();
   }

   public Iterator<Edge> getOutgoingEdgeIterator() {
      return this.outgoingEdges.iterator();
   }

   public void clear() throws MaltChainedException {
      super.clear();
      this.incomingEdges.clear();
      this.outgoingEdges.clear();
   }

   public int getInDegree() {
      return this.incomingEdges.size();
   }

   public int getOutDegree() {
      return this.outgoingEdges.size();
   }

   public SortedSet<Edge> getIncomingSecondaryEdges() {
      SortedSet<Edge> inSecEdges = new TreeSet();
      Iterator i$ = this.incomingEdges.iterator();

      while(i$.hasNext()) {
         Edge e = (Edge)i$.next();
         if (e.getType() == 3) {
            inSecEdges.add(e);
         }
      }

      return inSecEdges;
   }

   public SortedSet<Edge> getOutgoingSecondaryEdges() {
      SortedSet<Edge> outSecEdges = new TreeSet();
      Iterator i$ = this.outgoingEdges.iterator();

      while(i$.hasNext()) {
         Edge e = (Edge)i$.next();
         if (e.getType() == 3) {
            outSecEdges.add(e);
         }
      }

      return outSecEdges;
   }

   public int compareTo(ComparableNode o) {
      return super.compareTo((GraphElement)o);
   }

   public abstract int getIndex();

   public abstract void setIndex(int var1) throws MaltChainedException;

   public abstract boolean isRoot();

   public boolean equals(Object obj) {
      GraphNode v = (GraphNode)obj;
      return super.equals(obj) && this.incomingEdges.equals(v.incomingEdges) && this.outgoingEdges.equals(v.outgoingEdges);
   }

   public int hashCode() {
      int hash = 7;
      int hash = 31 * hash + super.hashCode();
      hash = 31 * hash + (null == this.incomingEdges ? 0 : this.incomingEdges.hashCode());
      hash = 31 * hash + (null == this.outgoingEdges ? 0 : this.outgoingEdges.hashCode());
      return hash;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getIndex());
      sb.append(" [I:");
      Iterator i$ = this.incomingEdges.iterator();

      Edge e;
      while(i$.hasNext()) {
         e = (Edge)i$.next();
         sb.append(e.getSource().getIndex());
         sb.append("(");
         sb.append(e.toString());
         sb.append(")");
         if (this.incomingEdges.last() != e) {
            sb.append(",");
         }
      }

      sb.append("][O:");
      i$ = this.outgoingEdges.iterator();

      while(i$.hasNext()) {
         e = (Edge)i$.next();
         sb.append(e.getTarget().getIndex());
         if (this.outgoingEdges.last() != e) {
            sb.append(",");
         }
      }

      sb.append("]");
      sb.append(super.toString());
      return sb.toString();
   }
}
