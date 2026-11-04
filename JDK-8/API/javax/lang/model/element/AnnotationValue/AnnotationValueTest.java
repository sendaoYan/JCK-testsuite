/*
 * @test
 * @summary javax.lang.model.element.AnnotationValue conformance: reflection shape checks.
 * @run main AnnotationValueTest
 */

import java.lang.reflect.*;

public class AnnotationValueTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.AnnotationValue");
        check(c != null, "javax.lang.model.element.AnnotationValue loads");
        check(c.getName().equals("javax.lang.model.element.AnnotationValue"), "binary name is javax.lang.model.element.AnnotationValue");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.AnnotationValue is public");
        check(c.isInterface(), "javax.lang.model.element.AnnotationValue is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getValue"), "declares getValue");
        check(declares(c, "toString"), "declares toString");
        check(declares(c, "accept"), "declares accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
