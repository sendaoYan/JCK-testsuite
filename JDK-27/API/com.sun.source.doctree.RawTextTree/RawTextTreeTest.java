/*
 * @test
 * @summary com.sun.source.doctree.RawTextTree conformance: reflection shape checks.
 * @run main RawTextTreeTest
 */

import java.lang.reflect.*;

public class RawTextTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.RawTextTree");
        check(c != null, "com.sun.source.doctree.RawTextTree loads");
        check(c.getName().equals("com.sun.source.doctree.RawTextTree"), "binary name is com.sun.source.doctree.RawTextTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.RawTextTree is public");
        check(c.isInterface(), "com.sun.source.doctree.RawTextTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.DocTree")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
