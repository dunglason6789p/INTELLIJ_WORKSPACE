package org.maltparser.parser.transition;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;

public class TransitionTableHandler implements TableHandler {
   private final HashMap<String, TransitionTable> transitionTables = new HashMap();

   public TransitionTableHandler() {
   }

   public Table addSymbolTable(String tableName) throws MaltChainedException {
      TransitionTable table = (TransitionTable)this.transitionTables.get(tableName);
      if (table == null) {
         table = new TransitionTable(tableName);
         this.transitionTables.put(tableName, table);
      }

      return table;
   }

   public Table getSymbolTable(String tableName) throws MaltChainedException {
      return (Table)this.transitionTables.get(tableName);
   }
}
