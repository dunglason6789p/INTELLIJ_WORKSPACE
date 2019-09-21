/*
 * Decompiled with CFR 0.146.
 */
package com.github.jcrfsuite.util;

import com.github.jcrfsuite.util.OSInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public class CrfSuiteLoader {
    public static final String CRFSUITE_SYSTEM_PROPERTIES_FILE = "org-chokkan-crfsuite.properties";
    public static final String KEY_CRFSUITE_LIB_PATH = "org.chokkan.crfsuite.lib.path";
    public static final String KEY_CRFSUITE_LIB_NAME = "org.chokkan.crfsuite.lib.name";
    public static final String KEY_CRFSUITE_TEMPDIR = "org.chokkan.crfsuite.tempdir";
    public static final String KEY_CRFSUITE_USE_SYSTEMLIB = "org.chokkan.crfsuite.use.systemlib";
    public static final String KEY_CRFSUITE_DISABLE_BUNDLED_LIBS = "org.chokkan.crfsuite.disable.bundled.libs";
    private static volatile boolean isLoaded = false;

    private static void loadCrfSuiteSystemProperties() {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CRFSUITE_SYSTEM_PROPERTIES_FILE);
            if (is == null) {
                return;
            }
            Properties props = new Properties();
            props.load(is);
            is.close();
            Enumeration<?> names = props.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String)names.nextElement();
                if (!name.startsWith("org.chokkan.crfsuite.") || System.getProperty(name) != null) continue;
                System.setProperty(name, props.getProperty(name));
            }
        }
        catch (Throwable ex) {
            System.err.println("Could not load 'org-chokkan-crfsuite.properties' from classpath: " + ex.toString());
        }
    }

    private static ClassLoader getRootClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl.getParent() != null) {
            cl = cl.getParent();
        }
        return cl;
    }

    private static byte[] getByteCode(String resourcePath) throws IOException {
        int readLength;
        InputStream in = CrfSuiteLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException(resourcePath + " is not found");
        }
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteCodeBuf = new ByteArrayOutputStream();
        while ((readLength = in.read(buf)) != -1) {
            byteCodeBuf.write(buf, 0, readLength);
        }
        in.close();
        return byteCodeBuf.toByteArray();
    }

    public static boolean isNativeLibraryLoaded() {
        return isLoaded;
    }

    private static boolean hasInjectedNativeLoader() {
        try {
            String nativeLoaderClassName = "native_loader.CrfSuiteNativeLoader";
            Class.forName("native_loader.CrfSuiteNativeLoader");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static synchronized void load() throws Exception {
        if (!isLoaded) {
            try {
                if (!CrfSuiteLoader.hasInjectedNativeLoader()) {
                    Class<?> nativeLoader = CrfSuiteLoader.injectCrfSuiteNativeLoader();
                    CrfSuiteLoader.loadNativeLibrary(nativeLoader);
                }
                Class.forName("com.github.jcrfsuite.util.CrfSuiteLoader");
                isLoaded = true;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Class<?> injectCrfSuiteNativeLoader() throws Exception {
        try {
            String nativeLoaderClassName = "native_loader.CrfSuiteNativeLoader";
            ClassLoader rootClassLoader = CrfSuiteLoader.getRootClassLoader();
            byte[] byteCode = CrfSuiteLoader.getByteCode("/crfsuite-0.12/native_loader/CrfSuiteNativeLoader.bytecode");
            String[] classesToPreload = new String[]{"third_party.org.chokkan.crfsuite.Attribute", "third_party.org.chokkan.crfsuite.crfsuite", "third_party.org.chokkan.crfsuite.crfsuiteJNI", "third_party.org.chokkan.crfsuite.Item", "third_party.org.chokkan.crfsuite.ItemSequence", "third_party.org.chokkan.crfsuite.StringList", "third_party.org.chokkan.crfsuite.Tagger", "third_party.org.chokkan.crfsuite.Trainer"};
            ArrayList<byte[]> preloadClassByteCode = new ArrayList<byte[]>(classesToPreload.length);
            for (String each : classesToPreload) {
                preloadClassByteCode.add(CrfSuiteLoader.getByteCode(String.format("/%s.class", each.replaceAll("\\.", "/"))));
            }
            Class<?> classLoader = Class.forName("java.lang.ClassLoader");
            Method defineClass = classLoader.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class);
            ProtectionDomain pd = System.class.getProtectionDomain();
            defineClass.setAccessible(true);
            try {
                defineClass.invoke(rootClassLoader, "native_loader.CrfSuiteNativeLoader", byteCode, 0, byteCode.length, pd);
                for (int i = 0; i < classesToPreload.length; ++i) {
                    byte[] b = (byte[])preloadClassByteCode.get(i);
                    defineClass.invoke(rootClassLoader, classesToPreload[i], b, 0, b.length, pd);
                }
            }
            finally {
                defineClass.setAccessible(false);
            }
            return rootClassLoader.loadClass("native_loader.CrfSuiteNativeLoader");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void loadNativeLibrary(Class<?> loaderClass) throws Exception {
        if (loaderClass == null) {
            throw new Exception("missing crfsuite native loader class");
        }
        File nativeLib = CrfSuiteLoader.findNativeLibrary();
        if (nativeLib != null) {
            Method loadMethod = loaderClass.getDeclaredMethod("loadLibByFile", String.class);
            loadMethod.invoke(null, nativeLib.getAbsolutePath());
        } else {
            Method loadMethod = loaderClass.getDeclaredMethod("loadLibrary", String.class);
            loadMethod.invoke(null, "crfsuite");
        }
    }

    static String md5sum(InputStream input) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(input);){
            MessageDigest digest = MessageDigest.getInstance("MD5");
            DigestInputStream digestInputStream = new DigestInputStream(in, digest);
            while (digestInputStream.read() >= 0) {
            }
            ByteArrayOutputStream md5out = new ByteArrayOutputStream();
            md5out.write(digest.digest());
            String string = md5out.toString();
            return string;
        }
    }

    private static File extractLibraryFile(String libFolderForCurrentOS, String libraryFileName, String targetFolder) throws Exception {
        String nativeLibraryFilePath = libFolderForCurrentOS + "/" + libraryFileName;
        String prefix = "crfsuite-" + CrfSuiteLoader.getVersion() + "-";
        String extractedLibFileName = prefix + libraryFileName;
        File extractedLibFile = new File(targetFolder, extractedLibFileName);
        try {
            if (extractedLibFile.exists()) {
                String md5sum2;
                String md5sum1 = CrfSuiteLoader.md5sum(CrfSuiteLoader.class.getResourceAsStream(nativeLibraryFilePath));
                if (md5sum1.equals(md5sum2 = CrfSuiteLoader.md5sum(new FileInputStream(extractedLibFile)))) {
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
            int bytesRead = 0;
            while ((bytesRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
            }
            writer.close();
            reader.close();
            if (!System.getProperty("os.name").contains("Windows")) {
                try {
                    Runtime.getRuntime().exec(new String[]{"chmod", "755", extractedLibFile.getAbsolutePath()}).waitFor();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return new File(targetFolder, extractedLibFileName);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static File findNativeLibrary() throws Exception {
        File nativeLib;
        boolean useSystemLib = Boolean.parseBoolean(System.getProperty(KEY_CRFSUITE_USE_SYSTEMLIB, "false"));
        if (useSystemLib) {
            return null;
        }
        boolean disabledBundledLibs = Boolean.parseBoolean(System.getProperty(KEY_CRFSUITE_DISABLE_BUNDLED_LIBS, "false"));
        if (disabledBundledLibs) {
            return null;
        }
        String crfsuiteNativeLibraryPath = System.getProperty(KEY_CRFSUITE_LIB_PATH);
        String crfsuiteNativeLibraryName = System.getProperty(KEY_CRFSUITE_LIB_NAME);
        if (crfsuiteNativeLibraryName == null) {
            crfsuiteNativeLibraryName = System.mapLibraryName("crfsuite");
        }
        if (crfsuiteNativeLibraryPath != null && (nativeLib = new File(crfsuiteNativeLibraryPath, crfsuiteNativeLibraryName)).exists()) {
            return nativeLib;
        }
        crfsuiteNativeLibraryPath = "/crfsuite-0.12/" + OSInfo.getNativeLibFolderPathForCurrentOS();
        if (CrfSuiteLoader.class.getResource(crfsuiteNativeLibraryPath + "/" + crfsuiteNativeLibraryName) != null) {
            String tempFolder = new File(System.getProperty(KEY_CRFSUITE_TEMPDIR, System.getProperty("java.io.tmpdir"))).getAbsolutePath();
            return CrfSuiteLoader.extractLibraryFile(crfsuiteNativeLibraryPath, crfsuiteNativeLibraryName, tempFolder);
        }
        return null;
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    static {
        CrfSuiteLoader.loadCrfSuiteSystemProperties();
    }
}

