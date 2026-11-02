/*
 * @test
 * @summary java.lang.classfile.PseudoInstruction conformance: reflection shape checks.
 * @run main PseudoInstructionTest
 */

import java.lang.reflect.*;

public class PseudoInstructionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.PseudoInstruction");
        check(c != null, "java.lang.classfile.PseudoInstruction loads");
        check(c.getName().equals("java.lang.classfile.PseudoInstruction"), "binary name is java.lang.classfile.PseudoInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.PseudoInstruction is public");
        check(c.isInterface(), "java.lang.classfile.PseudoInstruction is an interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
