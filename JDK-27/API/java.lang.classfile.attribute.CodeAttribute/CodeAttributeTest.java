/*
 * @test
 * @summary java.lang.classfile.attribute.CodeAttribute conformance: reflection shape checks.
 * @run main CodeAttributeTest
 */

import java.lang.reflect.*;

public class CodeAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.CodeAttribute");
        check(c != null, "java.lang.classfile.attribute.CodeAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.CodeAttribute"), "binary name is java.lang.classfile.attribute.CodeAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.CodeAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.CodeAttribute is an interface");
        check(declares(c, "codeArray"), "java.lang.classfile.attribute.CodeAttribute declares codeArray()");
        check(declares(c, "codeLength"), "java.lang.classfile.attribute.CodeAttribute declares codeLength()");
        check(declares(c, "labelToBci"), "java.lang.classfile.attribute.CodeAttribute declares labelToBci()");
        check(declares(c, "maxLocals"), "java.lang.classfile.attribute.CodeAttribute declares maxLocals()");
        check(declares(c, "maxStack"), "java.lang.classfile.attribute.CodeAttribute declares maxStack()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
