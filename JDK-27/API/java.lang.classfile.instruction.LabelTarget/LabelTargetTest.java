/*
 * @test
 * @summary java.lang.classfile.instruction.LabelTarget conformance: reflection shape checks.
 * @run main LabelTargetTest
 */

import java.lang.reflect.*;

public class LabelTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LabelTarget");
        check(c != null, "java.lang.classfile.instruction.LabelTarget loads");
        check(c.getName().equals("java.lang.classfile.instruction.LabelTarget"), "binary name is java.lang.classfile.instruction.LabelTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LabelTarget is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LabelTarget is an interface");
        check(declares(c, "label"), "java.lang.classfile.instruction.LabelTarget declares label()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
