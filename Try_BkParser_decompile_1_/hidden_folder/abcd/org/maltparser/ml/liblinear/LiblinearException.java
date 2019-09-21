/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.liblinear;

import org.maltparser.core.exception.MaltChainedException;

public class LiblinearException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public LiblinearException(String message) {
        super(message);
    }

    public LiblinearException(String message, Throwable cause) {
        super(message, cause);
    }
}

