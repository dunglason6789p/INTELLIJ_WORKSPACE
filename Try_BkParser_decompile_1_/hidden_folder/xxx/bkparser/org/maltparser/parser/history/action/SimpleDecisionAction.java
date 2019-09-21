package org.maltparser.parser.history.action;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.container.TableContainer;
import org.maltparser.parser.history.kbest.KBestList;

public class SimpleDecisionAction implements SingleDecision {
   private final TableContainer tableContainer;
   private int decision;
   private final KBestList kBestList;

   public SimpleDecisionAction(int kBestSize, TableContainer _tableContainer) throws MaltChainedException {
      this.tableContainer = _tableContainer;
      this.kBestList = new KBestList(kBestSize, this);
      this.clear();
   }

   public void clear() {
      this.decision = -1;
      this.kBestList.reset();
   }

   public int numberOfDecisions() {
      return 1;
   }

   public void addDecision(int code) throws MaltChainedException {
      if (code == -1 || !this.tableContainer.containCode(code)) {
         this.decision = -1;
      }

      this.decision = code;
   }

   public void addDecision(String symbol) throws MaltChainedException {
      this.decision = this.tableContainer.getCode(symbol);
   }

   public int getDecisionCode() throws MaltChainedException {
      return this.decision;
   }

   public int getDecisionCode(String symbol) throws MaltChainedException {
      return this.tableContainer.getCode(symbol);
   }

   public String getDecisionSymbol() throws MaltChainedException {
      return this.tableContainer.getSymbol(this.decision);
   }

   public boolean updateFromKBestList() throws MaltChainedException {
      return this.kBestList.updateActionWithNextKBest();
   }

   public boolean continueWithNextDecision() throws MaltChainedException {
      return this.tableContainer.continueWithNextDecision(this.decision);
   }

   public TableContainer getTableContainer() {
      return this.tableContainer;
   }

   public KBestList getKBestList() throws MaltChainedException {
      return this.kBestList;
   }

   public TableContainer.RelationToNextDecision getRelationToNextDecision() {
      return this.tableContainer.getRelationToNextDecision();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.decision);
      return sb.toString();
   }
}
