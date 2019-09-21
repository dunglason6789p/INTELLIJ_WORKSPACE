/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options;

import org.maltparser.core.exception.MaltChainedException;

public class OptionException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public OptionException(String message) {
        super(message);
    }

    public OptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

