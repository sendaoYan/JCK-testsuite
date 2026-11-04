/*
 * @test
 * @summary java.util.function.UnaryOperator API conformance: apply and static identity.
 * @run main UnaryOperatorTest
 */

import java.util.function.UnaryOperator;

public class UnaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnaryOperator() {
        UnaryOperator<Integer> negate = x -> -x;
        check(negate.apply(5) == -5, "apply");
        UnaryOperator<Integer> id = UnaryOperator.identity();
        check(id.apply(7) == 7, "identity");
    }

    public static void main(String[] args) {
        testUnaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
