/*
 * @test
 * @summary java.lang.ClassLoader conformance: white-box structure (abstract,
 *          Object superclass, protected defineClass) + black-box behavior
 *          (system loader, parent chain termination at bootstrap/null, resource
 *          lookup incl. missing -> null, loadClass known + missing CNFE, assertion
 *          status control, and protected members via a concrete subclass).
 * @run main ClassLoaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

public class ClassLoaderTest {
    static int passed;

    // Minimal concrete subclass exposing protected ClassLoader members.
    static class CL extends ClassLoader {
        CL() { super(ClassLoader.getSystemClassLoader()); }
        Class<?> findSystem(String n) throws ClassNotFoundException { return findSystemClass(n); }
        Class<?> findLoaded(String n) { return findLoadedClass(n); }
        Object lock(String n) { return getClassLoadingLock(n); }
        static boolean reg() { return registerAsParallelCapable(); }
        void resolve(Class<?> c) { resolveClass(c); }
        void sign(Class<?> c, Object[] s) { setSigners(c, s); }
        URL findRes(String n) { return findResource(n); }
        Enumeration<URL> findResAll(String n) throws java.io.IOException { return findResources(n); }
        String findLib(String n) { return findLibrary(n); }
        Package pkg(String n) { return getPackage(n); }
        Package[] pkgs() { return getPackages(); }
        Package definePkg(String name) {
            return definePackage(name, "specTitle", "1.0", "specVendor",
                                 "implTitle", "1.0", "implVendor", null);
        }
        Class<?> findCls(String n) throws ClassNotFoundException { return findClass(n); }
        Object lockFor(String n) { return getClassLoadingLock(n); }
    }

    // ===== 契约深化（2026-11）新增 fixtures =====
    static class RecordingLoader extends ClassLoader {
        final java.util.List<String> log = new java.util.ArrayList<String>();
        RecordingLoader(ClassLoader parent) { super(parent); }
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            log.add("load:" + name);
            try {
                Class<?> c = super.loadClass(name, resolve);
                log.add("ok:" + name);
                return c;
            } catch (ClassNotFoundException e) {
                log.add("fail:" + name);
                throw e;
            }
        }
        Class<?> defineByName(String name, byte[] b, int off, int len) { return defineClass(name, b, off, len); }
        Package defPkg(String name) {
            return definePackage(name, "specTitle", "9.9", "specVendor", "implTitle", "7.7", "implVendor", null);
        }
        Package defSealedPkg(String name) throws java.net.MalformedURLException {
            return definePackage(name, null, null, null, null, null, null,
                                 new java.net.URL("file:/tmp/jck-fake-seal.jar"));
        }
        Package defBarePkg(String name) {
            return definePackage(name, null, null, null, null, null, null, null);
        }
        Package pkg(String name) { return getPackage(name); }
        Package[] pkgs() { return getPackages(); }
        Class<?> findLoaded(String name) { return findLoadedClass(name); }
        Object lockFor(String n) { return getClassLoadingLock(n); }
    }


    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ClassLoader");
        check(Modifier.isAbstract(c.getModifiers()), "ClassLoader is abstract");
        check(c.getSuperclass() == Object.class, "ClassLoader extends Object");
        Method defineClass = c.getDeclaredMethod("defineClass", String.class, byte[].class,
                                                int.class, int.class);
        check(Modifier.isProtected(defineClass.getModifiers()), "defineClass is protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSystemLoader() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system != null, "getSystemClassLoader non-null");
    }

    static void testParentChain() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system.getParent() != null, "system loader parent non-null");
        ClassLoader cur = system;
        int depth = 0;
        while (cur != null && depth < 128) { cur = cur.getParent(); depth++; }
        check(depth >= 1, "parent chain terminates (bootstrap loader is null)");
    }

    static void testResources() throws Exception {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system.getResourceAsStream("java/lang/Object.class") != null,
              "getResourceAsStream reads java/lang/Object.class");
        check(ClassLoader.getSystemResourceAsStream("java/lang/Object.class") != null,
              "getSystemResourceAsStream reads java/lang/Object.class");
        check(ClassLoader.getSystemResource("java/lang/Object.class") != null,
              "getSystemResource finds java/lang/Object.class");
        URL url = system.getResource("java/lang/Object.class");
        check(url != null, "getResource finds java/lang/Object.class");
        Enumeration<URL> resources = system.getResources("java/lang/Object.class");
        check(resources != null && resources.hasMoreElements(), "getResources has an element");
        Enumeration<URL> sysResources = ClassLoader.getSystemResources("java/lang/Object.class");
        check(sysResources != null && sysResources.hasMoreElements(), "getSystemResources has element");
        check(system.getResourceAsStream("jck/no/such/resource.bin") == null,
              "getResourceAsStream missing resource -> null");
    }

    static void testLoadClass() throws Exception {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system.loadClass("java.lang.String") == String.class, "loadClass java.lang.String");
        check(system.loadClass("java.lang.Object") == Object.class, "loadClass java.lang.Object");
        check(Class.forName("[Ljava.lang.String;") != null, "Class.forName resolves array type");
        try {
            system.loadClass("jck.no.SuchClass");
            throw new AssertionError("loadClass(missing): expected ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            passed++;
        }
    }

    static void testAssertionStatus() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        system.setDefaultAssertionStatus(false);
        system.setPackageAssertionStatus("jck.test.pkg", true);
        system.setClassAssertionStatus("jck.test.Foo", false);
        system.clearAssertionStatus();
        passed += 4;
    }

    // ---- Part 2b: protected members via a concrete subclass ----
    static void testBlackBox() throws Exception {
        CL cl = new CL();
        check(cl.findSystem("java.lang.String") == String.class, "findSystemClass resolves String");
        check(cl.findLoaded("java.lang.String") == null, "findLoadedClass returns null (not loaded by us)");
        Object lock1 = cl.lock("jck.lock.name");
        Object lock2 = cl.lock("jck.lock.name");
        check(lock1 != null && lock1 == lock2, "getClassLoadingLock stable for same name");
        check(CL.reg(), "registerAsParallelCapable returns true");
        cl.resolve(String.class); // protected final, must not throw
        passed++;
        cl.sign(CL.class, new Object[0]); // deprecated, callable
        passed++;
        check(cl.findRes("java/lang/Object.class") == null, "findResource default returns null");
        check(!cl.findResAll("java/lang/Object.class").hasMoreElements(),
              "findResources default is empty");
        check(cl.findLib("jck-no-lib") == null, "findLibrary default returns null");
        check(cl.pkg("java.lang") != null && "java.lang".equals(cl.pkg("java.lang").getName()),
              "getPackage finds bootstrap java.lang via ancestors");
        boolean pkgsContainJavaLang = false;
        for (Package p : cl.pkgs()) if ("java.lang".equals(p.getName())) pkgsContainJavaLang = true;
        check(pkgsContainJavaLang, "getPackages includes java.lang");

        // definePackage defines a fresh package in this loader's namespace.
        String newPkg = "jck.definepkg.xyz";
        Package defined = cl.definePkg(newPkg);
        check(defined != null && newPkg.equals(defined.getName()), "definePackage returns package");
        try {
            cl.definePkg(newPkg);
            throw new AssertionError("definePackage duplicate: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // findClass(String) default throws CNFE.
        try {
            cl.findCls("jck.no.SuchClass");
            throw new AssertionError("findClass default: expected ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            passed++;
        }
    }

    // ===== 契约深化（2026-11）新增断言 =====

    static void testLoaderIdentityChain() throws Exception {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        check(ClassLoader.getSystemClassLoader() == sys, "getSystemClassLoader returns a stable instance");
        check(sys == Thread.currentThread().getContextClassLoader(),
              "system loader == main thread context loader");
        check("sun.misc.Launcher$AppClassLoader".equals(sys.getClass().getName()),
              "JDK-8 system loader class name");
        ClassLoader ext = sys.getParent();
        check(ext != null, "system loader parent non-null");
        check("sun.misc.Launcher$ExtClassLoader".equals(ext.getClass().getName()),
              "JDK-8 parent is the extension loader");
        check(ext.getParent() == null, "extension loader parent is bootstrap (null)");
        check(sys.getParent() != sys, "parent differs from system loader");
        check(String.class.getClassLoader() == null, "String defined by bootstrap");
        check(Object.class.getClassLoader() == null, "Object defined by bootstrap");
        check(Enum.class.getClassLoader() == null, "Enum defined by bootstrap");
        check(ClassLoader.class.getClassLoader() == null, "ClassLoader itself defined by bootstrap");
        check(int.class.getClassLoader() == null, "primitive classes have no loader");
        check(String[].class.getClassLoader() == null, "bootstrap array classes have no loader");
        check(ClassLoaderTest.class.getClassLoader() == sys, "test class defined by system loader");
        RecordingLoader rl = new RecordingLoader(null);
        check(rl.getParent() == null, "null-parent loader reports null");
        check(rl.loadClass("java.lang.String") == String.class,
              "null parent means delegation to bootstrap");
    }

    static void testLoadClassDelegation() throws Exception {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        String[] names = { "java.lang.String", "java.lang.Object", "java.lang.Integer",
                           "java.lang.Runnable", "java.lang.Enum",
                           "java.lang.annotation.Annotation" };
        Class<?>[] expected = { String.class, Object.class, Integer.class, Runnable.class,
                                Enum.class, java.lang.annotation.Annotation.class };
        for (int i = 0; i < names.length; i++) {
            check(sys.loadClass(names[i]) == expected[i], "loadClass " + names[i]);
            check(sys.loadClass(names[i]) == sys.loadClass(names[i]),
                  "loadClass caches Class objects: " + names[i]);
        }
        check(Class.forName("java.lang.String", true, sys) == String.class,
              "forName with explicit system loader");
        check(Class.forName("java.lang.String", false, sys) == String.class,
              "forName without initialization via system loader");
        check(sys.loadClass("ClassLoaderTest$CL") == CL.class, "loadClass nested class");
        try {
            sys.loadClass("[Ljava.lang.String;");
            throw new AssertionError("array descriptor via loadClass: expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            check("[Ljava.lang.String;".equals(e.getMessage()),
                  "array descriptors are not loadClass names (message exact)");
        }
        try {
            sys.loadClass(null);
            throw new AssertionError("loadClass(null): expected NullPointerException");
        } catch (NullPointerException expectedNpe) {
            passed++;
        }
        RecordingLoader rec = new RecordingLoader(sys);
        Class<?> c = rec.loadClass("java.lang.String");
        check(c == String.class, "recording loader resolves String via delegation");
        check("load:java.lang.String".equals(rec.log.get(0)), "loadClass hook invoked before delegation");
        check("ok:java.lang.String".equals(rec.log.get(1)), "parent delegation reported success");
        check(rec.log.size() == 2, "parent does not recurse into child loadClass");
        check(String.class.getClassLoader() == null, "String still owned by bootstrap");
        check(rec.findLoaded("java.lang.String") == null,
              "delegation does not define the class in the child");
        rec.loadClass(ClassLoaderTest.class.getName());
        check(rec.log.contains("ok:" + ClassLoaderTest.class.getName()),
              "classpath class resolved through parent");
        try {
            rec.loadClass("jck.no.Such2");
            throw new AssertionError("missing class via recording loader: expected CNFE");
        } catch (ClassNotFoundException e) {
            check("jck.no.Such2".equals(e.getMessage()), "recording loader CNFE message exact");
            check(rec.log.contains("fail:jck.no.Such2"), "failure recorded in log");
        }
        RecordingLoader boot = new RecordingLoader(null);
        check(boot.loadClass("java.lang.Integer") == Integer.class,
              "null-parent loader still reaches bootstrap classes");
        try {
            boot.loadClass(ClassLoaderTest.class.getName());
            throw new AssertionError("bootstrap-only lookup: expected CNFE for classpath class");
        } catch (ClassNotFoundException expectedCnfe) {
            passed++;
        }
        ClassLoader anon = new ClassLoader(sys) {};
        check(anon.getParent() == sys, "anonymous subclass keeps its parent");
        check(anon.loadClass("java.lang.String") == String.class, "anonymous subclass loads String");
        check(anon.toString() != null, "loader toString non-null");
    }

    static void testDefineClassProhibited() throws Exception {
        java.io.InputStream in = String.class.getResourceAsStream("/java/lang/String.class");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
        in.close();
        byte[] bytes = bos.toByteArray();
        check(bytes.length > 4, "String.class bytes were read fully");
        RecordingLoader dl = new RecordingLoader(null);
        try {
            dl.defineByName("java.lang.String", bytes, 0, bytes.length);
            throw new AssertionError("defineClass(java.lang...): expected SecurityException");
        } catch (SecurityException e) {
            check("Prohibited package name: java.lang".equals(e.getMessage()),
                  "prohibited package message exact: " + e.getMessage());
        }
        try {
            dl.defineByName(null, bytes, 0, bytes.length);
            throw new AssertionError("defineClass(null-name java.lang bytes): expected SecurityException");
        } catch (SecurityException e) {
            check("Prohibited package name: java.lang".equals(e.getMessage()),
                  "name inferred from bytecode is also screened");
        }
        try {
            dl.defineByName("java.util.Fake", bytes, 0, bytes.length);
            throw new AssertionError("defineClass(java.util...): expected SecurityException");
        } catch (SecurityException e) {
            check("Prohibited package name: java.util".equals(e.getMessage()),
                  "screening uses the package prefix");
        }
    }

    static void testResourceDetails() throws Exception {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        check(sys.getResourceAsStream("java/lang/String.class") != null,
              "String.class readable through system loader");
        java.io.InputStream in = sys.getResourceAsStream("java/lang/Object.class");
        byte[] magic = new byte[4];
        int off = 0;
        while (off < 4) {
            int r = in.read(magic, off, 4 - off);
            if (r < 0) break;
            off += r;
        }
        in.close();
        check(off == 4 && (magic[0] & 0xFF) == 0xCA, "resource starts with magic 0xCA");
        check((magic[1] & 0xFF) == 0xFE, "magic second byte 0xFE");
        check((magic[2] & 0xFF) == 0xBA, "magic third byte 0xBA");
        check((magic[3] & 0xFF) == 0xBE, "magic fourth byte 0xBE");
        check(sys.getResource("no/such/res.bin") == null, "missing resource is null");
        java.util.Enumeration<URL> empty = sys.getResources("no/such/res.bin");
        check(empty != null && !empty.hasMoreElements(), "missing resources: empty enumeration");
        check(ClassLoader.getSystemResource("no/such/res.bin") == null, "system resource missing null");
        java.util.Enumeration<URL> sysEmpty = ClassLoader.getSystemResources("no/such/res.bin");
        check(sysEmpty != null && !sysEmpty.hasMoreElements(), "system resources empty for missing");
        URL url = sys.getResource("java/lang/Object.class");
        check(url != null && url.getProtocol() != null, "resource URL carries a protocol");
        check(url.toExternalForm().endsWith("java/lang/Object.class"), "URL points at the resource");
        check(sys.getResource("/java/lang/Object.class") == null,
              "leading slash is not stripped for ClassLoader lookups");
        check(ClassLoader.getSystemResourceAsStream("java/lang/Object.class") != null,
              "getSystemResourceAsStream non-null");
        RecordingLoader rec = new RecordingLoader(sys);
        check(rec.getResource("java/lang/Object.class") != null,
              "resource lookup delegates to parent");
        java.util.Enumeration<URL> recs = rec.getResources("java/lang/Object.class");
        check(recs != null && recs.hasMoreElements(), "resource enumeration delegates to parent");
        check(rec.getResource("no/such/res.bin") == null, "child missing resource null");
        check(Class.forName("java.lang.String", true, rec) == String.class,
              "forName through a custom loader delegates");
    }

    static void testPackageNamespaces() throws Exception {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        Class.forName("java.util.ArrayList"); // ensure java.util is known to the loader
        // JDK-8: ClassLoader.getPackage/getPackages are protected; the static
        // Package accessors consult the caller's (system) loader instead.
        Package lang = Package.getPackage("java.lang");
        check(lang != null && "java.lang".equals(lang.getName()), "system loader sees java.lang");
        Package util = Package.getPackage("java.util");
        check(util != null && "java.util".equals(util.getName()), "system loader sees java.util");
        check(Package.getPackage("jck.no.such.pkg") == null, "unknown package is null");
        boolean hasLang = false, hasUtil = false;
        for (Package p : Package.getPackages()) {
            if ("java.lang".equals(p.getName())) hasLang = true;
            if ("java.util".equals(p.getName())) hasUtil = true;
        }
        check(hasLang, "getPackages includes java.lang");
        check(hasUtil, "getPackages includes java.util");
        RecordingLoader child = new RecordingLoader(sys);
        Package own = child.defPkg("jck.namespace.pkg");
        check(own != null && "jck.namespace.pkg".equals(own.getName()), "child defines its own package");
        check(child.pkg("jck.namespace.pkg") == own, "child sees its own package");
        check(child.pkg("java.lang") != null, "child sees parent packages");
        check(Package.getPackage("jck.namespace.pkg") == null,
              "system loader does not see child-defined packages (per-loader namespaces)");
        check("specTitle".equals(own.getSpecificationTitle()), "defined spec title round-trip");
        check("9.9".equals(own.getSpecificationVersion()), "defined spec version round-trip");
        check("specVendor".equals(own.getSpecificationVendor()), "defined spec vendor round-trip");
        check("implTitle".equals(own.getImplementationTitle()), "defined impl title round-trip");
        check("7.7".equals(own.getImplementationVersion()), "defined impl version round-trip");
        check("implVendor".equals(own.getImplementationVendor()), "defined impl vendor round-trip");
        check(!own.isSealed(), "no sealBase yields an unsealed package");
        check(own.isCompatibleWith("9.9"), "compatible with its own version");
        check(!own.isCompatibleWith("10.0"), "not compatible with a newer version");
        check(own.isCompatibleWith("1.0"), "compatible with older versions");
        check("package jck.namespace.pkg, specTitle, version 9.9".equals(own.toString()),
              "defined package toString exact: " + own.toString());
        check(own.hashCode() == "jck.namespace.pkg".hashCode(), "package hashCode is the name hash");
        Package sealed = child.defSealedPkg("jck.sealed.pkg");
        check(sealed != null && sealed.isSealed(), "non-null sealBase yields a sealed package");
        check("jck.sealed.pkg".equals(sealed.getName()), "sealed package name round-trip");
        Package bare = child.defBarePkg("jck.bare.pkg");
        check(bare != null && bare.getSpecificationTitle() == null
              && bare.getImplementationTitle() == null, "all-null attributes allowed");
        check(bare.isSealed() == false, "bare package unsealed");
        try {
            bare.isCompatibleWith("1.0");
            throw new AssertionError("null specVersion isCompatibleWith: expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
        check("package jck.bare.pkg".equals(bare.toString()), "bare package toString exact");
        check(bare.hashCode() == "jck.bare.pkg".hashCode(), "bare package hash is the name hash");
        check(own.isCompatibleWith("9.8"), "compatible with 9.8");
        check(!own.isCompatibleWith("9.9.1"), "not compatible with 9.9.1");
        boolean ownSeen = false;
        for (Package p : child.pkgs())
            if ("jck.namespace.pkg".equals(p.getName())) ownSeen = true;
        check(ownSeen, "child getPackages includes its own package");
    }

    static void testFindSystemAndProtected() throws Exception {
        CL cl = new CL();
        check(cl.findSystem("ClassLoaderTest") == ClassLoaderTest.class,
              "findSystemClass resolves classpath classes");
        try {
            cl.findSystem("jck.no.Such3");
            throw new AssertionError("findSystemClass missing: expected CNFE");
        } catch (ClassNotFoundException e) {
            check("jck.no.Such3".equals(e.getMessage()), "findSystemClass CNFE message exact");
        }
        check(cl.findLoaded("ClassLoaderTest") == null,
              "classes defined by the parent are not loaded by the child");
        cl.resolve(ClassLoaderTest.class); // idempotent for an already-linked class
        passed++;
        Object[] signers = { new Object() };
        cl.sign(ClassLoaderTest.class, signers);
        check(ClassLoaderTest.class.getSigners() != null, "setSigners installs a signers array");
        check(ClassLoaderTest.class.getSigners() != signers, "signers array is defensively copied");
        check(cl.findLib("no-such-lib") == null, "findLibrary default null");
        check(cl.findRes("java/lang/Object.class") == null, "findResource default null");
        check(!cl.findResAll("java/lang/Object.class").hasMoreElements(),
              "findResources default empty");
        check(cl.lockFor("x") == cl.lockFor("x"), "getClassLoadingLock stable per name");
        check(cl.lockFor("a") != cl.lockFor("b"),
              "parallel-capable loader (CL.reg() registered it) uses per-name locks");
        RecordingLoader plain = new RecordingLoader(ClassLoader.getSystemClassLoader());
        check(plain.lockFor("a") == plain.lockFor("b"),
              "non-parallel-capable loader falls back to one global lock");
        check(plain.lockFor("a") == plain,
              "fallback lock is the loader object itself");
    }

    static void testAssertionStatusReflection() {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        try {
            sys.setDefaultAssertionStatus(true);
            check(ClassLoaderTest.class.desiredAssertionStatus(),
                  "desiredAssertionStatus reflects loader default");
            // Only classes DEFINED by this loader consult it: bootstrap classes
            // (like ArrayList) do not, so the default package is used instead.
            sys.setPackageAssertionStatus("", true);
            check(ClassLoaderTest.class.desiredAssertionStatus(),
                  "package-specific status overrides the default");
            sys.setPackageAssertionStatus("", false);
            check(ClassLoaderTest.class.desiredAssertionStatus(),
                  "default still true after package reset");
            sys.setClassAssertionStatus(ClassLoaderTest.class.getName(), false);
            check(!ClassLoaderTest.class.desiredAssertionStatus(),
                  "class-specific status wins over both");
        } finally {
            sys.clearAssertionStatus();
        }
        check(!ClassLoaderTest.class.desiredAssertionStatus(),
              "clearAssertionStatus restores the default");
        check(!String.class.desiredAssertionStatus(),
              "bootstrap classes stay unasserted after clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSystemLoader();
        testParentChain();
        testResources();
        testLoadClass();
        testAssertionStatus();
        testBlackBox();
        testLoaderIdentityChain();
        testLoadClassDelegation();
        testDefineClassProhibited();
        testResourceDetails();
        testPackageNamespaces();
        testFindSystemAndProtected();
        testAssertionStatusReflection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
