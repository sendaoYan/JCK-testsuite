/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TargetType conformance: reflection shape checks.
 * @run main TargetTypeTest
 */

import java.lang.reflect.*;

public class TargetTypeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TargetType");
        check(c != null, "java.lang.classfile.TypeAnnotation.TargetType loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TargetType"), "binary name is java.lang.classfile.TypeAnnotation$TargetType");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TargetType is public");
        check(c.isEnum(), "java.lang.classfile.TypeAnnotation.TargetType is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeAnnotation.TargetType superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 22, "22 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("CLASS_TYPE_PARAMETER", "METHOD_TYPE_PARAMETER", "CLASS_EXTENDS", "CLASS_TYPE_PARAMETER_BOUND", "METHOD_TYPE_PARAMETER_BOUND", "FIELD", "METHOD_RETURN", "METHOD_RECEIVER", "METHOD_FORMAL_PARAMETER", "THROWS", "LOCAL_VARIABLE", "RESOURCE_VARIABLE", "EXCEPTION_PARAMETER", "INSTANCEOF", "NEW", "CONSTRUCTOR_REFERENCE", "METHOD_REFERENCE", "CAST", "CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT", "METHOD_INVOCATION_TYPE_ARGUMENT", "CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT", "METHOD_REFERENCE_TYPE_ARGUMENT")), "exact enum constant names");
        check(declares(c, "sizeIfFixed"), "java.lang.classfile.TypeAnnotation.TargetType declares sizeIfFixed()");
        check(declares(c, "targetTypeValue"), "java.lang.classfile.TypeAnnotation.TargetType declares targetTypeValue()");
        check(declares(c, "valueOf"), "java.lang.classfile.TypeAnnotation.TargetType declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.TypeAnnotation.TargetType declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
