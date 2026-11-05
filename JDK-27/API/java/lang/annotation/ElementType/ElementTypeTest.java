/*
 * @test
 * @summary java.lang.annotation.ElementType conformance: white-box structure + black-box behavior,
 *          exact 12-constant set (JDK 27 with MODULE/RECORD_COMPONENT).
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.ElementType");
        check(c.isEnum(), "ElementType is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getDeclaredFields().length >= 13, "constants declared as fields");
        check(c.getDeclaredMethods().length >= 2, "values/valueOf declared");
        check(c.getEnumConstants() != null, "getEnumConstants non-null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exact constant set (order per declaration)
        ElementType[] vals = ElementType.values();
        List<String> names = new ArrayList<>();
        for (ElementType v : vals) names.add(v.name());
        check(String.join(",", names).equals(
                "TYPE,FIELD,METHOD,PARAMETER,CONSTRUCTOR,LOCAL_VARIABLE,ANNOTATION_TYPE,PACKAGE,"
                + "TYPE_PARAMETER,TYPE_USE,MODULE,RECORD_COMPONENT"),
              "constants and declaration order: " + names);
        check(vals.length == 12, "exactly 12 constants");

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
        check(ElementType.MODULE.ordinal() == 10, "MODULE ordinal 10");
        check(ElementType.RECORD_COMPONENT.ordinal() == 11, "RECORD_COMPONENT ordinal 11");

        // enum mechanics: compareTo/name/toString are usable and consistent
        check(ElementType.TYPE.compareTo(ElementType.FIELD) < 0, "compareTo by ordinal");
        check(ElementType.RECORD_COMPONENT.compareTo(ElementType.TYPE) > 0, "compareTo last");
        check(ElementType.TYPE_USE.name().equals("TYPE_USE"), "name");
        check(ElementType.TYPE_USE.toString().equals("TYPE_USE"), "toString same as name");

        // equals is identity for enum constants
        check(ElementType.valueOf("TYPE").equals(ElementType.TYPE), "equals identity");
        check(ElementType.valueOf("TYPE") == ElementType.TYPE, "== identity");

        // values are usable inside @Target arrays (compile-time domain); spot-check runtime
        // membership semantics used by frameworks
        boolean hasModule = false, hasRecord = false;
        for (ElementType v : ElementType.values()) {
            if (v == ElementType.MODULE) hasModule = true;
            if (v == ElementType.RECORD_COMPONENT) hasRecord = true;
        }
        check(hasModule && hasRecord, "MODULE and RECORD_COMPONENT present");

        // switch covers the JDK 27 superset
        for (ElementType v : ElementType.values()) {
            String bucket;
            switch (v) {
                case TYPE:            bucket = "t"; break;
                case FIELD:           bucket = "f"; break;
                case METHOD:          bucket = "m"; break;
                case PARAMETER:       bucket = "p"; break;
                case CONSTRUCTOR:     bucket = "c"; break;
                case LOCAL_VARIABLE:  bucket = "l"; break;
                case ANNOTATION_TYPE: bucket = "a"; break;
                case PACKAGE:         bucket = "pk"; break;
                case TYPE_PARAMETER:  bucket = "tp"; break;
                case TYPE_USE:        bucket = "tu"; break;
                case MODULE:          bucket = "mo"; break;
                case RECORD_COMPONENT: bucket = "rc"; break;
                default: throw new AssertionError("unexpected " + v);
            }
            check(bucket != null, "switch covers " + v);
        }
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

        // JDK 27 additions: MODULE (JDK 9) and RECORD_COMPONENT (JDK 16) are the exact
        // two constants beyond the JDK 8 set of 10
        check(ElementType.MODULE.name().equals("MODULE"), "MODULE name");
        check(ElementType.RECORD_COMPONENT.name().equals("RECORD_COMPONENT"), "RECORD_COMPONENT name");
        check(ElementType.values().length == 12, "12 = 10 (JDK 8) + MODULE + RECORD_COMPONENT");

        // compareTo is the ordinal difference
        check(ElementType.MODULE.compareTo(ElementType.TYPE) == 10, "compareTo exact diff 10");
        check(ElementType.RECORD_COMPONENT.compareTo(ElementType.TYPE_USE) == 2,
              "compareTo exact diff 2");

        // values are usable as @Target members (compile-time domain); runtime readback
        check(ElementType.RECORD_COMPONENT.getDeclaringClass().isEnum(),
              "RECORD_COMPONENT declaring enum");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNegativeAndIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
