/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.container;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;

public interface DecisionPropertyTable {
    public boolean continueWithNextDecision(int var1) throws MaltChainedException;

    public boolean continueWithNextDecision(String var1) throws MaltChainedException;

    public Table getTableForNextDecision(int var1) throws MaltChainedException;

    public Table getTableForNextDecision(String var1) throws MaltChainedException;
}

