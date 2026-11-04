/*
 * @test
 * @summary java.util.Spliterators API conformance: array/empty spliterators.
 * @run main SpliteratorsTest
 */

import java.util.Spliterator;
import java.util.Spliterators;

public class SpliteratorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSpliterators() {
        Spliterator<String> s = Spliterators.spliterator(new String[] { "a", "b", "c" }, 0);
        check(s.estimateSize() == 3, "array spliterator estimateSize");

        Spliterator<Object> empty = Spliterators.emptySpliterator();
        check(empty.estimateSize() == 0, "emptySpliterator estimateSize");
        check(!empty.tryAdvance(x -> { }), "emptySpliterator has no elements");
    }

    public static void main(String[] args) {
        testSpliterators();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
