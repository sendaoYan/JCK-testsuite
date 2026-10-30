/*
 * @test
 * @summary java.util.Iterator API conformance: hasNext/next/remove, next() after exhaustion
 *          throwing NoSuchElementException, and the Java 8 default forEachRemaining.
 * @run main IteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNextRemove() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = l.iterator();
        check(it.hasNext() && it.next().equals("a"), "next 1");
        it.remove();
        check(l.equals(Arrays.asList("b", "c")), "remove removes last-returned");
        check(it.next().equals("b") && it.hasNext(), "next 2");
        check(it.next().equals("c") && !it.hasNext(), "next 3 / exhausted");
        try {
            it.next();
            throw new AssertionError("next after exhaustion: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    static void testForEachRemaining() {
        final List<String> seen = new ArrayList<>();
        new ArrayList<>(Arrays.asList("x", "y")).iterator().forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("x", "y")), "forEachRemaining");
    }

    public static void main(String[] args) {
        testNextRemove();
        testForEachRemaining();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
