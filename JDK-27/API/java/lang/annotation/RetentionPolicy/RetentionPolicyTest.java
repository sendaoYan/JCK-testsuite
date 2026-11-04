/*
 * @test
 * @summary java.lang.annotation.RetentionPolicy conformance: white-box structure + black-box behavior.
 * @run main RetentionPolicyTest
 */

import java.lang.annotation.RetentionPolicy;

public class RetentionPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.RetentionPolicy");
        check(c.isEnum(), "RetentionPolicy is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 3, "three constants");
        check(c.getDeclaredMethod("values") != null, "values() declared");
        check(c.getDeclaredMethod("valueOf", String.class) != null, "valueOf(String) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exact constant set and order
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

        // ordinals
        check(RetentionPolicy.SOURCE.ordinal() == 0, "SOURCE ordinal 0");
        check(RetentionPolicy.CLASS.ordinal() == 1, "CLASS ordinal 1");
        check(RetentionPolicy.RUNTIME.ordinal() == 2, "RUNTIME ordinal 2");

        // enum mechanics
        check(RetentionPolicy.SOURCE.compareTo(RetentionPolicy.RUNTIME) < 0, "compareTo ordering");
        check(RetentionPolicy.SOURCE.name().equals("SOURCE"), "name");
        check(RetentionPolicy.CLASS.toString().equals("CLASS"), "toString == name");

        // usable in switch (the canonical consumer pattern)
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

        // identity equality
        check(RetentionPolicy.valueOf("RUNTIME") == RetentionPolicy.RUNTIME, "identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
