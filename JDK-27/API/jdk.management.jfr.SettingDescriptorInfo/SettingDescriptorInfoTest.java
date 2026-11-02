/*
 * @test
 * @summary jdk.management.jfr.SettingDescriptorInfo conformance: class.
 * @run main SettingDescriptorInfoTest
 */

import java.lang.reflect.*;


public class SettingDescriptorInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.SettingDescriptorInfo");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getTypeName"), "declares getTypeName");
        check(hasMethod(c, "getContentType"), "declares getContentType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
