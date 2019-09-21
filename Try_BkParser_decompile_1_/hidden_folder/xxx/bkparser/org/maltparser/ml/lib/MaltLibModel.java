package org.maltparser.ml.lib;

public interface MaltLibModel {
   int[] predict(MaltFeatureNode[] var1);

   int predict_one(MaltFeatureNode[] var1);
}
