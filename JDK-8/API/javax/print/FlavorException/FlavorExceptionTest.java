/*
 * @test
 * @summary javax.print.FlavorException conformance: interface.
 * @run main FlavorExceptionTest
 */

import java.lang.reflect.*;


public class FlavorExceptionTest {
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
        Class<?> c = Class.forName("javax.print.FlavorException");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getUnsupportedFlavors"), "declares getUnsupportedFlavors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
