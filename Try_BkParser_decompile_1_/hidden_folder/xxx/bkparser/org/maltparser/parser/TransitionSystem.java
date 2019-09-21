package org.maltparser.parser;

import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.transition.TransitionTable;
import org.maltparser.parser.transition.TransitionTableHandler;

public abstract class TransitionSystem {
   public static final Pattern decisionSettingsSplitPattern = Pattern.compile(",|#|;|\\+");
   private final HashMap<String, TableHandler> tableHandlers = new HashMap();
   private final PropagationManager propagationManager;
   protected final TransitionTableHandler transitionTableHandler = new TransitionTableHandler();
   protected ActionContainer[] actionContainers;
   protected ActionContainer transActionContainer;
   protected ActionContainer[] arcLabelActionContainers;

   public TransitionSystem(PropagationManager _propagationManager) throws MaltChainedException {
      this.propagationManager = _propagationManager;
   }

   public abstract void apply(GuideUserAction var1, ParserConfiguration var2) throws MaltChainedException;

   public abstract boolean permissible(GuideUserAction var1, ParserConfiguration var2) throws MaltChainedException;

   public abstract GuideUserAction getDeterministicAction(GuideUserHistory var1, ParserConfiguration var2) throws MaltChainedException;

   protected abstract void addAvailableTransitionToTable(TransitionTable var1) throws MaltChainedException;

   protected abstract void initWithDefaultTransitions(GuideUserHistory var1) throws MaltChainedException;

   public abstract String getName();

   public abstract GuideUserAction defaultAction(GuideUserHistory var1, ParserConfiguration var2) throws MaltChainedException;

   protected GuideUserAction updateActionContainers(GuideUserHistory history, int transition, LabelSet arcLabels) throws MaltChainedException {
      this.transActionContainer.setAction(transition);
      int i;
      if (arcLabels == null) {
         for(i = 0; i < this.arcLabelActionContainers.length; ++i) {
            this.arcLabelActionContainers[i].setAction(-1);
         }
      } else {
         for(i = 0; i < this.arcLabelActionContainers.length; ++i) {
            if (this.arcLabelActionContainers[i] == null) {
               throw new MaltChainedException("arcLabelActionContainer " + i + " is null when doing transition " + transition);
            }

            Integer code = (Integer)arcLabels.get(this.arcLabelActionContainers[i].getTable());
            if (code != null) {
               this.arcLabelActionContainers[i].setAction(code.shortValue());
            } else {
               this.arcLabelActionContainers[i].setAction(-1);
            }
         }
      }

      GuideUserAction oracleAction = history.getEmptyGuideUserAction();
      oracleAction.addAction(this.actionContainers);
      return oracleAction;
   }

   protected boolean isActionContainersLabeled() {
      for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
         if (this.arcLabelActionContainers[i].getActionCode() < 0) {
            return false;
         }
      }

      return true;
   }

   protected void addEdgeLabels(Edge e) throws MaltChainedException {
      if (e != null) {
         for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
            if (this.arcLabelActionContainers[i].getActionCode() != -1) {
               e.addLabel((SymbolTable)this.arcLabelActionContainers[i].getTable(), this.arcLabelActionContainers[i].getActionCode());
            } else {
               e.addLabel((SymbolTable)this.arcLabelActionContainers[i].getTable(), ((DependencyStructure)e.getBelongsToGraph()).getDefaultRootEdgeLabelCode((SymbolTable)this.arcLabelActionContainers[i].getTable()));
            }
         }

         if (this.propagationManager != null) {
            this.propagationManager.propagate(e);
         }
      }

   }

   public void initTransitionSystem(GuideUserHistory history) throws MaltChainedException {
      this.actionContainers = history.getActionContainerArray();
      if (this.actionContainers.length < 1) {
         throw new ParsingException("Problem when initialize the history (sequence of actions). There are no action containers. ");
      } else {
         int nLabels = 0;

         int i;
         for(i = 0; i < this.actionContainers.length; ++i) {
            if (this.actionContainers[i].getTableContainerName().startsWith("A.")) {
               ++nLabels;
            }
         }

         i = 0;

         for(int i = 0; i < this.actionContainers.length; ++i) {
            if (this.actionContainers[i].getTableContainerName().equals("T.TRANS")) {
               this.transActionContainer = this.actionContainers[i];
            } else if (this.actionContainers[i].getTableContainerName().startsWith("A.")) {
               if (this.arcLabelActionContainers == null) {
                  this.arcLabelActionContainers = new ActionContainer[nLabels];
               }

               this.arcLabelActionContainers[i++] = this.actionContainers[i];
            }
         }

         this.initWithDefaultTransitions(history);
      }
   }

   public void initTableHandlers(String decisionSettings, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      if (!decisionSettings.equals("T.TRANS+A.DEPREL") && !decisionSettings.equals("T.TRANS#A.DEPREL") && !decisionSettings.equals("T.TRANS,A.DEPREL") && !decisionSettings.equals("T.TRANS;A.DEPREL")) {
         this.initTableHandlers(decisionSettingsSplitPattern.split(decisionSettings), decisionSettings, symbolTableHandler);
      } else {
         this.tableHandlers.put("T", this.transitionTableHandler);
         this.addAvailableTransitionToTable((TransitionTable)this.transitionTableHandler.addSymbolTable("TRANS"));
         this.tableHandlers.put("A", symbolTableHandler);
      }
   }

   public void initTableHandlers(String[] decisionElements, String decisionSettings, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      int nTrans = 0;

      for(int i = 0; i < decisionElements.length; ++i) {
         int index = decisionElements[i].indexOf(46);
         if (index == -1) {
            throw new ParsingException("Decision settings '" + decisionSettings + "' contain an item '" + decisionElements[i] + "' that does not follow the format {TableHandler}.{Table}. ");
         }

         if (decisionElements[i].substring(0, index).equals("T")) {
            if (!this.tableHandlers.containsKey("T")) {
               this.tableHandlers.put("T", this.transitionTableHandler);
            }

            if (decisionElements[i].substring(index + 1).equals("TRANS")) {
               if (nTrans != 0) {
                  throw new ParsingException("Illegal decision settings '" + decisionSettings + "'");
               }

               this.addAvailableTransitionToTable((TransitionTable)this.transitionTableHandler.addSymbolTable("TRANS"));
               ++nTrans;
            }
         } else {
            if (!decisionElements[i].substring(0, index).equals("A")) {
               throw new ParsingException("The decision settings '" + decisionSettings + "' contains an unknown table handler '" + decisionElements[i].substring(0, index) + "'. " + "Only T (Transition table handler) and A (ArcLabel table handler) is allowed. ");
            }

            if (!this.tableHandlers.containsKey("A")) {
               this.tableHandlers.put("A", symbolTableHandler);
            }
         }
      }

   }

   public void copyAction(GuideUserAction source, GuideUserAction target) throws MaltChainedException {
      source.getAction(this.actionContainers);
      target.addAction(this.actionContainers);
   }

   public HashMap<String, TableHandler> getTableHandlers() {
      return this.tableHandlers;
   }

   public String getActionString(GuideUserAction action) throws MaltChainedException {
      StringBuilder sb = new StringBuilder();
      action.getAction(this.actionContainers);
      Table ttable = this.transitionTableHandler.getSymbolTable("TRANS");
      sb.append(ttable.getSymbolCodeToString(this.transActionContainer.getActionCode()));

      for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
         if (this.arcLabelActionContainers[i].getActionCode() != -1) {
            sb.append("+");
            sb.append(this.arcLabelActionContainers[i].getTable().getSymbolCodeToString(this.arcLabelActionContainers[i].getActionCode()));
         }
      }

      return sb.toString();
   }
}
