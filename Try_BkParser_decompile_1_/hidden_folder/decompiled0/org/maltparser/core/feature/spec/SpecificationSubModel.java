/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.spec;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecificationSubModel
implements Iterable<String> {
    private final Pattern blanks = Pattern.compile("\\s+");
    private final Set<String> featureSpecSet;
    private final String name;

    public SpecificationSubModel() {
        this("MAIN");
    }

    public SpecificationSubModel(String _name) {
        this.name = _name;
        this.featureSpecSet = new TreeSet<String>();
    }

    public void add(String featureSpec) {
        if (featureSpec != null && featureSpec.trim().length() > 0) {
            String strippedFeatureSpec = this.blanks.matcher(featureSpec).replaceAll("");
            this.featureSpecSet.add(strippedFeatureSpec);
        }
    }

    public String getSubModelName() {
        return this.name;
    }

    public int size() {
        return this.featureSpecSet.size();
    }

    @Override
    public Iterator<String> iterator() {
        return this.featureSpecSet.iterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String str : this.featureSpecSet) {
            sb.append(str);
            sb.append('\n');
        }
        return sb.toString();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.featureSpecSet == null ? 0 : this.featureSpecSet.hashCode());
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

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
        SpecificationSubModel other = (SpecificationSubModel)obj;
        if (this.featureSpecSet == null ? other.featureSpecSet != null : !this.featureSpecSet.equals(other.featureSpecSet)) {
            return false;
        }
        return !(this.name == null ? other.name != null : !this.name.equals(other.name));
    }
}

