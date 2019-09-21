package org.maltparser.core.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.spec.SpecificationModel;
import org.maltparser.core.feature.spec.SpecificationSubModel;
import org.maltparser.core.feature.system.FeatureEngine;
import org.maltparser.core.helper.HashMap;

public class FeatureModel extends HashMap<String, FeatureVector> {
   public static final long serialVersionUID = 3256444702936019250L;
   private static final Pattern splitPattern = Pattern.compile("\\(|\\)|\\[|\\]|,");
   private final SpecificationModel specModel;
   private final ArrayList<AddressFunction> addressFunctionCache;
   private final ArrayList<FeatureFunction> featureFunctionCache;
   private final FeatureFunction divideFeatureFunction;
   private final FeatureRegistry registry;
   private final FeatureEngine featureEngine;
   private final FeatureVector mainFeatureVector;
   private final ArrayList<Integer> divideFeatureIndexVector;

   public FeatureModel(SpecificationModel _specModel, FeatureRegistry _registry, FeatureEngine _engine, String dataSplitColumn, String dataSplitStructure) throws MaltChainedException {
      this.specModel = _specModel;
      this.registry = _registry;
      this.featureEngine = _engine;
      this.addressFunctionCache = new ArrayList();
      this.featureFunctionCache = new ArrayList();
      FeatureVector tmpMainFeatureVector = null;
      Iterator i$ = this.specModel.iterator();

      while(i$.hasNext()) {
         SpecificationSubModel subModel = (SpecificationSubModel)i$.next();
         FeatureVector fv = new FeatureVector(this, subModel);
         if (tmpMainFeatureVector == null) {
            if (subModel.getSubModelName().equals("MAIN")) {
               tmpMainFeatureVector = fv;
            } else {
               tmpMainFeatureVector = fv;
               this.put(subModel.getSubModelName(), fv);
            }
         } else {
            this.put(subModel.getSubModelName(), fv);
         }
      }

      this.mainFeatureVector = tmpMainFeatureVector;
      if (dataSplitColumn != null && dataSplitColumn.length() > 0 && dataSplitStructure != null && dataSplitStructure.length() > 0) {
         StringBuilder sb = new StringBuilder();
         sb.append("InputColumn(");
         sb.append(dataSplitColumn);
         sb.append(", ");
         sb.append(dataSplitStructure);
         sb.append(')');
         this.divideFeatureFunction = this.identifyFeature(sb.toString());
         this.divideFeatureIndexVector = new ArrayList();

         for(int i = 0; i < this.mainFeatureVector.size(); ++i) {
            if (((FeatureFunction)this.mainFeatureVector.get(i)).equals(this.divideFeatureFunction)) {
               this.divideFeatureIndexVector.add(i);
            }
         }

         Iterator i$ = this.specModel.iterator();

         while(i$.hasNext()) {
            SpecificationSubModel subModel = (SpecificationSubModel)i$.next();
            FeatureVector featureVector = (FeatureVector)this.get(subModel.getSubModelName());
            if (featureVector == null) {
               featureVector = this.mainFeatureVector;
            }

            String divideKeyName = "/" + subModel.getSubModelName();
            FeatureVector divideFeatureVector = (FeatureVector)featureVector.clone();
            Iterator i$ = this.divideFeatureIndexVector.iterator();

            while(i$.hasNext()) {
               Integer i = (Integer)i$.next();
               divideFeatureVector.remove(divideFeatureVector.get(i));
            }

            this.put(divideKeyName, divideFeatureVector);
         }
      } else {
         this.divideFeatureFunction = null;
         this.divideFeatureIndexVector = null;
      }

   }

   public SpecificationModel getSpecModel() {
      return this.specModel;
   }

   public FeatureRegistry getRegistry() {
      return this.registry;
   }

   public FeatureEngine getFeatureEngine() {
      return this.featureEngine;
   }

   public FeatureVector getMainFeatureVector() {
      return this.mainFeatureVector;
   }

   public FeatureVector getFeatureVector(String subModelName) {
      return (FeatureVector)this.get(subModelName);
   }

   public FeatureVector getFeatureVector(String decisionSymbol, String subModelName) {
      StringBuilder sb = new StringBuilder();
      if (decisionSymbol.length() > 0) {
         sb.append(decisionSymbol);
         sb.append('.');
      }

      sb.append(subModelName);
      if (this.containsKey(sb.toString())) {
         return (FeatureVector)this.get(sb.toString());
      } else {
         return this.containsKey(subModelName) ? (FeatureVector)this.get(subModelName) : this.mainFeatureVector;
      }
   }

   public FeatureFunction getDivideFeatureFunction() {
      return this.divideFeatureFunction;
   }

   public boolean hasDivideFeatureFunction() {
      return this.divideFeatureFunction != null;
   }

   public ArrayList<Integer> getDivideFeatureIndexVector() {
      return this.divideFeatureIndexVector;
   }

   public boolean hasDivideFeatureIndexVector() {
      return this.divideFeatureIndexVector != null;
   }

   public void update() throws MaltChainedException {
      int i = 0;

      int n;
      for(n = this.addressFunctionCache.size(); i < n; ++i) {
         ((AddressFunction)this.addressFunctionCache.get(i)).update();
      }

      i = 0;

      for(n = this.featureFunctionCache.size(); i < n; ++i) {
         ((FeatureFunction)this.featureFunctionCache.get(i)).update();
      }

   }

   public void update(Object[] arguments) throws MaltChainedException {
      int i = 0;

      int n;
      for(n = this.addressFunctionCache.size(); i < n; ++i) {
         ((AddressFunction)this.addressFunctionCache.get(i)).update(arguments);
      }

      i = 0;

      for(n = this.featureFunctionCache.size(); i < n; ++i) {
         ((FeatureFunction)this.featureFunctionCache.get(i)).update();
      }

   }

   public FeatureFunction identifyFeature(String spec) throws MaltChainedException {
      String[] items = splitPattern.split(spec);
      Stack<Object> objects = new Stack();

      for(int i = items.length - 1; i >= 0; --i) {
         if (items[i].trim().length() != 0) {
            objects.push(items[i].trim());
         }
      }

      this.identifyFeatureFunction(objects);
      if (objects.size() == 1 && objects.peek() instanceof FeatureFunction && !(objects.peek() instanceof AddressFunction)) {
         return (FeatureFunction)objects.pop();
      } else {
         throw new FeatureException("The feature specification '" + spec + "' were not recognized properly. ");
      }
   }

   protected void identifyFeatureFunction(Stack<Object> objects) throws MaltChainedException {
      Function function = this.featureEngine.newFunction(objects.peek().toString(), this.registry);
      if (function != null) {
         objects.pop();
         if (!objects.isEmpty()) {
            this.identifyFeatureFunction(objects);
         }

         this.initializeFunction(function, objects);
      } else if (!objects.isEmpty()) {
         Object o = objects.pop();
         if (!objects.isEmpty()) {
            this.identifyFeatureFunction(objects);
         }

         objects.push(o);
      }

   }

   protected void initializeFunction(Function function, Stack<Object> objects) throws MaltChainedException {
      Class<?>[] paramTypes = function.getParameterTypes();
      Object[] arguments = new Object[paramTypes.length];

      int i;
      for(i = 0; i < paramTypes.length; ++i) {
         String object;
         if (paramTypes[i] == Integer.class) {
            if (!(objects.peek() instanceof String)) {
               throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + objects.peek() + "'" + ", expect an integer value. ");
            }

            object = (String)objects.pop();

            try {
               objects.push(Integer.parseInt(object));
            } catch (NumberFormatException var9) {
               throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + object + "'" + ", expect an integer value. ", var9);
            }
         } else if (paramTypes[i] == Double.class) {
            if (!(objects.peek() instanceof String)) {
               throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + objects.peek() + "'" + ", expect a numeric value. ");
            }

            object = (String)objects.pop();

            try {
               objects.push(Double.parseDouble(object));
            } catch (NumberFormatException var8) {
               throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + object + "'" + ", expect a numeric value. ", var8);
            }
         } else if (paramTypes[i] == Boolean.class) {
            if (!(objects.peek() instanceof String)) {
               throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + objects.peek() + "'" + ", expect a boolean value. ");
            }

            objects.push(Boolean.parseBoolean((String)objects.pop()));
         }

         if (!paramTypes[i].isInstance(objects.peek())) {
            throw new FeatureException("The function '" + function.getClass() + "' cannot be initialized with argument '" + objects.peek() + "'");
         }

         arguments[i] = objects.pop();
      }

      function.initialize(arguments);
      if (function instanceof AddressFunction) {
         i = this.addressFunctionCache.indexOf(function);
         if (i != -1) {
            function = (Function)this.addressFunctionCache.get(i);
         } else {
            this.addressFunctionCache.add((AddressFunction)function);
         }
      } else if (function instanceof FeatureFunction) {
         i = this.featureFunctionCache.indexOf(function);
         if (i != -1) {
            function = (Function)this.featureFunctionCache.get(i);
         } else {
            this.featureFunctionCache.add((FeatureFunction)function);
         }
      }

      objects.push(function);
   }

   public String toString() {
      return this.specModel.toString();
   }
}
