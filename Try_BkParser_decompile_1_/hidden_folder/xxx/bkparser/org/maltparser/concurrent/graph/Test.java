package org.maltparser.concurrent.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class Test {
   private static final String IGNORE_COLUMN_SIGN = "_";

   public Test() {
   }

   public static DependencyStructure getOldDependencyGraph(DataFormat dataFormat, String[] tokens) throws MaltChainedException {
      DependencyStructure oldGraph = new DependencyGraph(new HashSymbolTableHandler());

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
            String[] goldTokens = ConcurrentUtils.readSentence(reader);
            if (goldTokens.length == 0) {
               break;
            }

            ++var7;
            ConcurrentDependencyGraph newGraph = new ConcurrentDependencyGraph(dataFormat, goldTokens);
            DependencyStructure oldGraph = getOldDependencyGraph(dataFormat, goldTokens);

            for(int i = 0; i < newGraph.nDependencyNodes(); ++i) {
               int newGraphINT = newGraph.getDependencyNode(i).findComponent().getIndex();
               int oldGraphINT = oldGraph.getDependencyNode(i).findComponent().getIndex();
               newGraphINT = newGraph.getDependencyNode(i).getRank();
               oldGraphINT = oldGraph.getDependencyNode(i).getRank();
               if (newGraphINT != oldGraphINT) {
                  System.out.println(newGraphINT + "\t" + oldGraphINT);
               }
            }
         }
      } catch (IOException var30) {
         var30.printStackTrace();
      } catch (ConcurrentGraphException var31) {
         var31.printStackTrace();
      } catch (MaltChainedException var32) {
         var32.printStackTrace();
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException var29) {
               var29.printStackTrace();
            }
         }

      }

      long elapsed = System.currentTimeMillis() - startTime;
      System.out.println("Finished init basic   : " + (new Formatter()).format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L) + " (" + elapsed + " ms)");
   }
}
