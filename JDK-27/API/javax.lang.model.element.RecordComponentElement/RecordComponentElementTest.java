/*
 * @test
 * @summary javax.lang.model.element.RecordComponentElement conformance: reflection shape checks.
 * @run main RecordComponentElementTest
 */

import java.lang.reflect.*;

public class RecordComponentElementTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.RecordComponentElement");
        check(c != null, "javax.lang.model.element.RecordComponentElement loads");
        check(c.getName().equals("javax.lang.model.element.RecordComponentElement"), "binary name is javax.lang.model.element.RecordComponentElement");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.RecordComponentElement is public");
        check(c.isInterface(), "javax.lang.model.element.RecordComponentElement is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("javax.lang.model.element.Element")), "direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
