/*
 * @test
 * @summary javax.tools.DocumentationTool.Location conformance: reflection shape checks.
 * @run main LocationTest
 */

import java.lang.reflect.*;

public class LocationTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$Location");
        check(c != null, "javax.tools.DocumentationTool$Location loads");
        check(c.getName().equals("javax.tools.DocumentationTool$Location"), "binary name is javax.tools.DocumentationTool$Location");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.DocumentationTool$Location is public");
        check(c.isEnum(), "javax.tools.DocumentationTool$Location is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 3, "3 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("DOCUMENTATION_OUTPUT", "DOCLET_PATH", "TAGLET_PATH")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "DOCUMENTATION_OUTPUT") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
