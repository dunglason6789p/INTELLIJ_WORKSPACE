/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.parser;

import java.util.ArrayList;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.MultipleFeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.lw.parser.McoModel;
import org.maltparser.ml.lib.FeatureMap;
import org.maltparser.ml.lib.LibException;
import org.maltparser.ml.lib.MaltFeatureNode;
import org.maltparser.ml.lib.MaltLibModel;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;

public class LWClassifier {
    private final FeatureMap featureMap;
    private final boolean excludeNullValues;
    private final MaltLibModel model;

    public LWClassifier(McoModel mcoModel, String prefixFileName, boolean _excludeNullValues) {
        this.model = (MaltLibModel)mcoModel.getMcoEntryObject(prefixFileName + ".moo");
        this.featureMap = (FeatureMap)mcoModel.getMcoEntryObject(prefixFileName + ".map");
        this.excludeNullValues = _excludeNullValues;
    }

    public boolean predict(FeatureVector featureVector, SingleDecision decision, boolean one_prediction) throws MaltChainedException {
        ArrayList<MaltFeatureNode> featureList = new ArrayList<MaltFeatureNode>();
        int size = featureVector.size();
        for (int i = 1; i <= size; ++i) {
            FeatureValue featureValue = featureVector.getFeatureValue(i - 1);
            if (featureValue == null || this.excludeNullValues && featureValue.isNullValue()) continue;
            if (!featureValue.isMultiple()) {
                SingleFeatureValue singleFeatureValue = (SingleFeatureValue)featureValue;
                int index = this.featureMap.getIndex(i, singleFeatureValue.getIndexCode());
                if (index == -1 || singleFeatureValue.getValue() == 0.0) continue;
                featureList.add(new MaltFeatureNode(index, singleFeatureValue.getValue()));
                continue;
            }
            for (Integer value : ((MultipleFeatureValue)featureValue).getCodes()) {
                int v = this.featureMap.getIndex(i, value);
                if (v == -1) continue;
                featureList.add(new MaltFeatureNode(v, 1.0));
            }
        }
        try {
            if (one_prediction) {
                decision.getKBestList().add(this.model.predict_one(featureList.toArray(new MaltFeatureNode[featureList.size()])));
            } else {
                decision.getKBestList().addList(this.model.predict(featureList.toArray(new MaltFeatureNode[featureList.size()])));
            }
        }
        catch (OutOfMemoryError e) {
            throw new LibException("Out of memory. Please increase the Java heap size (-Xmx<size>). ", e);
        }
        return true;
    }
}

