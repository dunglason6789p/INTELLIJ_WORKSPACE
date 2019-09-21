package org.maltparser.core.lw.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Formatter;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class LWTest {
   private static final String IGNORE_COLUMN_SIGN = "_";

   public LWTest() {
   }

   public static String[] readSentences(BufferedReader reader) throws IOException {
      ArrayList tokens = new ArrayList();

      String line;
      while((line = reader.readLine()) != null && line.trim().length() != 0) {
         tokens.add(line.trim());
      }

      return (String[])tokens.toArray(new String[tokens.size()]);
   }

   public static DependencyStructure getOldDependencyGraph(DataFormat dataFormat, SymbolTableHandler symbolTableHandlers, String[] tokens) throws MaltChainedException {
      DependencyStructure oldGraph = new DependencyGraph(symbolTableHandlers);

      int i;
      for(i = 0; i < tokens.length; ++i) {
         oldGraph.addDependencyNode(i + 1);
      }

      for(i = 0; i < tokens.length; ++i) {
         DependencyNode node = oldGraph.getDependencyNode(i + 1);
         String[] items = tokens[i].split("\t");
         Edge edge = null;

         for(int j = 0; j < items.length; ++j) {
            ColumnDescription column = dataFormat.getColumnDescription(j);
            if (column.getCategory() == 1 && node != null) {
               oldGraph.addLabel(node, column.getName(), items[j]);
            } else if (column.getCategory() == 2) {
               if (column.getCategory() != 7 && !items[j].equals("_")) {
                  edge = oldGraph.addDependencyEdge(Integer.parseInt(items[j]), i + 1);
               }
            } else if (column.getCategory() == 3 && edge != null) {
               oldGraph.addLabel(edge, column.getName(), items[j]);
            }
         }
      }

      oldGraph.setDefaultRootEdgeLabel(oldGraph.getSymbolTables().getSymbolTable("DEPREL"), "ROOT");
      return oldGraph;
   }

   public static void main(String[] args) {
      long startTime = System.currentTimeMillis();
      String inFile = args[0];
      String charSet = "UTF-8";
      BufferedReader reader = null;

      try {
         DataFormat dataFormat = DataFormat.parseDataFormatXMLfile("/appdata/dataformat/conllx.xml");
         reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), charSet));
         int var7 = 0;

         while(true) {
            String[] goldTokens = readSentences(reader);
            if (goldTokens.length == 0) {
               break;
            }

            ++var7;
            SymbolTableHandler newTable = new HashSymbolTableHandler();
            new LWDependencyGraph(dataFormat, newTable, goldTokens, "ROOT");
         }
      } catch (IOException var29) {
         var29.printStackTrace();
      } catch (LWGraphException var30) {
         var30.printStackTrace();
      } catch (MaltChainedException var31) {
         var31.printStackTrace();
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException var28) {
               var28.printStackTrace();
            }
         }

      }

      long elapsed = System.currentTimeMillis() - startTime;
      System.out.println("Finished init basic   : " + (new Formatter()).format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L) + " (" + elapsed + " ms)");
   }
}
