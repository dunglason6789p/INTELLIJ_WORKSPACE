/*
 * Decompiled with CFR 0.146.
 */
package com.github.jcrfsuite;

import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Trainer;

public class CrfTrainer {
    private static final String DEFAULT_ALGORITHM = "lbfgs";
    private static final String DEFAULT_GRAPHICAL_MODEL_TYPE = "crf1d";
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static Pair<List<ItemSequence>, List<StringList>> loadTrainingInstances(String fileName, String encoding) throws IOException {
        ArrayList<ItemSequence> xseqs = new ArrayList<ItemSequence>();
        ArrayList<StringList> yseqs = new ArrayList<StringList>();
        ItemSequence xseq = new ItemSequence();
        StringList yseq = new StringList();
        try (BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(fileName), encoding));){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    String[] fields = line.split("\t");
                    yseq.add(fields[0]);
                    Item item = new Item();
                    for (int i = 1; i < fields.length; ++i) {
                        String field = fields[i];
                        String[] colonSplit = field.split(":", 2);
                        if (colonSplit.length == 2) {
                            try {
                                double val = Double.valueOf(colonSplit[1]);
                                item.add(new Attribute(colonSplit[0], val));
                            }
                            catch (NumberFormatException e) {
                                item.add(new Attribute(field));
                            }
                            continue;
                        }
                        item.add(new Attribute(field));
                    }
                    xseq.add(item);
                    continue;
                }
                xseqs.add(xseq);
                yseqs.add(yseq);
                xseq = new ItemSequence();
                yseq = new StringList();
            }
            if (!xseq.isEmpty()) {
                xseqs.add(xseq);
                yseqs.add(yseq);
            }
        }
        return new Pair<List<ItemSequence>, List<StringList>>(xseqs, yseqs);
    }

    public static void train(String fileName, String modelFile) throws IOException {
        CrfTrainer.train(fileName, modelFile, DEFAULT_ALGORITHM, DEFAULT_GRAPHICAL_MODEL_TYPE, DEFAULT_ENCODING, new Pair[0]);
    }

    public static void train(String fileName, String modelFile, String encoding) throws IOException {
        CrfTrainer.train(fileName, modelFile, DEFAULT_ALGORITHM, DEFAULT_GRAPHICAL_MODEL_TYPE, encoding, new Pair[0]);
    }

    public static void train(String fileName, String modelFile, String algorithm, String graphicalModelType, String encoding, Pair<String, String> ... parameters) throws IOException {
        Pair<List<ItemSequence>, List<StringList>> trainingData = CrfTrainer.loadTrainingInstances(fileName, encoding);
        List xseqs = (List)trainingData.first;
        List yseqs = (List)trainingData.second;
        CrfTrainer.train(xseqs, yseqs, modelFile, algorithm, graphicalModelType, parameters);
    }

    public static void train(List<ItemSequence> xseqs, List<StringList> yseqs, String modelFile) {
        CrfTrainer.train(xseqs, yseqs, modelFile, DEFAULT_ALGORITHM, DEFAULT_GRAPHICAL_MODEL_TYPE, new Pair[0]);
    }

    public static void train(List<ItemSequence> xseqs, List<StringList> yseqs, String modelFile, String algorithm, String graphicalModelType, Pair<String, String> ... parameters) {
        Trainer trainer = new Trainer();
        int n = xseqs.size();
        for (int i = 0; i < n; ++i) {
            trainer.append(xseqs.get(i), yseqs.get(i), 0);
        }
        trainer.select(algorithm, graphicalModelType);
        if (parameters != null) {
            for (Pair<String, String> attribute : parameters) {
                trainer.set((String)attribute.first, (String)attribute.second);
            }
        }
        StringList params = trainer.params();
        int i = 0;
        while ((long)i < params.size()) {
            String param = params.get(i);
            System.out.printf("%s, %s, %s\n", param, trainer.get(param), trainer.help(param));
            ++i;
        }
        trainer.train(modelFile, -1);
    }

    static {
        try {
            CrfSuiteLoader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

