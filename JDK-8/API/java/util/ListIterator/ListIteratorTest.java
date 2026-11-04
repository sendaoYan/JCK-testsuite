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

    public static void main(String[] args) {
        testIndexes();
        testBidirectionalBounds();
        testSetAddRemove();
        testIllegalState();
        testListIteratorIndex();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
