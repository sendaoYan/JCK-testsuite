/*
 * @test
 * @summary java.util.ListIterator API conformance: index bookkeeping (nextIndex/previousIndex),
 *          bidirectional traversal with boundary NoSuchElementException, set/add/remove on the
 *          last-returned element, and IllegalStateException when there is no last-returned
 *          element (before next/previous, after add, or after remove).
 * @run main ListIteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ListIteratorTest {
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

    static void testIndexes() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(!it.hasPrevious() && it.hasNext(), "initial position");
        check(it.nextIndex() == 0 && it.previousIndex() == -1, "indexes at start");
        check("a".equals(it.next()), "next");
        check(it.nextIndex() == 1 && it.previousIndex() == 0, "indexes after next");
        check("a".equals(it.previous()), "previous");
        check(it.previousIndex() == -1, "previousIndex after previous");
    }

    static void testBidirectionalBounds() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(it.next().equals("a") && it.next().equals("b") && it.next().equals("c"),
                "forward to end");
        check(!it.hasNext() && it.hasPrevious(), "at end");
        expectThrows(NoSuchElementException.class, it::next, "next past end NSEE");
        check("c".equals(it.previous()), "previous from end");

        ListIterator<String> it2 = l.listIterator();
        expectThrows(NoSuchElementException.class, it2::previous, "previous before start NSEE");
    }

    static void testSetAddRemove() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        it.next();
        it.next();
        it.set("B");
        check(l.get(1).equals("B"), "set replaces last-returned");
        it.add("x");
        check(l.equals(Arrays.asList("a", "B", "x", "c")), "add inserts before cursor");
        check("x".equals(it.previous()), "previous returns inserted element");
        it.remove();
        check(l.equals(Arrays.asList("a", "B", "c")), "remove removes last-returned");
    }

    static void testIllegalState() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        expectThrows(IllegalStateException.class, () -> it.set("z"), "set before next ISE");
        expectThrows(IllegalStateException.class, it::remove, "remove before next ISE");

        it.next();
        it.add("x");
        expectThrows(IllegalStateException.class, () -> it.set("z"), "set after add ISE");
        expectThrows(IllegalStateException.class, it::remove, "remove after add ISE");

        it.previous(); // returns the inserted "x"
        it.remove();
        expectThrows(IllegalStateException.class, it::remove, "double remove ISE");
    }

    static void testListIteratorIndex() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> mid = l.listIterator(1);
        check(mid.nextIndex() == 1 && mid.previousIndex() == 0, "listIterator(index) position");
        check("b".equals(mid.next()), "listIterator(index) next");
    }

    // ---- deepened contract coverage II ---------------------------------------

    static void testAlternatingTraversal() {
        // next/previous pairs: alternation always returns the same element pair
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(it.next().equals("a") && it.previous().equals("a"), "next then previous returns same");
        check(it.next().equals("a") && it.next().equals("b"), "forward again");
        check(it.previous().equals("b") && it.next().equals("b"), "back-and-forth at middle");
        check(it.next().equals("c") && !it.hasNext(), "reach end");
        check(it.previous().equals("c") && it.previous().equals("b") && it.previous().equals("a"),
              "walk fully back");
        check(!it.hasPrevious(), "back at start");
        // cursor never moves on failed operations
        ListIterator<String> it2 = l.listIterator(1);
        try { it2.previous(); } catch (Throwable t) { /* not expected here */ }
        check(it2.nextIndex() == 0 && it2.previousIndex() == -1, "cursor after previous to start");
    }

    static void testAddVisibilityMatrix() {
        // after add at cursor k: new element visible to previous(), invisible to next();
        // nextIndex advanced by one
        List<String> l = new ArrayList<>(Arrays.asList("a", "c"));
        ListIterator<String> it = l.listIterator(1);
        it.add("B");
        check(l.equals(Arrays.asList("a", "B", "c")), "add inserted at cursor");
        check(it.nextIndex() == 2 && it.previousIndex() == 1, "cursor shifted right by add");
        check(it.next().equals("c"), "next skips the inserted element");
        check(it.previous().equals("c") && it.previous().equals("B"),
              "previous walks back over inserted element");
        // set after that previous is legal and replaces "B"
        it.set("Z");
        check(l.equals(Arrays.asList("a", "Z", "c")), "set after previous over inserted");
        // add at head/tail
        ListIterator<String> head = l.listIterator();
        head.add("0");
        check(l.get(0).equals("0"), "add at head");
        ListIterator<String> tail = l.listIterator(l.size());
        tail.add("9");
        check(l.get(l.size() - 1).equals("9"), "add at tail");
    }

    static void testSetRemoveStateMatrix() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        // set returns void but the change is visible immediately
        it.next(); // a
        it.set("A");
        check(l.get(0).equals("A"), "set visible immediately");
        it.remove();
        check(l.equals(Arrays.asList("b", "c")), "remove deletes last-returned");
        check(it.nextIndex() == 0, "cursor moved back after remove");
        // remove after previous removes the previously-returned element
        it.next(); // b
        it.previous(); // b
        it.remove();
        check(l.equals(Arrays.asList("c")), "remove after previous");
        // remove shrinks subsequent indexes
        check(it.nextIndex() == 0 && it.previousIndex() == -1, "indexes after removal at head");
        // null elements are settable
        it.next();
        it.set(null);
        check(l.get(0) == null, "set to null");
        it.set("back");
        check(l.get(0).equals("back"), "set back from null");
    }

    static void testForEachRemaining() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        ListIterator<String> it = l.listIterator(1);
        StringBuilder sb = new StringBuilder();
        it.forEachRemaining(sb::append);
        check(sb.toString().equals("bcd"), "forEachRemaining from cursor 1");
        check(!it.hasNext(), "exhausted after forEachRemaining");
        // still can walk back
        check(it.previous().equals("d"), "previous still works after forEachRemaining");
        // forEachRemaining on fresh iterator visits everything
        StringBuilder sb2 = new StringBuilder();
        l.listIterator().forEachRemaining(sb2::append);
        check(sb2.toString().equals("abcd"), "forEachRemaining from start");
    }

    static void testIndexConstructorBounds() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        check(l.listIterator(0).nextIndex() == 0, "listIterator(0)");
        check(l.listIterator(l.size()).previousIndex() == l.size() - 1, "listIterator(size) at tail");
        expectThrows(IndexOutOfBoundsException.class, () -> l.listIterator(-1), "listIterator(-1) IOOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> l.listIterator(3), "listIterator(size+1) IOOBE");
    }

    static void testConcurrentModification() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        it.next();
        l.add("d");
        expectThrows(java.util.ConcurrentModificationException.class, it::next,
                     "listIterator next after structural mod CME");
        List<String> l2 = new ArrayList<>(Arrays.asList("a", "b"));
        ListIterator<String> it2 = l2.listIterator();
        it2.next();
        l2.remove(0);
        expectThrows(java.util.ConcurrentModificationException.class, it2::previous,
                     "listIterator previous after structural mod CME");
        // set() on a stale iterator may also detect CME (fail-fast family); not asserted
        // iterator's own add/remove are legal during iteration
        List<String> l3 = new ArrayList<>(Arrays.asList("a", "b"));
        ListIterator<String> it3 = l3.listIterator();
        it3.next();
        it3.add("X");
        expectThrows(IllegalStateException.class, it3::remove, "remove right after add ISE");
        it3.previous(); // "X"
        it3.remove();
        check(l3.equals(Arrays.asList("a", "b")), "own-modification path leaves consistent state");
    }

    public static void main(String[] args) {
        testIndexes();
        testBidirectionalBounds();
        testSetAddRemove();
        testIllegalState();
        testListIteratorIndex();
        testAlternatingTraversal();
        testAddVisibilityMatrix();
        testSetRemoveStateMatrix();
        testForEachRemaining();
        testIndexConstructorBounds();
        testConcurrentModification();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
