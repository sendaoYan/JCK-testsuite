/*
 * @test
 * @summary com.sun.source.tree.ModuleTree.ModuleKind conformance: reflection shape checks.
 * @run main ModuleKindTest
 */

import java.lang.reflect.*;

public class ModuleKindTest {
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
        Class<?> c = Class.forName("com.sun.source.tree.ModuleTree$ModuleKind");
        check(c != null, "com.sun.source.tree.ModuleTree.ModuleKind loads");
        check(c.getName().equals("com.sun.source.tree.ModuleTree$ModuleKind"), "binary name is com.sun.source.tree.ModuleTree$ModuleKind");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.ModuleTree.ModuleKind is public");
        check(c.isEnum(), "com.sun.source.tree.ModuleTree.ModuleKind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 2, "2 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("OPEN", "STRONG")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "OPEN") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
