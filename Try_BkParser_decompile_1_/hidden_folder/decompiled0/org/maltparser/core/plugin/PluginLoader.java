/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.plugin.JarLoader;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginException;

public class PluginLoader
implements Iterable<Plugin> {
    private HashMap<String, Plugin> plugins;
    private TreeSet<String> pluginNames = new TreeSet();
    private File[] directories;
    private JarLoader jarLoader = null;
    private static PluginLoader uniqueInstance = new PluginLoader();

    private PluginLoader() {
        this.plugins = new HashMap();
    }

    public static PluginLoader instance() {
        return uniqueInstance;
    }

    public void loadPlugins(File pluginDirectory) throws MaltChainedException {
        this.loadPlugins(new File[]{pluginDirectory});
    }

    public void loadPlugins(File[] pluginDirectories) throws MaltChainedException {
        this.directories = new File[pluginDirectories.length];
        for (int i = 0; i < this.directories.length; ++i) {
            this.directories[i] = pluginDirectories[i];
        }
        try {
            Class<?> self = Class.forName("org.maltparser.core.plugin.PluginLoader");
            this.jarLoader = new JarLoader(self.getClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new PluginException("The class 'org.maltparser.core.plugin.PluginLoader' not found. ", e);
        }
        this.traverseDirectories();
    }

    private void traverseDirectories() throws MaltChainedException {
        for (int i = 0; i < this.directories.length; ++i) {
            this.traverseDirectory(this.directories[i]);
        }
    }

    private void traverseDirectory(File directory) throws MaltChainedException {
        if (!directory.isDirectory() && directory.getName().endsWith(".jar")) {
            this.pluginNames.add(directory.getAbsolutePath());
            Plugin plugin = new Plugin(directory);
            this.plugins.put(directory.getAbsolutePath(), plugin);
            if (!this.jarLoader.readJarFile(plugin.getUrl())) {
                this.plugins.remove(directory.getAbsolutePath());
            }
        }
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; ++i) {
                this.traverseDirectory(new File(directory, children[i]));
            }
        }
    }

    public Class<?> getClass(String classname) {
        if (this.jarLoader != null) {
            return this.jarLoader.getClass(classname);
        }
        return null;
    }

    public Object newInstance(String classname, Class<?>[] argTypes, Object[] args) throws MaltChainedException {
        try {
            if (this.jarLoader == null) {
                return null;
            }
            Class<?> clazz = this.jarLoader.getClass(classname);
            Object o = null;
            if (clazz == null) {
                return null;
            }
            if (argTypes != null) {
                Constructor<?> constructor = clazz.getConstructor(argTypes);
                o = constructor.newInstance(args);
            } else {
                o = clazz.newInstance();
            }
            return o;
        }
        catch (NoSuchMethodException e) {
            throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", e);
        }
        catch (InstantiationException e) {
            throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", e);
        }
        catch (IllegalAccessException e) {
            throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", e);
        }
        catch (InvocationTargetException e) {
            throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", e);
        }
    }

    @Override
    public Iterator<Plugin> iterator() {
        return this.plugins.values().iterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Plugin plugin : this) {
            sb.append(plugin.toString() + "\n");
        }
        return sb.toString();
    }
}

