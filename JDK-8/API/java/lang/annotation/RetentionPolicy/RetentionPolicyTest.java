/*
 * @test
 * @summary java.lang.annotation.RetentionPolicy conformance: exact 3-constant set
 *          (SOURCE/CLASS/RUNTIME), values/valueOf round-trip, ordinal/name/compareTo.
 * @run main RetentionPolicyTest
 */

import java.lang.annotation.RetentionPolicy;

public class RetentionPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.RetentionPolicy");
        check(c.isEnum(), "RetentionPolicy is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 3, "three constants");
        check(c.getDeclaredMethod("values") != null, "values() declared");
        check(c.getDeclaredMethod("valueOf", String.class) != null, "valueOf(String) declared");
    }

    // ---- Part 2: behavior ----
    static void testBehavior() {
        RetentionPolicy[] vals = RetentionPolicy.values();
        check(vals.length == 3, "exactly three values");
        check(vals[0] == RetentionPolicy.SOURCE && vals[1] == RetentionPolicy.CLASS
              && vals[2] == RetentionPolicy.RUNTIME, "order: SOURCE, CLASS, RUNTIME");

        // values() defensive copy
        vals[0] = null;
        check(RetentionPolicy.values()[0] == RetentionPolicy.SOURCE, "values() fresh array");

        // valueOf round-trip
        check(RetentionPolicy.valueOf("SOURCE") == RetentionPolicy.SOURCE, "valueOf SOURCE");
        check(RetentionPolicy.valueOf("CLASS") == RetentionPolicy.CLASS, "valueOf CLASS");
        check(RetentionPolicy.valueOf("RUNTIME") == RetentionPolicy.RUNTIME, "valueOf RUNTIME");
        try {
            RetentionPolicy.valueOf("source");
            check(false, "valueOf is case-sensitive");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown/case-mismatch throws IAE");
        }

        // ordinal for every constant equals its declaration index
        for (int i = 0; i < RetentionPolicy.values().length; i++) {
            check(RetentionPolicy.values()[i].ordinal() == i, "ordinal " + i);
        }
        check(RetentionPolicy.SOURCE.ordinal() == 0, "SOURCE ordinal 0");
        check(RetentionPolicy.CLASS.ordinal() == 1, "CLASS ordinal 1");
        check(RetentionPolicy.RUNTIME.ordinal() == 2, "RUNTIME ordinal 2");

        // enum mechanics
        check(RetentionPolicy.SOURCE.compareTo(RetentionPolicy.RUNTIME) < 0, "compareTo ordering");
        check(RetentionPolicy.SOURCE.name().equals("SOURCE"), "name");
        check(RetentionPolicy.CLASS.toString().equals("CLASS"), "toString == name");
        check(RetentionPolicy.valueOf("RUNTIME") == RetentionPolicy.RUNTIME, "identity");

        // switch consumption over all constants
        for (RetentionPolicy p : RetentionPolicy.values()) {
            String bucket;
            switch (p) {
                case SOURCE:  bucket = "src"; break;
                case CLASS:   bucket = "cls"; break;
                case RUNTIME: bucket = "rt";  break;
                default: throw new AssertionError("unexpected " + p);
            }
            check(bucket != null, "switch covers " + p);
        }
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
