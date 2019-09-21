/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.FeatureNode;
import vn.edu.vnu.uet.liblinear.Linear;
import vn.edu.vnu.uet.liblinear.Model;
import vn.edu.vnu.uet.liblinear.Parameter;
import vn.edu.vnu.uet.liblinear.Problem;
import vn.edu.vnu.uet.liblinear.SolverType;
import vn.edu.vnu.uet.nlp.segmenter.Dictionary;
import vn.edu.vnu.uet.nlp.segmenter.FeatureExtractor;
import vn.edu.vnu.uet.nlp.segmenter.FeatureMap;
import vn.edu.vnu.uet.nlp.segmenter.RareNames;
import vn.edu.vnu.uet.nlp.segmenter.SegmentFeature;
import vn.edu.vnu.uet.nlp.segmenter.SyllabelFeature;
import vn.edu.vnu.uet.nlp.segmenter.SyllableType;
import vn.edu.vnu.uet.nlp.segmenter.measurement.F1Score;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

public class SegmentationSystem {
    private static double r = 0.33;
    private Problem problem = new Problem();
    private Parameter parameter = new Parameter(SolverType.L2R_LR, 1.0, 0.01);
    private Model model;
    private FeatureExtractor fe;
    private String pathToSave = "models";
    private int n;
    private List<SyllabelFeature> segmentList;
    private int N1 = 0;
    private int N2 = 0;
    private int N3 = 0;
    private double[] confidences;

    public SegmentationSystem(FeatureExtractor _fe, String pathToSave) {
        this.model = new Model();
        this.fe = _fe;
        this.pathToSave = pathToSave;
        File file = new File(pathToSave);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.n = this.fe.getFeatureMap().getSize();
    }

    public SegmentationSystem(String folderpath) throws ClassNotFoundException, IOException {
        this.load(folderpath);
    }

    private void setProblem() {
        int numSamples = this.fe.getNumSamples();
        FeatureNode[][] x = new FeatureNode[numSamples][];
        double[] y = new double[numSamples];
        int sampleNo = 0;
        for (int s = 0; s < this.fe.getNumSents(); ++s) {
            for (int i = 0; i < this.fe.getSegmentList().get(s).size(); ++i) {
                y[sampleNo] = this.fe.getSegmentList().get(s).get(i).getLabel();
                SortedSet<Integer> featSet = this.fe.getSegmentList().get(s).get(i).getFeatset();
                x[sampleNo] = new FeatureNode[featSet.size()];
                int cnt = 0;
                for (Integer t : featSet) {
                    x[sampleNo][cnt] = new FeatureNode(t + 1, 1.0);
                    ++cnt;
                }
                featSet.clear();
                ++sampleNo;
            }
        }
        this.problem.l = numSamples;
        this.problem.n = this.n;
        this.problem.y = y;
        this.problem.x = x;
        this.problem.bias = -1.0;
    }

    public void train() {
        OldLogging.info("saving feature map");
        this.saveMap();
        OldLogging.info("clear the map to free memory");
        this.fe.clearMap();
        OldLogging.info("setting up the problem");
        this.setProblem();
        OldLogging.info("start training");
        this.model = Linear.train(this.problem, this.parameter);
        OldLogging.info("finish training");
        OldLogging.info("saving model");
        this.saveModel();
        OldLogging.info("finish.");
    }

    public F1Score test(List<String> sentences) throws IOException {
        int sentCnt = 0;
        this.N1 = 0;
        this.N2 = 0;
        this.N3 = 0;
        try {
            File fol = new File("log");
            fol.mkdir();
        }
        catch (Exception fol) {
            // empty catch block
        }
        String logName = "log/log_test_" + new Date() + ".txt";
        BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile(logName);
        int sentID = 1;
        for (String sentence : sentences) {
            if (this.testSentence(sentence = Normalizer.normalize(sentence, Normalizer.Form.NFC))) {
                ++sentCnt;
            } else {
                bw.write("\n-----Sent " + sentID + "-----\n" + sentence + "\n" + this.segment(sentence) + "\n\n");
                bw.flush();
            }
            ++sentID;
        }
        bw.close();
        F1Score result = new F1Score(this.N1, this.N2, this.N3);
        double pre = result.getPrecision();
        double rec = result.getRecall();
        double f_measure = result.getF1Score();
        OldLogging.info("\nNumber of words recognized by the system:\t\t\t\tN1 = " + this.N1 + "\nNumber of words in reality appearing in the corpus:\t\tN2 = " + this.N2 + "\nNumber of words that are correctly recognized by the system:\tN3 = " + this.N3 + "\n");
        OldLogging.info("Precision\t\tP = N3/N1\t\t=\t" + pre + "%");
        OldLogging.info("Recall\t\tR = N3/N2\t\t=\t" + rec + "%");
        OldLogging.info("\nF-Measure\t\tF = (2*P*R)/(P+R)\t=\t" + f_measure + "%\n");
        OldLogging.info("\nNumber of sentences:\t" + sentences.size());
        OldLogging.info("Sentences right:\t\t" + sentCnt);
        OldLogging.info("\nSentences right accuracy:\t" + (double)sentCnt / (double)sentences.size() * 100.0 + "%");
        OldLogging.info("\nLogged wrong predictions to " + logName);
        return result;
    }

    private boolean testSentence(String sentence) {
        boolean sentCheck = true;
        this.fe.clearList();
        this.segmentList = new ArrayList<SyllabelFeature>();
        List<SyllabelFeature> sylList = this.fe.extract(sentence, 2);
        if (this.fe.getSegmentList().isEmpty()) {
            return true;
        }
        if (this.fe.getSegmentList().get(0).isEmpty()) {
            if (sylList.size() == 5) {
                ++this.N1;
                ++this.N2;
                ++this.N3;
                return true;
            }
            return true;
        }
        for (int i = 2; i < sylList.size() - 2; ++i) {
            this.segmentList.add(sylList.get(i));
        }
        sylList.clear();
        int size = this.segmentList.size() - 1;
        double[] reality = new double[size];
        double[] predictions = new double[size];
        this.confidences = new double[size];
        for (int i = 0; i < size; ++i) {
            reality[i] = this.segmentList.get(i).getLabel();
            this.confidences[i] = Double.MIN_VALUE;
        }
        this.setProblem();
        this.process(predictions, size, 2);
        boolean previousSpaceMatch = true;
        for (int i = 0; i < size; ++i) {
            if (reality[i] == 0.0) {
                ++this.N2;
            }
            if (predictions[i] == 0.0) {
                ++this.N1;
                if (reality[i] == 0.0) {
                    if (previousSpaceMatch) {
                        ++this.N3;
                    }
                    previousSpaceMatch = true;
                }
            }
            if (predictions[i] == reality[i]) continue;
            sentCheck = false;
            previousSpaceMatch = false;
        }
        ++this.N1;
        ++this.N2;
        if (previousSpaceMatch) {
            ++this.N3;
        }
        return sentCheck;
    }

    public String segment(String sentence) {
        this.fe.clearList();
        this.segmentList = new ArrayList<SyllabelFeature>();
        List<SyllabelFeature> sylList = this.fe.extract(sentence, 2);
        if (this.fe.getSegmentList().isEmpty()) {
            return "";
        }
        if (this.fe.getSegmentList().get(0).isEmpty()) {
            if (sylList.size() == 5) {
                return sylList.get(2).getSyllabel();
            }
            return "";
        }
        for (int i = 2; i < sylList.size() - 2; ++i) {
            this.segmentList.add(sylList.get(i));
        }
        sylList.clear();
        int size = this.segmentList.size() - 1;
        double[] predictions = new double[size];
        this.confidences = new double[size];
        for (int i = 0; i < size; ++i) {
            this.confidences[i] = Double.MIN_VALUE;
        }
        this.setProblem();
        this.process(predictions, size, 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append(this.segmentList.get(i).getSyllabel());
            if (predictions[i] == 0.0) {
                sb.append(" ");
                continue;
            }
            sb.append("_");
        }
        sb.append(this.segmentList.get(size).getSyllabel());
        this.delProblem();
        return sb.toString().trim();
    }

    private void process(double[] predictions, int size, int mode) {
        this.longestMatching(predictions, size, mode);
        this.logisticRegression(predictions, size, mode);
        this.postProcessing(predictions, size, mode);
    }

    private void longestMatching(double[] predictions, int size, int mode) {
        block0 : for (int i = 0; i < size; ++i) {
            if (this.segmentList.get(i).getType() == SyllableType.OTHER) continue;
            for (int n = 6; n >= 2; --n) {
                int k;
                StringBuilder sb = new StringBuilder();
                boolean hasUpper = false;
                boolean hasLower = false;
                int j = i;
                for (j = i; j < i + n && j < this.segmentList.size() && this.segmentList.get(j).getType() != SyllableType.OTHER; ++j) {
                    if (mode != 2 && (this.segmentList.get(j).getType() == SyllableType.UPPER || this.segmentList.get(j).getType() == SyllableType.ALLUPPER)) {
                        hasUpper = true;
                    }
                    if (this.segmentList.get(j).getType() == SyllableType.LOWER) {
                        hasLower = true;
                    }
                    sb.append(" " + this.segmentList.get(j).getSyllabel());
                }
                if (j != i + n) continue;
                String word = sb.toString();
                if (n > 2 && hasLower && Dictionary.inVNDict(word)) {
                    for (k = i; k < j - 1; ++k) {
                        predictions[k] = 1.0;
                    }
                    i = j - 1;
                    continue block0;
                }
                if (mode == 2 || !hasUpper || !RareNames.isRareName(word)) continue;
                for (k = i; k < j - 1; ++k) {
                    predictions[k] = 1.0;
                }
                i = j - 1;
                continue block0;
            }
        }
    }

    private void ruleForProperName(double[] predictions, int size, int mode) {
        int i;
        double[] temp = new double[size];
        for (i = 0; i < size; ++i) {
            temp[i] = predictions[i];
        }
        for (i = 0; i < size; ++i) {
            if (temp[i] == 1.0 || i != 0 && temp[i - 1] == 1.0 || i != size - 1 && temp[i + 1] == 1.0 || this.segmentList.get(i).getType() != SyllableType.UPPER || this.segmentList.get(i + 1).getType() != SyllableType.UPPER) continue;
            predictions[i] = 1.0;
        }
    }

    private void logisticRegression(double[] predictions, int size, int mode) {
        int i;
        double[] temp = new double[size];
        for (i = 0; i < size; ++i) {
            temp[i] = predictions[i];
        }
        for (i = 0; i < size; ++i) {
            if (temp[i] == 1.0 || i != 0 && temp[i - 1] == 1.0 || i != size - 1 && temp[i + 1] == 1.0) continue;
            predictions[i] = this.predict(this.problem.x[i], i, mode);
        }
    }

    private double predict(Feature[] featSet, int sampleNo, int mode) {
        double[] dec_values = new double[this.model.getNrClass()];
        double result = Linear.predict(this.model, featSet, dec_values);
        this.confidences[sampleNo] = dec_values[0];
        return result;
    }

    private void postProcessing(double[] predictions, int size, int mode) {
        if (size < 2) {
            return;
        }
        for (int i = 0; i < size - 1; ++i) {
            double sigm = this.sigmoid(this.confidences[i]);
            SyllabelFeature preSyl = i > 0 ? this.segmentList.get(i - 1) : null;
            SyllabelFeature thisSyl = this.segmentList.get(i);
            SyllabelFeature nextSyl = this.segmentList.get(i + 1);
            if ((i == 0 || predictions[i - 1] == 0.0) && predictions[i + 1] == 0.0) {
                if (!(Math.abs(sigm - 0.5) < r)) continue;
                String thisOne = thisSyl.getSyllabel();
                String nextOne = nextSyl.getSyllabel();
                if ((preSyl != null || nextSyl.getType() != SyllableType.LOWER) && (thisSyl.getType() != SyllableType.LOWER && thisSyl.getType() != SyllableType.UPPER || nextSyl.getType() != SyllableType.LOWER)) continue;
                String word1 = thisOne + " " + nextOne;
                if (Dictionary.inVNDict(word1)) {
                    predictions[i] = 1.0;
                }
                if (Dictionary.inVNDict(word1)) continue;
                predictions[i] = 0.0;
                continue;
            }
            if (predictions[i] != 1.0 || i != 0 && predictions[i - 1] != 0.0 || predictions[i + 1] != 1.0 || i != size - 2 && predictions[i + 2] != 0.0) continue;
            int j = i;
            boolean flag = false;
            for (j = i; j < i + 3; ++j) {
                if (j == 0 && (this.segmentList.get(j).getType() == SyllableType.LOWER || this.segmentList.get(j).getType() == SyllableType.UPPER)) continue;
                if (this.segmentList.get(j).getType() == SyllableType.LOWER) {
                    flag = true;
                }
                if (this.segmentList.get(j).getType() != SyllableType.LOWER && this.segmentList.get(j).getType() != SyllableType.UPPER) break;
            }
            if (j != i + 3 || !flag) continue;
            String word = this.segmentList.get(i).getSyllabel() + " " + this.segmentList.get(i + 1).getSyllabel() + " " + this.segmentList.get(i + 2).getSyllabel();
            if (!Dictionary.inVNDict(word) && !RareNames.isRareName(word)) {
                String leftWord = this.segmentList.get(i).getSyllabel() + " " + this.segmentList.get(i + 1).getSyllabel();
                String rightWord = this.segmentList.get(i + 1).getSyllabel() + " " + this.segmentList.get(i + 2).getSyllabel();
                if (Dictionary.inVNDict(leftWord) && !Dictionary.inVNDict(rightWord)) {
                    predictions[i + 1] = 0.0;
                }
                if (Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {
                    predictions[i] = 0.0;
                }
                if (Dictionary.inVNDict(rightWord) && Dictionary.inVNDict(leftWord) && this.confidences[i] * this.confidences[i + 1] > 0.0) {
                    if (Math.abs(this.confidences[i]) < Math.abs(this.confidences[i + 1])) {
                        predictions[i] = 0.0;
                    } else {
                        predictions[i + 1] = 0.0;
                    }
                }
                if (!Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {
                    if (this.segmentList.get(i).getType() == SyllableType.LOWER || this.segmentList.get(i + 1).getType() == SyllableType.LOWER) {
                        predictions[i] = 0.0;
                    }
                    if (this.segmentList.get(i + 2).getType() == SyllableType.LOWER || this.segmentList.get(i + 1).getType() == SyllableType.LOWER) {
                        predictions[i + 1] = 0.0;
                    }
                }
            }
            i += 2;
        }
    }

    private void delProblem() {
        this.problem = new Problem();
        this.fe.clearList();
        if (this.segmentList != null) {
            this.segmentList.clear();
        }
    }

    private void saveMap() {
        String mapFile = this.pathToSave + File.separator + "features";
        this.fe.saveMap(mapFile);
    }

    private void saveModel() {
        File modelFile = new File(this.pathToSave + File.separator + "model");
        try {
            this.model.save(modelFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load(String path) throws ClassNotFoundException, IOException {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String modelPath = path + File.separator + "model";
        String featMapPath = path + File.separator + "features";
        File modelFile = new File(modelPath);
        this.model = Model.load(modelFile);
        this.fe = new FeatureExtractor(featMapPath);
        this.pathToSave = path;
        this.n = this.fe.getFeatureMap().getSize();
    }

    public FeatureExtractor getFeatureExactor() {
        return this.fe;
    }

    private double sigmoid(double d) {
        return 1.0 / (1.0 + Math.exp(-d));
    }

    public void setR(double r) {
        if (r < 0.0 || r > 0.5) {
            return;
        }
        SegmentationSystem.r = r;
    }
}

