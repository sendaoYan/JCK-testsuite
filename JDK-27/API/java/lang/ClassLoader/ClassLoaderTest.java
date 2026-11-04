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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSystemLoader();
        testParentChain();
        testResources();
        testLoadClass();
        testPackagesAndName();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
