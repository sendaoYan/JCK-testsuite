/*
 * @test
 * @summary java.util.Spliterator API conformance: estimateSize/getExactSizeIfKnown, characteristics/
 *          hasCharacteristics, tryAdvance (including exhausted false), forEachRemaining, trySplit
 *          preserving total size, and getComparator throwing IllegalStateException on non-SORTED.
 * @run main SpliteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;

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
            if (exType.isInstance(t)) {
                passed++;
                return;
            }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static void testSized() {
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c")).spliterator();
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown when SIZED");
        check(sp.hasCharacteristics(Spliterator.ORDERED), "hasCharacteristics ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "hasCharacteristics SIZED");
        check(!sp.hasCharacteristics(Spliterator.SORTED), "!hasCharacteristics SORTED");
        List<String> seen = new ArrayList<>();
        while (sp.tryAdvance(seen::add)) {
            // consume
        }
        check(seen.equals(Arrays.asList("a", "b", "c")), "tryAdvance iteration");
        check(!sp.tryAdvance(x -> {
            throw new AssertionError("consumer must not be invoked when exhausted");
        }), "tryAdvance returns false when exhausted");
    }

    static void testSplit() {
        Spliterator<String> sp = new ArrayList<>(Arrays.asList("a", "b", "c", "d")).spliterator();
        Spliterator<String> split = sp.trySplit();
        check(split != null, "trySplit returns non-null for size >= 2");
        long total = split.estimateSize() + sp.estimateSize();
        check(total == 4, "trySplit preserves total size");
        List<String> all = new ArrayList<>();
        split.forEachRemaining(all::add);
        sp.forEachRemaining(all::add);
        check(all.size() == 4, "split covers all elements");
    }

    static void testForEachRemainingAndComparator() {
        List<String> remaining = new ArrayList<>();
        new ArrayList<>(Arrays.asList("x", "y")).spliterator().forEachRemaining(remaining::add);
        check(remaining.equals(Arrays.asList("x", "y")), "forEachRemaining");
        assertThrows(IllegalStateException.class,
                () -> new ArrayList<>(Arrays.asList("x")).spliterator().getComparator(),
                "getComparator on non-SORTED throws ISE");
    }

    static void testUnsized() {
        Spliterator<String> sp = Spliterators.spliteratorUnknownSize(
                Arrays.asList("a", "b").iterator(), 0);
        check(!sp.hasCharacteristics(Spliterator.SIZED), "unknown-size spliterator is not SIZED");
        check(sp.getExactSizeIfKnown() == -1, "getExactSizeIfKnown is -1 when not SIZED");
    }

    public static void main(String[] args) {
        testSized();
        testSplit();
        testForEachRemainingAndComparator();
        testUnsized();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
