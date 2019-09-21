/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.action;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.container.TableContainer;
import org.maltparser.parser.history.kbest.KBestList;

public interface SingleDecision
extends GuideDecision {
    public void addDecision(int var1) throws MaltChainedException;

    public void addDecision(String var1) throws MaltChainedException;

    public int getDecisionCode() throws MaltChainedException;

    public String getDecisionSymbol() throws MaltChainedException;

    public int getDecisionCode(String var1) throws MaltChainedException;

    public KBestList getKBestList() throws MaltChainedException;

    public boolean updateFromKBestList() throws MaltChainedException;

    public boolean continueWithNextDecision() throws MaltChainedException;

    public TableContainer getTableContainer();

    public TableContainer.RelationToNextDecision getRelationToNextDecision();
}

