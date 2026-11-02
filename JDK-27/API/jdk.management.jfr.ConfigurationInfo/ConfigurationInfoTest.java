/*
 * @test
 * @summary jdk.management.jfr.ConfigurationInfo conformance: class.
 * @run main ConfigurationInfoTest
 */

import java.lang.reflect.*;


public class ConfigurationInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "createMap"), "declares createMap");
        check(hasMethod(c, "getProvider"), "declares getProvider");
        check(hasMethod(c, "getContents"), "declares getContents");
        check(hasMethod(c, "getSettings"), "declares getSettings");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
