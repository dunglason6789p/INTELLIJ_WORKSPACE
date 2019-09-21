/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import org.maltparser.ml.lib.MaltFeatureNode;

public interface MaltLibModel {
    public int[] predict(MaltFeatureNode[] var1);

    public int predict_one(MaltFeatureNode[] var1);
}

