/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.propagation.spec;

import java.util.ArrayList;
import org.maltparser.core.propagation.spec.PropagationSpec;

public class PropagationSpecs
extends ArrayList<PropagationSpec> {
    public static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PropagationSpec spec : this) {
            sb.append(spec.toString() + "\n");
        }
        return sb.toString();
    }
}

