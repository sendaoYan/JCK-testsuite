/*
 * @test
 * @summary java.util.Deque conformance: white-box structure + black-box behavior.
 * @run main DequeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
