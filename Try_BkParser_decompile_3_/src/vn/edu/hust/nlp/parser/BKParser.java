/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.hust.nlp.parser;

import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import third_party.org.chokkan.crfsuite.ItemSequence;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.preprocess.FeaturesExtraction;
import vn.edu.vnu.uet.nlp.segmenter.UETSegmenter;

public class BKParser {
    private UETSegmenter uetSegmenter;
    private FeaturesExtraction featuresExtraction = new FeaturesExtraction();
    private CrfTagger crfTagger;
    private MaltParserService maltParserService;

    public BKParser(boolean loadUETSegmenter) {
        System.out.println("Loading CRF Tagger...");
        this.crfTagger = new CrfTagger("models/modelPOS.model");
        System.out.println("Loading Malt Parser...");
        try {
            this.maltParserService = new MaltParserService();
            this.maltParserService.initializeParserModel("-c modelDependency -m parse -w models -lfi parser.log");
        }
        catch (MaltChainedException e) {
            e.printStackTrace();
        }
        if (loadUETSegmenter) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
        }
    }

    public BKParser() {
        this(true);
    }

    public List<List<CONLLToken>> tag(String text) {
        if (this.uetSegmenter == null) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
        }
        ArrayList<List<CONLLToken>> taggerResult = new ArrayList<List<CONLLToken>>();
        List<String> sentences = this.segment(text);
        for (String sentence : sentences) {
            List<CONLLToken> sentenceTag = this.tagger(sentence);
            taggerResult.add(sentenceTag);
        }
        return taggerResult;
    }

    public List<List<CONLLToken>> parse(String text) {
        if (this.uetSegmenter == null) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
        }
        ArrayList<List<CONLLToken>> parserResult = new ArrayList<List<CONLLToken>>();
        List<String> sentences = this.segment(text);
        for (String sentence : sentences) {
            List<CONLLToken> sentenceParse = this.parseToCONLLToken(sentence);
            parserResult.add(sentenceParse);
        }
        return parserResult;
    }

    public List<List<CONLLToken>> tag(List<String> sentences, boolean isSegmented) {
        ArrayList<List<CONLLToken>> taggerResult = new ArrayList<List<CONLLToken>>();
        if (!isSegmented) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
            for (String sentence : sentences) {
                String segmentSentence = this.uetSegmenter.segment(sentence);
                List<CONLLToken> sentenceTag = this.tagger(segmentSentence);
                taggerResult.add(sentenceTag);
            }
            return taggerResult;
        }
        for (String sentence : sentences) {
            List<CONLLToken> sentenceTag = this.tagger(sentence);
            taggerResult.add(sentenceTag);
        }
        return taggerResult;
    }

    public List<List<CONLLToken>> parse(List<String> sentences, boolean isSegmented) {
        ArrayList<List<CONLLToken>> parserResult = new ArrayList<List<CONLLToken>>();
        if (!isSegmented) {
            if (this.uetSegmenter == null) {
                System.out.println("Loading UETSegmenter...");
                this.uetSegmenter = new UETSegmenter();
            }
            for (String sentence : sentences) {
                String segmentSentence = this.uetSegmenter.segment(sentence);
                List<CONLLToken> sentenceTag = this.parseToCONLLToken(segmentSentence);
                parserResult.add(sentenceTag);
            }
            return parserResult;
        }
        for (String sentence : sentences) {
            List<CONLLToken> sentenceTag = this.parseToCONLLToken(sentence);
            parserResult.add(sentenceTag);
        }
        return parserResult;
    }

    public List<List<CONLLToken>> tagFile(String filePath) {
        List<List<CONLLToken>> tagResult = new ArrayList<List<CONLLToken>>();
        try {
            String text = new Scanner(new File(filePath)).useDelimiter("\\Z").next();
            tagResult = this.tag(text);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tagResult;
    }

    public List<List<CONLLToken>> parseFile(String filePath) {
        List<List<CONLLToken>> parseResult = new ArrayList<List<CONLLToken>>();
        try {
            String text = new Scanner(new File(filePath)).useDelimiter("\\Z").next();
            parseResult = this.parse(text);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return parseResult;
    }

    public List<String> segment(String unsegmented_text) {
        if (this.uetSegmenter == null) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
        }
        String[] paras = unsegmented_text.split("\n");
        ArrayList<String> result = new ArrayList<String>();
        for (String para : paras) {
            List<String> paraSegmented = this.uetSegmenter.segmentSentences(para);
            result.addAll(paraSegmented);
        }
        return result;
    }

    private List<CONLLToken> tagger(String sentence) {
        String[] words = sentence.split(" ");
        int i = 0;
        ItemSequence itemSequence = this.featuresExtraction.sequence2itemsequence(sentence);
        List<Pair<String, Double>> tagProbs = this.crfTagger.tag(itemSequence);
        ArrayList<CONLLToken> tokenList = new ArrayList<CONLLToken>();
        for (Pair<String, Double> tagProb : tagProbs) {
            String prediction = (String)tagProb.first;
            int index = i + 1;
            String form = words[i];
            String uposTag = prediction;
            String xposTag = prediction;
            if (xposTag.equals("NML") || xposTag.equals("PFN")) {
                uposTag = "PART";
            }
            CONLLToken token = new CONLLToken(String.valueOf(index), form, "_", uposTag, xposTag, "_", "_", "_", "_", "_");
            tokenList.add(token);
            ++i;
        }
        return tokenList;
    }

    public List<CONLLToken> parseToCONLLToken(String sentence) {
        List<CONLLToken> tokenList = this.tagger(sentence);
        int n = tokenList.size();
        String[] tokens = new String[n];
        for (int i = 0; i < n; ++i) {
            CONLLToken token = tokenList.get(i);
            int id = i + 1;
            tokens[i] = id + "\t" + token.getForm() + "\t_\t" + token.getuPOSTag() + "\t" + token.getxPOSTag() + "\t_";
        }
        ArrayList<CONLLToken> parserTokens = new ArrayList<CONLLToken>();
        try {
            String[] depedency = this.maltParserService.parseTokens(tokens);
            for (int i = 0; i < depedency.length; ++i) {
                String[] parts = depedency[i].split("\t");
                CONLLToken token = new CONLLToken(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], "_", "_");
                parserTokens.add(token);
            }
        }
        catch (MaltChainedException e) {
            e.printStackTrace();
        }
        return parserTokens;
    }
}

