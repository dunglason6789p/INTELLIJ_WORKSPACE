package com.github.jcrfsuite.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class CrfSuiteLoader {
   public static final String CRFSUITE_SYSTEM_PROPERTIES_FILE = "org-chokkan-crfsuite.properties";
   public static final String KEY_CRFSUITE_LIB_PATH = "org.chokkan.crfsuite.lib.path";
   public static final String KEY_CRFSUITE_LIB_NAME = "org.chokkan.crfsuite.lib.name";
   public static final String KEY_CRFSUITE_TEMPDIR = "org.chokkan.crfsuite.tempdir";
   public static final String KEY_CRFSUITE_USE_SYSTEMLIB = "org.chokkan.crfsuite.use.systemlib";
   public static final String KEY_CRFSUITE_DISABLE_BUNDLED_LIBS = "org.chokkan.crfsuite.disable.bundled.libs";
   private static volatile boolean isLoaded = false;

   public CrfSuiteLoader() {
   }

   private static void loadCrfSuiteSystemProperties() {
      try {
         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org-chokkan-crfsuite.properties");
         if (is == null) {
            return;
         }

         Properties props = new Properties();
         props.load(is);
         is.close();
         Enumeration names = props.propertyNames();

         while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            if (name.startsWith("org.chokkan.crfsuite.") && System.getProperty(name) == null) {
               System.setProperty(name, props.getProperty(name));
            }
         }
      } catch (Throwable var4) {
         System.err.println("Could not load 'org-chokkan-crfsuite.properties' from classpath: " + var4.toString());
      }

   }

   private static ClassLoader getRootClassLoader() {
      ClassLoader cl;
      for(cl = Thread.currentThread().getContextClassLoader(); cl.getParent() != null; cl = cl.getParent()) {
      }

      return cl;
   }

   private static byte[] getByteCode(String resourcePath) throws IOException {
      InputStream in = CrfSuiteLoader.class.getResourceAsStream(resourcePath);
      if (in == null) {
         throw new IOException(resourcePath + " is not found");
      } else {
         byte[] buf = new byte[1024];
         ByteArrayOutputStream byteCodeBuf = new ByteArrayOutputStream();

         int readLength;
         while((readLength = in.read(buf)) != -1) {
            byteCodeBuf.write(buf, 0, readLength);
         }

         in.close();
         return byteCodeBuf.toByteArray();
      }
   }

   public static boolean isNativeLibraryLoaded() {
      return isLoaded;
   }

   private static boolean hasInjectedNativeLoader() {
      try {
         String nativeLoaderClassName = "native_loader.CrfSuiteNativeLoader";
         Class.forName("native_loader.CrfSuiteNativeLoader");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   public static synchronized void load() throws Exception {
      if (!isLoaded) {
         try {
            if (!hasInjectedNativeLoader()) {
               Class<?> nativeLoader = injectCrfSuiteNativeLoader();
               loadNativeLibrary(nativeLoader);
            }

            Class.forName("com.github.jcrfsuite.util.CrfSuiteLoader");
            isLoaded = true;
         } catch (Exception var1) {
            var1.printStackTrace();
            throw var1;
         }
      }

   }

   private static Class<?> injectCrfSuiteNativeLoader() throws Exception {
      try {
         String nativeLoaderClassName = "native_loader.CrfSuiteNativeLoader";
         ClassLoader rootClassLoader = getRootClassLoader();
         byte[] byteCode = getByteCode("/crfsuite-0.12/native_loader/CrfSuiteNativeLoader.bytecode");
         String[] classesToPreload = new String[]{"third_party.org.chokkan.crfsuite.Attribute", "third_party.org.chokkan.crfsuite.crfsuite", "third_party.org.chokkan.crfsuite.crfsuiteJNI", "third_party.org.chokkan.crfsuite.Item", "third_party.org.chokkan.crfsuite.ItemSequence", "third_party.org.chokkan.crfsuite.StringList", "third_party.org.chokkan.crfsuite.Tagger", "third_party.org.chokkan.crfsuite.Trainer"};
         List<byte[]> preloadClassByteCode = new ArrayList(classesToPreload.length);
         String[] var5 = classesToPreload;
         int var6 = classesToPreload.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String each = var5[var7];
            preloadClassByteCode.add(getByteCode(String.format("/%s.class", each.replaceAll("\\.", "/"))));
         }

         Class<?> classLoader = Class.forName("java.lang.ClassLoader");
         Method defineClass = classLoader.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class);
         ProtectionDomain pd = System.class.getProtectionDomain();
         defineClass.setAccessible(true);

         try {
            defineClass.invoke(rootClassLoader, "native_loader.CrfSuiteNativeLoader", byteCode, 0, byteCode.length, pd);

            for(int i = 0; i < classesToPreload.length; ++i) {
               byte[] b = (byte[])preloadClassByteCode.get(i);
               defineClass.invoke(rootClassLoader, classesToPreload[i], b, 0, b.length, pd);
            }
         } finally {
            defineClass.setAccessible(false);
         }

         return rootClassLoader.loadClass("native_loader.CrfSuiteNativeLoader");
      } catch (Exception var14) {
         var14.printStackTrace();
         throw var14;
      }
   }

   private static void loadNativeLibrary(Class<?> loaderClass) throws Exception {
      if (loaderClass == null) {
         throw new Exception("missing crfsuite native loader class");
      } else {
         File nativeLib = findNativeLibrary();
         Method loadMethod;
         if (nativeLib != null) {
            loadMethod = loaderClass.getDeclaredMethod("loadLibByFile", String.class);
            loadMethod.invoke((Object)null, nativeLib.getAbsolutePath());
         } else {
            loadMethod = loaderClass.getDeclaredMethod("loadLibrary", String.class);
            loadMethod.invoke((Object)null, "crfsuite");
         }

      }
   }

   static String md5sum(InputStream input) throws IOException {
      BufferedInputStream in = new BufferedInputStream(input);

      try {
         MessageDigest digest = MessageDigest.getInstance("MD5");
         DigestInputStream digestInputStream = new DigestInputStream(in, digest);

         while(digestInputStream.read() >= 0) {
         }

         ByteArrayOutputStream md5out = new ByteArrayOutputStream();
         md5out.write(digest.digest());
         String var5 = md5out.toString();
         return var5;
      } catch (NoSuchAlgorithmException var9) {
         throw new IllegalStateException("MD5 algorithm is not available: " + var9);
      } finally {
         in.close();
      }
   }

   private static File extractLibraryFile(String libFolderForCurrentOS, String libraryFileName, String targetFolder) throws Exception {
      String nativeLibraryFilePath = libFolderForCurrentOS + "/" + libraryFileName;
      String prefix = "crfsuite-" + getVersion() + "-";
      String extractedLibFileName = prefix + libraryFileName;
      File extractedLibFile = new File(targetFolder, extractedLibFileName);

      try {
         if (extractedLibFile.exists()) {
            String md5sum1 = md5sum(CrfSuiteLoader.class.getResourceAsStream(nativeLibraryFilePath));
            String md5sum2 = md5sum(new FileInputStream(extractedLibFile));
            if (md5sum1.equals(md5sum2)) {
               return new File(targetFolder, extractedLibFileName);
            }

            boolean deletionSucceeded = extractedLibFile.delete();
            if (!deletionSucceeded) {
               throw new IOException("failed to remove existing native library file: " + extractedLibFile.getAbsolutePath());
            }
         }

         InputStream reader = CrfSuiteLoader.class.getResourceAsStream(nativeLibraryFilePath);
         FileOutputStream writer = new FileOutputStream(extractedLibFile);
         byte[] buffer = new byte[8192];
         boolean var10 = false;

         int bytesRead;
         while((bytesRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, bytesRead);
         }

         writer.close();
         reader.close();
         if (!System.getProperty("os.name").contains("Windows")) {
            try {
               Runtime.getRuntime().exec(new String[]{"chmod", "755", extractedLibFile.getAbsolutePath()}).waitFor();
            } catch (Throwable var12) {
            }
         }

         return new File(targetFolder, extractedLibFileName);
      } catch (IOException var13) {
         var13.printStackTrace();
         return null;
      }
   }

   static File findNativeLibrary() throws Exception {
      boolean useSystemLib = Boolean.parseBoolean(System.getProperty("org.chokkan.crfsuite.use.systemlib", "false"));
      if (useSystemLib) {
         return null;
      } else {
         boolean disabledBundledLibs = Boolean.parseBoolean(System.getProperty("org.chokkan.crfsuite.disable.bundled.libs", "false"));
         if (disabledBundledLibs) {
            return null;
         } else {
            String crfsuiteNativeLibraryPath = System.getProperty("org.chokkan.crfsuite.lib.path");
            String crfsuiteNativeLibraryName = System.getProperty("org.chokkan.crfsuite.lib.name");
            if (crfsuiteNativeLibraryName == null) {
               crfsuiteNativeLibraryName = System.mapLibraryName("crfsuite");
            }

            if (crfsuiteNativeLibraryPath != null) {
               File nativeLib = new File(crfsuiteNativeLibraryPath, crfsuiteNativeLibraryName);
               if (nativeLib.exists()) {
                  return nativeLib;
               }
            }

            crfsuiteNativeLibraryPath = "/crfsuite-0.12/" + OSInfo.getNativeLibFolderPathForCurrentOS();
            if (CrfSuiteLoader.class.getResource(crfsuiteNativeLibraryPath + "/" + crfsuiteNativeLibraryName) != null) {
               String tempFolder = (new File(System.getProperty("org.chokkan.crfsuite.tempdir", System.getProperty("java.io.tmpdir")))).getAbsolutePath();
               return extractLibraryFile(crfsuiteNativeLibraryPath, crfsuiteNativeLibraryName, tempFolder);
            } else {
               return null;
            }
         }
      }
   }

   public static String getVersion() {
      URL versionFile = CrfSuiteLoader.class.getResource("/META-INF/maven/org.chokkan.crfsuite/pom.properties");
      if (versionFile == null) {
         versionFile = CrfSuiteLoader.class.getResource("/third_party/org/chokkan/crfsuite/VERSION");
      }

      String version = "unknown";

      try {
         if (versionFile != null) {
            Properties versionData = new Properties();
            versionData.load(versionFile.openStream());
            version = versionData.getProperty("version", version);
            if (version.equals("unknown")) {
               version = versionData.getProperty("VERSION", version);
            }

            version = version.trim().replaceAll("[^0-9M\\.]", "");
         }
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return version;
   }

   static {
      loadCrfSuiteSystemProperties();
   }
}
