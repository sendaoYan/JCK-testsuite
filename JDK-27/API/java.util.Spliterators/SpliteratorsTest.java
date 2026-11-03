/*
 * @test
 * @summary java.util.Spliterators conformance: white-box structure plus black-box behavior of
 *          array/empty spliterators, spliterator(Collection), spliteratorUnknownSize, and
 *          iterator conversion.
 * @run main SpliteratorsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;

public class SpliteratorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Spliterators");
        check(!c.isInterface(), "Spliterators is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Spliterators is final");
        c.getMethod("spliterator", Object[].class, int.class);
        c.getMethod("spliteratorUnknownSize", Iterator.class, int.class);
        c.getMethod("emptySpliterator");
        c.getMethod("iterator", Spliterator.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testArray();
        testEmpty();
        testCollection();
        testUnknownSize();
        testIteratorConversion();
    }

    static void testArray() {
        Spliterator<String> s = Spliterators.spliterator(new String[] { "a", "b", "c" }, 0);
        check(s.estimateSize() == 3, "array spliterator estimateSize");
        List<String> seen = new ArrayList<>();
        s.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("a", "b", "c")), "array spliterator iteration");
    }

    static void testEmpty() {
        Spliterator<Object> empty = Spliterators.emptySpliterator();
        check(empty.estimateSize() == 0, "emptySpliterator estimateSize");
        check(!empty.tryAdvance(x -> { }), "emptySpliterator has no elements");
    }

    static void testCollection() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        Spliterator<Integer> s = Spliterators.spliterator(list, Spliterator.ORDERED);
        check(s.estimateSize() == 3, "collection spliterator estimateSize");
        check(s.hasCharacteristics(Spliterator.ORDERED), "collection spliterator ORDERED");
    }

    static void testUnknownSize() {
        Iterator<String> it = Arrays.asList("x", "y", "z").iterator();
        Spliterator<String> s = Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED);
        check(s.estimateSize() == Long.MAX_VALUE, "unknownSize estimateSize == Long.MAX_VALUE");
        List<String> seen = new ArrayList<>();
        s.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("x", "y", "z")), "unknownSize iteration");
    }

    static void testIteratorConversion() {
        Spliterator<String> sp = Spliterators.spliterator(new String[] { "a", "b" }, 0);
        Iterator<String> it = Spliterators.iterator(sp);
        check("a".equals(it.next()) && "b".equals(it.next()), "iterator from spliterator");
        check(!it.hasNext(), "iterator exhausted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
