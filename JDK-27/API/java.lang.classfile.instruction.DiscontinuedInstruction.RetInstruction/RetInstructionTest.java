/*
 * @test
 * @summary java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction conformance: reflection shape checks.
 * @run main RetInstructionTest
 */

import java.lang.reflect.*;

public class RetInstructionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction");
        check(c != null, "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction"), "binary name is java.lang.classfile.instruction.DiscontinuedInstruction$RetInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction is an interface");
        check(declares(c, "of"), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction declares of()");
        check(declares(c, "slot"), "java.lang.classfile.instruction.DiscontinuedInstruction.RetInstruction declares slot()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
