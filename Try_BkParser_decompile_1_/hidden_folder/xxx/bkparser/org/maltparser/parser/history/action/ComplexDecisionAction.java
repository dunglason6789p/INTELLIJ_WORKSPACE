package org.maltparser.parser.history.action;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.HistoryException;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.CombinedTableContainer;
import org.maltparser.parser.history.container.TableContainer;
import org.maltparser.parser.history.kbest.ScoredKBestList;

public class ComplexDecisionAction implements GuideUserAction, MultipleDecision {
   private final GuideUserHistory history;
   private final ArrayList<SimpleDecisionAction> decisions;

   public ComplexDecisionAction(GuideUserHistory history) throws MaltChainedException {
      this.history = history;
      this.decisions = new ArrayList(history.getDecisionTables().size());
      int i = 0;

      for(int n = history.getDecisionTables().size(); i < n; ++i) {
         this.decisions.add(new SimpleDecisionAction(history.getKBestSize(), (TableContainer)history.getDecisionTables().get(i)));
      }

   }

   public void addAction(ArrayList<ActionContainer> actionContainers) throws MaltChainedException {
      if (actionContainers != null && actionContainers.size() == this.history.getActionTables().size()) {
         int j = 0;
         int i = 0;

         for(int n = this.history.getDecisionTables().size(); i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
               CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
               int nContainers = tableContainer.getNumberContainers();
               ((SimpleDecisionAction)this.decisions.get(i)).addDecision(tableContainer.getCombinedCode(actionContainers.subList(j, j + nContainers)));
               j += nContainers;
            } else {
               ((SimpleDecisionAction)this.decisions.get(i)).addDecision(((ActionContainer)actionContainers.get(j)).getActionCode());
               ++j;
            }
         }

      } else {
         throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
      }
   }

   public void getAction(ArrayList<ActionContainer> actionContainers) throws MaltChainedException {
      if (actionContainers != null && actionContainers.size() == this.history.getActionTables().size()) {
         int j = 0;
         int i = 0;

         for(int n = this.history.getDecisionTables().size(); i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
               CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
               int nContainers = tableContainer.getNumberContainers();
               tableContainer.setActionContainer(actionContainers.subList(j, j + nContainers), ((SimpleDecisionAction)this.decisions.get(i)).getDecisionCode());
               j += nContainers;
            } else {
               ((ActionContainer)actionContainers.get(j)).setAction(((SimpleDecisionAction)this.decisions.get(i)).getDecisionCode());
               ++j;
            }
         }

      } else {
         throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
      }
   }

   public void addAction(ActionContainer[] actionContainers) throws MaltChainedException {
      if (actionContainers != null && actionContainers.length == this.history.getActionTables().size()) {
         int j = 0;
         int i = 0;

         for(int n = this.history.getDecisionTables().size(); i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
               CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
               int nContainers = tableContainer.getNumberContainers();
               ((SimpleDecisionAction)this.decisions.get(i)).addDecision(tableContainer.getCombinedCode(actionContainers, j));
               j += nContainers;
            } else {
               ((SimpleDecisionAction)this.decisions.get(i)).addDecision(actionContainers[j].getActionCode());
               ++j;
            }
         }

      } else {
         throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
      }
   }

   public void getAction(ActionContainer[] actionContainers) throws MaltChainedException {
      if (actionContainers != null && actionContainers.length == this.history.getActionTables().size()) {
         int j = 0;
         int i = 0;

         for(int n = this.history.getDecisionTables().size(); i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
               CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
               int nContainers = tableContainer.getNumberContainers();
               tableContainer.setActionContainer(actionContainers, j, ((SimpleDecisionAction)this.decisions.get(i)).getDecisionCode());
               j += nContainers;
            } else {
               actionContainers[j].setAction(((SimpleDecisionAction)this.decisions.get(i)).getDecisionCode());
               ++j;
            }
         }

      } else {
         throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
      }
   }

   public void getKBestLists(ArrayList<ScoredKBestList> kbestListContainers) throws MaltChainedException {
      kbestListContainers.clear();
      int i = 0;

      for(int n = this.decisions.size(); i < n; ++i) {
         kbestListContainers.add((ScoredKBestList)((SimpleDecisionAction)this.decisions.get(i)).getKBestList());
      }

   }

   public void getKBestLists(ScoredKBestList[] kbestListContainers) throws MaltChainedException {
      int i = 0;

      for(int n = this.decisions.size(); i < n; ++i) {
         kbestListContainers[0] = (ScoredKBestList)((SimpleDecisionAction)this.decisions.get(i)).getKBestList();
      }

   }

   public int numberOfActions() {
      return this.history.getActionTables().size();
   }

   public void clear() {
      int i = 0;

      for(int n = this.decisions.size(); i < n; ++i) {
         ((SimpleDecisionAction)this.decisions.get(i)).clear();
      }

   }

   public SingleDecision getSingleDecision(int decisionIndex) throws MaltChainedException {
      return (SingleDecision)this.decisions.get(decisionIndex);
   }

   public int numberOfDecisions() {
      return this.history.getDecisionTables().size();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         ComplexDecisionAction other = (ComplexDecisionAction)obj;
         if (this.decisions == null) {
            if (other.decisions != null) {
               return false;
            }
         } else {
            if (this.decisions.size() != other.decisions.size()) {
               return false;
            }

            for(int i = 0; i < this.decisions.size(); ++i) {
               try {
                  if (((SimpleDecisionAction)this.decisions.get(i)).getDecisionCode() != ((SimpleDecisionAction)other.decisions.get(i)).getDecisionCode()) {
                     return false;
                  }
               } catch (MaltChainedException var5) {
                  System.err.println("Error in equals. ");
               }
            }
         }

         return true;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      int i = 0;

      for(int n = this.decisions.size(); i < n; ++i) {
         sb.append(this.decisions.get(i));
         sb.append(';');
      }

      if (sb.length() > 0) {
         sb.setLength(sb.length() - 1);
      }

      return sb.toString();
   }
}
