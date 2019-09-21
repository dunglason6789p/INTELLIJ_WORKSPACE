/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.propagation;

import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.Propagations;
import org.maltparser.core.propagation.spec.PropagationSpecs;
import org.maltparser.core.propagation.spec.PropagationSpecsReader;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;

public class PropagationManager {
    private final PropagationSpecs propagationSpecs = new PropagationSpecs();
    private Propagations propagations;

    public void loadSpecification(URL propagationSpecURL) throws MaltChainedException {
        PropagationSpecsReader reader = new PropagationSpecsReader();
        reader.load(propagationSpecURL, this.propagationSpecs);
    }

    public void createPropagations(DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
        this.propagations = new Propagations(this.propagationSpecs, dataFormatInstance, tableHandler);
    }

    public void propagate(Edge e) throws MaltChainedException {
        if (this.propagations != null && e != null) {
            this.propagations.propagate(e);
        }
    }

    public PropagationSpecs getPropagationSpecs() {
        return this.propagationSpecs;
    }

    public Propagations getPropagations() {
        return this.propagations;
    }
}

