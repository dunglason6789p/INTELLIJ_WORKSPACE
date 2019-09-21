/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import org.maltparser.core.helper.HashMap;

public class FeatureMap
implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private final HashMap<Long, Integer> map = new HashMap();
    private int featureCounter = 1;

    public int addIndex(int featurePosition, int code) {
        long key = (long)featurePosition << 48 | (long)code;
        Integer index = this.map.get(key);
        if (index == null) {
            index = this.featureCounter++;
            this.map.put(key, index);
        }
        return index;
    }

    public int getIndex(int featurePosition, int code) {
        Integer index = this.map.get((long)featurePosition << 48 | (long)code);
        return index == null ? -1 : index;
    }

    public int addIndex(int featurePosition, int code1, int code2) {
        long key = (long)featurePosition << 48 | (long)code1 << 24 | (long)code2;
        Integer index = this.map.get(key);
        if (index == null) {
            index = this.featureCounter++;
            this.map.put(key, index);
        }
        return index;
    }

    public int setIndex(long key, int index) {
        return this.map.put(key, index);
    }

    public int decrementIndex(Long key) {
        Integer index = this.map.get(key);
        if (index != null) {
            this.map.put(key, index - 1);
        }
        return index != null ? index - 1 : -1;
    }

    public void decrementfeatureCounter() {
        --this.featureCounter;
    }

    public Integer removeIndex(long key) {
        return this.map.remove(key);
    }

    public int getIndex(int featurePosition, int code1, int code2) {
        Integer index = this.map.get((long)featurePosition << 48 | (long)code1 << 24 | (long)code2);
        return index == null ? -1 : index;
    }

    public int size() {
        return this.map.size();
    }

    public Long[] reverseMap() {
        Long[] reverseMap = new Long[this.map.size() + 1];
        Iterator<Long> i$ = this.map.keySet().iterator();
        while (i$.hasNext()) {
            Long key;
            reverseMap[this.map.get((Object)key).intValue()] = key = i$.next();
        }
        return reverseMap;
    }

    public void setFeatureCounter(int featureCounter) {
        this.featureCounter = featureCounter;
    }

    public int getFeatureCounter() {
        return this.featureCounter;
    }
}

