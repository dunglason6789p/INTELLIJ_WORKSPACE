/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.headrules;

import org.maltparser.core.exception.MaltChainedException;

public class HeadRuleException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public HeadRuleException(String message) {
        super(message);
    }

    public HeadRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}

