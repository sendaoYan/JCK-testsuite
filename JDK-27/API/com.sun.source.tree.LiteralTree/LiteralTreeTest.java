/*
 * @test
 * @summary com.sun.source.tree.LiteralTree conformance: reflection shape checks.
 * @run main LiteralTreeTest
 */

import java.lang.reflect.*;

public class LiteralTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.tree.LiteralTree");
        check(c != null, "com.sun.source.tree.LiteralTree loads");
        check(c.getName().equals("com.sun.source.tree.LiteralTree"), "binary name is com.sun.source.tree.LiteralTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.LiteralTree is public");
        check(c.isInterface(), "com.sun.source.tree.LiteralTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.tree.ExpressionTree")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
