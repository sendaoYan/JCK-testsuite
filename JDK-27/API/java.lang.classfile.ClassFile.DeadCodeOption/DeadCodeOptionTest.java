/*
 * @test
 * @summary java.lang.classfile.ClassFile.DeadCodeOption conformance: reflection shape checks.
 * @run main DeadCodeOptionTest
 */

import java.lang.reflect.*;

public class DeadCodeOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$DeadCodeOption");
        check(c != null, "java.lang.classfile.ClassFile.DeadCodeOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$DeadCodeOption"), "binary name is java.lang.classfile.ClassFile$DeadCodeOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.DeadCodeOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.DeadCodeOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.DeadCodeOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PATCH_DEAD_CODE", "KEEP_DEAD_CODE")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.ClassFile.DeadCodeOption declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.ClassFile.DeadCodeOption declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
