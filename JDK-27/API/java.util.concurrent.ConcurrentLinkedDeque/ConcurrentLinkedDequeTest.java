/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedDeque conformance: white-box + black-box
 *          (first/last offer/peek/poll/remove, push/pop stack semantics, occurrence
 *          removal, descendingIterator, null rejection).
 * @run main ConcurrentLinkedDequeTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ConcurrentLinkedDequeTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentLinkedDeque");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractCollection.class, "extends AbstractCollection");
        check(Deque.class.isAssignableFrom(c), "implements Deque");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "addFirst", Object.class) && hasMethod(c, "addLast", Object.class),
              "addFirst/addLast");
        check(hasMethod(c, "offerFirst", Object.class) && hasMethod(c, "offerLast", Object.class),
              "offerFirst/offerLast");
        check(hasMethod(c, "peekFirst") && hasMethod(c, "peekLast"), "peekFirst/peekLast");
        check(hasMethod(c, "pollFirst") && hasMethod(c, "pollLast"), "pollFirst/pollLast");
        check(hasMethod(c, "push", Object.class) && hasMethod(c, "pop"), "push/pop");
        check(hasMethod(c, "removeFirstOccurrence", Object.class)
              && hasMethod(c, "removeLastOccurrence", Object.class),
              "removeFirstOccurrence/removeLastOccurrence");
        check(hasMethod(c, "descendingIterator"), "descendingIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testDequeSemantics() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.addFirst(1);
        d.addLast(2);
        d.offerFirst(0);
        d.offerLast(3);
        check(d.peekFirst() == 0 && d.peekLast() == 3, "peek first/last");
        check(d.size() == 4, "size");
        check(d.pollFirst() == 0 && d.pollLast() == 3, "poll first/last");
        check(d.peekFirst() == 1 && d.peekLast() == 2, "remaining");
        check(d.removeFirst() == 1 && d.removeLast() == 2, "remove first/last");
        check(d.isEmpty(), "empty after removes");
    }

    static void testStackSemantics() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.push(1);
        d.push(2);
        d.push(3);
        check(d.pop() == 3, "pop LIFO 3");
        check(d.pop() == 2, "pop LIFO 2");
        check(d.pop() == 1, "pop LIFO 1");
        check(d.isEmpty(), "empty after pops");
    }

    static void testOccurrenceRemoval() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.add(1);
        d.add(2);
        d.add(1);
        d.add(3);
        check(d.removeFirstOccurrence(1), "removeFirstOccurrence true");
        check(d.peekFirst() == 2 && d.peekLast() == 3, "first occurrence removed");
        check(d.removeLastOccurrence(1), "removeLastOccurrence true");
        check(!d.contains(1), "all occurrences of 1 removed");
        check(!d.removeFirstOccurrence(99), "removeFirstOccurrence missing false");
    }

    static void testNullRejection() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        expectThrows(() -> d.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> d.offerFirst(null), NullPointerException.class, "offerFirst(null)");
        expectThrows(() -> d.addLast(null), NullPointerException.class, "addLast(null)");
        check(!d.contains(null), "contains(null) returns false");
    }

    static void testDescendingIterator() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testDequeSemantics();
        testStackSemantics();
        testOccurrenceRemoval();
        testNullRejection();
        testDescendingIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
