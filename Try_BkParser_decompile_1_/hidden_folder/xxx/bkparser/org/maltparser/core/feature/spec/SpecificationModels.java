package org.maltparser.core.feature.spec;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.spec.reader.FeatureSpecReader;
import org.maltparser.core.feature.spec.reader.ParReader;
import org.maltparser.core.helper.HashMap;

public class SpecificationModels {
   private final HashMap<URL, FeatureSpecReader> specReaderMap = new HashMap();
   private final HashMap<String, SpecificationModel> specModelMap = new HashMap();
   private final HashMap<Integer, SpecificationModel> specModelIntMap = new HashMap();
   private final LinkedHashMap<URL, ArrayList<SpecificationModel>> specModelKeyMap = new LinkedHashMap();
   private final ArrayList<SpecificationModel> currentSpecModelURL = new ArrayList();
   private int counter = 0;

   public SpecificationModels() throws MaltChainedException {
   }

   public void add(int index, String featureSpec) throws MaltChainedException {
      this.add(Integer.toString(index), "MAIN", featureSpec);
   }

   public void add(String specModelName, String featureSpec) throws MaltChainedException {
      this.add(specModelName, "MAIN", featureSpec);
   }

   public void add(int index, String subModelName, String featureSpec) throws MaltChainedException {
      this.add(Integer.toString(index), subModelName, featureSpec);
   }

   public void add(String specModelName, String subModelName, String featureSpec) throws MaltChainedException {
      if (featureSpec == null) {
         throw new FeatureException("Feature specification is missing.");
      } else if (specModelName == null) {
         throw new FeatureException("Unknown feature model name.");
      } else if (subModelName == null) {
         throw new FeatureException("Unknown subfeature model name.");
      } else {
         if (!this.specModelMap.containsKey(specModelName.toUpperCase())) {
            SpecificationModel specModel = new SpecificationModel(specModelName.toUpperCase());
            this.specModelMap.put(specModelName.toUpperCase(), specModel);
            this.currentSpecModelURL.add(specModel);
            this.specModelIntMap.put(this.counter++, specModel);
         }

         ((SpecificationModel)this.specModelMap.get(specModelName.toUpperCase())).add(subModelName, featureSpec);
      }
   }

   public int getNextIndex() {
      return this.counter;
   }

   public void loadParReader(URL specModelURL, String markingStrategy, String coveredRoot) throws MaltChainedException {
      if (specModelURL == null) {
         throw new FeatureException("The URL to the feature specification model is missing or not well-formed. ");
      } else {
         FeatureSpecReader specReader = null;
         String urlSuffix = specModelURL.toString().substring(specModelURL.toString().length() - 3);
         urlSuffix = Character.toUpperCase(urlSuffix.charAt(0)) + urlSuffix.substring(1);

         try {
            Class<?> clazz = Class.forName("org.maltparser.core.feature.spec.reader." + urlSuffix + "Reader");
            specReader = (FeatureSpecReader)clazz.newInstance();
         } catch (InstantiationException var7) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), var7);
         } catch (IllegalAccessException var8) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), var8);
         } catch (ClassNotFoundException var9) {
            throw new FeatureException("Could not find the feature specification reader to read the specification file: " + specModelURL.toString(), var9);
         }

         this.specReaderMap.put(specModelURL, specReader);
         if (specReader instanceof ParReader) {
            if (markingStrategy.equalsIgnoreCase("head") || markingStrategy.equalsIgnoreCase("path") || markingStrategy.equalsIgnoreCase("head+path")) {
               ((ParReader)specReader).setPplifted(true);
            }

            if (markingStrategy.equalsIgnoreCase("path") || markingStrategy.equalsIgnoreCase("head+path")) {
               ((ParReader)specReader).setPppath(true);
            }

            if (!coveredRoot.equalsIgnoreCase("none")) {
               ((ParReader)specReader).setPpcoveredRoot(true);
            }
         }

         this.specModelKeyMap.put(specModelURL, this.currentSpecModelURL);
         specReader.load(specModelURL, this);
      }
   }

   public void load(URL specModelURL) throws MaltChainedException {
      if (specModelURL == null) {
         throw new FeatureException("The URL to the feature specification model is missing or not well-formed. ");
      } else {
         FeatureSpecReader specReader = null;
         String urlSuffix = specModelURL.toString().substring(specModelURL.toString().length() - 3);
         urlSuffix = Character.toUpperCase(urlSuffix.charAt(0)) + urlSuffix.substring(1);

         try {
            Class<?> clazz = Class.forName("org.maltparser.core.feature.spec.reader." + urlSuffix + "Reader");
            specReader = (FeatureSpecReader)clazz.newInstance();
         } catch (InstantiationException var5) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), var5);
         } catch (IllegalAccessException var6) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), var6);
         } catch (ClassNotFoundException var7) {
            throw new FeatureException("Could not find the feature specification reader to read the specification file: " + specModelURL.toString(), var7);
         }

         this.specReaderMap.put(specModelURL, specReader);
         this.specModelKeyMap.put(specModelURL, this.currentSpecModelURL);
         specReader.load(specModelURL, this);
      }
   }

   public SpecificationModel getSpecificationModel(URL url, int specModelUrlIndex) {
      return (SpecificationModel)((ArrayList)this.specModelKeyMap.get(url)).get(specModelUrlIndex);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.specModelKeyMap.keySet().iterator();

      while(i$.hasNext()) {
         URL url = (URL)i$.next();

         for(int i = 0; i < ((ArrayList)this.specModelKeyMap.get(url)).size(); ++i) {
            sb.append(url.toString());
            sb.append(':');
            sb.append(i);
            sb.append('\n');
            sb.append(((SpecificationModel)((ArrayList)this.specModelKeyMap.get(url)).get(i)).toString());
         }
      }

      return sb.toString();
   }
}
