/*
 * @test
 * @summary javax.lang.model.SourceVersion conformance: reflection shape checks.
 * @run main SourceVersionTest
 */

import java.lang.reflect.*;

public class SourceVersionTest {
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
        Class<?> c = Class.forName("javax.lang.model.SourceVersion");
        check(c != null, "javax.lang.model.SourceVersion loads");
        check(c.getName().equals("javax.lang.model.SourceVersion"), "binary name is javax.lang.model.SourceVersion");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.SourceVersion is public");
        check(c.isEnum(), "javax.lang.model.SourceVersion is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 9, "9 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("RELEASE_0", "RELEASE_1", "RELEASE_2", "RELEASE_3", "RELEASE_4", "RELEASE_5", "RELEASE_6", "RELEASE_7", "RELEASE_8")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "RELEASE_0") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
