package vn.edu.vnu.uet.nlp.segmenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import vn.edu.vnu.uet.liblinear.Feature;
import vn.edu.vnu.uet.liblinear.FeatureNode;
import vn.edu.vnu.uet.liblinear.Linear;
import vn.edu.vnu.uet.liblinear.Model;
import vn.edu.vnu.uet.liblinear.Parameter;
import vn.edu.vnu.uet.liblinear.Problem;
import vn.edu.vnu.uet.liblinear.SolverType;
import vn.edu.vnu.uet.nlp.segmenter.measurement.F1Score;
import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

public class SegmentationSystem {
   private static double r = 0.33D;
   private Problem problem = new Problem();
   private Parameter parameter;
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
      this.parameter = new Parameter(SolverType.L2R_LR, 1.0D, 0.01D);
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
      this.parameter = new Parameter(SolverType.L2R_LR, 1.0D, 0.01D);
      this.load(folderpath);
   }

   private void setProblem() {
      int numSamples = this.fe.getNumSamples();
      FeatureNode[][] x = new FeatureNode[numSamples][];
      double[] y = new double[numSamples];
      int sampleNo = 0;

      for(int s = 0; s < this.fe.getNumSents(); ++s) {
         for(int i = 0; i < ((List)this.fe.getSegmentList().get(s)).size(); ++i) {
            y[sampleNo] = (double)((SegmentFeature)((List)this.fe.getSegmentList().get(s)).get(i)).getLabel();
            SortedSet<Integer> featSet = ((SegmentFeature)((List)this.fe.getSegmentList().get(s)).get(i)).getFeatset();
            x[sampleNo] = new FeatureNode[featSet.size()];
            int cnt = 0;

            for(Iterator var9 = featSet.iterator(); var9.hasNext(); ++cnt) {
               Integer t = (Integer)var9.next();
               x[sampleNo][cnt] = new FeatureNode(t + 1, 1.0D);
            }

            featSet.clear();
            ++sampleNo;
         }
      }

      this.problem.l = numSamples;
      this.problem.n = this.n;
      this.problem.y = y;
      this.problem.x = x;
      this.problem.bias = -1.0D;
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
      } catch (Exception var13) {
      }

      String logName = "log/log_test_" + new Date() + ".txt";
      BufferedWriter bw = FileUtils.newUTF8BufferedWriterFromNewFile(logName);
      int sentID = 1;

      for(Iterator var6 = sentences.iterator(); var6.hasNext(); ++sentID) {
         String sentence = (String)var6.next();
         sentence = Normalizer.normalize(sentence, Form.NFC);
         if (this.testSentence(sentence)) {
            ++sentCnt;
         } else {
            bw.write("\n-----Sent " + sentID + "-----\n" + sentence + "\n" + this.segment(sentence) + "\n\n");
            bw.flush();
         }
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
      OldLogging.info("\nSentences right accuracy:\t" + (double)sentCnt / (double)sentences.size() * 100.0D + "%");
      OldLogging.info("\nLogged wrong predictions to " + logName);
      return result;
   }

   private boolean testSentence(String sentence) {
      boolean sentCheck = true;
      this.fe.clearList();
      this.segmentList = new ArrayList();
      List<SyllabelFeature> sylList = this.fe.extract((String)sentence, 2);
      if (this.fe.getSegmentList().isEmpty()) {
         return true;
      } else if (((List)this.fe.getSegmentList().get(0)).isEmpty()) {
         if (sylList.size() == 5) {
            ++this.N1;
            ++this.N2;
            ++this.N3;
            return true;
         } else {
            return true;
         }
      } else {
         int size;
         for(size = 2; size < sylList.size() - 2; ++size) {
            this.segmentList.add(sylList.get(size));
         }

         sylList.clear();
         size = this.segmentList.size() - 1;
         double[] reality = new double[size];
         double[] predictions = new double[size];
         this.confidences = new double[size];

         for(int i = 0; i < size; ++i) {
            reality[i] = (double)((SyllabelFeature)this.segmentList.get(i)).getLabel();
            this.confidences[i] = 4.9E-324D;
         }

         this.setProblem();
         this.process(predictions, size, 2);
         boolean previousSpaceMatch = true;

         for(int i = 0; i < size; ++i) {
            if (reality[i] == 0.0D) {
               ++this.N2;
            }

            if (predictions[i] == 0.0D) {
               ++this.N1;
               if (reality[i] == 0.0D) {
                  if (previousSpaceMatch) {
                     ++this.N3;
                  }

                  previousSpaceMatch = true;
               }
            }

            if (predictions[i] != reality[i]) {
               sentCheck = false;
               previousSpaceMatch = false;
            }
         }

         ++this.N1;
         ++this.N2;
         if (previousSpaceMatch) {
            ++this.N3;
         }

         return sentCheck;
      }
   }

   public String segment(String sentence) {
      this.fe.clearList();
      this.segmentList = new ArrayList();
      List<SyllabelFeature> sylList = this.fe.extract((String)sentence, 2);
      if (this.fe.getSegmentList().isEmpty()) {
         return "";
      } else if (((List)this.fe.getSegmentList().get(0)).isEmpty()) {
         return sylList.size() == 5 ? ((SyllabelFeature)sylList.get(2)).getSyllabel() : "";
      } else {
         int size;
         for(size = 2; size < sylList.size() - 2; ++size) {
            this.segmentList.add(sylList.get(size));
         }

         sylList.clear();
         size = this.segmentList.size() - 1;
         double[] predictions = new double[size];
         this.confidences = new double[size];

         for(int i = 0; i < size; ++i) {
            this.confidences[i] = 4.9E-324D;
         }

         this.setProblem();
         this.process(predictions, size, 1);
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < size; ++i) {
            sb.append(((SyllabelFeature)this.segmentList.get(i)).getSyllabel());
            if (predictions[i] == 0.0D) {
               sb.append(" ");
            } else {
               sb.append("_");
            }
         }

         sb.append(((SyllabelFeature)this.segmentList.get(size)).getSyllabel());
         this.delProblem();
         return sb.toString().trim();
      }
   }

   private void process(double[] predictions, int size, int mode) {
      this.longestMatching(predictions, size, mode);
      this.logisticRegression(predictions, size, mode);
      this.postProcessing(predictions, size, mode);
   }

   private void longestMatching(double[] predictions, int size, int mode) {
      for(int i = 0; i < size; ++i) {
         if (((SyllabelFeature)this.segmentList.get(i)).getType() != SyllableType.OTHER) {
            for(int n = 6; n >= 2; --n) {
               StringBuilder sb = new StringBuilder();
               boolean hasUpper = false;
               boolean hasLower = false;

               int j;
               for(j = i; j < i + n && j < this.segmentList.size() && ((SyllabelFeature)this.segmentList.get(j)).getType() != SyllableType.OTHER; ++j) {
                  if (mode != 2 && (((SyllabelFeature)this.segmentList.get(j)).getType() == SyllableType.UPPER || ((SyllabelFeature)this.segmentList.get(j)).getType() == SyllableType.ALLUPPER)) {
                     hasUpper = true;
                  }

                  if (((SyllabelFeature)this.segmentList.get(j)).getType() == SyllableType.LOWER) {
                     hasLower = true;
                  }

                  sb.append(" " + ((SyllabelFeature)this.segmentList.get(j)).getSyllabel());
               }

               if (j == i + n) {
                  String word = sb.toString();
                  int k;
                  if (n > 2 && hasLower && Dictionary.inVNDict(word)) {
                     for(k = i; k < j - 1; ++k) {
                        predictions[k] = 1.0D;
                     }

                     i = j - 1;
                     break;
                  }

                  if (mode != 2 && hasUpper && RareNames.isRareName(word)) {
                     for(k = i; k < j - 1; ++k) {
                        predictions[k] = 1.0D;
                     }

                     i = j - 1;
                     break;
                  }
               }
            }
         }
      }

   }

   private void ruleForProperName(double[] predictions, int size, int mode) {
      double[] temp = new double[size];

      int i;
      for(i = 0; i < size; ++i) {
         temp[i] = predictions[i];
      }

      for(i = 0; i < size; ++i) {
         if (temp[i] != 1.0D && (i == 0 || temp[i - 1] != 1.0D) && (i == size - 1 || temp[i + 1] != 1.0D) && ((SyllabelFeature)this.segmentList.get(i)).getType() == SyllableType.UPPER && ((SyllabelFeature)this.segmentList.get(i + 1)).getType() == SyllableType.UPPER) {
            predictions[i] = 1.0D;
         }
      }

   }

   private void logisticRegression(double[] predictions, int size, int mode) {
      double[] temp = new double[size];

      int i;
      for(i = 0; i < size; ++i) {
         temp[i] = predictions[i];
      }

      for(i = 0; i < size; ++i) {
         if (temp[i] != 1.0D && (i == 0 || temp[i - 1] != 1.0D) && (i == size - 1 || temp[i + 1] != 1.0D)) {
            predictions[i] = this.predict(this.problem.x[i], i, mode);
         }
      }

   }

   private double predict(Feature[] featSet, int sampleNo, int mode) {
      double[] dec_values = new double[this.model.getNrClass()];
      double result = Linear.predict(this.model, featSet, dec_values);
      this.confidences[sampleNo] = dec_values[0];
      return result;
   }

   private void postProcessing(double[] predictions, int size, int mode) {
      if (size >= 2) {
         for(int i = 0; i < size - 1; ++i) {
            double sigm = this.sigmoid(this.confidences[i]);
            SyllabelFeature preSyl = i > 0 ? (SyllabelFeature)this.segmentList.get(i - 1) : null;
            SyllabelFeature thisSyl = (SyllabelFeature)this.segmentList.get(i);
            SyllabelFeature nextSyl = (SyllabelFeature)this.segmentList.get(i + 1);
            String word;
            if ((i == 0 || predictions[i - 1] == 0.0D) && predictions[i + 1] == 0.0D) {
               if (Math.abs(sigm - 0.5D) < r) {
                  String thisOne = thisSyl.getSyllabel();
                  String nextOne = nextSyl.getSyllabel();
                  if (preSyl == null && nextSyl.getType() == SyllableType.LOWER || (thisSyl.getType() == SyllableType.LOWER || thisSyl.getType() == SyllableType.UPPER) && nextSyl.getType() == SyllableType.LOWER) {
                     word = thisOne + " " + nextOne;
                     if (Dictionary.inVNDict(word)) {
                        predictions[i] = 1.0D;
                     }

                     if (!Dictionary.inVNDict(word)) {
                        predictions[i] = 0.0D;
                     }
                  }
               }
            } else if (predictions[i] == 1.0D && (i == 0 || predictions[i - 1] == 0.0D) && predictions[i + 1] == 1.0D && (i == size - 2 || predictions[i + 2] == 0.0D)) {
               boolean flag = false;

               int j;
               for(j = i; j < i + 3; ++j) {
                  if (j != 0 || ((SyllabelFeature)this.segmentList.get(j)).getType() != SyllableType.LOWER && ((SyllabelFeature)this.segmentList.get(j)).getType() != SyllableType.UPPER) {
                     if (((SyllabelFeature)this.segmentList.get(j)).getType() == SyllableType.LOWER) {
                        flag = true;
                     }

                     if (((SyllabelFeature)this.segmentList.get(j)).getType() != SyllableType.LOWER && ((SyllabelFeature)this.segmentList.get(j)).getType() != SyllableType.UPPER) {
                        break;
                     }
                  }
               }

               if (j == i + 3 && flag) {
                  word = ((SyllabelFeature)this.segmentList.get(i)).getSyllabel() + " " + ((SyllabelFeature)this.segmentList.get(i + 1)).getSyllabel() + " " + ((SyllabelFeature)this.segmentList.get(i + 2)).getSyllabel();
                  if (!Dictionary.inVNDict(word) && !RareNames.isRareName(word)) {
                     String leftWord = ((SyllabelFeature)this.segmentList.get(i)).getSyllabel() + " " + ((SyllabelFeature)this.segmentList.get(i + 1)).getSyllabel();
                     String rightWord = ((SyllabelFeature)this.segmentList.get(i + 1)).getSyllabel() + " " + ((SyllabelFeature)this.segmentList.get(i + 2)).getSyllabel();
                     if (Dictionary.inVNDict(leftWord) && !Dictionary.inVNDict(rightWord)) {
                        predictions[i + 1] = 0.0D;
                     }

                     if (Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {
                        predictions[i] = 0.0D;
                     }

                     if (Dictionary.inVNDict(rightWord) && Dictionary.inVNDict(leftWord) && this.confidences[i] * this.confidences[i + 1] > 0.0D) {
                        if (Math.abs(this.confidences[i]) < Math.abs(this.confidences[i + 1])) {
                           predictions[i] = 0.0D;
                        } else {
                           predictions[i + 1] = 0.0D;
                        }
                     }

                     if (!Dictionary.inVNDict(rightWord) && !Dictionary.inVNDict(leftWord)) {
                        if (((SyllabelFeature)this.segmentList.get(i)).getType() == SyllableType.LOWER || ((SyllabelFeature)this.segmentList.get(i + 1)).getType() == SyllableType.LOWER) {
                           predictions[i] = 0.0D;
                        }

                        if (((SyllabelFeature)this.segmentList.get(i + 2)).getType() == SyllableType.LOWER || ((SyllabelFeature)this.segmentList.get(i + 1)).getType() == SyllableType.LOWER) {
                           predictions[i + 1] = 0.0D;
                        }
                     }
                  }

                  i += 2;
               }
            }
         }

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
      } catch (IOException var3) {
         var3.printStackTrace();
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
      return 1.0D / (1.0D + Math.exp(-d));
   }

   public void setR(double r) {
      if (r >= 0.0D && r <= 0.5D) {
         SegmentationSystem.r = r;
      }
   }
}
