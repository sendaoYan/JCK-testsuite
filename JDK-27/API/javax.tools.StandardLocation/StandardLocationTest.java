/*
 * @test
 * @summary javax.tools.StandardLocation conformance: enum.
 * @run main StandardLocationTest
 */

import java.lang.reflect.*;


public class StandardLocationTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.tools.StandardLocation");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length > 0, "enum constants");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager$Location"), "implements javax.tools.JavaFileManager$Location");
        check(hasMethod(c, "computeIsModuleOrientedLocation"), "declares computeIsModuleOrientedLocation");
        check(hasMethod(c, "locationFor"), "declares locationFor");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isOutputLocation"), "declares isOutputLocation");
        check(hasMethod(c, "isModuleOrientedLocation"), "declares isModuleOrientedLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
