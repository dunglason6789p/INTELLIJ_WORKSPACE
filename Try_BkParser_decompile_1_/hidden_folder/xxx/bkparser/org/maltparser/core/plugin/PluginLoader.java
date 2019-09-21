package org.maltparser.core.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;

public class PluginLoader implements Iterable<Plugin> {
   private HashMap<String, Plugin> plugins = new HashMap();
   private TreeSet<String> pluginNames = new TreeSet();
   private File[] directories;
   private JarLoader jarLoader = null;
   private static PluginLoader uniqueInstance = new PluginLoader();

   private PluginLoader() {
   }

   public static PluginLoader instance() {
      return uniqueInstance;
   }

   public void loadPlugins(File pluginDirectory) throws MaltChainedException {
      this.loadPlugins(new File[]{pluginDirectory});
   }

   public void loadPlugins(File[] pluginDirectories) throws MaltChainedException {
      this.directories = new File[pluginDirectories.length];

      for(int i = 0; i < this.directories.length; ++i) {
         this.directories[i] = pluginDirectories[i];
      }

      try {
         Class<?> self = Class.forName("org.maltparser.core.plugin.PluginLoader");
         this.jarLoader = new JarLoader(self.getClassLoader());
      } catch (ClassNotFoundException var3) {
         throw new PluginException("The class 'org.maltparser.core.plugin.PluginLoader' not found. ", var3);
      }

      this.traverseDirectories();
   }

   private void traverseDirectories() throws MaltChainedException {
      for(int i = 0; i < this.directories.length; ++i) {
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

         for(int i = 0; i < children.length; ++i) {
            this.traverseDirectory(new File(directory, children[i]));
         }
      }

   }

   public Class<?> getClass(String classname) {
      return this.jarLoader != null ? this.jarLoader.getClass(classname) : null;
   }

   public Object newInstance(String classname, Class<?>[] argTypes, Object[] args) throws MaltChainedException {
      try {
         if (this.jarLoader == null) {
            return null;
         } else {
            Class<?> clazz = this.jarLoader.getClass(classname);
            Object o = null;
            if (clazz == null) {
               return null;
            } else {
               if (argTypes != null) {
                  Constructor<?> constructor = clazz.getConstructor(argTypes);
                  o = constructor.newInstance(args);
               } else {
                  o = clazz.newInstance();
               }

               return o;
            }
         }
      } catch (NoSuchMethodException var7) {
         throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", var7);
      } catch (InstantiationException var8) {
         throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", var8);
      } catch (IllegalAccessException var9) {
         throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", var9);
      } catch (InvocationTargetException var10) {
         throw new PluginException("The plugin loader was not able to create an instance of the class '" + classname + "'. ", var10);
      }
   }

   public Iterator<Plugin> iterator() {
      return this.plugins.values().iterator();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         Plugin plugin = (Plugin)i$.next();
         sb.append(plugin.toString() + "\n");
      }

      return sb.toString();
   }
}
