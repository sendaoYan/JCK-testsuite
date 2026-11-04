/*
 * @test
 * @summary java.util.AbstractQueue API conformance: add delegates to offer (ISE when full),
 *          remove delegates to poll (NSEE when empty), element delegates to peek (NSEE when
 *          empty), clear loops poll, addAll(null) NPE, addAll(self) IAE.
 * @run main AbstractQueueTest
 */

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class AbstractQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class Q extends AbstractQueue<String> {
        private final LinkedList<String> list = new LinkedList<>();
        public boolean offer(String e) { return list.offer(e); }
        public String poll() { return list.poll(); }
        public String peek() { return list.peek(); }
        public Iterator<String> iterator() { return list.iterator(); }
        public int size() { return list.size(); }
    }

    // Bounded queue: offer rejects when already at capacity (returns false).
    static class Bounded extends AbstractQueue<String> {
        private final LinkedList<String> list = new LinkedList<>();
        private final int capacity;
        Bounded(int capacity) { this.capacity = capacity; }
        public boolean offer(String e) {
            if (list.size() >= capacity) return false;
            return list.offer(e);
        }
        public String poll() { return list.poll(); }
        public String peek() { return list.peek(); }
        public Iterator<String> iterator() { return list.iterator(); }
        public int size() { return list.size(); }
    }

    static void testAddOfferPoll() {
        Q q = new Q();
        check(q.add("a"), "add returns true");
        check(q.add("b"), "add second");
        check("a".equals(q.element()), "element returns head without removing");
        check("a".equals(q.remove()), "remove returns and removes head");
        check("b".equals(q.poll()), "poll returns and removes head");
        check(q.poll() == null, "poll on empty returns null");
    }

    static void testEmptyExceptions() {
        Q q = new Q();
        checkThrows(NoSuchElementException.class, q::remove, "remove on empty NSEE");
        checkThrows(NoSuchElementException.class, q::element, "element on empty NSEE");
    }

    static void testAddWhenFullISE() {
        Bounded b = new Bounded(1);
        check(b.add("a"), "add first succeeds");
        checkThrows(IllegalStateException.class, () -> b.add("b"), "add when full ISE");
    }

    static void testClear() {
        Q q = new Q();
        q.add("a");
        q.add("b");
        q.add("c");
        q.clear();
        check(q.isEmpty() && q.size() == 0, "clear empties via poll loop");
    }

    static void testAddAll() {
        Q q = new Q();
        checkThrows(NullPointerException.class, () -> q.addAll(null), "addAll(null) NPE");
        checkThrows(IllegalArgumentException.class, () -> q.addAll(q), "addAll(self) IAE");
        check(q.addAll(Arrays.asList("a", "b")), "addAll returns true");
        check(q.size() == 2 && q.peek().equals("a"), "addAll adds elements");
    }

    public static void main(String[] args) {
        testAddOfferPoll();
        testEmptyExceptions();
        testAddWhenFullISE();
        testClear();
        testAddAll();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
