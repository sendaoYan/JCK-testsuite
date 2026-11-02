/*
 * @test
 * @summary java.lang.classfile.AttributeMapper.AttributeStability conformance: reflection shape checks.
 * @run main AttributeStabilityTest
 */

import java.lang.reflect.*;

public class AttributeStabilityTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AttributeMapper$AttributeStability");
        check(c != null, "java.lang.classfile.AttributeMapper.AttributeStability loads");
        check(c.getName().equals("java.lang.classfile.AttributeMapper$AttributeStability"), "binary name is java.lang.classfile.AttributeMapper$AttributeStability");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AttributeMapper.AttributeStability is public");
        check(c.isEnum(), "java.lang.classfile.AttributeMapper.AttributeStability is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.AttributeMapper.AttributeStability superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 5, "5 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("STATELESS", "CP_REFS", "LABELS", "UNSTABLE", "UNKNOWN")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.AttributeMapper.AttributeStability declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.AttributeMapper.AttributeStability declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
