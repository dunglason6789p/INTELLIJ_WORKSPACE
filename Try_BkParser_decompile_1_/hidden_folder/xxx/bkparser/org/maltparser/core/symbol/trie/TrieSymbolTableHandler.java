package org.maltparser.core.symbol.trie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;

public class TrieSymbolTableHandler implements SymbolTableHandler {
   private final Trie trie = new Trie();
   private final HashMap<String, TrieSymbolTable> symbolTables = new HashMap();

   public TrieSymbolTableHandler() {
   }

   public TrieSymbolTable addSymbolTable(String tableName) throws MaltChainedException {
      TrieSymbolTable symbolTable = (TrieSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         symbolTable = new TrieSymbolTable(tableName, this.trie);
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public TrieSymbolTable addSymbolTable(String tableName, SymbolTable parentTable) throws MaltChainedException {
      TrieSymbolTable symbolTable = (TrieSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         TrieSymbolTable trieParentTable = (TrieSymbolTable)parentTable;
         symbolTable = new TrieSymbolTable(tableName, this.trie, trieParentTable.getCategory(), trieParentTable.getNullValueStrategy());
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public TrieSymbolTable addSymbolTable(String tableName, int columnCategory, int columnType, String nullValueStrategy) throws MaltChainedException {
      TrieSymbolTable symbolTable = (TrieSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         symbolTable = new TrieSymbolTable(tableName, this.trie, columnCategory, nullValueStrategy);
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public TrieSymbolTable getSymbolTable(String tableName) {
      return (TrieSymbolTable)this.symbolTables.get(tableName);
   }

   public Set<String> getSymbolTableNames() {
      return this.symbolTables.keySet();
   }

   public void cleanUp() {
   }

   public void save(OutputStreamWriter osw) throws MaltChainedException {
      try {
         BufferedWriter bout = new BufferedWriter(osw);
         Iterator i$ = this.symbolTables.values().iterator();

         TrieSymbolTable table;
         while(i$.hasNext()) {
            table = (TrieSymbolTable)i$.next();
            table.saveHeader(bout);
         }

         bout.write(10);
         i$ = this.symbolTables.values().iterator();

         while(i$.hasNext()) {
            table = (TrieSymbolTable)i$.next();
            table.save(bout);
         }

         bout.close();
      } catch (IOException var5) {
         throw new SymbolException("Could not save the symbol tables. ", var5);
      }
   }

   public void save(String fileName, String charSet) throws MaltChainedException {
      try {
         this.save(new OutputStreamWriter(new FileOutputStream(fileName), charSet));
      } catch (FileNotFoundException var4) {
         throw new SymbolException("The symbol table file '" + fileName + "' cannot be created. ", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new SymbolException("The char set '" + charSet + "' is not supported. ", var5);
      }
   }

   public void loadHeader(BufferedReader bin) throws MaltChainedException {
      String fileLine = "";
      Pattern tabPattern = Pattern.compile("\t");

      try {
         while((fileLine = bin.readLine()) != null && fileLine.length() != 0 && fileLine.charAt(0) == '\t') {
            String[] items;
            try {
               items = tabPattern.split(fileLine.substring(1));
            } catch (PatternSyntaxException var6) {
               throw new SymbolException("The header line of the symbol table  '" + fileLine.substring(1) + "' could not split into atomic parts. ", var6);
            }

            if (items.length == 4) {
               this.addSymbolTable(items[0], Integer.parseInt(items[1]), Integer.parseInt(items[2]), items[3]);
            } else {
               if (items.length != 3) {
                  throw new SymbolException("The header line of the symbol table  '" + fileLine.substring(1) + "' must contain three or four columns. ");
               }

               this.addSymbolTable(items[0], Integer.parseInt(items[1]), 1, items[2]);
            }
         }

      } catch (NumberFormatException var7) {
         throw new SymbolException("The symbol table file (.sym) contains a non-integer value in the header. ", var7);
      } catch (IOException var8) {
         throw new SymbolException("Could not load the symbol table. ", var8);
      }
   }

   public void load(InputStreamReader isr) throws MaltChainedException {
      try {
         BufferedReader bin = new BufferedReader(isr);
         SymbolTable table = null;
         bin.mark(2);
         if (bin.read() == 9) {
            bin.reset();
            this.loadHeader(bin);
         } else {
            bin.reset();
         }

         String fileLine;
         while((fileLine = bin.readLine()) != null) {
            if (fileLine.length() > 0) {
               table = this.addSymbolTable(fileLine);
               table.load(bin);
            }
         }

         bin.close();
      } catch (IOException var5) {
         throw new SymbolException("Could not load the symbol tables. ", var5);
      }
   }

   public void load(String fileName, String charSet) throws MaltChainedException {
      try {
         this.load(new InputStreamReader(new FileInputStream(fileName), charSet));
      } catch (FileNotFoundException var4) {
         throw new SymbolException("The symbol table file '" + fileName + "' cannot be found. ", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new SymbolException("The char set '" + charSet + "' is not supported. ", var5);
      }
   }

   public SymbolTable loadTagset(String fileName, String tableName, String charSet, int columnCategory, int columnType, String nullValueStrategy) throws MaltChainedException {
      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charSet));
         TrieSymbolTable table = this.addSymbolTable(tableName, columnCategory, columnType, nullValueStrategy);

         String fileLine;
         while((fileLine = br.readLine()) != null) {
            table.addSymbol(fileLine.trim());
         }

         br.close();
         return table;
      } catch (FileNotFoundException var10) {
         throw new SymbolException("The tagset file '" + fileName + "' cannot be found. ", var10);
      } catch (UnsupportedEncodingException var11) {
         throw new SymbolException("The char set '" + charSet + "' is not supported. ", var11);
      } catch (IOException var12) {
         throw new SymbolException("The tagset file '" + fileName + "' cannot be loaded. ", var12);
      }
   }
}
