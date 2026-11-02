/*
 * @test
 * @summary javax.tools.StandardLocation conformance: reflection shape checks.
 * @run main StandardLocationTest
 */

import java.lang.reflect.*;

public class StandardLocationTest {
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
        Class<?> c = Class.forName("javax.tools.StandardLocation");
        check(c != null, "javax.tools.StandardLocation loads");
        check(c.getName().equals("javax.tools.StandardLocation"), "binary name is javax.tools.StandardLocation");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.StandardLocation is public");
        check(c.isEnum(), "javax.tools.StandardLocation is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 7, "7 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("CLASS_OUTPUT", "SOURCE_OUTPUT", "CLASS_PATH", "SOURCE_PATH", "ANNOTATION_PROCESSOR_PATH", "PLATFORM_CLASS_PATH", "NATIVE_HEADER_OUTPUT")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "CLASS_OUTPUT") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
