/*
 * @test
 * @summary java.util.Deque API conformance: addFirst/addLast/offerFirst/offerLast, getFirst/
 *          getLast/peekFirst/peekLast, removeFirst/removeLast/pollFirst/pollLast, push/pop (LIFO),
 *          removeFirstOccurrence/removeLastOccurrence, ascending/descending iterators, and
 *          null-argument NPE plus empty-deque NoSuchElementException.
 * @run main DequeTest
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DequeTest {
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

    static void testAddGet() {
        Deque<String> d = new ArrayDeque<>();
        d.addFirst("a");
        d.addLast("b");
        check("a".equals(d.getFirst()) && "b".equals(d.getLast()), "getFirst/getLast");
        check("a".equals(d.peekFirst()) && "b".equals(d.peekLast()), "peekFirst/peekLast");
        check(d.offerFirst("x") && d.offerLast("y"), "offerFirst/offerLast return true");
        check("x".equals(d.getFirst()) && "y".equals(d.getLast()), "offerFirst/offerLast position");
        expectThrows(NullPointerException.class, () -> d.addFirst(null), "addFirst(null) NPE");
        expectThrows(NullPointerException.class, () -> d.offerLast(null), "offerLast(null) NPE");
    }

    static void testRemovePoll() {
        Deque<String> d = new ArrayDeque<>(Arrays.asList("a", "b", "c", "d"));
        check("a".equals(d.removeFirst()), "removeFirst");
        check("d".equals(d.removeLast()), "removeLast");
        check("b".equals(d.pollFirst()), "pollFirst");
        check("c".equals(d.pollLast()), "pollLast");
        check(d.isEmpty(), "emptied");
        check(d.pollFirst() == null && d.pollLast() == null, "poll null on empty");
        expectThrows(NoSuchElementException.class, d::removeFirst, "removeFirst on empty NSEE");
        expectThrows(NoSuchElementException.class, d::removeLast, "removeLast on empty NSEE");
        expectThrows(NoSuchElementException.class, d::getFirst, "getFirst on empty NSEE");
        expectThrows(NoSuchElementException.class, d::getLast, "getLast on empty NSEE");
    }

    static void testStackAndOccurrence() {
        Deque<String> d = new ArrayDeque<>();
        d.push("top");
        d.push("bottom");
        check("bottom".equals(d.peekFirst()), "push adds to head");
        check("bottom".equals(d.pop()), "pop LIFO");
        check("top".equals(d.pop()), "pop again LIFO");
        expectThrows(NoSuchElementException.class, d::pop, "pop on empty NSEE");

        Deque<String> d2 = new ArrayDeque<>(Arrays.asList("a", "b", "a", "c"));
        check(d2.removeFirstOccurrence("a"), "removeFirstOccurrence returns true");
        check(new ArrayList<>(d2).equals(Arrays.asList("b", "a", "c")), "removeFirstOccurrence removes first");
        check(d2.removeLastOccurrence("a"), "removeLastOccurrence returns true");
        check(new ArrayList<>(d2).equals(Arrays.asList("b", "c")), "removeLastOccurrence removes last");
        check(!d2.removeFirstOccurrence("z"), "removeFirstOccurrence missing returns false");
        check(!d2.removeFirstOccurrence(null), "removeFirstOccurrence(null) returns false");
    }

    static void testIterators() {
        Deque<String> d = new ArrayDeque<>(Arrays.asList("1", "2", "3"));
        Iterator<String> asc = d.iterator();
        check("1".equals(asc.next()) && "2".equals(asc.next()) && "3".equals(asc.next()),
                "iterator ascending");
        Iterator<String> desc = d.descendingIterator();
        check("3".equals(desc.next()) && "2".equals(desc.next()) && "1".equals(desc.next()),
                "descendingIterator descending");
        check(d.contains("2") && d.size() == 3, "contains/size");
        check(d.remove("2") && !d.contains("2"), "remove(Object)");
        check("1".equals(d.element()), "element (Queue) returns head");
        check("1".equals(d.peek()), "peek (Queue) returns head");
    }

    public static void main(String[] args) {
        testAddGet();
        testRemovePoll();
        testStackAndOccurrence();
        testIterators();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
