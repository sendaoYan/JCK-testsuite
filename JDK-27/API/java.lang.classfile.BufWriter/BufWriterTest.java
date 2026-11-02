/*
 * @test
 * @summary java.lang.classfile.BufWriter conformance: reflection shape checks.
 * @run main BufWriterTest
 */

import java.lang.reflect.*;

public class BufWriterTest {
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
        Class<?> c = Class.forName("java.lang.classfile.BufWriter");
        check(c != null, "java.lang.classfile.BufWriter loads");
        check(c.getName().equals("java.lang.classfile.BufWriter"), "binary name is java.lang.classfile.BufWriter");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.BufWriter is public");
        check(c.isInterface(), "java.lang.classfile.BufWriter is an interface");
        check(declares(c, "canWriteDirect"), "java.lang.classfile.BufWriter declares canWriteDirect()");
        check(declares(c, "constantPool"), "java.lang.classfile.BufWriter declares constantPool()");
        check(declares(c, "patchInt"), "java.lang.classfile.BufWriter declares patchInt()");
        check(declares(c, "reserveSpace"), "java.lang.classfile.BufWriter declares reserveSpace()");
        check(declares(c, "size"), "java.lang.classfile.BufWriter declares size()");
        check(declares(c, "writeBytes"), "java.lang.classfile.BufWriter declares writeBytes()");
        check(declares(c, "writeDouble"), "java.lang.classfile.BufWriter declares writeDouble()");
        check(declares(c, "writeFloat"), "java.lang.classfile.BufWriter declares writeFloat()");
        check(declares(c, "writeIndex"), "java.lang.classfile.BufWriter declares writeIndex()");
        check(declares(c, "writeIndexOrZero"), "java.lang.classfile.BufWriter declares writeIndexOrZero()");
        check(declares(c, "writeInt"), "java.lang.classfile.BufWriter declares writeInt()");
        check(declares(c, "writeIntBytes"), "java.lang.classfile.BufWriter declares writeIntBytes()");
        check(declares(c, "writeLong"), "java.lang.classfile.BufWriter declares writeLong()");
        check(declares(c, "writeU1"), "java.lang.classfile.BufWriter declares writeU1()");
        check(declares(c, "writeU2"), "java.lang.classfile.BufWriter declares writeU2()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
