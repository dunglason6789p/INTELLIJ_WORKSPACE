/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.writer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter;

public class TabWriter
implements SyntaxGraphWriter {
    private BufferedWriter writer;
    private DataFormatInstance dataFormatInstance;
    private final StringBuilder output;
    private boolean closeStream = true;
    private final char TAB = (char)9;
    private final char NEWLINE = (char)10;

    public TabWriter() {
        this.output = new StringBuilder();
    }

    @Override
    public void open(String fileName, String charsetName) throws MaltChainedException {
        try {
            this.open(new OutputStreamWriter((OutputStream)new FileOutputStream(fileName), charsetName));
        }
        catch (FileNotFoundException e) {
            throw new DataFormatException("The output file '" + fileName + "' cannot be found.", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported.", e);
        }
    }

    @Override
    public void open(OutputStream os, String charsetName) throws MaltChainedException {
        try {
            if (os == System.out || os == System.err) {
                this.closeStream = false;
            }
            this.open(new OutputStreamWriter(os, charsetName));
        }
        catch (UnsupportedEncodingException e) {
            throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported.", e);
        }
    }

    private void open(OutputStreamWriter osw) throws MaltChainedException {
        this.setWriter(new BufferedWriter(osw));
    }

    @Override
    public void writeProlog() throws MaltChainedException {
    }

    public void writeComments(TokenStructure syntaxGraph, int at_index) throws MaltChainedException {
        ArrayList<String> commentList = syntaxGraph.getComment(at_index);
        if (commentList != null) {
            try {
                for (int i = 0; i < commentList.size(); ++i) {
                    this.writer.write(commentList.get(i));
                    this.writer.write(10);
                }
            }
            catch (IOException e) {
                this.close();
                throw new DataFormatException("Could not write to the output file. ", e);
            }
        }
    }

    @Override
    public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
        if (syntaxGraph == null || this.dataFormatInstance == null || !syntaxGraph.hasTokens()) {
            return;
        }
        Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
        SymbolTableHandler symbolTables = syntaxGraph.getSymbolTables();
        Iterator i$ = syntaxGraph.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            this.writeComments(syntaxGraph, i);
            try {
                ColumnDescription column = null;
                while (columns.hasNext()) {
                    column = columns.next();
                    if (column.getCategory() == 1) {
                        TokenNode node = syntaxGraph.getTokenNode(i);
                        if (!column.getName().equals("ID")) {
                            if (node.hasLabel(symbolTables.getSymbolTable(column.getName()))) {
                                this.output.append(node.getLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                                if (this.output.length() != 0) {
                                    this.writer.write(this.output.toString());
                                } else {
                                    this.writer.write(95);
                                }
                            } else {
                                this.writer.write(95);
                            }
                        } else {
                            this.writer.write(Integer.toString(i));
                        }
                    } else if (column.getCategory() == 2 && syntaxGraph instanceof DependencyStructure) {
                        if (((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHead()) {
                            this.writer.write(Integer.toString(((DependencyStructure)syntaxGraph).getDependencyNode(i).getHead().getIndex()));
                        } else {
                            this.writer.write(Integer.toString(0));
                        }
                    } else if (column.getCategory() == 3 && syntaxGraph instanceof DependencyStructure) {
                        if (((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHead() && ((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHeadEdgeLabel(symbolTables.getSymbolTable(column.getName()))) {
                            this.output.append(((DependencyStructure)syntaxGraph).getDependencyNode(i).getHeadEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                        } else {
                            this.output.append(((DependencyStructure)syntaxGraph).getDefaultRootEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                        }
                        if (this.output.length() != 0) {
                            this.writer.write(this.output.toString());
                        }
                    } else {
                        this.writer.write(column.getDefaultOutput());
                    }
                    if (columns.hasNext()) {
                        this.writer.write(9);
                    }
                    this.output.setLength(0);
                }
                this.writer.write(10);
                columns = this.dataFormatInstance.iterator();
            }
            catch (IOException e) {
                this.close();
                throw new DataFormatException("Could not write to the output file. ", e);
            }
        }
        this.writeComments(syntaxGraph, syntaxGraph.nTokenNode() + 1);
        try {
            this.writer.write(10);
            this.writer.flush();
        }
        catch (IOException e) {
            this.close();
            throw new DataFormatException("Could not write to the output file. ", e);
        }
    }

    @Override
    public void writeEpilog() throws MaltChainedException {
    }

    public BufferedWriter getWriter() {
        return this.writer;
    }

    public void setWriter(BufferedWriter writer) throws MaltChainedException {
        this.close();
        this.writer = writer;
    }

    @Override
    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
    public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
        this.dataFormatInstance = dataFormatInstance;
    }

    @Override
    public String getOptions() {
        return null;
    }

    @Override
    public void setOptions(String optionString) throws MaltChainedException {
    }

    @Override
    public void close() throws MaltChainedException {
        try {
            if (this.writer != null) {
                this.writer.flush();
                if (this.closeStream) {
                    this.writer.close();
                }
                this.writer = null;
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Could not close the output file. ", e);
        }
    }
}

