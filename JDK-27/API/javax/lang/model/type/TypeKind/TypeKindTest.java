/*
 * @test
 * @summary javax.lang.model.type.TypeKind conformance: white-box structure + black-box behavior.
 * @run main TypeKindTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class TypeKindTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.type.TypeKind");
        check(c != null, "javax.lang.model.type.TypeKind loads");
        check(c.getName().equals("javax.lang.model.type.TypeKind"), "binary name is javax.lang.model.type.TypeKind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.TypeKind is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 22, "22 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("BOOLEAN", "BYTE", "SHORT", "INT", "LONG", "CHAR", "FLOAT", "DOUBLE", "VOID", "NONE", "NULL", "ARRAY", "DECLARED", "ERROR", "TYPEVAR", "WILDCARD", "PACKAGE", "EXECUTABLE", "OTHER", "UNION", "INTERSECTION", "MODULE")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "BOOLEAN") != null, "Enum.valueOf works");
        check(declares(c, "isPrimitive"), "declares isPrimitive()");
    }

    static void testBlackBox() throws Exception {
        TypeKind[] vals = TypeKind.values();
        check(vals.length == 22, "22 enum constants");
        check(TypeKind.valueOf("INT") == TypeKind.INT, "valueOf INT");
        check(TypeKind.valueOf("MODULE") == TypeKind.MODULE, "valueOf MODULE");
        check(TypeKind.INT.ordinal() == 3, "INT ordinal 3");
        check(TypeKind.INT.isPrimitive() && TypeKind.BOOLEAN.isPrimitive() && TypeKind.DOUBLE.isPrimitive(), "primitives isPrimitive");
        check(!TypeKind.VOID.isPrimitive() && !TypeKind.ARRAY.isPrimitive() && !TypeKind.DECLARED.isPrimitive() && !TypeKind.NULL.isPrimitive(), "non-primitives !isPrimitive");
        boolean threw = false;
        try { TypeKind.valueOf("nope"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf unknown throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
