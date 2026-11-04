/*
 * @test
 * @summary java.lang.Iterable API conformance: iterator() traversal order, default
 *          forEach(Consumer) visiting every element in order, and default spliterator()
 *          estimate/forEachRemaining.
 * @run main IterableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IterableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIterator() {
        Iterable<String> it = Arrays.asList("a", "b", "c");
        Iterator<String> i = it.iterator();
        check(i.hasNext() && i.next().equals("a"), "next 1");
        check(i.hasNext() && i.next().equals("b"), "next 2");
        check(i.hasNext() && i.next().equals("c"), "next 3");
        check(!i.hasNext(), "exhausted");
        try {
            i.next();
            throw new AssertionError("next() after exhaustion: expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            passed++;
        }
    }

    static void testForEach() {
        Iterable<Integer> it = Arrays.asList(1, 2, 3);
        final List<Integer> seen = new ArrayList<>();
        it.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach visits in order");
    }

    static void testSpliterator() {
        Iterable<Integer> it = Arrays.asList(1, 2, 3);
        java.util.Spliterator<Integer> sp = it.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize == 3");
        final int[] count = {0};
        sp.forEachRemaining(x -> count[0]++);
        check(count[0] == 3, "spliterator visits all elements");
    }

    public static void main(String[] args) {
        testIterator();
        testForEach();
        testSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
