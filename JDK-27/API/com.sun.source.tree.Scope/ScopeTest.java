/*
 * @test
 * @summary com.sun.source.tree.Scope conformance: reflection shape checks.
 * @run main ScopeTest
 */

import java.lang.reflect.*;

public class ScopeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.source.tree.Scope");
        check(c != null, "com.sun.source.tree.Scope loads");
        check(c.getName().equals("com.sun.source.tree.Scope"), "binary name is com.sun.source.tree.Scope");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.Scope is public");
        check(c.isInterface(), "com.sun.source.tree.Scope is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
