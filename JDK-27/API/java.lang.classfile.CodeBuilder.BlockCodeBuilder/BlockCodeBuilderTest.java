/*
 * @test
 * @summary java.lang.classfile.CodeBuilder.BlockCodeBuilder conformance: reflection shape checks.
 * @run main BlockCodeBuilderTest
 */

import java.lang.reflect.*;

public class BlockCodeBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder$BlockCodeBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder.BlockCodeBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder$BlockCodeBuilder"), "binary name is java.lang.classfile.CodeBuilder$BlockCodeBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder.BlockCodeBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder.BlockCodeBuilder is an interface");
        check(declares(c, "breakLabel"), "java.lang.classfile.CodeBuilder.BlockCodeBuilder declares breakLabel()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
