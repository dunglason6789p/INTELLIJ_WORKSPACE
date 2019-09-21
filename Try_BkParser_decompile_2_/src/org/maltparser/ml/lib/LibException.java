/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

import org.maltparser.core.exception.MaltChainedException;

public class LibException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public LibException(String message) {
        super(message);
    }

    public LibException(String message, Throwable cause) {
        super(message, cause);
    }
}

