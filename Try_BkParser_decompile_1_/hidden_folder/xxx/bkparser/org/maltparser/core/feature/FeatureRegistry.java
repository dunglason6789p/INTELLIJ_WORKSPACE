package org.maltparser.core.feature;

import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;

public interface FeatureRegistry {
   Object get(Class<?> var1);

   void put(Class<?> var1, Object var2);

   AbstractFeatureFactory getFactory(Class<?> var1);

   SymbolTableHandler getSymbolTableHandler();

   DataFormatInstance getDataFormatInstance();
}
