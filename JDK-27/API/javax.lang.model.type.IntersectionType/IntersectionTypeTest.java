/*
 * @test
 * @summary javax.lang.model.type.IntersectionType conformance: reflection shape checks.
 * @run main IntersectionTypeTest
 */

import java.lang.reflect.*;

public class IntersectionTypeTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.IntersectionType");
        check(c != null, "javax.lang.model.type.IntersectionType loads");
        check(c.getName().equals("javax.lang.model.type.IntersectionType"), "binary name is javax.lang.model.type.IntersectionType");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.IntersectionType is public");
        check(c.isInterface(), "javax.lang.model.type.IntersectionType is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("javax.lang.model.type.TypeMirror")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
