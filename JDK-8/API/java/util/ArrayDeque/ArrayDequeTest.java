/*
 * @test
 * @summary java.util.ArrayDeque API conformance: full contract coverage.
 *          add/peek/poll (FIFO), addFirst/addLast, peekLast, removeFirst/removeLast,
 *          offer/poll/peek variants, push/pop LIFO, removeFirstOccurrence/
 *          removeLastOccurrence, null rejection on every mutating entry point and
 *          false on contains/remove(null), empty semantics (poll/peek null vs
 *          remove/element/getFirst/getLast/pop NSEE), constructor matrix (negative/
 *          zero capacity usable, null collection NPE), iterator/descendingIterator
 *          order, fail-fast, toArray contract, clone (shallow + independence),
 *          serialization round-trip + serialVersionUID, growth across ring boundary,
 *          and state invariants.
 * @run main ArrayDequeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ArrayDequeTest {
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

    @SuppressWarnings("unchecked")
    static <E> ArrayDeque<E> roundTrip(ArrayDeque<E> d) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(d); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ArrayDeque<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testQueue() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        check(d.isEmpty(), "empty");
        check(d.add(1) && d.add(2), "add");
        check(d.peek() == 1, "peek head");
        check(d.poll() == 1, "poll removes head");
        check(d.peek() == 2, "peek after poll");
        check(!d.contains(1) && d.contains(2), "contains");
    }

    static void testDeque() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addFirst(0);
        d.addLast(3);
        check(d.peek() == 0 && d.peekLast() == 3, "addFirst/addLast");
        check(d.size() == 2, "size");
        check(d.removeFirst() == 0 && d.removeLast() == 3, "removeFirst/removeLast");
        check(d.isEmpty(), "empty after removes");
    }

    static void testNullRejection() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        checkThrows(NullPointerException.class, () -> d.add(null), "add(null) NPE");
        checkThrows(NullPointerException.class, () -> d.offer(null), "offer(null) NPE");
        checkThrows(NullPointerException.class, () -> d.addFirst(null), "addFirst(null) NPE");
        checkThrows(NullPointerException.class, () -> d.addLast(null), "addLast(null) NPE");
        checkThrows(NullPointerException.class, () -> d.offerFirst(null), "offerFirst(null) NPE");
        checkThrows(NullPointerException.class, () -> d.offerLast(null), "offerLast(null) NPE");
        checkThrows(NullPointerException.class, () -> d.push(null), "push(null) NPE");
        d.add(1);
        check(!d.contains(null), "contains(null) returns false");
        check(!d.remove(null), "remove(null) returns false");
    }

    // ---- constructor matrix + empty semantics -------------------------------

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new ArrayDeque<Integer>((Collection<Integer>) null),
                    "ArrayDeque(null Collection) NPE");
        checkThrows(NullPointerException.class, () -> new ArrayDeque<Integer>(Arrays.asList(1, null)),
                    "ArrayDeque(Collection with null) NPE");

        // negative/zero capacity are accepted (minimum capacity is used).
        ArrayDeque<Integer> neg = new ArrayDeque<>(-1);
        neg.addLast(1);
        check(neg.size() == 1 && neg.peekFirst() == 1, "ArrayDeque(-1) usable (min capacity)");
        ArrayDeque<Integer> zero = new ArrayDeque<>(0);
        zero.addLast(1);
        check(zero.size() == 1 && zero.peekFirst() == 1, "ArrayDeque(0) usable");

        ArrayDeque<Integer> copy = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        check(copy.size() == 3 && copy.peekFirst() == 1 && copy.peekLast() == 3, "ArrayDeque(Collection) copies");
    }

    static void testEmptySemantics() {
        ArrayDeque<Integer> empty = new ArrayDeque<>();
        check(empty.poll() == null && empty.peek() == null, "empty poll/peek null");
        check(empty.pollFirst() == null && empty.pollLast() == null, "empty pollFirst/pollLast null");
        check(empty.peekFirst() == null && empty.peekLast() == null, "empty peekFirst/peekLast null");
        checkThrows(NoSuchElementException.class, empty::remove, "remove empty NSEE");
        checkThrows(NoSuchElementException.class, empty::element, "element empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getFirst, "getFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getLast, "getLast empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeFirst, "removeFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeLast, "removeLast empty NSEE");
        checkThrows(NoSuchElementException.class, empty::pop, "pop empty NSEE");
        check(!empty.removeFirstOccurrence(1) && !empty.removeLastOccurrence(1), "occurrence on empty returns false");
    }

    // ---- push/pop + occurrences + iterators + fail-fast ---------------------

    static void testPushPopAndOccurrences() {
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("a");
        stack.push("b");
        check(stack.pop().equals("b") && stack.pop().equals("a"), "pop LIFO");

        ArrayDeque<Integer> occ = new ArrayDeque<>(Arrays.asList(1, 2, 1, 3, 1));
        check(occ.removeFirstOccurrence(1), "removeFirstOccurrence returns true");
        check(Arrays.equals(occ.toArray(), new Object[]{2, 1, 3, 1}), "removeFirstOccurrence removes first");
        check(occ.removeLastOccurrence(1), "removeLastOccurrence returns true");
        check(Arrays.equals(occ.toArray(), new Object[]{2, 1, 3}), "removeLastOccurrence removes last");
        check(!occ.removeFirstOccurrence(99) && !occ.removeLastOccurrence(99), "occurrence absent false");
    }

    static void testIterators() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addFirst(2);
        d.addFirst(1);
        d.addLast(3);
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3}), "toArray order");

        Iterator<Integer> it = d.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3 && !it.hasNext(), "iterator head-to-tail");

        Iterator<Integer> desc = d.descendingIterator();
        check(desc.next() == 3 && desc.next() == 2 && desc.next() == 1 && !desc.hasNext(), "descendingIterator tail-to-head");
    }

    static void testFailFast() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        d.addLast(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        Iterator<Integer> it2 = d.iterator();
        it2.next();
        d.removeLast();
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "iterator CME after tail remove");

        Iterator<Integer> desc = d.descendingIterator();
        d.addFirst(0);
        checkThrows(java.util.ConcurrentModificationException.class, desc::next, "descendingIterator CME after add");
    }

    // ---- toArray contract + clone + serialization + invariants --------------

    static void testToArrayContract() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Object[] oa = d.toArray();
        check(Arrays.equals(oa, new Object[]{1, 2, 3}), "toArray() order");

        Integer[] exact = d.toArray(new Integer[0]);
        check(exact.length == 3 && Arrays.equals(exact, new Integer[]{1, 2, 3}), "toArray(new T[0]) exact");

        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = d.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        check(Arrays.equals(Arrays.copyOf(reusable, 3), new Integer[]{1, 2, 3}), "toArray(T[]) content");
    }

    static void testClone() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        ArrayDeque<Integer> clone = d.clone();
        check(clone != d, "clone distinct object");
        check(Arrays.equals(clone.toArray(), d.toArray()), "clone content equal");
        clone.addLast(4);
        check(d.size() == 3 && clone.size() == 4, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        ArrayDeque<Integer> r = roundTrip(d);
        check(r != d, "round-trip distinct object");
        check(Arrays.equals(r.toArray(), new Object[]{1, 2, 3}), "round-trip order preserved");
        r.addLast(4);
        check(r.size() == 4 && d.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayDeque.class);
        check(osc.getSerialVersionUID() == 2340985798034038923L, "ArrayDeque serialVersionUID stable");
    }

    static void testGrowth() {
        ArrayDeque<Integer> d = new ArrayDeque<>(8);
        for (int i = 0; i < 100; i++) d.addLast(i);
        check(d.size() == 100, "size after growth");
        check(d.peekFirst() == 0 && d.peekLast() == 99, "growth preserves order");
        for (int i = 0; i < 100; i++) check(d.pollFirst() == i, "poll order after growth");
        check(d.isEmpty(), "drained after growth");
    }

    static void testStateInvariants() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        d.removeFirstOccurrence(3);
        check(d.size() == 4 && !d.contains(3), "remove updates size");
        check(d.contains(1) && d.contains(2) && d.contains(4) && d.contains(5), "others survive remove");
        checkThrows(NullPointerException.class, () -> d.add(null), "failed add leaves state intact");
        check(d.size() == 4, "state intact after failed op");
        d.clear();
        check(d.isEmpty() && d.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!d.contains(i), "clear removed " + i);
    }

    public static void main(String[] args) throws Exception {
        testQueue();
        testDeque();
        testNullRejection();
        testConstructorMatrix();
        testEmptySemantics();
        testPushPopAndOccurrences();
        testIterators();
        testFailFast();
        testToArrayContract();
        testClone();
        testSerialization();
        testGrowth();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
