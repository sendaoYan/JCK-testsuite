/*
 * @test
 * @summary org.w3c.dom.ranges.DocumentRange conformance: reflection shape checks.
 * @run main DocumentRangeTest
 */

import java.lang.reflect.*;

public class DocumentRangeTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ranges.DocumentRange");
        check(c != null, "org.w3c.dom.ranges.DocumentRange loads");
        check(c.getName().equals("org.w3c.dom.ranges.DocumentRange"), "binary name is org.w3c.dom.ranges.DocumentRange");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ranges.DocumentRange is public");
        check(c.isInterface(), "org.w3c.dom.ranges.DocumentRange is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createRange"), "declares createRange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
