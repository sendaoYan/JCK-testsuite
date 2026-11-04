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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSystemLoader();
        testParentChain();
        testResources();
        testLoadClass();
        testAssertionStatus();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
