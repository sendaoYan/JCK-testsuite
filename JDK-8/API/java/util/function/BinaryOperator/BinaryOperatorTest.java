/*
 * @test
 * @summary java.util.function.BinaryOperator API conformance: apply, and static maxBy/minBy.
 * @run main BinaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.BinaryOperator;

public class BinaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBinaryOperator() {
        BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
        check(max.apply(3, 5) == 5, "apply");
        BinaryOperator<Integer> maxBy = BinaryOperator.maxBy(Comparator.naturalOrder());
        check(maxBy.apply(3, 5) == 5, "maxBy");
        BinaryOperator<Integer> minBy = BinaryOperator.minBy(Comparator.naturalOrder());
        check(minBy.apply(3, 5) == 3, "minBy");
    }

    public static void main(String[] args) {
        testBinaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
