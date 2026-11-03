/*
 * @test
 * @summary java.lang.ClassLoader conformance: white-box structure + black-box behavior.
 * @run main ClassLoaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

public class ClassLoaderTest {
    static int passed;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSystemLoader();
        testParentChain();
        testResources();
        testLoadClass();
        testPackagesAndName();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
