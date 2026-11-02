/*
 * @test
 * @summary java.lang.classfile.AttributedElement conformance: reflection shape checks.
 * @run main AttributedElementTest
 */

import java.lang.reflect.*;

public class AttributedElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AttributedElement");
        check(c != null, "java.lang.classfile.AttributedElement loads");
        check(c.getName().equals("java.lang.classfile.AttributedElement"), "binary name is java.lang.classfile.AttributedElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AttributedElement is public");
        check(c.isInterface(), "java.lang.classfile.AttributedElement is an interface");
        check(declares(c, "attributes"), "java.lang.classfile.AttributedElement declares attributes()");
        check(declares(c, "findAttribute"), "java.lang.classfile.AttributedElement declares findAttribute()");
        check(declares(c, "findAttributes"), "java.lang.classfile.AttributedElement declares findAttributes()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
