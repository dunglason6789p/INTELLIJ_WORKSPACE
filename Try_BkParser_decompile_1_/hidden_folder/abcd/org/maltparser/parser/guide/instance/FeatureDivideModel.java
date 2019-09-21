/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide.instance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.spec.SpecificationSubModel;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.guide.instance.AtomicModel;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.SingleDecision;

public class FeatureDivideModel
implements InstanceModel {
    private final Model parent;
    private final SortedMap<Integer, AtomicModel> divideModels;
    private int frequency = 0;
    private final int divideThreshold;
    private AtomicModel masterModel;

    public FeatureDivideModel(Model parent) throws MaltChainedException {
        this.parent = parent;
        this.setFrequency(0);
        String data_split_threshold = this.getGuide().getConfiguration().getOptionValue("guide", "data_split_threshold").toString().trim();
        if (data_split_threshold != null) {
            try {
                this.divideThreshold = Integer.parseInt(data_split_threshold);
            }
            catch (NumberFormatException e) {
                throw new GuideException("The --guide-data_split_threshold option is not an integer value. ", e);
            }
        } else {
            this.divideThreshold = 0;
        }
        this.divideModels = new TreeMap<Integer, AtomicModel>();
        if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.BATCH) {
            this.masterModel = new AtomicModel(-1, this);
        } else if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
            this.load();
        }
    }

    @Override
    public void addInstance(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        SingleFeatureValue featureValue = (SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue();
        if (!this.divideModels.containsKey(featureValue.getIndexCode())) {
            this.divideModels.put(featureValue.getIndexCode(), new AtomicModel(featureValue.getIndexCode(), this));
        }
        FeatureVector divideFeatureVector = featureVector.getFeatureModel().getFeatureVector("/" + featureVector.getSpecSubModel().getSubModelName());
        ((AtomicModel)this.divideModels.get(featureValue.getIndexCode())).addInstance(divideFeatureVector, decision);
    }

    @Override
    public void noMoreInstances(FeatureModel featureModel) throws MaltChainedException {
        for (Integer index : this.divideModels.keySet()) {
            ((AtomicModel)this.divideModels.get(index)).noMoreInstances(featureModel);
        }
        TreeSet<Integer> removeSet = new TreeSet<Integer>();
        for (Integer index : this.divideModels.keySet()) {
            if (((AtomicModel)this.divideModels.get(index)).getFrequency() > this.divideThreshold) continue;
            ((AtomicModel)this.divideModels.get(index)).moveAllInstances(this.masterModel, featureModel.getDivideFeatureFunction(), featureModel.getDivideFeatureIndexVector());
            removeSet.add(index);
        }
        for (Integer index : removeSet) {
            this.divideModels.remove(index);
        }
        this.masterModel.noMoreInstances(featureModel);
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
        if (this.divideModels != null) {
            for (AtomicModel divideModel : this.divideModels.values()) {
                divideModel.finalizeSentence(dependencyGraph);
            }
        } else {
            throw new GuideException("The feature divide models cannot be found. ");
        }
    }

    @Override
    public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
        if (model == null) {
            if (this.getGuide().getConfiguration().isLoggerInfoEnabled()) {
                this.getGuide().getConfiguration().logInfoMessage("Could not predict the next parser decision because there is no divide or master model that covers the divide value '" + ((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue()).getIndexCode() + "', as default" + " class code '1' is used. ");
            }
            decision.addDecision(1);
            return true;
        }
        return model.predict(this.getModelFeatureVector(model, featureVector), decision);
    }

    @Override
    public FeatureVector predictExtract(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
        AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
        if (model == null) {
            return null;
        }
        return model.predictExtract(this.getModelFeatureVector(model, featureVector), decision);
    }

    @Override
    public FeatureVector extract(FeatureVector featureVector) throws MaltChainedException {
        AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
        if (model == null) {
            return featureVector;
        }
        return model.extract(this.getModelFeatureVector(model, featureVector));
    }

    private FeatureVector getModelFeatureVector(AtomicModel model, FeatureVector featureVector) {
        if (model.getIndex() == -1) {
            return featureVector;
        }
        return featureVector.getFeatureModel().getFeatureVector("/" + featureVector.getSpecSubModel().getSubModelName());
    }

    private AtomicModel getAtomicModel(SingleFeatureValue featureValue) throws MaltChainedException {
        if (this.divideModels != null && this.divideModels.containsKey(featureValue.getIndexCode())) {
            return (AtomicModel)this.divideModels.get(featureValue.getIndexCode());
        }
        if (this.masterModel != null && this.masterModel.getFrequency() > 0) {
            return this.masterModel;
        }
        return null;
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.divideModels != null) {
            for (AtomicModel divideModel : this.divideModels.values()) {
                divideModel.terminate();
            }
        }
        if (this.masterModel != null) {
            this.masterModel.terminate();
        }
    }

    @Override
    public void train() throws MaltChainedException {
        for (AtomicModel divideModel : this.divideModels.values()) {
            divideModel.train();
        }
        this.masterModel.train();
        this.save();
        for (AtomicModel divideModel : this.divideModels.values()) {
            divideModel.terminate();
        }
        this.masterModel.terminate();
    }

    protected void save() throws MaltChainedException {
        try {
            BufferedWriter out = new BufferedWriter(this.getGuide().getConfiguration().getOutputStreamWriter(this.getModelName() + ".dsm"));
            out.write(this.masterModel.getIndex() + "\t" + this.masterModel.getFrequency() + "\n");
            if (this.divideModels != null) {
                for (AtomicModel divideModel : this.divideModels.values()) {
                    out.write(divideModel.getIndex() + "\t" + divideModel.getFrequency() + "\n");
                }
            }
            out.close();
        }
        catch (IOException e) {
            throw new GuideException("Could not write to the guide model settings file '" + this.getModelName() + ".dsm" + "', when " + "saving the guide model settings to file. ", e);
        }
    }

    protected void load() throws MaltChainedException {
        String dsmString = this.getGuide().getConfiguration().getConfigFileEntryString(this.getModelName() + ".dsm");
        String[] lines = dsmString.split("\n");
        Pattern tabPattern = Pattern.compile("\t");
        for (int i = 0; i < lines.length; ++i) {
            String[] cols = tabPattern.split(lines[i]);
            if (cols.length != 2) {
                throw new GuideException("");
            }
            int code = -1;
            int freq = 0;
            try {
                code = Integer.parseInt(cols[0]);
                freq = Integer.parseInt(cols[1]);
            }
            catch (NumberFormatException e) {
                throw new GuideException("Could not convert a string value into an integer value when loading the feature divide model settings (.dsm). ", e);
            }
            if (code == -1) {
                this.masterModel = new AtomicModel(-1, this);
                this.masterModel.setFrequency(freq);
            } else if (this.divideModels != null) {
                this.divideModels.put(code, new AtomicModel(code, this));
                ((AtomicModel)this.divideModels.get(code)).setFrequency(freq);
            }
            this.setFrequency(this.getFrequency() + freq);
        }
    }

    public Model getParent() {
        return this.parent;
    }

    @Override
    public ClassifierGuide getGuide() {
        return this.parent.getGuide();
    }

    @Override
    public String getModelName() throws MaltChainedException {
        try {
            return this.parent.getModelName();
        }
        catch (NullPointerException e) {
            throw new GuideException("The parent guide model cannot be found. ", e);
        }
    }

    public int getFrequency() {
        return this.frequency;
    }

    @Override
    public void increaseFrequency() {
        if (this.parent instanceof InstanceModel) {
            ((InstanceModel)this.parent).increaseFrequency();
        }
        ++this.frequency;
    }

    @Override
    public void decreaseFrequency() {
        if (this.parent instanceof InstanceModel) {
            ((InstanceModel)this.parent).decreaseFrequency();
        }
        --this.frequency;
    }

    protected void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }
}

