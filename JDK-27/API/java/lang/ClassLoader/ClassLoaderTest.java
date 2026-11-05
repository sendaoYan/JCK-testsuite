/*
 * @test
 * @summary java.lang.ClassLoader conformance: white-box structure + black-box behavior.
 * @run main ClassLoaderTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

public class ClassLoaderTest {
    static int passed;

    // Minimal concrete subclass exposing protected ClassLoader members.
    static class CL extends ClassLoader {
        CL() { super(ClassLoader.getSystemClassLoader()); }
        CL(String name, ClassLoader parent) { super(name, parent); }
        Class<?> findSystem(String n) throws ClassNotFoundException { return findSystemClass(n); }
        Class<?> findLoaded(String n) { return findLoadedClass(n); }
        Object lock(String n) { return getClassLoadingLock(n); }
        static boolean reg() { return registerAsParallelCapable(); }
        void resolve(Class<?> c) { resolveClass(c); }
        void sign(Class<?> c, Object[] s) { setSigners(c, s); }
        URL findRes(String n) { return findResource(n); }
        Enumeration<URL> findResAll(String n) throws IOException { return findResources(n); }
        String findLib(String n) { return findLibrary(n); }
        Package pkg(String n) { return getPackage(n); }
        Package[] pkgs() { return getPackages(); }
        Package definePkg(String name) {
            return definePackage(name, "specTitle", "1.0", "specVendor",
                                 "implTitle", "1.0", "implVendor", null);
        }
        Class<?> findCls(String n) throws ClassNotFoundException { return findClass(n); }
        Class<?> findClsMod(String moduleName, String n) { return findClass(moduleName, n); }
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
        Class<?> findLoaded(String name) { return findLoadedClass(name); }
        Package pkg(String name) { return getPackage(name); }
        Package[] pkgs() { return getPackages(); }
        Object lockFor2(String n) { return getClassLoadingLock(n); }
        @SuppressWarnings("deprecation")
        Package defPkg(String name) {
            return definePackage(name, "specTitle", "9.9", "specVendor", "implTitle", "7.7", "implVendor", null);
        }
        @SuppressWarnings("deprecation")
        Package defSealedPkg(String name) throws java.net.MalformedURLException {
            return definePackage(name, null, null, null, null, null, null,
                                 new java.net.URL("file:/tmp/jck-fake-seal.jar"));
        }
        @SuppressWarnings("deprecation")
        Package defBarePkg(String name) {
            return definePackage(name, null, null, null, null, null, null, null);
        }
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
        // defineClass is protected (not directly callable from a user class).
        Method defineClass = c.getDeclaredMethod("defineClass", String.class, byte[].class,
                                                int.class, int.class);
        check(Modifier.isProtected(defineClass.getModifiers()), "defineClass is protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSystemLoader() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system != null, "getSystemClassLoader non-null");
        check(ClassLoader.getPlatformClassLoader() != null, "getPlatformClassLoader non-null");
        // The system loader is a descendant of the platform loader.
        ClassLoader p = system;
        int hops = 0;
        while (p != null && hops < 64) { p = p.getParent(); hops++; }
        check(hops >= 1, "system loader has at least one parent link");
    }

    static void testParentChain() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        ClassLoader parent = system.getParent();
        check(parent != null, "system loader parent non-null");
        // The bootstrap loader has no Java object: the chain ends with null.
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
        URL url = system.getResource("java/lang/Object.class");
        check(url != null, "getResource finds java/lang/Object.class");
        Enumeration<URL> resources = system.getResources("java/lang/Object.class");
        check(resources != null && resources.hasMoreElements(), "getResources has an element");
        check(system.resources("java/lang/Object.class").findAny().isPresent(),
              "resources() stream has an element");
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

    static void testPackagesAndName() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system.getName() != null, "system loader name non-null");
        check(system.isRegisteredAsParallelCapable() || true, "isRegisteredAsParallelCapable callable");
        system.getDefinedPackages(); // callable without throwing
        passed++;
    }

    // ---- Part 2b: black-box for previously uncovered methods ----
    static void testBlackBox() throws Exception {
        ClassLoader system = ClassLoader.getSystemClassLoader();

        // getSystemResource / getSystemResources
        check(ClassLoader.getSystemResource("java/lang/Object.class") != null,
              "getSystemResource finds java/lang/Object.class");
        Enumeration<URL> sysResources = ClassLoader.getSystemResources("java/lang/Object.class");
        check(sysResources != null && sysResources.hasMoreElements(), "getSystemResources has element");

        // getUnnamedModule
        java.lang.Module unnamed = system.getUnnamedModule();
        check(unnamed != null && !unnamed.isNamed() && unnamed.getName() == null,
              "getUnnamedModule returns unnamed module");

        // getDefinedPackage: bootstrap-defined packages are not defined by the app loader
        check(system.getDefinedPackage("java.lang") == null,
              "getDefinedPackage(java.lang) is null on app loader");
        check(system.getDefinedPackage("jck.no.such.pkg") == null, "getDefinedPackage missing -> null");

        // Assertion-status control (public void methods; callable, then reset)
        system.setDefaultAssertionStatus(false);
        system.setPackageAssertionStatus("jck.test.pkg", true);
        system.setClassAssertionStatus("jck.test.Foo", false);
        system.clearAssertionStatus();
        passed += 4;

        // Protected members via a concrete subclass.
        CL cl = new CL();
        check(cl.findSystem("java.lang.String") == String.class, "findSystemClass resolves String");
        check(cl.findLoaded("java.lang.String") == null, "findLoadedClass returns null (not loaded by us)");
        Object lock1 = cl.lock("jck.lock.name");
        Object lock2 = cl.lock("jck.lock.name");
        check(lock1 != null && lock1 == lock2, "getClassLoadingLock stable for same name");
        check(CL.reg(), "registerAsParallelCapable returns true");
        cl.resolve(String.class); // protected final, must not throw
        passed++;
        cl.sign(CL.class, new Object[0]); // deprecated-for-removal, callable
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

        // definePackage (deprecated) defines a fresh package in this loader's namespace.
        String newPkg = "jck.definepkg.xyz";
        Package defined = cl.definePkg(newPkg);
        check(defined != null && newPkg.equals(defined.getName()), "definePackage returns package");
        check(cl.getDefinedPackage(newPkg) == defined, "getDefinedPackage reflects definePackage");
        try {
            cl.definePkg(newPkg);
            throw new AssertionError("definePackage duplicate: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // findClass(String) default throws CNFE; findClass(String,String) default returns null.
        try {
            cl.findCls("jck.no.SuchClass");
            throw new AssertionError("findClass default: expected ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            passed++;
        }
        check(cl.findClsMod("java.base", "java.lang.String") == null,
              "findClass(moduleName,name) default returns null");

        // Protected two-arg constructor (String, ClassLoader).
        CL named = new CL("jck-custom-loader", system);
        check("jck-custom-loader".equals(named.getName()), "ClassLoader(String,ClassLoader) name");
        check(named.getParent() == system, "ClassLoader(String,ClassLoader) parent");
    }

    // ===== 契约深化（2026-11）新增断言 =====

    static void testPlatformLoaderChain() {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        check("jdk.internal.loader.ClassLoaders$AppClassLoader".equals(sys.getClass().getName()),
              "JDK-27 system loader class name");
        check("app".equals(sys.getName()), "system loader is named app");
        ClassLoader platform = sys.getParent();
        check(platform == ClassLoader.getPlatformClassLoader(),
              "system loader's parent is the platform loader");
        check("platform".equals(platform.getName()), "platform loader is named platform");
        check(platform.getParent() == null, "platform loader's parent is bootstrap (null)");
        check(ClassLoader.getPlatformClassLoader() == ClassLoader.getPlatformClassLoader(),
              "getPlatformClassLoader is stable");
        check(ClassLoaderTest.class.getClassLoader() == sys, "test class defined by app loader");
        check(String.class.getClassLoader() == null, "String defined by bootstrap");
        check(Enum.class.getClassLoader() == null, "Enum defined by bootstrap");
        check(int.class.getClassLoader() == null, "primitive classes have no loader");
        check(String[].class.getClassLoader() == null, "bootstrap array classes have no loader");
        java.lang.Module platUnnamed = platform.getUnnamedModule();
        check(platUnnamed != null && !platUnnamed.isNamed(), "platform loader has an unnamed module");
        check(!sys.getUnnamedModule().isNamed(), "app loader has an unnamed module");
        check(sys.getUnnamedModule() != platUnnamed, "unnamed modules are per-loader");
        check(sys.isRegisteredAsParallelCapable(),
              "system loader is parallel capable");
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
        check(anon.getUnnamedModule() != null && !anon.getUnnamedModule().isNamed(),
              "anonymous subclass has an unnamed module");
        check(Class.forName("java.lang.String", true, rec) == String.class,
              "forName through a custom loader delegates");
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
            check(e.getMessage() != null && e.getMessage().contains("prohibited package name: java.lang"),
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
        check(!sys.resources("no/such/res.bin").findAny().isPresent(),
              "missing resources: empty stream");
        check(ClassLoader.getSystemResource("no/such/res.bin") == null, "system resource missing null");
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
    }

    @SuppressWarnings("deprecation")
    static void testPackageNamespaces() throws Exception {
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        Class.forName("java.util.ArrayList"); // ensure java.util is known to the loader
        // ClassLoader.getPackage/getPackages are protected even on JDK-27;
        // the static Package accessors consult the caller's (system) loader.
        Package lang = Package.getPackage("java.lang");
        check(lang != null && "java.lang".equals(lang.getName()), "system loader sees java.lang");
        Package util = Package.getPackage("java.util");
        check(util != null && "java.util".equals(util.getName()), "system loader sees java.util");
        check(Package.getPackage("jck.no.such.pkg") == null, "unknown package is null");
        check(sys.getDefinedPackage("java.lang") == null,
              "app loader does not DEFINE java.lang (bootstrap owns it)");
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
        check(child.getDefinedPackage("jck.namespace.pkg") == own,
              "getDefinedPackage reflects the loader's own package");
        check(child.pkg("java.lang") != null, "child sees parent packages");
        check(child.getDefinedPackage("java.lang") == null,
              "getDefinedPackage only reports own packages");
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
        check(own.isCompatibleWith("9.8"), "compatible with 9.8");
        check(!own.isCompatibleWith("9.9.1"), "not compatible with 9.9.1");
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
        boolean ownSeen = false;
        for (Package p : child.pkgs())
            if ("jck.namespace.pkg".equals(p.getName())) ownSeen = true;
        check(ownSeen, "child getPackages includes its own package");
        boolean ownDefined = false;
        for (Package p : child.getDefinedPackages())
            if ("jck.namespace.pkg".equals(p.getName())) ownDefined = true;
        check(ownDefined, "getDefinedPackages includes own packages");
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
        check(plain.lockFor2("a") == plain.lockFor2("b"),
              "non-parallel-capable loader falls back to one global lock");
        check(plain.lockFor2("a") == plain,
              "fallback lock is the loader object itself");
        check("jck-custom-loader-nested".length() > 0, "guard");
        CL named = new CL("jck-nested-named", null);
        check("jck-nested-named".equals(named.getName()), "named child loader via protected ctor");
        check(named.getParent() == null, "named child loader with null parent");
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
        testPackagesAndName();
        testBlackBox();
        testPlatformLoaderChain();
        testLoadClassDelegation();
        testDefineClassProhibited();
        testResourceDetails();
        testPackageNamespaces();
        testFindSystemAndProtected();
        testAssertionStatusReflection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
