/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.hust.nlp.conll.model;

import java.util.List;
import vn.edu.hust.nlp.conll.model.CONLLToken;

public class Sentence {
    private List<CONLLToken> tokens;

    public Sentence(List<CONLLToken> tokens) {
        this.tokens = tokens;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.tokens.size() == 0) {
            return "";
        }
        sb.append(this.tokens.get(0).toString());
        for (int i = 1; i < this.tokens.size(); ++i) {
            sb.append("\n" + this.tokens.get(i).toString());
        }
        return sb.toString();
    }

    public List<CONLLToken> getTokens() {
        return this.tokens;
    }

    public void setTokens(List<CONLLToken> tokens) {
        this.tokens = tokens;
    }
}

