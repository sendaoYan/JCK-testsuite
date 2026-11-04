/*
 * @test
 * @summary javax.lang.model.util.AbstractElementVisitor8 conformance: reflection shape checks.
 * @run main AbstractElementVisitor8Test
 */

import java.lang.reflect.*;

public class AbstractElementVisitor8Test {
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
        Class<?> c = Class.forName("javax.lang.model.util.AbstractElementVisitor8");
        check(c != null, "javax.lang.model.util.AbstractElementVisitor8 loads");
        check(c.getName().equals("javax.lang.model.util.AbstractElementVisitor8"), "binary name is javax.lang.model.util.AbstractElementVisitor8");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.AbstractElementVisitor8 is public");
        check(!c.isInterface(), "javax.lang.model.util.AbstractElementVisitor8 is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "javax.lang.model.util.AbstractElementVisitor8 is abstract");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractElementVisitor7"), "superclass is javax.lang.model.util.AbstractElementVisitor7");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
