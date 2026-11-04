/*
 * @test
 * @summary javax.lang.model.type.TypeVisitor conformance: reflection shape checks.
 * @run main TypeVisitorTest
 */

import java.lang.reflect.*;

public class TypeVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.TypeVisitor");
        check(c != null, "javax.lang.model.type.TypeVisitor loads");
        check(c.getName().equals("javax.lang.model.type.TypeVisitor"), "binary name is javax.lang.model.type.TypeVisitor");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.TypeVisitor is public");
        check(c.isInterface(), "javax.lang.model.type.TypeVisitor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "visit"), "declares visit");
        check(declares(c, "visitPrimitive"), "declares visitPrimitive");
        check(declares(c, "visitNull"), "declares visitNull");
        check(declares(c, "visitArray"), "declares visitArray");
        check(declares(c, "visitDeclared"), "declares visitDeclared");
        check(declares(c, "visitError"), "declares visitError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
