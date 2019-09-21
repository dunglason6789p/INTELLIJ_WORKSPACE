/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.cheater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.ml.LearningMethod;
import org.maltparser.ml.cheater.CheaterException;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;

public class Cheater
implements LearningMethod {
    protected InstanceModel owner;
    protected int learnerMode;
    protected String name;
    protected int numberOfInstances;
    protected boolean excludeNullValues;
    private String cheaterFileName;
    private BufferedWriter cheaterWriter = null;
    private boolean saveCheatAction;
    private BufferedWriter instanceOutput = null;
    private ArrayList<Integer> cheatValues;
    private int cheaterPosition;
    private Verbostity verbosity;

    public Cheater(InstanceModel owner, Integer learnerMode) throws MaltChainedException {
        this.setOwner(owner);
        this.setLearningMethodName("cheater");
        this.setLearnerMode(learnerMode);
        this.setNumberOfInstances(0);
        this.verbosity = Verbostity.SILENT;
        this.initSpecialParameters();
        if (learnerMode == 0) {
            if (!this.saveCheatAction) {
                this.instanceOutput = new BufferedWriter(this.getInstanceOutputStreamWriter(".ins"));
            } else {
                try {
                    if (this.cheaterFileName != null && !this.cheaterFileName.equals("")) {
                        this.cheaterWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.cheaterFileName)));
                    }
                }
                catch (Exception e) {
                    throw new CheaterException("", e);
                }
            }
        }
    }

    @Override
    public void addInstance(SingleDecision decision, FeatureVector featureVector) throws MaltChainedException {
        if (featureVector == null) {
            throw new CheaterException("The feature vector cannot be found");
        }
        if (decision == null) {
            throw new CheaterException("The decision cannot be found");
        }
        if (this.saveCheatAction && this.cheaterWriter != null) {
            try {
                this.cheaterWriter.write(decision.getDecisionCode() + "\n");
            }
            catch (IOException e) {
                throw new CheaterException("The cheater learner cannot write to the cheater file. ", e);
            }
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
            throw new CheaterException("The cheater learner cannot write to the instance file. ", e);
        }
    }

    @Override
    public void train() throws MaltChainedException {
    }

    @Override
    public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        if (this.cheatValues == null) {
            if (this.cheaterFileName == null || this.cheaterFileName.equals("")) {
                throw new CheaterException("The cheater file name is assigned. ");
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.cheaterFileName)));
                String line = "";
                this.cheatValues = new ArrayList();
                while ((line = reader.readLine()) != null) {
                    this.cheatValues.add(Integer.parseInt(line));
                }
                this.cheaterPosition = 0;
                reader.close();
                this.cheaterWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.cheaterFileName + ".ins")));
            }
            catch (Exception e) {
                throw new CheaterException("Couldn't find or read from the cheater file '" + this.cheaterFileName + "'", e);
            }
        }
        int decisionValue = 0;
        StringBuilder csb = new StringBuilder();
        if (this.cheaterPosition >= this.cheatValues.size()) {
            throw new CheaterException("Not enough cheat values to complete all sentences. ");
        }
        decisionValue = this.cheatValues.get(this.cheaterPosition++);
        csb.append(decisionValue + " ");
        csb.setLength(csb.length() - 1);
        csb.append('\n');
        try {
            this.cheaterWriter.write(csb.toString());
            this.cheaterWriter.flush();
        }
        catch (Exception e) {
            throw new CheaterException("", e);
        }
        try {
            decision.getKBestList().add(decisionValue);
        }
        catch (Exception e) {
            decision.getKBestList().add(-1);
        }
        return true;
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void moveAllInstances(LearningMethod method, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
    }

    @Override
    public void noMoreInstances() throws MaltChainedException {
        this.closeInstanceWriter();
        this.closeCheaterWriter();
    }

    @Override
    public void terminate() throws MaltChainedException {
        this.closeInstanceWriter();
        this.closeCheaterWriter();
        this.owner = null;
    }

    protected void closeCheaterWriter() throws MaltChainedException {
        try {
            if (this.cheaterWriter != null) {
                this.cheaterWriter.flush();
                this.cheaterWriter.close();
                this.cheaterWriter = null;
            }
        }
        catch (IOException e) {
            throw new CheaterException("The cheater learner cannot close the cheater file. ", e);
        }
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
            throw new CheaterException("The cheater learner cannot close the instance file. ", e);
        }
    }

    protected void initSpecialParameters() throws MaltChainedException {
        this.excludeNullValues = this.getConfiguration().getOptionValue("singlemalt", "null_value") != null && this.getConfiguration().getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none");
        this.saveCheatAction = (Boolean)this.getConfiguration().getOptionValue("cheater", "save_cheat_action");
        if (!this.getConfiguration().getOptionValue("cheater", "cheater_file").toString().equals("")) {
            this.cheaterFileName = this.getConfiguration().getOptionValue("cheater", "cheater_file").toString();
        }
        if (this.getConfiguration().getOptionValue("liblinear", "verbosity") != null) {
            this.verbosity = Verbostity.valueOf(this.getConfiguration().getOptionValue("cheater", "verbosity").toString().toUpperCase());
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
                throw new CheaterException("The instance file contain a non-integer value, when converting the Malt SVM format into Liblinear format.");
            }
            in.close();
            out.close();
        }
        catch (IOException e) {
            throw new CheaterException("Cannot read from the instance file, when converting the Malt SVM format into Liblinear format. ", e);
        }
    }

    @Override
    public BufferedWriter getInstanceWriter() {
        return this.instanceOutput;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finalize() throws Throwable {
        try {
            this.closeInstanceWriter();
            this.closeCheaterWriter();
        }
        finally {
            super.finalize();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nCheater INTERFACE\n");
        return sb.toString();
    }

    public static enum Verbostity {
        SILENT,
        ERROR,
        ALL;
        
    }

}

