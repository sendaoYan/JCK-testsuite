/*
 * @test
 * @summary java.lang.classfile.ClassFileBuilder conformance: reflection shape checks.
 * @run main ClassFileBuilderTest
 */

import java.lang.reflect.*;

public class ClassFileBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileBuilder");
        check(c != null, "java.lang.classfile.ClassFileBuilder loads");
        check(c.getName().equals("java.lang.classfile.ClassFileBuilder"), "binary name is java.lang.classfile.ClassFileBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileBuilder is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileBuilder is an interface");
        check(declares(c, "accept"), "java.lang.classfile.ClassFileBuilder declares accept()");
        check(declares(c, "constantPool"), "java.lang.classfile.ClassFileBuilder declares constantPool()");
        check(declares(c, "transform"), "java.lang.classfile.ClassFileBuilder declares transform()");
        check(declares(c, "with"), "java.lang.classfile.ClassFileBuilder declares with()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
