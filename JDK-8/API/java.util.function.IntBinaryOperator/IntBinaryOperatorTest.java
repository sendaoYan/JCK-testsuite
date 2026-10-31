/*
 * @test
 * @summary java.util.function.IntBinaryOperator API conformance: applyAsInt.
 * @run main IntBinaryOperatorTest
 */

import java.util.function.IntBinaryOperator;

public class IntBinaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntBinaryOperator() {
        IntBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsInt(2, 3) == 5, "applyAsInt");
        IntBinaryOperator mul = (a, b) -> a * b;
        check(mul.applyAsInt(4, 5) == 20, "applyAsInt multiply");
    }

    public static void main(String[] args) {
        testIntBinaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
