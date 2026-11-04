/*
 * @test
 * @summary jdk.management.jfr.ConfigurationInfo conformance: reflection shape checks.
 * @run main ConfigurationInfoTest
 */

import java.lang.reflect.*;

public class ConfigurationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(c != null, "jdk.management.jfr.ConfigurationInfo loads");
        check(c.getName().equals("jdk.management.jfr.ConfigurationInfo"), "binary name is jdk.management.jfr.ConfigurationInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.ConfigurationInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
