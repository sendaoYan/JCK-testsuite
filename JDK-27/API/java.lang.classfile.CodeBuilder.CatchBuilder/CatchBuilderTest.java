/*
 * @test
 * @summary java.lang.classfile.CodeBuilder.CatchBuilder conformance: reflection shape checks.
 * @run main CatchBuilderTest
 */

import java.lang.reflect.*;

public class CatchBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder$CatchBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder.CatchBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder$CatchBuilder"), "binary name is java.lang.classfile.CodeBuilder$CatchBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder.CatchBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder.CatchBuilder is an interface");
        check(declares(c, "catching"), "java.lang.classfile.CodeBuilder.CatchBuilder declares catching()");
        check(declares(c, "catchingAll"), "java.lang.classfile.CodeBuilder.CatchBuilder declares catchingAll()");
        check(declares(c, "catchingMulti"), "java.lang.classfile.CodeBuilder.CatchBuilder declares catchingMulti()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
