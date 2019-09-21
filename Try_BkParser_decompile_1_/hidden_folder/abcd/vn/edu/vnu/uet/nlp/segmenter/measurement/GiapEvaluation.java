/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter.measurement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiapEvaluation {
    private static final String TEXT_DIR = "independent_test";
    private static final String SEG_MODEL_DIR = "independent_test/seg_model";
    private static final String SEG_HUMAN_DIR = "independent_test/seg_human";
    private static int totalHumanCount;
    private static int totalModelCount;
    private static int totalMatchCount;
    private static int totalLines;
    private static int humanCount;
    private static int modelCount;
    private static int matchCount;

    public static void main(String[] args) {
        long initTime = GiapEvaluation.init();
        System.out.println("Initialize time: " + initTime);
        System.out.println("--------------------\n");
        System.out.println("File\tLines\tHumanCount\tSystemCount\tMatchCount\tPre\tRecall\tF1");
        File textDir = new File(TEXT_DIR);
        for (String s : textDir.list()) {
            String fileName;
            try {
                fileName = s.substring(0, s.lastIndexOf(46));
            }
            catch (Exception e) {
                continue;
            }
            GiapEvaluation.process(fileName);
        }
        double precision = (double)totalMatchCount / (double)totalModelCount;
        double recall = (double)totalMatchCount / (double)totalHumanCount;
        double f1 = 2.0 * precision * recall / (precision + recall);
        System.out.println("Total\t" + totalLines + "\t" + totalHumanCount + "\t" + totalModelCount + "\t" + totalMatchCount + "\t" + precision + "\t" + recall + "\t" + f1);
    }

    private static long init() {
        long start = System.currentTimeMillis();
        totalMatchCount = 0;
        totalModelCount = 0;
        totalHumanCount = 0;
        totalLines = 0;
        return System.currentTimeMillis() - start;
    }

    private static void process(String fileName) {
        try {
            String line;
            String textPath = "independent_test/seg_model/" + fileName + ".seg";
            BufferedReader reader = new BufferedReader(new FileReader(textPath));
            int lineCount = 0;
            ArrayList<List<String>> segmentRes = new ArrayList<List<String>>();
            while ((line = reader.readLine()) != null) {
                segmentRes.add(Arrays.asList(line.split("\\s+")));
                ++lineCount;
            }
            reader.close();
            GiapEvaluation.evaluate(segmentRes, fileName);
            double precision = (double)matchCount / (double)modelCount;
            double recall = (double)matchCount / (double)humanCount;
            double f1 = 2.0 * precision * recall / (precision + recall);
            System.out.println(fileName + "\t" + lineCount + "\t" + humanCount + "\t" + modelCount + "\t" + matchCount + "\t" + precision + "\t" + recall + "\t" + f1);
            totalLines += lineCount;
            totalHumanCount += humanCount;
            totalModelCount += modelCount;
            totalMatchCount += matchCount;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveSegment(List<List<String>> seg, String fileName) {
        try {
            StringBuilder res = new StringBuilder();
            for (List<String> line : seg) {
                StringBuilder sb = new StringBuilder();
                for (String word : line) {
                    sb.append(word + " ");
                }
                res.append(sb.toString().trim() + "\n");
            }
            String outFilePath = "independent_test/seg_model/" + fileName + ".seg";
            FileWriter writer = new FileWriter(outFilePath);
            writer.write(res.toString());
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void evaluate(List<List<String>> modelSeg, String fileName) {
        try {
            matchCount = 0;
            modelCount = 0;
            humanCount = 0;
            String humanSeg = "independent_test/seg_human/" + fileName + ".seg";
            BufferedReader reader = new BufferedReader(new FileReader(humanSeg));
            for (List<String> modelWords : modelSeg) {
                String line = reader.readLine();
                if (line == null) break;
                String[] humanWords = line.split("\\s+");
                humanCount += humanWords.length;
                modelCount += modelWords.size();
                matchCount += GiapEvaluation.getMatchCount(humanWords, modelWords);
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] calculateIndex(String[] words) {
        int[] indexArr = new int[words.length];
        int index = 0;
        for (int i = 0; i < words.length; ++i) {
            indexArr[i] = index;
            index += words[i].replace("_", "").length();
        }
        return indexArr;
    }

    public static int[] calculateIndex(List<String> words) {
        int[] indexArr = new int[words.size()];
        int index = 0;
        for (int i = 0; i < words.size(); ++i) {
            indexArr[i] = index;
            index += words.get(i).replace("_", "").length();
        }
        return indexArr;
    }

    private static int getMatchCount(String[] humanWords, List<String> modelWords) {
        int[] humanIndex = GiapEvaluation.calculateIndex(humanWords);
        int[] modelIndex = GiapEvaluation.calculateIndex(modelWords);
        int matchCount = 0;
        int j = 0;
        for (int i = 0; i < humanWords.length; ++i) {
            while (j < modelIndex.length - 1 && humanIndex[i] > modelIndex[j]) {
                ++j;
            }
            if (humanIndex[i] != modelIndex[j] || !humanWords[i].equals(modelWords.get(j))) continue;
            ++matchCount;
        }
        return matchCount;
    }
}

