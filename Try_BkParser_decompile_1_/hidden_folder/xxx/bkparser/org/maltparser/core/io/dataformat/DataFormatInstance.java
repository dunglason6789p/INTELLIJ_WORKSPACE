package org.maltparser.core.io.dataformat;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;

public class DataFormatInstance implements Iterable<ColumnDescription> {
   private final SortedSet<ColumnDescription> columnDescriptions = new TreeSet();
   private SortedMap<String, ColumnDescription> headColumnDescriptions;
   private SortedMap<String, ColumnDescription> dependencyEdgeLabelColumnDescriptions;
   private SortedMap<String, ColumnDescription> phraseStructureEdgeLabelColumnDescriptions;
   private SortedMap<String, ColumnDescription> phraseStructureNodeLabelColumnDescriptions;
   private SortedMap<String, ColumnDescription> secondaryEdgeLabelColumnDescriptions;
   private SortedMap<String, ColumnDescription> inputColumnDescriptions;
   private SortedMap<String, ColumnDescription> ignoreColumnDescriptions;
   private SortedSet<ColumnDescription> headColumnDescriptionSet;
   private SortedSet<ColumnDescription> dependencyEdgeLabelColumnDescriptionSet;
   private SortedSet<ColumnDescription> phraseStructureEdgeLabelColumnDescriptionSet;
   private SortedSet<ColumnDescription> phraseStructureNodeLabelColumnDescriptionSet;
   private SortedSet<ColumnDescription> secondaryEdgeLabelColumnDescriptionSet;
   private SortedSet<ColumnDescription> inputColumnDescriptionSet;
   private SortedSet<ColumnDescription> ignoreColumnDescriptionSet;
   private SortedMap<String, SymbolTable> dependencyEdgeLabelSymbolTables;
   private SortedMap<String, SymbolTable> phraseStructureEdgeLabelSymbolTables;
   private SortedMap<String, SymbolTable> phraseStructureNodeLabelSymbolTables;
   private SortedMap<String, SymbolTable> secondaryEdgeLabelSymbolTables;
   private SortedMap<String, SymbolTable> inputSymbolTables;
   private SortedMap<String, ColumnDescription> internalColumnDescriptions;
   private SortedSet<ColumnDescription> internalColumnDescriptionSet;
   private final DataFormatSpecification dataFormarSpec;

   public DataFormatInstance(Map<String, DataFormatEntry> entries, SymbolTableHandler symbolTables, String nullValueStrategy, DataFormatSpecification dataFormarSpec) throws MaltChainedException {
      this.dataFormarSpec = dataFormarSpec;
      this.createColumnDescriptions(symbolTables, entries, nullValueStrategy);
   }

   public ColumnDescription addInternalColumnDescription(SymbolTableHandler symbolTables, String name, String category, String type, String defaultOutput, String nullValueStrategy) throws MaltChainedException {
      if (this.internalColumnDescriptions == null) {
         this.internalColumnDescriptions = new TreeMap();
         this.internalColumnDescriptionSet = new TreeSet();
      }

      if (!this.internalColumnDescriptions.containsKey(name)) {
         ColumnDescription internalColumn = new ColumnDescription(name, ColumnDescription.getCategory(category), ColumnDescription.getType(type), defaultOutput, nullValueStrategy, true);
         symbolTables.addSymbolTable(internalColumn.getName(), internalColumn.getCategory(), internalColumn.getType(), internalColumn.getNullValueStrategy());
         this.internalColumnDescriptions.put(name, internalColumn);
         this.internalColumnDescriptionSet.add(internalColumn);
         return internalColumn;
      } else {
         return (ColumnDescription)this.internalColumnDescriptions.get(name);
      }
   }

   public ColumnDescription addInternalColumnDescription(SymbolTableHandler symbolTables, String name, int category, int type, String defaultOutput, String nullValueStrategy) throws MaltChainedException {
      if (this.internalColumnDescriptions == null) {
         this.internalColumnDescriptions = new TreeMap();
         this.internalColumnDescriptionSet = new TreeSet();
      }

      if (!this.internalColumnDescriptions.containsKey(name)) {
         ColumnDescription internalColumn = new ColumnDescription(name, category, type, defaultOutput, nullValueStrategy, true);
         symbolTables.addSymbolTable(internalColumn.getName(), internalColumn.getCategory(), internalColumn.getType(), internalColumn.getNullValueStrategy());
         this.internalColumnDescriptions.put(name, internalColumn);
         this.internalColumnDescriptionSet.add(internalColumn);
         return internalColumn;
      } else {
         return (ColumnDescription)this.internalColumnDescriptions.get(name);
      }
   }

   public ColumnDescription addInternalColumnDescription(SymbolTableHandler symbolTables, String name, ColumnDescription column) throws MaltChainedException {
      return this.addInternalColumnDescription(symbolTables, name, column.getCategory(), column.getType(), column.getDefaultOutput(), column.getNullValueStrategy());
   }

   private void createColumnDescriptions(SymbolTableHandler symbolTables, Map<String, DataFormatEntry> entries, String nullValueStrategy) throws MaltChainedException {
      Iterator i$ = entries.values().iterator();

      while(i$.hasNext()) {
         DataFormatEntry entry = (DataFormatEntry)i$.next();
         ColumnDescription column = new ColumnDescription(entry.getDataFormatEntryName(), ColumnDescription.getCategory(entry.getCategory()), ColumnDescription.getType(entry.getType()), entry.getDefaultOutput(), nullValueStrategy, false);
         symbolTables.addSymbolTable(column.getName(), column.getCategory(), column.getType(), column.getNullValueStrategy());
         this.columnDescriptions.add(column);
      }

   }

   public ColumnDescription getColumnDescriptionByName(String name) {
      Iterator i$ = this.columnDescriptions.iterator();

      ColumnDescription internalColumn;
      while(i$.hasNext()) {
         internalColumn = (ColumnDescription)i$.next();
         if (internalColumn.getName().equals(name)) {
            return internalColumn;
         }
      }

      if (this.internalColumnDescriptionSet != null) {
         i$ = this.internalColumnDescriptionSet.iterator();

         while(i$.hasNext()) {
            internalColumn = (ColumnDescription)i$.next();
            if (internalColumn.getName().equals(name)) {
               return internalColumn;
            }
         }
      }

      return null;
   }

   public Iterator<ColumnDescription> iterator() {
      return this.columnDescriptions.iterator();
   }

   public DataFormatSpecification getDataFormarSpec() {
      return this.dataFormarSpec;
   }

   protected void createHeadColumnDescriptions() {
      this.headColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 2) {
            this.headColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public ColumnDescription getHeadColumnDescription() {
      if (this.headColumnDescriptions == null) {
         this.createHeadColumnDescriptions();
      }

      return (ColumnDescription)this.headColumnDescriptions.get(this.headColumnDescriptions.firstKey());
   }

   public SortedMap<String, ColumnDescription> getHeadColumnDescriptions() {
      if (this.headColumnDescriptions == null) {
         this.createHeadColumnDescriptions();
      }

      return this.headColumnDescriptions;
   }

   protected void createDependencyEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.dependencyEdgeLabelSymbolTables = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 3) {
            this.dependencyEdgeLabelSymbolTables.put(column.getName(), symbolTables.getSymbolTable(column.getName()));
         }
      }

   }

   public SortedMap<String, SymbolTable> getDependencyEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      if (this.dependencyEdgeLabelSymbolTables == null) {
         this.createDependencyEdgeLabelSymbolTables(symbolTables);
      }

      return this.dependencyEdgeLabelSymbolTables;
   }

   protected void createDependencyEdgeLabelColumnDescriptions() {
      this.dependencyEdgeLabelColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 3) {
            this.dependencyEdgeLabelColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getDependencyEdgeLabelColumnDescriptions() {
      if (this.dependencyEdgeLabelColumnDescriptions == null) {
         this.createDependencyEdgeLabelColumnDescriptions();
      }

      return this.dependencyEdgeLabelColumnDescriptions;
   }

   protected void createPhraseStructureEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.phraseStructureEdgeLabelSymbolTables = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 4) {
            this.phraseStructureEdgeLabelSymbolTables.put(column.getName(), symbolTables.getSymbolTable(column.getName()));
         }
      }

   }

   public SortedMap<String, SymbolTable> getPhraseStructureEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      if (this.phraseStructureEdgeLabelSymbolTables == null) {
         this.createPhraseStructureEdgeLabelSymbolTables(symbolTables);
      }

      return this.phraseStructureEdgeLabelSymbolTables;
   }

   protected void createPhraseStructureEdgeLabelColumnDescriptions() {
      this.phraseStructureEdgeLabelColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 4) {
            this.phraseStructureEdgeLabelColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getPhraseStructureEdgeLabelColumnDescriptions() {
      if (this.phraseStructureEdgeLabelColumnDescriptions == null) {
         this.createPhraseStructureEdgeLabelColumnDescriptions();
      }

      return this.phraseStructureEdgeLabelColumnDescriptions;
   }

   protected void createPhraseStructureNodeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.phraseStructureNodeLabelSymbolTables = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 5) {
            this.phraseStructureNodeLabelSymbolTables.put(column.getName(), symbolTables.getSymbolTable(column.getName()));
         }
      }

   }

   public SortedMap<String, SymbolTable> getPhraseStructureNodeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      if (this.phraseStructureNodeLabelSymbolTables == null) {
         this.createPhraseStructureNodeLabelSymbolTables(symbolTables);
      }

      return this.phraseStructureNodeLabelSymbolTables;
   }

   protected void createPhraseStructureNodeLabelColumnDescriptions() {
      this.phraseStructureNodeLabelColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 5) {
            this.phraseStructureNodeLabelColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getPhraseStructureNodeLabelColumnDescriptions() {
      if (this.phraseStructureNodeLabelColumnDescriptions == null) {
         this.createPhraseStructureNodeLabelColumnDescriptions();
      }

      return this.phraseStructureNodeLabelColumnDescriptions;
   }

   protected void createSecondaryEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.secondaryEdgeLabelSymbolTables = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 4) {
            this.secondaryEdgeLabelSymbolTables.put(column.getName(), symbolTables.getSymbolTable(column.getName()));
         }
      }

   }

   public SortedMap<String, SymbolTable> getSecondaryEdgeLabelSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      if (this.secondaryEdgeLabelSymbolTables == null) {
         this.createSecondaryEdgeLabelSymbolTables(symbolTables);
      }

      return this.secondaryEdgeLabelSymbolTables;
   }

   protected void createSecondaryEdgeLabelColumnDescriptions() {
      this.secondaryEdgeLabelColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 4) {
            this.secondaryEdgeLabelColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getSecondaryEdgeLabelColumnDescriptions() {
      if (this.secondaryEdgeLabelColumnDescriptions == null) {
         this.createSecondaryEdgeLabelColumnDescriptions();
      }

      return this.secondaryEdgeLabelColumnDescriptions;
   }

   protected void createInputSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.inputSymbolTables = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 1) {
            this.inputSymbolTables.put(column.getName(), symbolTables.getSymbolTable(column.getName()));
         }
      }

   }

   public SortedMap<String, SymbolTable> getInputSymbolTables(SymbolTableHandler symbolTables) throws MaltChainedException {
      if (this.inputSymbolTables == null) {
         this.createInputSymbolTables(symbolTables);
      }

      return this.inputSymbolTables;
   }

   protected void createInputColumnDescriptions() {
      this.inputColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 1) {
            this.inputColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getInputColumnDescriptions() {
      if (this.inputColumnDescriptions == null) {
         this.createInputColumnDescriptions();
      }

      return this.inputColumnDescriptions;
   }

   protected void createIgnoreColumnDescriptions() {
      this.ignoreColumnDescriptions = new TreeMap();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (column.getCategory() == 7) {
            this.ignoreColumnDescriptions.put(column.getName(), column);
         }
      }

   }

   public SortedMap<String, ColumnDescription> getIgnoreColumnDescriptions() {
      if (this.ignoreColumnDescriptions == null) {
         this.createIgnoreColumnDescriptions();
      }

      return this.ignoreColumnDescriptions;
   }

   public SortedSet<ColumnDescription> getHeadColumnDescriptionSet() {
      if (this.headColumnDescriptionSet == null) {
         this.headColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 2) {
               this.headColumnDescriptionSet.add(column);
            }
         }
      }

      return this.headColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getDependencyEdgeLabelColumnDescriptionSet() {
      if (this.dependencyEdgeLabelColumnDescriptionSet == null) {
         this.dependencyEdgeLabelColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 3) {
               this.dependencyEdgeLabelColumnDescriptionSet.add(column);
            }
         }
      }

      return this.dependencyEdgeLabelColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getPhraseStructureEdgeLabelColumnDescriptionSet() {
      if (this.phraseStructureEdgeLabelColumnDescriptionSet == null) {
         this.phraseStructureEdgeLabelColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 4) {
               this.phraseStructureEdgeLabelColumnDescriptionSet.add(column);
            }
         }
      }

      return this.phraseStructureEdgeLabelColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getPhraseStructureNodeLabelColumnDescriptionSet() {
      if (this.phraseStructureNodeLabelColumnDescriptionSet == null) {
         this.phraseStructureNodeLabelColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 5) {
               this.phraseStructureNodeLabelColumnDescriptionSet.add(column);
            }
         }
      }

      return this.phraseStructureNodeLabelColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getSecondaryEdgeLabelColumnDescriptionSet() {
      if (this.secondaryEdgeLabelColumnDescriptionSet == null) {
         this.secondaryEdgeLabelColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 6) {
               this.secondaryEdgeLabelColumnDescriptionSet.add(column);
            }
         }
      }

      return this.secondaryEdgeLabelColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getInputColumnDescriptionSet() {
      if (this.inputColumnDescriptionSet == null) {
         this.inputColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 1) {
               this.inputColumnDescriptionSet.add(column);
            }
         }
      }

      return this.inputColumnDescriptionSet;
   }

   public SortedSet<ColumnDescription> getIgnoreColumnDescriptionSet() {
      if (this.ignoreColumnDescriptionSet == null) {
         this.ignoreColumnDescriptionSet = new TreeSet();
         Iterator i$ = this.columnDescriptions.iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (column.getCategory() == 7) {
               this.ignoreColumnDescriptionSet.add(column);
            }
         }
      }

      return this.ignoreColumnDescriptionSet;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.columnDescriptions.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         sb.append(column);
         sb.append('\n');
      }

      return sb.toString();
   }
}
