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

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
