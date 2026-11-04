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

    // ---- contract deep-dive: 12-method head/tail matrix ---------------------

    static void testTwelveMethodMatrix() {
        Deque<Integer> d = new ArrayDeque<>();
        d.addLast(1);
        d.addLast(2);
        d.addLast(3);
        check(d.getFirst() == 1 && d.getLast() == 3, "getFirst/getLast on 3 elems");
        check(d.peekFirst() == 1 && d.peekLast() == 3, "peekFirst/peekLast on 3 elems");
        check(d.offerFirst(0) && d.offerLast(4), "offerFirst/offerLast return true");
        check(d.getFirst() == 0 && d.getLast() == 4, "positions after offers");
        check(d.size() == 5, "size 5");
        check(d.removeFirst() == 0 && d.removeLast() == 4, "removeFirst/removeLast");
        check(d.pollFirst() == 1 && d.pollLast() == 3, "pollFirst/pollLast");
        check(d.size() == 1 && d.getFirst() == 2, "middle element is now both ends");

        // Single element: first == last for all four accessors.
        Deque<Integer> s = new ArrayDeque<>();
        s.addLast(7);
        check(s.getFirst() == 7 && s.getLast() == 7, "single: getFirst==getLast");
        check(s.peekFirst() == 7 && s.peekLast() == 7, "single: peekFirst==peekLast");
        check(s.removeLast() == 7 && s.isEmpty(), "single drained from tail");

        // Empty state: throw vs null split.
        Deque<Integer> e = new ArrayDeque<>();
        check(e.peekFirst() == null && e.peekLast() == null, "empty peek null");
        check(e.pollFirst() == null && e.pollLast() == null, "empty poll null");
        expectThrows(NoSuchElementException.class, e::removeFirst, "empty removeFirst NSEE");
        expectThrows(NoSuchElementException.class, e::removeLast, "empty removeLast NSEE");
        expectThrows(NoSuchElementException.class, e::getFirst, "empty getFirst NSEE");
        expectThrows(NoSuchElementException.class, e::getLast, "empty getLast NSEE");
    }

    static void testQueueViewAliasing() {
        // add(e) == addLast(e); element() == getFirst(); peek() == peekFirst();
        // remove() == removeFirst(); poll() == pollFirst(); offer() == offerLast().
        Deque<String> d1 = new ArrayDeque<>();
        d1.add("m");
        check("m".equals(d1.peekLast()) && d1.size() == 1, "add == addLast");

        Deque<String> d2 = new ArrayDeque<>(Arrays.asList("a", "b"));
        d2.offer("c");
        check("c".equals(d2.getLast()), "offer == offerLast");

        Deque<String> d3 = new ArrayDeque<>(Arrays.asList("a", "b"));
        check(d3.element().equals(d3.getFirst()), "element == getFirst value");
        check(d3.peek().equals(d3.peekFirst()), "peek == peekFirst value");

        Deque<String> d4 = new ArrayDeque<>(Arrays.asList("a", "b"));
        check(d4.remove().equals("a"), "remove returns head");
        check(d4.peek().equals("b"), "remove == removeFirst semantics");

        Deque<String> d5 = new ArrayDeque<>(Arrays.asList("a", "b"));
        check(d5.poll().equals("a") && "b".equals(d5.peekFirst()), "poll == pollFirst semantics");

        // Stack aliasing: push == addFirst, pop == removeFirst.
        Deque<String> d6 = new ArrayDeque<>();
        d6.push("x");
        d6.addFirst("front");
        check("front".equals(d6.peekFirst()), "push/addFirst both prepend");
        check("front".equals(d6.pop()), "pop removes what addFirst added");
        check("x".equals(d6.pop()), "pop LIFO order");
    }

    static void testOccurrencesDeep() {
        Deque<String> d = new ArrayDeque<>(Arrays.asList("a", "b", "a", "c", "a"));
        check(d.removeFirstOccurrence("a"), "removeFirstOccurrence true");
        check(new ArrayList<>(d).equals(Arrays.asList("b", "a", "c", "a")),
                "first of three occurrences removed");
        check(d.removeLastOccurrence("a"), "removeLastOccurrence true");
        check(new ArrayList<>(d).equals(Arrays.asList("b", "a", "c")), "last occurrence removed");
        check(d.removeLastOccurrence("a"), "another last occurrence");
        check(new ArrayList<>(d).equals(Arrays.asList("b", "c")), "only one left");
        check(!d.removeFirstOccurrence("a") && !d.removeLastOccurrence("a"),
                "missing returns false both directions");
        check(!d.removeFirstOccurrence(null) && !d.removeLastOccurrence(null),
                "null occurrence returns false (no NPE)");
    }

    static void testBulkOperations() {
        Deque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(d.containsAll(Arrays.asList(2, 4)), "containsAll subset true");
        check(!d.containsAll(Arrays.asList(2, 9)), "containsAll false");
        check(d.removeAll(Arrays.asList(2, 4)), "removeAll changed");
        check(new ArrayList<>(d).equals(Arrays.asList(1, 3, 5)), "removeAll preserves order");
        check(!d.removeAll(Arrays.asList(9)), "removeAll no-match false");
        Deque<Integer> d2 = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(d2.retainAll(Arrays.asList(2, 4)), "retainAll changed");
        check(new ArrayList<>(d2).equals(Arrays.asList(2, 4)), "retainAll preserves order");
        check(!d2.retainAll(Arrays.asList(2, 4)), "retainAll no-op false");
        Deque<Integer> d3 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        d3.addAll(Arrays.asList(4, 5));
        check(new ArrayList<>(d3).equals(Arrays.asList(1, 2, 3, 4, 5)), "addAll appends in order");
    }

    static void testToArrayContract() {
        Deque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3}), "toArray head-to-tail");
        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = d.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses when it fits");
        check(reusable[3] == null, "toArray(T[]) null terminator after content");
        Integer[] exact = d.toArray(new Integer[3]);
        check(exact.length == 3 && exact[2] == 3 && returned == reusable, "exact-size array content");
        Deque<Integer> empty = new ArrayDeque<>();
        check(empty.toArray().length == 0, "empty toArray length 0");
    }

    static void testIteratorRemoveSemantics() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        it.next();
        it.remove();
        check(new ArrayList<>(d).equals(Arrays.asList(1, 3)), "iterator.remove removes current");
        expectThrows(IllegalStateException.class, it::remove,
                "double remove without next ISE");

        ArrayDeque<Integer> d2 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it2 = d2.iterator();
        expectThrows(IllegalStateException.class, it2::remove,
                "remove before next ISE");

        ArrayDeque<Integer> d3 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> desc = d3.descendingIterator();
        expectThrows(IllegalStateException.class, desc::remove,
                "descending remove before next ISE");
        desc.next();           // 3
        desc.remove();         // removes tail 3
        check(new ArrayList<>(d3).equals(Arrays.asList(1, 2)), "descendingIterator.remove removes tail");
        check(desc.next() == 2 && desc.next() == 1, "descending iteration continues after remove");
    }

    static void testFailFastCme() {
        Deque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        d.addLast(99);
        expectThrows(java.util.ConcurrentModificationException.class, it::next,
                "ascending CME after tail add");

        Deque<Integer> d2 = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> desc = d2.descendingIterator();
        desc.next();
        d2.addFirst(0);
        expectThrows(java.util.ConcurrentModificationException.class, desc::next,
                "descending CME after head add");

        // iterator.remove is the only legal mutation during iteration.
        Deque<Integer> d3 = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        Iterator<Integer> it3 = d3.iterator();
        while (it3.hasNext())
            if (it3.next() == 2) it3.remove();
        check(new ArrayList<>(d3).equals(Arrays.asList(1, 3, 4)),
                "live removal during iteration");
    }

    static void testNullMatrix() {
        Deque<String> d = new ArrayDeque<>();
        expectThrows(NullPointerException.class, () -> d.addFirst(null), "addFirst(null) NPE");
        expectThrows(NullPointerException.class, () -> d.addLast(null), "addLast(null) NPE");
        expectThrows(NullPointerException.class, () -> d.offerFirst(null), "offerFirst(null) NPE");
        expectThrows(NullPointerException.class, () -> d.offerLast(null), "offerLast(null) NPE");
        expectThrows(NullPointerException.class, () -> d.push(null), "push(null) NPE");
        expectThrows(NullPointerException.class, () -> d.add(null), "add(null) NPE");
        expectThrows(NullPointerException.class, () -> d.offer(null), "offer(null) NPE");
        d.add("kept");
        check(!d.contains(null), "contains(null) false");
        check(!d.remove(null), "remove(null) false");
        check(d.size() == 1, "state intact after null rejections");
    }

    static void testAlternatingDrain() {
        Deque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4));
        check(d.pollFirst() == 1, "alt drain: pollFirst");
        check(d.pollLast() == 4, "alt drain: pollLast");
        check(d.removeFirst() == 2, "alt drain: removeFirst");
        check(d.removeLast() == 3, "alt drain: removeLast");
        check(d.isEmpty(), "alt drain: empty");
    }

    public static void main(String[] args) {
        testAddGet();
        testRemovePoll();
        testStackAndOccurrence();
        testIterators();
        testTwelveMethodMatrix();
        testQueueViewAliasing();
        testOccurrencesDeep();
        testBulkOperations();
        testToArrayContract();
        testIteratorRemoveSemantics();
        testFailFastCme();
        testNullMatrix();
        testAlternatingDrain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
