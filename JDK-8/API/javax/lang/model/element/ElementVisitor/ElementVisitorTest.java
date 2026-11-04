/*
 * @test
 * @summary javax.lang.model.element.ElementVisitor conformance: reflection shape checks.
 * @run main ElementVisitorTest
 */

import java.lang.reflect.*;

public class ElementVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ElementVisitor");
        check(c != null, "javax.lang.model.element.ElementVisitor loads");
        check(c.getName().equals("javax.lang.model.element.ElementVisitor"), "binary name is javax.lang.model.element.ElementVisitor");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ElementVisitor is public");
        check(c.isInterface(), "javax.lang.model.element.ElementVisitor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "visit"), "declares visit");
        check(declares(c, "visitPackage"), "declares visitPackage");
        check(declares(c, "visitType"), "declares visitType");
        check(declares(c, "visitVariable"), "declares visitVariable");
        check(declares(c, "visitExecutable"), "declares visitExecutable");
        check(declares(c, "visitTypeParameter"), "declares visitTypeParameter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
