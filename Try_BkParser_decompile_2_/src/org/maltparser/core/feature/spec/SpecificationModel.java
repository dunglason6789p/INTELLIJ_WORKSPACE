/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.spec;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.spec.SpecificationSubModel;

public class SpecificationModel
implements Iterable<SpecificationSubModel> {
    private final String specModelName;
    private final LinkedHashMap<String, SpecificationSubModel> subModelMap;

    public SpecificationModel() throws MaltChainedException {
        this(null);
    }

    public SpecificationModel(String _specModelName) throws MaltChainedException {
        this.specModelName = _specModelName;
        this.subModelMap = new LinkedHashMap();
    }

    public void add(String featureSpec) throws MaltChainedException {
        this.add("MAIN", featureSpec);
    }

    public void add(String subModelName, String featureSpec) throws MaltChainedException {
        if (subModelName == null || subModelName.length() < 1 || subModelName.toUpperCase().equals("MAIN")) {
            if (!this.subModelMap.containsKey("MAIN")) {
                this.subModelMap.put("MAIN", new SpecificationSubModel("MAIN"));
            }
            this.subModelMap.get("MAIN").add(featureSpec);
        } else {
            if (!this.subModelMap.containsKey(subModelName.toUpperCase())) {
                this.subModelMap.put(subModelName.toUpperCase(), new SpecificationSubModel(subModelName.toUpperCase()));
            }
            this.subModelMap.get(subModelName.toUpperCase()).add(featureSpec);
        }
    }

    public String getSpecModelName() {
        return this.specModelName;
    }

    @Override
    public Iterator<SpecificationSubModel> iterator() {
        return this.subModelMap.values().iterator();
    }

    public int size() {
        return this.subModelMap.size();
    }

    public SpecificationSubModel getSpecSubModel(String subModelName) {
        return this.subModelMap.get(subModelName);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SpecificationSubModel subModel : this) {
            if (subModel.size() <= 0) continue;
            if (this.subModelMap.size() != 1 || subModel.getSubModelName().equalsIgnoreCase("MAIN")) {
                sb.append(subModel.getSubModelName());
                sb.append('\n');
            }
            sb.append(subModel.toString());
        }
        return sb.toString();
    }
}

