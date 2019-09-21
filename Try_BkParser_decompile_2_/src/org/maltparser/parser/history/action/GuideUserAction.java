/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.action;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.container.ActionContainer;

public interface GuideUserAction {
    public void addAction(ArrayList<ActionContainer> var1) throws MaltChainedException;

    public void addAction(ActionContainer[] var1) throws MaltChainedException;

    public void getAction(ArrayList<ActionContainer> var1) throws MaltChainedException;

    public void getAction(ActionContainer[] var1) throws MaltChainedException;

    public int numberOfActions();
}

