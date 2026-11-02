/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.LocalVarTargetInfo conformance: reflection shape checks.
 * @run main LocalVarTargetInfoTest
 */

import java.lang.reflect.*;

public class LocalVarTargetInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTargetInfo");
        check(c != null, "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$LocalVarTargetInfo"), "binary name is java.lang.classfile.TypeAnnotation$LocalVarTargetInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo is an interface");
        check(declares(c, "endLabel"), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo declares endLabel()");
        check(declares(c, "index"), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo declares index()");
        check(declares(c, "of"), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo declares of()");
        check(declares(c, "startLabel"), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo declares startLabel()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
