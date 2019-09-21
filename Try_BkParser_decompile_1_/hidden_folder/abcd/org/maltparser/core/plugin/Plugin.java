/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.plugin.PluginException;

public class Plugin {
    private JarFile archive;
    private URL url;
    private String pluginName;

    public Plugin(String filename) throws MaltChainedException {
        this(new File(filename));
    }

    public Plugin(File file) throws MaltChainedException {
        try {
            this.setArchive(new JarFile(file));
            this.setUrl(new URL("file", null, file.getAbsolutePath()));
            this.register();
        }
        catch (FileNotFoundException e) {
            throw new PluginException("The file '" + file.getPath() + File.separator + file.getName() + "' cannot be found. ", e);
        }
        catch (MalformedURLException e) {
            throw new PluginException("Malformed URL to the jar file '" + this.archive.getName() + "'. ", e);
        }
        catch (IOException e) {
            throw new PluginException("The jar file '" + file.getPath() + File.separator + file.getName() + "' cannot be initialized. ", e);
        }
    }

    private void register() throws MaltChainedException {
        try {
            Attributes atts = this.archive.getManifest().getMainAttributes();
            this.pluginName = atts.getValue("Plugin-Name");
            if (this.pluginName == null) {
                this.pluginName = this.archive.getName();
            }
        }
        catch (IOException e) {
            throw new PluginException("Could not get the 'Plugin-Name' in the manifest for the plugin (jar-file). ", e);
        }
    }

    public JarFile getArchive() {
        return this.archive;
    }

    public void setArchive(JarFile archive) {
        this.archive = archive;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.pluginName + " : " + this.url.toString());
        return sb.toString();
    }
}

