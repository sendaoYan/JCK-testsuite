/*
 * @test
 * @summary com.sun.source.util.DocTrees conformance: reflection shape checks.
 * @run main DocTreesTest
 */

import java.lang.reflect.*;

public class DocTreesTest {
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
        Class<?> c = Class.forName("com.sun.source.util.DocTrees");
        check(c != null, "com.sun.source.util.DocTrees loads");
        check(c.getName().equals("com.sun.source.util.DocTrees"), "binary name is com.sun.source.util.DocTrees");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.DocTrees is public");
        check(!c.isInterface(), "com.sun.source.util.DocTrees is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "com.sun.source.util.DocTrees is abstract");
        check(c.getSuperclass().getName().equals("com.sun.source.util.Trees"), "superclass is com.sun.source.util.Trees");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
