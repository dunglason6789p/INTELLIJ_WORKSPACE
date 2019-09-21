/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class EnumUtils {
    public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumClass) {
        LinkedHashMap<String, Enum> map = new LinkedHashMap<String, Enum>();
        for (Enum e : (Enum[])enumClass.getEnumConstants()) {
            map.put(e.name(), e);
        }
        return map;
    }

    public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
        return new ArrayList<E>(Arrays.asList(enumClass.getEnumConstants()));
    }

    public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
        if (enumName == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName) {
        if (enumName == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, enumName);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

