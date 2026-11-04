/*
 * @test
 * @summary java.util.Spliterator.OfPrimitive conformance: generic primitive spliterator.
 * @run main OfPrimitiveTest
 */

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.IntConsumer;

public class OfPrimitiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfPrimitive() {
        Spliterator.OfPrimitive<Integer, IntConsumer, Spliterator.OfInt> sp =
            Arrays.spliterator(new int[] { 1, 2, 3 });
        check(sp != null, "obtainable via OfInt");
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.characteristics() != 0, "characteristics");
    }

    public static void main(String[] args) {
        testOfPrimitive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
