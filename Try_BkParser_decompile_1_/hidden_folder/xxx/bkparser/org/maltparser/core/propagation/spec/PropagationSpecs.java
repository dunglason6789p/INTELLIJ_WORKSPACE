package org.maltparser.core.propagation.spec;

import java.util.ArrayList;
import java.util.Iterator;

public class PropagationSpecs extends ArrayList<PropagationSpec> {
   public static final long serialVersionUID = 1L;

   public PropagationSpecs() {
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         PropagationSpec spec = (PropagationSpec)i$.next();
         sb.append(spec.toString() + "\n");
      }

      return sb.toString();
   }
}
