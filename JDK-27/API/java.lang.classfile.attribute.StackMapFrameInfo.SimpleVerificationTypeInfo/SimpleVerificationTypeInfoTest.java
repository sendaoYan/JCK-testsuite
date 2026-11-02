/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo conformance: reflection shape checks.
 * @run main SimpleVerificationTypeInfoTest
 */

import java.lang.reflect.*;

public class SimpleVerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo"), "binary name is java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is public");
        check(c.isEnum(), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 7, "7 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("TOP", "INTEGER", "FLOAT", "DOUBLE", "LONG", "NULL", "UNINITIALIZED_THIS")), "exact enum constant names");
        check(declares(c, "tag"), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo declares tag()");
        check(declares(c, "valueOf"), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
