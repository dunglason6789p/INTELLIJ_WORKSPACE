/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.plugin;

import org.maltparser.core.exception.MaltChainedException;

public class PluginException
extends MaltChainedException {
    public static final long serialVersionUID = 8045568022124816379L;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}

