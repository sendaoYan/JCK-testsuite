/*
 * @test
 * @summary javax.lang.model.AnnotatedConstruct conformance: reflection shape checks.
 * @run main AnnotatedConstructTest
 */

import java.lang.reflect.*;

public class AnnotatedConstructTest {
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
        Class<?> c = Class.forName("javax.lang.model.AnnotatedConstruct");
        check(c != null, "javax.lang.model.AnnotatedConstruct loads");
        check(c.getName().equals("javax.lang.model.AnnotatedConstruct"), "binary name is javax.lang.model.AnnotatedConstruct");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.AnnotatedConstruct is public");
        check(c.isInterface(), "javax.lang.model.AnnotatedConstruct is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getAnnotationMirrors"), "declares getAnnotationMirrors");
        check(declares(c, "getAnnotation"), "declares getAnnotation");
        check(declares(c, "getAnnotationsByType"), "declares getAnnotationsByType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
