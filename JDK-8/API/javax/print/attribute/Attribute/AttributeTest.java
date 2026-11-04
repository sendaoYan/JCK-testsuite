/*
 * @test
 * @summary javax.print.attribute.Attribute conformance: interface.
 * @run main AttributeTest
 */

import java.lang.reflect.*;


public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.Attribute");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "java.io.Serializable"), "extends java.io.Serializable");
        check(hasMethod(c, "getCategory"), "declares getCategory");
        check(hasMethod(c, "getName"), "declares getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
