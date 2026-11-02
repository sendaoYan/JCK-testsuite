/*
 * @test
 * @summary javax.lang.model.element.ModuleElement.UsesDirective conformance: reflection shape checks.
 * @run main UsesDirectiveTest
 */

import java.lang.reflect.*;

public class UsesDirectiveTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ModuleElement$UsesDirective");
        check(c != null, "javax.lang.model.element.ModuleElement.UsesDirective loads");
        check(c.getName().equals("javax.lang.model.element.ModuleElement$UsesDirective"), "binary name is javax.lang.model.element.ModuleElement$UsesDirective");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ModuleElement.UsesDirective is public");
        check(c.isInterface(), "javax.lang.model.element.ModuleElement.UsesDirective is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("javax.lang.model.element.ModuleElement$Directive")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
