/*
 * @test
 * @summary javax.lang.model.element.Name conformance: reflection shape checks.
 * @run main NameTest
 */

import java.lang.reflect.*;

public class NameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.lang.model.element.Name");
        check(c != null, "javax.lang.model.element.Name loads");
        check(c.getName().equals("javax.lang.model.element.Name"), "binary name is javax.lang.model.element.Name");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.Name is public");
        check(c.isInterface(), "javax.lang.model.element.Name is an interface");
        check(ifaceNames(c).equals(set("java.lang.CharSequence")), "direct superinterfaces");
        check(declares(c, "equals"), "declares equals");
        check(declares(c, "hashCode"), "declares hashCode");
        check(declares(c, "contentEquals"), "declares contentEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
