/*
 * @test
 * @summary com.sun.source.util.DocTreeScanner conformance: reflection shape checks.
 * @run main DocTreeScannerTest
 */

import java.lang.reflect.*;

public class DocTreeScannerTest {
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
        Class<?> c = Class.forName("com.sun.source.util.DocTreeScanner");
        check(c != null, "com.sun.source.util.DocTreeScanner loads");
        check(c.getName().equals("com.sun.source.util.DocTreeScanner"), "binary name is com.sun.source.util.DocTreeScanner");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.DocTreeScanner is public");
        check(!c.isInterface(), "com.sun.source.util.DocTreeScanner is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "com.sun.source.util.DocTreeScanner is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.doctree.DocTreeVisitor")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
