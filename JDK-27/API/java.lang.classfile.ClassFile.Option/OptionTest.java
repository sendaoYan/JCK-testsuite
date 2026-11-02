/*
 * @test
 * @summary java.lang.classfile.ClassFile.Option conformance: reflection shape checks.
 * @run main OptionTest
 */

import java.lang.reflect.*;

public class OptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$Option");
        check(c != null, "java.lang.classfile.ClassFile.Option loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$Option"), "binary name is java.lang.classfile.ClassFile$Option");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.Option is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile.Option is an interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
