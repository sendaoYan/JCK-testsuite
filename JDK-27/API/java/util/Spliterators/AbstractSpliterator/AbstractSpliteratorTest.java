/*
 * @test
 * @summary java.util.Spliterators.AbstractSpliterator conformance.
 * @run main AbstractSpliteratorTest
 */

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class AbstractSpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSpliterator() {
        Spliterator<String> sp = new Spliterators.AbstractSpliterator<String>(3, Spliterator.SIZED) {
            int i = 0;
            public boolean tryAdvance(Consumer<? super String> action) {
                if (i < 3) { action.accept("e" + i); i++; return true; }
                return false;
            }
        };
        check(sp.estimateSize() == 3, "estimateSize");
        check((sp.characteristics() & Spliterator.SIZED) != 0, "characteristics include SIZED");
        final int[] count = new int[1];
        sp.forEachRemaining(s -> count[0]++);
        check(count[0] == 3, "forEachRemaining");
    }

    public static void main(String[] args) {
        testAbstractSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
