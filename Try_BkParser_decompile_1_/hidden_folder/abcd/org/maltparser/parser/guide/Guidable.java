/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public interface Guidable {
    public void setInstance(GuideUserAction var1) throws MaltChainedException;

    public void predict(GuideUserAction var1) throws MaltChainedException;

    public boolean predictFromKBestList(GuideUserAction var1) throws MaltChainedException;
}

