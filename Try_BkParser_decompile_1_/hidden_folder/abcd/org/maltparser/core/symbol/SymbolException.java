/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;

public class SymbolException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public SymbolException(String message) {
        super(message);
    }

    public SymbolException(String message, Throwable cause) {
        super(message, cause);
    }
}

