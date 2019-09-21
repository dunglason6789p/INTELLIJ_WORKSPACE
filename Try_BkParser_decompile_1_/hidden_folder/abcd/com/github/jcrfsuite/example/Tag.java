/*
 * Decompiled with CFR 0.146.
 */
package com.github.jcrfsuite.example;

import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;

public class Tag {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: " + Tag.class.getCanonicalName() + " <model file> <test file>");
            System.exit(1);
        }
        String modelFile = args[0];
        String testFile = args[1];
        CrfTagger crfTagger = new CrfTagger(modelFile);
        List<List<Pair<String, Double>>> tagProbLists = crfTagger.tag(testFile);
        int total = 0;
        int correct = 0;
        System.out.println("Gold\tPredict\tProbability");
        BufferedReader br = new BufferedReader(new FileReader(testFile));
        for (List<Pair<String, Double>> tagProbs : tagProbLists) {
            for (Pair<String, Double> tagProb : tagProbs) {
                String prediction = (String)tagProb.first;
                Double prob = (Double)tagProb.second;
                String line = br.readLine();
                if (line.length() == 0) {
                    line = br.readLine();
                }
                String gold = line.split("\t")[0];
                System.out.format("%s\t%s\t%.2f\n", gold, prediction, prob);
                ++total;
                if (!gold.equals(prediction)) continue;
                ++correct;
            }
            System.out.println();
        }
        br.close();
        System.out.format("Accuracy = %.2f%%\n", 100.0 * (double)correct / (double)total);
    }
}

