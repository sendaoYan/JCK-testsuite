/*
 * @test
 * @summary java.lang.classfile.ClassElement conformance: reflection shape checks.
 * @run main ClassElementTest
 */

import java.lang.reflect.*;

public class ClassElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassElement");
        check(c != null, "java.lang.classfile.ClassElement loads");
        check(c.getName().equals("java.lang.classfile.ClassElement"), "binary name is java.lang.classfile.ClassElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassElement is public");
        check(c.isInterface(), "java.lang.classfile.ClassElement is an interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
