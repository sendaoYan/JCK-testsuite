/*
 * @test
 * @summary java.lang.classfile.ClassFile.AttributesProcessingOption conformance: reflection shape checks.
 * @run main AttributesProcessingOptionTest
 */

import java.lang.reflect.*;

public class AttributesProcessingOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$AttributesProcessingOption");
        check(c != null, "java.lang.classfile.ClassFile.AttributesProcessingOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$AttributesProcessingOption"), "binary name is java.lang.classfile.ClassFile$AttributesProcessingOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.AttributesProcessingOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.AttributesProcessingOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.AttributesProcessingOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PASS_ALL_ATTRIBUTES", "DROP_UNKNOWN_ATTRIBUTES", "DROP_UNSTABLE_ATTRIBUTES")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.ClassFile.AttributesProcessingOption declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.ClassFile.AttributesProcessingOption declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
