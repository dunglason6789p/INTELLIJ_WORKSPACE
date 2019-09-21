/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class SegmentFeature {
    private int label;
    private SortedSet<Integer> featset;

    public SegmentFeature(int l, SortedSet<Integer> set) {
        this.setLabel(l);
        this.setFeatset(set);
    }

    public SortedSet<Integer> getFeatset() {
        return this.featset;
    }

    public int getLabel() {
        return this.label;
    }

    public void setFeatset(SortedSet<Integer> featset) {
        this.featset = new TreeSet<Integer>();
        this.featset.addAll(featset);
    }

    public void setLabel(int label) {
        this.label = label;
    }
}

