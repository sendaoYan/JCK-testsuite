/*
 * @test
 * @summary java.lang.annotation.RetentionPolicy conformance: enum constants/valueOf/ordinal.
 * @run main RetentionPolicyTest
 */

import java.lang.annotation.RetentionPolicy;

public class RetentionPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRetentionPolicy() {
        check(RetentionPolicy.values().length == 3, "three values");
        check(RetentionPolicy.valueOf("SOURCE") == RetentionPolicy.SOURCE, "valueOf SOURCE");
        check(RetentionPolicy.valueOf("CLASS") == RetentionPolicy.CLASS, "valueOf CLASS");
        check(RetentionPolicy.valueOf("RUNTIME") == RetentionPolicy.RUNTIME, "valueOf RUNTIME");
        check(RetentionPolicy.SOURCE.ordinal() == 0, "SOURCE ordinal 0");
        check(RetentionPolicy.CLASS.ordinal() == 1, "CLASS ordinal 1");
        check(RetentionPolicy.RUNTIME.ordinal() == 2, "RUNTIME ordinal 2");
    }

    public static void main(String[] args) {
        testRetentionPolicy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
