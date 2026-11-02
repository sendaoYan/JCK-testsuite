/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantDynamicEntry conformance: reflection shape checks.
 * @run main ConstantDynamicEntryTest
 */

import java.lang.reflect.*;

public class ConstantDynamicEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.ConstantDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantDynamicEntry"), "binary name is java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantDynamicEntry is an interface");
        check(declares(c, "asSymbol"), "java.lang.classfile.constantpool.ConstantDynamicEntry declares asSymbol()");
        check(declares(c, "constantValue"), "java.lang.classfile.constantpool.ConstantDynamicEntry declares constantValue()");
        check(declares(c, "typeKind"), "java.lang.classfile.constantpool.ConstantDynamicEntry declares typeKind()");
        check(declares(c, "typeSymbol"), "java.lang.classfile.constantpool.ConstantDynamicEntry declares typeSymbol()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
