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
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.Util;
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
import org.maltparser.core.syntaxgraph.reader.TigerXMLHeader;
import org.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter;

public class TigerXMLWriter
implements SyntaxGraphWriter {
    private BufferedWriter writer;
    private DataFormatInstance dataFormatInstance;
    private String optionString;
    private int sentenceCount;
    private TigerXMLHeader header;
    private RootHandling rootHandling;
    private String sentencePrefix = "s";
    private StringBuilder sentenceID = new StringBuilder();
    private StringBuilder tmpID = new StringBuilder();
    private StringBuilder rootID = new StringBuilder();
    private int START_ID_OF_NONTERMINALS = 500;
    private boolean labeledTerminalID = false;
    private String VROOT_SYMBOL = "VROOT";
    private boolean useVROOT = false;
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
        this.setSentenceCount(0);
    }

    @Override
    public void writeProlog() throws MaltChainedException {
        this.writeHeader();
    }

    @Override
    public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
        if (syntaxGraph == null || this.dataFormatInstance == null) {
            return;
        }
        if (syntaxGraph.hasTokens()) {
            ++this.sentenceCount;
            PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;
            try {
                this.sentenceID.setLength(0);
                this.sentenceID.append(this.sentencePrefix);
                if (phraseStructure.getSentenceID() != 0) {
                    this.sentenceID.append(Integer.toString(phraseStructure.getSentenceID()));
                } else {
                    this.sentenceID.append(Integer.toString(this.sentenceCount));
                }
                this.writer.write("    <s id=\"");
                this.writer.write(this.sentenceID.toString());
                this.writer.write("\">\n");
                this.setRootID(phraseStructure);
                this.writer.write("      <graph root=\"");
                this.writer.write(this.rootID.toString());
                this.writer.write("\" ");
                this.writer.write("discontinuous=\"");
                this.writer.write(Boolean.toString(!phraseStructure.isContinuous()));
                this.writer.write("\">\n");
                this.writeTerminals(phraseStructure);
                if (phraseStructure.nTokenNode() != 1 || this.rootHandling.equals((Object)RootHandling.TALBANKEN)) {
                    this.writeNonTerminals(phraseStructure);
                } else {
                    this.writer.write("        <nonterminals/>\n");
                }
                this.writer.write("      </graph>\n");
                this.writer.write("    </s>\n");
            }
            catch (IOException e) {
                throw new DataFormatException("The TigerXML writer could not write to file. ", e);
            }
        }
    }

    private void setRootID(PhraseStructure phraseStructure) throws MaltChainedException {
        this.useVROOT = false;
        PhraseStructureNode root = phraseStructure.getPhraseStructureRoot();
        SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
        for (ColumnDescription column : this.dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptionSet()) {
            if (!root.hasLabel(symbolTables.getSymbolTable(column.getName())) || !root.getLabelSymbol(symbolTables.getSymbolTable(column.getName())).equals(this.VROOT_SYMBOL)) continue;
            this.useVROOT = true;
            break;
        }
        if (this.useVROOT) {
            this.rootID.setLength(0);
            this.rootID.append(this.sentenceID);
            this.rootID.append('_');
            this.rootID.append(this.VROOT_SYMBOL);
        } else if (phraseStructure.nTokenNode() == 1 && phraseStructure.nNonTerminals() == 0 && !root.isLabeled()) {
            this.rootID.setLength(0);
            this.rootID.append(this.sentenceID);
            this.rootID.append("_1");
        } else {
            this.rootID.setLength(0);
            this.rootID.append(this.sentenceID);
            this.rootID.append('_');
            this.rootID.append(Integer.toString(this.START_ID_OF_NONTERMINALS + phraseStructure.nNonTerminals()));
        }
    }

    @Override
    public void writeEpilog() throws MaltChainedException {
        this.writeTail();
    }

    public BufferedWriter getWriter() {
        return this.writer;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
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

    private void writeHeader() throws MaltChainedException {
        try {
            if (this.header == null) {
                this.header = new TigerXMLHeader();
            }
            this.writer.write(this.header.toTigerXML());
        }
        catch (IOException e) {
            throw new DataFormatException("The TigerXML writer could not write to file. ", e);
        }
    }

    private void writeTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
        try {
            this.writer.write("        <terminals>\n");
            Iterator i$ = phraseStructure.getTokenIndices().iterator();
            while (i$.hasNext()) {
                int index = (Integer)i$.next();
                TokenNode t = phraseStructure.getTokenNode(index);
                this.writer.write("          <t ");
                if (!this.labeledTerminalID) {
                    this.tmpID.setLength(0);
                    this.tmpID.append(this.sentenceID);
                    this.tmpID.append('_');
                    this.tmpID.append(Integer.toString(t.getIndex()));
                    this.writer.write("id=\"");
                    this.writer.write(this.tmpID.toString());
                    this.writer.write("\" ");
                }
                for (ColumnDescription column : this.dataFormatInstance.getInputColumnDescriptionSet()) {
                    this.writer.write(column.getName().toLowerCase());
                    this.writer.write("=\"");
                    this.writer.write(Util.xmlEscape(t.getLabelSymbol(phraseStructure.getSymbolTables().getSymbolTable(column.getName()))));
                    this.writer.write("\" ");
                }
                this.writer.write("/>\n");
            }
            this.writer.write("        </terminals>\n");
        }
        catch (IOException e) {
            throw new DataFormatException("The TigerXML writer is not able to write. ", e);
        }
    }

    public void writeNonTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
        try {
            TreeMap<Integer, Integer> heights = new TreeMap<Integer, Integer>();
            for (int index : phraseStructure.getNonTerminalIndices()) {
                heights.put(index, ((NonTerminalNode)phraseStructure.getNonTerminalNode(index)).getHeight());
            }
            this.writer.write("        <nonterminals>\n");
            boolean done = false;
            int h = 1;
            while (!done) {
                done = true;
                for (int index : phraseStructure.getNonTerminalIndices()) {
                    if ((Integer)heights.get(index) != h) continue;
                    NonTerminalNode nt = (NonTerminalNode)phraseStructure.getNonTerminalNode(index);
                    this.tmpID.setLength(0);
                    this.tmpID.append(this.sentenceID);
                    this.tmpID.append('_');
                    this.tmpID.append(Integer.toString(nt.getIndex() + this.START_ID_OF_NONTERMINALS - 1));
                    this.writeNonTerminal(phraseStructure.getSymbolTables(), nt, this.tmpID.toString());
                    done = false;
                }
                ++h;
            }
            this.writeNonTerminal(phraseStructure.getSymbolTables(), (NonTerminalNode)phraseStructure.getPhraseStructureRoot(), this.rootID.toString());
            this.writer.write("        </nonterminals>\n");
        }
        catch (IOException e) {
            throw new DataFormatException("The TigerXML writer is not able to write. ", e);
        }
    }

    public void writeNonTerminal(SymbolTableHandler symbolTables, NonTerminalNode nt, String id) throws MaltChainedException {
        try {
            this.writer.write("          <nt");
            this.writer.write(" id=\"");
            this.writer.write(id);
            this.writer.write("\" ");
            for (ColumnDescription column : this.dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptionSet()) {
                if (!nt.hasLabel(symbolTables.getSymbolTable(column.getName()))) continue;
                this.writer.write(column.getName().toLowerCase());
                this.writer.write("=");
                this.writer.write("\"");
                this.writer.write(Util.xmlEscape(nt.getLabelSymbol(symbolTables.getSymbolTable(column.getName()))));
                this.writer.write("\" ");
            }
            this.writer.write(">\n");
            int n = nt.nChildren();
            for (int i = 0; i < n; ++i) {
                PhraseStructureNode child = nt.getChild(i);
                this.writer.write("            <edge ");
                for (ColumnDescription column : this.dataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptionSet()) {
                    if (!child.hasParentEdgeLabel(symbolTables.getSymbolTable(column.getName()))) continue;
                    this.writer.write(column.getName().toLowerCase());
                    this.writer.write("=\"");
                    this.writer.write(Util.xmlEscape(child.getParentEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName()))));
                    this.writer.write("\" ");
                }
                if (child instanceof TokenNode) {
                    if (!this.labeledTerminalID) {
                        this.tmpID.setLength(0);
                        this.tmpID.append(this.sentenceID);
                        this.tmpID.append('_');
                        this.tmpID.append(Integer.toString(child.getIndex()));
                        this.writer.write(" idref=\"");
                        this.writer.write(this.tmpID.toString());
                        this.writer.write("\"");
                    } else {
                        this.writer.write(" idref=\"");
                        this.writer.write(child.getLabelSymbol(symbolTables.getSymbolTable("ID")));
                        this.writer.write("\"");
                    }
                } else {
                    this.tmpID.setLength(0);
                    this.tmpID.append(this.sentenceID);
                    this.tmpID.append('_');
                    this.tmpID.append(Integer.toString(child.getIndex() + this.START_ID_OF_NONTERMINALS - 1));
                    this.writer.write(" idref=\"");
                    this.writer.write(this.tmpID.toString());
                    this.writer.write("\"");
                }
                this.writer.write(" />\n");
            }
            this.writer.write("          </nt>\n");
        }
        catch (IOException e) {
            throw new DataFormatException("The TigerXML writer is not able to write. ", e);
        }
    }

    private void writeTail() throws MaltChainedException {
        try {
            this.writer.write("  </body>\n");
            this.writer.write("</corpus>\n");
            this.writer.flush();
        }
        catch (IOException e) {
            throw new DataFormatException("The TigerXML writer is not able to write. ", e);
        }
    }

    public int getSentenceCount() {
        return this.sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    @Override
    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
    public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
        this.dataFormatInstance = dataFormatInstance;
        this.labeledTerminalID = dataFormatInstance.getInputColumnDescriptions().containsKey("id") || dataFormatInstance.getInputColumnDescriptions().containsKey("ID");
    }

    @Override
    public String getOptions() {
        return this.optionString;
    }

    @Override
    public void setOptions(String optionString) throws MaltChainedException {
        String[] argv;
        this.optionString = optionString;
        this.rootHandling = RootHandling.NORMAL;
        try {
            argv = optionString.split("[_\\p{Blank}]");
        }
        catch (PatternSyntaxException e) {
            throw new DataFormatException("Could not split the TigerXML writer option '" + optionString + "'. ", e);
        }
        block9 : for (int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
                throw new DataFormatException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }
            if (++i >= argv.length) {
                throw new DataFormatException("The last argument does not have any value. ");
            }
            switch (argv[i - 1].charAt(1)) {
                case 'r': {
                    if (argv[i].equals("n")) {
                        this.rootHandling = RootHandling.NORMAL;
                        continue block9;
                    }
                    if (!argv[i].equals("tal")) continue block9;
                    this.rootHandling = RootHandling.TALBANKEN;
                    continue block9;
                }
                case 's': {
                    try {
                        this.START_ID_OF_NONTERMINALS = Integer.parseInt(argv[i]);
                        continue block9;
                    }
                    catch (NumberFormatException e) {
                        throw new MaltChainedException("The TigerXML writer option -s must be an integer value. ");
                    }
                }
                case 'v': {
                    this.VROOT_SYMBOL = argv[i];
                    continue block9;
                }
                default: {
                    throw new DataFormatException("Unknown TigerXML writer option: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
                }
            }
        }
    }

    private static enum RootHandling {
        TALBANKEN,
        NORMAL;
        
    }

}

