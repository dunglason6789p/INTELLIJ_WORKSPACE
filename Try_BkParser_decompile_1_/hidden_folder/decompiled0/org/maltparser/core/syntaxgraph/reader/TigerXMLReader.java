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
import java.util.regex.PatternSyntaxException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;

public class TigerXMLReader
implements SyntaxGraphReader {
    private XMLStreamReader reader;
    private int sentenceCount;
    private DataFormatInstance dataFormatInstance;
    private StringBuffer ntid = new StringBuffer();
    private final StringBuilder graphRootID = new StringBuilder();
    private String optionString;
    private String fileName = null;
    private URL url = null;
    private String charsetName;
    private int nIterations = 1;
    private int cIterations = 1;
    private int START_ID_OF_NONTERMINALS = 500;
    private boolean closeStream = true;

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
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            this.setReader(factory.createXMLStreamReader(new BufferedReader(isr)));
        }
        catch (XMLStreamException e) {
            throw new DataFormatException("XML input file could be opened. ", e);
        }
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
        PhraseStructureNode parent = null;
        PhraseStructureNode child = null;
        try {
            block4 : do {
                int event;
                Iterator<String> i$;
                String name;
                if ((event = this.reader.next()) == 1) {
                    if (this.reader.getLocalName().length() == 0) continue;
                    if (this.reader.getLocalName().charAt(0) == 'e') {
                        if (this.reader.getLocalName().length() == 4) {
                            int childid = -1;
                            int indexSep = this.reader.getAttributeValue(null, "idref").indexOf(95);
                            try {
                                childid = indexSep != -1 ? Integer.parseInt(this.reader.getAttributeValue(null, "idref").substring(indexSep + 1)) : Integer.parseInt(this.reader.getAttributeValue(null, "idref"));
                                if (childid == -1) {
                                    throw new SyntaxGraphException("The tiger reader couldn't recognize the idref attribute '" + this.reader.getAttributeValue(null, "idref") + "' of the edge element. ");
                                }
                            }
                            catch (NumberFormatException e) {
                                throw new SyntaxGraphException("The tiger reader couldn't recognize the idref attribute '" + this.reader.getAttributeValue(null, "idref") + "' of the edge element. ");
                            }
                            child = childid < this.START_ID_OF_NONTERMINALS ? phraseStructure.getTokenNode(childid) : phraseStructure.getNonTerminalNode(childid - this.START_ID_OF_NONTERMINALS + 1);
                            Edge e = phraseStructure.addPhraseStructureEdge(parent, child);
                            SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getPhraseStructureEdgeLabelSymbolTables(phraseStructure.getSymbolTables());
                            Iterator<String> i$2 = inputTables.keySet().iterator();
                            do {
                                if (!i$2.hasNext()) continue block4;
                                String name2 = i$2.next();
                                e.addLabel((SymbolTable)inputTables.get(name2), this.reader.getAttributeValue(null, name2.toLowerCase()));
                            } while (true);
                        }
                        if (!this.reader.getLocalName().equals("edgelabel")) continue;
                        continue;
                    }
                    if (this.reader.getLocalName().charAt(0) == 'n') {
                        if (this.reader.getLocalName().length() == 2) {
                            String id = this.reader.getAttributeValue(null, "id");
                            if (this.graphRootID.length() == id.length() && this.graphRootID.toString().equals(id)) {
                                parent = phraseStructure.getPhraseStructureRoot();
                            } else {
                                int index = id.indexOf(95);
                                if (index != -1) {
                                    parent = phraseStructure.addNonTerminalNode(Integer.parseInt(id.substring(index + 1)) - this.START_ID_OF_NONTERMINALS + 1);
                                }
                            }
                            SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getPhraseStructureNodeLabelSymbolTables(phraseStructure.getSymbolTables());
                            i$ = inputTables.keySet().iterator();
                            do {
                                if (!i$.hasNext()) continue block4;
                                name = i$.next();
                                parent.addLabel((SymbolTable)inputTables.get(name), this.reader.getAttributeValue(null, name.toLowerCase()));
                            } while (true);
                        }
                        if (!this.reader.getLocalName().equals("name")) continue;
                        continue;
                    }
                    if (this.reader.getLocalName().charAt(0) == 't') {
                        if (this.reader.getLocalName().length() != 1) continue;
                        SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getInputSymbolTables(phraseStructure.getSymbolTables());
                        child = syntaxGraph.addTokenNode();
                        Iterator<String> i$3 = inputTables.keySet().iterator();
                        do {
                            if (!i$3.hasNext()) continue block4;
                            String name3 = i$3.next();
                            child.addLabel((SymbolTable)inputTables.get(name3), this.reader.getAttributeValue(null, name3.toLowerCase()));
                        } while (true);
                    }
                    if (this.reader.getLocalName().charAt(0) == 's') {
                        if (this.reader.getLocalName().length() != 1) continue;
                        String id = this.reader.getAttributeValue(null, "id");
                        boolean indexable = false;
                        int index = -1;
                        if (id != null && id.length() > 0) {
                            int n = id.length();
                            for (int i = 0; i < n; ++i) {
                                if (!Character.isDigit(id.charAt(i))) continue;
                                if (index == -1) {
                                    index = i;
                                }
                                indexable = true;
                            }
                        }
                        if (indexable) {
                            phraseStructure.setSentenceID(Integer.parseInt(id.substring(index)));
                            continue;
                        }
                        phraseStructure.setSentenceID(this.sentenceCount + 1);
                        continue;
                    }
                    if (this.reader.getLocalName().charAt(0) == 'v') continue;
                    if (this.reader.getLocalName().equals("graph")) {
                        this.graphRootID.setLength(0);
                        this.graphRootID.append(this.reader.getAttributeValue(null, "root"));
                        continue;
                    }
                    if (this.reader.getLocalName().equals("corpus") || this.reader.getLocalName().equals("feature") || this.reader.getLocalName().equals("secedgelabel") || this.reader.getLocalName().equals("author") || this.reader.getLocalName().equals("date") || this.reader.getLocalName().equals("description") || this.reader.getLocalName().equals("format") || !this.reader.getLocalName().equals("history")) continue;
                    continue;
                }
                if (event == 2) {
                    if (this.reader.getLocalName().length() == 0 || this.reader.getLocalName().charAt(0) == 'e') continue;
                    if (this.reader.getLocalName().charAt(0) == 'n') {
                        if (this.reader.getLocalName().equals("nt")) {
                            this.ntid.setLength(0);
                            continue;
                        }
                        if (!this.reader.getLocalName().equals("nonterminals") || phraseStructure.nTokenNode() != 1 || phraseStructure.nNonTerminals() != 0 || ((NonTerminalNode)phraseStructure.getPhraseStructureRoot()).nChildren() != 0) continue;
                        Edge e = phraseStructure.addPhraseStructureEdge(phraseStructure.getPhraseStructureRoot(), phraseStructure.getTokenNode(1));
                        SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getPhraseStructureEdgeLabelSymbolTables(phraseStructure.getSymbolTables());
                        i$ = inputTables.keySet().iterator();
                        do {
                            if (!i$.hasNext()) continue block4;
                            name = i$.next();
                            e.addLabel((SymbolTable)inputTables.get(name), "--");
                        } while (true);
                    }
                    if (this.reader.getLocalName().charAt(0) == 't') continue;
                    if (this.reader.getLocalName().charAt(0) == 's') {
                        if (!this.reader.getLocalName().equals("s")) continue;
                        if (syntaxGraph.hasTokens()) {
                            ++this.sentenceCount;
                        }
                        if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                            ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                        }
                        return true;
                    }
                    if (this.reader.getLocalName().charAt(0) == 'v' || this.reader.getLocalName().equals("body") || this.reader.getLocalName().equals("author") || this.reader.getLocalName().equals("date") || this.reader.getLocalName().equals("description") || this.reader.getLocalName().equals("format") || !this.reader.getLocalName().equals("history")) continue;
                    continue;
                }
                if (event == 8) {
                    if (syntaxGraph.hasTokens()) {
                        ++this.sentenceCount;
                    }
                    if (this.cIterations < this.nIterations) {
                        ++this.cIterations;
                        this.reopen();
                        return true;
                    }
                    return false;
                }
                if (event != 4) continue;
            } while (true);
        }
        catch (XMLStreamException e) {
            throw new DataFormatException("", e);
        }
    }

    @Override
    public int getSentenceCount() {
        return this.sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public XMLStreamReader getReader() {
        return this.reader;
    }

    public void setReader(XMLStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public void readEpilog() throws MaltChainedException {
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
        catch (XMLStreamException e) {
            throw new DataFormatException("The XML input file could be closed. ", e);
        }
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
            throw new DataFormatException("Could not split the TigerXML reader option '" + optionString + "'. ", e);
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
                    throw new DataFormatException("Unknown TigerXMLReader parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
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
}

