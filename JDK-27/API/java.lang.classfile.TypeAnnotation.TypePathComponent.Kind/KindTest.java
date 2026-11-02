/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypePathComponent.Kind conformance: reflection shape checks.
 * @run main KindTest
 */

import java.lang.reflect.*;

public class KindTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypePathComponent$Kind");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypePathComponent$Kind"), "binary name is java.lang.classfile.TypeAnnotation$TypePathComponent$Kind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind is public");
        check(c.isEnum(), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 4, "4 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("ARRAY", "INNER_TYPE", "WILDCARD", "TYPE_ARGUMENT")), "exact enum constant names");
        check(declares(c, "tag"), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind declares tag()");
        check(declares(c, "valueOf"), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
