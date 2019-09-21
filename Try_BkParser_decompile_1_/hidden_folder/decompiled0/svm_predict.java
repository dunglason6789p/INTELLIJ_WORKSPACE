/*
 * Decompiled with CFR 0.146.
 */
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.StringTokenizer;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

class svm_predict {
    svm_predict() {
    }

    private static double atof(String s) {
        return Double.valueOf(s);
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException {
        String line;
        int correct = 0;
        int total = 0;
        double error = 0.0;
        double sumv = 0.0;
        double sumy = 0.0;
        double sumvv = 0.0;
        double sumyy = 0.0;
        double sumvy = 0.0;
        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;
        if (predict_probability == 1) {
            if (svm_type == 3 || svm_type == 4) {
                System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for (int j = 0; j < nr_class; ++j) {
                    output.writeBytes(" " + labels[j]);
                }
                output.writeBytes("\n");
            }
        }
        while ((line = input.readLine()) != null) {
            double v;
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
            double target = svm_predict.atof(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; ++j) {
                x[j] = new svm_node();
                x[j].index = svm_predict.atoi(st.nextToken());
                x[j].value = svm_predict.atof(st.nextToken());
            }
            if (predict_probability == 1 && (svm_type == 0 || svm_type == 1)) {
                v = svm.svm_predict_probability(model, x, prob_estimates);
                output.writeBytes(v + " ");
                for (int j = 0; j < nr_class; ++j) {
                    output.writeBytes(prob_estimates[j] + " ");
                }
                output.writeBytes("\n");
            } else {
                v = svm.svm_predict(model, x);
                output.writeBytes(v + "\n");
            }
            if (v == target) {
                ++correct;
            }
            error += (v - target) * (v - target);
            sumv += v;
            sumy += target;
            sumvv += v * v;
            sumyy += target * target;
            sumvy += v * target;
            ++total;
        }
        if (svm_type == 3 || svm_type == 4) {
            System.out.print("Mean squared error = " + error / (double)total + " (regression)\n");
            System.out.print("Squared correlation coefficient = " + ((double)total * sumvy - sumv * sumy) * ((double)total * sumvy - sumv * sumy) / (((double)total * sumvv - sumv * sumv) * ((double)total * sumyy - sumy * sumy)) + " (regression)\n");
        } else {
            System.out.print("Accuracy = " + (double)correct / (double)total * 100.0 + "% (" + correct + "/" + total + ") (classification)\n");
        }
    }

    private static void exit_with_help() {
        System.err.print("usage: svm_predict [options] test_file model_file output_file\noptions:\n-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
        System.exit(1);
    }

    public static void main(String[] argv) throws IOException {
        int i;
        int predict_probability = 0;
        block6 : for (i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
            switch (argv[++i - 1].charAt(1)) {
                case 'b': {
                    predict_probability = svm_predict.atoi(argv[i]);
                    continue block6;
                }
                default: {
                    System.err.print("Unknown option: " + argv[i - 1] + "\n");
                    svm_predict.exit_with_help();
                }
            }
        }
        if (i >= argv.length - 2) {
            svm_predict.exit_with_help();
        }
        try {
            BufferedReader input = new BufferedReader(new FileReader(argv[i]));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i + 2])));
            svm_model model = svm.svm_load_model(argv[i + 1]);
            if (predict_probability == 1) {
                if (svm.svm_check_probability_model(model) == 0) {
                    System.err.print("Model does not support probabiliy estimates\n");
                    System.exit(1);
                }
            } else if (svm.svm_check_probability_model(model) != 0) {
                System.out.print("Model supports probability estimates, but disabled in prediction.\n");
            }
            svm_predict.predict(input, output, model, predict_probability);
            input.close();
            output.close();
        }
        catch (FileNotFoundException e) {
            svm_predict.exit_with_help();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            svm_predict.exit_with_help();
        }
    }
}

