/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.transform.pseudo;

import org.maltparser.core.exception.MaltChainedException;

public class TransformationException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}

