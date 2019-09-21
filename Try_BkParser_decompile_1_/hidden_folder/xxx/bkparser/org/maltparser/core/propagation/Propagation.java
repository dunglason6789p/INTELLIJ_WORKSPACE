package org.maltparser.core.propagation;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.spec.PropagationSpec;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class Propagation {
   private SymbolTable fromTable;
   private SymbolTable toTable;
   private SymbolTable deprelTable;
   private final SortedSet<String> forSet;
   private final SortedSet<String> overSet;
   private final Pattern symbolSeparator;

   public Propagation(PropagationSpec spec, DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      ColumnDescription fromColumn = dataFormatInstance.getColumnDescriptionByName(spec.getFrom());
      if (fromColumn == null) {
         throw new PropagationException("The symbol table '" + spec.getFrom() + " does not exists.");
      } else {
         this.fromTable = tableHandler.getSymbolTable(spec.getFrom());
         ColumnDescription toColumn = dataFormatInstance.getColumnDescriptionByName(spec.getTo());
         if (toColumn == null) {
            dataFormatInstance.addInternalColumnDescription(tableHandler, spec.getTo(), fromColumn);
            this.toTable = tableHandler.getSymbolTable(spec.getTo());
         }

         this.forSet = new TreeSet();
         String[] items;
         String[] arr$;
         int len$;
         int i$;
         String item;
         if (spec.getFor() != null && spec.getFor().length() > 0) {
            items = spec.getFor().split("\\|");
            arr$ = items;
            len$ = items.length;

            for(i$ = 0; i$ < len$; ++i$) {
               item = arr$[i$];
               this.forSet.add(item);
            }
         }

         this.overSet = new TreeSet();
         if (spec.getOver() != null && spec.getOver().length() > 0) {
            items = spec.getOver().split("\\|");
            arr$ = items;
            len$ = items.length;

            for(i$ = 0; i$ < len$; ++i$) {
               item = arr$[i$];
               this.overSet.add(item);
            }
         }

         this.deprelTable = tableHandler.getSymbolTable("DEPREL");
         this.symbolSeparator = Pattern.compile("\\|");
      }
   }

   public void propagate(Edge e) throws MaltChainedException {
      if (e != null && e.hasLabel(this.deprelTable) && !e.getSource().isRoot() && (this.overSet.size() == 0 || this.overSet.contains(e.getLabelSymbol(this.deprelTable)))) {
         DependencyNode to = (DependencyNode)e.getSource();
         DependencyNode from = (DependencyNode)e.getTarget();
         String fromSymbol = null;
         if (e.hasLabel(this.fromTable)) {
            fromSymbol = e.getLabelSymbol(this.fromTable);
         } else if (from.hasLabel(this.fromTable)) {
            fromSymbol = from.getLabelSymbol(this.fromTable);
         }

         String propSymbol = null;
         if (to.hasLabel(this.toTable)) {
            propSymbol = this.union(fromSymbol, to.getLabelSymbol(this.toTable));
         } else if (this.forSet.size() == 0 || this.forSet.contains(fromSymbol)) {
            propSymbol = fromSymbol;
         }

         if (propSymbol != null) {
            to.addLabel(this.toTable, propSymbol);
         }
      }

   }

   private String union(String fromSymbol, String toSymbol) {
      SortedSet<String> symbolSet = new TreeSet();
      String[] toSymbols;
      int i;
      if (fromSymbol != null && fromSymbol.length() != 0) {
         toSymbols = this.symbolSeparator.split(fromSymbol);

         for(i = 0; i < toSymbols.length; ++i) {
            if (this.forSet.size() == 0 || this.forSet.contains(toSymbols[i])) {
               symbolSet.add(toSymbols[i]);
            }
         }
      }

      if (toSymbol != null && toSymbol.length() != 0) {
         toSymbols = this.symbolSeparator.split(toSymbol);

         for(i = 0; i < toSymbols.length; ++i) {
            symbolSet.add(toSymbols[i]);
         }
      }

      if (symbolSet.size() <= 0) {
         return "";
      } else {
         StringBuilder sb = new StringBuilder();
         Iterator i$ = symbolSet.iterator();

         while(i$.hasNext()) {
            String symbol = (String)i$.next();
            sb.append(symbol);
            sb.append('|');
         }

         sb.setLength(sb.length() - 1);
         return sb.toString();
      }
   }

   public String toString() {
      return "Propagation [forSet=" + this.forSet + ", fromTable=" + this.fromTable + ", overSet=" + this.overSet + ", toTable=" + this.toTable + "]";
   }
}
