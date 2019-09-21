/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Formatter;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.lw.graph.LWDependencyGraph;
import org.maltparser.core.lw.graph.LWGraphException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class LWTest {
    private static final String IGNORE_COLUMN_SIGN = "_";

    public static String[] readSentences(BufferedReader reader) throws IOException {
        String line;
        ArrayList<String> tokens = new ArrayList<String>();
        while ((line = reader.readLine()) != null && line.trim().length() != 0) {
            tokens.add(line.trim());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    public static DependencyStructure getOldDependencyGraph(DataFormat dataFormat, SymbolTableHandler symbolTableHandlers, String[] tokens) throws MaltChainedException {
        int i;
        DependencyGraph oldGraph = new DependencyGraph(symbolTableHandlers);
        for (i = 0; i < tokens.length; ++i) {
            oldGraph.addDependencyNode(i + 1);
        }
        for (i = 0; i < tokens.length; ++i) {
            DependencyNode node = oldGraph.getDependencyNode(i + 1);
            String[] items = tokens[i].split("\t");
            Edge edge = null;
            for (int j = 0; j < items.length; ++j) {
                ColumnDescription column = dataFormat.getColumnDescription(j);
                if (column.getCategory() == 1 && node != null) {
                    oldGraph.addLabel(node, column.getName(), items[j]);
                    continue;
                }
                if (column.getCategory() == 2) {
                    if (column.getCategory() == 7 || items[j].equals(IGNORE_COLUMN_SIGN)) continue;
                    edge = oldGraph.addDependencyEdge(Integer.parseInt(items[j]), i + 1);
                    continue;
                }
                if (column.getCategory() != 3 || edge == null) continue;
                oldGraph.addLabel(edge, column.getName(), items[j]);
            }
        }
        oldGraph.setDefaultRootEdgeLabel(oldGraph.getSymbolTables().getSymbolTable("DEPREL"), "ROOT");
        return oldGraph;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String inFile = args[0];
        String charSet = "UTF-8";
        BufferedReader reader = null;
        try {
            String[] goldTokens;
            DataFormat dataFormat = DataFormat.parseDataFormatXMLfile("/appdata/dataformat/conllx.xml");
            reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(inFile), charSet));
            int sentenceCounter = 0;
            while ((goldTokens = LWTest.readSentences(reader)).length != 0) {
                ++sentenceCounter;
                HashSymbolTableHandler newTable = new HashSymbolTableHandler();
                LWDependencyGraph newGraph = new LWDependencyGraph(dataFormat, newTable, goldTokens, "ROOT");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (LWGraphException e) {
            e.printStackTrace();
        }
        catch (MaltChainedException e) {
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Finished init basic   : " + new Formatter().format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L) + " (" + elapsed + " ms)");
    }
}

