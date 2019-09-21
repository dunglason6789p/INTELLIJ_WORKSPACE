package org.maltparser.ml.lib;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.maltparser.core.config.Configuration;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.NoPrintStream;
import org.maltparser.parser.guide.instance.InstanceModel;

public class LibSvm extends Lib {
   public LibSvm(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
      super(owner, learnerMode, "libsvm");
      if (learnerMode == 1) {
         this.model = (MaltLibModel)this.getConfigFileEntryObject(".moo");
      }

   }

   protected void trainInternal(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      try {
         svm_problem prob = this.readProblem(this.getInstanceInputStreamReader(".ins"), libOptions);
         svm_parameter param = this.getLibSvmParameters(libOptions);
         if (svm.svm_check_parameter(prob, param) != null) {
            throw new LibException(svm.svm_check_parameter(prob, param));
         } else {
            Configuration config = this.getConfiguration();
            if (config.isLoggerInfoEnabled()) {
               config.logInfoMessage("Creating LIBSVM model " + this.getFile(".moo").getName() + "\n");
            }

            PrintStream out = System.out;
            PrintStream err = System.err;
            System.setOut(NoPrintStream.NO_PRINTSTREAM);
            System.setErr(NoPrintStream.NO_PRINTSTREAM);
            svm_model model = svm.svm_train(prob, param);
            System.setOut(err);
            System.setOut(out);
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));

            try {
               output.writeObject(new MaltLibsvmModel(model, prob));
            } finally {
               output.close();
            }

            boolean var9 = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
            if (!var9) {
               this.getFile(".ins").delete();
            }

         }
      } catch (OutOfMemoryError var16) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var16);
      } catch (IllegalArgumentException var17) {
         throw new LibException("The LIBSVM learner was not able to redirect Standard Error stream. ", var17);
      } catch (SecurityException var18) {
         throw new LibException("The LIBSVM learner cannot remove the instance file. ", var18);
      } catch (IOException var19) {
         throw new LibException("The LIBSVM learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var19);
      }
   }

   protected void trainExternal(String pathExternalTrain, LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      try {
         this.binariesInstances2SVMFileFormat(this.getInstanceInputStreamReader(".ins"), this.getInstanceOutputStreamWriter(".ins.tmp"));
         Configuration config = this.getConfiguration();
         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("Creating learner model (external) " + this.getFile(".mod").getName());
         }

         svm_problem prob = this.readProblem(this.getInstanceInputStreamReader(".ins"), libOptions);
         String[] params = this.getLibParamStringArray(libOptions);
         String[] arrayCommands = new String[params.length + 3];
         int i = 0;
         int i = i + 1;

         for(arrayCommands[i] = pathExternalTrain; i <= params.length; ++i) {
            arrayCommands[i] = params[i - 1];
         }

         arrayCommands[i++] = this.getFile(".ins.tmp").getAbsolutePath();
         arrayCommands[i++] = this.getFile(".mod").getAbsolutePath();
         if (this.verbosity == Lib.Verbostity.ALL) {
            config.logInfoMessage('\n');
         }

         Process child = Runtime.getRuntime().exec(arrayCommands);
         InputStream in = child.getInputStream();
         InputStream err = child.getErrorStream();

         int c;
         while((c = in.read()) != -1) {
            if (this.verbosity == Lib.Verbostity.ALL) {
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
                  svm_model model = svm.svm_load_model(this.getFile(".mod").getAbsolutePath());
                  MaltLibsvmModel xmodel = new MaltLibsvmModel(model, prob);
                  ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));

                  try {
                     output.writeObject(xmodel);
                  } finally {
                     output.close();
                  }

                  boolean var15 = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
                  if (!var15) {
                     this.getFile(".ins").delete();
                     this.getFile(".mod").delete();
                     this.getFile(".ins.tmp").delete();
                  }

                  if (config.isLoggerInfoEnabled()) {
                     config.logInfoMessage('\n');
                  }

                  return;
               }
            } while(this.verbosity != Lib.Verbostity.ALL && this.verbosity != Lib.Verbostity.ERROR);

            config.logInfoMessage((char)c);
         }
      } catch (InterruptedException var23) {
         throw new LibException("Learner is interrupted. ", var23);
      } catch (IllegalArgumentException var24) {
         throw new LibException("The learner was not able to redirect Standard Error stream. ", var24);
      } catch (SecurityException var25) {
         throw new LibException("The learner cannot remove the instance file. ", var25);
      } catch (IOException var26) {
         throw new LibException("The learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var26);
      } catch (OutOfMemoryError var27) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var27);
      }
   }

   public void terminate() throws MaltChainedException {
      super.terminate();
   }

   public LinkedHashMap<String, String> getDefaultLibOptions() {
      LinkedHashMap<String, String> libOptions = new LinkedHashMap();
      libOptions.put("s", Integer.toString(0));
      libOptions.put("t", Integer.toString(1));
      libOptions.put("d", Integer.toString(2));
      libOptions.put("g", Double.toString(0.2D));
      libOptions.put("r", Double.toString(0.0D));
      libOptions.put("n", Double.toString(0.5D));
      libOptions.put("m", Integer.toString(100));
      libOptions.put("c", Double.toString(1.0D));
      libOptions.put("e", Double.toString(1.0D));
      libOptions.put("p", Double.toString(0.1D));
      libOptions.put("h", Integer.toString(1));
      libOptions.put("b", Integer.toString(0));
      return libOptions;
   }

   public String getAllowedLibOptionFlags() {
      return "stdgrnmcepb";
   }

   private svm_parameter getLibSvmParameters(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      svm_parameter param = new svm_parameter();
      param.svm_type = Integer.parseInt((String)libOptions.get("s"));
      param.kernel_type = Integer.parseInt((String)libOptions.get("t"));
      param.degree = Integer.parseInt((String)libOptions.get("d"));
      param.gamma = Double.valueOf((String)libOptions.get("g"));
      param.coef0 = Double.valueOf((String)libOptions.get("r"));
      param.nu = Double.valueOf((String)libOptions.get("n"));
      param.cache_size = Double.valueOf((String)libOptions.get("m"));
      param.C = Double.valueOf((String)libOptions.get("c"));
      param.eps = Double.valueOf((String)libOptions.get("e"));
      param.p = Double.valueOf((String)libOptions.get("p"));
      param.shrinking = Integer.parseInt((String)libOptions.get("h"));
      param.probability = Integer.parseInt((String)libOptions.get("b"));
      param.nr_weight = 0;
      param.weight_label = new int[0];
      param.weight = new double[0];
      return param;
   }

   private svm_problem readProblem(InputStreamReader isr, LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      svm_problem problem = new svm_problem();
      svm_parameter param = this.getLibSvmParameters(libOptions);
      FeatureList featureList = new FeatureList();

      try {
         BufferedReader fp = new BufferedReader(isr);
         problem.l = this.getNumberOfInstances();
         problem.x = new svm_node[problem.l][];
         problem.y = new double[problem.l];
         int i = 0;

         while(true) {
            int y;
            do {
               String line = fp.readLine();
               if (line == null) {
                  fp.close();
                  if (param.gamma == 0.0D) {
                     param.gamma = 1.0D / (double)this.featureMap.getFeatureCounter();
                  }

                  return problem;
               }

               y = this.binariesInstance(line, featureList);
            } while(y == -1);

            try {
               problem.y[i] = (double)y;
               problem.x[i] = new svm_node[featureList.size()];
               int p = 0;

               for(int k = 0; k < featureList.size(); ++k) {
                  MaltFeatureNode x = featureList.get(k);
                  problem.x[i][p] = new svm_node();
                  problem.x[i][p].value = x.getValue();
                  problem.x[i][p].index = x.getIndex();
                  ++p;
               }

               ++i;
            } catch (ArrayIndexOutOfBoundsException var13) {
               throw new LibException("Couldn't read libsvm problem from the instance file. ", var13);
            }
         }
      } catch (IOException var14) {
         throw new LibException("Couldn't read libsvm problem from the instance file. ", var14);
      }
   }
}
