/*
 * @test
 * @summary javax.lang.model.element.ModuleElement.Directive conformance: reflection shape checks.
 * @run main DirectiveTest
 */

import java.lang.reflect.*;

public class DirectiveTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ModuleElement$Directive");
        check(c != null, "javax.lang.model.element.ModuleElement.Directive loads");
        check(c.getName().equals("javax.lang.model.element.ModuleElement$Directive"), "binary name is javax.lang.model.element.ModuleElement$Directive");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ModuleElement.Directive is public");
        check(c.isInterface(), "javax.lang.model.element.ModuleElement.Directive is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
