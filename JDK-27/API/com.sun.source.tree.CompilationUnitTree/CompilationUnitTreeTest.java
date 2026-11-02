/*
 * @test
 * @summary com.sun.source.tree.CompilationUnitTree conformance: reflection shape checks.
 * @run main CompilationUnitTreeTest
 */

import java.lang.reflect.*;

public class CompilationUnitTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.tree.CompilationUnitTree");
        check(c != null, "com.sun.source.tree.CompilationUnitTree loads");
        check(c.getName().equals("com.sun.source.tree.CompilationUnitTree"), "binary name is com.sun.source.tree.CompilationUnitTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.CompilationUnitTree is public");
        check(c.isInterface(), "com.sun.source.tree.CompilationUnitTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.tree.Tree")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
