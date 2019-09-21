/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;

public interface Function {
    public void initialize(Object[] var1) throws MaltChainedException;

    public Class<?>[] getParameterTypes();

    public void update() throws MaltChainedException;
}

