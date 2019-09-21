package org.maltparser.ml.lib;

import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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

public abstract class Lib implements LearningMethod {
   protected final Lib.Verbostity verbosity;
   private final InstanceModel owner;
   private final int learnerMode;
   private final String name;
   protected final FeatureMap featureMap;
   private final boolean excludeNullValues;
   private BufferedWriter instanceOutput = null;
   protected MaltLibModel model = null;
   private int numberOfInstances;

   public Lib(InstanceModel owner, Integer learnerMode, String learningMethodName) throws MaltChainedException {
      this.owner = owner;
      this.learnerMode = learnerMode;
      this.name = learningMethodName;
      if (this.getConfiguration().getOptionValue("lib", "verbosity") != null) {
         this.verbosity = Lib.Verbostity.valueOf(this.getConfiguration().getOptionValue("lib", "verbosity").toString().toUpperCase());
      } else {
         this.verbosity = Lib.Verbostity.SILENT;
      }

      this.setNumberOfInstances(0);
      if (this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none")) {
         this.excludeNullValues = true;
      } else {
         this.excludeNullValues = false;
      }

      if (learnerMode == 0) {
         this.featureMap = new FeatureMap();
         this.instanceOutput = new BufferedWriter(this.getInstanceOutputStreamWriter(".ins"));
      } else if (learnerMode == 1) {
         this.featureMap = (FeatureMap)this.getConfigFileEntryObject(".map");
      } else {
         this.featureMap = null;
      }

   }

   public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
      if (featureVector == null) {
         throw new LibException("The feature vector cannot be found");
      } else if (decision == null) {
         throw new LibException("The decision cannot be found");
      } else {
         try {
            StringBuilder sb = new StringBuilder();
            sb.append(decision.getDecisionCode() + "\t");
            int n = featureVector.size();

            for(int i = 0; i < n; ++i) {
               FeatureValue featureValue = featureVector.getFeatureValue(i);
               if (featureValue != null && (!this.excludeNullValues || !featureValue.isNullValue())) {
                  if (!featureValue.isMultiple()) {
                     SingleFeatureValue singleFeatureValue = (SingleFeatureValue)featureValue;
                     if (singleFeatureValue.getValue() == 1.0D) {
                        sb.append(singleFeatureValue.getIndexCode());
                     } else if (singleFeatureValue.getValue() == 0.0D) {
                        sb.append("-1");
                     } else {
                        sb.append(singleFeatureValue.getIndexCode());
                        sb.append(":");
                        sb.append(singleFeatureValue.getValue());
                     }
                  } else {
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
               } else {
                  sb.append("-1");
               }

               sb.append('\t');
            }

            sb.append('\n');
            this.instanceOutput.write(sb.toString());
            this.instanceOutput.flush();
            this.increaseNumberOfInstances();
         } catch (IOException var11) {
            throw new LibException("The learner cannot write to the instance file. ", var11);
         }
      }
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
   }

   public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
      if (method == null) {
         throw new LibException("The learning method cannot be found. ");
      } else if (divideFeature == null) {
         throw new LibException("The divide feature cannot be found. ");
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
            throw new LibException("The learner cannot remove the instance file. ", var10);
         } catch (NullPointerException var11) {
            throw new LibException("The instance file cannot be found. ", var11);
         } catch (FileNotFoundException var12) {
            throw new LibException("The instance file cannot be found. ", var12);
         } catch (IOException var13) {
            throw new LibException("The learner read from the instance file. ", var13);
         }
      }
   }

   public void noMoreInstances() throws MaltChainedException {
      this.closeInstanceWriter();
   }

   public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      FeatureList featureList = new FeatureList();
      int size = featureVector.size();

      for(int i = 1; i <= size; ++i) {
         FeatureValue featureValue = featureVector.getFeatureValue(i - 1);
         if (featureValue != null && (!this.excludeNullValues || !featureValue.isNullValue())) {
            if (!featureValue.isMultiple()) {
               SingleFeatureValue singleFeatureValue = (SingleFeatureValue)featureValue;
               int index = this.featureMap.getIndex(i, singleFeatureValue.getIndexCode());
               if (index != -1 && singleFeatureValue.getValue() != 0.0D) {
                  featureList.add(index, singleFeatureValue.getValue());
               }
            } else {
               Iterator i$ = ((MultipleFeatureValue)featureValue).getCodes().iterator();

               while(i$.hasNext()) {
                  Integer value = (Integer)i$.next();
                  int v = this.featureMap.getIndex(i, value);
                  if (v != -1) {
                     featureList.add(v, 1.0D);
                  }
               }
            }
         }
      }

      try {
         decision.getKBestList().addList(this.model.predict(featureList.toArray()));
         return true;
      } catch (OutOfMemoryError var10) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var10);
      }
   }

   public void train() throws MaltChainedException {
      if (this.owner == null) {
         throw new LibException("The parent guide model cannot be found. ");
      } else {
         String pathExternalTrain = null;
         if (!this.getConfiguration().getOptionValue("lib", "external").toString().equals("")) {
            String path = this.getConfiguration().getOptionValue("lib", "external").toString();

            try {
               if (!(new File(path)).exists()) {
                  throw new LibException("The path to the external  trainer 'svm-train' is wrong.");
               }

               if ((new File(path)).isDirectory()) {
                  throw new LibException("The option --lib-external points to a directory, the path should point at the 'train' file or the 'train.exe' file in the libsvm or the liblinear package");
               }

               if (!path.endsWith("train") && !path.endsWith("train.exe")) {
                  throw new LibException("The option --lib-external does not specify the path to 'train' file or the 'train.exe' file in the libsvm or the liblinear package. ");
               }

               pathExternalTrain = path;
            } catch (SecurityException var5) {
               throw new LibException("Access denied to the file specified by the option --lib-external. ", var5);
            }
         }

         LinkedHashMap<String, String> libOptions = this.getDefaultLibOptions();
         this.parseParameters(this.getConfiguration().getOptionValue("lib", "options").toString(), libOptions, this.getAllowedLibOptionFlags());
         if (pathExternalTrain != null) {
            this.trainExternal(pathExternalTrain, libOptions);
         } else {
            this.trainInternal(libOptions);
         }

         try {
            this.saveFeatureMap(new BufferedOutputStream(new FileOutputStream(this.getFile(".map").getAbsolutePath())), this.featureMap);
         } catch (FileNotFoundException var4) {
            throw new LibException("The learner cannot save the feature map file '" + this.getFile(".map").getAbsolutePath() + "'. ", var4);
         }
      }
   }

   protected abstract void trainExternal(String var1, LinkedHashMap<String, String> var2) throws MaltChainedException;

   protected abstract void trainInternal(LinkedHashMap<String, String> var1) throws MaltChainedException;

   public void terminate() throws MaltChainedException {
      this.closeInstanceWriter();
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
         throw new LibException("The learner cannot close the instance file. ", var2);
      }
   }

   public InstanceModel getOwner() {
      return this.owner;
   }

   public int getLearnerMode() {
      return this.learnerMode;
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

   protected OutputStreamWriter getInstanceOutputStreamWriter(String suffix) throws MaltChainedException {
      return this.getConfiguration().getAppendOutputStreamWriter(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected InputStreamReader getInstanceInputStreamReader(String suffix) throws MaltChainedException {
      return this.getConfiguration().getInputStreamReader(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected InputStream getInputStreamFromConfigFileEntry(String suffix) throws MaltChainedException {
      return this.getConfiguration().getInputStreamFromConfigFileEntry(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected File getFile(String suffix) throws MaltChainedException {
      return this.getConfiguration().getFile(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   protected Object getConfigFileEntryObject(String suffix) throws MaltChainedException {
      return this.getConfiguration().getConfigFileEntryObject(this.owner.getModelName() + this.getLearningMethodName() + suffix);
   }

   public String[] getLibParamStringArray(LinkedHashMap<String, String> libOptions) {
      ArrayList<String> params = new ArrayList();
      Iterator i$ = libOptions.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         params.add("-" + key);
         params.add(libOptions.get(key));
      }

      return (String[])params.toArray(new String[params.size()]);
   }

   public abstract LinkedHashMap<String, String> getDefaultLibOptions();

   public abstract String getAllowedLibOptionFlags();

   public void parseParameters(String paramstring, LinkedHashMap<String, String> libOptions, String allowedLibOptionFlags) throws MaltChainedException {
      if (paramstring != null) {
         String[] argv;
         try {
            argv = paramstring.split("[_\\p{Blank}]");
         } catch (PatternSyntaxException var7) {
            throw new LibException("Could not split the parameter string '" + paramstring + "'. ", var7);
         }

         for(int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
               throw new LibException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }

            ++i;
            if (i >= argv.length) {
               throw new LibException("The last argument does not have any value. ");
            }

            try {
               int index = allowedLibOptionFlags.indexOf(argv[i - 1].charAt(1));
               if (index == -1) {
                  throw new LibException("Unknown learner parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
               }

               libOptions.put(Character.toString(argv[i - 1].charAt(1)), argv[i]);
            } catch (ArrayIndexOutOfBoundsException var8) {
               throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var8);
            } catch (NumberFormatException var9) {
               throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var9);
            } catch (NullPointerException var10) {
               throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", var10);
            }
         }

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
      sb.append('\n');
      sb.append(this.getLearningMethodName());
      sb.append(" INTERFACE\n");

      try {
         sb.append(this.getConfiguration().getOptionValue("lib", "options").toString());
      } catch (MaltChainedException var3) {
      }

      return sb.toString();
   }

   protected int binariesInstance(String line, FeatureList featureList) throws MaltChainedException {
      Pattern tabPattern = Pattern.compile("\t");
      Pattern pipePattern = Pattern.compile("\\|");
      int y = true;
      featureList.clear();

      try {
         String[] columns = tabPattern.split(line);
         if (columns.length == 0) {
            return -1;
         } else {
            int y;
            try {
               y = Integer.parseInt(columns[0]);
            } catch (NumberFormatException var15) {
               throw new LibException("The instance file contain a non-integer value '" + columns[0] + "'", var15);
            }

            for(int j = 1; j < columns.length; ++j) {
               String[] items = pipePattern.split(columns[j]);

               for(int k = 0; k < items.length; ++k) {
                  try {
                     int colon = items[k].indexOf(58);
                     int v;
                     if (colon == -1) {
                        if (Integer.parseInt(items[k]) != -1) {
                           v = this.featureMap.addIndex(j, Integer.parseInt(items[k]));
                           if (v != -1) {
                              featureList.add(v, 1.0D);
                           }
                        }
                     } else {
                        v = this.featureMap.addIndex(j, Integer.parseInt(items[k].substring(0, colon)));
                        double value;
                        if (items[k].substring(colon + 1).indexOf(46) != -1) {
                           value = Double.parseDouble(items[k].substring(colon + 1));
                        } else {
                           value = (double)Integer.parseInt(items[k].substring(colon + 1));
                        }

                        featureList.add(v, value);
                     }
                  } catch (NumberFormatException var14) {
                     throw new LibException("The instance file contain a non-numeric value '" + items[k] + "'", var14);
                  }
               }
            }

            return y;
         }
      } catch (ArrayIndexOutOfBoundsException var16) {
         throw new LibException("Couln't read from the instance file. ", var16);
      }
   }

   protected void binariesInstances2SVMFileFormat(InputStreamReader isr, OutputStreamWriter osw) throws MaltChainedException {
      try {
         BufferedReader in = new BufferedReader(isr);
         BufferedWriter out = new BufferedWriter(osw);
         FeatureList featureSet = new FeatureList();

         while(true) {
            int y;
            do {
               String line = in.readLine();
               if (line == null) {
                  in.close();
                  out.close();
                  return;
               }

               y = this.binariesInstance(line, featureSet);
            } while(y == -1);

            out.write(Integer.toString(y));

            for(int k = 0; k < featureSet.size(); ++k) {
               MaltFeatureNode x = featureSet.get(k);
               out.write(32);
               out.write(Integer.toString(x.getIndex()));
               out.write(58);
               out.write(Double.toString(x.getValue()));
            }

            out.write(10);
         }
      } catch (NumberFormatException var10) {
         throw new LibException("The instance file contain a non-numeric value", var10);
      } catch (IOException var11) {
         throw new LibException("Couldn't read from the instance file, when converting the Malt instances into LIBSVM/LIBLINEAR format. ", var11);
      }
   }

   protected void saveFeatureMap(OutputStream os, FeatureMap map) throws MaltChainedException {
      try {
         ObjectOutputStream output = new ObjectOutputStream(os);

         try {
            output.writeObject(map);
         } finally {
            output.close();
         }

      } catch (IOException var8) {
         throw new LibException("Save feature map error", var8);
      }
   }

   protected FeatureMap loadFeatureMap(InputStream is) throws MaltChainedException {
      new FeatureMap();

      try {
         ObjectInputStream input = new ObjectInputStream(is);

         FeatureMap map;
         try {
            map = (FeatureMap)input.readObject();
         } finally {
            input.close();
         }

         return map;
      } catch (ClassNotFoundException var9) {
         throw new LibException("Load feature map error", var9);
      } catch (IOException var10) {
         throw new LibException("Load feature map error", var10);
      }
   }

   public static enum Verbostity {
      SILENT,
      ERROR,
      ALL;

      private Verbostity() {
      }
   }
}
