/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Predict {
    private static boolean flag_predict_probability = false;
    private static final Pattern COLON = Pattern.compile(":");

    static void doPredict(BufferedReader reader, Writer writer, Model model) throws IOException {
        int correct = 0;
        int total = 0;
        int nr_class = model.getNrClass();
        double[] prob_estimates = null;
        int nr_feature = model.getNrFeature();
        int n = model.bias >= 0.0 ? nr_feature + 1 : nr_feature;
        Formatter out = new Formatter(writer);
        if (flag_predict_probability) {
            if (!model.isProbabilityModel()) {
                throw new IllegalArgumentException("probability output is only supported for logistic regression");
            }
            int[] labels = model.getLabels();
            prob_estimates = new double[nr_class];
            Linear.printf(out, "labels", new Object[0]);
            for (int j = 0; j < nr_class; ++j) {
                Linear.printf(out, " %d", labels[j]);
            }
            Linear.printf(out, "\n", new Object[0]);
        }
        String line = null;
        while ((line = reader.readLine()) != null) {
            int target_label;
            int predict_label;
            ArrayList<FeatureNode> x = new ArrayList<FeatureNode>();
            StringTokenizer st = new StringTokenizer(line, " \t\n");
            try {
                String label = st.nextToken();
                target_label = Linear.atoi(label);
            }
            catch (NoSuchElementException e) {
                throw new RuntimeException("Wrong input format at line " + (total + 1), e);
            }
            while (st.hasMoreTokens()) {
                String[] split = COLON.split(st.nextToken(), 2);
                if (split == null || split.length < 2) {
                    throw new RuntimeException("Wrong input format at line " + (total + 1));
                }
                try {
                    int idx = Linear.atoi(split[0]);
                    double val = Linear.atof(split[1]);
                    if (idx > nr_feature) continue;
                    FeatureNode node = new FeatureNode(idx, val);
                    x.add(node);
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException("Wrong input format at line " + (total + 1), e);
                }
            }
            if (model.bias >= 0.0) {
                FeatureNode node = new FeatureNode(n, model.bias);
                x.add(node);
            }
            FeatureNode[] nodes = new FeatureNode[x.size()];
            nodes = x.toArray(nodes);
            if (flag_predict_probability) {
                assert (prob_estimates != null);
                predict_label = Linear.predictProbability(model, nodes, prob_estimates);
                Linear.printf(out, "%d", predict_label);
                for (int j = 0; j < model.nr_class; ++j) {
                    Linear.printf(out, " %g", prob_estimates[j]);
                }
                Linear.printf(out, "\n", new Object[0]);
            } else {
                predict_label = Linear.predict(model, nodes);
                Linear.printf(out, "%d\n", predict_label);
            }
            if (predict_label == target_label) {
                ++correct;
            }
            ++total;
        }
        System.out.printf("Accuracy = %g%% (%d/%d)%n", (double)correct / (double)total * 100.0, correct, total);
    }

    private static void exit_with_help() {
        System.out.printf("Usage: predict [options] test_file model_file output_file%noptions:%n-b probability_estimates: whether to output probability estimates, 0 or 1 (default 0)%n", new Object[0]);
        System.exit(1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] argv) throws IOException {
        int i;
        block7 : for (i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
            switch (argv[++i - 1].charAt(1)) {
                case 'b': {
                    try {
                        flag_predict_probability = Linear.atoi(argv[i]) != 0;
                    }
                    catch (NumberFormatException e) {
                        Predict.exit_with_help();
                    }
                    continue block7;
                }
                default: {
                    System.err.printf("unknown option: -%d%n", Character.valueOf(argv[i - 1].charAt(1)));
                    Predict.exit_with_help();
                }
            }
        }
        if (i >= argv.length || argv.length <= i + 2) {
            Predict.exit_with_help();
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(argv[i]), Linear.FILE_CHARSET));
            writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(argv[i + 2]), Linear.FILE_CHARSET));
            Model model = Linear.loadModel(new File(argv[i + 1]));
            Predict.doPredict(reader, writer, model);
        }
        catch (Throwable throwable) {
            Linear.closeQuietly(reader);
            Linear.closeQuietly(writer);
            throw throwable;
        }
        Linear.closeQuietly(reader);
        Linear.closeQuietly(writer);
    }
}

