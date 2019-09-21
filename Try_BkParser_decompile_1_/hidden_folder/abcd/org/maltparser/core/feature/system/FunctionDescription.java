/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.system;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.AbstractFeatureFactory;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;

public class FunctionDescription {
    private final String name;
    private final Class<?> functionClass;
    private final boolean hasSubfunctions;
    private final boolean hasFactory;

    public FunctionDescription(String _name, Class<?> _functionClass, boolean _hasSubfunctions, boolean _hasFactory) {
        this.name = _name;
        this.functionClass = _functionClass;
        this.hasSubfunctions = _hasSubfunctions;
        this.hasFactory = _hasFactory;
    }

    public Function newFunction(FeatureRegistry registry) throws MaltChainedException {
        if (this.hasFactory) {
            return registry.getFactory(this.functionClass).makeFunction(this.name, registry);
        }
        Constructor<?>[] constructors = this.functionClass.getConstructors();
        if (constructors.length == 0) {
            try {
                return (Function)this.functionClass.newInstance();
            }
            catch (InstantiationException e) {
                throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
            }
            catch (IllegalAccessException e) {
                throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
            }
        }
        Class<?>[] params = constructors[0].getParameterTypes();
        if (params.length == 0) {
            try {
                return (Function)this.functionClass.newInstance();
            }
            catch (InstantiationException e) {
                throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
            }
            catch (IllegalAccessException e) {
                throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
            }
        }
        Object[] arguments = new Object[params.length];
        for (int i = 0; i < params.length; ++i) {
            if (this.hasSubfunctions && params[i] == String.class) {
                arguments[i] = this.name;
                continue;
            }
            arguments[i] = registry.get(params[i]);
            if (arguments[i] != null) continue;
            return null;
        }
        try {
            return (Function)constructors[0].newInstance(arguments);
        }
        catch (InstantiationException e) {
            throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
        }
        catch (IllegalAccessException e) {
            throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
        }
        catch (InvocationTargetException e) {
            throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", e);
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getFunctionClass() {
        return this.functionClass;
    }

    public boolean isHasSubfunctions() {
        return this.hasSubfunctions;
    }

    public boolean isHasFactory() {
        return this.hasFactory;
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
        if (!this.name.equalsIgnoreCase(((FunctionDescription)obj).getName())) {
            return false;
        }
        return this.functionClass.equals(((FunctionDescription)obj).getFunctionClass());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append("->");
        sb.append(this.functionClass.getName());
        return sb.toString();
    }
}

