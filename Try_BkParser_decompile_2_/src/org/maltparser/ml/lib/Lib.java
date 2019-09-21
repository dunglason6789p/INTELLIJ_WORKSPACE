/*
 * Decompiled with CFR 0.146.
 */
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
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
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
import org.maltparser.ml.lib.FeatureList;
import org.maltparser.ml.lib.FeatureMap;
import org.maltparser.ml.lib.LibException;
import org.maltparser.ml.lib.MaltFeatureNode;
import org.maltparser.ml.lib.MaltLibModel;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;

public abstract class Lib
implements LearningMethod {
    protected final Verbostity verbosity;
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
        this.verbosity = this.getConfiguration().getOptionValue("lib", "verbosity") != null ? Verbostity.valueOf(this.getConfiguration().getOptionValue("lib", "verbosity").toString().toUpperCase()) : Verbostity.SILENT;
        this.setNumberOfInstances(0);
        this.excludeNullValues = this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none");
        if (learnerMode == 0) {
            this.featureMap = new FeatureMap();
            this.instanceOutput = new BufferedWriter(this.getInstanceOutputStreamWriter(".ins"));
        } else {
            this.featureMap = learnerMode == 1 ? (FeatureMap)this.getConfigFileEntryObject(".map") : null;
        }
    }

    @Override
    public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
        if (featureVector == null) {
            throw new LibException("The feature vector cannot be found");
        }
        if (decision == null) {
            throw new LibException("The decision cannot be found");
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(decision.getDecisionCode() + "\t");
            int n = featureVector.size();
            for (int i = 0; i < n; ++i) {
                FeatureValue featureValue = featureVector.getFeatureValue(i);
                if (featureValue == null || this.excludeNullValues && featureValue.isNullValue()) {
                    sb.append("-1");
                } else if (!featureValue.isMultiple()) {
                    SingleFeatureValue singleFeatureValue = (SingleFeatureValue)featureValue;
                    if (singleFeatureValue.getValue() == 1.0) {
                        sb.append(singleFeatureValue.getIndexCode());
                    } else if (singleFeatureValue.getValue() == 0.0) {
                        sb.append("-1");
                    } else {
                        sb.append(singleFeatureValue.getIndexCode());
                        sb.append(":");
                        sb.append(singleFeatureValue.getValue());
                    }
                } else {
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
            throw new LibException("The learner cannot write to the instance file. ", e);
        }
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
        if (method == null) {
            throw new LibException("The learning method cannot be found. ");
        }
        if (divideFeature == null) {
            throw new LibException("The divide feature cannot be found. ");
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
            throw new LibException("The learner cannot remove the instance file. ", e);
        }
        catch (NullPointerException e) {
            throw new LibException("The instance file cannot be found. ", e);
        }
        catch (FileNotFoundException e) {
            throw new LibException("The instance file cannot be found. ", e);
        }
        catch (IOException e) {
            throw new LibException("The learner read from the instance file. ", e);
        }
    }

    @Override
    public void noMoreInstances() throws MaltChainedException {
        this.closeInstanceWriter();
    }

    @Override
    public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        FeatureList featureList = new FeatureList();
        int size = featureVector.size();
        for (int i = 1; i <= size; ++i) {
            FeatureValue featureValue = featureVector.getFeatureValue(i - 1);
            if (featureValue == null || this.excludeNullValues && featureValue.isNullValue()) continue;
            if (!featureValue.isMultiple()) {
                SingleFeatureValue singleFeatureValue = (SingleFeatureValue)featureValue;
                int index = this.featureMap.getIndex(i, singleFeatureValue.getIndexCode());
                if (index == -1 || singleFeatureValue.getValue() == 0.0) continue;
                featureList.add(index, singleFeatureValue.getValue());
                continue;
            }
            for (Integer value : ((MultipleFeatureValue)featureValue).getCodes()) {
                int v = this.featureMap.getIndex(i, value);
                if (v == -1) continue;
                featureList.add(v, 1.0);
            }
        }
        try {
            decision.getKBestList().addList(this.model.predict(featureList.toArray()));
        }
        catch (OutOfMemoryError e) {
            throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
        }
        return true;
    }

    @Override
    public void train() throws MaltChainedException {
        if (this.owner == null) {
            throw new LibException("The parent guide model cannot be found. ");
        }
        String pathExternalTrain = null;
        if (!this.getConfiguration().getOptionValue("lib", "external").toString().equals("")) {
            String path = this.getConfiguration().getOptionValue("lib", "external").toString();
            try {
                if (!new File(path).exists()) {
                    throw new LibException("The path to the external  trainer 'svm-train' is wrong.");
                }
                if (new File(path).isDirectory()) {
                    throw new LibException("The option --lib-external points to a directory, the path should point at the 'train' file or the 'train.exe' file in the libsvm or the liblinear package");
                }
                if (!path.endsWith("train") && !path.endsWith("train.exe")) {
                    throw new LibException("The option --lib-external does not specify the path to 'train' file or the 'train.exe' file in the libsvm or the liblinear package. ");
                }
                pathExternalTrain = path;
            }
            catch (SecurityException e) {
                throw new LibException("Access denied to the file specified by the option --lib-external. ", e);
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
        }
        catch (FileNotFoundException e) {
            throw new LibException("The learner cannot save the feature map file '" + this.getFile(".map").getAbsolutePath() + "'. ", e);
        }
    }

    protected abstract void trainExternal(String var1, LinkedHashMap<String, String> var2) throws MaltChainedException;

    protected abstract void trainInternal(LinkedHashMap<String, String> var1) throws MaltChainedException;

    @Override
    public void terminate() throws MaltChainedException {
        this.closeInstanceWriter();
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
            throw new LibException("The learner cannot close the instance file. ", e);
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
        ArrayList<String> params = new ArrayList<String>();
        for (String key : libOptions.keySet()) {
            params.add("-" + key);
            params.add(libOptions.get(key));
        }
        return params.toArray(new String[params.size()]);
    }

    public abstract LinkedHashMap<String, String> getDefaultLibOptions();

    public abstract String getAllowedLibOptionFlags();

    public void parseParameters(String paramstring, LinkedHashMap<String, String> libOptions, String allowedLibOptionFlags) throws MaltChainedException {
        String[] argv;
        if (paramstring == null) {
            return;
        }
        try {
            argv = paramstring.split("[_\\p{Blank}]");
        }
        catch (PatternSyntaxException e) {
            throw new LibException("Could not split the parameter string '" + paramstring + "'. ", e);
        }
        for (int i = 0; i < argv.length - 1; ++i) {
            if (argv[i].charAt(0) != '-') {
                throw new LibException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
            }
            if (++i >= argv.length) {
                throw new LibException("The last argument does not have any value. ");
            }
            try {
                int index = allowedLibOptionFlags.indexOf(argv[i - 1].charAt(1));
                if (index == -1) {
                    throw new LibException("Unknown learner parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
                }
                libOptions.put(Character.toString(argv[i - 1].charAt(1)), argv[i]);
                continue;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
            catch (NumberFormatException e) {
                throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
            catch (NullPointerException e) {
                throw new LibException("The learner parameter '" + argv[i - 1] + "' could not convert the string value '" + argv[i] + "' into a correct numeric value. ", e);
            }
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
        sb.append('\n');
        sb.append(this.getLearningMethodName());
        sb.append(" INTERFACE\n");
        try {
            sb.append(this.getConfiguration().getOptionValue("lib", "options").toString());
        }
        catch (MaltChainedException e) {
            // empty catch block
        }
        return sb.toString();
    }

    protected int binariesInstance(String line, FeatureList featureList) throws MaltChainedException {
        Pattern tabPattern = Pattern.compile("\t");
        Pattern pipePattern = Pattern.compile("\\|");
        int y = -1;
        featureList.clear();
        try {
            String[] columns = tabPattern.split(line);
            if (columns.length == 0) {
                return -1;
            }
            try {
                y = Integer.parseInt(columns[0]);
            }
            catch (NumberFormatException e) {
                throw new LibException("The instance file contain a non-integer value '" + columns[0] + "'", e);
            }
            for (int j = 1; j < columns.length; ++j) {
                String[] items = pipePattern.split(columns[j]);
                for (int k = 0; k < items.length; ++k) {
                    try {
                        int colon = items[k].indexOf(58);
                        if (colon == -1) {
                            int v;
                            if (Integer.parseInt(items[k]) == -1 || (v = this.featureMap.addIndex(j, Integer.parseInt(items[k]))) == -1) continue;
                            featureList.add(v, 1.0);
                            continue;
                        }
                        int index = this.featureMap.addIndex(j, Integer.parseInt(items[k].substring(0, colon)));
                        double value = items[k].substring(colon + 1).indexOf(46) != -1 ? Double.parseDouble(items[k].substring(colon + 1)) : (double)Integer.parseInt(items[k].substring(colon + 1));
                        featureList.add(index, value);
                        continue;
                    }
                    catch (NumberFormatException e) {
                        throw new LibException("The instance file contain a non-numeric value '" + items[k] + "'", e);
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new LibException("Couln't read from the instance file. ", e);
        }
        return y;
    }

    protected void binariesInstances2SVMFileFormat(InputStreamReader isr, OutputStreamWriter osw) throws MaltChainedException {
        try {
            String line;
            BufferedReader in = new BufferedReader(isr);
            BufferedWriter out = new BufferedWriter(osw);
            FeatureList featureSet = new FeatureList();
            while ((line = in.readLine()) != null) {
                int y = this.binariesInstance(line, featureSet);
                if (y == -1) continue;
                out.write(Integer.toString(y));
                for (int k = 0; k < featureSet.size(); ++k) {
                    MaltFeatureNode x = featureSet.get(k);
                    out.write(32);
                    out.write(Integer.toString(x.getIndex()));
                    out.write(58);
                    out.write(Double.toString(x.getValue()));
                }
                out.write(10);
            }
            in.close();
            out.close();
        }
        catch (NumberFormatException e) {
            throw new LibException("The instance file contain a non-numeric value", e);
        }
        catch (IOException e) {
            throw new LibException("Couldn't read from the instance file, when converting the Malt instances into LIBSVM/LIBLINEAR format. ", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void saveFeatureMap(OutputStream os, FeatureMap map) throws MaltChainedException {
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(os);){
                output.writeObject(map);
            }
        }
        catch (IOException e) {
            throw new LibException("Save feature map error", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected FeatureMap loadFeatureMap(InputStream is) throws MaltChainedException {
        FeatureMap map = new FeatureMap();
        try {
            try (ObjectInputStream input = new ObjectInputStream(is);){
                map = (FeatureMap)input.readObject();
            }
        }
        catch (ClassNotFoundException e) {
            throw new LibException("Load feature map error", e);
        }
        catch (IOException e) {
            throw new LibException("Load feature map error", e);
        }
        return map;
    }

    public static enum Verbostity {
        SILENT,
        ERROR,
        ALL;
        
    }

}

