/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableInfo conformance: reflection shape checks.
 * @run main LocalVariableInfoTest
 */

import java.lang.reflect.*;

public class LocalVariableInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableInfo");
        check(c != null, "java.lang.classfile.attribute.LocalVariableInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableInfo"), "binary name is java.lang.classfile.attribute.LocalVariableInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableInfo is an interface");
        check(declares(c, "length"), "java.lang.classfile.attribute.LocalVariableInfo declares length()");
        check(declares(c, "name"), "java.lang.classfile.attribute.LocalVariableInfo declares name()");
        check(declares(c, "slot"), "java.lang.classfile.attribute.LocalVariableInfo declares slot()");
        check(declares(c, "startPc"), "java.lang.classfile.attribute.LocalVariableInfo declares startPc()");
        check(declares(c, "type"), "java.lang.classfile.attribute.LocalVariableInfo declares type()");
        check(declares(c, "typeSymbol"), "java.lang.classfile.attribute.LocalVariableInfo declares typeSymbol()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
