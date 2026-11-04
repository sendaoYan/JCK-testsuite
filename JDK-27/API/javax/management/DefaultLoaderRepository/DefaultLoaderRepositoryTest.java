/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.DefaultLoaderRepository conformance: white-box structure + light behavior.
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
        deepRepositorySemantics();
        deepStructure();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

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

    // ==== appended deepening (2026-11): repository semantics ====

    static void deepRepositorySemantics() throws Exception {
        // with no tracked MBeanServer the repository consults nothing
        try {
            javax.management.DefaultLoaderRepository.loadClass("java.lang.String");
            check(false, "no tracked server must fail");
        } catch (ClassNotFoundException e) {
            check(true, "no tracked MBeanServer -> ClassNotFoundException");
        }
        // an untracked newMBeanServer does not contribute to the default repository
        javax.management.MBeanServer untracked = javax.management.MBeanServerFactory.newMBeanServer();
        try {
            javax.management.DefaultLoaderRepository.loadClass("java.lang.String");
            check(false, "untracked-only must fail");
        } catch (ClassNotFoundException e) {
            check(true, "untracked newMBeanServer does not feed the default loader repository");
        }
        javax.management.MBeanServer tracked = null;
        try {
            tracked = javax.management.MBeanServerFactory.createMBeanServer("jck.dlr.deep");
            check(javax.management.DefaultLoaderRepository.loadClass("java.lang.String") == String.class,
                    "loadClass resolves java.lang.String");
            check(javax.management.DefaultLoaderRepository.loadClass("javax.management.MBeanServer")
                            == javax.management.MBeanServer.class,
                    "loadClass resolves a javax.management class");
            check(javax.management.DefaultLoaderRepository.loadClassWithout(null, "java.lang.Integer")
                            == Integer.class,
                    "loadClassWithout(null, name) resolves like loadClass");
        } finally {
            if (tracked != null) {
                javax.management.MBeanServerFactory.releaseMBeanServer(tracked);
            }
        }
        // after release the consulted set is empty again
        try {
            javax.management.DefaultLoaderRepository.loadClass("java.lang.String");
            check(false, "post-release must fail");
        } catch (ClassNotFoundException e) {
            check(true, "releaseMBeanServer empties the consulted set");
        }
        // unknown class name, message carries the class name
        javax.management.MBeanServer t2 = javax.management.MBeanServerFactory.createMBeanServer("jck.dlr.deep2");
        try {
            try {
                javax.management.DefaultLoaderRepository.loadClass("no.such.ClassXYZ");
                check(false, "unknown class must fail");
            } catch (ClassNotFoundException e) {
                check(e.getMessage() != null && e.getMessage().contains("no.such.ClassXYZ"),
                        "ClassNotFoundException message carries the class name");
            }
            try {
                javax.management.DefaultLoaderRepository.loadClassWithout(null, "no.such.ClassZYX");
                check(false, "loadClassWithout unknown class must fail");
            } catch (ClassNotFoundException e) {
                check(true, "loadClassWithout unknown class -> ClassNotFoundException");
            }
        } finally {
            javax.management.MBeanServerFactory.releaseMBeanServer(t2);
        }
    }

    // ==== appended deepening (2026-11): structure ====

    static void deepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.DefaultLoaderRepository");
        check(java.lang.reflect.Modifier.isStatic(c.getMethod("loadClass", String.class).getModifiers()),
                "loadClass(String) is static");
        check(java.lang.reflect.Modifier.isStatic(
                        c.getMethod("loadClassWithout", ClassLoader.class, String.class).getModifiers()),
                "loadClassWithout is static");
        check(c.isAnnotationPresent(Deprecated.class), "class is @Deprecated");
    }
}

