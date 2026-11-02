/*
 * @test
 * @summary com.sun.source.doctree.DocTree conformance: reflection shape checks.
 * @run main DocTreeTest
 */

import java.lang.reflect.*;

public class DocTreeTest {
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
        Class<?> c = Class.forName("com.sun.source.doctree.DocTree");
        check(c != null, "com.sun.source.doctree.DocTree loads");
        check(c.getName().equals("com.sun.source.doctree.DocTree"), "binary name is com.sun.source.doctree.DocTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.DocTree is public");
        check(c.isInterface(), "com.sun.source.doctree.DocTree is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getKind"), "declares getKind()");
        check(declares(c, "accept"), "declares accept()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
