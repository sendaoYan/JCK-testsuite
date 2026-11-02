/*
 * @test
 * @summary javax.lang.model.element.ModuleElement.DirectiveVisitor conformance: reflection shape checks.
 * @run main DirectiveVisitorTest
 */

import java.lang.reflect.*;

public class DirectiveVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ModuleElement$DirectiveVisitor");
        check(c != null, "javax.lang.model.element.ModuleElement.DirectiveVisitor loads");
        check(c.getName().equals("javax.lang.model.element.ModuleElement$DirectiveVisitor"), "binary name is javax.lang.model.element.ModuleElement$DirectiveVisitor");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ModuleElement.DirectiveVisitor is public");
        check(c.isInterface(), "javax.lang.model.element.ModuleElement.DirectiveVisitor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
