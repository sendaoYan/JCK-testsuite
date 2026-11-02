/*
 * @test
 * @summary java.lang.classfile.constantpool.DynamicConstantPoolEntry conformance: reflection shape checks.
 * @run main DynamicConstantPoolEntryTest
 */

import java.lang.reflect.*;

public class DynamicConstantPoolEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(c != null, "java.lang.classfile.constantpool.DynamicConstantPoolEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.DynamicConstantPoolEntry"), "binary name is java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is an interface");
        check(declares(c, "bootstrap"), "java.lang.classfile.constantpool.DynamicConstantPoolEntry declares bootstrap()");
        check(declares(c, "bootstrapMethodIndex"), "java.lang.classfile.constantpool.DynamicConstantPoolEntry declares bootstrapMethodIndex()");
        check(declares(c, "name"), "java.lang.classfile.constantpool.DynamicConstantPoolEntry declares name()");
        check(declares(c, "nameAndType"), "java.lang.classfile.constantpool.DynamicConstantPoolEntry declares nameAndType()");
        check(declares(c, "type"), "java.lang.classfile.constantpool.DynamicConstantPoolEntry declares type()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
