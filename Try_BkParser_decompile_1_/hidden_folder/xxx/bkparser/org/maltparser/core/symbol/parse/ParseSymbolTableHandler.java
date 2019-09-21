package org.maltparser.core.symbol.parse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class ParseSymbolTableHandler implements SymbolTableHandler {
   private final SymbolTableHandler parentSymbolTableHandler;
   private final HashMap<String, ParseSymbolTable> symbolTables;

   public ParseSymbolTableHandler(SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
      this.parentSymbolTableHandler = parentSymbolTableHandler;
      this.symbolTables = new HashMap();
      Iterator i$ = parentSymbolTableHandler.getSymbolTableNames().iterator();

      while(i$.hasNext()) {
         String tableName = (String)i$.next();
         this.addSymbolTable(tableName);
      }

   }

   public SymbolTable addSymbolTable(String tableName) throws MaltChainedException {
      ParseSymbolTable symbolTable = (ParseSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         symbolTable = new ParseSymbolTable(tableName, this.parentSymbolTableHandler);
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public SymbolTable addSymbolTable(String tableName, SymbolTable parentTable) throws MaltChainedException {
      ParseSymbolTable symbolTable = (ParseSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         symbolTable = new ParseSymbolTable(tableName, parentTable, this.parentSymbolTableHandler);
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public SymbolTable addSymbolTable(String tableName, int columnCategory, int columnType, String nullValueStrategy) throws MaltChainedException {
      ParseSymbolTable symbolTable = (ParseSymbolTable)this.symbolTables.get(tableName);
      if (symbolTable == null) {
         symbolTable = new ParseSymbolTable(tableName, columnCategory, columnType, nullValueStrategy, this.parentSymbolTableHandler);
         this.symbolTables.put(tableName, symbolTable);
      }

      return symbolTable;
   }

   public SymbolTable getSymbolTable(String tableName) {
      return (SymbolTable)this.symbolTables.get(tableName);
   }

   public Set<String> getSymbolTableNames() {
      return this.symbolTables.keySet();
   }

   public void cleanUp() {
      Iterator i$ = this.symbolTables.values().iterator();

      while(i$.hasNext()) {
         ParseSymbolTable table = (ParseSymbolTable)i$.next();
         table.clearTmpStorage();
      }

   }

   public void save(OutputStreamWriter osw) throws MaltChainedException {
      this.parentSymbolTableHandler.save(osw);
   }

   public void save(String fileName, String charSet) throws MaltChainedException {
      this.parentSymbolTableHandler.save(fileName, charSet);
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
         SymbolTable table = this.addSymbolTable(tableName, columnCategory, columnType, nullValueStrategy);

         String fileLine;
         while((fileLine = br.readLine()) != null) {
            table.addSymbol(fileLine.trim());
         }

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
