/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.LocalVarTarget conformance: reflection shape checks.
 * @run main LocalVarTargetTest
 */

import java.lang.reflect.*;

public class LocalVarTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.LocalVarTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$LocalVarTarget"), "binary name is java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.LocalVarTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.LocalVarTarget is an interface");
        check(declares(c, "table"), "java.lang.classfile.TypeAnnotation.LocalVarTarget declares table()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
