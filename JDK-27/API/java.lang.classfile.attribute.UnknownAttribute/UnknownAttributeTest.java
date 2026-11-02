/*
 * @test
 * @summary java.lang.classfile.attribute.UnknownAttribute conformance: reflection shape checks.
 * @run main UnknownAttributeTest
 */

import java.lang.reflect.*;

public class UnknownAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.UnknownAttribute");
        check(c != null, "java.lang.classfile.attribute.UnknownAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.UnknownAttribute"), "binary name is java.lang.classfile.attribute.UnknownAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.UnknownAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.UnknownAttribute is an interface");
        check(declares(c, "contents"), "java.lang.classfile.attribute.UnknownAttribute declares contents()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
