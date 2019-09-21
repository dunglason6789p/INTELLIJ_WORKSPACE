package org.maltparser.ml.liblinear;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.config.Configuration;
import org.maltparser.core.config.ConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.helper.NoPrintStream;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.ml.LearningMethod;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;
import org.maltparser.parser.history.kbest.ScoredKBestList;

public class Liblinear implements LearningMethod {
   public static final String LIBLINEAR_VERSION = "1.51";
   private LinkedHashMap<String, String> liblinearOptions;
   protected InstanceModel owner;
   protected int learnerMode;
   protected String name;
   protected int numberOfInstances;
   protected boolean saveInstanceFiles;
   protected boolean excludeNullValues;
   protected String pathExternalLiblinearTrain = null;
   private BufferedWriter instanceOutput = null;
   private Model model = null;
   private String paramString;
   private ArrayList<FeatureNode> xlist = null;
   private Liblinear.Verbostity verbosity;
   private HashMap<Long, Integer> featureMap;
   private int featureCounter = 1;
   private boolean featurePruning = false;
   private TreeSet<XNode> featureSet;

   public Liblinear(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
      this.setOwner(owner);
      this.setLearningMethodName("liblinear");
      this.setLearnerMode(learnerMode);
      this.setNumberOfInstances(0);
      this.verbosity = Liblinear.Verbostity.SILENT;
      this.liblinearOptions = new LinkedHashMap();
      this.initLiblinearOptions();
      this.parseParameters(this.getConfiguration().getOptionValue("liblinear", "liblinear_options").toString());
      this.initSpecialParameters();
      if (learnerMode == 0) {
         if (this.featurePruning) {
            this.featureMap = new HashMap();
         }

         this.instanceOutput = new BufferedWriter(this.getInstanceOutputStreamWriter(".ins"));
      }

      if (this.featurePruning) {
         this.featureSet = new TreeSet();
      }

   }

   private int addFeatureMapValue(int featurePosition, int code) {
      long key = (long)featurePosition << 48 | (long)code;
      if (this.featureMap.containsKey(key)) {
         return (Integer)this.featureMap.get(key);
      } else {
         int value = this.featureCounter++;
         this.featureMap.put(key, value);
         return value;
      }
   }

   private int getFeatureMapValue(int featurePosition, int code) {
      long key = (long)featurePosition << 48 | (long)code;
      return this.featureMap.containsKey(key) ? (Integer)this.featureMap.get(key) : -1;
   }

   private void saveFeatureMap(OutputStream os, HashMap<Long, Integer> map) throws MaltChainedException {
      try {
         ObjectOutputStream obj_out_stream = new ObjectOutputStream(os);
         obj_out_stream.writeObject(map);
         obj_out_stream.close();
      } catch (IOException var4) {
         throw new LiblinearException("Save feature map error", var4);
      }
   }

   private HashMap<Long, Integer> loadFeatureMap(InputStream is) throws MaltChainedException {
      new HashMap();

      try {
         ObjectInputStream obj_in_stream = new ObjectInputStream(is);
         HashMap<Long, Integer> map = (HashMap)obj_in_stream.readObject();
         obj_in_stream.close();
         return map;
      } catch (ClassNotFoundException var4) {
         throw new LiblinearException("Load feature map error", var4);
      } catch (IOException var5) {
         throw new LiblinearException("Load feature map error", var5);
      }
   }

   public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
      if (featureVector == null) {
         throw new LiblinearException("The feature vector cannot be found");
      } else if (decision == null) {
         throw new LiblinearException("The decision cannot be found");
      } else {
         StringBuilder sb = new StringBuilder();

         try {
            sb.append(decision.getDecisionCode() + "\t");
            int n = featureVector.size();

            for(int i = 0; i < n; ++i) {
               FeatureValue featureValue = ((FeatureFunction)featureVector.get(i)).getFeatureValue();
               if (this.excludeNullValues && featureValue.isNullValue()) {
                  sb.append("-1");
               } else if (featureValue instanceof SingleFeatureValue) {
                  sb.append(((SingleFeatureValue)featureValue).getIndexCode() + "");
               } else if (featureValue instanceof MultipleFeatureValue) {
                  Set<Integer> values = ((MultipleFeatureValue)featureValue).getCodes();
                  int j = 0;

                  for(Iterator i$ = values.iterator(); i$.hasNext(); ++j) {
                     Integer value = (Integer)i$.next();
                     sb.append(value.toString());
                     if (j != values.size() - 1) {
                        sb.append("|");
                     }
                  }
               }

               sb.append('\t');
            }

            sb.append('\n');
            this.instanceOutput.write(sb.toString());
            this.instanceOutput.flush();
            this.increaseNumberOfInstances();
         } catch (IOException var11) {
            throw new LiblinearException("The Liblinear learner cannot write to the instance file. ", var11);
         }
      }
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
   }

   public void noMoreInstances() throws MaltChainedException {
      this.closeInstanceWriter();
   }

   public void train() throws MaltChainedException {
      if (this.owner == null) {
         throw new LiblinearException("The parent guide model cannot be found. ");
      } else {
         if (this.pathExternalLiblinearTrain == null) {
            try {
               Problem problem = null;
               if (this.featurePruning) {
                  problem = this.readLibLinearProblemWithFeaturePruning(this.getInstanceInputStreamReader(".ins"));
               }

               Configuration config = this.owner.getGuide().getConfiguration();
               if (config.isLoggerInfoEnabled()) {
                  config.logInfoMessage("Creating Liblinear model " + this.getFile(".mod").getName() + "\n");
               }

               PrintStream out = System.out;
               PrintStream err = System.err;
               System.setOut(NoPrintStream.NO_PRINTSTREAM);
               System.setErr(NoPrintStream.NO_PRINTSTREAM);
               Linear.saveModel(new File(this.getFile(".mod").getAbsolutePath()), Linear.train(problem, this.getLiblinearParameters()));
               System.setOut(err);
               System.setOut(out);
               if (!this.saveInstanceFiles) {
                  this.getFile(".ins").delete();
               }
            } catch (OutOfMemoryError var6) {
               throw new LiblinearException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var6);
            } catch (IllegalArgumentException var7) {
               throw new LiblinearException("The Liblinear learner was not able to redirect Standard Error stream. ", var7);
            } catch (SecurityException var8) {
               throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", var8);
            } catch (IOException var9) {
               throw new LiblinearException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var9);
            }
         } else {
            this.trainExternal();
         }

         if (this.featurePruning) {
            try {
               this.saveFeatureMap(new FileOutputStream(this.getFile(".map").getAbsolutePath()), this.featureMap);
            } catch (FileNotFoundException var5) {
               throw new LiblinearException("The Liblinear learner cannot save the feature map file '" + this.getFile(".map").getAbsolutePath() + "'. ", var5);
            }
         }

      }
   }

   private void trainExternal() throws MaltChainedException {
      try {
         Configuration config = this.owner.getGuide().getConfiguration();
         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("Creating Liblinear model (external) " + this.getFile(".mod").getName());
         }

         String[] params = this.getLibLinearParamStringArray();
         String[] arrayCommands = new String[params.length + 3];
         int i = 0;
         int i = i + 1;

         for(arrayCommands[i] = this.pathExternalLiblinearTrain; i <= params.length; ++i) {
            arrayCommands[i] = params[i - 1];
         }

         arrayCommands[i++] = this.getFile(".ins.tmp").getAbsolutePath();
         arrayCommands[i++] = this.getFile(".mod").getAbsolutePath();
         if (this.verbosity == Liblinear.Verbostity.ALL) {
            config.logInfoMessage('\n');
         }

         Process child = Runtime.getRuntime().exec(arrayCommands);
         InputStream in = child.getInputStream();
         InputStream err = child.getErrorStream();

         int c;
         while((c = in.read()) != -1) {
            if (this.verbosity == Liblinear.Verbostity.ALL) {
               config.logInfoMessage((char)c);
            }
         }

         while(true) {
            do {
               if ((c = err.read()) == -1) {
                  if (child.waitFor() != 0) {
                     config.logErrorMessage(" FAILED (" + child.exitValue() + ")");
                  }

                  in.close();
                  err.close();
                  if (!this.saveInstanceFiles) {
                     this.getFile(".ins").delete();
                     this.getFile(".ins.tmp").delete();
                  }

                  if (config.isLoggerInfoEnabled()) {
                     config.logInfoMessage('\n');
                  }

                  return;
               }
            } while(this.verbosity != Liblinear.Verbostity.ALL && this.verbosity != Liblinear.Verbostity.ERROR);

            config.logInfoMessage((char)c);
         }
      } catch (InterruptedException var9) {
         throw new LiblinearException("Liblinear is interrupted. ", var9);
      } catch (IllegalArgumentException var10) {
         throw new LiblinearException("The Liblinear learner was not able to redirect Standard Error stream. ", var10);
      } catch (SecurityException var11) {
         throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", var11);
      } catch (IOException var12) {
         throw new LiblinearException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var12);
      } catch (OutOfMemoryError var13) {
         throw new LiblinearException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var13);
      }
   }

   public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
      if (method == null) {
         throw new LiblinearException("The learning method cannot be found. ");
      } else if (divideFeature == null) {
         throw new LiblinearException("The divide feature cannot be found. ");
      } else {
         try {
            BufferedReader in = new BufferedReader(this.getInstanceInputStreamReader(".ins"));
            BufferedWriter out = method.getInstanceWriter();
            StringBuilder sb = new StringBuilder(6);
            int l = in.read();
            int j = 0;

            while(l != -1) {
               char c = (char)l;
               l = in.read();
               if (c == '\t') {
                  if (divideFeatureIndexVector.contains(j - 1)) {
                     out.write(Integer.toString(((SingleFeatureValue)divideFeature.getFeatureValue()).getIndexCode()));
                     out.write(9);
                  }

                  out.write(sb.toString());
                  ++j;
                  out.write(9);
                  sb.setLength(0);
               } else if (c == '\n') {
                  out.write(sb.toString());
                  if (divideFeatureIndexVector.contains(j - 1)) {
                     out.write(9);
                     out.write(Integer.toString(((SingleFeatureValue)divideFeature.getFeatureValue()).getIndexCode()));
                  }

                  out.write(10);
                  sb.setLength(0);
                  method.increaseNumberOfInstances();
                  this.decreaseNumberOfInstances();
                  j = 0;
               } else {
                  sb.append(c);
               }
            }

            sb.setLength(0);
            in.close();
            this.getFile(".ins").delete();
            out.flush();
         } catch (SecurityException var10) {
            throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", var10);
         } catch (NullPointerException var11) {
            throw new LiblinearException("The instance file cannot be found. ", var11);
         } catch (FileNotFoundException var12) {
            throw new LiblinearException("The instance file cannot be found. ", var12);
         } catch (IOException var13) {
            throw new LiblinearException("The Liblinear learner read from the instance file. ", var13);
         }
      }
   }

   public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      if (this.model == null) {
         try {
            this.model = Linear.loadModel((Reader)(new BufferedReader(this.getInstanceInputStreamReaderFromConfigFile(".mod"))));
         } catch (IOException var5) {
            throw new LiblinearException("The model cannot be loaded. ", var5);
         }
      }

      if (this.model == null) {
         throw new LiblinearException("The Liblinear learner cannot predict the next class, because the learning model cannot be found. ");
      } else if (featureVector == null) {
         throw new LiblinearException("The Liblinear learner cannot predict the next class, because the feature vector cannot be found. ");
      } else if (this.featurePruning) {
         return this.predictWithFeaturePruning(featureVector, decision);
      } else {
         if (this.xlist == null) {
            this.xlist = new ArrayList(featureVector.size());
         }

         FeatureNode[] xarray = new FeatureNode[this.xlist.size()];

         for(int k = 0; k < this.xlist.size(); ++k) {
            xarray[k] = (FeatureNode)this.xlist.get(k);
         }

         if (decision.getKBestList().getK() == 1) {
            decision.getKBestList().add(Linear.predict(this.model, xarray));
         } else {
            this.liblinear_predict_with_kbestlist(this.model, xarray, decision.getKBestList());
         }

         this.xlist.clear();
         return true;
      }
   }

   public boolean predictWithFeaturePruning(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      if (this.featureMap == null) {
         this.featureMap = this.loadFeatureMap(this.getInputStreamFromConfigFileEntry(".map"));
      }

      Iterator i$;
      for(int i = 0; i < featureVector.size(); ++i) {
         FeatureValue featureValue = featureVector.getFeatureValue(i - 1);
         if (!this.excludeNullValues || !featureValue.isNullValue()) {
            if (featureValue instanceof SingleFeatureValue) {
               int v = this.getFeatureMapValue(i, ((SingleFeatureValue)featureValue).getIndexCode());
               if (v != -1) {
                  this.featureSet.add(new XNode(v, 1.0D));
               }
            } else if (featureValue instanceof MultipleFeatureValue) {
               i$ = ((MultipleFeatureValue)featureValue).getCodes().iterator();

               while(i$.hasNext()) {
                  Integer value = (Integer)i$.next();
                  int v = this.getFeatureMapValue(i, value);
                  if (v != -1) {
                     this.featureSet.add(new XNode(v, 1.0D));
                  }
               }
            }
         }
      }

      FeatureNode[] xarray = new FeatureNode[this.featureSet.size()];
      int k = 0;

      XNode x;
      for(i$ = this.featureSet.iterator(); i$.hasNext(); xarray[k++] = new FeatureNode(x.getIndex(), x.getValue())) {
         x = (XNode)i$.next();
      }

      if (decision.getKBestList().getK() == 1) {
         decision.getKBestList().add(Linear.predict(this.model, xarray));
      } else {
         this.liblinear_predict_with_kbestlist(this.model, xarray, decision.getKBestList());
      }

      this.featureSet.clear();
      return true;
   }

   public void terminate() throws MaltChainedException {
      this.closeInstanceWriter();
      this.model = null;
      this.xlist = null;
      this.owner = null;
   }

   public BufferedWriter getInstanceWriter() {
      return this.instanceOutput;
   }

   protected void closeInstanceWriter() throws MaltChainedException {
      try {
         if (this.instanceOutput != null) {
            this.instanceOutput.flush();
            this.instanceOutput.close();
            this.instanceOutput = null;
         }

      } catch (IOException var2) {
         throw new LiblinearException("The Liblinear learner cannot close the instance file. ", var2);
      }
   }

   public String getParamString() {
      return this.paramString;
   }

   public InstanceModel getOwner() {
      return this.owner;
   }

   protected void setOwner(InstanceModel owner) {
      this.owner = owner;
   }

   public int getLearnerMode() {
      return this.learnerMode;
   }

   public void setLearnerMode(int learnerMode) throws MaltChainedException {
      this.learnerMode = learnerMode;
   }

   public String getLearningMethodName() {
      return this.name;
   }

   public DependencyParserConfig getConfiguration() throws MaltChainedException {
      return this.owner.getGuide().getConfiguration();
   }

   public int getNumberOfInstances() throws MaltChainedException {
      if (this.numberOfInstances != 0) {
         return this.numberOfInstances;
      } else {
         BufferedReader reader = new BufferedReader(this.getInstanceInputStreamReader(".ins"));

         try {
            while(reader.readLine() != null) {
               ++this.numberOfInstances;
               this.owner.increaseFrequency();
            }

            reader.close();
         } catch (IOException var3) {
            throw new MaltChainedException("No instances found in file", var3);
         }

         return this.numberOfInstances;
      }
   }

   public void increaseNumberOfInstances() {
      ++this.numberOfInstances;
      this.owner.increaseFrequency();
   }

   public void decreaseNumberOfInstances() {
      --this.numberOfInstances;
      this.owner.decreaseFrequency();
   }

   protected void setNumberOfInstances(int numberOfInstances) {
      this.numberOfInstances = 0;
   }

   protected void setLearningMethodName(String name) {
      this.name = name;
   }

   protected OutputStreamWriter getInstanceOutputStreamWriter(String suffix) throws MaltChainedException {
      return this.getConfiguration().getAppendOutputStreamWriter(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected InputStreamReader getInstanceInputStreamReader(String suffix) throws MaltChainedException {
      return this.getConfiguration().getInputStreamReader(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected InputStreamReader getInstanceInputStreamReaderFromConfigFile(String suffix) throws MaltChainedException {
      try {
         return new InputStreamReader(this.getInputStreamFromConfigFileEntry(suffix), "UTF-8");
      } catch (UnsupportedEncodingException var3) {
         throw new ConfigurationException("The char set UTF-8 is not supported. ", var3);
      }
   }

   protected InputStream getInputStreamFromConfigFileEntry(String suffix) throws MaltChainedException {
      return this.getConfiguration().getInputStreamFromConfigFileEntry(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected File getFile(String suffix) throws MaltChainedException {
      return this.getConfiguration().getFile(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   public Problem readLibLinearProblemWithFeaturePruning(InputStreamReader isr) throws MaltChainedException {
      Problem problem = new Problem();

      try {
         BufferedReader fp = new BufferedReader(isr);
         problem.bias = -1.0D;
         problem.l = this.getNumberOfInstances();
         problem.x = new FeatureNode[problem.l][];
         problem.y = new int[problem.l];
         int i = 0;
         Pattern tabPattern = Pattern.compile("\t");
         Pattern pipePattern = Pattern.compile("\\|");

         while(true) {
            String[] columns;
            do {
               String line = fp.readLine();
               if (line == null) {
                  fp.close();
                  this.featureSet = null;
                  problem.n = this.featureMap.size();
                  System.out.println("Number of features: " + problem.n);
                  return problem;
               }

               columns = tabPattern.split(line);
            } while(columns.length == 0);

            byte j = 0;

            try {
               problem.y[i] = Integer.parseInt(columns[j]);

               for(int j = 1; j < columns.length; ++j) {
                  String[] items = pipePattern.split(columns[j]);

                  for(int k = 0; k < items.length; ++k) {
                     try {
                        int colon = items[k].indexOf(58);
                        int v;
                        if (colon == -1) {
                           if (Integer.parseInt(items[k]) != -1) {
                              v = this.addFeatureMapValue(j, Integer.parseInt(items[k]));
                              if (v != -1) {
                                 this.featureSet.add(new XNode(v, 1.0D));
                              }
                           }
                        } else {
                           v = this.addFeatureMapValue(j, Integer.parseInt(items[k].substring(0, colon)));
                           double value;
                           if (items[k].substring(colon + 1).indexOf(46) != -1) {
                              value = Double.parseDouble(items[k].substring(colon + 1));
                           } else {
                              value = (double)Integer.parseInt(items[k].substring(colon + 1));
                           }

                           this.featureSet.add(new XNode(v, value));
                        }
                     } catch (NumberFormatException var16) {
                        throw new LiblinearException("The instance file contain a non-integer value '" + items[k] + "'", var16);
                     }
                  }
               }

               problem.x[i] = new FeatureNode[this.featureSet.size()];
               int p = 0;

               XNode x;
               for(Iterator i$ = this.featureSet.iterator(); i$.hasNext(); problem.x[i][p++] = new FeatureNode(x.getIndex(), x.getValue())) {
                  x = (XNode)i$.next();
               }

               this.featureSet.clear();
               ++i;
            } catch (ArrayIndexOutOfBoundsException var17) {
               throw new LiblinearException("Cannot read from the instance file. ", var17);
            }
         }
      } catch (IOException var18) {
         throw new LiblinearException("Cannot read from the instance file. ", var18);
      }
   }

   public Problem readLibLinearProblem(InputStreamReader isr, int[] cardinalities) throws MaltChainedException {
      Problem problem = new Problem();

      try {
         BufferedReader fp = new BufferedReader(isr);
         int max_index = 0;
         if (this.xlist == null) {
            this.xlist = new ArrayList();
         }

         problem.bias = -1.0D;
         problem.l = this.getNumberOfInstances();
         problem.x = new FeatureNode[problem.l][];
         problem.y = new int[problem.l];
         int i = 0;
         Pattern tabPattern = Pattern.compile("\t");
         Pattern pipePattern = Pattern.compile("\\|");

         while(true) {
            String[] columns;
            do {
               String line = fp.readLine();
               if (line == null) {
                  fp.close();
                  problem.n = max_index;
                  System.out.println("Number of features: " + problem.n);
                  this.xlist = null;
                  return problem;
               }

               columns = tabPattern.split(line);
            } while(columns.length == 0);

            int offset = 1;
            byte j = 0;

            try {
               problem.y[i] = Integer.parseInt(columns[j]);
               int p = 0;

               for(int j = 1; j < columns.length; ++j) {
                  String[] items = pipePattern.split(columns[j]);

                  for(int k = 0; k < items.length; ++k) {
                     try {
                        if (Integer.parseInt(items[k]) != -1) {
                           this.xlist.add(p, new FeatureNode(Integer.parseInt(items[k]) + offset, 1.0D));
                           ++p;
                        }
                     } catch (NumberFormatException var17) {
                        throw new LiblinearException("The instance file contain a non-integer value '" + items[k] + "'", var17);
                     }
                  }

                  offset += cardinalities[j - 1];
               }

               problem.x[i] = (FeatureNode[])this.xlist.subList(0, p).toArray(new FeatureNode[0]);
               if (columns.length > 1) {
                  max_index = Math.max(max_index, problem.x[i][p - 1].index);
               }

               ++i;
               this.xlist.clear();
            } catch (ArrayIndexOutOfBoundsException var18) {
               throw new LiblinearException("Cannot read from the instance file. ", var18);
            }
         }
      } catch (IOException var19) {
         throw new LiblinearException("Cannot read from the instance file. ", var19);
      }
   }

   protected void initSpecialParameters() throws MaltChainedException {
      if (this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none")) {
         this.excludeNullValues = true;
      } else {
         this.excludeNullValues = false;
      }

      this.saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("liblinear", "save_instance_files");
      this.featurePruning = true;
      if (!this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().equals("")) {
         try {
            if (!(new File(this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString())).exists()) {
               throw new LiblinearException("The path to the external Liblinear trainer 'svm-train' is wrong.");
            }

            if ((new File(this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString())).isDirectory()) {
               throw new LiblinearException("The option --liblinear-liblinear_external points to a directory, the path should point at the 'train' file or the 'train.exe' file");
            }

            if (!this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().endsWith("train") && !this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().endsWith("train.exe")) {
               throw new LiblinearException("The option --liblinear-liblinear_external does not specify the path to 'train' file or the 'train.exe' file. ");
            }

            this.pathExternalLiblinearTrain = this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString();
         } catch (SecurityException var2) {
            throw new LiblinearException("Access denied to the file specified by the option --liblinear-liblinear_external. ", var2);
         }
      }

      if (this.getConfiguration().getOptionValue("liblinear", "verbosity") != null) {
         this.verbosity = Liblinear.Verbostity.valueOf(this.getConfiguration().getOptionValue("liblinear", "verbosity").toString().toUpperCase());
      }

   }

   public String getLibLinearOptions() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.liblinearOptions.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         sb.append('-');
         sb.append(key);
         sb.append(' ');
         sb.append((String)this.liblinearOptions.get(key));
         sb.append(' ');
      }

      return sb.toString();
   }

   public void parseParameters(String paramstring) throws MaltChainedException {
      if (paramstring != null) {
         String allowedFlags = "sceB";

         String[] argv;
         try {
            argv = paramstring.split("[_\\p{Blank}]");
         } catch (PatternSyntaxException var6) {
            throw new LiblinearException("Could not split the liblinear-parameter string '" + paramstring + "'. ", var6);
         }

         for(int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
               throw new LiblinearException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }

            ++i;
            if (i >= argv.length) {
               throw new LiblinearException("The last argument does not have any value. ");
            }

            try {
               int index = allowedFlags.indexOf(argv[i - 1].charAt(1));
               if (index == -1) {
                  throw new LiblinearException("Unknown liblinear parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
               }

               this.liblinearOptions.put(Character.toString(argv[i - 1].charAt(1)), argv[i]);
            } catch (ArrayIndexOutOfBoundsException var7) {
               throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var7);
            } catch (NumberFormatException var8) {
               throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var8);
            } catch (NullPointerException var9) {
               throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var9);
            }
         }

      }
   }

   public double getBias() throws MaltChainedException {
      try {
         return Double.valueOf((String)this.liblinearOptions.get("B"));
      } catch (NumberFormatException var2) {
         throw new LiblinearException("The liblinear bias value is not numerical value. ", var2);
      }
   }

   public Parameter getLiblinearParameters() throws MaltChainedException {
      Parameter param = new Parameter(SolverType.MCSVM_CS, 0.1D, 0.1D);
      String type = (String)this.liblinearOptions.get("s");
      if (type.equals("0")) {
         param.setSolverType(SolverType.L2R_LR);
      } else if (type.equals("1")) {
         param.setSolverType(SolverType.L2R_L2LOSS_SVC_DUAL);
      } else if (type.equals("2")) {
         param.setSolverType(SolverType.L2R_L2LOSS_SVC);
      } else if (type.equals("3")) {
         param.setSolverType(SolverType.L2R_L1LOSS_SVC_DUAL);
      } else if (type.equals("4")) {
         param.setSolverType(SolverType.MCSVM_CS);
      } else if (type.equals("5")) {
         param.setSolverType(SolverType.L1R_L2LOSS_SVC);
      } else {
         if (!type.equals("6")) {
            throw new LiblinearException("The liblinear type (-s) is not an integer value between 0 and 4. ");
         }

         param.setSolverType(SolverType.L1R_LR);
      }

      try {
         param.setC(Double.valueOf((String)this.liblinearOptions.get("c")));
      } catch (NumberFormatException var5) {
         throw new LiblinearException("The liblinear cost (-c) value is not numerical value. ", var5);
      }

      try {
         param.setEps(Double.valueOf((String)this.liblinearOptions.get("e")));
         return param;
      } catch (NumberFormatException var4) {
         throw new LiblinearException("The liblinear epsilon (-e) value is not numerical value. ", var4);
      }
   }

   public void initLiblinearOptions() {
      this.liblinearOptions.put("s", "4");
      this.liblinearOptions.put("c", "0.1");
      this.liblinearOptions.put("e", "0.1");
      this.liblinearOptions.put("B", "-1");
   }

   public String[] getLibLinearParamStringArray() {
      ArrayList<String> params = new ArrayList();
      Iterator i$ = this.liblinearOptions.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         params.add("-" + key);
         params.add(this.liblinearOptions.get(key));
      }

      return (String[])params.toArray(new String[params.size()]);
   }

   public void liblinear_predict_with_kbestlist(Model model, FeatureNode[] x, KBestList kBestList) throws MaltChainedException {
      int nr_class = model.getNrClass();
      double[] dec_values = new double[nr_class];
      Linear.predictValues(model, x, dec_values);
      int[] labels = model.getLabels();
      int[] predictionList = new int[nr_class];

      int i;
      for(i = 0; i < nr_class; ++i) {
         predictionList[i] = labels[i];
      }

      int j;
      for(i = 0; i < nr_class - 1; ++i) {
         int lagest = i;

         for(j = i; j < nr_class; ++j) {
            if (dec_values[j] > dec_values[lagest]) {
               lagest = j;
            }
         }

         double tmpDec = dec_values[lagest];
         dec_values[lagest] = dec_values[i];
         dec_values[i] = tmpDec;
         int tmpObj = predictionList[lagest];
         predictionList[lagest] = predictionList[i];
         predictionList[i] = tmpObj;
      }

      j = nr_class - 1;
      if (kBestList.getK() != -1) {
         j = kBestList.getK() - 1;
      }

      for(i = 0; i < nr_class && j >= 0; --j) {
         if (kBestList instanceof ScoredKBestList) {
            ((ScoredKBestList)kBestList).add(predictionList[i], (float)dec_values[i]);
         } else {
            kBestList.add(predictionList[i]);
         }

         ++i;
      }

   }

   public static void maltSVMFormat2OriginalSVMFormat(InputStreamReader isr, OutputStreamWriter osw, int[] cardinalities) throws MaltChainedException {
      try {
         BufferedReader in = new BufferedReader(isr);
         BufferedWriter out = new BufferedWriter(osw);
         int j = 0;
         int offset = 1;
         int code = 0;

         while(true) {
            int c;
            label56:
            do {
               while(true) {
                  while(true) {
                     while(true) {
                        c = in.read();
                        if (c == -1) {
                           in.close();
                           out.close();
                           return;
                        }

                        if (c != 9 && c != 124) {
                           if (c != 10) {
                              if (c != 45) {
                                 continue label56;
                              }

                              code = -1;
                           } else {
                              j = 0;
                              offset = 1;
                              out.write(10);
                              code = 0;
                           }
                        } else {
                           if (j == 0) {
                              out.write(Integer.toString(code));
                              ++j;
                           } else {
                              if (code != -1) {
                                 out.write(32);
                                 out.write(Integer.toString(code + offset));
                                 out.write(":1");
                              }

                              if (c == 9) {
                                 offset += cardinalities[j - 1];
                                 ++j;
                              }
                           }

                           code = 0;
                        }
                     }
                  }
               }
            } while(code == -1);

            if (c <= 47 || c >= 58) {
               throw new LiblinearException("The instance file contain a non-integer value, when converting the Malt SVM format into Liblinear format.");
            }

            code = code * 10 + (c - 48);
         }
      } catch (IOException var9) {
         throw new LiblinearException("Cannot read from the instance file, when converting the Malt SVM format into Liblinear format. ", var9);
      }
   }

   protected void finalize() throws Throwable {
      try {
         this.closeInstanceWriter();
      } finally {
         super.finalize();
      }

   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("\nLiblinear INTERFACE\n");
      sb.append("  Liblinear version: 1.51\n");
      sb.append("  Liblinear string: " + this.paramString + "\n");
      sb.append(this.getLibLinearOptions());
      return sb.toString();
   }

   public static enum Verbostity {
      SILENT,
      ERROR,
      ALL;

      private Verbostity() {
      }
   }
}
