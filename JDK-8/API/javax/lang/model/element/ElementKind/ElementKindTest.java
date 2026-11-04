/*
 * @test
 * @summary javax.lang.model.element.ElementKind conformance: reflection shape checks.
 * @run main ElementKindTest
 */

import java.lang.reflect.*;

public class ElementKindTest {
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
        Class<?> c = Class.forName("javax.lang.model.element.ElementKind");
        check(c != null, "javax.lang.model.element.ElementKind loads");
        check(c.getName().equals("javax.lang.model.element.ElementKind"), "binary name is javax.lang.model.element.ElementKind");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ElementKind is public");
        check(c.isEnum(), "javax.lang.model.element.ElementKind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 17, "17 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("PACKAGE", "ENUM", "CLASS", "ANNOTATION_TYPE", "INTERFACE", "ENUM_CONSTANT", "FIELD", "PARAMETER", "LOCAL_VARIABLE", "EXCEPTION_PARAMETER", "METHOD", "CONSTRUCTOR", "STATIC_INIT", "INSTANCE_INIT", "TYPE_PARAMETER", "OTHER", "RESOURCE_VARIABLE")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "PACKAGE") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
