/*
 * @test
 * @summary com.sun.source.util.TaskEvent.Kind conformance: reflection shape checks.
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
        Class<?> c = Class.forName("com.sun.source.util.TaskEvent$Kind");
        check(c != null, "com.sun.source.util.TaskEvent.Kind loads");
        check(c.getName().equals("com.sun.source.util.TaskEvent$Kind"), "binary name is com.sun.source.util.TaskEvent$Kind");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.util.TaskEvent.Kind is public");
        check(c.isEnum(), "com.sun.source.util.TaskEvent.Kind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 7, "7 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("PARSE", "ENTER", "ANALYZE", "GENERATE", "ANNOTATION_PROCESSING", "ANNOTATION_PROCESSING_ROUND", "COMPILATION")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "PARSE") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
