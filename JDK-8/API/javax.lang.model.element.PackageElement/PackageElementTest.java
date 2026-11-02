/*
 * @test
 * @summary javax.lang.model.element.PackageElement conformance: reflection shape checks.
 * @run main PackageElementTest
 */

import java.lang.reflect.*;

public class PackageElementTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.PackageElement");
        check(c != null, "javax.lang.model.element.PackageElement loads");
        check(c.getName().equals("javax.lang.model.element.PackageElement"), "binary name is javax.lang.model.element.PackageElement");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.PackageElement is public");
        check(c.isInterface(), "javax.lang.model.element.PackageElement is an interface");
        check(ifaceNames(c).equals(set("javax.lang.model.element.Element", "javax.lang.model.element.QualifiedNameable")), "direct superinterfaces");
        check(declares(c, "getQualifiedName"), "declares getQualifiedName");
        check(declares(c, "getSimpleName"), "declares getSimpleName");
        check(declares(c, "getEnclosedElements"), "declares getEnclosedElements");
        check(declares(c, "isUnnamed"), "declares isUnnamed");
        check(declares(c, "getEnclosingElement"), "declares getEnclosingElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
