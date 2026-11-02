/*
 * @test
 * @summary javax.lang.model.util.SimpleTypeVisitor6 conformance: reflection shape checks.
 * @run main SimpleTypeVisitor6Test
 */

import java.lang.reflect.*;

public class SimpleTypeVisitor6Test {
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
        Class<?> c = Class.forName("javax.lang.model.util.SimpleTypeVisitor6");
        check(c != null, "javax.lang.model.util.SimpleTypeVisitor6 loads");
        check(c.getName().equals("javax.lang.model.util.SimpleTypeVisitor6"), "binary name is javax.lang.model.util.SimpleTypeVisitor6");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.SimpleTypeVisitor6 is public");
        check(!c.isInterface(), "javax.lang.model.util.SimpleTypeVisitor6 is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "javax.lang.model.util.SimpleTypeVisitor6 is concrete");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractTypeVisitor6"), "superclass is javax.lang.model.util.AbstractTypeVisitor6");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
