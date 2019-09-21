/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;

public class BracketReader
implements SyntaxGraphReader {
    private BufferedReader reader;
    private DataFormatInstance dataFormatInstance;
    private int sentenceCount;
    private StringBuilder input;
    private int terminalCounter;
    private int nonTerminalCounter;
    private String optionString;
    private SortedMap<String, ColumnDescription> inputColumns;
    private SortedMap<String, ColumnDescription> edgeLabelColumns;
    private SortedMap<String, ColumnDescription> phraseLabelColumns;
    private String fileName = null;
    private URL url = null;
    private String charsetName;
    private int nIterations = 1;
    private int cIterations = 1;
    private boolean closeStream = true;
    private char STARTING_BRACKET = (char)40;
    private char CLOSING_BRACKET = (char)41;
    private char INPUT_SEPARATOR = (char)32;
    private char EDGELABEL_SEPARATOR = (char)45;
    private char SENTENCE_SEPARATOR = (char)10;
    private char BLANK = (char)32;
    private char CARRIAGE_RETURN = (char)13;
    private char TAB = (char)9;

    public BracketReader() {
        this.input = new StringBuilder();
    }

    private void reopen() throws MaltChainedException {
        this.close();
        if (this.fileName != null) {
            this.open(this.fileName, this.charsetName);
        } else if (this.url != null) {
            this.open(this.url, this.charsetName);
        } else {
            throw new DataFormatException("The input stream cannot be reopen. ");
        }
    }

    @Override
    public void open(String fileName, String charsetName) throws MaltChainedException {
        this.setFileName(fileName);
        this.setCharsetName(charsetName);
        try {
            this.open(new FileInputStream(fileName), charsetName);
        }
        catch (FileNotFoundException e) {
            throw new DataFormatException("The input file '" + fileName + "' cannot be found. ", e);
        }
    }

    @Override
    public void open(URL url, String charsetName) throws MaltChainedException {
        this.setUrl(url);
        this.setCharsetName(charsetName);
        try {
            this.open(url.openStream(), charsetName);
        }
        catch (IOException e) {
            throw new DataFormatException("The URL '" + url.toString() + "' cannot be opened. ", e);
        }
    }

    @Override
    public void open(InputStream is, String charsetName) throws MaltChainedException {
        try {
            if (is == System.in) {
                this.closeStream = false;
            }
            this.open(new InputStreamReader(is, charsetName));
        }
        catch (UnsupportedEncodingException e) {
            throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported. ", e);
        }
    }

    private void open(InputStreamReader isr) throws MaltChainedException {
        this.setReader(new BufferedReader(isr));
        this.setSentenceCount(0);
    }

    @Override
    public void readProlog() throws MaltChainedException {
    }

    @Override
    public boolean readSentence(TokenStructure syntaxGraph) throws MaltChainedException {
        if (syntaxGraph == null || this.dataFormatInstance == null) {
            return false;
        }
        syntaxGraph.clear();
        int brackets = 0;
        try {
            char c;
            int l = this.reader.read();
            this.input.setLength(0);
            do {
                if (l == -1) {
                    this.input.setLength(0);
                    return false;
                }
                c = (char)l;
                l = this.reader.read();
                if (c != this.SENTENCE_SEPARATOR && c != this.CARRIAGE_RETURN && c != this.TAB && c != '\uffffffff') {
                    if (c == this.STARTING_BRACKET) {
                        this.input.append(c);
                        ++brackets;
                    } else if (c == this.CLOSING_BRACKET) {
                        this.input.append(c);
                        --brackets;
                    } else if (c == this.INPUT_SEPARATOR) {
                        if (l != this.STARTING_BRACKET && l != this.CLOSING_BRACKET && l != this.INPUT_SEPARATOR && l != this.SENTENCE_SEPARATOR && l != this.CARRIAGE_RETURN && l != this.TAB && l != -1) {
                            this.input.append(c);
                        }
                    } else if (c == '\\') {
                        c = (char)l;
                        l = this.reader.read();
                        if (c != ' ' && c != '(' && c != ')' && c != '\\' && c != 'n' && c != 'r' && c != 't' && c != '\"' && c != '\'') {
                            System.exit(1);
                        } else {
                            this.input.append("\\" + c);
                        }
                    } else if (brackets != 0) {
                        this.input.append(c);
                    }
                }
                if (brackets != 0 || this.input.length() == 0) continue;
                ++this.sentenceCount;
                this.terminalCounter = 1;
                this.nonTerminalCounter = 1;
                if (syntaxGraph instanceof PhraseStructure) {
                    this.bracketing((PhraseStructure)syntaxGraph, 0, this.input.length(), null);
                    if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                        ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                    }
                }
                return true;
            } while (c != '\uffffffff');
            if (brackets != 0) {
                this.close();
                throw new MaltChainedException("Error when reading from the input file. ");
            }
            if (this.cIterations < this.nIterations) {
                ++this.cIterations;
                this.reopen();
                return true;
            }
            return false;
        }
        catch (IOException e) {
            this.close();
            throw new MaltChainedException("Error when reading from the input file. ", e);
        }
    }

    private void bracketing(PhraseStructure phraseStructure, int start, int end, PhraseStructureNode parent) throws MaltChainedException {
        int bracketsdepth = 0;
        int startpos = start - 1;
        int n = end;
        for (int i = start; i < n; ++i) {
            if (this.input.charAt(i) == this.STARTING_BRACKET && (i == 0 || this.input.charAt(i - 1) != '\\')) {
                if (bracketsdepth == 0) {
                    startpos = i;
                }
                ++bracketsdepth;
                continue;
            }
            if (this.input.charAt(i) != this.CLOSING_BRACKET || i != 0 && this.input.charAt(i - 1) == '\\' || --bracketsdepth != 0) continue;
            this.extract(phraseStructure, startpos + 1, i, parent);
        }
    }

    private void extract(PhraseStructure phraseStructure, int begin, int end, PhraseStructureNode parent) throws MaltChainedException {
        SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
        int index = -1;
        for (int i = begin; i < end; ++i) {
            if (this.input.charAt(i) != this.STARTING_BRACKET || i != begin && this.input.charAt(i - 1) == '\\') continue;
            index = i;
            break;
        }
        if (index == -1) {
            TokenNode t = phraseStructure.addTokenNode(this.terminalCounter);
            if (t == null) {
                this.close();
                throw new MaltChainedException("Bracket Reader error: could not create a terminal node. ");
            }
            ++this.terminalCounter;
            Edge e = null;
            if (parent == null) {
                this.close();
                throw new MaltChainedException("Bracket Reader error: could not find the parent node. ");
            }
            e = phraseStructure.addPhraseStructureEdge(parent, t);
            int start = begin;
            Iterator<String> inputColumnsIterator = this.inputColumns.keySet().iterator();
            Iterator<String> edgeLabelsColumnsIterator = this.edgeLabelColumns.keySet().iterator();
            boolean noneNode = false;
            boolean edgeLabels = false;
            for (int i = begin; i < end; ++i) {
                if (this.input.charAt(i) != this.EDGELABEL_SEPARATOR && (this.input.charAt(i) != this.INPUT_SEPARATOR || i != begin && this.input.charAt(i - 1) == '\\') && i != end - 1) continue;
                if (i == begin && this.input.charAt(i) == this.EDGELABEL_SEPARATOR) {
                    noneNode = true;
                    continue;
                }
                if (start == begin) {
                    if ((!noneNode || this.input.charAt(i) == this.EDGELABEL_SEPARATOR) && noneNode) continue;
                    if (inputColumnsIterator.hasNext()) {
                        t.addLabel(symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumnsIterator.next())).getName()), this.decodeString(i == end - 1 ? this.input.substring(start, end) : this.input.substring(start, i)));
                    }
                    start = i + 1;
                    if (this.input.charAt(i) != this.EDGELABEL_SEPARATOR) continue;
                    edgeLabels = true;
                    continue;
                }
                if (edgeLabels && e != null) {
                    if (edgeLabelsColumnsIterator.hasNext()) {
                        e.addLabel(symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelsColumnsIterator.next())).getName()), i == end - 1 ? this.input.substring(start, end) : this.input.substring(start, i));
                    }
                    start = i + 1;
                    if (this.input.charAt(i) != this.INPUT_SEPARATOR || i != begin && this.input.charAt(i - 1) == '\\') continue;
                    edgeLabels = false;
                    continue;
                }
                if (this.input.charAt(i) == this.EDGELABEL_SEPARATOR && i != end - 1 && this.input.charAt(i + 1) != this.INPUT_SEPARATOR && (i == begin || this.input.charAt(i - 1) != '\\')) continue;
                if (inputColumnsIterator.hasNext()) {
                    t.addLabel(symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumnsIterator.next())).getName()), i == end - 1 ? this.input.substring(start, end) : this.input.substring(start, i));
                }
                start = i + 1;
            }
        } else {
            PhraseStructureNode nt;
            Edge e = null;
            if (parent == null) {
                nt = phraseStructure.getPhraseStructureRoot();
            } else {
                nt = phraseStructure.addNonTerminalNode(this.nonTerminalCounter);
                if (nt == null) {
                    this.close();
                    throw new MaltChainedException("Bracket Reader error: could not create a nonterminal node. ");
                }
                ++this.nonTerminalCounter;
                e = phraseStructure.addPhraseStructureEdge(parent, nt);
            }
            Iterator<String> phraseLabelColumnsIterator = this.phraseLabelColumns.keySet().iterator();
            Iterator<String> edgeLabelsColumnsIterator = this.edgeLabelColumns.keySet().iterator();
            int newbegin = begin;
            int start = begin;
            for (int i = begin; i < index; ++i) {
                if (this.input.charAt(i) == this.EDGELABEL_SEPARATOR || i == index - 1) {
                    if (start == newbegin) {
                        if (phraseLabelColumnsIterator.hasNext()) {
                            nt.addLabel(symbolTables.getSymbolTable(((ColumnDescription)this.phraseLabelColumns.get(phraseLabelColumnsIterator.next())).getName()), i == index - 1 ? this.input.substring(start, index) : this.input.substring(start, i));
                        }
                        start = i + 1;
                        continue;
                    }
                    if (e == null) continue;
                    if (edgeLabelsColumnsIterator.hasNext()) {
                        e.addLabel(symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelsColumnsIterator.next())).getName()), i == index - 1 ? this.input.substring(start, index) : this.input.substring(start, i));
                    }
                    start = i + 1;
                    continue;
                }
                if (this.input.charAt(i) != this.BLANK) continue;
                ++start;
                ++newbegin;
            }
            this.bracketing(phraseStructure, index, end, nt);
        }
    }

    private String decodeString(String string) {
        return string.replace("\\(", "(").replace("\\)", ")").replace("\\ ", " ");
    }

    @Override
    public void readEpilog() throws MaltChainedException {
    }

    public BufferedReader getReader() {
        return this.reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public int getSentenceCount() throws MaltChainedException {
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
    public void setDataFormatInstance(DataFormatInstance inputDataFormatInstance) {
        this.dataFormatInstance = inputDataFormatInstance;
        this.inputColumns = this.dataFormatInstance.getInputColumnDescriptions();
        this.edgeLabelColumns = this.dataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptions();
        this.phraseLabelColumns = this.dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptions();
    }

    @Override
    public String getOptions() {
        return this.optionString;
    }

    @Override
    public void setOptions(String optionString) throws MaltChainedException {
        this.optionString = optionString;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getCharsetName() {
        return this.charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    @Override
    public int getNIterations() {
        return this.nIterations;
    }

    @Override
    public void setNIterations(int iterations) {
        this.nIterations = iterations;
    }

    @Override
    public int getIterationCounter() {
        return this.cIterations;
    }

    @Override
    public void close() throws MaltChainedException {
        try {
            if (this.reader != null) {
                if (this.closeStream) {
                    this.reader.close();
                }
                this.reader = null;
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Error when closing the input file.", e);
        }
    }
}

