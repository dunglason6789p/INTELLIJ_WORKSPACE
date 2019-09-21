/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.core.exception.MaltChainedException;

public class MaltParserRunnable
implements Runnable {
    private final List<String[]> inputSentences;
    private List<String[]> outputSentences;
    private final ConcurrentMaltParserModel model;

    public MaltParserRunnable(List<String[]> sentences, ConcurrentMaltParserModel _model) {
        this.inputSentences = new ArrayList<String[]>(sentences);
        this.outputSentences = null;
        this.model = _model;
    }

    @Override
    public void run() {
        try {
            this.outputSentences = this.model.parseSentences(this.inputSentences);
        }
        catch (MaltChainedException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getOutputSentences() {
        if (this.outputSentences == null) {
            return Collections.synchronizedList(new ArrayList());
        }
        return Collections.synchronizedList(new ArrayList<String[]>(this.outputSentences));
    }
}

