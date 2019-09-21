/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;

public class GuideException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public GuideException(String message) {
        super(message);
    }

    public GuideException(String message, Throwable cause) {
        super(message, cause);
    }
}

