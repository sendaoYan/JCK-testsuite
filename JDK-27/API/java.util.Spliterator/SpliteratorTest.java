/*
 * @test
 * @summary java.util.Spliterator API conformance: estimateSize, characteristics, and
 *          tryAdvance iteration.
 * @run main SpliteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;

public class SpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSpliterator() {
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.hasCharacteristics(Spliterator.ORDERED), "hasCharacteristics ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "hasCharacteristics SIZED");
        List<String> seen = new ArrayList<>();
        while (sp.tryAdvance(seen::add)) {
            // consume
        }
        check(seen.equals(Arrays.asList("a", "b", "c")), "tryAdvance iteration");
    }

    public static void main(String[] args) {
        testSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
