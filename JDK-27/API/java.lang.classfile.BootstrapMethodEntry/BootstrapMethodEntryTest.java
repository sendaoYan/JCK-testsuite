/*
 * @test
 * @summary java.lang.classfile.BootstrapMethodEntry conformance: reflection shape checks.
 * @run main BootstrapMethodEntryTest
 */

import java.lang.reflect.*;

public class BootstrapMethodEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.BootstrapMethodEntry");
        check(c != null, "java.lang.classfile.BootstrapMethodEntry loads");
        check(c.getName().equals("java.lang.classfile.BootstrapMethodEntry"), "binary name is java.lang.classfile.BootstrapMethodEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.BootstrapMethodEntry is public");
        check(c.isInterface(), "java.lang.classfile.BootstrapMethodEntry is an interface");
        check(declares(c, "arguments"), "java.lang.classfile.BootstrapMethodEntry declares arguments()");
        check(declares(c, "bootstrapMethod"), "java.lang.classfile.BootstrapMethodEntry declares bootstrapMethod()");
        check(declares(c, "bsmIndex"), "java.lang.classfile.BootstrapMethodEntry declares bsmIndex()");
        check(declares(c, "constantPool"), "java.lang.classfile.BootstrapMethodEntry declares constantPool()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
