/*
 * @test
 * @summary javax.lang.model.util.Elements.DocCommentKind conformance: reflection shape checks.
 * @run main DocCommentKindTest
 */

import java.lang.reflect.*;

public class DocCommentKindTest {
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
        Class<?> c = Class.forName("javax.lang.model.util.Elements$DocCommentKind");
        check(c != null, "javax.lang.model.util.Elements.DocCommentKind loads");
        check(c.getName().equals("javax.lang.model.util.Elements$DocCommentKind"), "binary name is javax.lang.model.util.Elements$DocCommentKind");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements.DocCommentKind is public");
        check(c.isEnum(), "javax.lang.model.util.Elements.DocCommentKind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 2, "2 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("END_OF_LINE", "TRADITIONAL")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "END_OF_LINE") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
