/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypeArgumentTarget conformance: reflection shape checks.
 * @run main TypeArgumentTargetTest
 */

import java.lang.reflect.*;

public class TypeArgumentTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypeArgumentTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypeArgumentTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypeArgumentTarget"), "binary name is java.lang.classfile.TypeAnnotation$TypeArgumentTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget is an interface");
        check(declares(c, "target"), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget declares target()");
        check(declares(c, "typeArgumentIndex"), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget declares typeArgumentIndex()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
