/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow;

import org.maltparser.core.exception.MaltChainedException;

public class FlowException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public FlowException(String message) {
        super(message);
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }
}

