/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.ConcurrentUtils;
import org.maltparser.concurrent.MaltParserRunnable;
import org.maltparser.core.config.ConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionManager;

public class ConcurrentEngine {
    private final int optionContainer;
    private ConcurrentMaltParserModel model;

    public ConcurrentEngine(int optionContainer) {
        System.out.println("Start ConcurrentEngine");
        this.optionContainer = optionContainer;
    }

    public static boolean canUseConcurrentEngine(int optionContainer) throws MaltChainedException {
        if (!OptionManager.instance().getOptionValueString(optionContainer, "config", "flowchart").equals("parse")) {
            return false;
        }
        return OptionManager.instance().getOptionValueString(optionContainer, "config", "url").length() <= 0;
    }

    public static String getMessageWithElapsed(String message, long startTime) {
        StringBuilder sb = new StringBuilder();
        long elapsed = (System.nanoTime() - startTime) / 1000000L;
        sb.append(message);
        sb.append(": ");
        sb.append(elapsed);
        sb.append(" ms");
        return sb.toString();
    }

    public void loadModel() throws MaltChainedException {
        System.out.println("Start loadModel");
        long startTime = System.nanoTime();
        File workingDirectory = this.getWorkingDirectory(OptionManager.instance().getOptionValue(this.optionContainer, "config", "workingdir").toString());
        String configName = OptionManager.instance().getOptionValueString(this.optionContainer, "config", "name");
        String pathToModel = workingDirectory.getPath() + File.separator + configName + ".mco";
        try {
            this.model = ConcurrentMaltParserService.initializeParserModel(new File(pathToModel).toURI().toURL());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ConcurrentEngine.getMessageWithElapsed("Loading time", startTime));
    }

    public File getWorkingDirectory(String path) throws MaltChainedException {
        File workingDirectory = path == null || path.equalsIgnoreCase("user.dir") || path.equalsIgnoreCase(".") ? new File(System.getProperty("user.dir")) : new File(path);
        if (workingDirectory == null || !workingDirectory.isDirectory()) {
            new ConfigurationException("The specified working directory '" + path + "' is not a directory. ");
        }
        return workingDirectory;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void parse() throws MaltChainedException {
        int i;
        System.out.println("Start parse");
        long startTime = System.nanoTime();
        ArrayList<String[]> inputSentences = new ArrayList<String[]>();
        BufferedReader reader = null;
        String infile = OptionManager.instance().getOptionValueString(this.optionContainer, "input", "infile");
        String incharset = OptionManager.instance().getOptionValueString(this.optionContainer, "input", "charset");
        try {
            String[] tokens;
            reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(infile), incharset));
            while ((tokens = ConcurrentUtils.readSentence(reader)).length != 0) {
                inputSentences.add(tokens);
            }
        }
        catch (IOException e) {
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
        System.out.println(ConcurrentEngine.getMessageWithElapsed("Read sentences time", startTime));
        int numberOfThreads = 8;
        Thread[] threads = new Thread[numberOfThreads];
        MaltParserRunnable[] runnables = new MaltParserRunnable[numberOfThreads];
        int nSentences = inputSentences.size();
        int interval = nSentences / numberOfThreads;
        int startIndex = 0;
        int t = 0;
        System.out.println("Number of sentences : " + nSentences);
        while (startIndex < nSentences) {
            int endIndex = startIndex + interval < nSentences && t < threads.length - 1 ? startIndex + interval : nSentences;
            System.out.println("  Thread " + String.format("%03d", t) + " will parse sentences between " + String.format("%04d", startIndex) + " - " + String.format("%04d", endIndex - 1) + ", number of sentences: " + (endIndex - startIndex));
            runnables[t] = new MaltParserRunnable(inputSentences.subList(startIndex, endIndex), this.model);
            threads[t] = new Thread(runnables[t]);
            startIndex = endIndex;
            ++t;
        }
        System.out.println(ConcurrentEngine.getMessageWithElapsed("Create threads time", startTime));
        startTime = System.nanoTime();
        for (i = 0; i < threads.length; ++i) {
            if (threads[i] != null) {
                threads[i].start();
                continue;
            }
            System.err.println("Thread " + i + " is null");
        }
        for (i = 0; i < threads.length; ++i) {
            try {
                if (threads[i] != null) {
                    threads[i].join();
                    continue;
                }
                System.err.println("Thread " + i + " is null");
                continue;
            }
            catch (InterruptedException ignore) {
                // empty catch block
            }
        }
        System.out.println(ConcurrentEngine.getMessageWithElapsed("Parsing time", startTime));
        startTime = System.nanoTime();
        String outfile = OptionManager.instance().getOptionValueString(this.optionContainer, "output", "outfile");
        String outcharset = OptionManager.instance().getOptionValueString(this.optionContainer, "output", "charset");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(outfile), outcharset));
            for (int i2 = 0; i2 < threads.length; ++i2) {
                List<String[]> outputSentences = runnables[i2].getOutputSentences();
                for (int j = 0; j < outputSentences.size(); ++j) {
                    ConcurrentUtils.writeSentence(outputSentences.get(j), writer);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(ConcurrentEngine.getMessageWithElapsed("Write sentences time", startTime));
    }

    public void terminate() throws MaltChainedException {
        System.out.println("Start terminate");
    }
}

