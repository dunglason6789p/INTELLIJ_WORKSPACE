/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.spec;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.spec.SpecificationModel;
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
        }
        if (specModelName == null) {
            throw new FeatureException("Unknown feature model name.");
        }
        if (subModelName == null) {
            throw new FeatureException("Unknown subfeature model name.");
        }
        if (!this.specModelMap.containsKey(specModelName.toUpperCase())) {
            SpecificationModel specModel = new SpecificationModel(specModelName.toUpperCase());
            this.specModelMap.put(specModelName.toUpperCase(), specModel);
            this.currentSpecModelURL.add(specModel);
            this.specModelIntMap.put(this.counter++, specModel);
        }
        this.specModelMap.get(specModelName.toUpperCase()).add(subModelName, featureSpec);
    }

    public int getNextIndex() {
        return this.counter;
    }

    public void loadParReader(URL specModelURL, String markingStrategy, String coveredRoot) throws MaltChainedException {
        if (specModelURL == null) {
            throw new FeatureException("The URL to the feature specification model is missing or not well-formed. ");
        }
        FeatureSpecReader specReader = null;
        String urlSuffix = specModelURL.toString().substring(specModelURL.toString().length() - 3);
        urlSuffix = Character.toUpperCase(urlSuffix.charAt(0)) + urlSuffix.substring(1);
        try {
            Class<?> clazz = Class.forName("org.maltparser.core.feature.spec.reader." + urlSuffix + "Reader");
            specReader = (FeatureSpecReader)clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), e);
        }
        catch (IllegalAccessException e) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), e);
        }
        catch (ClassNotFoundException e) {
            throw new FeatureException("Could not find the feature specification reader to read the specification file: " + specModelURL.toString(), e);
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

    public void load(URL specModelURL) throws MaltChainedException {
        if (specModelURL == null) {
            throw new FeatureException("The URL to the feature specification model is missing or not well-formed. ");
        }
        FeatureSpecReader specReader = null;
        String urlSuffix = specModelURL.toString().substring(specModelURL.toString().length() - 3);
        urlSuffix = Character.toUpperCase(urlSuffix.charAt(0)) + urlSuffix.substring(1);
        try {
            Class<?> clazz = Class.forName("org.maltparser.core.feature.spec.reader." + urlSuffix + "Reader");
            specReader = (FeatureSpecReader)clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), e);
        }
        catch (IllegalAccessException e) {
            throw new FeatureException("Could not initialize the feature specification reader to read the specification file: " + specModelURL.toString(), e);
        }
        catch (ClassNotFoundException e) {
            throw new FeatureException("Could not find the feature specification reader to read the specification file: " + specModelURL.toString(), e);
        }
        this.specReaderMap.put(specModelURL, specReader);
        this.specModelKeyMap.put(specModelURL, this.currentSpecModelURL);
        specReader.load(specModelURL, this);
    }

    public SpecificationModel getSpecificationModel(URL url, int specModelUrlIndex) {
        return this.specModelKeyMap.get(url).get(specModelUrlIndex);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (URL url : this.specModelKeyMap.keySet()) {
            for (int i = 0; i < this.specModelKeyMap.get(url).size(); ++i) {
                sb.append(url.toString());
                sb.append(':');
                sb.append(i);
                sb.append('\n');
                sb.append(this.specModelKeyMap.get(url).get(i).toString());
            }
        }
        return sb.toString();
    }
}

