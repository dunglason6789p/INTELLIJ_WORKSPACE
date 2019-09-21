/*
 * Decompiled with CFR 0.146.
 */
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
import org.maltparser.ml.liblinear.LiblinearException;
import org.maltparser.ml.liblinear.XNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;
import org.maltparser.parser.history.kbest.ScoredKBestList;

public class Liblinear
implements LearningMethod {
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
    private Verbostity verbosity;
    private HashMap<Long, Integer> featureMap;
    private int featureCounter = 1;
    private boolean featurePruning = false;
    private TreeSet<XNode> featureSet;

    public Liblinear(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
        this.setOwner(owner);
        this.setLearningMethodName("liblinear");
        this.setLearnerMode(learnerMode);
        this.setNumberOfInstances(0);
        this.verbosity = Verbostity.SILENT;
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
            return this.featureMap.get(key);
        }
        int value = this.featureCounter++;
        this.featureMap.put(key, value);
        return value;
    }

    private int getFeatureMapValue(int featurePosition, int code) {
        long key = (long)featurePosition << 48 | (long)code;
        if (this.featureMap.containsKey(key)) {
            return this.featureMap.get(key);
        }
        return -1;
    }

    private void saveFeatureMap(OutputStream os, HashMap<Long, Integer> map) throws MaltChainedException {
        try {
            ObjectOutputStream obj_out_stream = new ObjectOutputStream(os);
            obj_out_stream.writeObject(map);
            obj_out_stream.close();
        }
        catch (IOException e) {
            throw new LiblinearException("Save feature map error", e);
        }
    }

    private HashMap<Long, Integer> loadFeatureMap(InputStream is) throws MaltChainedException {
        HashMap map = new HashMap();
        try {
            ObjectInputStream obj_in_stream = new ObjectInputStream(is);
            map = (HashMap)obj_in_stream.readObject();
            obj_in_stream.close();
        }
        catch (ClassNotFoundException e) {
            throw new LiblinearException("Load feature map error", e);
        }
        catch (IOException e) {
            throw new LiblinearException("Load feature map error", e);
        }
        return map;
    }

    @Override
    public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
        if (featureVector == null) {
            throw new LiblinearException("The feature vector cannot be found");
        }
        if (decision == null) {
            throw new LiblinearException("The decision cannot be found");
        }
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(decision.getDecisionCode() + "\t");
            int n = featureVector.size();
            for (int i = 0; i < n; ++i) {
                FeatureValue featureValue = ((FeatureFunction)featureVector.get(i)).getFeatureValue();
                if (this.excludeNullValues && featureValue.isNullValue()) {
                    sb.append("-1");
                } else if (featureValue instanceof SingleFeatureValue) {
                    sb.append(((SingleFeatureValue)featureValue).getIndexCode() + "");
                } else if (featureValue instanceof MultipleFeatureValue) {
                    Set<Integer> values = ((MultipleFeatureValue)featureValue).getCodes();
                    int j = 0;
                    for (Integer value : values) {
                        sb.append(value.toString());
                        if (j != values.size() - 1) {
                            sb.append("|");
                        }
                        ++j;
                    }
                }
                sb.append('\t');
            }
            sb.append('\n');
            this.instanceOutput.write(sb.toString());
            this.instanceOutput.flush();
            this.increaseNumberOfInstances();
        }
        catch (IOException e) {
            throw new LiblinearException("The Liblinear learner cannot write to the instance file. ", e);
        }
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void noMoreInstances() throws MaltChainedException {
        this.closeInstanceWriter();
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    @Override
    public void train() throws MaltChainedException {
        if (this.owner == null) {
            throw new LiblinearException("The parent guide model cannot be found. ");
        }
        if (this.pathExternalLiblinearTrain == null) {
            try {
                problem = null;
                if (this.featurePruning) {
                    problem = this.readLibLinearProblemWithFeaturePruning(this.getInstanceInputStreamReader(".ins"));
                }
                if ((config = this.owner.getGuide().getConfiguration()).isLoggerInfoEnabled()) {
                    config.logInfoMessage("Creating Liblinear model " + this.getFile(".mod").getName() + "\n");
                }
                out = System.out;
                err = System.err;
                System.setOut(NoPrintStream.NO_PRINTSTREAM);
                System.setErr(NoPrintStream.NO_PRINTSTREAM);
                Linear.saveModel(new File(this.getFile(".mod").getAbsolutePath()), Linear.train(problem, this.getLiblinearParameters()));
                System.setOut(err);
                System.setOut(out);
                if (this.saveInstanceFiles) ** GOTO lbl30
                this.getFile(".ins").delete();
            }
            catch (OutOfMemoryError e) {
                throw new LiblinearException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
            }
            catch (IllegalArgumentException e) {
                throw new LiblinearException("The Liblinear learner was not able to redirect Standard Error stream. ", e);
            }
            catch (SecurityException e) {
                throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", e);
            }
            catch (IOException e) {
                throw new LiblinearException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", e);
            }
        } else {
            this.trainExternal();
        }
lbl30: // 3 sources:
        if (this.featurePruning == false) return;
        try {
            this.saveFeatureMap(new FileOutputStream(this.getFile(".map").getAbsolutePath()), this.featureMap);
            return;
        }
        catch (FileNotFoundException e) {
            throw new LiblinearException("The Liblinear learner cannot save the feature map file '" + this.getFile(".map").getAbsolutePath() + "'. ", e);
        }
    }

    private void trainExternal() throws MaltChainedException {
        try {
            int c;
            DependencyParserConfig config = this.owner.getGuide().getConfiguration();
            if (config.isLoggerInfoEnabled()) {
                config.logInfoMessage("Creating Liblinear model (external) " + this.getFile(".mod").getName());
            }
            String[] params = this.getLibLinearParamStringArray();
            String[] arrayCommands = new String[params.length + 3];
            int i = 0;
            arrayCommands[i++] = this.pathExternalLiblinearTrain;
            while (i <= params.length) {
                arrayCommands[i] = params[i - 1];
                ++i;
            }
            arrayCommands[i++] = this.getFile(".ins.tmp").getAbsolutePath();
            arrayCommands[i++] = this.getFile(".mod").getAbsolutePath();
            if (this.verbosity == Verbostity.ALL) {
                config.logInfoMessage('\n');
            }
            Process child = Runtime.getRuntime().exec(arrayCommands);
            InputStream in = child.getInputStream();
            InputStream err = child.getErrorStream();
            while ((c = in.read()) != -1) {
                if (this.verbosity != Verbostity.ALL) continue;
                config.logInfoMessage((char)c);
            }
            while ((c = err.read()) != -1) {
                if (this.verbosity != Verbostity.ALL && this.verbosity != Verbostity.ERROR) continue;
                config.logInfoMessage((char)c);
            }
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
        }
        catch (InterruptedException e) {
            throw new LiblinearException("Liblinear is interrupted. ", e);
        }
        catch (IllegalArgumentException e) {
            throw new LiblinearException("The Liblinear learner was not able to redirect Standard Error stream. ", e);
        }
        catch (SecurityException e) {
            throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", e);
        }
        catch (IOException e) {
            throw new LiblinearException("The Liblinear learner cannot save the model file '" + this.getFile(".mod").getAbsolutePath() + "'. ", e);
        }
        catch (OutOfMemoryError e) {
            throw new LiblinearException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
        }
    }

    @Override
    public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
        if (method == null) {
            throw new LiblinearException("The learning method cannot be found. ");
        }
        if (divideFeature == null) {
            throw new LiblinearException("The divide feature cannot be found. ");
        }
        try {
            BufferedReader in = new BufferedReader(this.getInstanceInputStreamReader(".ins"));
            BufferedWriter out = method.getInstanceWriter();
            StringBuilder sb = new StringBuilder(6);
            int l = in.read();
            int j = 0;
            do {
                if (l == -1) break;
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
                    continue;
                }
                if (c == '\n') {
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
                    continue;
                }
                sb.append(c);
            } while (true);
            sb.setLength(0);
            in.close();
            this.getFile(".ins").delete();
            out.flush();
        }
        catch (SecurityException e) {
            throw new LiblinearException("The Liblinear learner cannot remove the instance file. ", e);
        }
        catch (NullPointerException e) {
            throw new LiblinearException("The instance file cannot be found. ", e);
        }
        catch (FileNotFoundException e) {
            throw new LiblinearException("The instance file cannot be found. ", e);
        }
        catch (IOException e) {
            throw new LiblinearException("The Liblinear learner read from the instance file. ", e);
        }
    }

    @Override
    public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        if (this.model == null) {
            try {
                this.model = Linear.loadModel(new BufferedReader(this.getInstanceInputStreamReaderFromConfigFile(".mod")));
            }
            catch (IOException e) {
                throw new LiblinearException("The model cannot be loaded. ", e);
            }
        }
        if (this.model == null) {
            throw new LiblinearException("The Liblinear learner cannot predict the next class, because the learning model cannot be found. ");
        }
        if (featureVector == null) {
            throw new LiblinearException("The Liblinear learner cannot predict the next class, because the feature vector cannot be found. ");
        }
        if (this.featurePruning) {
            return this.predictWithFeaturePruning(featureVector, decision);
        }
        if (this.xlist == null) {
            this.xlist = new ArrayList(featureVector.size());
        }
        FeatureNode[] xarray = new FeatureNode[this.xlist.size()];
        for (int k = 0; k < this.xlist.size(); ++k) {
            xarray[k] = this.xlist.get(k);
        }
        if (decision.getKBestList().getK() == 1) {
            decision.getKBestList().add(Linear.predict(this.model, xarray));
        } else {
            this.liblinear_predict_with_kbestlist(this.model, xarray, decision.getKBestList());
        }
        this.xlist.clear();
        return true;
    }

    public boolean predictWithFeaturePruning(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        if (this.featureMap == null) {
            this.featureMap = this.loadFeatureMap(this.getInputStreamFromConfigFileEntry(".map"));
        }
        for (int i = 0; i < featureVector.size(); ++i) {
            FeatureValue featureValue = featureVector.getFeatureValue(i - 1);
            if (this.excludeNullValues && featureValue.isNullValue()) continue;
            if (featureValue instanceof SingleFeatureValue) {
                int v = this.getFeatureMapValue(i, ((SingleFeatureValue)featureValue).getIndexCode());
                if (v == -1) continue;
                this.featureSet.add(new XNode(v, 1.0));
                continue;
            }
            if (!(featureValue instanceof MultipleFeatureValue)) continue;
            for (Integer value : ((MultipleFeatureValue)featureValue).getCodes()) {
                int v = this.getFeatureMapValue(i, value);
                if (v == -1) continue;
                this.featureSet.add(new XNode(v, 1.0));
            }
        }
        FeatureNode[] xarray = new FeatureNode[this.featureSet.size()];
        int k = 0;
        for (XNode x : this.featureSet) {
            xarray[k++] = new FeatureNode(x.getIndex(), x.getValue());
        }
        if (decision.getKBestList().getK() == 1) {
            decision.getKBestList().add(Linear.predict(this.model, xarray));
        } else {
            this.liblinear_predict_with_kbestlist(this.model, xarray, decision.getKBestList());
        }
        this.featureSet.clear();
        return true;
    }

    @Override
    public void terminate() throws MaltChainedException {
        this.closeInstanceWriter();
        this.model = null;
        this.xlist = null;
        this.owner = null;
    }

    @Override
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
        }
        catch (IOException e) {
            throw new LiblinearException("The Liblinear learner cannot close the instance file. ", e);
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
        }
        BufferedReader reader = new BufferedReader(this.getInstanceInputStreamReader(".ins"));
        try {
            while (reader.readLine() != null) {
                ++this.numberOfInstances;
                this.owner.increaseFrequency();
            }
            reader.close();
        }
        catch (IOException e) {
            throw new MaltChainedException("No instances found in file", e);
        }
        return this.numberOfInstances;
    }

    @Override
    public void increaseNumberOfInstances() {
        ++this.numberOfInstances;
        this.owner.increaseFrequency();
    }

    @Override
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
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set UTF-8 is not supported. ", e);
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
            String line;
            BufferedReader fp = new BufferedReader(isr);
            problem.bias = -1.0;
            problem.l = this.getNumberOfInstances();
            problem.x = new FeatureNode[problem.l][];
            problem.y = new int[problem.l];
            int i = 0;
            Pattern tabPattern = Pattern.compile("\t");
            Pattern pipePattern = Pattern.compile("\\|");
            while ((line = fp.readLine()) != null) {
                String[] columns = tabPattern.split(line);
                if (columns.length == 0) continue;
                int j = 0;
                try {
                    problem.y[i] = Integer.parseInt(columns[j]);
                    for (j = 1; j < columns.length; ++j) {
                        String[] items = pipePattern.split(columns[j]);
                        for (int k = 0; k < items.length; ++k) {
                            try {
                                int colon = items[k].indexOf(58);
                                if (colon == -1) {
                                    int v;
                                    if (Integer.parseInt(items[k]) == -1 || (v = this.addFeatureMapValue(j, Integer.parseInt(items[k]))) == -1) continue;
                                    this.featureSet.add(new XNode(v, 1.0));
                                    continue;
                                }
                                int index = this.addFeatureMapValue(j, Integer.parseInt(items[k].substring(0, colon)));
                                double value = items[k].substring(colon + 1).indexOf(46) != -1 ? Double.parseDouble(items[k].substring(colon + 1)) : (double)Integer.parseInt(items[k].substring(colon + 1));
                                this.featureSet.add(new XNode(index, value));
                                continue;
                            }
                            catch (NumberFormatException e) {
                                throw new LiblinearException("The instance file contain a non-integer value '" + items[k] + "'", e);
                            }
                        }
                    }
                    problem.x[i] = new FeatureNode[this.featureSet.size()];
                    int p = 0;
                    for (XNode x : this.featureSet) {
                        problem.x[i][p++] = new FeatureNode(x.getIndex(), x.getValue());
                    }
                    this.featureSet.clear();
                    ++i;
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new LiblinearException("Cannot read from the instance file. ", e);
                }
            }
            fp.close();
            this.featureSet = null;
            problem.n = this.featureMap.size();
            System.out.println("Number of features: " + problem.n);
        }
        catch (IOException e) {
            throw new LiblinearException("Cannot read from the instance file. ", e);
        }
        return problem;
    }

    public Problem readLibLinearProblem(InputStreamReader isr, int[] cardinalities) throws MaltChainedException {
        Problem problem = new Problem();
        try {
            String line;
            BufferedReader fp = new BufferedReader(isr);
            int max_index = 0;
            if (this.xlist == null) {
                this.xlist = new ArrayList();
            }
            problem.bias = -1.0;
            problem.l = this.getNumberOfInstances();
            problem.x = new FeatureNode[problem.l][];
            problem.y = new int[problem.l];
            int i = 0;
            Pattern tabPattern = Pattern.compile("\t");
            Pattern pipePattern = Pattern.compile("\\|");
            while ((line = fp.readLine()) != null) {
                String[] columns = tabPattern.split(line);
                if (columns.length == 0) continue;
                int offset = 1;
                int j = 0;
                try {
                    problem.y[i] = Integer.parseInt(columns[j]);
                    int p = 0;
                    for (j = 1; j < columns.length; ++j) {
                        String[] items = pipePattern.split(columns[j]);
                        for (int k = 0; k < items.length; ++k) {
                            try {
                                if (Integer.parseInt(items[k]) == -1) continue;
                                this.xlist.add(p, new FeatureNode(Integer.parseInt(items[k]) + offset, 1.0));
                                ++p;
                                continue;
                            }
                            catch (NumberFormatException e) {
                                throw new LiblinearException("The instance file contain a non-integer value '" + items[k] + "'", e);
                            }
                        }
                        offset += cardinalities[j - 1];
                    }
                    problem.x[i] = this.xlist.subList(0, p).toArray(new FeatureNode[0]);
                    if (columns.length > 1) {
                        max_index = Math.max(max_index, problem.x[i][p - 1].index);
                    }
                    ++i;
                    this.xlist.clear();
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new LiblinearException("Cannot read from the instance file. ", e);
                }
            }
            fp.close();
            problem.n = max_index;
            System.out.println("Number of features: " + problem.n);
            this.xlist = null;
        }
        catch (IOException e) {
            throw new LiblinearException("Cannot read from the instance file. ", e);
        }
        return problem;
    }

    protected void initSpecialParameters() throws MaltChainedException {
        this.excludeNullValues = this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none");
        this.saveInstanceFiles = (Boolean)this.getConfiguration().getOptionValue("liblinear", "save_instance_files");
        this.featurePruning = true;
        if (!this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().equals("")) {
            try {
                if (!new File(this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString()).exists()) {
                    throw new LiblinearException("The path to the external Liblinear trainer 'svm-train' is wrong.");
                }
                if (new File(this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString()).isDirectory()) {
                    throw new LiblinearException("The option --liblinear-liblinear_external points to a directory, the path should point at the 'train' file or the 'train.exe' file");
                }
                if (!this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().endsWith("train") && !this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString().endsWith("train.exe")) {
                    throw new LiblinearException("The option --liblinear-liblinear_external does not specify the path to 'train' file or the 'train.exe' file. ");
                }
                this.pathExternalLiblinearTrain = this.getConfiguration().getOptionValue("liblinear", "liblinear_external").toString();
            }
            catch (SecurityException e) {
                throw new LiblinearException("Access denied to the file specified by the option --liblinear-liblinear_external. ", e);
            }
        }
        if (this.getConfiguration().getOptionValue("liblinear", "verbosity") != null) {
            this.verbosity = Verbostity.valueOf(this.getConfiguration().getOptionValue("liblinear", "verbosity").toString().toUpperCase());
        }
    }

    public String getLibLinearOptions() {
        StringBuilder sb = new StringBuilder();
        for (String key : this.liblinearOptions.keySet()) {
            sb.append('-');
            sb.append(key);
            sb.append(' ');
            sb.append(this.liblinearOptions.get(key));
            sb.append(' ');
        }
        return sb.toString();
    }

    public void parseParameters(String paramstring) throws MaltChainedException {
        String[] argv;
        if (paramstring == null) {
            return;
        }
        String allowedFlags = "sceB";
        try {
            argv = paramstring.split("[_\\p{Blank}]");
        }
        catch (PatternSyntaxException e) {
            throw new LiblinearException("Could not split the liblinear-parameter string '" + paramstring + "'. ", e);
        }
        for (int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
                throw new LiblinearException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }
            if (++i >= argv.length) {
                throw new LiblinearException("The last argument does not have any value. ");
            }
            try {
                int index = allowedFlags.indexOf(argv[i - 1].charAt(1));
                if (index == -1) {
                    throw new LiblinearException("Unknown liblinear parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
                }
                this.liblinearOptions.put(Character.toString(argv[i - 1].charAt(1)), argv[i]);
                continue;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
            catch (NumberFormatException e) {
                throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
            catch (NullPointerException e) {
                throw new LiblinearException("The liblinear parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
        }
    }

    public double getBias() throws MaltChainedException {
        try {
            return Double.valueOf(this.liblinearOptions.get("B"));
        }
        catch (NumberFormatException e) {
            throw new LiblinearException("The liblinear bias value is not numerical value. ", e);
        }
    }

    public Parameter getLiblinearParameters() throws MaltChainedException {
        Parameter param = new Parameter(SolverType.MCSVM_CS, 0.1, 0.1);
        String type = this.liblinearOptions.get("s");
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
            throw new LiblinearException("The liblinear type (-s) is not an integer value between 0 and 4. ");
        }
        try {
            param.setC(Double.valueOf(this.liblinearOptions.get("c")));
        }
        catch (NumberFormatException e) {
            throw new LiblinearException("The liblinear cost (-c) value is not numerical value. ", e);
        }
        try {
            param.setEps(Double.valueOf(this.liblinearOptions.get("e")));
        }
        catch (NumberFormatException e) {
            throw new LiblinearException("The liblinear epsilon (-e) value is not numerical value. ", e);
        }
        return param;
    }

    public void initLiblinearOptions() {
        this.liblinearOptions.put("s", "4");
        this.liblinearOptions.put("c", "0.1");
        this.liblinearOptions.put("e", "0.1");
        this.liblinearOptions.put("B", "-1");
    }

    public String[] getLibLinearParamStringArray() {
        ArrayList<String> params = new ArrayList<String>();
        for (String key : this.liblinearOptions.keySet()) {
            params.add("-" + key);
            params.add(this.liblinearOptions.get(key));
        }
        return params.toArray(new String[params.size()]);
    }

    public void liblinear_predict_with_kbestlist(Model model, FeatureNode[] x, KBestList kBestList) throws MaltChainedException {
        int i;
        int nr_class = model.getNrClass();
        double[] dec_values = new double[nr_class];
        Linear.predictValues(model, x, dec_values);
        int[] labels = model.getLabels();
        int[] predictionList = new int[nr_class];
        for (i = 0; i < nr_class; ++i) {
            predictionList[i] = labels[i];
        }
        for (i = 0; i < nr_class - 1; ++i) {
            int lagest = i;
            for (int j = i; j < nr_class; ++j) {
                if (!(dec_values[j] > dec_values[lagest])) continue;
                lagest = j;
            }
            double tmpDec = dec_values[lagest];
            dec_values[lagest] = dec_values[i];
            dec_values[i] = tmpDec;
            int tmpObj = predictionList[lagest];
            predictionList[lagest] = predictionList[i];
            predictionList[i] = tmpObj;
        }
        int k = nr_class - 1;
        if (kBestList.getK() != -1) {
            k = kBestList.getK() - 1;
        }
        for (i = 0; i < nr_class && k >= 0; ++i, --k) {
            if (kBestList instanceof ScoredKBestList) {
                ((ScoredKBestList)kBestList).add(predictionList[i], (float)dec_values[i]);
                continue;
            }
            kBestList.add(predictionList[i]);
        }
    }

    public static void maltSVMFormat2OriginalSVMFormat(InputStreamReader isr, OutputStreamWriter osw, int[] cardinalities) throws MaltChainedException {
        try {
            int c;
            BufferedReader in = new BufferedReader(isr);
            BufferedWriter out = new BufferedWriter(osw);
            int j = 0;
            int offset = 1;
            int code = 0;
            while ((c = in.read()) != -1) {
                if (c == 9 || c == 124) {
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
                    continue;
                }
                if (c == 10) {
                    j = 0;
                    offset = 1;
                    out.write(10);
                    code = 0;
                    continue;
                }
                if (c == 45) {
                    code = -1;
                    continue;
                }
                if (code == -1) continue;
                if (c > 47 && c < 58) {
                    code = code * 10 + (c - 48);
                    continue;
                }
                throw new LiblinearException("The instance file contain a non-integer value, when converting the Malt SVM format into Liblinear format.");
            }
            in.close();
            out.close();
        }
        catch (IOException e) {
            throw new LiblinearException("Cannot read from the instance file, when converting the Malt SVM format into Liblinear format. ", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finalize() throws Throwable {
        try {
            this.closeInstanceWriter();
        }
        finally {
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
        
    }

}

