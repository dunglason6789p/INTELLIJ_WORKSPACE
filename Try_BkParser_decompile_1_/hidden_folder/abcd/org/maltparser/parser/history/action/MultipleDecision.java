/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.action;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.SingleDecision;

public interface MultipleDecision
extends GuideDecision {
    public SingleDecision getSingleDecision(int var1) throws MaltChainedException;
}

