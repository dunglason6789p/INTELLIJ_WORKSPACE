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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;

public class NegraReader
implements SyntaxGraphReader {
    private BufferedReader reader;
    private DataFormatInstance dataFormatInstance;
    private int sentenceCount;
    private String optionString;
    private int formatVersion;
    private NegraTables currentHeaderTable = NegraTables.UNDEF;
    private int currentTerminalSize;
    private int currentNonTerminalSize;
    private SortedMap<Integer, PhraseStructureNode> nonterminals;
    private StringBuilder edgelabelSymbol = new StringBuilder();
    private StringBuilder edgelabelTableName = new StringBuilder();
    private int START_ID_OF_NONTERMINALS = 500;
    private String fileName = null;
    private URL url = null;
    private String charsetName;
    private int nIterations = 1;
    private int cIterations = 1;
    private boolean closeStream = true;

    public NegraReader() {
        this.nonterminals = new TreeMap<Integer, PhraseStructureNode>();
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
        if (syntaxGraph == null || !(syntaxGraph instanceof PhraseStructure)) {
            return false;
        }
        syntaxGraph.clear();
        PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;
        SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
        PhraseStructureNode parent = null;
        PhraseStructureNode child = null;
        this.currentHeaderTable = NegraTables.UNDEF;
        String line = null;
        syntaxGraph.clear();
        this.nonterminals.clear();
        try {
            block2 : do {
                if ((line = this.reader.readLine()) == null) {
                    if (syntaxGraph.hasTokens()) {
                        ++this.sentenceCount;
                        if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                            ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                        }
                    }
                    if (this.cIterations < this.nIterations) {
                        ++this.cIterations;
                        this.reopen();
                        return true;
                    }
                    return false;
                }
                if (line.startsWith("#EOS")) {
                    this.currentTerminalSize = 0;
                    this.currentNonTerminalSize = 0;
                    this.currentHeaderTable = NegraTables.UNDEF;
                    if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                        ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                    }
                    return true;
                }
                if (line.startsWith("#BOS")) {
                    this.currentHeaderTable = NegraTables.SENTENCE;
                    int s = -1;
                    int e = -1;
                    int n = line.length();
                    for (int i = 5; i < n; ++i) {
                        if (Character.isDigit(line.charAt(i)) && s == -1) {
                            s = i;
                        }
                        if (line.charAt(i) != ' ') continue;
                        e = i;
                        break;
                    }
                    if (s != e && s != -1 && e != -1) {
                        phraseStructure.setSentenceID(Integer.parseInt(line.substring(s, e)));
                    }
                    ++this.sentenceCount;
                    continue;
                }
                if (this.currentHeaderTable == NegraTables.SENTENCE) {
                    int index;
                    Edge e;
                    int n;
                    int start;
                    int i;
                    int secedgecounter;
                    if (line.length() >= 2 && line.charAt(0) == '#' && Character.isDigit(line.charAt(1))) {
                        Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
                        ColumnDescription column = null;
                        ++this.currentNonTerminalSize;
                        char[] lineChars = line.toCharArray();
                        start = 0;
                        secedgecounter = 0;
                        i = 0;
                        n = lineChars.length;
                        do {
                            if (i >= n) continue block2;
                            if (lineChars[i] == '\t' && start == i) {
                                ++start;
                            } else if (lineChars[i] == '\t' || i == n - 1) {
                                if (columns.hasNext()) {
                                    column = columns.next();
                                }
                                if (column.getPosition() == 0) {
                                    index = Integer.parseInt(i == n - 1 ? line.substring(start + 1) : line.substring(start + 1, i));
                                    child = (PhraseStructureNode)this.nonterminals.get(index);
                                    if (child == null) {
                                        if (index != 0) {
                                            child = ((PhraseStructure)syntaxGraph).addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                        }
                                        this.nonterminals.put(index, child);
                                    }
                                } else if (column.getPosition() == 2 && child != null) {
                                    syntaxGraph.addLabel(child, "CAT", i == n - 1 ? line.substring(start) : line.substring(start, i));
                                } else if (column.getCategory() == 4) {
                                    this.edgelabelSymbol.setLength(0);
                                    this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                    this.edgelabelTableName.setLength(0);
                                    this.edgelabelTableName.append(column.getName());
                                } else if (column.getCategory() == 5 && child != null) {
                                    index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                    parent = (PhraseStructureNode)this.nonterminals.get(index);
                                    if (parent == null) {
                                        parent = index == 0 ? phraseStructure.getPhraseStructureRoot() : phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                        this.nonterminals.put(index, parent);
                                    }
                                    e = phraseStructure.addPhraseStructureEdge(parent, child);
                                    syntaxGraph.addLabel(e, this.edgelabelTableName.toString(), this.edgelabelSymbol.toString());
                                } else if (column.getCategory() == 6 && child != null) {
                                    if (secedgecounter % 2 == 0) {
                                        this.edgelabelSymbol.setLength(0);
                                        this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                        ++secedgecounter;
                                    } else {
                                        index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                        if (index == 0) {
                                            parent = phraseStructure.getPhraseStructureRoot();
                                        } else if (index < this.START_ID_OF_NONTERMINALS) {
                                            parent = phraseStructure.getTokenNode(index);
                                        } else {
                                            parent = (PhraseStructureNode)this.nonterminals.get(index);
                                            if (parent == null) {
                                                parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                                this.nonterminals.put(index, parent);
                                            }
                                        }
                                        e = phraseStructure.addSecondaryEdge(parent, child);
                                        e.addLabel(symbolTables.getSymbolTable(column.getName()), this.edgelabelSymbol.toString());
                                        ++secedgecounter;
                                    }
                                }
                                start = i + 1;
                            }
                            ++i;
                        } while (true);
                    }
                    Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
                    ColumnDescription column = null;
                    ++this.currentTerminalSize;
                    child = syntaxGraph.addTokenNode(this.currentTerminalSize);
                    char[] lineChars = line.toCharArray();
                    start = 0;
                    secedgecounter = 0;
                    i = 0;
                    n = lineChars.length;
                    do {
                        if (i >= n) continue block2;
                        if (lineChars[i] == '\t' && start == i) {
                            ++start;
                        } else if (lineChars[i] == '\t' || i == n - 1) {
                            if (columns.hasNext()) {
                                column = columns.next();
                            }
                            if (column.getCategory() == 1 && child != null) {
                                syntaxGraph.addLabel(child, column.getName(), i == n - 1 ? line.substring(start) : line.substring(start, i));
                            } else if (column.getCategory() == 4 && child != null) {
                                this.edgelabelSymbol.setLength(0);
                                this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                this.edgelabelTableName.setLength(0);
                                this.edgelabelTableName.append(column.getName());
                            } else if (column.getCategory() == 5 && child != null) {
                                index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                parent = (PhraseStructureNode)this.nonterminals.get(index);
                                if (parent == null) {
                                    parent = index == 0 ? phraseStructure.getPhraseStructureRoot() : phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                    this.nonterminals.put(index, parent);
                                }
                                e = phraseStructure.addPhraseStructureEdge(parent, child);
                                syntaxGraph.addLabel(e, this.edgelabelTableName.toString(), this.edgelabelSymbol.toString());
                            } else if (column.getCategory() == 6 && child != null) {
                                if (secedgecounter % 2 == 0) {
                                    this.edgelabelSymbol.setLength(0);
                                    this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                    ++secedgecounter;
                                } else {
                                    index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                    if (index == 0) {
                                        parent = phraseStructure.getPhraseStructureRoot();
                                    } else if (index < this.START_ID_OF_NONTERMINALS) {
                                        parent = phraseStructure.getTokenNode(index);
                                    } else {
                                        parent = (PhraseStructureNode)this.nonterminals.get(index);
                                        if (parent == null) {
                                            parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                            this.nonterminals.put(index, parent);
                                        }
                                    }
                                    e = phraseStructure.addSecondaryEdge(parent, child);
                                    e.addLabel(symbolTables.getSymbolTable(column.getName()), this.edgelabelSymbol.toString());
                                    ++secedgecounter;
                                }
                            }
                            start = i + 1;
                        }
                        ++i;
                    } while (true);
                }
                if (line.startsWith("%%") || line.startsWith("#FORMAT") || line.startsWith("#BOT") || !line.startsWith("#EOT")) continue;
                this.currentHeaderTable = NegraTables.UNDEF;
            } while (true);
        }
        catch (IOException e) {
            throw new DataFormatException("Error when reading from the input file. ", e);
        }
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
    public int getSentenceCount() {
        return this.sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public int getFormatVersion() {
        return this.formatVersion;
    }

    public void setFormatVersion(int formatVersion) {
        this.formatVersion = formatVersion;
    }

    @Override
    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
    public void setDataFormatInstance(DataFormatInstance inputDataFormatInstance) {
        this.dataFormatInstance = inputDataFormatInstance;
    }

    @Override
    public String getOptions() {
        return this.optionString;
    }

    @Override
    public void setOptions(String optionString) throws MaltChainedException {
        String[] argv;
        this.optionString = optionString;
        try {
            argv = optionString.split("[_\\p{Blank}]");
        }
        catch (PatternSyntaxException e) {
            throw new DataFormatException("Could not split the penn writer option '" + optionString + "'. ", e);
        }
        block7 : for (int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
                throw new DataFormatException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }
            if (++i >= argv.length) {
                throw new DataFormatException("The last argument does not have any value. ");
            }
            switch (argv[i - 1].charAt(1)) {
                case 's': {
                    try {
                        this.START_ID_OF_NONTERMINALS = Integer.parseInt(argv[i]);
                        continue block7;
                    }
                    catch (NumberFormatException e) {
                        throw new MaltChainedException("The TigerXML Reader option -s must be an integer value. ");
                    }
                }
                default: {
                    throw new DataFormatException("Unknown NegraReader parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
                }
            }
        }
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

    private static enum NegraTables {
        ORIGIN,
        EDITOR,
        WORDTAG,
        MORPHTAG,
        NODETAG,
        EDGETAG,
        SECEDGETAG,
        SENTENCE,
        UNDEF;
        
    }

}

