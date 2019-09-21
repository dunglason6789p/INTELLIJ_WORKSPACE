/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j;

class CategoryKey {
    String name;
    int hashCache;
    static /* synthetic */ Class class$org$apache$log4j$CategoryKey;

    CategoryKey(String name) {
        this.name = name;
        this.hashCache = name.hashCode();
    }

    public final int hashCode() {
        return this.hashCache;
    }

    public final boolean equals(Object rArg) {
        if (this == rArg) {
            return true;
        }
        if (rArg != null && (class$org$apache$log4j$CategoryKey == null ? (class$org$apache$log4j$CategoryKey = CategoryKey.class$("org.apache.log4j.CategoryKey")) : class$org$apache$log4j$CategoryKey) == rArg.getClass()) {
            return this.name.equals(((CategoryKey)rArg).name);
        }
        return false;
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

