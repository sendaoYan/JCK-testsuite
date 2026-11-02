/*
 * @test
 * @summary jdk.management.jfr.RecordingInfo conformance: class.
 * @run main RecordingInfoTest
 */

import java.lang.reflect.*;


public class RecordingInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.RecordingInfo");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getDumpOnExit"), "declares getDumpOnExit");
        check(hasMethod(c, "getMaxAge"), "declares getMaxAge");
        check(hasMethod(c, "getMaxSize"), "declares getMaxSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
