package org.maltparser.parser.guide.instance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Formatter;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Modifiable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.ml.LearningMethod;
import org.maltparser.ml.lib.LibLinear;
import org.maltparser.ml.lib.LibSvm;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.SingleDecision;

public class AtomicModel implements InstanceModel {
   public static final Class<?>[] argTypes = new Class[]{InstanceModel.class, Integer.class};
   private final Model parent;
   private final String modelName;
   private final int index;
   private final LearningMethod method;
   private int frequency = 0;

   public AtomicModel(int index, Model parent) throws MaltChainedException {
      this.parent = parent;
      this.index = index;
      if (index == -1) {
         this.modelName = parent.getModelName() + ".";
      } else {
         this.modelName = parent.getModelName() + "." + (new Formatter()).format("%03d", index) + ".";
      }

      this.frequency = 0;
      Integer learnerMode = null;
      if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
         learnerMode = 1;
      } else if (this.getGuide().getGuideMode() == ClassifierGuide.GuideMode.BATCH) {
         learnerMode = 0;
      }

      Class<?> clazz = (Class)this.getGuide().getConfiguration().getOptionValue("guide", "learner");
      if (clazz == LibSvm.class) {
         this.method = new LibSvm(this, learnerMode);
      } else if (clazz == LibLinear.class) {
         this.method = new LibLinear(this, learnerMode);
      } else {
         Object[] arguments = new Object[]{this, learnerMode};

         try {
            Constructor<?> constructor = clazz.getConstructor(argTypes);
            this.method = (LearningMethod)constructor.newInstance(arguments);
         } catch (NoSuchMethodException var7) {
            throw new GuideException("The learner class '" + clazz.getName() + "' cannot be initialized. ", var7);
         } catch (InstantiationException var8) {
            throw new GuideException("The learner class '" + clazz.getName() + "' cannot be initialized. ", var8);
         } catch (IllegalAccessException var9) {
            throw new GuideException("The learner class '" + clazz.getName() + "' cannot be initialized. ", var9);
         } catch (InvocationTargetException var10) {
            throw new GuideException("The learner class '" + clazz.getName() + "' cannot be initialized. ", var10);
         }
      }

      if (learnerMode == 0 && index == -1 && this.getGuide().getConfiguration() != null) {
         this.getGuide().getConfiguration().writeInfoToConfigFile(this.method.toString());
      }

   }

   public void addInstance(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      try {
         this.method.addInstance(decision, featureVector);
      } catch (NullPointerException var4) {
         throw new GuideException("The learner cannot be found. ", var4);
      }
   }

   public void noMoreInstances(FeatureModel featureModel) throws MaltChainedException {
      try {
         this.method.noMoreInstances();
      } catch (NullPointerException var3) {
         throw new GuideException("The learner cannot be found. ", var3);
      }
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      try {
         this.method.finalizeSentence(dependencyGraph);
      } catch (NullPointerException var3) {
         throw new GuideException("The learner cannot be found. ", var3);
      }
   }

   public boolean predict(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      try {
         return this.method.predict(featureVector, decision);
      } catch (NullPointerException var4) {
         throw new GuideException("The learner cannot be found. ", var4);
      }
   }

   public FeatureVector predictExtract(FeatureVector featureVector, SingleDecision decision) throws MaltChainedException {
      try {
         return this.method.predict(featureVector, decision) ? featureVector : null;
      } catch (NullPointerException var4) {
         throw new GuideException("The learner cannot be found. ", var4);
      }
   }

   public FeatureVector extract(FeatureVector featureVector) throws MaltChainedException {
      return featureVector;
   }

   public void terminate() throws MaltChainedException {
      if (this.method != null) {
         this.method.terminate();
      }

   }

   public void moveAllInstances(AtomicModel model, FeatureFunction divideFeature, ArrayList<Integer> divideFeatureIndexVector) throws MaltChainedException {
      if (this.method == null) {
         throw new GuideException("The learner cannot be found. ");
      } else if (model == null) {
         throw new GuideException("The guide model cannot be found. ");
      } else if (divideFeature == null) {
         throw new GuideException("The divide feature cannot be found. ");
      } else if (divideFeatureIndexVector == null) {
         throw new GuideException("The divide feature index vector cannot be found. ");
      } else {
         ((Modifiable)divideFeature).setFeatureValue(this.index);
         this.method.moveAllInstances(model.getMethod(), divideFeature, divideFeatureIndexVector);
         this.method.terminate();
      }
   }

   public void train() throws MaltChainedException {
      try {
         this.method.train();
         this.method.terminate();
      } catch (NullPointerException var2) {
         throw new GuideException("The learner cannot be found. ", var2);
      }
   }

   public Model getParent() throws MaltChainedException {
      if (this.parent == null) {
         throw new GuideException("The atomic model can only be used by a parent model. ");
      } else {
         return this.parent;
      }
   }

   public String getModelName() {
      return this.modelName;
   }

   public ClassifierGuide getGuide() {
      return this.parent.getGuide();
   }

   public int getIndex() {
      return this.index;
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

   public LearningMethod getMethod() {
      return this.method;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.method.toString());
      return sb.toString();
   }
}
