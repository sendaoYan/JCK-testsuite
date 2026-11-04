/*
 * @test
 * @summary java.awt.ImageCapabilities conformance: white-box structure + black-box behavior.
 * @run main ImageCapabilitiesTest
 */

import java.lang.reflect.*;

public class ImageCapabilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.ImageCapabilities", false, ImageCapabilitiesTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.ImageCapabilities");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
    }

    static void testBlackBox() throws Exception {
        java.awt.ImageCapabilities acc = new java.awt.ImageCapabilities(true);
        check(acc.isAccelerated(), "accelerated ctor");
        check(!acc.isTrueVolatile(), "not true volatile by default");
        java.awt.ImageCapabilities plain = new java.awt.ImageCapabilities(false);
        check(!plain.isAccelerated(), "plain not accelerated");
        check(plain.isAccelerated() == false, "isAccelerated false");
        java.awt.ImageCapabilities cl = (java.awt.ImageCapabilities) acc.clone();
        check(cl != acc, "clone distinct");
        check(cl.isAccelerated() == acc.isAccelerated(), "clone equal value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
