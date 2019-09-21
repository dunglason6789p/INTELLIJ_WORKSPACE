/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.exception;

public class MaltChainedException
extends Exception {
    public static final long serialVersionUID = 8045568022124816379L;
    private final Throwable cause;

    public MaltChainedException(String message) {
        this(message, null);
    }

    public MaltChainedException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    public String getMessageChain() {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = this; t != null; t = t.getCause()) {
            if (t.getMessage() == null || !(t instanceof MaltChainedException)) continue;
            sb.append(t.getMessage() + "\n");
        }
        return sb.toString();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (this.cause != null) {
            this.cause.printStackTrace();
        }
    }
}

