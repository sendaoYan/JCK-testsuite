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

    static void testBlackBox() {
        // constructor(Collection) + getFirst/getLast + element
        ConcurrentLinkedDeque<String> d = new ConcurrentLinkedDeque<>(java.util.Arrays.asList("a", "b", "c"));
        check(d.size() == 3, "constructor(Collection) copies");
        check("a".equals(d.getFirst()) && "c".equals(d.getLast()), "getFirst/getLast");
        check("a".equals(d.element()), "element returns head");

        // queue-style offer/peek/poll
        check(d.offer("d"), "offer adds to tail");
        check("a".equals(d.peek()), "peek head");
        check("a".equals(d.poll()), "poll head");
        check("b".equals(d.peek()), "peek after poll");

        // addAll
        ConcurrentLinkedDeque<String> d2 = new ConcurrentLinkedDeque<>();
        check(d2.addAll(java.util.Arrays.asList("x", "y")), "addAll true");
        check(d2.size() == 2, "addAll size");

        // remove()/remove(Object)
        check("b".equals(d.remove()), "remove() returns head");
        check(d.remove("c"), "remove(Object) true");
        check(!d.remove("zzz"), "remove(Object) missing false");

        // removeAll / retainAll / removeIf
        ConcurrentLinkedDeque<Integer> q = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3, 4, 5));
        check(q.removeAll(java.util.Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(java.util.Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // forEach order
        ConcurrentLinkedDeque<Integer> f = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3));
        java.util.ArrayList<Integer> seen = new java.util.ArrayList<>();
        f.forEach(seen::add);
        check(seen.equals(java.util.Arrays.asList(1, 2, 3)), "forEach order");

        // iterator
        Iterator<Integer> it = f.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3, "iterator order");

        // toArray / toArray(T[])
        Object[] arr = f.toArray();
        check(arr.length == 3 && arr[0].equals(1), "toArray()");
        Integer[] typed = f.toArray(new Integer[0]);
        check(typed.length == 3 && typed[0] == 1 && typed[2] == 3, "toArray(T[])");

        // clear
        f.clear();
        check(f.isEmpty(), "clear empties");

        // spliterator
        ConcurrentLinkedDeque<Integer> spd = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = spd.spliterator();
        check(sp.estimateSize() == Long.MAX_VALUE, "spliterator estimateSize Long.MAX_VALUE");
        java.util.ArrayList<Integer> viaSp = new java.util.ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(java.util.Arrays.asList(1, 2, 3)), "spliterator order");

        // toString
        check(spd.toString() != null && spd.toString().contains("1"), "toString non-null");
    }

    // ---- appended: contract deepening ----
    static void testEmptyStateMatrix() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        check(d.isEmpty() && d.size() == 0, "fresh deque empty");
        check(d.peekFirst() == null && d.peekLast() == null, "peekFirst/Last empty null");
        check(d.pollFirst() == null && d.pollLast() == null, "pollFirst/Last empty null");
        check(d.peek() == null && d.poll() == null, "peek/poll empty null");
        expectThrows(() -> d.getFirst(), java.util.NoSuchElementException.class, "getFirst empty");
        expectThrows(() -> d.getLast(), java.util.NoSuchElementException.class, "getLast empty");
        expectThrows(() -> d.removeFirst(), java.util.NoSuchElementException.class, "removeFirst empty");
        expectThrows(() -> d.removeLast(), java.util.NoSuchElementException.class, "removeLast empty");
        expectThrows(() -> d.pop(), java.util.NoSuchElementException.class, "pop empty");
        expectThrows(() -> d.remove(), java.util.NoSuchElementException.class, "remove() empty");
        expectThrows(() -> d.element(), java.util.NoSuchElementException.class, "element() empty");
        expectThrows(() -> d.iterator().next(), java.util.NoSuchElementException.class, "iterator next empty");
        expectThrows(() -> d.descendingIterator().next(), java.util.NoSuchElementException.class,
                     "descendingIterator next empty");
        check(d.toString().equals("[]"), "empty toString []");
    }

    static void testNullMatrix() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>();
        expectThrows(() -> d.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> d.offerLast(null), NullPointerException.class, "offerLast(null)");
        expectThrows(() -> d.push(null), NullPointerException.class, "push(null)");
        // CLD quirk: occurrence-removal rejects null with NPE (unlike contains)
        expectThrows(() -> d.removeFirstOccurrence(null), NullPointerException.class,
                     "removeFirstOccurrence(null) throws NPE");
        expectThrows(() -> d.removeLastOccurrence(null), NullPointerException.class,
                     "removeLastOccurrence(null) throws NPE");
        expectThrows(() -> d.remove(null), NullPointerException.class,
                     "remove(null) delegates to removeFirstOccurrence -> NPE");
        expectThrows(() -> new ConcurrentLinkedDeque<Integer>(null), NullPointerException.class,
                     "ctor(null Collection)");
        expectThrows(() -> new ConcurrentLinkedDeque<Integer>(java.util.Arrays.asList(1, null)),
                     NullPointerException.class, "ctor(Collection with null)");
        expectThrows(() -> d.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> d.addAll(java.util.Arrays.asList(1, null)), NullPointerException.class,
                     "addAll(Collection with null)");
        check(!d.contains(null), "contains(null) returns false");
    }

    static void testOccurrenceRemovalThree() {
        // three occurrences: both directions remove exactly one, distinguishable instance
        ConcurrentLinkedDeque<Integer> t = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(7, 0, 7, 7));
        t.removeFirstOccurrence(7);
        check(t.peekFirst() == 0, "rfo removes the head instance");
        check(t.size() == 3, "rfo removes exactly one");
        ConcurrentLinkedDeque<Integer> u = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(7, 0, 7, 7));
        u.removeLastOccurrence(7);
        check(u.peekLast() == 7 && u.peekFirst() == 7 && u.size() == 3,
              "rlo removes only the last instance");
        java.util.List<Integer> rest = new java.util.ArrayList<>();
        for (int x : u) rest.add(x);
        check(rest.equals(java.util.Arrays.asList(7, 0, 7)), "rlo leaves [7,0,7]");
    }

    static void testIterators() {
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3));
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
        ConcurrentLinkedDeque<Integer> w = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3));
        Iterator<Integer> wit = w.iterator();
        w.addLast(99);
        int n = 0;
        while (wit.hasNext()) { wit.next(); n++; }
        check(n >= 3, "iterator completes after concurrent add (no CME)");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentLinkedDeque.class).getSerialVersionUID()
              == 876323262645176354L, "serialVersionUID locked");
        ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>(java.util.Arrays.asList(1, 2, 3));
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
        // bounded two-producer proof: head producer + tail producer, then verify exact accounting
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
        testDequeSemantics();
        testStackSemantics();
        testOccurrenceRemoval();
        testNullRejection();
        testDescendingIterator();
        testBlackBox();
        testEmptyStateMatrix();
        testNullMatrix();
        testOccurrenceRemovalThree();
        testIterators();
        testSerialization();
        testConcurrentBothEnds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
