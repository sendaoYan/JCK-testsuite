/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator conformance: reflection shape checks.
 * @run main WildcardIndicatorTest
 */

import java.lang.reflect.*;

public class WildcardIndicatorTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator");
        check(c != null, "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator"), "binary name is java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator is public");
        check(c.isEnum(), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("NONE", "EXTENDS", "SUPER")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
