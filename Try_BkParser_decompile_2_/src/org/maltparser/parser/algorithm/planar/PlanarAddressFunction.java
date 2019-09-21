/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.planar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.algorithm.planar.PlanarConfig;

public final class PlanarAddressFunction
extends AddressFunction {
    public static final Class<?>[] paramTypes = new Class[]{Integer.class};
    private final String subFunctionName;
    private final PlanarSubFunction subFunction;
    private final AlgoritmInterface parsingAlgorithm;
    private int index;

    public PlanarAddressFunction(String _subFunctionName, AlgoritmInterface _parsingAlgorithm) {
        this.subFunctionName = _subFunctionName;
        this.subFunction = PlanarSubFunction.valueOf(this.subFunctionName.toUpperCase());
        this.parsingAlgorithm = _parsingAlgorithm;
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 1) {
            throw new ParsingException("Could not initialize " + this.getClass().getName() + ": number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof Integer)) {
            throw new ParsingException("Could not initialize " + this.getClass().getName() + ": the first argument is not an integer. ");
        }
        this.setIndex((Integer)arguments[0]);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public void update() throws MaltChainedException {
        this.update((PlanarConfig)this.parsingAlgorithm.getCurrentParserConfiguration());
    }

    @Override
    public void update(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 1 || !(arguments[0] instanceof PlanarConfig)) {
            throw new ParsingException("Arguments to the planar address function is not correct. ");
        }
        this.update((PlanarConfig)arguments[0]);
    }

    private void update(PlanarConfig config) throws MaltChainedException {
        if (this.subFunction == PlanarSubFunction.STACK) {
            this.address.setAddress(config.getStackNode(this.index));
        } else if (this.subFunction == PlanarSubFunction.INPUT) {
            this.address.setAddress(config.getInputNode(this.index));
        } else {
            this.address.setAddress(null);
        }
    }

    public String getSubFunctionName() {
        return this.subFunctionName;
    }

    public PlanarSubFunction getSubFunction() {
        return this.subFunction;
    }

    @Override
    public AddressValue getAddressValue() {
        return this.address;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
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
        PlanarAddressFunction other = (PlanarAddressFunction)obj;
        if (this.index != other.index) {
            return false;
        }
        if (this.parsingAlgorithm == null ? other.parsingAlgorithm != null : !this.parsingAlgorithm.equals(other.parsingAlgorithm)) {
            return false;
        }
        return !(this.subFunction == null ? other.subFunction != null : !this.subFunction.equals((Object)other.subFunction));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.subFunctionName);
        sb.append('[');
        sb.append(this.index);
        sb.append(']');
        return sb.toString();
    }

    public static enum PlanarSubFunction {
        STACK,
        INPUT;
        
    }

}

