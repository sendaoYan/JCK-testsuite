/*
 * @test
 * @summary java.lang.classfile.instruction.LocalVariableType conformance: reflection shape checks.
 * @run main LocalVariableTypeTest
 */

import java.lang.reflect.*;

public class LocalVariableTypeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LocalVariableType");
        check(c != null, "java.lang.classfile.instruction.LocalVariableType loads");
        check(c.getName().equals("java.lang.classfile.instruction.LocalVariableType"), "binary name is java.lang.classfile.instruction.LocalVariableType");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LocalVariableType is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LocalVariableType is an interface");
        check(declares(c, "endScope"), "java.lang.classfile.instruction.LocalVariableType declares endScope()");
        check(declares(c, "name"), "java.lang.classfile.instruction.LocalVariableType declares name()");
        check(declares(c, "of"), "java.lang.classfile.instruction.LocalVariableType declares of()");
        check(declares(c, "signature"), "java.lang.classfile.instruction.LocalVariableType declares signature()");
        check(declares(c, "signatureSymbol"), "java.lang.classfile.instruction.LocalVariableType declares signatureSymbol()");
        check(declares(c, "slot"), "java.lang.classfile.instruction.LocalVariableType declares slot()");
        check(declares(c, "startScope"), "java.lang.classfile.instruction.LocalVariableType declares startScope()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
