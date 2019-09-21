package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import java.util.SortedMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;

public class RootLabels {
   public static final String DEFAULT_ROOTSYMBOL = "ROOT";
   private final LabelSet rootLabelCodes = new LabelSet();

   public RootLabels() {
   }

   public void setRootLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
      if (edgeSymbolTables != null) {
         if (rootLabelOption != null && rootLabelOption.trim().length() != 0) {
            SymbolTable table;
            Iterator i$;
            if (rootLabelOption.trim().indexOf(44) == -1) {
               int index = rootLabelOption.trim().indexOf(61);
               if (index == -1) {
                  i$ = edgeSymbolTables.values().iterator();

                  while(i$.hasNext()) {
                     table = (SymbolTable)i$.next();
                     this.rootLabelCodes.put(table, table.addSymbol(rootLabelOption.trim()));
                  }
               } else {
                  String name = rootLabelOption.trim().substring(0, index);
                  if (edgeSymbolTables.get(name) == null) {
                     throw new SyntaxGraphException("The symbol table '" + name + "' cannot be found when defining the root symbol. ");
                  }

                  this.rootLabelCodes.put(edgeSymbolTables.get(name), ((SymbolTable)edgeSymbolTables.get(name)).addSymbol(rootLabelOption.trim().substring(index + 1)));
                  if (edgeSymbolTables.size() > 1) {
                     Iterator i$ = edgeSymbolTables.values().iterator();

                     while(i$.hasNext()) {
                        SymbolTable table = (SymbolTable)i$.next();
                        if (!table.getName().equals(name)) {
                           this.rootLabelCodes.put(table, table.addSymbol("ROOT"));
                        }
                     }
                  }
               }
            } else {
               String[] items = rootLabelOption.trim().split(",");

               for(int i = 0; i < items.length; ++i) {
                  int index = items[i].trim().indexOf(61);
                  if (index == -1) {
                     throw new SyntaxGraphException("The root symbol is undefinied. ");
                  }

                  String name = items[i].trim().substring(0, index);
                  if (edgeSymbolTables.get(name) == null) {
                     throw new SyntaxGraphException("The symbol table'" + name + "' cannot be found when defining the root symbol. ");
                  }

                  this.rootLabelCodes.put(edgeSymbolTables.get(name), ((SymbolTable)edgeSymbolTables.get(name)).addSymbol(items[i].trim().substring(index + 1)));
               }

               i$ = edgeSymbolTables.values().iterator();

               while(i$.hasNext()) {
                  table = (SymbolTable)i$.next();
                  if (!this.rootLabelCodes.containsKey(table)) {
                     this.rootLabelCodes.put(table, table.addSymbol("ROOT"));
                  }
               }
            }
         } else {
            Iterator i$ = edgeSymbolTables.values().iterator();

            while(i$.hasNext()) {
               SymbolTable table = (SymbolTable)i$.next();
               this.rootLabelCodes.put(table, table.addSymbol("ROOT"));
            }
         }

      }
   }

   public void setDefaultRootLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
      this.rootLabelCodes.put(table, table.addSymbol(defaultRootSymbol));
   }

   public Integer getDefaultRootLabelCode(SymbolTable table) throws MaltChainedException {
      Integer res = (Integer)this.rootLabelCodes.get(table);
      return res == null ? table.addSymbol("ROOT") : res;
   }

   public LabelSet getDefaultRootLabels() throws MaltChainedException {
      return new LabelSet(this.rootLabelCodes);
   }

   public String getDefaultRootLabelSymbol(SymbolTable table) throws MaltChainedException {
      return table.getSymbolCodeToString(this.getDefaultRootLabelCode(table));
   }

   public boolean checkRootLabelCodes(LabelSet rlc) {
      if (rlc == null && this.rootLabelCodes == null) {
         return true;
      } else if ((rlc != null || this.rootLabelCodes == null) && (rlc == null || this.rootLabelCodes != null)) {
         if (rlc.size() != this.rootLabelCodes.size()) {
            return false;
         } else {
            Iterator i$ = this.rootLabelCodes.keySet().iterator();

            SymbolTable table;
            do {
               if (!i$.hasNext()) {
                  return true;
               }

               table = (SymbolTable)i$.next();
            } while(((Integer)this.rootLabelCodes.get(table)).equals(rlc.get(table)));

            return false;
         }
      } else {
         return false;
      }
   }
}
