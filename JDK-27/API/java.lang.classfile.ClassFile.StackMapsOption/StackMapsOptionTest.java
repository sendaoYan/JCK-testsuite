/*
 * @test
 * @summary java.lang.classfile.ClassFile.StackMapsOption conformance: reflection shape checks.
 * @run main StackMapsOptionTest
 */

import java.lang.reflect.*;

public class StackMapsOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$StackMapsOption");
        check(c != null, "java.lang.classfile.ClassFile.StackMapsOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$StackMapsOption"), "binary name is java.lang.classfile.ClassFile$StackMapsOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.StackMapsOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.StackMapsOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.StackMapsOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("STACK_MAPS_WHEN_REQUIRED", "GENERATE_STACK_MAPS", "DROP_STACK_MAPS")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.ClassFile.StackMapsOption declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.ClassFile.StackMapsOption declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
