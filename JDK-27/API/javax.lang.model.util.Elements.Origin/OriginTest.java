/*
 * @test
 * @summary javax.lang.model.util.Elements.Origin conformance: reflection shape checks.
 * @run main OriginTest
 */

import java.lang.reflect.*;

public class OriginTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.Elements$Origin");
        check(c != null, "javax.lang.model.util.Elements.Origin loads");
        check(c.getName().equals("javax.lang.model.util.Elements$Origin"), "binary name is javax.lang.model.util.Elements$Origin");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements.Origin is public");
        check(c.isEnum(), "javax.lang.model.util.Elements.Origin is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 3, "3 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("EXPLICIT", "MANDATED", "SYNTHETIC")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "EXPLICIT") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
