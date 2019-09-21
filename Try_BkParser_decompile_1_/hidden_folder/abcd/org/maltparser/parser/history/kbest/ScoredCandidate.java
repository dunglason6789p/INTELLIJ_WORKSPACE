/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.kbest;

import org.maltparser.parser.history.kbest.Candidate;

public class ScoredCandidate
extends Candidate {
    protected float score;

    public float getScore() {
        return this.score;
    }

    public void setScore(Float score) {
        this.score = score.floatValue();
    }

    @Override
    public void reset() {
        super.reset();
        this.score = Float.NaN;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ScoredCandidate item = (ScoredCandidate)obj;
        return this.actionCode == item.actionCode && this.score == item.score;
    }

    @Override
    public int hashCode() {
        return (217 + this.actionCode) * 31 + Float.floatToIntBits(this.score);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append('\t');
        sb.append(this.score);
        return sb.toString();
    }
}

