/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import vn.edu.vnu.uet.nlp.segmenter.FamilyName;
import vn.edu.vnu.uet.nlp.segmenter.FeatureMap;
import vn.edu.vnu.uet.nlp.segmenter.SegmentFeature;
import vn.edu.vnu.uet.nlp.segmenter.SyllabelFeature;
import vn.edu.vnu.uet.nlp.segmenter.SyllableList;
import vn.edu.vnu.uet.nlp.segmenter.SyllableType;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

public class FeatureExtractor {
    private FeatureMap featureMap;
    private List<List<SegmentFeature>> listOfSegmentFeatureLists = new ArrayList<List<SegmentFeature>>();
    private static Map<String, String> normalizationMap = new HashMap<String, String>();
    private static Set<String> normalizationSet;

    public FeatureExtractor() {
        this.featureMap = new FeatureMap();
    }

    public FeatureExtractor(String featMapPath) throws ClassNotFoundException, IOException {
        this.featureMap = new FeatureMap();
        this.loadMap(featMapPath);
    }

    public void extract(List<String> sentences, int mode) {
        for (int i = 0; i < sentences.size(); ++i) {
            this.extract(sentences.get(i), mode);
            if (i % 1000 != 999 && i != sentences.size() - 1) continue;
            OldLogging.info(i + 1 + " sentences extracted to features");
        }
    }

    public List<SyllabelFeature> extract(String sentence, int mode) {
        List<SyllabelFeature> sylList = FeatureExtractor.convertToFeatureOfSyllabel(sentence, mode);
        int length = sylList.size();
        if (length == 0) {
            return null;
        }
        TreeSet<Integer> indexSet = new TreeSet<Integer>();
        ArrayList<SegmentFeature> segfeats = new ArrayList<SegmentFeature>();
        for (int i = 2; i < length - 2 - 1; ++i) {
            int j;
            String featureName;
            for (j = i - 2; j <= i + 2; ++j) {
                featureName = j - i + "|" + sylList.get(j).getSyllabel().toLowerCase();
                indexSet.add(this.featureMap.getIndex(featureName, mode));
                if (sylList.get(j).getType() == SyllableType.LOWER) continue;
                featureName = j - i + ":" + (Object)((Object)sylList.get(j).getType());
                indexSet.add(this.featureMap.getIndex(featureName, mode));
            }
            for (j = i - 2; j < i + 2; ++j) {
                featureName = j - i + "||" + sylList.get(j).getSyllabel().toLowerCase() + " " + sylList.get(j + 1).getSyllabel().toLowerCase();
                indexSet.add(this.featureMap.getIndex(featureName, mode));
                if (sylList.get(j).getType() == SyllableType.LOWER) continue;
                featureName = j - i + "::" + (Object)((Object)sylList.get(j).getType()) + " " + (Object)((Object)sylList.get(j + 1).getType());
                indexSet.add(this.featureMap.getIndex(featureName, mode));
            }
            for (j = i - 2; j < i + 2 - 1; ++j) {
                if (sylList.get(j).getType() == SyllableType.LOWER) continue;
                featureName = j - i + ":::" + (Object)((Object)sylList.get(j).getType()) + " " + (Object)((Object)sylList.get(j + 1).getType()) + " " + (Object)((Object)sylList.get(j + 2).getType());
                indexSet.add(this.featureMap.getIndex(featureName, mode));
            }
            String thisSyl = sylList.get(i).getSyllabel().toLowerCase();
            String nextSyl = sylList.get(i + 1).getSyllabel().toLowerCase();
            if (sylList.get(i).getType() == SyllableType.UPPER && sylList.get(i + 1).getType() == SyllableType.UPPER) {
                if (SyllableList.isVNsyl(thisSyl) && !SyllableList.isVNsyl(nextSyl) || SyllableList.isVNsyl(nextSyl) && !SyllableList.isVNsyl(thisSyl)) {
                    featureName = "(0:vi&&1:en)||(0:en&&1:vi)";
                    indexSet.add(this.featureMap.getIndex(featureName, mode));
                }
                if (FamilyName.isVNFamilyName(thisSyl)) {
                    featureName = "0.isVNFamilyName";
                    indexSet.add(this.featureMap.getIndex(featureName, mode));
                }
            }
            if (sylList.get(i).getType() == SyllableType.LOWER && sylList.get(i + 1).getType() == SyllableType.LOWER && thisSyl.equalsIgnoreCase(nextSyl)) {
                featureName = "0&&1.reduplicativeword";
                indexSet.add(this.featureMap.getIndex(featureName, mode));
            }
            if (indexSet.size() > 0) {
                segfeats.add(new SegmentFeature(sylList.get(i).getLabel(), indexSet));
            }
            featureName = "";
            indexSet.clear();
        }
        this.listOfSegmentFeatureLists.add(segfeats);
        return sylList;
    }

    public static List<SyllabelFeature> convertToFeatureOfSyllabel(String sentence, int mode) {
        String sent = sentence.trim();
        if (sent.equals(" ") || sent.isEmpty()) {
            return new ArrayList<SyllabelFeature>();
        }
        for (int i = 0; i < 2; ++i) {
            sent = "<s> " + sent + " " + "</s>";
        }
        return FeatureExtractor.token(sent, mode);
    }

    public static List<SyllabelFeature> token(String sent, int mode) {
        ArrayList<SyllabelFeature> list = new ArrayList<SyllabelFeature>();
        String[] tokens = sent.split("\\s+");
        if (mode == 0 || mode == 2) {
            for (String token : tokens) {
                String[] tmp;
                if (token.contains("_")) {
                    tmp = token.split("_");
                    for (int i = 0; i < tmp.length - 1; ++i) {
                        String tmp_i = FeatureExtractor.normalize(tmp[i]);
                        list.add(new SyllabelFeature(tmp_i, FeatureExtractor.typeOf(tmp_i), 1));
                    }
                    try {
                        String tmp_last = FeatureExtractor.normalize(tmp[tmp.length - 1]);
                        list.add(new SyllabelFeature(tmp_last, FeatureExtractor.typeOf(tmp_last), 0));
                    }
                    catch (Exception tmp_last) {}
                    continue;
                }
                tmp = FeatureExtractor.normalize(token);
                list.add(new SyllabelFeature((String)tmp, FeatureExtractor.typeOf((String)tmp), 0));
            }
        } else {
            for (String token : tokens) {
                String tmp = FeatureExtractor.normalize(token);
                list.add(new SyllabelFeature(tmp, FeatureExtractor.typeOf(tmp), 0));
            }
        }
        return list;
    }

    public static String normalize(String token) {
        if (SyllableList.isVNsyl(token)) {
            return token;
        }
        for (String wrongTyping : normalizationSet) {
            if (!token.contains(wrongTyping)) continue;
            token = token.replace(wrongTyping, normalizationMap.get(wrongTyping));
            break;
        }
        return token;
    }

    public static SyllableType typeOf(String syllabel) {
        if (syllabel.equals("<s>")) {
            return SyllableType.BOS;
        }
        if (syllabel.equals("</s>")) {
            return SyllableType.EOS;
        }
        boolean upper = false;
        boolean lower = false;
        boolean num = false;
        boolean other = false;
        if (syllabel.matches("\\p{Upper}\\p{L}*\\.") || syllabel.matches("\\p{Upper}\\p{L}*-\\w+")) {
            return SyllableType.UPPER;
        }
        for (int i = 0; i < syllabel.length(); ++i) {
            char character = syllabel.charAt(i);
            if (!Character.isLetterOrDigit(character)) {
                if (character == '.' || character == ',') {
                    other = true;
                    continue;
                }
                return SyllableType.OTHER;
            }
            if (Character.isDigit(character)) {
                num = true;
                continue;
            }
            if (Character.isLowerCase(character)) {
                lower = true;
                continue;
            }
            upper = true;
        }
        if (num) {
            if (syllabel.matches("[-+]?\\d+([\\.,]\\d+)*")) {
                return SyllableType.NUMBER;
            }
            return SyllableType.OTHER;
        }
        if (other) {
            return SyllableType.OTHER;
        }
        if (lower) {
            if (upper) {
                return SyllableType.UPPER;
            }
            return SyllableType.LOWER;
        }
        if (upper) {
            return SyllableType.ALLUPPER;
        }
        return SyllableType.OTHER;
    }

    protected FeatureMap getFeatureMap() {
        return this.featureMap;
    }

    protected void saveMap(String path) {
        try {
            this.featureMap.save(path);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadMap(String path) throws ClassNotFoundException, IOException {
        this.featureMap.load(path);
    }

    protected void clearList() {
        this.listOfSegmentFeatureLists.clear();
    }

    protected void clearMap() {
        this.featureMap.clear();
    }

    protected int getNumSents() {
        return this.listOfSegmentFeatureLists.size();
    }

    protected int getNumSamples() {
        int cnt = 0;
        for (int i = 0; i < this.listOfSegmentFeatureLists.size(); ++i) {
            cnt += this.listOfSegmentFeatureLists.get(i).size();
        }
        return cnt;
    }

    protected List<List<SegmentFeature>> getSegmentList() {
        return this.listOfSegmentFeatureLists;
    }

    public int getFeatureMapSize() {
        return this.featureMap.getSize();
    }

    static {
        normalizationMap.put("\u00f2a", "o\u00e0");
        normalizationMap.put("\u00f3a", "o\u00e1");
        normalizationMap.put("\u1ecfa", "o\u1ea3");
        normalizationMap.put("\u00f5a", "o\u00e3");
        normalizationMap.put("\u1ecda", "o\u1ea1");
        normalizationMap.put("\u00f2e", "o\u00e8");
        normalizationMap.put("\u00f3e", "o\u00e9");
        normalizationMap.put("\u1ecfe", "o\u1ebb");
        normalizationMap.put("\u00f5e", "o\u1ebd");
        normalizationMap.put("\u1ecde", "o\u1eb9");
        normalizationMap.put("\u00f9y", "u\u1ef3");
        normalizationMap.put("\u00fay", "u\u00fd");
        normalizationMap.put("\u1ee7y", "u\u1ef7");
        normalizationMap.put("\u0169y", "u\u1ef9");
        normalizationMap.put("\u1ee5y", "u\u1ef5");
        normalizationMap.put("\u1ee6y", "U\u1ef7");
        normalizationSet = normalizationMap.keySet();
    }
}

