package org.maltparser.parser.guide.instance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.SingleDecision;

public class FeatureDivideModel implements InstanceModel {
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
         } catch (NumberFormatException var4) {
            throw new GuideException("The --guide-data_split_threshold option is not an integer value. ", var4);
         }
      } else {
         this.divideThreshold = 0;
      }

      this.divideModels = new TreeMap();
      if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.BATCH) {
         this.masterModel = new AtomicModel(-1, this);
      } else if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
         this.load();
      }

   }

   public void addInstance(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      SingleFeatureValue featureValue = (SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue();
      if (!this.divideModels.containsKey(featureValue.getIndexCode())) {
         this.divideModels.put(featureValue.getIndexCode(), new AtomicModel(featureValue.getIndexCode(), this));
      }

      FeatureVector divideFeatureVector = featureVector.getFeatureModel().getFeatureVector("/" + featureVector.getSpecSubModel().getSubModelName());
      ((AtomicModel)this.divideModels.get(featureValue.getIndexCode())).addInstance(divideFeatureVector, decision);
   }

   public void noMoreInstances(FeatureModel featureModel) throws MaltChainedException {
      Iterator i$ = this.divideModels.keySet().iterator();

      while(i$.hasNext()) {
         Integer index = (Integer)i$.next();
         ((AtomicModel)this.divideModels.get(index)).noMoreInstances(featureModel);
      }

      TreeSet<Integer> removeSet = new TreeSet();
      Iterator i$ = this.divideModels.keySet().iterator();

      Integer index;
      while(i$.hasNext()) {
         index = (Integer)i$.next();
         if (((AtomicModel)this.divideModels.get(index)).getFrequency() <= this.divideThreshold) {
            ((AtomicModel)this.divideModels.get(index)).moveAllInstances(this.masterModel, featureModel.getDivideFeatureFunction(), featureModel.getDivideFeatureIndexVector());
            removeSet.add(index);
         }
      }

      i$ = removeSet.iterator();

      while(i$.hasNext()) {
         index = (Integer)i$.next();
         this.divideModels.remove(index);
      }

      this.masterModel.noMoreInstances(featureModel);
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      if (this.divideModels == null) {
         throw new GuideException("The feature divide models cannot be found. ");
      } else {
         Iterator i$ = this.divideModels.values().iterator();

         while(i$.hasNext()) {
            AtomicModel divideModel = (AtomicModel)i$.next();
            divideModel.finalizeSentence(dependencyGraph);
         }

      }
   }

   public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
      if (model == null) {
         if (this.getGuide().getConfiguration().isLoggerInfoEnabled()) {
            this.getGuide().getConfiguration().logInfoMessage("Could not predict the next parser decision because there is no divide or master model that covers the divide value '" + ((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue()).getIndexCode() + "', as default" + " class code '1' is used. ");
         }

         decision.addDecision(1);
         return true;
      } else {
         return model.predict(this.getModelFeatureVector(model, featureVector), decision);
      }
   }

   public FeatureVector predictExtract(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
      return model == null ? null : model.predictExtract(this.getModelFeatureVector(model, featureVector), decision);
   }

   public FeatureVector extract(FeatureVector featureVector) throws MaltChainedException {
      AtomicModel model = this.getAtomicModel((SingleFeatureValue)featureVector.getFeatureModel().getDivideFeatureFunction().getFeatureValue());
      return model == null ? featureVector : model.extract(this.getModelFeatureVector(model, featureVector));
   }

   private FeatureVector getModelFeatureVector(AtomicModel model, FeatureVector featureVector) {
      return model.getIndex() == -1 ? featureVector : featureVector.getFeatureModel().getFeatureVector("/" + featureVector.getSpecSubModel().getSubModelName());
   }

   private AtomicModel getAtomicModel(SingleFeatureValue featureValue) throws MaltChainedException {
      if (this.divideModels != null && this.divideModels.containsKey(featureValue.getIndexCode())) {
         return (AtomicModel)this.divideModels.get(featureValue.getIndexCode());
      } else {
         return this.masterModel != null && this.masterModel.getFrequency() > 0 ? this.masterModel : null;
      }
   }

   public void terminate() throws MaltChainedException {
      if (this.divideModels != null) {
         Iterator i$ = this.divideModels.values().iterator();

         while(i$.hasNext()) {
            AtomicModel divideModel = (AtomicModel)i$.next();
            divideModel.terminate();
         }
      }

      if (this.masterModel != null) {
         this.masterModel.terminate();
      }

   }

   public void train() throws MaltChainedException {
      Iterator i$ = this.divideModels.values().iterator();

      AtomicModel divideModel;
      while(i$.hasNext()) {
         divideModel = (AtomicModel)i$.next();
         divideModel.train();
      }

      this.masterModel.train();
      this.save();
      i$ = this.divideModels.values().iterator();

      while(i$.hasNext()) {
         divideModel = (AtomicModel)i$.next();
         divideModel.terminate();
      }

      this.masterModel.terminate();
   }

   protected void save() throws MaltChainedException {
      try {
         BufferedWriter out = new BufferedWriter(this.getGuide().getConfiguration().getOutputStreamWriter(this.getModelName() + ".dsm"));
         out.write(this.masterModel.getIndex() + "\t" + this.masterModel.getFrequency() + "\n");
         if (this.divideModels != null) {
            Iterator i$ = this.divideModels.values().iterator();

            while(i$.hasNext()) {
               AtomicModel divideModel = (AtomicModel)i$.next();
               out.write(divideModel.getIndex() + "\t" + divideModel.getFrequency() + "\n");
            }
         }

         out.close();
      } catch (IOException var4) {
         throw new GuideException("Could not write to the guide model settings file '" + this.getModelName() + ".dsm" + "', when " + "saving the guide model settings to file. ", var4);
      }
   }

   protected void load() throws MaltChainedException {
      String dsmString = this.getGuide().getConfiguration().getConfigFileEntryString(this.getModelName() + ".dsm");
      String[] lines = dsmString.split("\n");
      Pattern tabPattern = Pattern.compile("\t");

      for(int i = 0; i < lines.length; ++i) {
         String[] cols = tabPattern.split(lines[i]);
         if (cols.length != 2) {
            throw new GuideException("");
         }

         int code = true;
         boolean var7 = false;

         int code;
         int freq;
         try {
            code = Integer.parseInt(cols[0]);
            freq = Integer.parseInt(cols[1]);
         } catch (NumberFormatException var9) {
            throw new GuideException("Could not convert a string value into an integer value when loading the feature divide model settings (.dsm). ", var9);
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

   public ClassifierGuide getGuide() {
      return this.parent.getGuide();
   }

   public String getModelName() throws MaltChainedException {
      try {
         return this.parent.getModelName();
      } catch (NullPointerException var2) {
         throw new GuideException("The parent guide model cannot be found. ", var2);
      }
   }

   public int getFrequency() {
      return this.frequency;
   }

   public void increaseFrequency() {
      if (this.parent instanceof InstanceModel) {
         ((InstanceModel)this.parent).increaseFrequency();
      }

      ++this.frequency;
   }

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
