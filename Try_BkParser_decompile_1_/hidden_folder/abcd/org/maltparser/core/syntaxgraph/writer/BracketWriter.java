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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter;

public class BracketWriter
implements SyntaxGraphWriter {
    private PennWriterFormat format;
    private BufferedWriter writer;
    private DataFormatInstance dataFormatInstance;
    private SortedMap<String, ColumnDescription> inputColumns;
    private SortedMap<String, ColumnDescription> edgeLabelColumns;
    private SortedMap<String, ColumnDescription> phraseLabelColumns;
    private char STARTING_BRACKET = (char)40;
    private String EMPTY_EDGELABEL = "??";
    private char CLOSING_BRACKET = (char)41;
    private char INPUT_SEPARATOR = (char)32;
    private char EDGELABEL_SEPARATOR = (char)45;
    private char SENTENCE_SEPARATOR = (char)10;
    private String optionString;
    private boolean closeStream = true;

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
    public void writeEpilog() throws MaltChainedException {
    }

    @Override
    public void writeProlog() throws MaltChainedException {
    }

    @Override
    public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
        if (syntaxGraph == null || this.dataFormatInstance == null) {
            return;
        }
        if (syntaxGraph instanceof PhraseStructure && syntaxGraph.hasTokens()) {
            if (this.format == PennWriterFormat.PRETTY) {
                this.writeElement(syntaxGraph.getSymbolTables(), ((PhraseStructure)syntaxGraph).getPhraseStructureRoot(), 0);
            } else {
                this.writeElement(syntaxGraph.getSymbolTables(), ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
            }
            try {
                this.writer.write(this.SENTENCE_SEPARATOR);
                this.writer.flush();
            }
            catch (IOException e) {
                this.close();
                throw new DataFormatException("Could not write to the output file. ", e);
            }
        }
    }

    private void writeElement(SymbolTableHandler symbolTables, PhraseStructureNode element) throws MaltChainedException {
        try {
            if (element instanceof TokenNode) {
                PhraseStructureNode t = element;
                SymbolTable table = null;
                this.writer.write(this.STARTING_BRACKET);
                int i = 0;
                for (String inputColumn : this.inputColumns.keySet()) {
                    if (i != 0) {
                        this.writer.write(this.INPUT_SEPARATOR);
                    }
                    if (t.hasLabel(table = symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumn)).getName()))) {
                        this.writer.write(t.getLabelSymbol(table));
                    }
                    if (i == 0) {
                        for (String edgeLabelColumn : this.edgeLabelColumns.keySet()) {
                            table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                            if (!t.hasParentEdgeLabel(table) || t.getParent().isRoot() || t.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) continue;
                            this.writer.write(this.EDGELABEL_SEPARATOR);
                            this.writer.write(t.getParentEdgeLabelSymbol(table));
                        }
                    }
                    ++i;
                }
                this.writer.write(this.CLOSING_BRACKET);
            } else {
                NonTerminalNode nt = (NonTerminalNode)element;
                this.writer.write(this.STARTING_BRACKET);
                SymbolTable table = null;
                int i = 0;
                for (String phraseLabelColumn : this.phraseLabelColumns.keySet()) {
                    if (i != 0) {
                        this.writer.write(this.INPUT_SEPARATOR);
                    }
                    if (nt.hasLabel(table = symbolTables.getSymbolTable(((ColumnDescription)this.phraseLabelColumns.get(phraseLabelColumn)).getName()))) {
                        this.writer.write(nt.getLabelSymbol(table));
                    }
                    if (i == 0) {
                        for (String edgeLabelColumn : this.edgeLabelColumns.keySet()) {
                            table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                            if (!nt.hasParentEdgeLabel(table) || nt.getParent().isRoot() || nt.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) continue;
                            this.writer.write(this.EDGELABEL_SEPARATOR);
                            this.writer.write(nt.getParentEdgeLabelSymbol(table));
                        }
                    }
                    ++i;
                }
                for (PhraseStructureNode node : ((NonTerminalNode)element).getChildren()) {
                    this.writeElement(symbolTables, node);
                }
                this.writer.write(this.CLOSING_BRACKET);
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Could not write to the output file. ", e);
        }
    }

    private String getIndentation(int depth) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < depth; ++i) {
            sb.append("\t");
        }
        return sb.toString();
    }

    private void writeElement(SymbolTableHandler symbolTables, PhraseStructureNode element, int depth) throws MaltChainedException {
        try {
            if (element instanceof TokenNode) {
                PhraseStructureNode t = element;
                SymbolTable table = null;
                this.writer.write("\n" + this.getIndentation(depth) + this.STARTING_BRACKET);
                int i = 0;
                for (String inputColumn : this.inputColumns.keySet()) {
                    if (i != 0) {
                        this.writer.write(this.INPUT_SEPARATOR);
                    }
                    if (t.hasLabel(table = symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumn)).getName()))) {
                        this.writer.write(this.encodeString(t.getLabelSymbol(table)));
                    }
                    if (i == 0) {
                        for (String edgeLabelColumn : this.edgeLabelColumns.keySet()) {
                            table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                            if (!t.hasParentEdgeLabel(table) || t.getParent().isRoot() || t.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) continue;
                            this.writer.write(this.EDGELABEL_SEPARATOR);
                            this.writer.write(t.getParentEdgeLabelSymbol(table));
                        }
                    }
                    ++i;
                }
                this.writer.write(this.CLOSING_BRACKET);
            } else {
                NonTerminalNode nt = (NonTerminalNode)element;
                this.writer.write("\n" + this.getIndentation(depth) + this.STARTING_BRACKET);
                SymbolTable table = null;
                int i = 0;
                for (String phraseLabelColumn : this.phraseLabelColumns.keySet()) {
                    if (i != 0) {
                        this.writer.write(this.INPUT_SEPARATOR);
                    }
                    if (nt.hasLabel(table = symbolTables.getSymbolTable(((ColumnDescription)this.phraseLabelColumns.get(phraseLabelColumn)).getName()))) {
                        this.writer.write(nt.getLabelSymbol(table));
                    }
                    if (i == 0) {
                        for (String edgeLabelColumn : this.edgeLabelColumns.keySet()) {
                            table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                            if (!nt.hasParentEdgeLabel(table) || nt.getParent().isRoot() || nt.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) continue;
                            this.writer.write(this.EDGELABEL_SEPARATOR);
                            this.writer.write(nt.getParentEdgeLabelSymbol(table));
                        }
                    }
                    ++i;
                }
                for (PhraseStructureNode node : ((NonTerminalNode)element).getChildren()) {
                    this.writeElement(symbolTables, node, depth + 1);
                }
                this.writer.write("\n" + this.getIndentation(depth) + this.CLOSING_BRACKET);
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Could not write to the output file. ", e);
        }
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
        this.inputColumns = dataFormatInstance.getInputColumnDescriptions();
        this.edgeLabelColumns = dataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptions();
        this.phraseLabelColumns = dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptions();
    }

    @Override
    public String getOptions() {
        return this.optionString;
    }

    @Override
    public void setOptions(String optionString) throws MaltChainedException {
        String[] argv;
        this.optionString = optionString;
        this.format = PennWriterFormat.DEFAULT;
        try {
            argv = optionString.split("[_\\p{Blank}]");
        }
        catch (PatternSyntaxException e) {
            throw new DataFormatException("Could not split the bracket writer option '" + optionString + "'. ", e);
        }
        block5 : for (int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
                throw new DataFormatException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }
            if (++i >= argv.length) {
                throw new DataFormatException("The last argument does not have any value. ");
            }
            switch (argv[i - 1].charAt(1)) {
                case 'f': {
                    if (argv[i].equals("p")) {
                        this.format = PennWriterFormat.PRETTY;
                        continue block5;
                    }
                    if (!argv[i].equals("p")) continue block5;
                    this.format = PennWriterFormat.DEFAULT;
                    continue block5;
                }
                default: {
                    throw new DataFormatException("Unknown bracket writer option: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
                }
            }
        }
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

    private String encodeString(String string) {
        return string.replace("(", "-LRB-").replace(")", "-RRB-").replace("[", "-LSB-").replace("]", "-RSB-").replace("{", "-LCB-").replace("}", "-RCB-");
    }

    private static enum PennWriterFormat {
        DEFAULT,
        PRETTY;
        
    }

}

