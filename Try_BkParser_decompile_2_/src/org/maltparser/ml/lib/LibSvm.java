/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

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
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.NoPrintStream;
import org.maltparser.ml.lib.FeatureList;
import org.maltparser.ml.lib.FeatureMap;
import org.maltparser.ml.lib.Lib;
import org.maltparser.ml.lib.LibException;
import org.maltparser.ml.lib.MaltFeatureNode;
import org.maltparser.ml.lib.MaltLibModel;
import org.maltparser.ml.lib.MaltLibsvmModel;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.instance.InstanceModel;

public class LibSvm
extends Lib {
    public LibSvm(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
        super(owner, learnerMode, "libsvm");
        if (learnerMode == 1) {
            this.model = (MaltLibModel)this.getConfigFileEntryObject(".moo");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void trainInternal(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        try {
            svm_problem prob = this.readProblem(this.getInstanceInputStreamReader(".ins"), libOptions);
            svm_parameter param = this.getLibSvmParameters(libOptions);
            if (svm.svm_check_parameter(prob, param) != null) {
                throw new LibException(svm.svm_check_parameter(prob, param));
            }
            DependencyParserConfig config = this.getConfiguration();
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
            try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.getFile(".moo").getAbsolutePath())));){
                output.writeObject(new MaltLibsvmModel(model, prob));
            }
            boolean saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("lib", "save_instance_files");
            if (!saveInstanceFiles) {
                this.getFile(".ins").delete();
            }
        }
        catch (OutOfMemoryError e) {
            throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
        }
        catch (IllegalArgumentException e) {
            throw new LibException("The LIBSVM learner was not able to redirect Standard Error stream. ", e);
        }
        catch (SecurityException e) {
            throw new LibException("The LIBSVM learner cannot remove the instance file. ", e);
        }
        catch (IOException e) {
            throw new LibException("The LIBSVM learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void trainExternal(String pathExternalTrain, LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        try {
            int c;
            this.binariesInstances2SVMFileFormat(this.getInstanceInputStreamReader(".ins"), this.getInstanceOutputStreamWriter(".ins.tmp"));
            DependencyParserConfig config = this.getConfiguration();
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("Creating learner model (external) " + this.getFile(".mod").getName());
            }
            svm_problem prob = this.readProblem(this.getInstanceInputStreamReader(".ins"), libOptions);
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
            svm_model model = svm.svm_load_model(this.getFile(".mod").getAbsolutePath());
            MaltLibsvmModel xmodel = new MaltLibsvmModel(model, prob);
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
        libOptions.put("s", Integer.toString(0));
        libOptions.put("t", Integer.toString(1));
        libOptions.put("d", Integer.toString(2));
        libOptions.put("g", Double.toString(0.2));
        libOptions.put("r", Double.toString(0.0));
        libOptions.put("n", Double.toString(0.5));
        libOptions.put("m", Integer.toString(100));
        libOptions.put("c", Double.toString(1.0));
        libOptions.put("e", Double.toString(1.0));
        libOptions.put("p", Double.toString(0.1));
        libOptions.put("h", Integer.toString(1));
        libOptions.put("b", Integer.toString(0));
        return libOptions;
    }

    @Override
    public String getAllowedLibOptionFlags() {
        return "stdgrnmcepb";
    }

    private svm_parameter getLibSvmParameters(LinkedHashMap<String, String> libOptions) throws MaltChainedException {
        svm_parameter param = new svm_parameter();
        param.svm_type = Integer.parseInt(libOptions.get("s"));
        param.kernel_type = Integer.parseInt(libOptions.get("t"));
        param.degree = Integer.parseInt(libOptions.get("d"));
        param.gamma = Double.valueOf(libOptions.get("g"));
        param.coef0 = Double.valueOf(libOptions.get("r"));
        param.nu = Double.valueOf(libOptions.get("n"));
        param.cache_size = Double.valueOf(libOptions.get("m"));
        param.C = Double.valueOf(libOptions.get("c"));
        param.eps = Double.valueOf(libOptions.get("e"));
        param.p = Double.valueOf(libOptions.get("p"));
        param.shrinking = Integer.parseInt(libOptions.get("h"));
        param.probability = Integer.parseInt(libOptions.get("b"));
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
            String line;
            BufferedReader fp = new BufferedReader(isr);
            problem.l = this.getNumberOfInstances();
            problem.x = new svm_node[problem.l][];
            problem.y = new double[problem.l];
            int i = 0;
            while ((line = fp.readLine()) != null) {
                int y = this.binariesInstance(line, featureList);
                if (y == -1) continue;
                try {
                    problem.y[i] = y;
                    problem.x[i] = new svm_node[featureList.size()];
                    int p = 0;
                    for (int k = 0; k < featureList.size(); ++k) {
                        MaltFeatureNode x = featureList.get(k);
                        problem.x[i][p] = new svm_node();
                        problem.x[i][p].value = x.getValue();
                        problem.x[i][p].index = x.getIndex();
                        ++p;
                    }
                    ++i;
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new LibException("Couldn't read libsvm problem from the instance file. ", e);
                }
            }
            fp.close();
            if (param.gamma == 0.0) {
                param.gamma = 1.0 / (double)this.featureMap.getFeatureCounter();
            }
        }
        catch (IOException e) {
            throw new LibException("Couldn't read libsvm problem from the instance file. ", e);
        }
        return problem;
    }
}

