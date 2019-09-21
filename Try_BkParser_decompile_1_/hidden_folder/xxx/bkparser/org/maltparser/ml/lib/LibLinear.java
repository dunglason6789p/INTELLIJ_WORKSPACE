package org.maltparser.ml.lib;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import org.maltparser.core.config.Configuration;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.NoPrintStream;
import org.maltparser.core.helper.Util;
import org.maltparser.parser.guide.instance.InstanceModel;

public class LibLinear extends Lib {
   public LibLinear(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
      super(owner, learnerMode, "liblinear");
      if (learnerMode == 1) {
         this.model = (MaltLibModel)this.getConfigFileEntryObject(".moo");
      }

   }

   protected void trainInternal(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      Configuration config = this.getConfiguration();
      if (config.isLoggerInfoEnabled()) {
         config.logInfoMessage("Creating Liblinear model " + this.getFile(".moo").getName() + "\n");
      }

      double[] wmodel = null;
      int[] labels = null;
      int nr_class = false;
      int nr_feature = false;
      Parameter parameter = this.getLiblinearParameters(libOptions);

      Problem problem;
      double[] wmodel;
      int[] labels;
      int nr_class;
      int nr_feature;
      try {
         problem = this.readProblem(this.getInstanceInputStreamReader(".ins"));
         boolean res = this.checkProblem(problem);
         if (!res) {
            throw new LibException("Abort (The number of training instances * the number of classes) > 2147483647 and this is not supported by LibLinear. ");
         }

         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("- Train a parser model using LibLinear.\n");
         }

         PrintStream out = System.out;
         PrintStream err = System.err;
         System.setOut(NoPrintStream.NO_PRINTSTREAM);
         System.setErr(NoPrintStream.NO_PRINTSTREAM);
         Model model = Linear.train(problem, parameter);
         System.setOut(err);
         System.setOut(out);
         problem = null;
         wmodel = model.getFeatureWeights();
         labels = model.getLabels();
         nr_class = model.getNrClass();
         nr_feature = model.getNrFeature();
         boolean saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
         if (!saveInstanceFiles) {
            this.getFile(".ins").delete();
         }
      } catch (OutOfMemoryError var31) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var31);
      } catch (IllegalArgumentException var32) {
         throw new LibException("The Liblinear learner was not able to redirect Standard Error stream. ", var32);
      } catch (SecurityException var33) {
         throw new LibException("The Liblinear learner cannot remove the instance file. ", var33);
      } catch (NegativeArraySizeException var34) {
         throw new LibException("(The number of training instances * the number of classes) > 2147483647 and this is not supported by LibLinear.", var34);
      }

      if (config.isLoggerInfoEnabled()) {
         config.logInfoMessage("- Optimize the memory usage\n");
      }

      problem = null;

      MaltLiblinearModel xmodel;
      try {
         double[][] wmatrix = this.convert2(wmodel, nr_class, nr_feature);
         xmodel = new MaltLiblinearModel(labels, nr_class, wmatrix.length, wmatrix, parameter.getSolverType());
         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("- Save the Liblinear model " + this.getFile(".moo").getName() + "\n");
         }
      } catch (OutOfMemoryError var30) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var30);
      }

      try {
         if (xmodel != null) {
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));

            try {
               output.writeObject(xmodel);
            } finally {
               output.close();
            }
         }

      } catch (OutOfMemoryError var26) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var26);
      } catch (IllegalArgumentException var27) {
         throw new LibException("The Liblinear learner was not able to redirect Standard Error stream. ", var27);
      } catch (SecurityException var28) {
         throw new LibException("The Liblinear learner cannot remove the instance file. ", var28);
      } catch (IOException var29) {
         throw new LibException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var29);
      }
   }

   private double[][] convert2(double[] w, int nr_class, int nr_feature) {
      int[] wlength = new int[nr_feature];
      int nr_nfeature = 0;

      int in;
      for(int i = 0; i < nr_feature; ++i) {
         int k = nr_class;

         int b;
         for(b = i * nr_class; b + (k - 1) >= b && w[b + k - 1] == 0.0D; --k) {
         }

         b = k;
         if (k != 0) {
            for(in = i * nr_class; in + (b - 1) >= in && (b == k || w[in + b - 1] == w[in + b]); --b) {
            }
         }

         if (k != 0 && b != 0) {
            wlength[i] = k;
            ++nr_nfeature;
         } else {
            wlength[i] = 0;
         }
      }

      double[][] wmatrix = new double[nr_nfeature][];
      double[] wsignature = new double[nr_nfeature];
      Long[] reverseMap = this.featureMap.reverseMap();
      in = 0;

      for(int i = 0; i < nr_feature; ++i) {
         if (wlength[i] == 0) {
            this.featureMap.removeIndex(reverseMap[i + 1]);
            reverseMap[i + 1] = null;
         } else {
            boolean reuse = false;
            double[] copy = new double[wlength[i]];
            System.arraycopy(w, i * nr_class, copy, 0, wlength[i]);
            this.featureMap.setIndex(reverseMap[i + 1], in + 1);

            int j;
            for(j = 0; j < copy.length; ++j) {
               wsignature[in] += copy[j];
            }

            for(j = 0; j < in; ++j) {
               if (wsignature[j] == wsignature[in] && Util.equals(copy, wmatrix[j])) {
                  wmatrix[in] = wmatrix[j];
                  reuse = true;
                  break;
               }
            }

            if (!reuse) {
               wmatrix[in] = copy;
            }

            ++in;
         }
      }

      this.featureMap.setFeatureCounter(nr_nfeature);
      return wmatrix;
   }

   public static boolean eliminate(double[] a) {
      if (a.length == 0) {
         return true;
      } else {
         for(int i = 1; i < a.length; ++i) {
            if (a[i] != a[i - 1]) {
               return false;
            }
         }

         return true;
      }
   }

   protected void trainExternal(String pathExternalTrain, LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      try {
         Configuration config = this.getConfiguration();
         if (config.isLoggerInfoEnabled()) {
            config.logInfoMessage("Creating liblinear model (external) " + this.getFile(".mod").getName());
         }

         this.binariesInstances2SVMFileFormat(this.getInstanceInputStreamReader(".ins"), this.getInstanceOutputStreamWriter(".ins.tmp"));
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
                  if (config.isLoggerInfoEnabled()) {
                     config.logInfoMessage("\nSaving Liblinear model " + this.getFile(".moo").getName() + "\n");
                  }

                  MaltLiblinearModel xmodel = new MaltLiblinearModel(this.getFile(".mod"));
                  ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));

                  try {
                     output.writeObject(xmodel);
                  } finally {
                     output.close();
                  }

                  boolean var13 = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
                  if (!var13) {
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
      } catch (InterruptedException var21) {
         throw new LibException("Learner is interrupted. ", var21);
      } catch (IllegalArgumentException var22) {
         throw new LibException("The learner was not able to redirect Standard Error stream. ", var22);
      } catch (SecurityException var23) {
         throw new LibException("The learner cannot remove the instance file. ", var23);
      } catch (IOException var24) {
         throw new LibException("The learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", var24);
      } catch (OutOfMemoryError var25) {
         throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", var25);
      }
   }

   public void terminate() throws MaltChainedException {
      super.terminate();
   }

   public LinkedHashMap<String, String> getDefaultLibOptions() {
      LinkedHashMap<String, String> libOptions = new LinkedHashMap();
      libOptions.put("s", "4");
      libOptions.put("c", "0.1");
      libOptions.put("e", "0.1");
      libOptions.put("B", "-1");
      return libOptions;
   }

   public String getAllowedLibOptionFlags() {
      return "sceB";
   }

   private Problem readProblem(InputStreamReader isr) throws MaltChainedException {
      Problem problem = new Problem();
      FeatureList featureList = new FeatureList();
      if (this.getConfiguration().isLoggerInfoEnabled()) {
         this.getConfiguration().logInfoMessage("- Read all training instances.\n");
      }

      try {
         BufferedReader fp = new BufferedReader(isr);
         problem.bias = -1.0D;
         problem.l = this.getNumberOfInstances();
         problem.x = new FeatureNode[problem.l][];
         problem.y = new int[problem.l];
         int i = 0;

         while(true) {
            int y;
            do {
               String line = fp.readLine();
               if (line == null) {
                  fp.close();
                  problem.n = this.featureMap.size();
                  return problem;
               }

               y = this.binariesInstance(line, featureList);
            } while(y == -1);

            try {
               problem.y[i] = y;
               problem.x[i] = new FeatureNode[featureList.size()];
               int p = 0;

               for(int k = 0; k < featureList.size(); ++k) {
                  MaltFeatureNode x = featureList.get(k);
                  problem.x[i][p++] = new FeatureNode(x.getIndex(), x.getValue());
               }

               ++i;
            } catch (ArrayIndexOutOfBoundsException var11) {
               throw new LibException("Couldn't read liblinear problem from the instance file. ", var11);
            }
         }
      } catch (IOException var12) {
         throw new LibException("Cannot read from the instance file. ", var12);
      }
   }

   private boolean checkProblem(Problem problem) throws MaltChainedException {
      int max_y = problem.y[0];

      for(int i = 1; i < problem.y.length; ++i) {
         if (problem.y[i] > max_y) {
            max_y = problem.y[i];
         }
      }

      if (max_y * problem.l < 0) {
         if (this.getConfiguration().isLoggerInfoEnabled()) {
            this.getConfiguration().logInfoMessage("*** Abort (The number of training instances * the number of classes) > Max array size: (" + problem.l + " * " + max_y + ") > " + 2147483647 + " and this is not supported by LibLinear.\n");
         }

         return false;
      } else {
         return true;
      }
   }

   private Parameter getLiblinearParameters(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
      Parameter param = new Parameter(SolverType.MCSVM_CS, 0.1D, 0.1D);
      String type = (String)libOptions.get("s");
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
      } else if (type.equals("6")) {
         param.setSolverType(SolverType.L1R_LR);
      } else {
         if (!type.equals("7")) {
            throw new LibException("The liblinear type (-s) is not an integer value between 0 and 4. ");
         }

         param.setSolverType(SolverType.L2R_LR_DUAL);
      }

      try {
         param.setC(Double.valueOf((String)libOptions.get("c")));
      } catch (NumberFormatException var6) {
         throw new LibException("The liblinear cost (-c) value is not numerical value. ", var6);
      }

      try {
         param.setEps(Double.valueOf((String)libOptions.get("e")));
         return param;
      } catch (NumberFormatException var5) {
         throw new LibException("The liblinear epsilon (-e) value is not numerical value. ", var5);
      }
   }
}
