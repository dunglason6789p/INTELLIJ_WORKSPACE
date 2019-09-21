/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.SecureClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.plugin.PluginException;

public class JarLoader
extends SecureClassLoader {
    private HashMap<String, byte[]> classByteArrays = new HashMap();
    private HashMap<String, Class<?>> classes = new HashMap();

    public JarLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) {
        int i;
        byte[] buf;
        String urlName = name.replace('.', '/');
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && (i = name.lastIndexOf(46)) >= 0) {
            sm.checkPackageDefinition(name.substring(0, i));
        }
        if ((buf = this.classByteArrays.get(urlName)) != null) {
            return this.defineClass(null, buf, 0, buf.length);
        }
        return null;
    }

    public boolean readJarFile(URL jarUrl) throws MaltChainedException {
        HashSet<URL> pluginXMLs;
        block20 : {
            JarFile jarFile;
            pluginXMLs = new HashSet<URL>();
            try {
                jarFile = new JarFile(jarUrl.getFile());
            }
            catch (IOException e) {
                throw new PluginException("Could not open jar file " + jarUrl + ". ", e);
            }
            try {
                Manifest manifest = jarFile.getManifest();
                if (manifest == null) break block20;
                Attributes manifestAttributes = manifest.getMainAttributes();
                if (manifestAttributes.getValue("MaltParser-Plugin") == null || !manifestAttributes.getValue("MaltParser-Plugin").equals("true")) {
                    return false;
                }
                if (manifestAttributes.getValue("Class-Path") == null) break block20;
                String[] classPathItems = manifestAttributes.getValue("Class-Path").split(" ");
                for (int i = 0; i < classPathItems.length; ++i) {
                    URL u;
                    try {
                        u = new URL(jarUrl.getProtocol() + ":" + new File(jarFile.getName()).getParentFile().getPath() + "/" + classPathItems[i]);
                    }
                    catch (MalformedURLException e) {
                        throw new PluginException("The URL to the plugin jar-class-path '" + jarUrl.getProtocol() + ":" + new File(jarFile.getName()).getParentFile().getPath() + "/" + classPathItems[i] + "' is wrong. ", e);
                    }
                    URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
                    Class<URLClassLoader> sysclass = URLClassLoader.class;
                    Method method = sysclass.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(sysloader, u);
                }
            }
            catch (PatternSyntaxException e) {
                throw new PluginException("Could not split jar-class-path entries in the jar-file '" + jarFile.getName() + "'. ", e);
            }
            catch (IOException e) {
                throw new PluginException("Could not read the manifest file in the jar-file '" + jarFile.getName() + "'. ", e);
            }
            catch (NoSuchMethodException e) {
                throw new PluginException("", e);
            }
            catch (IllegalAccessException e) {
                throw new PluginException("", e);
            }
            catch (InvocationTargetException e) {
                throw new PluginException("", e);
            }
        }
        try {
            JarEntry je;
            JarInputStream jis = new JarInputStream(jarUrl.openConnection().getInputStream());
            while ((je = jis.getNextJarEntry()) != null) {
                String jarName = je.getName();
                if (jarName.endsWith(".class")) {
                    this.loadClassBytes(jis, jarName);
                    Class<?> clazz = this.findClass(jarName.substring(0, jarName.length() - 6));
                    this.classes.put(jarName.substring(0, jarName.length() - 6).replace('/', '.'), clazz);
                    this.loadClass(jarName.substring(0, jarName.length() - 6).replace('/', '.'));
                }
                if (jarName.endsWith("plugin.xml")) {
                    pluginXMLs.add(new URL("jar:" + jarUrl.getProtocol() + ":" + jarUrl.getPath() + "!/" + jarName));
                }
                jis.closeEntry();
            }
            for (URL url : pluginXMLs) {
                OptionManager.instance().loadOptionDescriptionFile(url);
            }
        }
        catch (MalformedURLException e) {
            throw new PluginException("The URL to the plugin.xml is wrong. ", e);
        }
        catch (IOException e) {
            throw new PluginException("cannot open jar file " + jarUrl + ". ", e);
        }
        catch (ClassNotFoundException e) {
            throw new PluginException("The class " + e.getMessage() + " can't be found. ", e);
        }
        return true;
    }

    public Class<?> getClass(String classname) {
        return this.classes.get(classname);
    }

    private void loadClassBytes(JarInputStream jis, String jarName) throws MaltChainedException {
        BufferedInputStream jarBuf = new BufferedInputStream(jis);
        ByteArrayOutputStream jarOut = new ByteArrayOutputStream();
        try {
            int b;
            while ((b = jarBuf.read()) != -1) {
                jarOut.write(b);
            }
            this.classByteArrays.put(jarName.substring(0, jarName.length() - 6), jarOut.toByteArray());
        }
        catch (IOException e) {
            throw new PluginException("Error reading entry " + jarName + ". ", e);
        }
    }

    protected void checkPackageAccess(String name) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPackageAccess(name);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("The MaltParser Plugin Loader (JarLoader)\n");
        sb.append("---------------------------------------------------------------------\n");
        for (String entry : new TreeSet<String>(this.classes.keySet())) {
            sb.append("   " + entry + "\n");
        }
        return sb.toString();
    }
}

