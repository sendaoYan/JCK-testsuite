/*
 * @test
 * @summary java.lang.classfile.instruction.ExceptionCatch conformance: reflection shape checks.
 * @run main ExceptionCatchTest
 */

import java.lang.reflect.*;

public class ExceptionCatchTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ExceptionCatch");
        check(c != null, "java.lang.classfile.instruction.ExceptionCatch loads");
        check(c.getName().equals("java.lang.classfile.instruction.ExceptionCatch"), "binary name is java.lang.classfile.instruction.ExceptionCatch");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ExceptionCatch is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ExceptionCatch is an interface");
        check(declares(c, "catchType"), "java.lang.classfile.instruction.ExceptionCatch declares catchType()");
        check(declares(c, "handler"), "java.lang.classfile.instruction.ExceptionCatch declares handler()");
        check(declares(c, "of"), "java.lang.classfile.instruction.ExceptionCatch declares of()");
        check(declares(c, "tryEnd"), "java.lang.classfile.instruction.ExceptionCatch declares tryEnd()");
        check(declares(c, "tryStart"), "java.lang.classfile.instruction.ExceptionCatch declares tryStart()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
