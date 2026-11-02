/*
 * @test
 * @summary com.sun.source.util.TreePath conformance: reflection shape checks.
 * @run main TreePathTest
 */

import java.lang.reflect.*;

public class TreePathTest {
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
        Class<?> c = Class.forName("com.sun.source.util.TreePath");
        check(c != null, "com.sun.source.util.TreePath loads");
        check(c.getName().equals("com.sun.source.util.TreePath"), "binary name is com.sun.source.util.TreePath");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.TreePath is public");
        check(!c.isInterface(), "com.sun.source.util.TreePath is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "com.sun.source.util.TreePath is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(ifaceNames(c).equals(java.util.Set.of("java.lang.Iterable")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
