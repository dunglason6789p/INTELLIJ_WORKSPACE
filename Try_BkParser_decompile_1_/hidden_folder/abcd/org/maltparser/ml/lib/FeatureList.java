/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import java.util.ArrayList;
import org.maltparser.ml.lib.MaltFeatureNode;

public class FeatureList {
    private static final long serialVersionUID = 7526471155622776147L;
    private final ArrayList<MaltFeatureNode> list;

    public FeatureList() {
        this.list = new ArrayList();
    }

    public FeatureList(int size) {
        this.list = new ArrayList(size);
    }

    public void add(MaltFeatureNode x) {
        if (this.list.size() == 0 || this.list.get(this.list.size() - 1).compareTo(x) <= 0) {
            this.list.add(x);
        } else {
            int low = 0;
            int high = this.list.size() - 1;
            while (low <= high) {
                int mid = (low + high) / 2;
                MaltFeatureNode y = this.list.get(mid);
                if (y.compareTo(x) < 0) {
                    low = mid + 1;
                    continue;
                }
                if (y.compareTo(x) <= 0) break;
                high = mid - 1;
            }
            this.list.add(low, x);
        }
    }

    public void add(int index, double value) {
        this.add(new MaltFeatureNode(index, value));
    }

    public MaltFeatureNode get(int i) {
        if (i < 0 || i >= this.list.size()) {
            return null;
        }
        return this.list.get(i);
    }

    public void clear() {
        this.list.clear();
    }

    public int size() {
        return this.list.size();
    }

    public MaltFeatureNode[] toArray() {
        MaltFeatureNode[] nodes = new MaltFeatureNode[this.list.size()];
        int len = nodes.length;
        for (int i = 0; i < len; ++i) {
            nodes[i] = this.list.get(i);
        }
        return nodes;
    }
}

