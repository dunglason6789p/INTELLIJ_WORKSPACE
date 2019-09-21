/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import vn.edu.vnu.uet.nlp.segmenter.SyllableType;

public class SyllabelFeature {
    private String syllabel;
    private SyllableType type;
    private int label;

    public SyllabelFeature(String _syllabel, SyllableType _type, int _label) {
        this.syllabel = _syllabel;
        this.type = _type;
        this.label = _label;
    }

    public int getLabel() {
        return this.label;
    }

    public String getSyllabel() {
        return this.syllabel;
    }

    public SyllableType getType() {
        return this.type;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String toString() {
        return "(" + this.syllabel + ", " + (Object)((Object)this.type) + ", " + this.label + ")";
    }
}

