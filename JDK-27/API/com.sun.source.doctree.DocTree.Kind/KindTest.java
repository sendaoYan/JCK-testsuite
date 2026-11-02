/*
 * @test
 * @summary com.sun.source.doctree.DocTree.Kind conformance: reflection shape checks.
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
        Class<?> c = Class.forName("com.sun.source.doctree.DocTree$Kind");
        check(c != null, "com.sun.source.doctree.DocTree.Kind loads");
        check(c.getName().equals("com.sun.source.doctree.DocTree$Kind"), "binary name is com.sun.source.doctree.DocTree$Kind");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.doctree.DocTree.Kind is public");
        check(c.isEnum(), "com.sun.source.doctree.DocTree.Kind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 43, "43 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("ATTRIBUTE", "AUTHOR", "CODE", "COMMENT", "DEPRECATED", "DOC_COMMENT", "DOC_ROOT", "DOC_TYPE", "END_ELEMENT", "ENTITY", "ERRONEOUS", "ESCAPE", "EXCEPTION", "HIDDEN", "IDENTIFIER", "INDEX", "INHERIT_DOC", "LINK", "LINK_PLAIN", "LITERAL", "MARKDOWN", "PARAM", "PROVIDES", "REFERENCE", "RETURN", "SEE", "SERIAL", "SERIAL_DATA", "SERIAL_FIELD", "SINCE", "SNIPPET", "SPEC", "START_ELEMENT", "SYSTEM_PROPERTY", "SUMMARY", "TEXT", "THROWS", "UNKNOWN_BLOCK_TAG", "UNKNOWN_INLINE_TAG", "USES", "VALUE", "VERSION", "OTHER")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "ATTRIBUTE") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
