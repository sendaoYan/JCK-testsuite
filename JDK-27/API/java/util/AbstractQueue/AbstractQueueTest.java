/*
 * @test
 * @summary java.util.AbstractQueue conformance: white-box structure + black-box behavior.
 * @run main AbstractQueueTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class AbstractQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
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

    /** Minimal concrete subclass implementing only offer/poll/peek/iterator/size. */
    static class Q extends AbstractQueue<String> {
        private final LinkedList<String> list = new LinkedList<>();
        @Override public boolean offer(String e) { return list.offer(e); }
        @Override public String poll() { return list.poll(); }
        @Override public String peek() { return list.peek(); }
        @Override public Iterator<String> iterator() { return list.iterator(); }
        @Override public int size() { return list.size(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractQueue");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractCollection.class.isAssignableFrom(c), "extends AbstractCollection");
        check(Queue.class.isAssignableFrom(c), "implements Queue");
        check(!Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()), "add concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("remove").getModifiers()), "remove concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("element").getModifiers()), "element concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("clear").getModifiers()), "clear concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("addAll", java.util.Collection.class).getModifiers()),
                "addAll concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. add == offer, element == peek (non-throwing), remove == poll
        Q q = new Q();
        check(q.add("a") && q.add("b"), "add appends");
        check("a".equals(q.element()), "element returns head without removing");
        check("a".equals(q.remove()), "remove returns and removes head");
        check("b".equals(q.poll()), "poll returns and removes head");
        check(q.size() == 0, "emptied");

        // 2. element()/remove() throw NoSuchElementException on empty; peek/poll return null
        expectThrows(q::element, NoSuchElementException.class, "element on empty");
        expectThrows(q::remove, NoSuchElementException.class, "remove on empty");
        check(q.peek() == null && q.poll() == null, "peek/poll null on empty");

        // 3. clear empties
        q.add("x");
        q.add("y");
        q.clear();
        check(q.isEmpty() && q.size() == 0, "clear empties");

        // 4. addAll adds each element (returns true when queue changed)
        Q q2 = new Q();
        List<String> src = new ArrayList<>(Arrays.asList("m", "n"));
        check(q2.addAll(src), "addAll returns true");
        check(q2.size() == 2 && "m".equals(q2.peek()), "addAll adds in order");

        // 5. iterator() + AbstractCollection.toString integration
        check("[m, n]".equals(q2.toString()), "toString via iterator");
    }

    static void testAddWhenFullAndAddAllEdges() {
        // bounded queue: offer returns false when full -> add throws IllegalStateException
        class Bounded extends AbstractQueue<String> {
            final LinkedList<String> list = new LinkedList<>();
            public boolean offer(String e) {
                if (list.size() >= 1) return false;
                return list.offer(e);
            }
            public String poll() { return list.poll(); }
            public String peek() { return list.peek(); }
            public Iterator<String> iterator() { return list.iterator(); }
            public int size() { return list.size(); }
        }
        Bounded b = new Bounded();
        check(b.add("a"), "add first succeeds");
        expectThrows(() -> b.add("b"), IllegalStateException.class, "add when full throws ISE");

        Q q = new Q();
        expectThrows(() -> q.addAll(null), NullPointerException.class, "addAll(null) NPE");
        expectThrows(() -> q.addAll(q), IllegalArgumentException.class, "addAll(self) IAE");
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

    /** Bounded queue: offer rejects when already at capacity (returns false). */
    static class FixedBounded extends AbstractQueue<String> {
        final LinkedList<String> list = new LinkedList<>();
        private final int capacity;
        FixedBounded(int capacity) { this.capacity = capacity; }
        public boolean offer(String e) {
            if (list.size() >= capacity) return false;
            return list.offer(e);
        }
        public String poll() { return list.poll(); }
        public String peek() { return list.peek(); }
        public Iterator<String> iterator() { return list.iterator(); }
        public int size() { return list.size(); }
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

        q.add("x");
        q.add("y");
        check(q.offers == 3, "three adds -> three offers");
        check(q.element().equals("x") && q.peeks == 2, "second element -> second peek");
        check(q.remove().equals("x") && q.polls == 2, "second remove -> second poll");
    }

    static void testAddFullExactMessage() {
        FixedBounded b = new FixedBounded(1);
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
        expectThrows(() -> q.addAll(q), IllegalArgumentException.class, "addAll(self) IAE");

        // null element: AbstractQueue.addAll itself does not pre-check; the NPE
        // comes from the underlying queue when it rejects null.
        NullRejecting nr = new NullRejecting();
        nr.add("x");
        expectThrows(() -> nr.addAll(Arrays.asList("y", null)),
                NullPointerException.class,
                "null-rejecting queue: addAll hits NPE at the null element");
        check(nr.size() == 2, "elements before the null were already added (partial)");
    }

    static void testAddAllPartialFailure() {
        FixedBounded b = new FixedBounded(1);
        expectThrows(() -> b.addAll(Arrays.asList("p", "q")),
                IllegalStateException.class, "bounded addAll overflows with ISE");
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
        check(q.containsAll(List.of("n")) && !q.containsAll(List.of("n", "k")),
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAddWhenFullAndAddAllEdges();
        testDispatchCounters();
        testAddFullExactMessage();
        testClearPollLoop();
        testAddAllSemantics();
        testAddAllPartialFailure();
        testInheritedCollectionOps();
        testEqualsHashCodeIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
