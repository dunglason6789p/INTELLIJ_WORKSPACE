package org.maltparser.concurrent.graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;

public final class ConcurrentDependencyNode implements Comparable<ConcurrentDependencyNode> {
   private final ConcurrentDependencyGraph graph;
   private final int index;
   private final SortedMap<Integer, String> labels;
   private final int headIndex;

   protected ConcurrentDependencyNode(ConcurrentDependencyNode node) throws ConcurrentGraphException {
      this(node.graph, node);
   }

   protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, ConcurrentDependencyNode node) throws ConcurrentGraphException {
      if (_graph == null) {
         throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
      } else {
         this.graph = _graph;
         this.index = node.index;
         this.labels = new TreeMap(node.labels);
         this.headIndex = node.headIndex;
      }
   }

   protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, int _index, SortedMap<Integer, String> _labels, int _headIndex) throws ConcurrentGraphException {
      if (_graph == null) {
         throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
      } else if (_index < 0) {
         throw new ConcurrentGraphException("Not allowed to have negative node index");
      } else if (_headIndex < -1) {
         throw new ConcurrentGraphException("Not allowed to have head index less than -1.");
      } else if (_index == 0 && _headIndex != -1) {
         throw new ConcurrentGraphException("Not allowed to add head to a root node.");
      } else if (_index == _headIndex) {
         throw new ConcurrentGraphException("Not allowed to add head to itself");
      } else {
         this.graph = _graph;
         this.index = _index;
         this.labels = new TreeMap(_labels);
         this.headIndex = _headIndex;
      }
   }

   protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, int _index, String[] _labels) throws ConcurrentGraphException {
      if (_graph == null) {
         throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
      } else if (_index < 0) {
         throw new ConcurrentGraphException("Not allowed to have negative node index");
      } else {
         this.graph = _graph;
         this.index = _index;
         this.labels = new TreeMap();
         int tmpHeadIndex = -1;
         if (_labels != null) {
            for(int i = 0; i < _labels.length; ++i) {
               int columnCategory = this.graph.getDataFormat().getColumnDescription(i).getCategory();
               if (columnCategory == 2) {
                  tmpHeadIndex = Integer.parseInt(_labels[i]);
               } else if (columnCategory == 1 || columnCategory == 3) {
                  this.labels.put(i, _labels[i]);
               }
            }
         }

         this.headIndex = tmpHeadIndex;
         if (this.headIndex < -1) {
            throw new ConcurrentGraphException("Not allowed to have head index less than -1.");
         } else if (this.index == 0 && this.headIndex != -1) {
            throw new ConcurrentGraphException("Not allowed to add head to a root node.");
         } else if (this.index == this.headIndex) {
            throw new ConcurrentGraphException("Not allowed to add head to itself");
         }
      }
   }

   public int getIndex() {
      return this.index;
   }

   public String getLabel(int columnPosition) {
      if (this.labels.containsKey(columnPosition)) {
         return (String)this.labels.get(columnPosition);
      } else {
         return this.graph.getDataFormat().getColumnDescription(columnPosition).getCategory() == 7 ? this.graph.getDataFormat().getColumnDescription(columnPosition).getDefaultOutput() : "";
      }
   }

   public String getLabel(String columnName) {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
      return column != null ? this.getLabel(column.getPosition()) : "";
   }

   public String getLabel(ColumnDescription column) {
      return this.getLabel(column.getPosition());
   }

   public boolean hasLabel(int columnPosition) {
      return this.labels.containsKey(columnPosition);
   }

   public boolean hasLabel(String columnName) {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
      return column != null ? this.hasLabel(column.getPosition()) : false;
   }

   public boolean hasLabel(ColumnDescription column) {
      return this.labels.containsKey(column.getPosition());
   }

   public boolean isLabeled() {
      Iterator i$ = this.labels.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         key = (Integer)i$.next();
      } while(this.graph.getDataFormat().getColumnDescription(key).getCategory() != 1);

      return true;
   }

   public boolean isHeadLabeled() {
      Iterator i$ = this.labels.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         key = (Integer)i$.next();
      } while(this.graph.getDataFormat().getColumnDescription(key).getCategory() != 3);

      return true;
   }

   public int getHeadIndex() {
      return this.headIndex;
   }

   public SortedMap<ColumnDescription, String> getNodeLabels() {
      SortedMap<ColumnDescription, String> nodeLabels = Collections.synchronizedSortedMap(new TreeMap());
      Iterator i$ = this.labels.keySet().iterator();

      while(i$.hasNext()) {
         Integer key = (Integer)i$.next();
         if (this.graph.getDataFormat().getColumnDescription(key).getCategory() == 1) {
            nodeLabels.put(this.graph.getDataFormat().getColumnDescription(key), this.labels.get(key));
         }
      }

      return nodeLabels;
   }

   public SortedMap<ColumnDescription, String> getEdgeLabels() {
      SortedMap<ColumnDescription, String> edgeLabels = Collections.synchronizedSortedMap(new TreeMap());
      Iterator i$ = this.labels.keySet().iterator();

      while(i$.hasNext()) {
         Integer key = (Integer)i$.next();
         if (this.graph.getDataFormat().getColumnDescription(key).getCategory() == 3) {
            edgeLabels.put(this.graph.getDataFormat().getColumnDescription(key), this.labels.get(key));
         }
      }

      return edgeLabels;
   }

   public ConcurrentDependencyNode getPredecessor() {
      return this.index > 1 ? this.graph.getDependencyNode(this.index - 1) : null;
   }

   public ConcurrentDependencyNode getSuccessor() {
      return this.graph.getDependencyNode(this.index + 1);
   }

   public boolean isRoot() {
      return this.index == 0;
   }

   public boolean hasAtMostOneHead() {
      return true;
   }

   public boolean hasHead() {
      return this.headIndex != -1;
   }

   public boolean hasDependent() {
      return this.graph.hasDependent(this.index);
   }

   public boolean hasLeftDependent() {
      return this.graph.hasLeftDependent(this.index);
   }

   public boolean hasRightDependent() {
      return this.graph.hasRightDependent(this.index);
   }

   public SortedSet<ConcurrentDependencyNode> getHeads() {
      SortedSet<ConcurrentDependencyNode> heads = Collections.synchronizedSortedSet(new TreeSet());
      ConcurrentDependencyNode head = this.getHead();
      if (head != null) {
         heads.add(head);
      }

      return heads;
   }

   public ConcurrentDependencyNode getHead() {
      return this.graph.getDependencyNode(this.headIndex);
   }

   public ConcurrentDependencyNode getLeftDependent(int leftDependentIndex) {
      List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependentIndex >= 0 && leftDependentIndex < leftDependents.size() ? (ConcurrentDependencyNode)leftDependents.get(leftDependentIndex) : null;
   }

   public int getLeftDependentCount() {
      return this.graph.getListOfLeftDependents(this.index).size();
   }

   public SortedSet<ConcurrentDependencyNode> getLeftDependents() {
      return this.graph.getSortedSetOfLeftDependents(this.index);
   }

   public List<ConcurrentDependencyNode> getListOfLeftDependents() {
      return this.graph.getListOfLeftDependents(this.index);
   }

   public ConcurrentDependencyNode getLeftSibling() {
      if (this.headIndex == -1) {
         return null;
      } else {
         int nodeDepedentPosition = 0;
         List<ConcurrentDependencyNode> headDependents = this.getHead().getListOfDependents();

         for(int i = 0; i < headDependents.size(); ++i) {
            if (((ConcurrentDependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition > 0 ? (ConcurrentDependencyNode)headDependents.get(nodeDepedentPosition - 1) : null;
      }
   }

   public ConcurrentDependencyNode getSameSideLeftSibling() {
      if (this.headIndex == -1) {
         return null;
      } else {
         List headDependents;
         if (this.index < this.headIndex) {
            headDependents = this.getHead().getListOfLeftDependents();
         } else {
            headDependents = this.getHead().getListOfRightDependents();
         }

         int nodeDepedentPosition = 0;

         for(int i = 0; i < headDependents.size(); ++i) {
            if (((ConcurrentDependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition > 0 ? (ConcurrentDependencyNode)headDependents.get(nodeDepedentPosition - 1) : null;
      }
   }

   public ConcurrentDependencyNode getClosestLeftDependent() {
      List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependents.size() > 0 ? (ConcurrentDependencyNode)leftDependents.get(leftDependents.size() - 1) : null;
   }

   public ConcurrentDependencyNode getLeftmostDependent() {
      List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependents.size() > 0 ? (ConcurrentDependencyNode)leftDependents.get(0) : null;
   }

   public ConcurrentDependencyNode getRightDependent(int rightDependentIndex) {
      List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependentIndex >= 0 && rightDependentIndex < rightDependents.size() ? (ConcurrentDependencyNode)rightDependents.get(rightDependents.size() - 1 - rightDependentIndex) : null;
   }

   public int getRightDependentCount() {
      return this.graph.getListOfRightDependents(this.index).size();
   }

   public SortedSet<ConcurrentDependencyNode> getRightDependents() {
      return this.graph.getSortedSetOfRightDependents(this.index);
   }

   public List<ConcurrentDependencyNode> getListOfRightDependents() {
      return this.graph.getListOfRightDependents(this.index);
   }

   public ConcurrentDependencyNode getRightSibling() {
      if (this.headIndex == -1) {
         return null;
      } else {
         List<ConcurrentDependencyNode> headDependents = this.getHead().getListOfDependents();
         int nodeDepedentPosition = headDependents.size() - 1;

         for(int i = headDependents.size() - 1; i >= 0; --i) {
            if (((ConcurrentDependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition < headDependents.size() - 1 ? (ConcurrentDependencyNode)headDependents.get(nodeDepedentPosition + 1) : null;
      }
   }

   public ConcurrentDependencyNode getSameSideRightSibling() {
      if (this.headIndex == -1) {
         return null;
      } else {
         List headDependents;
         if (this.index < this.headIndex) {
            headDependents = this.getHead().getListOfLeftDependents();
         } else {
            headDependents = this.getHead().getListOfRightDependents();
         }

         int nodeDepedentPosition = headDependents.size() - 1;

         for(int i = headDependents.size() - 1; i >= 0; --i) {
            if (((ConcurrentDependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition < headDependents.size() - 1 ? (ConcurrentDependencyNode)headDependents.get(nodeDepedentPosition + 1) : null;
      }
   }

   public ConcurrentDependencyNode getClosestRightDependent() {
      List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependents.size() > 0 ? (ConcurrentDependencyNode)rightDependents.get(0) : null;
   }

   public ConcurrentDependencyNode getRightmostDependent() {
      List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependents.size() > 0 ? (ConcurrentDependencyNode)rightDependents.get(rightDependents.size() - 1) : null;
   }

   public SortedSet<ConcurrentDependencyNode> getDependents() {
      return this.graph.getSortedSetOfDependents(this.index);
   }

   public List<ConcurrentDependencyNode> getListOfDependents() {
      return this.graph.getListOfDependents(this.index);
   }

   public int getInDegree() {
      return this.hasHead() ? 1 : 0;
   }

   public int getOutDegree() {
      return this.graph.getListOfDependents(this.index).size();
   }

   public ConcurrentDependencyNode getAncestor() {
      if (!this.hasHead()) {
         return this;
      } else {
         ConcurrentDependencyNode tmp;
         for(tmp = this; tmp.hasHead(); tmp = tmp.getHead()) {
         }

         return tmp;
      }
   }

   public ConcurrentDependencyNode getProperAncestor() {
      if (!this.hasHead()) {
         return null;
      } else {
         ConcurrentDependencyNode tmp;
         for(tmp = this; tmp.hasHead() && !tmp.isRoot(); tmp = tmp.getHead()) {
         }

         return tmp;
      }
   }

   public boolean hasAncestorInside(int left, int right) {
      if (this.index == 0) {
         return false;
      } else {
         if (this.getHead() != null) {
            ConcurrentDependencyNode tmp = this.getHead();
            if (tmp.getIndex() >= left && tmp.getIndex() <= right) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isProjective() {
      if (this.headIndex > 0) {
         ConcurrentDependencyNode head = this.getHead();
         ConcurrentDependencyNode terminals;
         ConcurrentDependencyNode tmp;
         if (this.headIndex < this.index) {
            terminals = head;
            tmp = null;

            while(true) {
               if (terminals == null || terminals.getSuccessor() == null) {
                  return false;
               }

               if (terminals.getSuccessor() == this) {
                  break;
               }

               for(tmp = terminals = terminals.getSuccessor(); tmp != this && tmp != head; tmp = tmp.getHead()) {
                  if (!tmp.hasHead()) {
                     return false;
                  }
               }
            }
         } else {
            terminals = this;
            tmp = null;

            while(true) {
               if (terminals == null || terminals.getSuccessor() == null) {
                  return false;
               }

               if (terminals.getSuccessor() == head) {
                  break;
               }

               for(tmp = terminals = terminals.getSuccessor(); tmp != this && tmp != head; tmp = tmp.getHead()) {
                  if (!tmp.hasHead()) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   public int getDependencyNodeDepth() {
      ConcurrentDependencyNode tmp = this;

      int depth;
      for(depth = 0; tmp.hasHead(); tmp = tmp.getHead()) {
         ++depth;
      }

      return depth;
   }

   public ConcurrentDependencyNode getLeftmostProperDescendant() {
      ConcurrentDependencyNode candidate = null;
      List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         ConcurrentDependencyNode dep = (ConcurrentDependencyNode)dependents.get(i);
         if (candidate == null || dep.getIndex() < candidate.getIndex()) {
            candidate = dep;
         }

         ConcurrentDependencyNode tmp = dep.getLeftmostProperDescendant();
         if (tmp != null) {
            if (candidate == null || tmp.getIndex() < candidate.getIndex()) {
               candidate = tmp;
            }

            if (candidate.getIndex() == 1) {
               return candidate;
            }
         }
      }

      return candidate;
   }

   public ConcurrentDependencyNode getRightmostProperDescendant() {
      ConcurrentDependencyNode candidate = null;
      List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         ConcurrentDependencyNode dep = (ConcurrentDependencyNode)dependents.get(i);
         if (candidate == null || dep.getIndex() > candidate.getIndex()) {
            candidate = dep;
         }

         ConcurrentDependencyNode tmp = dep.getRightmostProperDescendant();
         if (tmp != null && (candidate == null || tmp.getIndex() > candidate.getIndex())) {
            candidate = tmp;
         }
      }

      return candidate;
   }

   public int getLeftmostProperDescendantIndex() {
      ConcurrentDependencyNode node = this.getLeftmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public int getRightmostProperDescendantIndex() {
      ConcurrentDependencyNode node = this.getRightmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public ConcurrentDependencyNode getLeftmostDescendant() {
      ConcurrentDependencyNode candidate = this;
      List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         ConcurrentDependencyNode dep = (ConcurrentDependencyNode)dependents.get(i);
         if (dep.getIndex() < candidate.getIndex()) {
            candidate = dep;
         }

         ConcurrentDependencyNode tmp = dep.getLeftmostDescendant();
         if (tmp != null) {
            if (tmp.getIndex() < candidate.getIndex()) {
               candidate = tmp;
            }

            if (candidate.getIndex() == 1) {
               return candidate;
            }
         }
      }

      return candidate;
   }

   public ConcurrentDependencyNode getRightmostDescendant() {
      ConcurrentDependencyNode candidate = this;
      List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         ConcurrentDependencyNode dep = (ConcurrentDependencyNode)dependents.get(i);
         if (dep.getIndex() > candidate.getIndex()) {
            candidate = dep;
         }

         ConcurrentDependencyNode tmp = dep.getRightmostDescendant();
         if (tmp != null && tmp.getIndex() > candidate.getIndex()) {
            candidate = tmp;
         }
      }

      return candidate;
   }

   public int getLeftmostDescendantIndex() {
      ConcurrentDependencyNode node = this.getLeftmostDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public int getRightmostDescendantIndex() {
      ConcurrentDependencyNode node = this.getRightmostDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public ConcurrentDependencyNode findComponent() {
      return this.graph.findComponent(this.index);
   }

   public int getRank() {
      return this.graph.getRank(this.index);
   }

   public ConcurrentDependencyEdge getHeadEdge() throws ConcurrentGraphException {
      return !this.hasHead() ? null : new ConcurrentDependencyEdge(this.graph.getDataFormat(), this.getHead(), this, this.labels);
   }

   public SortedSet<ConcurrentDependencyEdge> getHeadEdges() throws ConcurrentGraphException {
      SortedSet<ConcurrentDependencyEdge> edges = Collections.synchronizedSortedSet(new TreeSet());
      if (this.hasHead()) {
         edges.add(new ConcurrentDependencyEdge(this.graph.getDataFormat(), this.getHead(), this, this.labels));
      }

      return edges;
   }

   public boolean isHeadEdgeLabeled() {
      return this.getEdgeLabels().size() > 0;
   }

   public int nHeadEdgeLabels() {
      return this.getEdgeLabels().size();
   }

   public DataFormat getDataFormat() {
      return this.graph.getDataFormat();
   }

   public int compareTo(ConcurrentDependencyNode that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.index < that.getIndex()) {
         return -1;
      } else {
         return this.index > that.getIndex() ? 1 : 0;
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.headIndex;
      result = 31 * result + this.index;
      result = 31 * result + (this.labels == null ? 0 : this.labels.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         ConcurrentDependencyNode other = (ConcurrentDependencyNode)obj;
         if (this.headIndex != other.headIndex) {
            return false;
         } else if (this.index != other.index) {
            return false;
         } else {
            if (this.labels == null) {
               if (other.labels != null) {
                  return false;
               }
            } else if (!this.labels.equals(other.labels)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.graph.getDataFormat().numberOfColumns(); ++i) {
         ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
         if (!column.isInternal()) {
            if (column.getCategory() == 2) {
               sb.append(this.headIndex);
            } else if (column.getCategory() != 1 && column.getCategory() != 3) {
               if (column.getCategory() == 7) {
                  sb.append(column.getDefaultOutput());
               }
            } else {
               sb.append((String)this.labels.get(column.getPosition()));
            }

            sb.append('\t');
         }
      }

      sb.setLength(sb.length() > 0 ? sb.length() - 1 : 0);
      return sb.toString();
   }
}
