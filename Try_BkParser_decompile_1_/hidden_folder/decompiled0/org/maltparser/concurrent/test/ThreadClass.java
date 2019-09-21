/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.core.exception.MaltChainedException;

public class ThreadClass
extends Thread {
    private URL inURL;
    private File outFile;
    private String charSet;
    private ConcurrentMaltParserModel model;

    public ThreadClass(String _charSet, String _inFile, String _outFile, ConcurrentMaltParserModel _model) throws MalformedURLException {
        this.charSet = _charSet;
        this.inURL = new File(_inFile).toURI().toURL();
        this.outFile = new File(_outFile);
        this.model = _model;
    }

    public ThreadClass(String _charSet, URL _inUrl, File _outFile, ConcurrentMaltParserModel _model) {
        this.charSet = _charSet;
        this.inURL = _inUrl;
        this.outFile = _outFile;
        this.model = _model;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            String[] goldTokens;
            reader = new BufferedReader(new InputStreamReader(this.inURL.openStream(), this.charSet));
            writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(this.outFile), this.charSet));
            int diffCount = 0;
            int sentenceCount = 0;
            while ((goldTokens = ConcurrentUtils.readSentence(reader)).length != 0) {
                String[] inputTokens = ConcurrentUtils.stripGold(goldTokens);
                String[] outputTokens = this.model.parseTokens(inputTokens);
                diffCount = ConcurrentUtils.diffSentences(goldTokens, outputTokens) ? diffCount + 1 : diffCount;
                ++sentenceCount;
                ConcurrentUtils.writeSentence(outputTokens, writer);
            }
            System.out.println("DiffCount: " + diffCount + "/" + sentenceCount + "(ThreadID:" + Thread.currentThread().getId() + ")");
        }
        catch (IOException e) {
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
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

