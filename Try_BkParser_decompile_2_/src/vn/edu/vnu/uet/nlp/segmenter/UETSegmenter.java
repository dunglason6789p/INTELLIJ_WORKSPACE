/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import vn.edu.vnu.uet.nlp.segmenter.SegmentationSystem;
import vn.edu.vnu.uet.nlp.tokenizer.Tokenizer;

public class UETSegmenter {
    private SegmentationSystem machine = null;

    public UETSegmenter() {
        this("models");
    }

    public UETSegmenter(String modelpath) {
        if (this.machine == null) {
            try {
                this.machine = new SegmentationSystem(modelpath);
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String segmentTokenizedText(String str) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> tokens = new ArrayList<String>();
        List<Object> sentences = new ArrayList();
        tokens.addAll(Arrays.asList(str.split("\\s+")));
        sentences = Tokenizer.joinSentences(tokens);
        for (String sentence : sentences) {
            sb.append(this.machine.segment(sentence));
            sb.append(" ");
        }
        tokens.clear();
        sentences.clear();
        return sb.toString().trim();
    }

    public String segment(String str) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> tokens = new ArrayList();
        List<Object> sentences = new ArrayList();
        try {
            tokens = Tokenizer.tokenize(str);
            sentences = Tokenizer.joinSentences(tokens);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (String sentence : sentences) {
            sb.append(this.machine.segment(sentence));
            sb.append(" ");
        }
        tokens.clear();
        sentences.clear();
        return sb.toString().trim();
    }

    public List<String> segmentSentences(String corpus) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> tokens = new ArrayList();
        List<Object> sentences = new ArrayList();
        try {
            tokens = Tokenizer.tokenize(corpus);
            sentences = Tokenizer.joinSentences(tokens);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (String sentence : sentences) {
            result.add(this.machine.segment(sentence));
        }
        tokens.clear();
        sentences.clear();
        return result;
    }

    public void setR(double r) {
        this.machine.setR(r);
    }
}

