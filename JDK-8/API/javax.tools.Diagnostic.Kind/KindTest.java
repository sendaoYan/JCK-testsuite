/*
 * @test
 * @summary javax.tools.Diagnostic.Kind conformance: reflection shape checks.
 * @run main KindTest
 */

import java.lang.reflect.*;

public class KindTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.tools.Diagnostic$Kind");
        check(c != null, "javax.tools.Diagnostic$Kind loads");
        check(c.getName().equals("javax.tools.Diagnostic$Kind"), "binary name is javax.tools.Diagnostic$Kind");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.Diagnostic$Kind is public");
        check(c.isEnum(), "javax.tools.Diagnostic$Kind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 5, "5 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("ERROR", "WARNING", "MANDATORY_WARNING", "NOTE", "OTHER")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "ERROR") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
