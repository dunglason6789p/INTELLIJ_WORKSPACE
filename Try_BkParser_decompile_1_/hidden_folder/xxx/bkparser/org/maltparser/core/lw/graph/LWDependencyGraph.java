package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.RootLabels;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public final class LWDependencyGraph implements DependencyStructure {
   private static final String TAB_SIGN = "\t";
   private final DataFormat dataFormat;
   private final SymbolTableHandler symbolTables;
   private final RootLabels rootLabels;
   private final List<LWNode> nodes;
   private final HashMap<Integer, ArrayList<String>> comments;

   public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables) throws MaltChainedException {
      this.dataFormat = _dataFormat;
      this.symbolTables = _symbolTables;
      this.rootLabels = new RootLabels();
      this.nodes = new ArrayList();
      this.nodes.add(new LWNode(this, 0));
      this.comments = new HashMap();
   }

   public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables, String[] inputTokens, String defaultRootLabel) throws MaltChainedException {
      this(_dataFormat, _symbolTables, inputTokens, defaultRootLabel, true);
   }

   public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables, String[] inputTokens, String defaultRootLabel, boolean addEdges) throws MaltChainedException {
      this.dataFormat = _dataFormat;
      this.symbolTables = _symbolTables;
      this.rootLabels = new RootLabels();
      this.nodes = new ArrayList(inputTokens.length + 1);
      this.comments = new HashMap();
      this.resetTokens(inputTokens, defaultRootLabel, addEdges);
   }

   public void resetTokens(String[] inputTokens, String defaultRootLabel, boolean addEdges) throws MaltChainedException {
      this.nodes.clear();
      this.comments.clear();
      this.symbolTables.cleanUp();
      this.nodes.add(new LWNode(this, 0));

      int i;
      for(i = 0; i < inputTokens.length; ++i) {
         this.nodes.add(new LWNode(this, i + 1));
      }

      for(i = 0; i < inputTokens.length; ++i) {
         ((LWNode)this.nodes.get(i + 1)).addColumnLabels(inputTokens[i].split("\t"), addEdges);
      }

      for(i = 0; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).getHeadIndex() >= this.nodes.size()) {
            throw new LWGraphException("Not allowed to add a head node that doesn't exists");
         }
      }

      for(i = 0; i < this.dataFormat.numberOfColumns(); ++i) {
         ColumnDescription column = this.dataFormat.getColumnDescription(i);
         if (!column.isInternal() && column.getCategory() == 3) {
            this.rootLabels.setDefaultRootLabel(this.symbolTables.getSymbolTable(column.getName()), defaultRootLabel);
         }
      }

   }

   public DataFormat getDataFormat() {
      return this.dataFormat;
   }

   public LWNode getNode(int nodeIndex) {
      return nodeIndex >= 0 && nodeIndex < this.nodes.size() ? (LWNode)this.nodes.get(nodeIndex) : null;
   }

   public int nNodes() {
      return this.nodes.size();
   }

   protected boolean hasDependent(int nodeIndex) {
      for(int i = 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasLeftDependent(int nodeIndex) {
      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasRightDependent(int nodeIndex) {
      for(int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected List<DependencyNode> getListOfLeftDependents(int nodeIndex) {
      List<DependencyNode> leftDependents = Collections.synchronizedList(new ArrayList());

      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            leftDependents.add(this.nodes.get(i));
         }
      }

      return leftDependents;
   }

   protected SortedSet<DependencyNode> getSortedSetOfLeftDependents(int nodeIndex) {
      SortedSet<DependencyNode> leftDependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            leftDependents.add(this.nodes.get(i));
         }
      }

      return leftDependents;
   }

   protected List<DependencyNode> getListOfRightDependents(int nodeIndex) {
      List<DependencyNode> rightDependents = Collections.synchronizedList(new ArrayList());

      for(int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            rightDependents.add(this.nodes.get(i));
         }
      }

      return rightDependents;
   }

   protected SortedSet<DependencyNode> getSortedSetOfRightDependents(int nodeIndex) {
      SortedSet<DependencyNode> rightDependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            rightDependents.add(this.nodes.get(i));
         }
      }

      return rightDependents;
   }

   protected List<DependencyNode> getListOfDependents(int nodeIndex) {
      List<DependencyNode> dependents = Collections.synchronizedList(new ArrayList());

      for(int i = 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            dependents.add(this.nodes.get(i));
         }
      }

      return dependents;
   }

   protected SortedSet<DependencyNode> getSortedSetOfDependents(int nodeIndex) {
      SortedSet<DependencyNode> dependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.size(); ++i) {
         if (nodeIndex == ((LWNode)this.nodes.get(i)).getHeadIndex()) {
            dependents.add(this.nodes.get(i));
         }
      }

      return dependents;
   }

   protected int getRank(int nodeIndex) {
      int[] components = new int[this.nodes.size()];
      int[] ranks = new int[this.nodes.size()];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).hasHead()) {
            int hcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getHead().getIndex(), components);
            int dcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return ranks[nodeIndex];
   }

   protected DependencyNode findComponent(int nodeIndex) {
      int[] components = new int[this.nodes.size()];
      int[] ranks = new int[this.nodes.size()];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).hasHead()) {
            int hcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getHead().getIndex(), components);
            int dcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return (DependencyNode)this.nodes.get(this.findComponent(nodeIndex, components));
   }

   private int[] findComponents() {
      int[] components = new int[this.nodes.size()];
      int[] ranks = new int[this.nodes.size()];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).hasHead()) {
            int hcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getHead().getIndex(), components);
            int dcIndex = this.findComponent(((LWNode)this.nodes.get(i)).getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return components;
   }

   private int findComponent(int xIndex, int[] components) {
      if (xIndex != components[xIndex]) {
         components[xIndex] = this.findComponent(components[xIndex], components);
      }

      return components[xIndex];
   }

   private int link(int xIndex, int yIndex, int[] components, int[] ranks) {
      if (ranks[xIndex] > ranks[yIndex]) {
         components[yIndex] = xIndex;
         return xIndex;
      } else {
         components[xIndex] = yIndex;
         if (ranks[xIndex] == ranks[yIndex]) {
            int var10002 = ranks[yIndex]++;
         }

         return yIndex;
      }
   }

   public TokenNode addTokenNode() throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public TokenNode addTokenNode(int index) throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public TokenNode getTokenNode(int index) {
      return null;
   }

   public void addComment(String comment, int at_index) {
      ArrayList<String> commentList = (ArrayList)this.comments.get(at_index);
      if (commentList == null) {
         commentList = (ArrayList)this.comments.put(at_index, new ArrayList());
      }

      commentList.add(comment);
   }

   public ArrayList<String> getComment(int at_index) {
      return (ArrayList)this.comments.get(at_index);
   }

   public boolean hasComments() {
      return this.comments.size() > 0;
   }

   public int nTokenNode() {
      return this.nodes.size() - 1;
   }

   public SortedSet<Integer> getTokenIndices() {
      SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.size(); ++i) {
         indices.add(i);
      }

      return indices;
   }

   public int getHighestTokenIndex() {
      return this.nodes.size() - 1;
   }

   public boolean hasTokens() {
      return this.nodes.size() > 1;
   }

   public int getSentenceID() {
      return 0;
   }

   public void setSentenceID(int sentenceID) {
   }

   public void clear() throws MaltChainedException {
      this.nodes.clear();
   }

   public SymbolTableHandler getSymbolTables() {
      return this.symbolTables;
   }

   public void setSymbolTables(SymbolTableHandler symbolTables) {
   }

   public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
      element.addLabel(this.symbolTables.addSymbolTable(labelFunction), label);
   }

   public LabelSet checkOutNewLabelSet() throws MaltChainedException {
      throw new LWGraphException("Not implemented in light-weight dependency graph");
   }

   public void checkInLabelSet(LabelSet labelSet) throws MaltChainedException {
      throw new LWGraphException("Not implemented in light-weight dependency graph");
   }

   public Edge addSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
      throw new LWGraphException("Not implemented in light-weight dependency graph");
   }

   public void removeSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
      throw new LWGraphException("Not implemented in light-weight dependency graph");
   }

   public DependencyNode addDependencyNode() throws MaltChainedException {
      LWNode node = new LWNode(this, this.nodes.size());
      this.nodes.add(node);
      return node;
   }

   public DependencyNode addDependencyNode(int index) throws MaltChainedException {
      if (index == 0) {
         return (DependencyNode)this.nodes.get(0);
      } else if (index == this.nodes.size()) {
         return this.addDependencyNode();
      } else {
         throw new LWGraphException("Not implemented in light-weight dependency graph");
      }
   }

   public DependencyNode getDependencyNode(int index) throws MaltChainedException {
      return index >= 0 && index < this.nodes.size() ? (DependencyNode)this.nodes.get(index) : null;
   }

   public int nDependencyNode() {
      return this.nodes.size();
   }

   public int getHighestDependencyNodeIndex() {
      return this.nodes.size() - 1;
   }

   public Edge addDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
      if (headIndex < 0 && headIndex >= this.nodes.size()) {
         throw new LWGraphException("The head doesn't exists");
      } else if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
         throw new LWGraphException("The dependent doesn't exists");
      } else {
         LWNode head = (LWNode)this.nodes.get(headIndex);
         LWNode dependent = (LWNode)this.nodes.get(dependentIndex);
         Edge headEdge = new LWEdge(head, dependent);
         dependent.addIncomingEdge(headEdge);
         return headEdge;
      }
   }

   public Edge moveDependencyEdge(int newHeadIndex, int dependentIndex) throws MaltChainedException {
      if (newHeadIndex < 0 && newHeadIndex >= this.nodes.size()) {
         throw new LWGraphException("The head doesn't exists");
      } else if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
         throw new LWGraphException("The dependent doesn't exists");
      } else {
         LWNode head = (LWNode)this.nodes.get(newHeadIndex);
         LWNode dependent = (LWNode)this.nodes.get(dependentIndex);
         Edge oldheadEdge = dependent.getHeadEdge();
         Edge headEdge = new LWEdge(head, dependent);
         headEdge.addLabel(oldheadEdge.getLabelSet());
         dependent.addIncomingEdge(headEdge);
         return headEdge;
      }
   }

   public void removeDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
      if (headIndex < 0 && headIndex >= this.nodes.size()) {
         throw new LWGraphException("The head doesn't exists");
      } else if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
         throw new LWGraphException("The dependent doesn't exists");
      } else {
         LWNode head = (LWNode)this.nodes.get(headIndex);
         LWNode dependent = (LWNode)this.nodes.get(dependentIndex);
         Edge headEdge = new LWEdge(head, dependent);
         dependent.removeIncomingEdge(headEdge);
      }
   }

   public void linkAllTreesToRoot() throws MaltChainedException {
      for(int i = 0; i < this.nodes.size(); ++i) {
         if (!((LWNode)this.nodes.get(i)).hasHead()) {
            LWNode head = (LWNode)this.nodes.get(0);
            LWNode dependent = (LWNode)this.nodes.get(i);
            Edge headEdge = new LWEdge(head, dependent);
            headEdge.addLabel(this.getDefaultRootEdgeLabels());
            dependent.addIncomingEdge(headEdge);
         }
      }

   }

   public int nEdges() {
      int n = 0;

      for(int i = 1; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).hasHead()) {
            ++n;
         }
      }

      return n;
   }

   public SortedSet<Edge> getEdges() {
      SortedSet<Edge> edges = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.size(); ++i) {
         if (((LWNode)this.nodes.get(i)).hasHead()) {
            edges.add(((LWNode)this.nodes.get(i)).getHeadEdge());
         }
      }

      return edges;
   }

   public SortedSet<Integer> getDependencyIndices() {
      SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 0; i < this.nodes.size(); ++i) {
         indices.add(i);
      }

      return indices;
   }

   public DependencyNode getDependencyRoot() {
      return (DependencyNode)this.nodes.get(0);
   }

   public boolean hasLabeledDependency(int index) {
      if (index >= 0 && index < this.nodes.size()) {
         return !((LWNode)this.nodes.get(index)).hasHead() ? false : ((LWNode)this.nodes.get(index)).isHeadLabeled();
      } else {
         return false;
      }
   }

   public boolean isConnected() {
      int[] components = this.findComponents();
      int tmp = components[0];

      for(int i = 1; i < components.length; ++i) {
         if (tmp != components[i]) {
            return false;
         }
      }

      return true;
   }

   public boolean isProjective() throws MaltChainedException {
      for(int i = 1; i < this.nodes.size(); ++i) {
         if (!((LWNode)this.nodes.get(i)).isProjective()) {
            return false;
         }
      }

      return true;
   }

   public boolean isSingleHeaded() {
      return true;
   }

   public boolean isTree() {
      return this.isConnected() && this.isSingleHeaded();
   }

   public int nNonProjectiveEdges() throws MaltChainedException {
      int c = 0;

      for(int i = 1; i < this.nodes.size(); ++i) {
         if (!((LWNode)this.nodes.get(i)).isProjective()) {
            ++c;
         }
      }

      return c;
   }

   public LabelSet getDefaultRootEdgeLabels() throws MaltChainedException {
      return this.rootLabels.getDefaultRootLabels();
   }

   public String getDefaultRootEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return this.rootLabels.getDefaultRootLabelSymbol(table);
   }

   public int getDefaultRootEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return this.rootLabels.getDefaultRootLabelCode(table);
   }

   public void setDefaultRootEdgeLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
      this.rootLabels.setDefaultRootLabel(table, defaultRootSymbol);
   }

   public void setDefaultRootEdgeLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
      this.rootLabels.setRootLabels(rootLabelOption, edgeSymbolTables);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.nodes.iterator();

      while(i$.hasNext()) {
         LWNode node = (LWNode)i$.next();
         sb.append(node.toString().trim());
         sb.append('\n');
      }

      sb.append('\n');
      return sb.toString();
   }
}
