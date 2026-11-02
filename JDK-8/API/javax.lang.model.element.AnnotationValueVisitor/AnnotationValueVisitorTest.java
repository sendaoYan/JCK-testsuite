/*
 * @test
 * @summary javax.lang.model.element.AnnotationValueVisitor conformance: reflection shape checks.
 * @run main AnnotationValueVisitorTest
 */

import java.lang.reflect.*;

public class AnnotationValueVisitorTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.AnnotationValueVisitor");
        check(c != null, "javax.lang.model.element.AnnotationValueVisitor loads");
        check(c.getName().equals("javax.lang.model.element.AnnotationValueVisitor"), "binary name is javax.lang.model.element.AnnotationValueVisitor");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.AnnotationValueVisitor is public");
        check(c.isInterface(), "javax.lang.model.element.AnnotationValueVisitor is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "visit"), "declares visit");
        check(declares(c, "visitBoolean"), "declares visitBoolean");
        check(declares(c, "visitByte"), "declares visitByte");
        check(declares(c, "visitChar"), "declares visitChar");
        check(declares(c, "visitDouble"), "declares visitDouble");
        check(declares(c, "visitFloat"), "declares visitFloat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
