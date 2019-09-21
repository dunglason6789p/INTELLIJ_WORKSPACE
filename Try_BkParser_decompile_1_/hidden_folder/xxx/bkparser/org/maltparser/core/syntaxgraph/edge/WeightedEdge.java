package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Weightable;
import org.maltparser.core.syntaxgraph.node.Node;

public class WeightedEdge extends GraphEdge implements Weightable {
   private Double weight = 0.0D / 0.0;

   public WeightedEdge() {
   }

   public WeightedEdge(Node source, Node target, int type) throws MaltChainedException {
      super(source, target, type);
   }

   public WeightedEdge(Node source, Node target, int type, Double weight) throws MaltChainedException {
      super(source, target, type);
      this.setWeight(weight);
   }

   public void clear() throws MaltChainedException {
      super.clear();
      this.weight = 0.0D / 0.0;
   }

   public double getWeight() {
      return this.weight;
   }

   public void setWeight(double weight) {
      this.weight = weight;
   }

   public int compareTo(WeightedEdge that) {
      if (this == that) {
         return 0;
      } else {
         int comparison = this.weight.compareTo(that.getWeight());
         return comparison != 0 ? comparison : super.compareTo((GraphEdge)that);
      }
   }

   public boolean equals(Object obj) {
      WeightedEdge e = (WeightedEdge)obj;
      return this.weight.equals(e.getWeight()) && super.equals(obj);
   }

   public int hashCode() {
      int hash = 7;
      int hash = 31 * hash + (null == this.weight ? 0 : this.weight.hashCode());
      return 31 * hash + super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getWeight());
      sb.append(' ');
      sb.append(this.getSource().getIndex());
      sb.append("->");
      sb.append(this.getTarget().getIndex());
      sb.append(' ');
      sb.append(super.toString());
      return sb.toString();
   }
}
