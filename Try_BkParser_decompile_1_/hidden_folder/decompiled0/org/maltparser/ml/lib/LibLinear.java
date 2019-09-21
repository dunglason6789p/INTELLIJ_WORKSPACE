/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.NoPrintStream;
import org.maltparser.core.helper.Util;
import org.maltparser.ml.lib.FeatureList;
import org.maltparser.ml.lib.FeatureMap;
import org.maltparser.ml.lib.Lib;
import org.maltparser.ml.lib.LibException;
import org.maltparser.ml.lib.MaltFeatureNode;
import org.maltparser.ml.lib.MaltLibModel;
import org.maltparser.ml.lib.MaltLiblinearModel;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.instance.InstanceModel;

public class LibLinear
extends Lib {
    public LibLinear(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
        super(owner, learnerMode, "liblinear");
        if (learnerMode == 1) {
            this.model = (MaltLibModel)this.getConfigFileEntryObject(".moo");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void trainInternal(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        block21 : {
            DependencyParserConfig config = this.getConfiguration();
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("Creating Liblinear model " + this.getFile(".moo").getName() + "\n");
            }
            double[] wmodel = null;
            int[] labels = null;
            int nr_class = 0;
            int nr_feature = 0;
            Parameter parameter = this.getLiblinearParameters(libOptions);
            try {
                Problem problem = this.readProblem(this.getInstanceInputStreamReader(".ins"));
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
            }
            catch (OutOfMemoryError e) {
                throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
            }
            catch (IllegalArgumentException e) {
                throw new LibException("The Liblinear learner was not able to redirect Standard Error stream. ", e);
            }
            catch (SecurityException e) {
                throw new LibException("The Liblinear learner cannot remove the instance file. ", e);
            }
            catch (NegativeArraySizeException e) {
                throw new LibException("(The number of training instances * the number of classes) > 2147483647 and this is not supported by LibLinear.", e);
            }
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("- Optimize the memory usage\n");
            }
            MaltLiblinearModel xmodel = null;
            try {
                double[][] wmatrix = this.convert2(wmodel, nr_class, nr_feature);
                xmodel = new MaltLiblinearModel(labels, nr_class, wmatrix.length, wmatrix, parameter.getSolverType());
                if (config.isLoggerInfoEnabled()) {
                    config.logInfoMessage("- Save the Liblinear model " + this.getFile(".moo").getName() + "\n");
                }
            }
            catch (OutOfMemoryError e) {
                throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
            }
            try {
                if (xmodel == null) break block21;
                try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));){
                    output.writeObject(xmodel);
                }
            }
            catch (OutOfMemoryError e) {
                throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
            }
            catch (IllegalArgumentException e) {
                throw new LibException("The Liblinear learner was not able to redirect Standard Error stream. ", e);
            }
            catch (SecurityException e) {
                throw new LibException("The Liblinear learner cannot remove the instance file. ", e);
            }
            catch (IOException e) {
                throw new LibException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", e);
            }
        }
    }

    private double[][] convert2(double[] w, int nr_class, int nr_feature) {
        int[] wlength = new int[nr_feature];
        int nr_nfeature = 0;
        for (int i = 0; i < nr_feature; ++i) {
            int k = nr_class;
            int t = i * nr_class;
            while (t + (k - 1) >= t && w[t + k - 1] == 0.0) {
                --k;
            }
            int b = k;
            if (b != 0) {
                int t2 = i * nr_class;
                while (t2 + (b - 1) >= t2 && (b == k || w[t2 + b - 1] == w[t2 + b])) {
                    --b;
                }
            }
            if (k == 0 || b == 0) {
                wlength[i] = 0;
                continue;
            }
            wlength[i] = k;
            ++nr_nfeature;
        }
        double[][] wmatrix = new double[nr_nfeature][];
        double[] wsignature = new double[nr_nfeature];
        Long[] reverseMap = this.featureMap.reverseMap();
        int in = 0;
        for (int i = 0; i < nr_feature; ++i) {
            int j;
            if (wlength[i] == 0) {
                this.featureMap.removeIndex(reverseMap[i + 1]);
                reverseMap[i + 1] = null;
                continue;
            }
            boolean reuse = false;
            double[] copy = new double[wlength[i]];
            System.arraycopy(w, i * nr_class, copy, 0, wlength[i]);
            this.featureMap.setIndex(reverseMap[i + 1], in + 1);
            for (j = 0; j < copy.length; ++j) {
                double[] arrd = wsignature;
                int n = in;
                arrd[n] = arrd[n] + copy[j];
            }
            for (j = 0; j < in; ++j) {
                if (wsignature[j] != wsignature[in] || !Util.equals(copy, wmatrix[j])) continue;
                wmatrix[in] = wmatrix[j];
                reuse = true;
                break;
            }
            if (!reuse) {
                wmatrix[in] = copy;
            }
            ++in;
        }
        this.featureMap.setFeatureCounter(nr_nfeature);
        return wmatrix;
    }

    public static boolean eliminate(double[] a) {
        if (a.length == 0) {
            return true;
        }
        for (int i = 1; i < a.length; ++i) {
            if (a[i] == a[i - 1]) continue;
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void trainExternal(String pathExternalTrain, LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        try {
            int c;
            DependencyParserConfig config = this.getConfiguration();
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("Creating liblinear model (external) " + this.getFile(".mod").getName());
            }
            this.binariesInstances2SVMFileFormat(this.getInstanceInputStreamReader(".ins"), this.getInstanceOutputStreamWriter(".ins.tmp"));
            String[] params = this.getLibParamStringArray(libOptions);
            String[] arrayCommands = new String[params.length + 3];
            int i = 0;
            arrayCommands[i++] = pathExternalTrain;
            while (i <= params.length) {
                arrayCommands[i] = params[i - 1];
                ++i;
            }
            arrayCommands[i++] = this.getFile(".ins.tmp").getAbsolutePath();
            arrayCommands[i++] = this.getFile(".mod").getAbsolutePath();
            if (this.verbosity == Lib.Verbostity.ALL) {
                config.logInfoMessage('\n');
            }
            Process child = Runtime.getRuntime().exec(arrayCommands);
            InputStream in = child.getInputStream();
            InputStream err = child.getErrorStream();
            while ((c = in.read()) != -1) {
                if (this.verbosity != Lib.Verbostity.ALL) continue;
                config.logInfoMessage((char)c);
            }
            while ((c = err.read()) != -1) {
                if (this.verbosity != Lib.Verbostity.ALL && this.verbosity != Lib.Verbostity.ERROR) continue;
                config.logInfoMessage((char)c);
            }
            if (child.waitFor() != 0) {
                config.logErrorMessage(" FAILED (" + child.exitValue() + ")");
            }
            in.close();
            err.close();
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("\nSaving Liblinear model " + this.getFile(".moo").getName() + "\n");
            }
            MaltLiblinearModel xmodel = new MaltLiblinearModel(this.getFile(".mod"));
            try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));){
                output.writeObject(xmodel);
            }
            boolean saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
            if (!saveInstanceFiles) {
                this.getFile(".ins").delete();
                this.getFile(".mod").delete();
                this.getFile(".ins.tmp").delete();
            }
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage('\n');
            }
        }
        catch (InterruptedException e) {
            throw new LibException("Learner is interrupted. ", e);
        }
        catch (IllegalArgumentException e) {
            throw new LibException("The learner was not able to redirect Standard Error stream. ", e);
        }
        catch (SecurityException e) {
            throw new LibException("The learner cannot remove the instance file. ", e);
        }
        catch (IOException e) {
            throw new LibException("The learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", e);
        }
        catch (OutOfMemoryError e) {
            throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
        }
    }

    @Override
    public void terminate() throws MaltChainedException {
        super.terminate();
    }

    @Override
    public LinkedHashMap<String, String> getDefaultLibOptions() {
        LinkedHashMap<String, String> libOptions = new LinkedHashMap<String, String>();
        libOptions.put("s", "4");
        libOptions.put("c", "0.1");
        libOptions.put("e", "0.1");
        libOptions.put("B", "-1");
        return libOptions;
    }

    @Override
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
            String line;
            BufferedReader fp = new BufferedReader(isr);
            problem.bias = -1.0;
            problem.l = this.getNumberOfInstances();
            problem.x = new FeatureNode[problem.l][];
            problem.y = new int[problem.l];
            int i = 0;
            while ((line = fp.readLine()) != null) {
                int y = this.binariesInstance(line, featureList);
                if (y == -1) continue;
                try {
                    problem.y[i] = y;
                    problem.x[i] = new FeatureNode[featureList.size()];
                    int p = 0;
                    for (int k = 0; k < featureList.size(); ++k) {
                        MaltFeatureNode x = featureList.get(k);
                        problem.x[i][p++] = new FeatureNode(x.getIndex(), x.getValue());
                    }
                    ++i;
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new LibException("Couldn't read liblinear problem from the instance file. ", e);
                }
            }
            fp.close();
            problem.n = this.featureMap.size();
        }
        catch (IOException e) {
            throw new LibException("Cannot read from the instance file. ", e);
        }
        return problem;
    }

    private boolean checkProblem(Problem problem) throws MaltChainedException {
        int max_y = problem.y[0];
        for (int i = 1; i < problem.y.length; ++i) {
            if (problem.y[i] <= max_y) continue;
            max_y = problem.y[i];
        }
        if (max_y * problem.l < 0) {
            if (this.getConfiguration().isLoggerInfoEnabled()) {
                this.getConfiguration().logInfoMessage("*** Abort (The number of training instances * the number of classes) > Max array size: (" + problem.l + " * " + max_y + ") > " + Integer.MAX_VALUE + " and this is not supported by LibLinear.\n");
            }
            return false;
        }
        return true;
    }

    private Parameter getLiblinearParameters(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        Parameter param = new Parameter(SolverType.MCSVM_CS, 0.1, 0.1);
        String type = libOptions.get("s");
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
        } else if (type.equals("7")) {
            param.setSolverType(SolverType.L2R_LR_DUAL);
        } else {
            throw new LibException("The liblinear type (-s) is not an integer value between 0 and 4. ");
        }
        try {
            param.setC(Double.valueOf(libOptions.get("c")));
        }
        catch (NumberFormatException e) {
            throw new LibException("The liblinear cost (-c) value is not numerical value. ", e);
        }
        try {
            param.setEps(Double.valueOf(libOptions.get("e")));
        }
        catch (NumberFormatException e) {
            throw new LibException("The liblinear epsilon (-e) value is not numerical value. ", e);
        }
        return param;
    }
}

