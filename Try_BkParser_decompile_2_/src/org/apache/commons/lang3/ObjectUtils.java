/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.lang3.exception.CloneFailedException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ObjectUtils {
    public static final Null NULL = new Null();

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    public static <T> T firstNonNull(T ... values) {
        if (values != null) {
            for (T val : values) {
                if (val == null) continue;
                return val;
            }
        }
        return null;
    }

    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        return object1.equals(object2);
    }

    public static boolean notEqual(Object object1, Object object2) {
        return !ObjectUtils.equals(object1, object2);
    }

    public static int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    public static int hashCodeMulti(Object ... objects) {
        int hash = 1;
        if (objects != null) {
            for (Object object : objects) {
                hash = hash * 31 + ObjectUtils.hashCode(object);
            }
        }
        return hash;
    }

    public static String identityToString(Object object) {
        if (object == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        ObjectUtils.identityToString(buffer, object);
        return buffer.toString();
    }

    public static void identityToString(StringBuffer buffer, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static String toString(Object obj, String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    public static <T extends Comparable<? super T>> T min(T ... values) {
        T result = null;
        if (values != null) {
            for (T value : values) {
                if (ObjectUtils.compare(value, result, true) >= 0) continue;
                result = value;
            }
        }
        return result;
    }

    public static <T extends Comparable<? super T>> T max(T ... values) {
        T result = null;
        if (values != null) {
            for (T value : values) {
                if (ObjectUtils.compare(value, result, false) <= 0) continue;
                result = value;
            }
        }
        return result;
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return ObjectUtils.compare(c1, c2, false);
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        }
        if (c1 == null) {
            return nullGreater ? 1 : -1;
        }
        if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    public static <T> T clone(T obj) {
        if (obj instanceof Cloneable) {
            Object result;
            if (obj.getClass().isArray()) {
                Class<?> componentType = obj.getClass().getComponentType();
                if (!componentType.isPrimitive()) {
                    result = ((Object[])obj).clone();
                } else {
                    int length = Array.getLength(obj);
                    result = Array.newInstance(componentType, length);
                    while (length-- > 0) {
                        Array.set(result, length, Array.get(obj, length));
                    }
                }
            } else {
                try {
                    Method clone = obj.getClass().getMethod("clone", new Class[0]);
                    result = clone.invoke(obj, new Object[0]);
                }
                catch (NoSuchMethodException e) {
                    throw new CloneFailedException("Cloneable type " + obj.getClass().getName() + " has no clone method", e);
                }
                catch (IllegalAccessException e) {
                    throw new CloneFailedException("Cannot clone Cloneable type " + obj.getClass().getName(), e);
                }
                catch (InvocationTargetException e) {
                    throw new CloneFailedException("Exception cloning Cloneable type " + obj.getClass().getName(), e.getCause());
                }
            }
            Object checked = result;
            return (T)checked;
        }
        return null;
    }

    public static <T> T cloneIfPossible(T obj) {
        T clone = ObjectUtils.clone(obj);
        return clone == null ? obj : clone;
    }

    public static class Null
    implements Serializable {
        private static final long serialVersionUID = 7092611880189329093L;

        Null() {
        }

        private Object readResolve() {
            return NULL;
        }
    }

}

