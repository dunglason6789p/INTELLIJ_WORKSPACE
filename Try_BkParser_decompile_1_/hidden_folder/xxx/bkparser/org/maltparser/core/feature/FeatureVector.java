package org.maltparser.core.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.spec.SpecificationSubModel;
import org.maltparser.core.feature.value.FeatureValue;

public class FeatureVector extends ArrayList<FeatureFunction> implements Serializable {
   public static final long serialVersionUID = 3256444702936019250L;
   private final SpecificationSubModel specSubModel;
   private final FeatureModel featureModel;

   public FeatureVector(FeatureModel _featureModel, SpecificationSubModel _specSubModel) throws MaltChainedException {
      this.specSubModel = _specSubModel;
      this.featureModel = _featureModel;
      Iterator i$ = this.specSubModel.iterator();

      while(i$.hasNext()) {
         String spec = (String)i$.next();
         this.add(this.featureModel.identifyFeature(spec));
      }

   }

   public SpecificationSubModel getSpecSubModel() {
      return this.specSubModel;
   }

   public FeatureModel getFeatureModel() {
      return this.featureModel;
   }

   public FeatureValue getFeatureValue(int index) {
      return ((FeatureFunction)this.get(index)).getFeatureValue();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         FeatureFunction function = (FeatureFunction)i$.next();
         if (function != null) {
            sb.append(function.getFeatureValue().toString());
            sb.append('\n');
         }
      }

      return sb.toString();
   }
}
