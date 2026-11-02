/*
 * @test
 * @summary java.lang.classfile.Opcode.Kind conformance: reflection shape checks.
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
        Class<?> c = Class.forName("java.lang.classfile.Opcode$Kind");
        check(c != null, "java.lang.classfile.Opcode.Kind loads");
        check(c.getName().equals("java.lang.classfile.Opcode$Kind"), "binary name is java.lang.classfile.Opcode$Kind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Opcode.Kind is public");
        check(c.isEnum(), "java.lang.classfile.Opcode.Kind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.Opcode.Kind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 26, "26 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("LOAD", "STORE", "INCREMENT", "BRANCH", "LOOKUP_SWITCH", "TABLE_SWITCH", "RETURN", "THROW_EXCEPTION", "FIELD_ACCESS", "INVOKE", "INVOKE_DYNAMIC", "NEW_OBJECT", "NEW_PRIMITIVE_ARRAY", "NEW_REF_ARRAY", "NEW_MULTI_ARRAY", "TYPE_CHECK", "ARRAY_LOAD", "ARRAY_STORE", "STACK", "CONVERT", "OPERATOR", "CONSTANT", "MONITOR", "NOP", "DISCONTINUED_JSR", "DISCONTINUED_RET")), "exact enum constant names");
        check(declares(c, "valueOf"), "java.lang.classfile.Opcode.Kind declares valueOf()");
        check(declares(c, "values"), "java.lang.classfile.Opcode.Kind declares values()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
