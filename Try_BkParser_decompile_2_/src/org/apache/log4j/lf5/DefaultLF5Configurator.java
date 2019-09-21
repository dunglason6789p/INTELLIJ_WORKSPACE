/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.lf5;

import java.io.IOException;
import java.net.URL;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;

public class DefaultLF5Configurator
implements Configurator {
    static /* synthetic */ Class class$org$apache$log4j$lf5$DefaultLF5Configurator;

    private DefaultLF5Configurator() {
    }

    public static void configure() throws IOException {
        String resource;
        URL configFileResource = (class$org$apache$log4j$lf5$DefaultLF5Configurator == null ? (class$org$apache$log4j$lf5$DefaultLF5Configurator = DefaultLF5Configurator.class$("org.apache.log4j.lf5.DefaultLF5Configurator")) : class$org$apache$log4j$lf5$DefaultLF5Configurator).getResource(resource = "/org/apache/log4j/lf5/config/defaultconfig.properties");
        if (configFileResource == null) {
            throw new IOException("Error: Unable to open the resource" + resource);
        }
        PropertyConfigurator.configure(configFileResource);
    }

    public void doConfigure(URL configURL, LoggerRepository repository) {
        throw new IllegalStateException("This class should NOT be instantiated!");
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

