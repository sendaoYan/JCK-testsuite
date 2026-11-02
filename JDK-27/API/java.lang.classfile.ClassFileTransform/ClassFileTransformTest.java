/*
 * @test
 * @summary java.lang.classfile.ClassFileTransform conformance: reflection shape checks.
 * @run main ClassFileTransformTest
 */

import java.lang.reflect.*;

public class ClassFileTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileTransform");
        check(c != null, "java.lang.classfile.ClassFileTransform loads");
        check(c.getName().equals("java.lang.classfile.ClassFileTransform"), "binary name is java.lang.classfile.ClassFileTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileTransform is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileTransform is an interface");
        check(declares(c, "accept"), "java.lang.classfile.ClassFileTransform declares accept()");
        check(declares(c, "andThen"), "java.lang.classfile.ClassFileTransform declares andThen()");
        check(declares(c, "atEnd"), "java.lang.classfile.ClassFileTransform declares atEnd()");
        check(declares(c, "atStart"), "java.lang.classfile.ClassFileTransform declares atStart()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
