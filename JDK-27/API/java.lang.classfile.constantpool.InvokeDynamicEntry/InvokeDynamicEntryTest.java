/*
 * @test
 * @summary java.lang.classfile.constantpool.InvokeDynamicEntry conformance: reflection shape checks.
 * @run main InvokeDynamicEntryTest
 */

import java.lang.reflect.*;

public class InvokeDynamicEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.InvokeDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.InvokeDynamicEntry"), "binary name is java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.InvokeDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.InvokeDynamicEntry is an interface");
        check(declares(c, "asSymbol"), "java.lang.classfile.constantpool.InvokeDynamicEntry declares asSymbol()");
        check(declares(c, "typeSymbol"), "java.lang.classfile.constantpool.InvokeDynamicEntry declares typeSymbol()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
