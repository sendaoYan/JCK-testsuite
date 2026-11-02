/*
 * @test
 * @summary java.lang.classfile.instruction.CharacterRange conformance: reflection shape checks.
 * @run main CharacterRangeTest
 */

import java.lang.reflect.*;

public class CharacterRangeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.CharacterRange");
        check(c != null, "java.lang.classfile.instruction.CharacterRange loads");
        check(c.getName().equals("java.lang.classfile.instruction.CharacterRange"), "binary name is java.lang.classfile.instruction.CharacterRange");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.CharacterRange is public");
        check(c.isInterface(), "java.lang.classfile.instruction.CharacterRange is an interface");
        check(declares(c, "characterRangeEnd"), "java.lang.classfile.instruction.CharacterRange declares characterRangeEnd()");
        check(declares(c, "characterRangeStart"), "java.lang.classfile.instruction.CharacterRange declares characterRangeStart()");
        check(declares(c, "endScope"), "java.lang.classfile.instruction.CharacterRange declares endScope()");
        check(declares(c, "flags"), "java.lang.classfile.instruction.CharacterRange declares flags()");
        check(declares(c, "of"), "java.lang.classfile.instruction.CharacterRange declares of()");
        check(declares(c, "startScope"), "java.lang.classfile.instruction.CharacterRange declares startScope()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
