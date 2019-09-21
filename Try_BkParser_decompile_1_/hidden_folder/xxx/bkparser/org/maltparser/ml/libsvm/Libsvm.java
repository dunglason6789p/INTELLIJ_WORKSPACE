package org.maltparser.ml.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.maltparser.core.config.Configuration;
import org.maltparser.core.config.ConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.ml.LearningMethod;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;
import org.maltparser.parser.history.kbest.ScoredKBestList;

public class Libsvm implements LearningMethod {
   public static final String LIBSVM_VERSION = "2.91";
   protected InstanceModel owner;
   protected int learnerMode;
   protected String name;
   protected int numberOfInstances;
   protected boolean saveInstanceFiles;
   protected boolean excludeNullValues;
   protected String pathExternalSVMTrain = null;
   private BufferedWriter instanceOutput = null;
   private svm_model model = null;
   private svm_parameter svmParam;
   private String paramString;
   private ArrayList<svm_node> xlist = null;
   private Libsvm.Verbostity verbosity;

   public Libsvm(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
      this.setOwner(owner);
      this.setLearningMethodName("libsvm");
      this.setLearnerMode(learnerMode);
      this.setNumberOfInstances(0);
      this.verbosity = Libsvm.Verbostity.SILENT;
      this.initSvmParam(this.getConfiguration().getOptionValue("libsvm", "libsvm_options").toString());
      this.initSpecialParameters();
      if (learnerMode == 0) {
         this.instanceOutput = new BufferedWriter(this.getInstanceOutputStreamWriter(".ins"));
      }

   }

   public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
      if (featureVector == null) {
         throw new LibsvmException("The feature vector cannot be found");
      } else if (decision == null) {
         throw new LibsvmException("The decision cannot be found");
      } else {
         try {
            this.instanceOutput.write(decision.getDecisionCode() + "\t");

            for(int i = 0; i < featureVector.size(); ++i) {
               FeatureValue featureValue = ((FeatureFunction)featureVector.get(i)).getFeatureValue();
               if (this.excludeNullValues && featureValue.isNullValue()) {
                  this.instanceOutput.write("-1");
               } else if (featureValue instanceof SingleFeatureValue) {
                  this.instanceOutput.write(((SingleFeatureValue)featureValue).getIndexCode() + "");
               } else if (featureValue instanceof MultipleFeatureValue) {
                  Set<Integer> values = ((MultipleFeatureValue)featureValue).getCodes();
                  int j = 0;

                  for(Iterator i$ = values.iterator(); i$.hasNext(); ++j) {
                     Integer value = (Integer)i$.next();
                     this.instanceOutput.write(value.toString());
                     if (j != values.size() - 1) {
                        this.instanceOutput.write("|");
                     }
                  }
               }

               if (i != featureVector.size()) {
                  this.instanceOutput.write(9);
               }
            }

            this.instanceOutput.write(10);
            this.instanceOutput.flush();
            this.increaseNumberOfInstances();
         } catch (IOException var9) {
            throw new LibsvmException("The LIBSVM learner cannot write to the instance file. ", var9);
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
         throw new LibsvmException("The parent guide model cannot be found. ");
      }
   }

   private void trainExternal() throws MaltChainedException {
      try {
         Configuration config = this.owner.getGuide().getConfiguration();
         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("Creating LIBSVM model (svm-train) " + this.getFile(".mod").getName());
         }

         ArrayList<String> commands = new ArrayList();
         commands.add(this.pathExternalSVMTrain);
         String[] params = this.getSVMParamStringArray(this.svmParam);

         for(int i = 0; i < params.length; ++i) {
            commands.add(params[i]);
         }

         commands.add(this.getFile(".ins.tmp").getAbsolutePath());
         commands.add(this.getFile(".mod").getAbsolutePath());
         String[] arrayCommands = (String[])commands.toArray(new String[commands.size()]);
         if (this.verbosity == Libsvm.Verbostity.ALL) {
            config.logInfoMessage('\n');
         }

         Process child = Runtime.getRuntime().exec(arrayCommands);
         InputStream in = child.getInputStream();
         InputStream err = child.getErrorStream();

         int c;
         while((c = in.read()) != -1) {
            if (this.verbosity == Libsvm.Verbostity.ALL) {
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
            } while(this.verbosity != Libsvm.Verbostity.ALL && this.verbosity != Libsvm.Verbostity.ERROR);

            config.logInfoMessage((char)c);
         }
      } catch (InterruptedException var9) {
         throw new LibsvmException("SVM-trainer is interrupted. ", var9);
      } catch (IllegalArgumentException var10) {
         throw new LibsvmException("The LIBSVM learner was not able to redirect Standard Error stream. ", var10);
      } catch (SecurityException var11) {
         throw new LibsvmException("The LIBSVM learner cannot remove the instance file. ", var11);
      } catch (IOException var12) {
         throw new LibsvmException("The LIBSVM learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var12);
      } catch (OutOfMemoryError var13) {
         throw new LibsvmException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var13);
      }
   }

   public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
      if (method == null) {
         throw new LibsvmException("The learning method cannot be found. ");
      } else if (divideFeature == null) {
         throw new LibsvmException("The divide feature cannot be found. ");
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
                  if (sb.length() > 0) {
                     out.write(sb.toString());
                  }

                  if (divideFeatureIndexVector.contains(j - 1)) {
                     if (sb.length() > 0) {
                        out.write(9);
                     }

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
         } catch (SecurityException var10) {
            throw new LibsvmException("The LIBSVM learner cannot remove the instance file. ", var10);
         } catch (NullPointerException var11) {
            throw new LibsvmException("The instance file cannot be found. ", var11);
         } catch (FileNotFoundException var12) {
            throw new LibsvmException("The instance file cannot be found. ", var12);
         } catch (IOException var13) {
            throw new LibsvmException("The LIBSVM learner read from the instance file. ", var13);
         }
      }
   }

   public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      if (this.model == null) {
         try {
            this.model = svm.svm_load_model(new BufferedReader(this.getInstanceInputStreamReaderFromConfigFile(".mod")));
         } catch (IOException var4) {
            throw new LibsvmException("The model cannot be loaded. ", var4);
         }
      }

      if (this.xlist == null) {
         this.xlist = new ArrayList(featureVector.size());
      }

      if (this.model == null) {
         throw new LibsvmException("The LIBSVM learner cannot predict the next class, because the learning model cannot be found. ");
      } else if (featureVector == null) {
         throw new LibsvmException("The LIBSVM learner cannot predict the next class, because the feature vector cannot be found. ");
      } else {
         return true;
      }
   }

   public void terminate() throws MaltChainedException {
      this.closeInstanceWriter();
      this.model = null;
      this.svmParam = null;
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
         throw new LibsvmException("The LIBSVM learner cannot close the instance file. ", var2);
      }
   }

   protected void initSvmParam(String paramString) throws MaltChainedException {
      this.paramString = paramString;
      this.svmParam = new svm_parameter();
      this.initParameters(this.svmParam);
      this.parseParameters(paramString, this.svmParam);
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

   public final svm_problem readProblemMaltSVMFormat(InputStreamReader isr, int[] cardinalities, svm_parameter param) throws MaltChainedException {
      svm_problem prob = new svm_problem();

      try {
         BufferedReader fp = new BufferedReader(isr);
         int max_index = 0;
         if (this.xlist == null) {
            this.xlist = new ArrayList();
         }

         prob.l = this.getNumberOfInstances();
         prob.x = new svm_node[prob.l][];
         prob.y = new double[prob.l];
         int i = 0;
         Pattern tabPattern = Pattern.compile("\t");
         Pattern pipePattern = Pattern.compile("\\|");

         while(true) {
            String[] columns;
            do {
               String line = fp.readLine();
               if (line == null) {
                  fp.close();
                  if (param.gamma == 0.0D) {
                     param.gamma = 1.0D / (double)max_index;
                  }

                  this.xlist = null;
                  return prob;
               }

               columns = tabPattern.split(line);
            } while(columns.length == 0);

            int offset = 0;
            byte j = 0;

            try {
               prob.y[i] = (double)Integer.parseInt(columns[j]);
               int p = 0;

               for(int j = 1; j < columns.length; ++j) {
                  String[] items = pipePattern.split(columns[j]);

                  for(int k = 0; k < items.length; ++k) {
                     try {
                        if (Integer.parseInt(items[k]) != -1) {
                           this.xlist.add(p, new svm_node());
                           ((svm_node)this.xlist.get(p)).value = 1.0D;
                           ((svm_node)this.xlist.get(p)).index = Integer.parseInt(items[k]) + offset;
                           ++p;
                        }
                     } catch (NumberFormatException var18) {
                        throw new LibsvmException("The instance file contain a non-integer value '" + items[k] + "'", var18);
                     }
                  }

                  offset += cardinalities[j - 1];
               }

               prob.x[i] = (svm_node[])this.xlist.subList(0, p).toArray(new svm_node[0]);
               if (columns.length > 1) {
                  max_index = Math.max(max_index, ((svm_node)this.xlist.get(p - 1)).index);
               }

               ++i;
               this.xlist.clear();
            } catch (ArrayIndexOutOfBoundsException var19) {
               throw new LibsvmException("Cannot read from the instance file. ", var19);
            }
         }
      } catch (IOException var20) {
         throw new LibsvmException("Cannot read from the instance file. ", var20);
      }
   }

   protected void initSpecialParameters() throws MaltChainedException {
      if (this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none")) {
         this.excludeNullValues = true;
      } else {
         this.excludeNullValues = false;
      }

      this.saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("libsvm", "save_instance_files");
      if (!this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString().equals("")) {
         try {
            if (!(new File(this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString())).exists()) {
               throw new LibsvmException("The path to the external LIBSVM trainer 'svm-train' is wrong.");
            }

            if ((new File(this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString())).isDirectory()) {
               throw new LibsvmException("The option --libsvm-libsvm_external points to a directory, the path should point at the 'svm-train' file or the 'svm-train.exe' file");
            }

            if (!this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString().endsWith("svm-train") && !this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString().endsWith("svm-train.exe")) {
               throw new LibsvmException("The option --libsvm-libsvm_external does not specify the path to 'svm-train' file or the 'svm-train.exe' file. ");
            }

            this.pathExternalSVMTrain = this.getConfiguration().getOptionValue("libsvm", "libsvm_external").toString();
         } catch (SecurityException var2) {
            throw new LibsvmException("Access denied to the file specified by the option --libsvm-libsvm_external. ", var2);
         }
      }

      if (this.getConfiguration().getOptionValue("libsvm", "verbosity") != null) {
         this.verbosity = Libsvm.Verbostity.valueOf(this.getConfiguration().getOptionValue("libsvm", "verbosity").toString().toUpperCase());
      }

   }

   protected void initParameters(svm_parameter param) throws MaltChainedException {
      if (param == null) {
         throw new LibsvmException("Svm-parameters cannot be found. ");
      } else {
         param.svm_type = 0;
         param.kernel_type = 1;
         param.degree = 2;
         param.gamma = 0.2D;
         param.coef0 = 0.0D;
         param.nu = 0.5D;
         param.cache_size = 100.0D;
         param.C = 1.0D;
         param.eps = 1.0D;
         param.p = 0.1D;
         param.shrinking = 1;
         param.probability = 0;
         param.nr_weight = 0;
         param.weight_label = new int[0];
         param.weight = new double[0];
      }
   }

   public String toStringParameters(svm_parameter param) {
      if (param == null) {
         throw new IllegalArgumentException("Svm-parameters cannot be found. ");
      } else {
         StringBuffer sb = new StringBuffer();
         String[] svmtypes = new String[]{"C_SVC", "NU_SVC", "ONE_CLASS", "EPSILON_SVR", "NU_SVR"};
         String[] kerneltypes = new String[]{"LINEAR", "POLY", "RBF", "SIGMOID", "PRECOMPUTED"};
         DecimalFormat dform = new DecimalFormat("#0.0#");
         DecimalFormatSymbols sym = new DecimalFormatSymbols();
         sym.setDecimalSeparator('.');
         dform.setDecimalFormatSymbols(sym);
         sb.append("LIBSVM SETTINGS\n");
         sb.append("  SVM type      : " + svmtypes[param.svm_type] + " (" + param.svm_type + ")\n");
         sb.append("  Kernel        : " + kerneltypes[param.kernel_type] + " (" + param.kernel_type + ")\n");
         if (param.kernel_type == 1) {
            sb.append("  Degree        : " + param.degree + "\n");
         }

         if (param.kernel_type == 1 || param.kernel_type == 2 || param.kernel_type == 3) {
            sb.append("  Gamma         : " + dform.format(param.gamma) + "\n");
            if (param.kernel_type == 1 || param.kernel_type == 3) {
               sb.append("  Coef0         : " + dform.format(param.coef0) + "\n");
            }
         }

         if (param.svm_type == 1 || param.svm_type == 4 || param.svm_type == 2) {
            sb.append("  Nu            : " + dform.format(param.nu) + "\n");
         }

         sb.append("  Cache Size    : " + dform.format(param.cache_size) + " MB\n");
         if (param.svm_type == 0 || param.svm_type == 4 || param.svm_type == 3) {
            sb.append("  C             : " + dform.format(param.C) + "\n");
         }

         sb.append("  Eps           : " + dform.format(param.eps) + "\n");
         if (param.svm_type == 3) {
            sb.append("  P             : " + dform.format(param.p) + "\n");
         }

         sb.append("  Shrinking     : " + param.shrinking + "\n");
         sb.append("  Probability   : " + param.probability + "\n");
         if (param.svm_type == 0) {
            sb.append("  #Weight       : " + param.nr_weight + "\n");
            if (param.nr_weight > 0) {
               sb.append("  Weight labels : ");

               int i;
               for(i = 0; i < param.nr_weight; ++i) {
                  sb.append(param.weight_label[i]);
                  if (i != param.nr_weight - 1) {
                     sb.append(", ");
                  }
               }

               sb.append("\n");

               for(i = 0; i < param.nr_weight; ++i) {
                  sb.append(dform.format(param.weight));
                  if (i != param.nr_weight - 1) {
                     sb.append(", ");
                  }
               }

               sb.append("\n");
            }
         }

         return sb.toString();
      }
   }

   public String[] getSVMParamStringArray(svm_parameter param) {
      ArrayList<String> params = new ArrayList();
      if (param.svm_type != 0) {
         params.add("-s");
         params.add((new Integer(param.svm_type)).toString());
      }

      if (param.kernel_type != 2) {
         params.add("-t");
         params.add((new Integer(param.kernel_type)).toString());
      }

      if (param.degree != 3) {
         params.add("-d");
         params.add((new Integer(param.degree)).toString());
      }

      params.add("-g");
      params.add((new Double(param.gamma)).toString());
      if (param.coef0 != 0.0D) {
         params.add("-r");
         params.add((new Double(param.coef0)).toString());
      }

      if (param.nu != 0.5D) {
         params.add("-n");
         params.add((new Double(param.nu)).toString());
      }

      if (param.cache_size != 100.0D) {
         params.add("-m");
         params.add((new Double(param.cache_size)).toString());
      }

      if (param.C != 1.0D) {
         params.add("-c");
         params.add((new Double(param.C)).toString());
      }

      if (param.eps != 0.001D) {
         params.add("-e");
         params.add((new Double(param.eps)).toString());
      }

      if (param.p != 0.1D) {
         params.add("-p");
         params.add((new Double(param.p)).toString());
      }

      if (param.shrinking != 1) {
         params.add("-h");
         params.add((new Integer(param.shrinking)).toString());
      }

      if (param.probability != 0) {
         params.add("-b");
         params.add((new Integer(param.probability)).toString());
      }

      return (String[])params.toArray(new String[params.size()]);
   }

   public void parseParameters(String paramstring, svm_parameter param) throws MaltChainedException {
      if (param == null) {
         throw new LibsvmException("Svm-parameters cannot be found. ");
      } else if (paramstring != null) {
         String[] argv;
         try {
            argv = paramstring.split("[_\\p{Blank}]");
         } catch (PatternSyntaxException var9) {
            throw new LibsvmException("Could not split the svm-parameter string '" + paramstring + "'. ", var9);
         }

         for(int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
               throw new LibsvmException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }

            ++i;
            if (i >= argv.length) {
               throw new LibsvmException("The last argument does not have any value. ");
            }

            try {
               switch(argv[i - 1].charAt(1)) {
               case 'F':
               case 'M':
               case 'N':
               case 'S':
               case 'T':
               case 'V':
               case 'Y':
                  break;
               case 'G':
               case 'H':
               case 'I':
               case 'J':
               case 'K':
               case 'L':
               case 'O':
               case 'P':
               case 'Q':
               case 'R':
               case 'U':
               case 'W':
               case 'X':
               case 'Z':
               case '[':
               case '\\':
               case ']':
               case '^':
               case '_':
               case '`':
               case 'a':
               case 'f':
               case 'i':
               case 'j':
               case 'k':
               case 'l':
               case 'o':
               case 'q':
               case 'u':
               case 'v':
               default:
                  throw new LibsvmException("Unknown svm parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
               case 'b':
                  param.probability = Integer.parseInt(argv[i]);
                  break;
               case 'c':
                  param.C = Double.valueOf(argv[i]);
                  break;
               case 'd':
                  param.degree = Integer.parseInt(argv[i]);
                  break;
               case 'e':
                  param.eps = Double.valueOf(argv[i]);
                  break;
               case 'g':
                  param.gamma = Double.valueOf(argv[i]);
                  break;
               case 'h':
                  param.shrinking = Integer.parseInt(argv[i]);
                  break;
               case 'm':
                  param.cache_size = Double.valueOf(argv[i]);
                  break;
               case 'n':
                  param.nu = Double.valueOf(argv[i]);
                  break;
               case 'p':
                  param.p = Double.valueOf(argv[i]);
                  break;
               case 'r':
                  param.coef0 = Double.valueOf(argv[i]);
                  break;
               case 's':
                  param.svm_type = Integer.parseInt(argv[i]);
                  break;
               case 't':
                  param.kernel_type = Integer.parseInt(argv[i]);
                  break;
               case 'w':
                  ++param.nr_weight;
                  int[] old = param.weight_label;
                  param.weight_label = new int[param.nr_weight];
                  System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
                  double[] old = param.weight;
                  param.weight = new double[param.nr_weight];
                  System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
                  param.weight_label[param.nr_weight - 1] = Integer.parseInt(argv[i].substring(2));
                  param.weight[param.nr_weight - 1] = Double.valueOf(argv[i]);
               }
            } catch (ArrayIndexOutOfBoundsException var6) {
               throw new LibsvmException("The svm-parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var6);
            } catch (NumberFormatException var7) {
               throw new LibsvmException("The svm-parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var7);
            } catch (NullPointerException var8) {
               throw new LibsvmException("The svm-parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var8);
            }
         }

      }
   }

   public void svm_predict_with_kbestlist(svm_model model, svm_node[] x, KBestList kBestList) throws MaltChainedException {
      int nr_class = svm.svm_get_nr_class(model);
      double[] dec_values = new double[nr_class * (nr_class - 1) / 2];
      svm.svm_predict_values(model, x, dec_values);
      int[] vote = new int[nr_class];
      double[] score = new double[nr_class];
      int[] voteindex = new int[nr_class];

      int i;
      for(i = 0; i < nr_class; voteindex[i] = i++) {
         vote[i] = 0;
         score[i] = 0.0D;
      }

      int pos = 0;

      int lagest;
      for(i = 0; i < nr_class; ++i) {
         for(lagest = i + 1; lagest < nr_class; ++lagest) {
            int var10002;
            if (dec_values[pos] > 0.0D) {
               var10002 = vote[i]++;
            } else {
               var10002 = vote[lagest]++;
            }

            score[i] += dec_values[pos];
            score[lagest] += dec_values[pos];
            ++pos;
         }
      }

      for(i = 0; i < nr_class; ++i) {
         score[i] /= (double)nr_class;
      }

      for(i = 0; i < nr_class - 1; ++i) {
         lagest = i;

         for(int j = i; j < nr_class; ++j) {
            if (vote[j] > vote[lagest]) {
               lagest = j;
            }
         }

         int tmpint = vote[lagest];
         vote[lagest] = vote[i];
         vote[i] = tmpint;
         double tmpdouble = score[lagest];
         score[lagest] = score[i];
         score[i] = tmpdouble;
         tmpint = voteindex[lagest];
         voteindex[lagest] = voteindex[i];
         voteindex[i] = tmpint;
      }

      int[] labels = new int[nr_class];
      svm.svm_get_labels(model, labels);
      int k = nr_class - 1;
      if (kBestList.getK() != -1) {
         k = kBestList.getK() - 1;
      }

      for(i = 0; i < nr_class && k >= 0; --k) {
         if (vote[i] > 0 || i == 0) {
            if (kBestList instanceof ScoredKBestList) {
               ((ScoredKBestList)kBestList).add(labels[voteindex[i]], (float)vote[i] / (float)(nr_class * (nr_class - 1) / 2));
            } else {
               kBestList.add(labels[voteindex[i]]);
            }
         }

         ++i;
      }

   }

   public static void maltSVMFormat2OriginalSVMFormat(InputStreamReader isr, OutputStreamWriter osw, int[] cardinalities) throws MaltChainedException {
      try {
         BufferedReader in = new BufferedReader(isr);
         BufferedWriter out = new BufferedWriter(osw);
         int j = 0;
         int offset = 0;
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
                              offset = 0;
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
               throw new LibsvmException("The instance file contain a non-integer value, when converting the Malt SVM format into LIBSVM format.");
            }

            code = code * 10 + (c - 48);
         }
      } catch (IOException var9) {
         throw new LibsvmException("Cannot read from the instance file, when converting the Malt SVM format into LIBSVM format. ", var9);
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
      sb.append("\nLIBSVM INTERFACE\n");
      sb.append("  LIBSVM version: 2.91\n");
      sb.append("  SVM-param string: " + this.paramString + "\n");
      sb.append(this.toStringParameters(this.svmParam));
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
