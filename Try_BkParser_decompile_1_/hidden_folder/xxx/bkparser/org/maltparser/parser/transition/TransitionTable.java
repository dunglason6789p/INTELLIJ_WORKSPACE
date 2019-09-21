package org.maltparser.parser.transition;

import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.Table;
import org.maltparser.parser.history.container.DecisionPropertyTable;

public class TransitionTable implements Table, DecisionPropertyTable {
   private final String name;
   private final SortedMap<Integer, Transition> code2transitionMap;
   private final HashMap<String, Transition> symbol2transitionMap;
   private final HashMap<Transition, TransitionTable> childrenTables;

   public TransitionTable(String tableName) {
      this.name = tableName;
      this.code2transitionMap = new TreeMap();
      this.symbol2transitionMap = new HashMap();
      this.childrenTables = new HashMap();
   }

   public void addTransition(int code, String symbol, boolean labeled, TransitionTable childrenTable) {
      Transition transition = new Transition(code, symbol, labeled);
      this.code2transitionMap.put(code, transition);
      this.symbol2transitionMap.put(symbol, transition);
      this.childrenTables.put(transition, childrenTable);
   }

   public boolean continueWithNextDecision(int code) throws MaltChainedException {
      return this.code2transitionMap.containsKey(code) ? ((Transition)this.code2transitionMap.get(code)).isLabeled() : true;
   }

   public boolean continueWithNextDecision(String symbol) throws MaltChainedException {
      return this.symbol2transitionMap.containsKey(symbol) ? ((Transition)this.symbol2transitionMap.get(symbol)).isLabeled() : true;
   }

   public Table getTableForNextDecision(int code) throws MaltChainedException {
      return this.code2transitionMap.containsKey(code) ? (Table)this.childrenTables.get(this.code2transitionMap.get(code)) : null;
   }

   public Table getTableForNextDecision(String symbol) throws MaltChainedException {
      return this.symbol2transitionMap.containsKey(symbol) ? (Table)this.childrenTables.get(this.symbol2transitionMap.get(symbol)) : null;
   }

   public Transition getTransition(String symbol) {
      return (Transition)this.symbol2transitionMap.get(symbol);
   }

   public Transition getTransition(int code) {
      return (Transition)this.code2transitionMap.get(code);
   }

   public int addSymbol(String symbol) throws MaltChainedException {
      return -1;
   }

   public String getName() {
      return this.name;
   }

   public String getSymbolCodeToString(int code) throws MaltChainedException {
      return code < 0 ? null : ((Transition)this.code2transitionMap.get(code)).getSymbol();
   }

   public int getSymbolStringToCode(String symbol) throws MaltChainedException {
      return symbol == null ? -1 : ((Transition)this.symbol2transitionMap.get(symbol)).getCode();
   }

   public double getSymbolStringToValue(String symbol) throws MaltChainedException {
      return 1.0D;
   }

   public int size() {
      return this.code2transitionMap.size();
   }
}
