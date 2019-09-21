/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.chainsaw;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;

class ExitAction
extends AbstractAction {
    private static final Logger LOG = Logger.getLogger(class$org$apache$log4j$chainsaw$ExitAction == null ? (class$org$apache$log4j$chainsaw$ExitAction = ExitAction.class$("org.apache.log4j.chainsaw.ExitAction")) : class$org$apache$log4j$chainsaw$ExitAction);
    public static final ExitAction INSTANCE = new ExitAction();
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$ExitAction;

    private ExitAction() {
    }

    public void actionPerformed(ActionEvent aIgnore) {
        LOG.info("shutting down");
        System.exit(0);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

