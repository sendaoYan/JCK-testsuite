/*
 * @test
 * @summary javax.management.DefaultLoaderRepository conformance: white-box structure + light behavior.
 * @run main DefaultLoaderRepositoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

@SuppressWarnings("deprecation")
public class DefaultLoaderRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DefaultLoaderRepository");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated class");
        check(hasMethod(c, "loadClass"), "has static loadClass");
        check(hasMethod(c, "loadClassWithout"), "has static loadClassWithout");
        check(Modifier.isStatic(c.getMethod("loadClass", String.class).getModifiers()), "loadClass is static");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // DefaultLoaderRepository consults the ClassLoaderRepository of each
        // tracked MBean server (findMBeanServer(null)). A fresh JVM has none,
        // so create a tracked server first; release it in finally.
        MBeanServer mbs = MBeanServerFactory.createMBeanServer("jck.dlr");
        try {
            check(DefaultLoaderRepository.loadClass("java.lang.String") == String.class, "loadClass resolves String");
            check(DefaultLoaderRepository.loadClass("java.util.ArrayList") == java.util.ArrayList.class, "loadClass resolves ArrayList");
            check(DefaultLoaderRepository.loadClassWithout(null, "java.lang.Integer") == Integer.class, "loadClassWithout resolves Integer");
        } finally {
            MBeanServerFactory.releaseMBeanServer(mbs);
        }

        // missing class throws ClassNotFoundException
        boolean threw = false;
        try { DefaultLoaderRepository.loadClass("does.not.Exist"); } catch (ClassNotFoundException ex) { threw = true; }
        check(threw, "loadClass throws ClassNotFoundException for missing class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
