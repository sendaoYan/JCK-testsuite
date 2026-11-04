/*
 * @test
 * @summary javax.lang.model.element.Parameterizable conformance: reflection shape checks.
 * @run main ParameterizableTest
 */

import java.lang.reflect.*;

public class ParameterizableTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.Parameterizable");
        check(c != null, "javax.lang.model.element.Parameterizable loads");
        check(c.getName().equals("javax.lang.model.element.Parameterizable"), "binary name is javax.lang.model.element.Parameterizable");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.Parameterizable is public");
        check(c.isInterface(), "javax.lang.model.element.Parameterizable is an interface");
        check(ifaceNames(c).equals(set("javax.lang.model.element.Element")), "direct superinterfaces");
        check(declares(c, "getTypeParameters"), "declares getTypeParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
