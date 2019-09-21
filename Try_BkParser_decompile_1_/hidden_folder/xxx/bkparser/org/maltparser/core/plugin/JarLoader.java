package org.maltparser.core.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Iterator;
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

public class JarLoader extends SecureClassLoader {
   private HashMap<String, byte[]> classByteArrays = new HashMap();
   private HashMap<String, Class<?>> classes = new HashMap();

   public JarLoader(ClassLoader parent) {
      super(parent);
   }

   protected Class<?> findClass(String name) {
      String urlName = name.replace('.', '/');
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
         int i = name.lastIndexOf(46);
         if (i >= 0) {
            sm.checkPackageDefinition(name.substring(0, i));
         }
      }

      byte[] buf = (byte[])((byte[])this.classByteArrays.get(urlName));
      return buf != null ? this.defineClass((String)null, buf, 0, buf.length) : null;
   }

   public boolean readJarFile(URL jarUrl) throws MaltChainedException {
      HashSet pluginXMLs = new HashSet();

      JarFile jarFile;
      try {
         jarFile = new JarFile(jarUrl.getFile());
      } catch (IOException var15) {
         throw new PluginException("Could not open jar file " + jarUrl + ". ", var15);
      }

      try {
         Manifest manifest = jarFile.getManifest();
         if (manifest != null) {
            Attributes manifestAttributes = manifest.getMainAttributes();
            if (manifestAttributes.getValue("MaltParser-Plugin") == null || !manifestAttributes.getValue("MaltParser-Plugin").equals("true")) {
               return false;
            }

            if (manifestAttributes.getValue("Class-Path") != null) {
               String[] classPathItems = manifestAttributes.getValue("Class-Path").split(" ");

               for(int i = 0; i < classPathItems.length; ++i) {
                  URL u;
                  try {
                     u = new URL(jarUrl.getProtocol() + ":" + (new File(jarFile.getName())).getParentFile().getPath() + "/" + classPathItems[i]);
                  } catch (MalformedURLException var14) {
                     throw new PluginException("The URL to the plugin jar-class-path '" + jarUrl.getProtocol() + ":" + (new File(jarFile.getName())).getParentFile().getPath() + "/" + classPathItems[i] + "' is wrong. ", var14);
                  }

                  URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
                  Class<?> sysclass = URLClassLoader.class;
                  Method method = sysclass.getDeclaredMethod("addURL", URL.class);
                  method.setAccessible(true);
                  method.invoke(sysloader, u);
               }
            }
         }
      } catch (PatternSyntaxException var19) {
         throw new PluginException("Could not split jar-class-path entries in the jar-file '" + jarFile.getName() + "'. ", var19);
      } catch (IOException var20) {
         throw new PluginException("Could not read the manifest file in the jar-file '" + jarFile.getName() + "'. ", var20);
      } catch (NoSuchMethodException var21) {
         throw new PluginException("", var21);
      } catch (IllegalAccessException var22) {
         throw new PluginException("", var22);
      } catch (InvocationTargetException var23) {
         throw new PluginException("", var23);
      }

      try {
         JarEntry je;
         for(JarInputStream jis = new JarInputStream(jarUrl.openConnection().getInputStream()); (je = jis.getNextJarEntry()) != null; jis.closeEntry()) {
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
         }

         Iterator i$ = pluginXMLs.iterator();

         while(i$.hasNext()) {
            URL url = (URL)i$.next();
            OptionManager.instance().loadOptionDescriptionFile(url);
         }

         return true;
      } catch (MalformedURLException var16) {
         throw new PluginException("The URL to the plugin.xml is wrong. ", var16);
      } catch (IOException var17) {
         throw new PluginException("cannot open jar file " + jarUrl + ". ", var17);
      } catch (ClassNotFoundException var18) {
         throw new PluginException("The class " + var18.getMessage() + " can't be found. ", var18);
      }
   }

   public Class<?> getClass(String classname) {
      return (Class)this.classes.get(classname);
   }

   private void loadClassBytes(JarInputStream jis, String jarName) throws MaltChainedException {
      BufferedInputStream jarBuf = new BufferedInputStream(jis);
      ByteArrayOutputStream jarOut = new ByteArrayOutputStream();

      try {
         int b;
         while((b = jarBuf.read()) != -1) {
            jarOut.write(b);
         }

         this.classByteArrays.put(jarName.substring(0, jarName.length() - 6), jarOut.toByteArray());
      } catch (IOException var7) {
         throw new PluginException("Error reading entry " + jarName + ". ", var7);
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
      Iterator i$ = (new TreeSet(this.classes.keySet())).iterator();

      while(i$.hasNext()) {
         String entry = (String)i$.next();
         sb.append("   " + entry + "\n");
      }

      return sb.toString();
   }
}
