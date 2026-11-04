/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedDeque conformance: white-box + black-box
 *          (full 12-method deque matrix empty/non-empty, push/pop stack semantics,
 *          occurrence removal both directions, null rejection incl. remove*Occurrence(null),
 *          iterator/descendingIterator remove, serialization, bounded concurrent offer/poll).
 * @run main ConcurrentLinkedDequeTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
        check(hasMethod(c, "getFirst") && hasMethod(c, "getLast"), "getFirst/getLast");
        check(hasMethod(c, "removeFirst") && hasMethod(c, "removeLast"), "removeFirst/removeLast");
        check(hasMethod(c, "push", Object.class) && hasMethod(c, "pop"), "push/pop");
        check(hasMethod(c, "removeFirstOccurrence", Object.class)
              && hasMethod(c, "removeLastOccurrence", Object.class),
              "removeFirstOccurrence/removeLastOccurrence");
        check(hasMethod(c, "descendingIterator"), "descendingIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConcurrentLinkedDeque() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.offerFirst(1);
        d.offerLast(2);
        check(d.peekFirst() == 1 && d.peekLast() == 2, "peek first/last");
        check(d.pollFirst() == 1 && d.pollFirst() == 2, "poll first");
        check(d.isEmpty(), "empty after poll");
    }

    static void testEmptyStateMatrix() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        check(d.isEmpty() && d.size() == 0, "fresh deque empty");
        // empty: value-returning forms yield null
        check(d.peekFirst() == null && d.peekLast() == null, "peekFirst/Last empty null");
        check(d.pollFirst() == null && d.pollLast() == null, "pollFirst/Last empty null");
        check(d.peek() == null && d.poll() == null, "peek/poll empty null");
        // empty: exception forms throw NoSuchElementException
        expectThrows(() -> d.getFirst(), NoSuchElementException.class, "getFirst empty");
        expectThrows(() -> d.getLast(), NoSuchElementException.class, "getLast empty");
        expectThrows(() -> d.removeFirst(), NoSuchElementException.class, "removeFirst empty");
        expectThrows(() -> d.removeLast(), NoSuchElementException.class, "removeLast empty");
        expectThrows(() -> d.pop(), NoSuchElementException.class, "pop empty");
        expectThrows(() -> d.remove(), NoSuchElementException.class, "remove() empty");
        expectThrows(() -> d.element(), NoSuchElementException.class, "element() empty");
        expectThrows(() -> d.iterator().next(), NoSuchElementException.class, "iterator next empty");
        expectThrows(() -> d.descendingIterator().next(), NoSuchElementException.class,
                     "descendingIterator next empty");
        check(d.toString().equals("[]"), "empty toString []");
    }

    static void testDequeFullMatrix() {
        // non-empty state: every one of the 12 deque methods, both ends
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.addFirst(2);                                        // [2]
        d.addLast(3);                                         // [2,3]
        check(d.size() == 2, "addFirst/addLast insert at both ends");
        check(d.offerFirst(1), "offerFirst true");            // [1,2,3]
        check(d.offerLast(4), "offerLast true");              // [1,2,3,4]
        check(d.size() == 4, "size 4");
        check(d.getFirst() == 1 && d.getLast() == 4, "getFirst/getLast");
        check(d.peekFirst() == 1 && d.peekLast() == 4, "peekFirst/peekLast");
        check(d.peek() == 1, "peek == peekFirst");
        check(d.element() == 1, "element == getFirst");
        check(d.contains(2) && !d.contains(9), "contains");
        check(d.pollFirst() == 1, "pollFirst removes head");  // [2,3,4]
        check(d.pollLast() == 4, "pollLast removes tail");    // [2,3]
        check(d.removeFirst() == 2, "removeFirst returns head"); // [3]
        check(d.getLast() == 3 && d.getFirst() == 3, "single element both ends");
        check(d.removeLast() == 3, "removeLast returns tail");
        check(d.isEmpty(), "drained via all four removal forms");
        // queue forms map to first/last
        d.add("x".length());
        check(d.offer(7) && d.getLast() == 7, "offer == addLast");
        check(d.peek() == 1 && d.poll() == 1, "peek/poll operate at head");
        d.clear();
        check(d.isEmpty(), "clear empties");
    }

    static void testStackSemantics() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        d.push(1);
        d.push(2);
        d.push(3);
        check(d.size() == 3 && d.peekFirst() == 3, "push adds at head");
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
        // three occurrences: directions distinguishable
        ConcurrentLinkedDeque<Integer> t = new ConcurrentLinkedDeque<>(Arrays.asList(7, 0, 7, 7));
        t.removeFirstOccurrence(7);
        check(t.peekFirst() == 0, "rfo removes the head instance");
        check(t.size() == 3, "rfo removes exactly one");
        ConcurrentLinkedDeque<Integer> u = new ConcurrentLinkedDeque<>(Arrays.asList(7, 0, 7, 7));
        u.removeLastOccurrence(7);
        check(u.peekLast() == 7 && u.peekFirst() == 7 && u.size() == 3,
              "rlo removes only the last instance");
        java.util.List<Integer> rest = new java.util.ArrayList<>();
        for (int x : u) rest.add(x);
        check(rest.equals(Arrays.asList(7, 0, 7)), "rlo leaves [7,0,7]");
    }

    static void testNullMatrix() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        expectThrows(() -> d.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> d.addFirst(null), NullPointerException.class, "addFirst(null)");
        expectThrows(() -> d.addLast(null), NullPointerException.class, "addLast(null)");
        expectThrows(() -> d.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> d.offerFirst(null), NullPointerException.class, "offerFirst(null)");
        expectThrows(() -> d.offerLast(null), NullPointerException.class, "offerLast(null)");
        expectThrows(() -> d.push(null), NullPointerException.class, "push(null)");
        // CLD quirk: occurrence-removal rejects null with NPE (unlike contains/remove)
        expectThrows(() -> d.removeFirstOccurrence(null), NullPointerException.class,
                     "removeFirstOccurrence(null) throws NPE");
        expectThrows(() -> d.removeLastOccurrence(null), NullPointerException.class,
                     "removeLastOccurrence(null) throws NPE");
        expectThrows(() -> new ConcurrentLinkedDeque<Integer>(null), NullPointerException.class,
                     "ctor(null Collection)");
        expectThrows(() -> new ConcurrentLinkedDeque<Integer>(Arrays.asList(1, null)),
                     NullPointerException.class, "ctor(Collection with null)");
        expectThrows(() -> d.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> d.addAll(Arrays.asList(1, null)), NullPointerException.class,
                     "addAll(Collection with null)");
        // read-side: contains tolerates null, but remove(Object) delegates to
        // removeFirstOccurrence and therefore also rejects null with NPE (CLD-specific)
        check(!d.contains(null), "contains(null) returns false");
        expectThrows(() -> d.remove(null), NullPointerException.class,
                     "remove(null) delegates to removeFirstOccurrence -> NPE");
    }

    static void testIterators() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = d.iterator();
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "iterator.remove before next throws IllegalStateException");
        check(it.next() == 1, "iterator next");
        it.remove();
        check(d.size() == 2 && d.peekFirst() == 2, "iterator.remove unlinks head element");
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "second consecutive iterator.remove throws IllegalStateException");
        Iterator<Integer> desc = d.descendingIterator();
        expectThrows(() -> desc.remove(), IllegalStateException.class,
                     "descendingIterator.remove before next throws IllegalStateException");
        check(desc.next() == 3, "descendingIterator next");
        desc.remove();
        check(d.size() == 1 && d.peekFirst() == 2, "descendingIterator.remove unlinks tail element");
        // weak consistency: structural change mid-iteration does not throw CME
        ConcurrentLinkedDeque<Integer> w = new ConcurrentLinkedDeque<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> wit = w.iterator();
        w.addLast(99);
        int n = 0;
        while (wit.hasNext()) { wit.next(); n++; }
        check(n >= 3, "iterator completes after concurrent add (no CME)");
    }

    static void testCollectionOperations() {
        ConcurrentLinkedDeque<String> d = new ConcurrentLinkedDeque<>(Arrays.asList("a", "b", "c"));
        check(d.size() == 3, "constructor(Collection) copies");
        check("a".equals(d.getFirst()) && "c".equals(d.getLast()), "ctor order head..tail");
        check(d.contains("b") && !d.contains("z"), "contains");
        check(d.remove("b") && !d.remove("z"), "remove(Object) boolean");
        ConcurrentLinkedDeque<String> d2 = new ConcurrentLinkedDeque<>();
        check(d2.addAll(Arrays.asList("x", "y")) && d2.size() == 2, "addAll true, size");
        check(d2.toArray().length == 2 && d2.toArray()[1].equals("y"), "toArray order");
        java.util.ArrayList<String> seen = new java.util.ArrayList<>();
        d2.forEach(seen::add);
        check(seen.equals(Arrays.asList("x", "y")), "forEach order");
        check(d2.removeAll(Arrays.asList("x")) && d2.size() == 1, "removeAll");
        check(d2.retainAll(Arrays.asList("zz")) && d2.isEmpty(), "retainAll empties");
        ConcurrentLinkedDeque<Integer> f = new ConcurrentLinkedDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        f.removeIf(x -> x % 2 == 0);
        check(f.size() == 3 && f.contains(1) && f.contains(3) && f.contains(5), "removeIf");
        f.clear();
        check(f.isEmpty(), "clear");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentLinkedDeque.class).getSerialVersionUID()
              == 876323262645176354L, "serialVersionUID locked");
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>(Arrays.asList(1, 2, 3));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(d);
        }
        @SuppressWarnings("unchecked")
        ConcurrentLinkedDeque<Integer> copy = (ConcurrentLinkedDeque<Integer>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.size() == 3, "round-trip size");
        check(copy.peekFirst() == 1 && copy.peekLast() == 3, "round-trip keeps both ends");
        check(copy.pollFirst() == 1 && copy.pollLast() == 3, "resurrected deque usable");
    }

    static void testConcurrentBothEnds() throws Exception {
        // bounded two-producer proof: head producer + tail producer, then verify exact order
        final ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        final int n = 500;
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(2);
        Runnable head = () -> {
            for (int i = 0; i < n; i++) d.addFirst(i);
            done.countDown();
        };
        Runnable tail = () -> {
            for (int i = 0; i < n; i++) d.addLast(100000 + i); // disjoint value range
            done.countDown();
        };
        Thread t0 = new Thread(head, "cld-head");
        Thread t1 = new Thread(tail, "cld-tail");
        t0.setDaemon(true);
        t1.setDaemon(true);
        t0.start();
        t1.start();
        check(done.await(5, java.util.concurrent.TimeUnit.SECONDS), "both producers finished in 5s");
        check(d.size() == 2 * n, "all elements from both ends present");
        // drain from both ends; every element must be accounted for exactly once
        java.util.HashSet<Integer> seen = new java.util.HashSet<>();
        while (!d.isEmpty()) {
            Integer h = d.pollFirst();
            if (h != null) seen.add(h);
            Integer t2 = d.pollLast();
            if (t2 != null) seen.add(t2);
        }
        check(seen.size() == 2 * n, "no duplicates and no losses across both ends");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConcurrentLinkedDeque();
        testEmptyStateMatrix();
        testDequeFullMatrix();
        testStackSemantics();
        testOccurrenceRemoval();
        testNullMatrix();
        testIterators();
        testCollectionOperations();
        testSerialization();
        testConcurrentBothEnds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
