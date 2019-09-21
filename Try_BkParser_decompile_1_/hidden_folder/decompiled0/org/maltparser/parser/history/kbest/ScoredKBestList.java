/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.kbest;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.kbest.KBestList;
import org.maltparser.parser.history.kbest.ScoredCandidate;

public class ScoredKBestList
extends KBestList {
    public ScoredKBestList(SingleDecision action) {
        this(-1, action);
    }

    public ScoredKBestList(Integer k, SingleDecision action) {
        super(k, action);
    }

    @Override
    protected void initKBestList() {
        for (int i = 0; i < this.k; ++i) {
            this.kBestList.add(new ScoredCandidate());
        }
    }

    public void add(int actionCode, float score) throws MaltChainedException {
        if (this.k != -1 && this.addCandidateIndex >= this.k) {
            return;
        }
        if (this.addCandidateIndex >= this.kBestList.size()) {
            this.kBestList.add(new ScoredCandidate());
        }
        if (!(this.kBestList.get(this.addCandidateIndex) instanceof ScoredCandidate)) {
            super.add(actionCode);
            return;
        }
        ScoredCandidate scand = (ScoredCandidate)this.kBestList.get(this.addCandidateIndex);
        scand.setActionCode(actionCode);
        scand.setScore(Float.valueOf(score));
        if (this.addCandidateIndex == 0) {
            if (this.decision instanceof SingleDecision) {
                this.decision.addDecision(actionCode);
            }
            ++this.topCandidateIndex;
        }
        ++this.addCandidateIndex;
    }

    public void add(String symbol, float score) throws MaltChainedException {
        if (this.decision instanceof SingleDecision) {
            this.add(this.decision.getDecisionCode(symbol), score);
        }
    }

    public float peekNextKBestScore() {
        if (!(this.kBestList.get(this.addCandidateIndex) instanceof ScoredCandidate)) {
            return Float.NaN;
        }
        if (this.addCandidateIndex != 0 && this.topCandidateIndex < this.addCandidateIndex && this.topCandidateIndex < this.kBestList.size()) {
            return ((ScoredCandidate)this.kBestList.get(this.topCandidateIndex)).getScore();
        }
        return Float.NaN;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

