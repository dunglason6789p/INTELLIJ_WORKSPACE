package de.bwaldvogel.liblinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Predict {
   private static boolean flag_predict_probability = false;
   private static final Pattern COLON = Pattern.compile(":");

   public Predict() {
   }

   static void doPredict(BufferedReader reader, Writer writer, Model model) throws IOException {
      int correct = 0;
      int total = 0;
      int nr_class = model.getNrClass();
      double[] prob_estimates = null;
      int nr_feature = model.getNrFeature();
      int n;
      if (model.bias >= 0.0D) {
         n = nr_feature + 1;
      } else {
         n = nr_feature;
      }

      Formatter out = new Formatter(writer);
      if (flag_predict_probability) {
         if (!model.isProbabilityModel()) {
            throw new IllegalArgumentException("probability output is only supported for logistic regression");
         }

         int[] labels = model.getLabels();
         prob_estimates = new double[nr_class];
         Linear.printf(out, "labels");

         for(int j = 0; j < nr_class; ++j) {
            Linear.printf(out, " %d", labels[j]);
         }

         Linear.printf(out, "\n");
      }

      for(String line = null; (line = reader.readLine()) != null; ++total) {
         List<FeatureNode> x = new ArrayList();
         StringTokenizer st = new StringTokenizer(line, " \t\n");

         int target_label;
         try {
            String label = st.nextToken();
            target_label = Linear.atoi(label);
         } catch (NoSuchElementException var20) {
            throw new RuntimeException("Wrong input format at line " + (total + 1), var20);
         }

         int predict_label;
         while(st.hasMoreTokens()) {
            String[] split = COLON.split(st.nextToken(), 2);
            if (split == null || split.length < 2) {
               throw new RuntimeException("Wrong input format at line " + (total + 1));
            }

            try {
               predict_label = Linear.atoi(split[0]);
               double val = Linear.atof(split[1]);
               if (predict_label <= nr_feature) {
                  FeatureNode node = new FeatureNode(predict_label, val);
                  x.add(node);
               }
            } catch (NumberFormatException var19) {
               throw new RuntimeException("Wrong input format at line " + (total + 1), var19);
            }
         }

         if (model.bias >= 0.0D) {
            FeatureNode node = new FeatureNode(n, model.bias);
            x.add(node);
         }

         FeatureNode[] nodes = new FeatureNode[x.size()];
         nodes = (FeatureNode[])x.toArray(nodes);
         if (flag_predict_probability) {
            assert prob_estimates != null;

            predict_label = Linear.predictProbability(model, nodes, prob_estimates);
            Linear.printf(out, "%d", predict_label);

            for(int j = 0; j < model.nr_class; ++j) {
               Linear.printf(out, " %g", prob_estimates[j]);
            }

            Linear.printf(out, "\n");
         } else {
            predict_label = Linear.predict(model, nodes);
            Linear.printf(out, "%d\n", predict_label);
         }

         if (predict_label == target_label) {
            ++correct;
         }
      }

      System.out.printf("Accuracy = %g%% (%d/%d)%n", (double)correct / (double)total * 100.0D, correct, total);
   }

   private static void exit_with_help() {
      System.out.printf("Usage: predict [options] test_file model_file output_file%noptions:%n-b probability_estimates: whether to output probability estimates, 0 or 1 (default 0)%n");
      System.exit(1);
   }

   public static void main(String[] argv) throws IOException {
      int i;
      for(i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
         ++i;
         switch(argv[i - 1].charAt(1)) {
         case 'b':
            try {
               flag_predict_probability = Linear.atoi(argv[i]) != 0;
            } catch (NumberFormatException var9) {
               exit_with_help();
            }
            break;
         default:
            System.err.printf("unknown option: -%d%n", argv[i - 1].charAt(1));
            exit_with_help();
         }
      }

      if (i >= argv.length || argv.length <= i + 2) {
         exit_with_help();
      }

      BufferedReader reader = null;
      BufferedWriter writer = null;

      try {
         reader = new BufferedReader(new InputStreamReader(new FileInputStream(argv[i]), Linear.FILE_CHARSET));
         writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argv[i + 2]), Linear.FILE_CHARSET));
         Model model = Linear.loadModel(new File(argv[i + 1]));
         doPredict(reader, writer, model);
      } finally {
         Linear.closeQuietly(reader);
         Linear.closeQuietly(writer);
      }

   }
}
