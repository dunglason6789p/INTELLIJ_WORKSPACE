package org.maltparser;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.SingleMalt;

public class MaltParserService {
   private Engine engine;
   private FlowChartInstance flowChartInstance;
   private DataFormatInstance dataFormatInstance;
   private SingleMalt singleMalt;
   private int optionContainer;
   private boolean initialized;

   public MaltParserService() throws MaltChainedException {
      this(0);
   }

   public MaltParserService(int optionContainer) throws MaltChainedException {
      this.initialized = false;
      this.optionContainer = optionContainer;
      this.initialize();
   }

   public MaltParserService(boolean optionFreeInitialization) throws MaltChainedException {
      this.initialized = false;
      if (!optionFreeInitialization) {
         this.optionContainer = 0;
         this.initialize();
      } else {
         this.optionContainer = -1;
      }

   }

   public void runExperiment(String commandLine) throws MaltChainedException {
      OptionManager.instance().parseCommandLine(commandLine, this.optionContainer);
      this.engine = new Engine();
      this.engine.initialize(this.optionContainer);
      this.engine.process(this.optionContainer);
      this.engine.terminate(this.optionContainer);
   }

   public void initializeParserModel(String commandLine) throws MaltChainedException {
      if (this.optionContainer == -1) {
         throw new MaltChainedException("MaltParserService has been initialized as an option free initialization and therefore no parser model can be initialized.");
      } else {
         OptionManager.instance().parseCommandLine(commandLine, this.optionContainer);
         this.engine = new Engine();
         this.flowChartInstance = this.engine.initialize(this.optionContainer);
         if (this.flowChartInstance.hasPreProcessChartItems()) {
            this.flowChartInstance.preprocess();
         }

         this.singleMalt = (SingleMalt)this.flowChartInstance.getFlowChartRegistry(SingleMalt.class, "singlemalt");
         this.singleMalt.getConfigurationDir().initDataFormat();
         this.dataFormatInstance = this.singleMalt.getConfigurationDir().getDataFormatManager().getInputDataFormatSpec().createDataFormatInstance(this.singleMalt.getSymbolTables(), OptionManager.instance().getOptionValueString(this.optionContainer, "singlemalt", "null_value"));
         this.initialized = true;
      }
   }

   public DependencyStructure parse(String[] tokens) throws MaltChainedException {
      if (!this.initialized) {
         throw new MaltChainedException("No parser model has been initialized. Please use the method initializeParserModel() before invoking this method.");
      } else if (tokens != null && tokens.length != 0) {
         DependencyStructure outputGraph = new DependencyGraph(this.singleMalt.getSymbolTables());

         for(int i = 0; i < tokens.length; ++i) {
            Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
            DependencyNode node = outputGraph.addDependencyNode(i + 1);
            String[] items = tokens[i].split("\t");

            for(int j = 0; j < items.length; ++j) {
               if (columns.hasNext()) {
                  ColumnDescription column = (ColumnDescription)columns.next();
                  if (column.getCategory() == 1 && node != null) {
                     outputGraph.addLabel(node, column.getName(), items[j]);
                  }
               }
            }
         }

         outputGraph.setDefaultRootEdgeLabel(outputGraph.getSymbolTables().getSymbolTable("DEPREL"), "ROOT");
         this.singleMalt.parse(outputGraph);
         return outputGraph;
      } else {
         throw new MaltChainedException("Nothing to parse. ");
      }
   }

   public DependencyStructure toDependencyStructure(String[] tokens) throws MaltChainedException {
      if (!this.initialized) {
         throw new MaltChainedException("No parser model has been initialized. Please use the method initializeParserModel() before invoking this method.");
      } else if (tokens != null && tokens.length != 0) {
         DependencyStructure outputGraph = new DependencyGraph(this.singleMalt.getSymbolTables());

         for(int i = 0; i < tokens.length; ++i) {
            Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
            DependencyNode node = outputGraph.addDependencyNode(i + 1);
            String[] items = tokens[i].split("\t");
            Edge edge = null;

            for(int j = 0; j < items.length; ++j) {
               if (columns.hasNext()) {
                  ColumnDescription column = (ColumnDescription)columns.next();
                  if (column.getCategory() == 1 && node != null) {
                     outputGraph.addLabel(node, column.getName(), items[j]);
                  } else if (column.getCategory() == 2) {
                     if (column.getCategory() != 7 && !items[j].equals("_")) {
                        edge = outputGraph.addDependencyEdge(Integer.parseInt(items[j]), i + 1);
                     }
                  } else if (column.getCategory() == 3 && edge != null) {
                     outputGraph.addLabel(edge, column.getName(), items[j]);
                  }
               }
            }
         }

         outputGraph.setDefaultRootEdgeLabel(outputGraph.getSymbolTables().getSymbolTable("DEPREL"), "ROOT");
         return outputGraph;
      } else {
         throw new MaltChainedException("Nothing to convert. ");
      }
   }

   public DataFormatSpecification readDataFormatSpecification(String dataFormatFileName) throws MaltChainedException {
      DataFormatSpecification dataFormat = new DataFormatSpecification();
      dataFormat.parseDataFormatXMLfile(dataFormatFileName);
      return dataFormat;
   }

   public DependencyStructure toDependencyStructure(String[] tokens, DataFormatSpecification dataFormatSpecification) throws MaltChainedException {
      SymbolTableHandler symbolTables = new HashSymbolTableHandler();
      DataFormatInstance dataFormatInstance = dataFormatSpecification.createDataFormatInstance(symbolTables, "none");
      if (tokens != null && tokens.length != 0) {
         DependencyStructure outputGraph = new DependencyGraph(symbolTables);

         for(int i = 0; i < tokens.length; ++i) {
            Iterator<ColumnDescription> columns = dataFormatInstance.iterator();
            DependencyNode node = outputGraph.addDependencyNode(i + 1);
            String[] items = tokens[i].split("\t");
            Edge edge = null;

            for(int j = 0; j < items.length; ++j) {
               if (columns.hasNext()) {
                  ColumnDescription column = (ColumnDescription)columns.next();
                  if (column.getCategory() == 1 && node != null) {
                     outputGraph.addLabel(node, column.getName(), items[j]);
                  } else if (column.getCategory() == 2) {
                     if (column.getCategory() != 7 && !items[j].equals("_")) {
                        edge = outputGraph.addDependencyEdge(Integer.parseInt(items[j]), i + 1);
                     }
                  } else if (column.getCategory() == 3 && edge != null) {
                     outputGraph.addLabel(edge, column.getName(), items[j]);
                  }
               }
            }
         }

         outputGraph.setDefaultRootEdgeLabel(outputGraph.getSymbolTables().getSymbolTable("DEPREL"), "ROOT");
         return outputGraph;
      } else {
         throw new MaltChainedException("Nothing to convert. ");
      }
   }

   public DependencyStructure toDependencyStructure(String[] tokens, String dataFormatFileName) throws MaltChainedException {
      return this.toDependencyStructure(tokens, this.readDataFormatSpecification(dataFormatFileName));
   }

   public String[] parseTokens(String[] tokens) throws MaltChainedException {
      DependencyStructure outputGraph = this.parse(tokens);
      StringBuilder sb = new StringBuilder();
      String[] outputTokens = new String[tokens.length];
      SymbolTable deprelTable = outputGraph.getSymbolTables().getSymbolTable("DEPREL");
      Iterator i$ = outputGraph.getTokenIndices().iterator();

      while(i$.hasNext()) {
         Integer index = (Integer)i$.next();
         sb.setLength(0);
         if (index <= tokens.length) {
            DependencyNode node = outputGraph.getDependencyNode(index);
            sb.append(tokens[index - 1]);
            sb.append('\t');
            sb.append(node.getHead().getIndex());
            sb.append('\t');
            if (node.getHeadEdge().hasLabel(deprelTable)) {
               sb.append(node.getHeadEdge().getLabelSymbol(deprelTable));
            } else {
               sb.append(outputGraph.getDefaultRootEdgeLabelSymbol(deprelTable));
            }

            outputTokens[index - 1] = sb.toString();
         }
      }

      return outputTokens;
   }

   public void terminateParserModel() throws MaltChainedException {
      if (!this.initialized) {
         throw new MaltChainedException("No parser model has been initialized. Please use the method initializeParserModel() before invoking this method.");
      } else {
         if (this.flowChartInstance.hasPostProcessChartItems()) {
            this.flowChartInstance.postprocess();
         }

         this.engine.terminate(this.optionContainer);
      }
   }

   private void initialize() throws MaltChainedException {
      if (OptionManager.instance().getOptionDescriptions().getOptionGroupNameSet().size() <= 0) {
         OptionManager.instance().loadOptionDescriptionFile();
         OptionManager.instance().generateMaps();
      }
   }

   public int getOptionContainer() {
      return this.optionContainer;
   }
}
