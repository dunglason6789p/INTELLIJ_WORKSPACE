/*
 * Decompiled with CFR 0.146.
 */
package com.github.jcrfsuite;

import com.github.jcrfsuite.CrfTrainer;
import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Tagger;

public class CrfTagger {
    private final Tagger tagger = new Tagger();

    public CrfTagger(String modelFile) {
        this.tagger.open(modelFile);
    }

    public synchronized List<Pair<String, Double>> tag(ItemSequence xseq) {
        ArrayList<Pair<String, Double>> predicted = new ArrayList<Pair<String, Double>>();
        this.tagger.set(xseq);
        StringList labels = this.tagger.viterbi();
        int i = 0;
        while ((long)i < labels.size()) {
            String label = labels.get(i);
            predicted.add(new Pair<String, Double>(label, this.tagger.marginal(label, i)));
            ++i;
        }
        return predicted;
    }

    public List<List<Pair<String, Double>>> tag(String fileName) throws IOException {
        ArrayList<List<Pair<String, Double>>> taggedSentences = new ArrayList<List<Pair<String, Double>>>();
        Pair<List<ItemSequence>, List<StringList>> taggingSequences = CrfTrainer.loadTrainingInstances(fileName, "UTF-8");
        for (ItemSequence xseq : taggingSequences.getFirst()) {
            taggedSentences.add(this.tag(xseq));
        }
        return taggedSentences;
    }

    public List<List<Pair<String, Double>>> tag(String fileName, String encoding) throws IOException {
        ArrayList<List<Pair<String, Double>>> taggedSentences = new ArrayList<List<Pair<String, Double>>>();
        Pair<List<ItemSequence>, List<StringList>> taggingSequences = CrfTrainer.loadTrainingInstances(fileName, encoding);
        for (ItemSequence xseq : taggingSequences.getFirst()) {
            taggedSentences.add(this.tag(xseq));
        }
        return taggedSentences;
    }

    public List<String> getlabels() {
        StringList labels = this.tagger.labels();
        int numLabels = (int)labels.size();
        ArrayList<String> result = new ArrayList<String>(numLabels);
        for (int labelIndex = 0; labelIndex < numLabels; ++labelIndex) {
            result.add(labels.get(labelIndex));
        }
        return result;
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

