/*
 * @test
 * @summary com.sun.source.tree.ForLoopTree conformance: reflection shape checks.
 * @run main ForLoopTreeTest
 */

import java.lang.reflect.*;

public class ForLoopTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.tree.ForLoopTree");
        check(c != null, "com.sun.source.tree.ForLoopTree loads");
        check(c.getName().equals("com.sun.source.tree.ForLoopTree"), "binary name is com.sun.source.tree.ForLoopTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.ForLoopTree is public");
        check(c.isInterface(), "com.sun.source.tree.ForLoopTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.tree.StatementTree")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
