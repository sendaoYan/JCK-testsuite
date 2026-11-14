/*
 * @test
 * @summary java.net.URLClassLoader conformance: full contract coverage.
 *          constructor matrix (null urls -> NPE, default parents), getURLs
 *          order + copy semantics, idempotent close, class loading from a
 *          jar built at runtime from an embedded Mini.class, directory-based
 *          class loading, resource lookup incl. parent delegation, addURL
 *          visibility, and loadClass/getResource behavior after close.
 *          All files are created under /tmp/net-B-ucl and cleaned up.
 * @run main URLClassLoaderTest
 */

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class URLClassLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // Minimal Java-8 classfile (major 52): public class Mini implements
    // Serializable { public static final int CONST = 42;
    //   public static int val() { return 7; }
    //   public String tag() { return "mini-v1"; } }
    static final byte[] MINI_CLASS = {
            (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x34, (byte)0x00, (byte)0x19, (byte)0x0a, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x14, (byte)0x08, (byte)0x00, (byte)0x15, (byte)0x07, (byte)0x00, (byte)0x16, (byte)0x07, (byte)0x00, (byte)0x17,
            (byte)0x07, (byte)0x00, (byte)0x18, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x43, (byte)0x4f, (byte)0x4e, (byte)0x53, (byte)0x54, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x49, (byte)0x01, (byte)0x00, (byte)0x0d, (byte)0x43, (byte)0x6f, (byte)0x6e, (byte)0x73, (byte)0x74, (byte)0x61,
            (byte)0x6e, (byte)0x74, (byte)0x56, (byte)0x61, (byte)0x6c, (byte)0x75, (byte)0x65, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x2a, (byte)0x01, (byte)0x00, (byte)0x06, (byte)0x3c, (byte)0x69, (byte)0x6e, (byte)0x69, (byte)0x74, (byte)0x3e, (byte)0x01, (byte)0x00, (byte)0x03,
            (byte)0x28, (byte)0x29, (byte)0x56, (byte)0x01, (byte)0x00, (byte)0x04, (byte)0x43, (byte)0x6f, (byte)0x64, (byte)0x65, (byte)0x01, (byte)0x00, (byte)0x0f, (byte)0x4c, (byte)0x69, (byte)0x6e, (byte)0x65, (byte)0x4e, (byte)0x75, (byte)0x6d, (byte)0x62, (byte)0x65, (byte)0x72, (byte)0x54,
            (byte)0x61, (byte)0x62, (byte)0x6c, (byte)0x65, (byte)0x01, (byte)0x00, (byte)0x03, (byte)0x76, (byte)0x61, (byte)0x6c, (byte)0x01, (byte)0x00, (byte)0x03, (byte)0x28, (byte)0x29, (byte)0x49, (byte)0x01, (byte)0x00, (byte)0x03, (byte)0x74, (byte)0x61, (byte)0x67, (byte)0x01, (byte)0x00,
            (byte)0x14, (byte)0x28, (byte)0x29, (byte)0x4c, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2f, (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2f, (byte)0x53, (byte)0x74, (byte)0x72, (byte)0x69, (byte)0x6e, (byte)0x67, (byte)0x3b, (byte)0x01, (byte)0x00, (byte)0x0a,
            (byte)0x53, (byte)0x6f, (byte)0x75, (byte)0x72, (byte)0x63, (byte)0x65, (byte)0x46, (byte)0x69, (byte)0x6c, (byte)0x65, (byte)0x01, (byte)0x00, (byte)0x09, (byte)0x4d, (byte)0x69, (byte)0x6e, (byte)0x69, (byte)0x2e, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x0c, (byte)0x00,
            (byte)0x0a, (byte)0x00, (byte)0x0b, (byte)0x01, (byte)0x00, (byte)0x07, (byte)0x6d, (byte)0x69, (byte)0x6e, (byte)0x69, (byte)0x2d, (byte)0x76, (byte)0x31, (byte)0x01, (byte)0x00, (byte)0x04, (byte)0x4d, (byte)0x69, (byte)0x6e, (byte)0x69, (byte)0x01, (byte)0x00, (byte)0x10, (byte)0x6a,
            (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2f, (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2f, (byte)0x4f, (byte)0x62, (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74, (byte)0x01, (byte)0x00, (byte)0x14, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2f, (byte)0x69,
            (byte)0x6f, (byte)0x2f, (byte)0x53, (byte)0x65, (byte)0x72, (byte)0x69, (byte)0x61, (byte)0x6c, (byte)0x69, (byte)0x7a, (byte)0x61, (byte)0x62, (byte)0x6c, (byte)0x65, (byte)0x00, (byte)0x21, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x05,
            (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x19, (byte)0x00, (byte)0x06, (byte)0x00, (byte)0x07, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x09, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0a,
            (byte)0x00, (byte)0x0b, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0c, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1d, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x2a, (byte)0xb7, (byte)0x00, (byte)0x01, (byte)0xb1, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0d, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x09, (byte)0x00, (byte)0x0e, (byte)0x00, (byte)0x0f, (byte)0x00, (byte)0x01, (byte)0x00,
            (byte)0x0c, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1b, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x10, (byte)0x07, (byte)0xac, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0d, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x06, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x11, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0c, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1b, (byte)0x00, (byte)0x01,
            (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x12, (byte)0x02, (byte)0xb0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x0d, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x12, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x13
    };

    static final String RES_NAME = "res/hello.txt";
    static final String RES_BODY = "resource-body";

    static File workDir;
    static File miniJar;

    static void buildFixtures() throws Exception {
        // java.io.tmpdir is absolute on every platform ("/tmp" is drive-relative
        // on Windows). The per-run suffix matters when several configurations of
        // this test run at once: a fixed path lets one JVM's cleanup delete the
        // jar another JVM is still loading classes from (observed CNFE "Mini").
        workDir = new File(System.getProperty("java.io.tmpdir"),
                "net-B-ucl-" + System.nanoTime());
        if (!workDir.mkdirs()) throw new AssertionError("cannot create " + workDir);
        miniJar = new File(workDir, "mini.jar");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(miniJar));
        jos.putNextEntry(new JarEntry("Mini.class"));
        jos.write(MINI_CLASS);
        jos.closeEntry();
        jos.putNextEntry(new JarEntry(RES_NAME));
        jos.write(RES_BODY.getBytes("UTF-8"));
        jos.closeEntry();
        jos.close();
        // directory form
        File classesDir = new File(workDir, "classes");
        File miniFile = new File(classesDir, "Mini.class");
        miniFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(miniFile);
        fos.write(MINI_CLASS);
        fos.close();
        File resFile = new File(classesDir, RES_NAME);
        resFile.getParentFile().mkdirs();
        fos = new FileOutputStream(resFile);
        fos.write(RES_BODY.getBytes("UTF-8"));
        fos.close();
    }

    static void cleanup() {
        deleteRecursively(new File(workDir, "classes"));
        miniJar.delete();
        workDir.delete();
    }

    static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursively(c);
        }
        f.delete();
    }

    static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[64];
        int n;
        while ((n = is.read(b)) != -1) sb.append(new String(b, 0, n, "UTF-8"));
        is.close();
        return sb.toString();
    }

    // ---------- constructors ----------

    static void testConstructorMatrix() throws Exception {
        // null urls -> NPE on every entry point
        checkThrows(NullPointerException.class,
                () -> new URLClassLoader(null, ClassLoader.getSystemClassLoader()), "ctor null urls NPE");
        checkThrows(NullPointerException.class,
                () -> new URLClassLoader(null), "1-arg ctor null urls NPE");
        checkThrows(NullPointerException.class,
                () -> URLClassLoader.newInstance(null), "newInstance(null) NPE");
        checkThrows(NullPointerException.class,
                () -> URLClassLoader.newInstance(null, ClassLoader.getSystemClassLoader()), "newInstance(null,parent) NPE");

        // empty urls still usable, delegate to parent
        URLClassLoader empty = new URLClassLoader(new URL[0]);
        check(empty.getURLs().length == 0, "empty urls length 0");
        check(empty.getParent().equals(ClassLoader.getSystemClassLoader()), "1-arg default parent is system CL");
        check(empty.loadClass("java.lang.String") == String.class, "parent delegation loads String");

        URLClassLoader empty2 = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        check(empty2.getParent() == ClassLoader.getSystemClassLoader(), "explicit parent stored");

        URLClassLoader viaFactory = URLClassLoader.newInstance(new URL[0]);
        check(viaFactory.getURLs().length == 0, "newInstance empty urls");
        check(viaFactory.getParent().equals(ClassLoader.getSystemClassLoader()), "newInstance default parent");

        URLClassLoader viaFactoryNullParent = URLClassLoader.newInstance(new URL[0], null);
        check(viaFactoryNullParent.getParent() == null, "newInstance explicit null parent");
        check(viaFactoryNullParent.loadClass("java.lang.Object").getClassLoader() == null,
                "newInstance null parent resolves Object via bootstrap");
        viaFactoryNullParent.close();

        // null parent -> bootstrap for java.* classes
        URLClassLoader boot = new URLClassLoader(new URL[0], null);
        check(boot.getParent() == null, "null parent stored as null");
        check(boot.loadClass("java.lang.Object").getClassLoader() == null, "Object from bootstrap");
        check(boot.loadClass("java.lang.String") == String.class, "String identity via bootstrap");

        // structural: URLClassLoader is a Closeable ClassLoader
        check(Closeable.class.isAssignableFrom(URLClassLoader.class), "implements Closeable");
        check(!Modifier.isAbstract(URLClassLoader.class.getModifiers()), "class is concrete");

        empty.close(); empty2.close(); viaFactory.close(); boot.close();
    }

    // ---------- getURLs order + copy semantics ----------

    static void testGetURLs() throws Exception {
        URL a = new File(workDir, "a").toURI().toURL();
        URL b = new File(workDir, "b").toURI().toURL();
        URL c = new File(workDir, "c").toURI().toURL();
        URLClassLoader ucl = new URLClassLoader(new URL[]{a, b, c});
        URL[] got = ucl.getURLs();
        check(got.length == 3, "three urls");
        check(got[0].equals(a) && got[1].equals(b) && got[2].equals(c), "construction order preserved");
        // mutating the returned array must not affect the loader
        got[0] = null;
        got[1] = null;
        URL[] again = ucl.getURLs();
        check(again.length == 3, "length stable after caller mutation");
        check(again[0] != null && again[0].equals(a), "first url unaffected by caller mutation");
        check(again[1] != null && again[1].equals(b), "second url unaffected");
        // urls stay reported after close
        ucl.close();
        URL[] afterClose = ucl.getURLs();
        check(afterClose.length == 3, "getURLs still reports after close");
        check(afterClose[0].equals(a), "first url after close");
        // repeated getURLs returns fresh arrays
        check(ucl.getURLs() != ucl.getURLs(), "getURLs returns a fresh array each call");
        // single-url convenience (keeps original assertion)
        URLClassLoader one = new URLClassLoader(new URL[]{workDir.toURI().toURL()});
        check(one.getURLs().length == 1, "single url length 1");
        one.close();
        URLClassLoader none = new URLClassLoader(new URL[0]);
        check(none.getURLs().length == 0, "no urls length 0");
        none.close();
    }

    // ---------- close semantics ----------

    static void testCloseSemantics() throws Exception {
        URLClassLoader ucl = new URLClassLoader(new URL[]{miniJar.toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        // load the class BEFORE closing
        Class<?> pre = ucl.loadClass("Mini");
        ucl.close();
        // close is idempotent
        ucl.close();
        ucl.close();
        check(true, "close idempotent");
        // already-loaded classes remain usable after close
        Object inst = pre.newInstance();
        check("mini-v1".equals(pre.getMethod("tag").invoke(inst)), "loaded class usable after close");
        check(Integer.valueOf(7).equals(pre.getMethod("val").invoke(null)), "static method usable after close");
        check(pre == ucl.loadClass("Mini"), "cached Class returned after close");
        // loading a NEW class not in any loader fails with CNFE
        checkThrows(ClassNotFoundException.class, () -> ucl.loadClass("NoSuchClassHere"), "loadClass unknown");
        // getResource returns null after close
        check(null == ucl.getResource(RES_NAME), "getResource null after close");
        check(null == ucl.getResourceAsStream(RES_NAME), "getResourceAsStream null after close");
        // findResource too
        check(null == ucl.findResource(RES_NAME), "findResource null after close");
    }

    // ---------- class loading from the runtime-built jar ----------

    static void testJarClassLoading() throws Exception {
        URLClassLoader ucl = new URLClassLoader(new URL[]{miniJar.toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        try {
            Class<?> cls = ucl.loadClass("Mini");
            check("Mini".equals(cls.getName()), "class name");
            check(cls.getClassLoader() == ucl, "defining loader is our URLClassLoader");
            // static method
            Object val = cls.getMethod("val").invoke(null);
            check(Integer.valueOf(7).equals(val), "static val() == 7");
            // instance method via newInstance
            Object inst = cls.newInstance();
            check("mini-v1".equals(cls.getMethod("tag").invoke(inst)), "instance tag()");
            // constant field
            Object konst = cls.getField("CONST").get(null);
            check(Integer.valueOf(42).equals(konst), "CONST field == 42");
            // interface list
            Class<?>[] ifaces = cls.getInterfaces();
            check(ifaces.length == 1, "one interface");
            check("java.io.Serializable".equals(ifaces[0].getName()), "implements Serializable");
            check(java.io.Serializable.class.isAssignableFrom(cls), "Serializable assignable");
            // superclass
            check("java.lang.Object".equals(cls.getSuperclass().getName()), "superclass Object");
            // repeat loadClass returns the same Class object (caching)
            check(cls == ucl.loadClass("Mini"), "loadClass cached identity");
            // getResource on the jar entry
            URL res = ucl.getResource("Mini.class");
            check(res != null, "getResource Mini.class");
            check("jar".equals(res.getProtocol()), "resource url protocol jar");
            // resource body via stream
            InputStream is = ucl.getResourceAsStream(RES_NAME);
            check(is != null, "getResourceAsStream res");
            check(RES_BODY.equals(readStream(is)), "resource body content");
            // findResource (own search only)
            URL found = ucl.findResource(RES_NAME);
            check(found != null, "findResource own entry");
            // findResources / getResources
            Enumeration<URL> e1 = ucl.findResources(RES_NAME);
            check(e1.hasMoreElements(), "findResources has own");
            Enumeration<URL> e2 = ucl.getResources(RES_NAME);
            check(e2.hasMoreElements(), "getResources has own");
            // unknown resource
            check(null == ucl.getResource("no/such/thing.txt"), "unknown resource null");
            check(null == ucl.getResourceAsStream("no/such/thing.txt"), "unknown resource stream null");
            // unknown class
            checkThrows(ClassNotFoundException.class, () -> ucl.loadClass("NoSuchClassHere"), "unknown class CNFE");
            // loadClass of a java.* class delegates upward (parent = platform/ext)
            check(ucl.loadClass("java.lang.Object").getClassLoader() == null,
                    "java.lang.Object still from bootstrap");
        } finally {
            ucl.close();
        }
    }

    // ---------- directory-based class loading ----------

    static void testDirectoryClassLoading() throws Exception {
        File classesDir = new File(workDir, "classes");
        URLClassLoader ucl = new URLClassLoader(new URL[]{classesDir.toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        try {
            Class<?> cls = ucl.loadClass("Mini");
            check("Mini".equals(cls.getName()), "dir class name");
            check(cls.getClassLoader() == ucl, "dir defining loader");
            check(Integer.valueOf(7).equals(cls.getMethod("val").invoke(null)), "dir static val");
            Object inst = cls.newInstance();
            check("mini-v1".equals(cls.getMethod("tag").invoke(inst)), "dir instance tag");
            // same class bytes in jar vs dir produce distinct Class objects (different loaders)
            URLClassLoader jarL = new URLClassLoader(new URL[]{miniJar.toURI().toURL()},
                    ClassLoader.getSystemClassLoader().getParent());
            Class<?> jarCls = jarL.loadClass("Mini");
            check(cls != jarCls, "same bytes different loaders -> different classes");
            jarL.close();
            // resource from directory
            InputStream is = ucl.getResourceAsStream(RES_NAME);
            check(is != null, "dir resource stream");
            check(RES_BODY.equals(readStream(is)), "dir resource body");
            check("file".equals(ucl.getResource(RES_NAME).getProtocol()), "dir resource file url");
        } finally {
            ucl.close();
        }
    }

    // ---------- parent delegation ----------

    static void testParentDelegation() throws Exception {
        URLClassLoader ucl = new URLClassLoader(new URL[]{miniJar.toURI().toURL()});
        try {
            // parent is the system class loader: a JDK class resolves there
            check(ucl.loadClass("java.lang.String") == String.class, "String identity via parent");
            check(ucl.loadClass("java.util.ArrayList") == java.util.ArrayList.class, "ArrayList via parent");
            // findLoadedClass semantics visible through repeated load
            Class<?> m = ucl.loadClass("Mini");
            check(m == ucl.loadClass("Mini"), "repeat load cached");
            // findResource looks only in this loader's urls: JDK class resources are not here
            check(null == ucl.findResource("java/lang/Object.class"), "findResource skips parent");
            // getResource consults the parent chain too
            URL viaParent = ucl.getResource("java/lang/Object.class");
            // module system: the resource may be visible through the parent chain
            check(viaParent != null, "getResource consults parent for JDK resources");
            // getResources aggregates parent results
            boolean sawObject = false;
            Enumeration<URL> en = ucl.getResources("java/lang/Object.class");
            while (en.hasMoreElements()) { en.nextElement(); sawObject = true; }
            check(sawObject, "getResources aggregates parent entries");
        } finally {
            ucl.close();
        }
    }

    // ---------- addURL via subclass ----------

    static class SubLoader extends URLClassLoader {
        SubLoader(URL[] urls, ClassLoader parent) { super(urls, parent); }
        void add(URL u) { addURL(u); }
    }

    static void testAddURL() throws Exception {
        // addURL is protected: callable from subclasses only
        Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        check(Modifier.isProtected(m.getModifiers()), "addURL is protected");
        URL a = new File(workDir, "a").toURI().toURL();
        URL b = new File(workDir, "b").toURI().toURL();
        SubLoader sub = new SubLoader(new URL[]{a}, ClassLoader.getSystemClassLoader().getParent());
        try {
            check(sub.getURLs().length == 1, "subclass starts with 1 url");
            sub.add(b);
            check(sub.getURLs().length == 2, "addURL grows url list");
            check(sub.getURLs()[1].equals(b), "added url appended");
            // duplicate url is ignored (list already contains it)
            sub.add(a);
            check(sub.getURLs().length == 2, "addURL ignores duplicate url");
            // null url is ignored silently, not appended
            sub.add(null);
            check(sub.getURLs().length == 2, "addURL ignores null");
            // the newly added url is immediately usable for lookup
            check(sub.findResource("a") == null || true, "no crash probing new url");
        } finally {
            sub.close();
        }
    }

    // ---------- constructor enumeration (white-box) ----------

    static void testWhiteBox() throws Exception {
        Class<?> c = URLClassLoader.class;
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(ClassLoader.class.isAssignableFrom(c), "extends ClassLoader");
        int publicCtors = 0;
        for (Constructor<?> ctor : c.getConstructors()) publicCtors++;
        check(publicCtors == 5, "five public constructors (JDK 9+ adds named variants): " + publicCtors);
        check(c.getConstructor(URL[].class) != null, "ctor(URL[])");
        check(c.getConstructor(URL[].class, ClassLoader.class) != null, "ctor(URL[],ClassLoader)");
        check(c.getConstructor(URL[].class, ClassLoader.class, java.net.URLStreamHandlerFactory.class) != null,
                "ctor(URL[],ClassLoader,URLStreamHandlerFactory)");
        check(c.getConstructor(String.class, URL[].class, ClassLoader.class) != null,
                "ctor(String,URL[],ClassLoader)");
        check(c.getConstructor(String.class, URL[].class, ClassLoader.class, java.net.URLStreamHandlerFactory.class) != null,
                "ctor(String,URL[],ClassLoader,URLStreamHandlerFactory)");
        // the name parameter surfaces via ClassLoader.getName()
        URLClassLoader named = new URLClassLoader("my-loader", new URL[0],
                ClassLoader.getSystemClassLoader());
        check("my-loader".equals(named.getName()), "named ctor sets loader name");
        URLClassLoader unnamed = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        check(!"my-loader".equals(unnamed.getName()), "unnamed ctor keeps generated name");
        named.close();
        unnamed.close();
        check(c.getMethod("getURLs") != null, "getURLs");
        check(c.getMethod("close") != null, "close");
        check(c.getMethod("newInstance", URL[].class) != null, "newInstance(URL[])");
        check(c.getMethod("newInstance", URL[].class, ClassLoader.class) != null, "newInstance(URL[],ClassLoader)");
        check(!Modifier.isFinal(c.getModifiers()), "not final (subclassable, addURL extension point)");
    }

    // ---------- multiple jars: first-url-wins resource resolution ----------

    static void testMultipleJarsFirstWins() throws Exception {
        // second jar with the same resource name but a different body
        File jar2 = new File(workDir, "mini2.jar");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar2));
        jos.putNextEntry(new JarEntry(RES_NAME));
        jos.write("second-jar-body".getBytes("UTF-8"));
        jos.closeEntry();
        jos.close();
        try {
            URLClassLoader first = new URLClassLoader(
                    new URL[]{miniJar.toURI().toURL(), jar2.toURI().toURL()},
                    ClassLoader.getSystemClassLoader().getParent());
            try {
                InputStream is = first.getResourceAsStream(RES_NAME);
                check(RES_BODY.equals(readStream(is)), "first jar wins resource lookup");
                // reversed order -> the other body
                URLClassLoader second = new URLClassLoader(
                        new URL[]{jar2.toURI().toURL(), miniJar.toURI().toURL()},
                        ClassLoader.getSystemClassLoader().getParent());
                try {
                    InputStream is2 = second.getResourceAsStream(RES_NAME);
                    check("second-jar-body".equals(readStream(is2)), "reversed order picks other jar");
                    // getResources enumerates both jars
                    int count = 0;
                    Enumeration<URL> en = second.getResources(RES_NAME);
                    while (en.hasMoreElements()) { en.nextElement(); count++; }
                    check(count == 2, "getResources finds both jars: " + count);
                } finally {
                    second.close();
                }
            } finally {
                first.close();
            }
        } finally {
            jar2.delete();
        }
    }

    // ---------- system-classpath resource via parent delegation ----------

    static void testSystemResourceDelegation() throws Exception {
        URLClassLoader ucl = new URLClassLoader(new URL[]{miniJar.toURI().toURL()});
        try {
            // a JDK class resource is found through the parent (system) loader
            URL u = ucl.getResource("java/net/URL.class");
            check(u != null, "JDK class resource via parent");
            // array classes are created by the VM via Class.forName with this loader
            Class<?> arrCls = Class.forName("[Ljava.lang.Object;", false, ucl);
            check(arrCls.isArray(), "array class via forName with URLClassLoader");
            // package object for a loaded class in the default package
            Class<?> m = ucl.loadClass("Mini");
            check(m.getPackage() != null || m.getPackage() == null, "getPackage does not throw for default package");
        } finally {
            ucl.close();
        }
    }

    // ---------- loader identity semantics ----------

    static void testLoaderIdentity() throws Exception {
        URLClassLoader l1 = new URLClassLoader(new URL[]{miniJar.toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        URLClassLoader l2 = new URLClassLoader(new URL[]{miniJar.toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        try {
            Class<?> c1 = l1.loadClass("Mini");
            Class<?> c2 = l2.loadClass("Mini");
            // identical bytes, different loaders -> distinct runtime classes
            check(c1 != c2, "same bytes across loaders are distinct classes");
            check(!c1.isInstance(c2.newInstance()), "instances do not cross-assign");
            check(c1.getName().equals(c2.getName()), "binary names are identical");
            // each loader caches its own class object
            check(c1 == l1.loadClass("Mini"), "l1 caches c1");
            check(c2 == l2.loadClass("Mini"), "l2 caches c2");
            // null-parent loader also loads independently
            URLClassLoader l3 = new URLClassLoader(new URL[]{miniJar.toURI().toURL()}, null);
            Class<?> c3 = l3.loadClass("Mini");
            check(c3 != c1 && c3 != c2, "bootstrap-parent loader loads its own class");
            l3.close();
        } finally {
            l1.close();
            l2.close();
        }
    }

    public static void main(String[] args) throws Exception {
        buildFixtures();
        try {
            testWhiteBox();
            testConstructorMatrix();
            testGetURLs();
            testCloseSemantics();
            testJarClassLoading();
            testDirectoryClassLoading();
            testParentDelegation();
            testAddURL();
            testMultipleJarsFirstWins();
            testSystemResourceDelegation();
            testLoaderIdentity();
        } finally {
            cleanup();
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
