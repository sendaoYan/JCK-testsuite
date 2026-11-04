/*
 * @test
 * @summary javax.lang.model.element.TypeElement conformance: reflection shape checks.
 * @run main TypeElementTest
 */

import java.lang.reflect.*;

public class TypeElementTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.TypeElement");
        check(c != null, "javax.lang.model.element.TypeElement loads");
        check(c.getName().equals("javax.lang.model.element.TypeElement"), "binary name is javax.lang.model.element.TypeElement");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.TypeElement is public");
        check(c.isInterface(), "javax.lang.model.element.TypeElement is an interface");
        check(ifaceNames(c).equals(set("javax.lang.model.element.Element", "javax.lang.model.element.Parameterizable", "javax.lang.model.element.QualifiedNameable")), "direct superinterfaces");
        check(declares(c, "getEnclosedElements"), "declares getEnclosedElements");
        check(declares(c, "getNestingKind"), "declares getNestingKind");
        check(declares(c, "getQualifiedName"), "declares getQualifiedName");
        check(declares(c, "getSimpleName"), "declares getSimpleName");
        check(declares(c, "getSuperclass"), "declares getSuperclass");
        check(declares(c, "getInterfaces"), "declares getInterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
