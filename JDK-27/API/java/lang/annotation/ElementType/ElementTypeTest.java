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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
