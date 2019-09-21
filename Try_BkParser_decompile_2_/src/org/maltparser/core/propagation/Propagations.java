/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.propagation;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.Propagation;
import org.maltparser.core.propagation.spec.PropagationSpec;
import org.maltparser.core.propagation.spec.PropagationSpecs;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;

public class Propagations {
    private final ArrayList<Propagation> propagations;

    public Propagations(PropagationSpecs specs, DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
        this.propagations = new ArrayList(specs.size());
        for (PropagationSpec spec : specs) {
            this.propagations.add(new Propagation(spec, dataFormatInstance, tableHandler));
        }
    }

    public void propagate(Edge e) throws MaltChainedException {
        for (Propagation propagation : this.propagations) {
            propagation.propagate(e);
        }
    }

    public ArrayList<Propagation> getPropagations() {
        return this.propagations;
    }

    public String toString() {
        return "Propagations [propagations=" + this.propagations + "]";
    }
}

