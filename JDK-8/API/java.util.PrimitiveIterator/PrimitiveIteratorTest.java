/*
 * @test
 * @summary java.util.PrimitiveIterator API conformance: hasNext/nextInt, and nextInt after
 *          exhaustion throwing NoSuchElementException.
 * @run main PrimitiveIteratorTest
 */

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class PrimitiveIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPrimitiveIterator() {
        PrimitiveIterator.OfInt it = IntStream.of(1, 2, 3).iterator();
        check(it.hasNext() && it.nextInt() == 1, "hasNext/nextInt");
        check(it.nextInt() == 2 && it.nextInt() == 3 && !it.hasNext(), "consume to exhaustion");
        try {
            it.nextInt();
            throw new AssertionError("nextInt exhausted: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testPrimitiveIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
