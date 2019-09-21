/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature;

import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.spec.SpecificationModel;
import org.maltparser.core.feature.spec.SpecificationModels;
import org.maltparser.core.feature.system.FeatureEngine;

public class FeatureModelManager {
    private final SpecificationModels specModels = new SpecificationModels();
    private final FeatureEngine featureEngine;

    public FeatureModelManager(FeatureEngine engine) throws MaltChainedException {
        this.featureEngine = engine;
    }

    public void loadSpecification(URL specModelURL) throws MaltChainedException {
        this.specModels.load(specModelURL);
    }

    public void loadParSpecification(URL specModelURL, String markingStrategy, String coveredRoot) throws MaltChainedException {
        this.specModels.loadParReader(specModelURL, markingStrategy, coveredRoot);
    }

    public FeatureModel getFeatureModel(URL specModelURL, int specModelUrlIndex, FeatureRegistry registry, String dataSplitColumn, String dataSplitStructure) throws MaltChainedException {
        return new FeatureModel(this.specModels.getSpecificationModel(specModelURL, specModelUrlIndex), registry, this.featureEngine, dataSplitColumn, dataSplitStructure);
    }

    public FeatureModel getFeatureModel(URL specModelURL, FeatureRegistry registry, String dataSplitColumn, String dataSplitStructure) throws MaltChainedException {
        return new FeatureModel(this.specModels.getSpecificationModel(specModelURL, 0), registry, this.featureEngine, dataSplitColumn, dataSplitStructure);
    }

    public FeatureModel getFeatureModel(SpecificationModel specModel, FeatureRegistry registry, String dataSplitColumn, String dataSplitStructure) throws MaltChainedException {
        return new FeatureModel(specModel, registry, this.featureEngine, dataSplitColumn, dataSplitStructure);
    }

    public SpecificationModels getSpecModels() {
        return this.specModels;
    }

    public FeatureEngine getFeatureEngine() {
        return this.featureEngine;
    }

    public String toString() {
        return this.specModels.toString();
    }
}

