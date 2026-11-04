/*
 * @test
 * @summary java.util.Queue API conformance: offer/add return semantics, peek/element and
 *          poll/remove return-and-remove semantics, empty-queue null vs NoSuchElementException,
 *          and null-argument NPE.
 * @run main QueueTest
 */

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testOfferAdd() {
        Queue<String> q = new ArrayDeque<>();
        check(q.offer("a"), "offer returns true");
        check(q.add("b"), "add returns true (unbounded)");
        check(q.size() == 2, "size after offer/add");
        expectThrows(NullPointerException.class, () -> q.offer(null), "offer(null) NPE");
        expectThrows(NullPointerException.class, () -> q.add(null), "add(null) NPE");
    }

    static void testPeekElement() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b"));
        check("a".equals(q.peek()), "peek head (no remove)");
        check("a".equals(q.element()), "element head (no remove)");
        check(q.size() == 2, "peek/element do not remove");
    }

    static void testPollRemove() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b"));
        check("a".equals(q.poll()), "poll removes head");
        check("b".equals(q.remove()), "remove removes head");
        check(q.isEmpty(), "emptied after poll/remove");
    }

    static void testEmptySemantics() {
        Queue<String> q = new ArrayDeque<>();
        check(q.peek() == null, "peek on empty returns null");
        check(q.poll() == null, "poll on empty returns null");
        expectThrows(NoSuchElementException.class, q::element, "element on empty NSEE");
        expectThrows(NoSuchElementException.class, q::remove, "remove on empty NSEE");
    }

    static void testCollectionContract() {
        Queue<String> q = new ArrayDeque<>(Arrays.asList("a", "b", "c"));
        check(q.contains("b") && !q.contains("z"), "contains");
        check(q.remove("b") && q.size() == 2, "remove(Object)");
        Iterator<String> it = q.iterator();
        check("a".equals(it.next()), "iterator yields head first");
    }

    public static void main(String[] args) {
        testOfferAdd();
        testPeekElement();
        testPollRemove();
        testEmptySemantics();
        testCollectionContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
