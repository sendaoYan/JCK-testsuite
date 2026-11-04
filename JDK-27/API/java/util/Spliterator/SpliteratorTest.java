/*
 * @test
 * @summary java.util.Spliterator conformance: white-box structure plus black-box behavior of
 *          estimateSize/getExactSizeIfKnown, characteristics/hasCharacteristics, tryAdvance,
 *          forEachRemaining, trySplit, and getComparator on a non-SORTED spliterator.
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

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Spliterator");
        check(c.isInterface(), "Spliterator is an interface");
        c.getMethod("tryAdvance", java.util.function.Consumer.class);
        c.getMethod("trySplit");
        c.getMethod("estimateSize");
        c.getMethod("characteristics");
        c.getMethod("hasCharacteristics", int.class);
        c.getMethod("getExactSizeIfKnown");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown");
        check(sp.hasCharacteristics(Spliterator.ORDERED), "hasCharacteristics ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "hasCharacteristics SIZED");
        check(!sp.hasCharacteristics(Spliterator.SORTED), "!hasCharacteristics SORTED");

        List<String> seen = new ArrayList<>();
        while (sp.tryAdvance(seen::add)) {
            // consume
        }
        check(seen.equals(Arrays.asList("a", "b", "c")), "tryAdvance iteration");

        Spliterator<String> sp2 = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        Spliterator<String> split = sp2.trySplit();
        long total = (split == null ? 0 : split.estimateSize()) + sp2.estimateSize();
        check(total == 3, "trySplit preserves total size");
        assertThrows(IllegalStateException.class, sp2::getComparator,
                "getComparator on non-SORTED throws IllegalStateException");

        List<String> remaining = new ArrayList<>();
        Spliterator<String> sp3 = new ArrayList<>(Arrays.asList("x", "y")).spliterator();
        sp3.forEachRemaining(remaining::add);
        check(remaining.equals(Arrays.asList("x", "y")), "forEachRemaining");

        Spliterator<String> exhausted = new ArrayList<>(Arrays.asList("q")).spliterator();
        exhausted.tryAdvance(x -> { });
        check(!exhausted.tryAdvance(x -> {
            throw new AssertionError("consumer must not run when exhausted");
        }), "tryAdvance exhausted false");
        check(new ArrayList<String>().spliterator().estimateSize() == 0, "empty estimateSize 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
