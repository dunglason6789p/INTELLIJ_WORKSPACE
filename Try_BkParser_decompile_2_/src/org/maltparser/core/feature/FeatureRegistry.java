/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature;

import org.maltparser.core.feature.AbstractFeatureFactory;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;

public interface FeatureRegistry {
    public Object get(Class<?> var1);

    public void put(Class<?> var1, Object var2);

    public AbstractFeatureFactory getFactory(Class<?> var1);

    public SymbolTableHandler getSymbolTableHandler();

    public DataFormatInstance getDataFormatInstance();
}

