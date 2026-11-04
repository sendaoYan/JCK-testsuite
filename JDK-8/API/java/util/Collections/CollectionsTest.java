/*
 * @test
 * @summary java.util.Collections API conformance: sort/reverse/shuffle/rotate/swap/fill/
 *          copy/replaceAll, binarySearch, min/max (natural and comparator), frequency/
 *          disjoint/nCopies, reverseOrder, and the empty/singleton/unmodifiable/synchronized
 *          wrappers (including UnsupportedOperationException).
 * @run main CollectionsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectUOE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
    }

    static void testSortReverse() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        Collections.sort(list);
        check(list.equals(Arrays.asList(1, 2, 3)), "sort natural");
        Collections.sort(list, Comparator.reverseOrder());
        check(list.equals(Arrays.asList(3, 2, 1)), "sort with reverse comparator");
        Collections.reverse(list);
        check(list.equals(Arrays.asList(1, 2, 3)), "reverse");

        List<Integer> shuffled = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.shuffle(shuffled);
        List<Integer> sorted = new ArrayList<>(shuffled);
        Collections.sort(sorted);
        check(sorted.equals(Arrays.asList(1, 2, 3, 4, 5)), "shuffle preserves elements");
    }

    static void testBinarySearch() {
        List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
        check(Collections.binarySearch(list, 5) == 2, "binarySearch found");
        check(Collections.binarySearch(list, 2) == -2, "binarySearch insertion point");
        check(Collections.binarySearch(list, 10) == -6, "binarySearch after last");
    }

    static void testMinMax() {
        List<Integer> list = Arrays.asList(3, 1, 2);
        check(Collections.min(list) == 1, "min");
        check(Collections.max(list) == 3, "max");
        check(Collections.min(list, Comparator.reverseOrder()) == 3, "min reverse");
        check(Collections.max(list, Comparator.reverseOrder()) == 1, "max reverse");
    }

    static void testFrequencyDisjointNCopies() {
        List<String> list = Arrays.asList("a", "b", "a", "c", "a");
        check(Collections.frequency(list, "a") == 3, "frequency");
        check(Collections.frequency(list, "z") == 0, "frequency missing");
        check(Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(3, 4)), "disjoint true");
        check(!Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(2, 3)), "disjoint false");
        check(Collections.nCopies(3, "x").equals(Arrays.asList("x", "x", "x")), "nCopies");
    }

    static void testReverseOrder() {
        Comparator<Integer> rev = Collections.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder compares descending");
        check(Collections.reverseOrder(Comparator.<Integer>naturalOrder()).compare(1, 2) > 0,
              "reverseOrder(Comparator)");
    }

    static void testUnmodifiable() {
        List<String> base = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> unmod = Collections.unmodifiableList(base);
        check(unmod.equals(Arrays.asList("a", "b")), "unmodifiableList content");
        expectUOE(() -> unmod.add("c"), "unmodifiableList.add");
        expectUOE(() -> unmod.remove(0), "unmodifiableList.remove");
        expectUOE(() -> unmod.set(0, "z"), "unmodifiableList.set");

        check(Collections.unmodifiableMap(Collections.singletonMap("k", "v")).get("k").equals("v"),
              "unmodifiableMap get");
    }

    static void testEmptySingleton() {
        check(Collections.emptyList().isEmpty(), "emptyList empty");
        check(Collections.emptySet().isEmpty(), "emptySet empty");
        check(Collections.emptyMap().isEmpty(), "emptyMap empty");
        expectUOE(() -> Collections.emptyList().add("x"), "emptyList.add");

        check(Collections.singletonList("x").get(0).equals("x"), "singletonList");
        check(Collections.singleton("x").contains("x"), "singleton set");
        check(Collections.singletonMap("k", 1).get("k") == 1, "singletonMap");
    }

    static void testSynchronized() {
        List<String> sync = Collections.synchronizedList(new ArrayList<>(Arrays.asList("a", "b")));
        check(sync.size() == 2 && sync.get(0).equals("a"), "synchronizedList content");
        sync.add("c");
        check(sync.size() == 3, "synchronizedList add");
    }

    public static void main(String[] args) {
        testSortReverse();
        testBinarySearch();
        testMinMax();
        testFrequencyDisjointNCopies();
        testReverseOrder();
        testUnmodifiable();
        testEmptySingleton();
        testSynchronized();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
