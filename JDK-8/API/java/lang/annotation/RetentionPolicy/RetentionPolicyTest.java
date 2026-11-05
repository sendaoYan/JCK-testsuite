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

    // ---- Part 3: exact negative messages and identity mechanics ----
    static void testNegativeExactAndIdentity() {
        // valueOf with an unknown name: exact message, identical on JDK 8 and JDK 27
        try {
            RetentionPolicy.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().equals(
                    "No enum constant java.lang.annotation.RetentionPolicy.BOGUS"),
                  "exact IAE message: " + expected.getMessage());
        }
        try {
            RetentionPolicy.valueOf("");
            check(false, "valueOf(\"\") should throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().equals(
                    "No enum constant java.lang.annotation.RetentionPolicy."),
                  "empty-name IAE message: " + expected.getMessage());
        }
        // null name is a NullPointerException
        try {
            RetentionPolicy.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException expected) {
            check(true, "valueOf(null) throws NPE");
        }

        // the canonical consumer: RetentionPolicy as an annotation member value
        java.lang.annotation.Retention self =
                java.lang.annotation.Retention.class.getAnnotation(java.lang.annotation.Retention.class);
        check(self != null && self.value() == RetentionPolicy.RUNTIME,
              "Retention itself is @Retention(RUNTIME) -- only RUNTIME is reflectable");
        check(java.lang.annotation.Retention.class.isAnnotationPresent(
                java.lang.annotation.Documented.class), "Retention is @Documented");

        // singleton identity: values() and getEnumConstants() share instances
        RetentionPolicy[] vals = RetentionPolicy.values();
        RetentionPolicy[] consts = RetentionPolicy.class.getEnumConstants();
        check(vals.length == consts.length && vals.length == 3, "same length 3");
        boolean identical = true;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != consts[i]) identical = false;
        }
        check(identical, "shared singleton instances");

        // getDeclaringClass is the enum itself; compareTo is the ordinal difference
        check(RetentionPolicy.SOURCE.getDeclaringClass() == RetentionPolicy.class,
              "getDeclaringClass");
        check(RetentionPolicy.RUNTIME.compareTo(RetentionPolicy.CLASS) == 1,
              "compareTo exact diff +1");
        check(RetentionPolicy.SOURCE.compareTo(RetentionPolicy.RUNTIME) == -2,
              "compareTo exact diff -2");

        // name() of every constant, exactly three, in declaration order
        check((RetentionPolicy.SOURCE.name() + "," + RetentionPolicy.CLASS.name()
              + "," + RetentionPolicy.RUNTIME.name()).equals("SOURCE,CLASS,RUNTIME"),
              "names are SOURCE,CLASS,RUNTIME");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        testNegativeExactAndIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
