/*
 * @test
 * @summary com.sun.source.tree.NewClassTree conformance: reflection shape checks.
 * @run main NewClassTreeTest
 */

import java.lang.reflect.*;

public class NewClassTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.tree.NewClassTree");
        check(c != null, "com.sun.source.tree.NewClassTree loads");
        check(c.getName().equals("com.sun.source.tree.NewClassTree"), "binary name is com.sun.source.tree.NewClassTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.NewClassTree is public");
        check(c.isInterface(), "com.sun.source.tree.NewClassTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.tree.ExpressionTree")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
