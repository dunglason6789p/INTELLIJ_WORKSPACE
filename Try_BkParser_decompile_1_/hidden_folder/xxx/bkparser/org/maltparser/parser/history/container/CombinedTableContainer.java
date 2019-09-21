package org.maltparser.parser.history.container;

import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;

public class CombinedTableContainer extends TableContainer implements Table {
   private final TableHandler tableHandler;
   private final char separator;
   private final TableContainer[] containers;
   private final StringBuilder[] cachedSymbols;
   private final int[] cachedCodes;

   public CombinedTableContainer(TableHandler _tableHandler, String _separator, List<TableContainer> _containers, char decisionSeparator) throws MaltChainedException {
      super((Table)null, (String)null, decisionSeparator);
      this.tableHandler = _tableHandler;
      if (_separator.length() > 0) {
         this.separator = _separator.charAt(0);
      } else {
         this.separator = '~';
      }

      this.containers = new TableContainer[_containers.size()];

      for(int i = 0; i < _containers.size(); ++i) {
         this.containers[i] = (TableContainer)_containers.get(i);
      }

      StringBuilder sb = new StringBuilder();

      int i;
      for(i = 0; i < this.containers.length; ++i) {
         sb.append(this.containers[i].getTableContainerName());
         sb.append('+');
      }

      sb.setLength(sb.length() - 1);
      this.setTable(this.tableHandler.addSymbolTable(sb.toString()));
      this.setName(sb.toString());
      this.cachedSymbols = new StringBuilder[this.containers.length];
      this.cachedCodes = new int[this.containers.length];

      for(i = 0; i < this.containers.length; ++i) {
         this.cachedCodes[i] = -1;
         this.cachedSymbols[i] = new StringBuilder();
      }

   }

   public void clearCache() {
      super.clearCache();

      int i;
      for(i = 0; i < this.cachedCodes.length; ++i) {
         this.cachedCodes[i] = -1;
      }

      for(i = 0; i < this.cachedSymbols.length; ++i) {
         this.cachedSymbols[i].setLength(0);
      }

   }

   public int addSymbol(String value) throws MaltChainedException {
      return this.table.addSymbol(value);
   }

   public String getName() {
      return this.table.getName();
   }

   public String getSymbolCodeToString(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public int getSymbolStringToCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public double getSymbolStringToValue(String symbol) throws MaltChainedException {
      return (double)this.table.getSymbolStringToCode(symbol);
   }

   public int getNumberContainers() {
      return this.containers.length;
   }

   public String getSymbol(int code) throws MaltChainedException {
      if (code < 0 && !this.containCode(code)) {
         this.clearCache();
         return null;
      } else {
         if (this.cachedCode != code) {
            this.clearCache();
            this.cachedCode = code;
            this.cachedSymbol.append(this.table.getSymbolCodeToString(this.cachedCode));
            this.split();
         }

         return this.cachedSymbol.toString();
      }
   }

   public int getCode(String symbol) throws MaltChainedException {
      if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
         this.clearCache();
         this.cachedSymbol.append(symbol);
         this.cachedCode = this.table.getSymbolStringToCode(symbol);
         this.split();
      }

      return this.cachedCode;
   }

   public boolean containCode(int code) throws MaltChainedException {
      if (this.cachedCode != code) {
         this.clearCache();
         this.cachedSymbol.append(this.table.getSymbolCodeToString(code));
         if (this.cachedSymbol == null && this.cachedSymbol.length() == 0) {
            return false;
         }

         this.cachedCode = code;
         this.split();
      }

      return true;
   }

   public boolean containSymbol(String symbol) throws MaltChainedException {
      if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
         this.clearCache();
         this.cachedCode = this.table.getSymbolStringToCode(symbol);
         if (this.cachedCode < 0) {
            return false;
         }

         this.cachedSymbol.append(symbol);
         this.split();
      }

      return true;
   }

   public int getCombinedCode(List<ActionContainer> codesToCombine) throws MaltChainedException {
      boolean cachedUsed = true;
      if (this.containers.length != codesToCombine.size()) {
         this.clearCache();
         return -1;
      } else {
         int i;
         for(i = 0; i < this.containers.length; ++i) {
            if (((ActionContainer)codesToCombine.get(i)).getActionCode() != this.cachedCodes[i]) {
               cachedUsed = false;
               if (((ActionContainer)codesToCombine.get(i)).getActionCode() >= 0 && this.containers[i].containCode(((ActionContainer)codesToCombine.get(i)).getActionCode())) {
                  this.cachedSymbols[i].setLength(0);
                  this.cachedSymbols[i].append(this.containers[i].getSymbol(((ActionContainer)codesToCombine.get(i)).getActionCode()));
                  this.cachedCodes[i] = ((ActionContainer)codesToCombine.get(i)).getActionCode();
               } else {
                  this.cachedSymbols[i].setLength(0);
                  this.cachedCodes[i] = -1;
               }
            }
         }

         if (!cachedUsed) {
            this.cachedSymbol.setLength(0);

            for(i = 0; i < this.containers.length; ++i) {
               if (this.cachedSymbols[i].length() != 0) {
                  this.cachedSymbol.append(this.cachedSymbols[i]);
                  this.cachedSymbol.append(this.separator);
               }
            }

            if (this.cachedSymbol.length() > 0) {
               this.cachedSymbol.setLength(this.cachedSymbol.length() - 1);
            }

            if (this.cachedSymbol.length() > 0) {
               this.cachedCode = this.table.addSymbol(this.cachedSymbol.toString());
            } else {
               this.cachedCode = -1;
            }
         }

         return this.cachedCode;
      }
   }

   public int getCombinedCode(ActionContainer[] codesToCombine, int start) throws MaltChainedException {
      boolean cachedUsed = true;
      if (start >= 0 && this.containers.length <= codesToCombine.length - start) {
         int i;
         for(i = 0; i < this.containers.length; ++i) {
            int code = codesToCombine[i + start].getActionCode();
            if (code != this.cachedCodes[i]) {
               cachedUsed = false;
               if (code >= 0 && this.containers[i].containCode(code)) {
                  this.cachedSymbols[i].setLength(0);
                  this.cachedSymbols[i].append(this.containers[i].getSymbol(code));
                  this.cachedCodes[i] = code;
               } else {
                  this.cachedSymbols[i].setLength(0);
                  this.cachedCodes[i] = -1;
               }
            }
         }

         if (!cachedUsed) {
            this.cachedSymbol.setLength(0);

            for(i = 0; i < this.containers.length; ++i) {
               if (this.cachedSymbols[i].length() != 0) {
                  this.cachedSymbol.append(this.cachedSymbols[i]);
                  this.cachedSymbol.append(this.separator);
               }
            }

            if (this.cachedSymbol.length() > 0) {
               this.cachedSymbol.setLength(this.cachedSymbol.length() - 1);
            }

            if (this.cachedSymbol.length() > 0) {
               this.cachedCode = this.table.addSymbol(this.cachedSymbol.toString());
            } else {
               this.cachedCode = -1;
            }
         }

         return this.cachedCode;
      } else {
         this.clearCache();
         return -1;
      }
   }

   public void setActionContainer(List<ActionContainer> actionContainers, int decision) throws MaltChainedException {
      if (decision != this.cachedCode) {
         this.clearCache();
         if (decision != -1) {
            this.cachedSymbol.append(this.table.getSymbolCodeToString(decision));
            this.cachedCode = decision;
         }

         this.split();
      }

      for(int i = 0; i < this.containers.length; ++i) {
         if (this.cachedSymbols[i].length() != 0) {
            this.cachedCodes[i] = ((ActionContainer)actionContainers.get(i)).setAction(this.cachedSymbols[i].toString());
         } else {
            this.cachedCodes[i] = ((ActionContainer)actionContainers.get(i)).setAction((String)null);
         }
      }

   }

   public void setActionContainer(ActionContainer[] actionContainers, int start, int decision) throws MaltChainedException {
      if (decision != this.cachedCode) {
         this.clearCache();
         if (decision != -1) {
            this.cachedSymbol.append(this.table.getSymbolCodeToString(decision));
            this.cachedCode = decision;
         }

         this.split();
      }

      for(int i = 0; i < this.containers.length; ++i) {
         if (this.cachedSymbols[i].length() != 0) {
            this.cachedCodes[i] = actionContainers[i + start].setAction(this.cachedSymbols[i].toString());
         } else {
            this.cachedCodes[i] = actionContainers[i + start].setAction((String)null);
         }
      }

   }

   protected void split() throws MaltChainedException {
      int j = 0;

      int i;
      for(i = 0; i < this.containers.length; ++i) {
         this.cachedSymbols[i].setLength(0);
      }

      for(i = 0; i < this.cachedSymbol.length(); ++i) {
         if (this.cachedSymbol.charAt(i) == this.separator) {
            ++j;
         } else {
            this.cachedSymbols[j].append(this.cachedSymbol.charAt(i));
         }
      }

      for(i = j + 1; i < this.containers.length; ++i) {
         this.cachedSymbols[i].setLength(0);
      }

      for(i = 0; i < this.containers.length; ++i) {
         if (this.cachedSymbols[i].length() != 0) {
            this.cachedCodes[i] = this.containers[i].getCode(this.cachedSymbols[i].toString());
         } else {
            this.cachedCodes[i] = -1;
         }
      }

   }

   public char getSeparator() {
      return this.separator;
   }

   protected void initSymbolTable() throws MaltChainedException {
   }
}
