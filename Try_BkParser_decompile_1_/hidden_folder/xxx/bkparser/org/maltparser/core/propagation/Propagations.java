package org.maltparser.core.propagation;

import java.util.ArrayList;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.spec.PropagationSpec;
import org.maltparser.core.propagation.spec.PropagationSpecs;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;

public class Propagations {
   private final ArrayList<Propagation> propagations;

   public Propagations(PropagationSpecs specs, DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
      this.propagations = new ArrayList(specs.size());
      Iterator i$ = specs.iterator();

      while(i$.hasNext()) {
         PropagationSpec spec = (PropagationSpec)i$.next();
         this.propagations.add(new Propagation(spec, dataFormatInstance, tableHandler));
      }

   }

   public void propagate(Edge e) throws MaltChainedException {
      Iterator i$ = this.propagations.iterator();

      while(i$.hasNext()) {
         Propagation propagation = (Propagation)i$.next();
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
