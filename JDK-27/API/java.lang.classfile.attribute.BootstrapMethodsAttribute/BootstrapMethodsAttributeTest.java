/*
 * @test
 * @summary java.lang.classfile.attribute.BootstrapMethodsAttribute conformance: reflection shape checks.
 * @run main BootstrapMethodsAttributeTest
 */

import java.lang.reflect.*;

public class BootstrapMethodsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(c != null, "java.lang.classfile.attribute.BootstrapMethodsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.BootstrapMethodsAttribute"), "binary name is java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.BootstrapMethodsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.BootstrapMethodsAttribute is an interface");
        check(declares(c, "bootstrapMethods"), "java.lang.classfile.attribute.BootstrapMethodsAttribute declares bootstrapMethods()");
        check(declares(c, "bootstrapMethodsSize"), "java.lang.classfile.attribute.BootstrapMethodsAttribute declares bootstrapMethodsSize()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
