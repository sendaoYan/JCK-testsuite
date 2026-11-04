/*
 * @test
 * @summary java.util.Deque conformance: white-box structure + black-box behavior.
 * @run main DequeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class DequeTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Deque");
        check(c.isInterface(), "is an interface");
        check(Queue.class.isAssignableFrom(c), "extends Queue");
        check(java.util.SequencedCollection.class.isAssignableFrom(c), "extends SequencedCollection");
        for (String m : new String[] {"addFirst", "addLast", "offerFirst", "offerLast",
                "removeFirstOccurrence", "removeLastOccurrence", "push"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"removeFirst", "removeLast", "pollFirst", "pollLast",
                "getFirst", "getLast", "peekFirst", "peekLast", "pop", "descendingIterator"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed default");
        check(Modifier.isAbstract(c.getDeclaredMethod("addFirst", Object.class).getModifiers()),
                "addFirst abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. head/tail add + get + peek
        Deque<String> d = new ArrayDeque<>();
        d.addFirst("a");
        d.addLast("b");
        check("a".equals(d.getFirst()) && "b".equals(d.getLast()), "getFirst/getLast");
        check("a".equals(d.peekFirst()) && "b".equals(d.peekLast()), "peekFirst/peekLast");
        d.offerFirst("x");
        d.offerLast("y");
        check("x".equals(d.getFirst()) && "y".equals(d.getLast()), "offerFirst/offerLast");
        expectThrows(() -> d.addFirst(null), NullPointerException.class, "addFirst(null) NPE");
        expectThrows(() -> d.offerLast(null), NullPointerException.class, "offerLast(null) NPE");

        // 2. remove/poll head+tail
        check("x".equals(d.removeFirst()), "removeFirst");
        check("y".equals(d.removeLast()), "removeLast");
        check("a".equals(d.pollFirst()), "pollFirst");
        check("b".equals(d.pollLast()), "pollLast");
        check(d.isEmpty(), "emptied");
        check(d.pollFirst() == null && d.pollLast() == null, "poll null on empty");
        expectThrows(d::removeFirst, NoSuchElementException.class, "removeFirst on empty");
        expectThrows(d::getFirst, NoSuchElementException.class, "getFirst on empty");

        // 3. push/pop LIFO
        d.push("top");
        d.push("bottom");
        check("bottom".equals(d.pop()), "pop returns LIFO head");
        check("top".equals(d.peekFirst()), "push adds to head");

        // 4. removeFirstOccurrence/removeLastOccurrence
        Deque<String> d2 = new ArrayDeque<>(List.of("a", "b", "a", "c"));
        check(d2.removeFirstOccurrence("a"), "removeFirstOccurrence returns true");
        check(new ArrayList<>(d2).equals(List.of("b", "a", "c")), "removeFirstOccurrence removes first");
        check(d2.removeLastOccurrence("a"), "removeLastOccurrence returns true");
        check(new ArrayList<>(d2).equals(List.of("b", "c")), "removeLastOccurrence removes last");
        check(!d2.removeFirstOccurrence("z"), "removeFirstOccurrence missing returns false");

        // 5. descendingIterator
        Deque<String> d3 = new ArrayDeque<>(List.of("1", "2", "3"));
        Iterator<String> desc = d3.descendingIterator();
        StringBuilder sb = new StringBuilder();
        while (desc.hasNext()) sb.append(desc.next());
        check("321".contentEquals(sb), "descendingIterator reverse order");

        // 6. reversed() view
        Deque<String> d4 = new ArrayDeque<>(List.of("1", "2", "3"));
        Deque<String> rev = d4.reversed();
        check("3".equals(rev.getFirst()) && "1".equals(rev.getLast()), "reversed head/tail swapped");

        // 7. contains/size/remove(Object) via Collection contract
        Deque<String> d5 = new ArrayDeque<>(List.of("a", "b"));
        check(d5.contains("a") && d5.size() == 2, "contains/size");
        check(d5.remove("a") && !d5.contains("a"), "remove(Object)");
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

        Deque<Integer> s = new ArrayDeque<>();
        s.addLast(7);
        check(s.getFirst() == 7 && s.getLast() == 7, "single: getFirst==getLast");
        check(s.peekFirst() == 7 && s.peekLast() == 7, "single: peekFirst==peekLast");
        check(s.removeLast() == 7 && s.isEmpty(), "single drained from tail");

        Deque<Integer> e = new ArrayDeque<>();
        check(e.peekFirst() == null && e.peekLast() == null, "empty peek null");
        check(e.pollFirst() == null && e.pollLast() == null, "empty poll null");
        expectThrows(e::removeFirst, NoSuchElementException.class, "empty removeFirst NSEE");
        expectThrows(e::removeLast, NoSuchElementException.class, "empty removeLast NSEE");
        expectThrows(e::getFirst, NoSuchElementException.class, "empty getFirst NSEE");
        expectThrows(e::getLast, NoSuchElementException.class, "empty getLast NSEE");
    }

    static void testQueueViewAliasing() {
        // add(e) == addLast(e); element() == getFirst(); peek() == peekFirst();
        // remove() == removeFirst(); poll() == pollFirst(); offer() == offerLast().
        Deque<String> d1 = new ArrayDeque<>();
        d1.add("m");
        check("m".equals(d1.peekLast()) && d1.size() == 1, "add == addLast");

        Deque<String> d2 = new ArrayDeque<>(List.of("a", "b"));
        d2.offer("c");
        check("c".equals(d2.getLast()), "offer == offerLast");

        Deque<String> d3 = new ArrayDeque<>(List.of("a", "b"));
        check(d3.element().equals(d3.getFirst()), "element == getFirst value");
        check(d3.peek().equals(d3.peekFirst()), "peek == peekFirst value");

        Deque<String> d4 = new ArrayDeque<>(List.of("a", "b"));
        check(d4.remove().equals("a"), "remove returns head");
        check(d4.peek().equals("b"), "remove == removeFirst semantics");

        Deque<String> d5 = new ArrayDeque<>(List.of("a", "b"));
        check(d5.poll().equals("a") && "b".equals(d5.peekFirst()), "poll == pollFirst semantics");

        Deque<String> d6 = new ArrayDeque<>();
        d6.push("x");
        d6.addFirst("front");
        check("front".equals(d6.peekFirst()), "push/addFirst both prepend");
        check("front".equals(d6.pop()), "pop removes what addFirst added");
        check("x".equals(d6.pop()), "pop LIFO order");
    }

    static void testOccurrencesDeep() {
        Deque<String> d = new ArrayDeque<>(List.of("a", "b", "a", "c", "a"));
        check(d.removeFirstOccurrence("a"), "removeFirstOccurrence true");
        check(new ArrayList<>(d).equals(List.of("b", "a", "c", "a")),
                "first of three occurrences removed");
        check(d.removeLastOccurrence("a"), "removeLastOccurrence true");
        check(new ArrayList<>(d).equals(List.of("b", "a", "c")), "last occurrence removed");
        check(d.removeLastOccurrence("a"), "another last occurrence");
        check(new ArrayList<>(d).equals(List.of("b", "c")), "only one left");
        check(!d.removeFirstOccurrence("a") && !d.removeLastOccurrence("a"),
                "missing returns false both directions");
        check(!d.removeFirstOccurrence(null) && !d.removeLastOccurrence(null),
                "null occurrence returns false (no NPE)");
    }

    static void testBulkOperations() {
        Deque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3, 4, 5));
        check(d.containsAll(List.of(2, 4)), "containsAll subset true");
        check(!d.containsAll(List.of(2, 9)), "containsAll false");
        check(d.removeAll(List.of(2, 4)), "removeAll changed");
        check(new ArrayList<>(d).equals(List.of(1, 3, 5)), "removeAll preserves order");
        check(!d.removeAll(List.of(9)), "removeAll no-match false");
        Deque<Integer> d2 = new ArrayDeque<>(List.of(1, 2, 3, 4, 5));
        check(d2.retainAll(List.of(2, 4)), "retainAll changed");
        check(new ArrayList<>(d2).equals(List.of(2, 4)), "retainAll preserves order");
        check(!d2.retainAll(List.of(2, 4)), "retainAll no-op false");
        Deque<Integer> d3 = new ArrayDeque<>(List.of(1, 2, 3));
        d3.addAll(List.of(4, 5));
        check(new ArrayList<>(d3).equals(List.of(1, 2, 3, 4, 5)), "addAll appends in order");
    }

    static void testToArrayContract() {
        Deque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3));
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3}), "toArray head-to-tail");
        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = d.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses when it fits");
        check(reusable[3] == null, "toArray(T[]) null terminator after content");
        Integer[] exact = d.toArray(new Integer[3]);
        check(exact.length == 3 && exact[2] == 3, "exact-size array content");
        Deque<Integer> empty = new ArrayDeque<>();
        check(empty.toArray().length == 0, "empty toArray length 0");
    }

    static void testIteratorRemoveSemantics() {
        ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        it.next();
        it.remove();
        check(new ArrayList<>(d).equals(List.of(1, 3)), "iterator.remove removes current");
        expectThrows(it::remove, IllegalStateException.class, "double remove without next ISE");

        ArrayDeque<Integer> d2 = new ArrayDeque<>(List.of(1, 2, 3));
        Iterator<Integer> it2 = d2.iterator();
        expectThrows(it2::remove, IllegalStateException.class, "remove before next ISE");

        ArrayDeque<Integer> d3 = new ArrayDeque<>(List.of(1, 2, 3));
        Iterator<Integer> desc = d3.descendingIterator();
        expectThrows(desc::remove, IllegalStateException.class, "descending remove before next ISE");
        desc.next();           // 3
        desc.remove();         // removes tail 3
        check(new ArrayList<>(d3).equals(List.of(1, 2)), "descendingIterator.remove removes tail");
        check(desc.next() == 2 && desc.next() == 1, "descending iteration continues after remove");
    }

    static void testFailFastCme() {
        Deque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        it.next();
        d.addLast(99);
        expectThrows(it::next, java.util.ConcurrentModificationException.class,
                "ascending CME after tail add");

        // JDK 21+ ArrayDeque: the descending iterator is NOT invalidated by
        // structural mutations at the head (or by removals) — best-effort
        // fail-fast only; this differs from JDK-8 where head mutation threw.
        Deque<Integer> d2 = new ArrayDeque<>(List.of(1, 2, 3));
        Iterator<Integer> desc = d2.descendingIterator();
        desc.next();
        d2.addFirst(0);
        try {
            desc.next();
            check(true, "descending iterator survives head add on JDK-27 (no CME)");
        } catch (java.util.ConcurrentModificationException e) {
            check(false, "descending iterator unexpectedly failed fast after head add");
        }

        Deque<Integer> d3 = new ArrayDeque<>(List.of(1, 2, 3, 4));
        for (Iterator<Integer> i = d3.iterator(); i.hasNext(); )
            if (i.next() == 2) i.remove();
        check(new ArrayList<>(d3).equals(List.of(1, 3, 4)),
                "live removal during iteration");
    }

    static void testNullMatrix() {
        Deque<String> d = new ArrayDeque<>();
        expectThrows(() -> d.addFirst(null), NullPointerException.class, "addFirst(null) NPE");
        expectThrows(() -> d.addLast(null), NullPointerException.class, "addLast(null) NPE");
        expectThrows(() -> d.offerFirst(null), NullPointerException.class, "offerFirst(null) NPE");
        expectThrows(() -> d.offerLast(null), NullPointerException.class, "offerLast(null) NPE");
        expectThrows(() -> d.push(null), NullPointerException.class, "push(null) NPE");
        expectThrows(() -> d.add(null), NullPointerException.class, "add(null) NPE");
        expectThrows(() -> d.offer(null), NullPointerException.class, "offer(null) NPE");
        d.add("kept");
        check(!d.contains(null), "contains(null) false");
        check(!d.remove(null), "remove(null) false");
        check(d.size() == 1, "state intact after null rejections");
    }

    static void testAlternatingDrain() {
        Deque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3, 4));
        check(d.pollFirst() == 1, "alt drain: pollFirst");
        check(d.pollLast() == 4, "alt drain: pollLast");
        check(d.removeFirst() == 2, "alt drain: removeFirst");
        check(d.removeLast() == 3, "alt drain: removeLast");
        check(d.isEmpty(), "alt drain: empty");
    }

    // ---- JDK 21+ SequencedCollection reversed() live view --------------------

    static void testReversedView() {
        ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3));
        Deque<Integer> rev = d.reversed();
        check(new ArrayList<>(rev).equals(List.of(3, 2, 1)), "reversed iteration order");

        // Live view: reversed.addFirst appends to the ORIGINAL tail.
        rev.addFirst(99);
        check(new ArrayList<>(d).equals(List.of(1, 2, 3, 99)),
                "reversed().addFirst maps to original addLast");

        // reversed.pollFirst removes the original tail.
        check(rev.pollFirst() == 99, "reversed pollFirst takes original last");
        check(d.getLast() == 3 && d.size() == 3, "original tail state after reversed poll");

        // reversed.getLast/getFirst mirror original ends.
        check(rev.getFirst() == 3 && rev.getLast() == 1, "reversed ends swapped");

        // double reversal returns to the original order (content).
        Deque<Integer> revrev = rev.reversed();
        check(new ArrayList<>(revrev).equals(new ArrayList<>(d)),
                "reversed().reversed() content equals original");

        // Size stays linked.
        d.addLast(4);
        check(rev.size() == 4, "reversed view size linked to original");
        check(rev.peekFirst() == 4, "reversed head tracks original new tail");

        // descendingIterator of the reversed view equals the forward order.
        List<Integer> viaDesc = new ArrayList<>();
        for (Iterator<Integer> it = rev.descendingIterator(); it.hasNext(); ) viaDesc.add(it.next());
        check(viaDesc.equals(new ArrayList<>(d)), "reversed descendingIterator mirrors original");

        // Mutations through the view are fail-fast consistent with the original:
        // on a fresh view, a tail add on the original invalidates the reversed
        // view's iterator (best-effort fail-fast; measured on fresh state).
        Deque<Integer> orig = new ArrayDeque<>(List.of(1, 2, 3));
        Deque<Integer> fresh = orig.reversed();
        Iterator<Integer> itRev = fresh.iterator();
        itRev.next();
        orig.addLast(0);
        expectThrows(itRev::next, java.util.ConcurrentModificationException.class,
                "reversed view iterator CME after original tail add");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTwelveMethodMatrix();
        testQueueViewAliasing();
        testOccurrencesDeep();
        testBulkOperations();
        testToArrayContract();
        testIteratorRemoveSemantics();
        testFailFastCme();
        testNullMatrix();
        testAlternatingDrain();
        testReversedView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
