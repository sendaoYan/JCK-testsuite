/*
 * @test
 * @summary jdk.management.jfr.EventTypeInfo conformance: reflection shape checks.
 * @run main EventTypeInfoTest
 */

import java.lang.reflect.*;

public class EventTypeInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(c != null, "jdk.management.jfr.EventTypeInfo loads");
        check(c.getName().equals("jdk.management.jfr.EventTypeInfo"), "binary name is jdk.management.jfr.EventTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.EventTypeInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
