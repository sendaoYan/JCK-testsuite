/*
 * @test
 * @summary java.util.AbstractQueue API conformance: add delegates to offer (ISE when full),
 *          remove delegates to poll (NSEE when empty), element delegates to peek (NSEE when
 *          empty), clear loops poll, addAll(null) NPE, addAll(self) IAE.
 * @run main AbstractQueueTest
 */

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
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

    // ---- contract deep-dive: default-method dispatch counters ---------------

    /** Probe recording every offer/poll/peek invocation. */
    static class Counting extends AbstractQueue<String> {
        final LinkedList<String> list = new LinkedList<>();
        int offers, polls, peeks;
        public boolean offer(String e) { offers++; return list.offer(e); }
        public String poll() { polls++; return list.poll(); }
        public String peek() { peeks++; return list.peek(); }
        public Iterator<String> iterator() { return list.iterator(); }
        public int size() { return list.size(); }
    }

    /** Null-rejecting backing queue (ArrayDeque semantics). */
    static class NullRejecting extends AbstractQueue<String> {
        final ArrayDeque<String> d = new ArrayDeque<>();
        public boolean offer(String e) { d.addLast(e); return true; }
        public String poll() { return d.pollFirst(); }
        public String peek() { return d.peekFirst(); }
        public Iterator<String> iterator() { return d.iterator(); }
        public int size() { return d.size(); }
    }

    static void testDispatchCounters() {
        Counting q = new Counting();
        check(q.add("a"), "add returns true");
        check(q.offers == 1, "add dispatches to exactly one offer call");
        check(q.element().equals("a"), "element returns head");
        check(q.peeks == 1, "element dispatches to exactly one peek call");
        check(q.remove().equals("a"), "remove returns head");
        check(q.polls == 1, "remove dispatches to exactly one poll call");
        check(q.isEmpty(), "empty after remove");

        // repeated calls stay 1:1
        q.add("x");
        q.add("y");
        check(q.offers == 3, "three adds -> three offers");
        check(q.element().equals("x") && q.peeks == 2, "second element -> second peek");
        check(q.remove().equals("x") && q.polls == 2, "second remove -> second poll");
    }

    static void testAddFullExactMessage() {
        Bounded b = new Bounded(1);
        check(b.add("a"), "first add succeeds");
        try {
            b.add("b");
            check(false, "add when full must throw");
        } catch (IllegalStateException e) {
            check("Queue full".equals(e.getMessage()),
                    "IllegalStateException message is exactly 'Queue full'");
            passed++;
        }
    }

    static void testClearPollLoop() {
        Counting q = new Counting();
        q.add("a");
        q.add("b");
        check(q.polls == 0, "no polls yet");
        q.clear();
        // clear() polls until null: once per element plus one final null probe.
        check(q.polls == 3, "clear of 2 elements performs exactly 3 polls (n + sentinel)");
        check(q.isEmpty() && q.size() == 0, "clear empties");
    }

    static void testAddAllSemantics() {
        Counting q = new Counting();
        check(!q.addAll(java.util.Collections.<String>emptyList()),
                "addAll(empty) returns false, no offers");
        check(q.offers == 0, "no elements offered for empty addAll");
        check(q.addAll(Arrays.asList("a", "b")), "addAll returns true when changed");
        check(q.offers == 2 && q.size() == 2, "one offer per element via add");
        checkThrows(IllegalArgumentException.class, () -> q.addAll(q),
                "addAll(self) IAE");

        // null element: AbstractQueue.addAll itself does not pre-check; the NPE
        // comes from the underlying queue when it rejects null.
        NullRejecting nr = new NullRejecting();
        nr.add("x");
        checkThrows(NullPointerException.class,
                () -> nr.addAll(Arrays.asList("y", null)),
                "null-rejecting queue: addAll hits NPE at the null element");
        check(nr.size() == 2, "elements before the null were already added (partial)");
    }

    static void testAddAllPartialFailure() {
        Bounded b = new Bounded(1);
        checkThrows(IllegalStateException.class,
                () -> b.addAll(Arrays.asList("p", "q")),
                "bounded addAll overflows with ISE");
        check(b.size() == 1 && "p".equals(b.peek()),
                "elements before the failing one stay queued (partial application)");
    }

    static void testInheritedCollectionOps() {
        Q q = new Q();
        q.add("m");
        q.add("n");
        check(q.contains("m"), "contains via inherited iterator scan");
        check(!q.contains("z"), "contains absent false");
        check(q.remove("m") && q.size() == 1, "remove(Object) via inherited iterator");
        check("[n]".equals(q.toString()), "toString from AbstractCollection via iterator");
        check("[]".equals(new Q().toString()), "empty toString []");
        check(q.containsAll(Arrays.asList("n")) && !q.containsAll(Arrays.asList("n", "k")),
                "containsAll inherited");
    }

    static void testEqualsHashCodeIdentity() {
        Q a = new Q();
        Q b = new Q();
        a.add("s");
        b.add("s");
        check(!a.equals(b), "AbstractQueue does not override equals (identity)");
        check(a.equals(a), "self equals");
        check(a.hashCode() == System.identityHashCode(a),
                "hashCode is Object identity hashCode");
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractQueue");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
                "AbstractQueue is abstract");
        check(c.getSuperclass() == java.util.AbstractCollection.class,
                "extends AbstractCollection");
        // Exactly five public methods are implemented here: add/remove/element/
        // clear/addAll (all others are inherited or interface-abstract).
        java.lang.reflect.Method[] declared = {
                c.getDeclaredMethod("add", Object.class),
                c.getDeclaredMethod("remove"),
                c.getDeclaredMethod("element"),
                c.getDeclaredMethod("clear"),
                c.getDeclaredMethod("addAll", java.util.Collection.class)};
        String[] names = {"add", "remove", "element", "clear", "addAll"};
        for (int i = 0; i < declared.length; i++)
            check(!java.lang.reflect.Modifier.isAbstract(declared[i].getModifiers()),
                    names[i] + " is implemented (concrete) in AbstractQueue");
    }

    public static void main(String[] args) throws Exception {
        testAddOfferPoll();
        testEmptyExceptions();
        testAddWhenFullISE();
        testClear();
        testAddAll();
        testDispatchCounters();
        testAddFullExactMessage();
        testClearPollLoop();
        testAddAllSemantics();
        testAddAllPartialFailure();
        testInheritedCollectionOps();
        testEqualsHashCodeIdentity();
        testWhiteBoxStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
