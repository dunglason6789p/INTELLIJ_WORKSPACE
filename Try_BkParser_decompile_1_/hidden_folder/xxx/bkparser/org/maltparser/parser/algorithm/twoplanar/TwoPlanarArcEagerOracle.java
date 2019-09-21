package org.maltparser.parser.algorithm.twoplanar;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class TwoPlanarArcEagerOracle extends Oracle {
   private static final int ANY_PLANE = 0;
   private static final int FIRST_PLANE = 1;
   private static final int SECOND_PLANE = 2;
   private static final int NO_PLANE = 3;
   private Map<Edge, Integer> linksToPlanes = new IdentityHashMap();
   private Map<Edge, List<Edge>> crossingsGraph = null;

   public TwoPlanarArcEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
      super(manager, history);
      this.setGuideName("Two-Planar");
   }

   public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
      TwoPlanarConfig planarConfig = (TwoPlanarConfig)config;
      DependencyStructure dg = planarConfig.getDependencyGraph();
      DependencyNode activeStackPeek = (DependencyNode)planarConfig.getActiveStack().peek();
      DependencyNode inactiveStackPeek = (DependencyNode)planarConfig.getInactiveStack().peek();
      int activeStackPeekIndex = activeStackPeek.getIndex();
      int inactiveStackPeekIndex = inactiveStackPeek.getIndex();
      int inputPeekIndex = ((DependencyNode)planarConfig.getInput().peek()).getIndex();
      if (this.crossingsGraph == null) {
         this.initCrossingsGraph(gold);
      }

      if (!activeStackPeek.isRoot() && gold.getTokenNode(activeStackPeekIndex).getHead().getIndex() == inputPeekIndex && !this.checkIfArcExists(dg, inputPeekIndex, activeStackPeekIndex)) {
         if (!planarConfig.getStackActivityState()) {
            this.propagatePlaneConstraint(gold.getTokenNode(activeStackPeekIndex).getHeadEdge(), 1);
         } else {
            this.propagatePlaneConstraint(gold.getTokenNode(activeStackPeekIndex).getHeadEdge(), 2);
         }

         return this.updateActionContainers(4, gold.getTokenNode(activeStackPeekIndex).getHeadEdge().getLabelSet());
      } else if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == activeStackPeekIndex && !this.checkIfArcExists(dg, activeStackPeekIndex, inputPeekIndex)) {
         if (!planarConfig.getStackActivityState()) {
            this.propagatePlaneConstraint(gold.getTokenNode(inputPeekIndex).getHeadEdge(), 1);
         } else {
            this.propagatePlaneConstraint(gold.getTokenNode(inputPeekIndex).getHeadEdge(), 2);
         }

         return this.updateActionContainers(3, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
      } else if (!inactiveStackPeek.isRoot() && gold.getTokenNode(inactiveStackPeekIndex).getHead().getIndex() == inputPeekIndex && !this.checkIfArcExists(dg, inputPeekIndex, inactiveStackPeekIndex)) {
         return this.updateActionContainers(2, (LabelSet)null);
      } else if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == inactiveStackPeekIndex && !this.checkIfArcExists(dg, inactiveStackPeekIndex, inputPeekIndex)) {
         return this.updateActionContainers(2, (LabelSet)null);
      } else if (this.getFirstPendingLinkOnActivePlane(planarConfig, gold) != null) {
         return this.updateActionContainers(5, (LabelSet)null);
      } else {
         return this.getFirstPendingLinkOnInactivePlane(planarConfig, gold) != null ? this.updateActionContainers(2, (LabelSet)null) : this.updateActionContainers(1, (LabelSet)null);
      }
   }

   private boolean checkIfArcExists(DependencyStructure dg, int index1, int index2) throws MaltChainedException {
      return dg.getTokenNode(index2).hasHead() && dg.getTokenNode(index2).getHead().getIndex() == index1;
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      this.crossingsGraph = null;
      this.linksToPlanes.clear();
   }

   public void terminate() throws MaltChainedException {
   }

   private static boolean cross(Edge e1, Edge e2) {
      int xSource = e1.getSource().getIndex();
      int xTarget = e1.getTarget().getIndex();
      int ySource = e2.getSource().getIndex();
      int yTarget = e2.getTarget().getIndex();
      int xMin = Math.min(xSource, xTarget);
      int xMax = Math.max(xSource, xTarget);
      int yMin = Math.min(ySource, yTarget);
      int yMax = Math.max(ySource, yTarget);
      return xMin < yMin && yMin < xMax && xMax < yMax || yMin < xMin && xMin < yMax && yMax < xMax;
   }

   private void initCrossingsGraph(DependencyStructure dg) {
      this.crossingsGraph = new IdentityHashMap();
      SortedSet<Edge> edges = dg.getEdges();
      Iterator iterator1 = edges.iterator();

      while(iterator1.hasNext()) {
         Edge edge1 = (Edge)iterator1.next();
         Iterator iterator2 = edges.iterator();

         while(iterator2.hasNext()) {
            Edge edge2 = (Edge)iterator2.next();
            if (edge1.getSource().getIndex() < edge2.getSource().getIndex() && cross(edge1, edge2)) {
               List<Edge> crossingEdge1 = (List)this.crossingsGraph.get(edge1);
               if (crossingEdge1 == null) {
                  crossingEdge1 = new LinkedList();
                  this.crossingsGraph.put(edge1, crossingEdge1);
               }

               ((List)crossingEdge1).add(edge2);
               List<Edge> crossingEdge2 = (List)this.crossingsGraph.get(edge2);
               if (crossingEdge2 == null) {
                  crossingEdge2 = new LinkedList();
                  this.crossingsGraph.put(edge2, crossingEdge2);
               }

               ((List)crossingEdge2).add(edge1);
            }
         }
      }

   }

   private List<Edge> getCrossingEdges(Edge e) {
      return (List)this.crossingsGraph.get(e);
   }

   private void setPlaneConstraint(Edge e, int requiredPlane) {
      this.linksToPlanes.put(e, requiredPlane);
   }

   private int getPlaneConstraint(Edge e) {
      Integer constr = (Integer)this.linksToPlanes.get(e);
      if (constr == null) {
         this.setPlaneConstraint(e, 0);
         return 0;
      } else {
         return constr;
      }
   }

   private void propagatePlaneConstraint(Edge e, int requiredPlane) {
      this.setPlaneConstraint(e, requiredPlane);
      if (requiredPlane == 1 || requiredPlane == 2) {
         List<Edge> crossingEdges = this.getCrossingEdges(e);
         if (crossingEdges != null) {
            Iterator iterator = crossingEdges.iterator();

            while(iterator.hasNext()) {
               Edge crossingEdge = (Edge)iterator.next();

               assert requiredPlane == 1 || requiredPlane == 2;

               int crossingEdgeConstraint = this.getPlaneConstraint(crossingEdge);
               if (crossingEdgeConstraint == 0) {
                  if (requiredPlane == 1) {
                     this.propagatePlaneConstraint(crossingEdge, 2);
                  } else if (requiredPlane == 2) {
                     this.propagatePlaneConstraint(crossingEdge, 1);
                  }
               } else if (crossingEdgeConstraint != 3) {
                  if (crossingEdgeConstraint == 1) {
                     if (requiredPlane == 1) {
                        this.propagatePlaneConstraint(crossingEdge, 3);
                     }
                  } else if (crossingEdgeConstraint == 2 && requiredPlane == 2) {
                     this.propagatePlaneConstraint(crossingEdge, 3);
                  }
               }
            }
         }
      }

   }

   private int getLinkDecision(Edge e, TwoPlanarConfig config) {
      int constraint = this.getPlaneConstraint(e);
      if (constraint == 0) {
         return !config.getStackActivityState() ? 1 : 2;
      } else {
         return constraint;
      }
   }

   private Edge getFirstPendingLinkOnActivePlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
      return this.getFirstPendingLinkOnPlane(config, gold, !config.getStackActivityState() ? 1 : 2, ((DependencyNode)config.getActiveStack().peek()).getIndex());
   }

   private Edge getFirstPendingLinkOnInactivePlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
      return this.getFirstPendingLinkOnPlane(config, gold, !config.getStackActivityState() ? 2 : 1, ((DependencyNode)config.getInactiveStack().peek()).getIndex());
   }

   private Edge getFirstPendingLinkOnAnyPlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
      Edge e1 = this.getFirstPendingLinkOnActivePlane(config, gold);
      Edge e2 = this.getFirstPendingLinkOnInactivePlane(config, gold);
      int left1 = Math.min(e1.getSource().getIndex(), e1.getTarget().getIndex());
      int left2 = Math.min(e2.getSource().getIndex(), e2.getTarget().getIndex());
      return left1 > left2 ? e1 : e2;
   }

   private Edge getFirstPendingLinkOnPlane(TwoPlanarConfig config, DependencyStructure gold, int plane, int rightmostLimit) throws MaltChainedException {
      int inputPeekIndex = ((DependencyNode)config.getInput().peek()).getIndex();
      Edge current = null;
      int maxIndex;
      if (config.getRootHandling() == 1) {
         maxIndex = -1;
      } else {
         maxIndex = 0;
      }

      if (gold.getTokenNode(inputPeekIndex).hasLeftDependent() && gold.getTokenNode(inputPeekIndex).getLeftmostDependent().getIndex() < rightmostLimit) {
         SortedSet<DependencyNode> dependents = gold.getTokenNode(inputPeekIndex).getLeftDependents();
         Iterator iterator = dependents.iterator();

         while(iterator.hasNext()) {
            DependencyNode dependent = (DependencyNode)iterator.next();
            if (dependent.getIndex() > maxIndex && dependent.getIndex() < rightmostLimit && this.getLinkDecision(dependent.getHeadEdge(), config) == plane) {
               maxIndex = dependent.getIndex();
               current = dependent.getHeadEdge();
            }
         }
      }

      if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() < rightmostLimit && gold.getTokenNode(inputPeekIndex).getHead().getIndex() > maxIndex && this.getLinkDecision(gold.getTokenNode(inputPeekIndex).getHeadEdge(), config) == plane) {
         current = gold.getTokenNode(inputPeekIndex).getHeadEdge();
      }

      return current;
   }
}
