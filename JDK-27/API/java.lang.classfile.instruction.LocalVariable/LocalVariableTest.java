/*
 * @test
 * @summary java.lang.classfile.instruction.LocalVariable conformance: reflection shape checks.
 * @run main LocalVariableTest
 */

import java.lang.reflect.*;

public class LocalVariableTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LocalVariable");
        check(c != null, "java.lang.classfile.instruction.LocalVariable loads");
        check(c.getName().equals("java.lang.classfile.instruction.LocalVariable"), "binary name is java.lang.classfile.instruction.LocalVariable");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LocalVariable is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LocalVariable is an interface");
        check(declares(c, "endScope"), "java.lang.classfile.instruction.LocalVariable declares endScope()");
        check(declares(c, "name"), "java.lang.classfile.instruction.LocalVariable declares name()");
        check(declares(c, "of"), "java.lang.classfile.instruction.LocalVariable declares of()");
        check(declares(c, "slot"), "java.lang.classfile.instruction.LocalVariable declares slot()");
        check(declares(c, "startScope"), "java.lang.classfile.instruction.LocalVariable declares startScope()");
        check(declares(c, "type"), "java.lang.classfile.instruction.LocalVariable declares type()");
        check(declares(c, "typeSymbol"), "java.lang.classfile.instruction.LocalVariable declares typeSymbol()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
