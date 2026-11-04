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
import java.util.Collections;
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

    // ---- fail-fast matrix (probe-locked JDK-8 semantics) ---------------------
    // The ascending iterator fences on tail, the descending iterator fences on
    // head, so each is invalidated by mutations at the opposite end only.

    static boolean cme(boolean descending, java.util.function.Consumer<ArrayDeque<Integer>> mut) {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        try {
            Iterator<Integer> it = descending ? d.descendingIterator() : d.iterator();
            it.next();
            mut.accept(d);
            it.next();
            return false;
        } catch (java.util.ConcurrentModificationException e) {
            return true;
        }
    }

    static void testFailFastMatrix() {
        check(cme(false, q -> q.addLast(9)), "JDK-8: asc CME after addLast");
        check(!cme(false, q -> q.addFirst(0)), "JDK-8: asc survives addFirst (head-side fence)");
        check(cme(false, q -> q.removeLast()), "JDK-8: asc CME after removeLast");
        check(cme(true, q -> q.addFirst(0)), "JDK-8: desc CME after addFirst");
        check(!cme(true, q -> q.addLast(9)), "JDK-8: desc survives addLast (tail-side fence)");
        check(cme(true, q -> q.removeFirst()), "JDK-8: desc CME after removeFirst");
        check(!cme(true, q -> q.removeLast()), "JDK-8: desc survives removeLast");
    }

    // ---- contract deep-dive: identity equals / toString / bulk ops ----------
    // ---- / spliterator / iterator remove / wrap serialization / growth ------

    static void testEqualsHashCodeIdentity() {
        // AbstractCollection does not override equals/hashCode: identity semantics.
        ArrayDeque<Integer> a = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        ArrayDeque<Integer> b = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        check(!a.equals(b), "same-content ArrayDeques are not equals (identity semantics)");
        check(a.equals(a), "self equals");
        check(a.hashCode() == System.identityHashCode(a),
                "hashCode is Object identity hashCode (not overridden)");
        List<Integer> sameContent = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(!a.equals(sameContent), "never equals a content-equal List (no equals override)");
        check(!sameContent.equals(a), "List side is also false (asymmetric types)");
    }

    static void testToString() {
        check("[1, 2, 3]".equals(new ArrayDeque<>(Arrays.asList(1, 2, 3)).toString()),
                "toString head-to-tail [1, 2, 3]");
        check("[]".equals(new ArrayDeque<>().toString()), "empty toString []");
        check("[a, b]".equals(new ArrayDeque<>(Arrays.asList("a", "b")).toString()),
                "toString strings");
    }

    static void testBulkOperations() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(d.containsAll(Arrays.asList(2, 4)), "containsAll true");
        check(!d.containsAll(Arrays.asList(2, 9)), "containsAll false");
        check(d.removeAll(Arrays.asList(2, 4)), "removeAll returns true");
        check(Arrays.equals(d.toArray(), new Object[]{1, 3, 5}), "removeAll preserves order");
        check(!d.removeAll(Arrays.asList(9)), "removeAll no-match false");
        check(!d.removeAll(Collections.<Integer>emptyList()), "removeAll empty false");

        ArrayDeque<Integer> r = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(r.retainAll(Arrays.asList(2, 4)), "retainAll returns true");
        check(Arrays.equals(r.toArray(), new Object[]{2, 4}), "retainAll preserves order");
        check(!r.retainAll(Arrays.asList(2, 4)), "retainAll no-op false");

        ArrayDeque<Integer> f = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(f.removeIf(x -> x % 2 == 0), "removeIf returns true");
        check(Arrays.equals(f.toArray(), new Object[]{1, 3, 5}), "removeIf removes evens in place");
        check(!f.removeIf(x -> x > 100), "removeIf no-match false");
    }

    static void testForEachOrder() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        List<Integer> seen = new ArrayList<>();
        d.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3, 4)), "forEach encounter order head-to-tail");
    }

    static void testSpliterator() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(3, 1, 2));
        java.util.Spliterator<Integer> sp = d.spliterator();
        check(sp.estimateSize() == 3, "estimateSize == size");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED
                        | java.util.Spliterator.SIZED
                        | java.util.Spliterator.SUBSIZED
                        | java.util.Spliterator.NONNULL),
                "characteristics ORDERED|SIZED|SUBSIZED|NONNULL");

        // forEachRemaining walks in encounter order.
        List<Integer> walked = new ArrayList<>();
        sp.forEachRemaining(walked::add);
        check(walked.equals(Arrays.asList(3, 1, 2)), "spliterator encounter order");

        // trySplit yields a prefix and a suffix whose concatenation is the deque.
        ArrayDeque<Integer> d4 = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        java.util.Spliterator<Integer> whole = d4.spliterator();
        java.util.Spliterator<Integer> front = whole.trySplit();
        check(front != null, "trySplit splits SIZED source");
        check(front.getExactSizeIfKnown() + whole.getExactSizeIfKnown() == 4,
                "split sizes sum to original");
        List<Integer> fl = new ArrayList<>(), bl = new ArrayList<>();
        front.forEachRemaining(fl::add);
        whole.forEachRemaining(bl::add);
        List<Integer> joined = new ArrayList<>(fl);
        joined.addAll(bl);
        check(joined.equals(Arrays.asList(1, 2, 3, 4)), "split prefix+suffix equals original");
        check(fl.size() == 2 && bl.size() == 2, "split halves sized evenly");

        java.util.Spliterator<Integer> one = new ArrayDeque<Integer>(Arrays.asList(5)).spliterator();
        check(one.tryAdvance(x -> check(x == 5, "tryAdvance visits element")), "tryAdvance true");
        check(!one.tryAdvance(x -> { }), "tryAdvance exhausted false");
    }

    static void testIteratorRemoveSemantics() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        it.next();
        it.remove();
        check(Arrays.equals(d.toArray(), new Object[]{1, 3}), "iterator.remove removes current");
        checkThrows(IllegalStateException.class, it::remove, "second remove without next ISE");

        ArrayDeque<Integer> d2 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it2 = d2.iterator();
        checkThrows(IllegalStateException.class, it2::remove, "remove before first next ISE");

        // descendingIterator supports remove and it removes the tail-most visited.
        ArrayDeque<Integer> d3 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> desc = d3.descendingIterator();
        checkThrows(IllegalStateException.class, desc::remove, "descending remove before next ISE");
        desc.next();   // 3
        desc.remove();
        check(Arrays.equals(d3.toArray(), new Object[]{1, 2}), "descendingIterator.remove drops tail");
        check(desc.next() == 2 && desc.next() == 1 && !desc.hasNext(),
                "descending iteration continues after remove");

        // Live filtering with iterator.remove keeps order.
        ArrayDeque<Integer> d4 = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        for (Iterator<Integer> i = d4.iterator(); i.hasNext(); )
            if (i.next() % 2 == 0) i.remove();
        check(Arrays.equals(d4.toArray(), new Object[]{1, 3}), "live filter during iteration");
    }

    static void testWrapAroundSerialization() throws Exception {
        // Build a deque whose ring buffer has wrapped: add 6, drain 4, add 4 more.
        ArrayDeque<Integer> w = new ArrayDeque<>(4);
        for (int i = 0; i < 6; i++) w.addLast(i);
        for (int i = 0; i < 4; i++) w.pollFirst();
        for (int i = 6; i < 10; i++) w.addLast(i);
        check(Arrays.equals(w.toArray(), new Object[]{4, 5, 6, 7, 8, 9}),
                "wrapped deque logical order");
        ArrayDeque<Integer> r = roundTrip(w);
        check(Arrays.equals(r.toArray(), new Object[]{4, 5, 6, 7, 8, 9}),
                "round-trip preserves wrapped order");
        check(r.peekFirst() == 4 && r.peekLast() == 9, "round-trip ends intact");
    }

    static void testMixedGrowth() {
        ArrayDeque<Integer> d = new ArrayDeque<>(4);
        for (int i = 0; i < 300; i++) d.addFirst(i);
        for (int i = 0; i < 300; i++) d.addLast(1000 + i);
        check(d.size() == 600, "mixed growth size");
        check(d.peekFirst() == 299, "head is last addFirst");
        check(d.peekLast() == 1299, "tail is last addLast");
        // Drain from both ends and verify exact pairing.
        for (int i = 299; i >= 0; i--) check(d.pollFirst() == i, "head drain " + i);
        for (int i = 1000; i < 1300; i++) check(d.pollLast() == 1299 - (i - 1000), "tail drain");
        check(d.isEmpty(), "mixed growth drained");
    }

    static void testToArrayExactSize() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Integer[] exact = new Integer[3];
        Integer[] ret = d.toArray(exact);
        check(ret == exact, "exact-size array is reused");
        check(ret[0] == 1 && ret[1] == 2 && ret[2] == 3, "exact-size content");
    }

    static void testDuplicateRemoval() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 1));
        check(d.remove(Integer.valueOf(1)), "remove first occurrence of duplicate");
        check(Arrays.equals(d.toArray(), new Object[]{2, 1}), "later duplicate survives");
        check(d.contains(1), "duplicate still contained");
        check(d.size() == 2, "size after duplicate removal");
    }

    static void testCloneSharesElements() {
        Object shared = new Object();
        ArrayDeque<Object> d = new ArrayDeque<>();
        d.addLast(shared);
        ArrayDeque<Object> c = d.clone();
        check(c.getFirst() == shared, "clone is shallow: same element instance");
        check(c.contains(shared), "clone contains same instance");
        c.clear();
        check(d.size() == 1, "clone clear does not touch original");
    }

    static void testAddAllContract() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1));
        check(d.addAll(Arrays.asList(2, 3)), "addAll returns true");
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3}), "addAll appends at tail in order");
        check(!d.addAll(Collections.<Integer>emptyList()), "addAll empty returns false");
        checkThrows(NullPointerException.class, () -> d.addAll(Arrays.asList(4, null)),
                "addAll with null element NPE (relay)");
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3, 4}),
                "null rejected mid-addAll: elements before the null were added");
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
        testFailFastMatrix();
        testEqualsHashCodeIdentity();
        testToString();
        testBulkOperations();
        testForEachOrder();
        testSpliterator();
        testIteratorRemoveSemantics();
        testWrapAroundSerialization();
        testMixedGrowth();
        testToArrayExactSize();
        testDuplicateRemoval();
        testCloneSharesElements();
        testAddAllContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
