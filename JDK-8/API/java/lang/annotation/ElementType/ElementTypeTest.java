/*
 * @test
 * @summary java.lang.annotation.ElementType conformance: exact 10-constant set (JDK 8, no
 *          MODULE/RECORD_COMPONENT), values/valueOf round-trip, ordinal/name/compareTo.
 * @run main ElementTypeTest
 */

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

public class ElementTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.ElementType");
        check(c.isEnum(), "ElementType is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null, "getEnumConstants non-null");
        check(c.getDeclaredMethod("values") != null, "values() declared");
        check(c.getDeclaredMethod("valueOf", String.class) != null, "valueOf(String) declared");
    }

    // ---- Part 2: behavior ----
    static void testBehavior() {
        // JDK 8 exact constant set (declaration order), 10 constants
        ElementType[] vals = ElementType.values();
        List<String> names = new ArrayList<>();
        for (ElementType v : vals) names.add(v.name());
        check(String.join(",", names).equals(
                "TYPE,FIELD,METHOD,PARAMETER,CONSTRUCTOR,LOCAL_VARIABLE,ANNOTATION_TYPE,PACKAGE,"
                + "TYPE_PARAMETER,TYPE_USE"),
              "constants and declaration order: " + names);
        check(vals.length == 10, "exactly 10 constants (no MODULE/RECORD_COMPONENT in JDK 8)");

        // values() returns a fresh array each call
        vals[0] = null;
        check(ElementType.values()[0] == ElementType.TYPE, "values() defensive copy");

        // valueOf round-trip for every constant
        for (ElementType v : ElementType.values()) {
            check(ElementType.valueOf(v.name()) == v, "valueOf round-trip " + v);
        }

        // valueOf with unknown name throws IllegalArgumentException
        try {
            ElementType.valueOf("NOPE");
            check(false, "valueOf unknown should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown throws IAE");
        }

        // ordinal for every constant equals its declaration index
        for (int i = 0; i < ElementType.values().length; i++) {
            check(ElementType.values()[i].ordinal() == i, "ordinal " + i + " for "
                  + ElementType.values()[i]);
        }
        check(ElementType.TYPE.ordinal() == 0, "TYPE ordinal 0");
        check(ElementType.PACKAGE.ordinal() == 7, "PACKAGE ordinal 7");
        check(ElementType.TYPE_USE.ordinal() == 9, "TYPE_USE ordinal 9");

        // enum mechanics
        check(ElementType.TYPE.compareTo(ElementType.FIELD) < 0, "compareTo by ordinal");
        check(ElementType.TYPE_USE.compareTo(ElementType.TYPE) > 0, "compareTo last");
        check(ElementType.TYPE_USE.name().equals("TYPE_USE"), "name");
        check(ElementType.TYPE_USE.toString().equals("TYPE_USE"), "toString same as name");
        check(ElementType.valueOf("TYPE").equals(ElementType.TYPE), "equals identity");
        check(ElementType.valueOf("TYPE") == ElementType.TYPE, "== identity");
    }

    // ---- Part 3: negative messages and identity mechanics ----
    static void testNegativeAndIdentity() {
        // valueOf with an unknown name: exact message, identical on JDK 8 and JDK 27
        try {
            ElementType.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().equals(
                    "No enum constant java.lang.annotation.ElementType.BOGUS"),
                  "exact IAE message: " + expected.getMessage());
        }
        // empty string is just another unknown name
        try {
            ElementType.valueOf("");
            check(false, "valueOf(\"\") should throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().equals(
                    "No enum constant java.lang.annotation.ElementType."),
                  "empty-name IAE message: " + expected.getMessage());
        }
        // null name is a NullPointerException, not an IAE
        try {
            ElementType.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException expected) {
            check(true, "valueOf(null) throws NPE");
        }

        // enum singleton identity: getEnumConstants shares the same instances as values()
        ElementType[] vals = ElementType.values();
        ElementType[] consts = ElementType.class.getEnumConstants();
        check(vals.length == consts.length, "values() and getEnumConstants() same length");
        boolean allIdentical = true;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != consts[i]) allIdentical = false;
        }
        check(allIdentical, "values() and getEnumConstants() share singleton instances");

        // getDeclaringClass of every constant is the enum class itself
        boolean allDeclaring = true;
        for (ElementType v : vals) {
            if (v.getDeclaringClass() != ElementType.class) allDeclaring = false;
        }
        check(allDeclaring, "getDeclaringClass is ElementType for every constant");

        // names of the JDK 8 additions (JSR 308 type annotations, present since Java 8)
        check(ElementType.TYPE_PARAMETER.name().equals("TYPE_PARAMETER"), "TYPE_PARAMETER name");
        check(ElementType.TYPE_USE.name().equals("TYPE_USE"), "TYPE_USE name");
        // JDK 8 has exactly 10 constants: the 8 pre-Java-8 values plus these two;
        // MODULE and RECORD_COMPONENT do not exist here (they are JDK 9+/14+)
        boolean hasModule = false, hasRecord = false;
        for (ElementType v : vals) {
            if ("MODULE".equals(v.name())) hasModule = true;
            if ("RECORD_COMPONENT".equals(v.name())) hasRecord = true;
        }
        check(!hasModule && !hasRecord, "no MODULE/RECORD_COMPONENT in JDK 8");

        // compareTo is the ordinal difference
        check(ElementType.PACKAGE.compareTo(ElementType.TYPE) == 7, "compareTo exact diff 7");
        check(ElementType.TYPE.compareTo(ElementType.TYPE_USE) == -9, "compareTo exact diff -9");

        // values are usable as @Target members (compile-time domain); runtime readback
        check(ElementType.TYPE_USE.getDeclaringClass().isEnum(), "TYPE_USE declaring enum");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        testNegativeAndIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
