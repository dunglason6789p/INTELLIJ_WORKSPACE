/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class DGraphAddressFunction
extends AddressFunction {
    public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class};
    private AddressFunction addressFunction;
    private final String subFunctionName;
    private final DGraphSubFunction subFunction;

    public DGraphAddressFunction(String _subFunctionName) {
        this.subFunctionName = _subFunctionName;
        this.subFunction = DGraphSubFunction.valueOf(this.subFunctionName.toUpperCase());
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 1) {
            throw new SyntaxGraphException("Could not initialize DGraphAddressFunction: number of arguments are not correct. ");
        }
        if (!(arguments[0] instanceof AddressFunction)) {
            throw new SyntaxGraphException("Could not initialize DGraphAddressFunction: the second argument is not an addres function. ");
        }
        this.addressFunction = (AddressFunction)arguments[0];
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public void update() throws MaltChainedException {
        AddressValue a = this.addressFunction.getAddressValue();
        if (a.getAddress() == null) {
            this.address.setAddress(null);
        } else {
            DependencyNode node = (DependencyNode)a.getAddress();
            if (this.subFunction == DGraphSubFunction.HEAD && !node.isRoot()) {
                this.address.setAddress(node.getHead());
            } else if (this.subFunction == DGraphSubFunction.LDEP) {
                this.address.setAddress(node.getLeftmostDependent());
            } else if (this.subFunction == DGraphSubFunction.RDEP) {
                this.address.setAddress(node.getRightmostDependent());
            } else if (this.subFunction == DGraphSubFunction.RDEP2) {
                if (!node.isRoot()) {
                    this.address.setAddress(node.getRightmostDependent());
                } else {
                    this.address.setAddress(null);
                }
            } else if (this.subFunction == DGraphSubFunction.LSIB) {
                this.address.setAddress(node.getSameSideLeftSibling());
            } else if (this.subFunction == DGraphSubFunction.RSIB) {
                this.address.setAddress(node.getSameSideRightSibling());
            } else if (this.subFunction == DGraphSubFunction.PRED && !node.isRoot()) {
                this.address.setAddress(node.getPredecessor());
            } else if (this.subFunction == DGraphSubFunction.SUCC && !node.isRoot()) {
                this.address.setAddress(node.getSuccessor());
            } else if (this.subFunction == DGraphSubFunction.ANC) {
                this.address.setAddress(node.getAncestor());
            } else if (this.subFunction == DGraphSubFunction.PANC) {
                this.address.setAddress(node.getProperAncestor());
            } else if (this.subFunction == DGraphSubFunction.LDESC) {
                this.address.setAddress(node.getLeftmostDescendant());
            } else if (this.subFunction == DGraphSubFunction.PLDESC) {
                this.address.setAddress(node.getLeftmostProperDescendant());
            } else if (this.subFunction == DGraphSubFunction.RDESC) {
                this.address.setAddress(node.getRightmostDescendant());
            } else if (this.subFunction == DGraphSubFunction.PRDESC) {
                this.address.setAddress(node.getRightmostProperDescendant());
            } else {
                this.address.setAddress(null);
            }
        }
    }

    @Override
    public void update(Object[] arguments) throws MaltChainedException {
        this.update();
    }

    public AddressFunction getAddressFunction() {
        return this.addressFunction;
    }

    public String getSubFunctionName() {
        return this.subFunctionName;
    }

    public DGraphSubFunction getSubFunction() {
        return this.subFunction;
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
        if (!this.addressFunction.equals(((DGraphAddressFunction)obj).getAddressFunction())) {
            return false;
        }
        return this.subFunction.equals((Object)((DGraphAddressFunction)obj).getSubFunction());
    }

    @Override
    public String toString() {
        return this.subFunctionName + "(" + this.addressFunction.toString() + ")";
    }

    public static enum DGraphSubFunction {
        HEAD,
        LDEP,
        RDEP,
        RDEP2,
        LSIB,
        RSIB,
        PRED,
        SUCC,
        ANC,
        PANC,
        LDESC,
        PLDESC,
        RDESC,
        PRDESC;
        
    }

}

