/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.OffsetTarget conformance: reflection shape checks.
 * @run main OffsetTargetTest
 */

import java.lang.reflect.*;

public class OffsetTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$OffsetTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.OffsetTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$OffsetTarget"), "binary name is java.lang.classfile.TypeAnnotation$OffsetTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.OffsetTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.OffsetTarget is an interface");
        check(declares(c, "target"), "java.lang.classfile.TypeAnnotation.OffsetTarget declares target()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
