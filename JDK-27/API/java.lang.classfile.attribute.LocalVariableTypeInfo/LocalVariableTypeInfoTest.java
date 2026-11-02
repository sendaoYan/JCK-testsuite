/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableTypeInfo conformance: reflection shape checks.
 * @run main LocalVariableTypeInfoTest
 */

import java.lang.reflect.*;

public class LocalVariableTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(c != null, "java.lang.classfile.attribute.LocalVariableTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableTypeInfo"), "binary name is java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableTypeInfo is an interface");
        check(declares(c, "length"), "java.lang.classfile.attribute.LocalVariableTypeInfo declares length()");
        check(declares(c, "name"), "java.lang.classfile.attribute.LocalVariableTypeInfo declares name()");
        check(declares(c, "signature"), "java.lang.classfile.attribute.LocalVariableTypeInfo declares signature()");
        check(declares(c, "slot"), "java.lang.classfile.attribute.LocalVariableTypeInfo declares slot()");
        check(declares(c, "startPc"), "java.lang.classfile.attribute.LocalVariableTypeInfo declares startPc()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
