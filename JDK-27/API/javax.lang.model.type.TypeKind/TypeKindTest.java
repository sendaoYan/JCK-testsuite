/*
 * @test
 * @summary javax.lang.model.type.TypeKind conformance: reflection shape checks.
 * @run main TypeKindTest
 */

import java.lang.reflect.*;

public class TypeKindTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.TypeKind");
        check(c != null, "javax.lang.model.type.TypeKind loads");
        check(c.getName().equals("javax.lang.model.type.TypeKind"), "binary name is javax.lang.model.type.TypeKind");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.TypeKind is public");
        check(c.isEnum(), "javax.lang.model.type.TypeKind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 22, "22 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("BOOLEAN", "BYTE", "SHORT", "INT", "LONG", "CHAR", "FLOAT", "DOUBLE", "VOID", "NONE", "NULL", "ARRAY", "DECLARED", "ERROR", "TYPEVAR", "WILDCARD", "PACKAGE", "EXECUTABLE", "OTHER", "UNION", "INTERSECTION", "MODULE")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "BOOLEAN") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
