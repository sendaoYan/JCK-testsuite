/*
 * @test
 * @summary java.lang.classfile.attribute.EnclosingMethodAttribute conformance: reflection shape checks.
 * @run main EnclosingMethodAttributeTest
 */

import java.lang.reflect.*;

public class EnclosingMethodAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.EnclosingMethodAttribute");
        check(c != null, "java.lang.classfile.attribute.EnclosingMethodAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.EnclosingMethodAttribute"), "binary name is java.lang.classfile.attribute.EnclosingMethodAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.EnclosingMethodAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.EnclosingMethodAttribute is an interface");
        check(declares(c, "enclosingClass"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares enclosingClass()");
        check(declares(c, "enclosingMethod"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares enclosingMethod()");
        check(declares(c, "enclosingMethodName"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares enclosingMethodName()");
        check(declares(c, "enclosingMethodType"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares enclosingMethodType()");
        check(declares(c, "enclosingMethodTypeSymbol"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares enclosingMethodTypeSymbol()");
        check(declares(c, "of"), "java.lang.classfile.attribute.EnclosingMethodAttribute declares of()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
