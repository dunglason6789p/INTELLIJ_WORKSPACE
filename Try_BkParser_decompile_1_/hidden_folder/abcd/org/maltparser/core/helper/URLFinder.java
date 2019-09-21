/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;

public class URLFinder {
    public URL findURL(String fileString) throws MaltChainedException {
        File specFile = new File(fileString);
        try {
            if (specFile.exists()) {
                return new URL("file:///" + specFile.getAbsolutePath());
            }
            if (fileString.startsWith("http:") || fileString.startsWith("file:") || fileString.startsWith("ftp:") || fileString.startsWith("jar:")) {
                return new URL(fileString);
            }
            return this.findURLinJars(fileString);
        }
        catch (MalformedURLException e) {
            throw new MaltChainedException("Malformed URL: " + fileString, e);
        }
    }

    public URL findURLinJars(String fileString) throws MaltChainedException {
        try {
            if (this.getClass().getResource(fileString) != null) {
                return this.getClass().getResource(fileString);
            }
            for (Plugin plugin : PluginLoader.instance()) {
                URL url = null;
                url = !fileString.startsWith("/") ? new URL("jar:" + plugin.getUrl() + "!/" + fileString) : new URL("jar:" + plugin.getUrl() + "!" + fileString);
                try {
                    InputStream is = url.openStream();
                    is.close();
                }
                catch (IOException e) {
                    continue;
                }
                return url;
            }
            return null;
        }
        catch (MalformedURLException e) {
            throw new MaltChainedException("Malformed URL: " + fileString, e);
        }
    }
}

