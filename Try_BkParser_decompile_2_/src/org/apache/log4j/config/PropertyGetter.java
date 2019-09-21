/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.helpers.LogLog;

public class PropertyGetter {
    protected static final Object[] NULL_ARG = new Object[0];
    protected Object obj;
    protected PropertyDescriptor[] props;
    static /* synthetic */ Class class$java$lang$String;
    static /* synthetic */ Class class$org$apache$log4j$Priority;

    public PropertyGetter(Object obj) throws IntrospectionException {
        BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
        this.props = bi.getPropertyDescriptors();
        this.obj = obj;
    }

    public static void getProperties(Object obj, PropertyCallback callback, String prefix) {
        try {
            new PropertyGetter(obj).getProperties(callback, prefix);
        }
        catch (IntrospectionException ex) {
            LogLog.error("Failed to introspect object " + obj, ex);
        }
    }

    public void getProperties(PropertyCallback callback, String prefix) {
        for (int i = 0; i < this.props.length; ++i) {
            Method getter = this.props[i].getReadMethod();
            if (getter == null || !this.isHandledType(getter.getReturnType())) continue;
            String name = this.props[i].getName();
            try {
                Object result = getter.invoke(this.obj, NULL_ARG);
                if (result == null) continue;
                callback.foundProperty(this.obj, prefix, name, result);
                continue;
            }
            catch (IllegalAccessException ex) {
                LogLog.warn("Failed to get value of property " + name);
                continue;
            }
            catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof InterruptedException || ex.getTargetException() instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.warn("Failed to get value of property " + name);
                continue;
            }
            catch (RuntimeException ex) {
                LogLog.warn("Failed to get value of property " + name);
            }
        }
    }

    protected boolean isHandledType(Class type) {
        return (class$java$lang$String == null ? (class$java$lang$String = PropertyGetter.class$("java.lang.String")) : class$java$lang$String).isAssignableFrom(type) || Integer.TYPE.isAssignableFrom(type) || Long.TYPE.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type) || (class$org$apache$log4j$Priority == null ? (class$org$apache$log4j$Priority = PropertyGetter.class$("org.apache.log4j.Priority")) : class$org$apache$log4j$Priority).isAssignableFrom(type);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public static interface PropertyCallback {
        public void foundProperty(Object var1, String var2, String var3, Object var4);
    }

}

