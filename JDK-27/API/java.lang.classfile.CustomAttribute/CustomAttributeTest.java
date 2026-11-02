/*
 * @test
 * @summary java.lang.classfile.CustomAttribute conformance: reflection shape checks.
 * @run main CustomAttributeTest
 */

import java.lang.reflect.*;

public class CustomAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CustomAttribute");
        check(c != null, "java.lang.classfile.CustomAttribute loads");
        check(c.getName().equals("java.lang.classfile.CustomAttribute"), "binary name is java.lang.classfile.CustomAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CustomAttribute is public");
        check(Modifier.isAbstract(c.getModifiers()) && !c.isInterface() && !c.isEnum(), "java.lang.classfile.CustomAttribute is an abstract class");
        check(declares(c, "attributeMapper"), "java.lang.classfile.CustomAttribute declares attributeMapper()");
        check(declares(c, "attributeName"), "java.lang.classfile.CustomAttribute declares attributeName()");
        check(declares(c, "toString"), "java.lang.classfile.CustomAttribute declares toString()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
