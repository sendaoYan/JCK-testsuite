/*
 * @test
 * @summary java.util.concurrent.LinkedBlockingDeque conformance: white-box + black-box
 *          (first/last offer/peek/poll/remove, blocking putFirst/takeFirst handoff,
 *          push/pop, occurrence removal, drainTo, descendingIterator, null rejection).
 * @run main LinkedBlockingDequeTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedBlockingDequeTest {
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

    static Thread startDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.LinkedBlockingDeque");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingDeque.class.isAssignableFrom(c), "implements BlockingDeque");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(int.class) != null,
              "constructors ()/(int)");
        check(hasMethod(c, "addFirst", Object.class) && hasMethod(c, "addLast", Object.class),
              "addFirst/addLast");
        check(hasMethod(c, "offerFirst", Object.class) && hasMethod(c, "offerLast", Object.class),
              "offerFirst/offerLast");
        check(hasMethod(c, "putFirst", Object.class) && hasMethod(c, "putLast", Object.class),
              "putFirst/putLast");
        check(hasMethod(c, "takeFirst") && hasMethod(c, "takeLast"), "takeFirst/takeLast");
        check(hasMethod(c, "pollFirst") && hasMethod(c, "pollLast"), "pollFirst/pollLast");
        check(hasMethod(c, "peekFirst") && hasMethod(c, "peekLast"), "peekFirst/peekLast");
        check(hasMethod(c, "push", Object.class) && hasMethod(c, "pop"), "push/pop");
        check(hasMethod(c, "removeFirstOccurrence", Object.class)
              && hasMethod(c, "removeLastOccurrence", Object.class),
              "removeFirstOccurrence/removeLastOccurrence");
        check(hasMethod(c, "descendingIterator"), "descendingIterator");
        check(hasMethod(c, "remainingCapacity") && hasMethod(c, "drainTo", java.util.Collection.class),
              "remainingCapacity/drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testDequeSemantics() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        d.addFirst(1);
        d.addLast(3);
        d.offerFirst(0);
        d.offerLast(4);
        check(d.peekFirst() == 0 && d.peekLast() == 4, "peek first/last");
        check(d.size() == 4, "size");
        check(d.pollFirst() == 0 && d.pollLast() == 4, "poll first/last");
        check(d.peekFirst() == 1 && d.peekLast() == 3, "remaining");
        check(d.removeFirst() == 1 && d.removeLast() == 3, "remove first/last");
        check(d.isEmpty(), "empty");
    }

    static void testBlockingHandoff() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(1);
        d.putFirst(1); // fills the single slot
        AtomicInteger taken = new AtomicInteger(-1);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                taken.set(d.takeFirst());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        done.await();
        check(taken.get() == 1, "takeFirst receives putFirst value");
        t.join();
        check(d.isEmpty(), "empty after take");
    }

    static void testPushPop() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        d.push(1);
        d.push(2);
        d.push(3);
        check(d.pop() == 3 && d.pop() == 2 && d.pop() == 1, "push/pop LIFO");
        check(d.isEmpty(), "empty after pops");
    }

    static void testOccurrenceRemoval() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        d.add(1);
        d.add(2);
        d.add(1);
        d.add(3);
        check(d.removeFirstOccurrence(1), "removeFirstOccurrence true");
        check(d.peekFirst() == 2 && d.peekLast() == 3, "first occurrence removed");
        check(d.removeLastOccurrence(1), "removeLastOccurrence true");
        check(!d.contains(1), "all occurrences removed");
    }

    static void testDrainToAndRemainingCapacity() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(5);
        check(d.remainingCapacity() == 5, "remainingCapacity initial");
        d.add(1);
        d.add(2);
        d.add(3);
        List<Integer> list = new ArrayList<>();
        check(d.drainTo(list) == 3, "drainTo count");
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo FIFO order");
        check(d.isEmpty(), "empty after drainTo");
        check(d.remainingCapacity() == 5, "remainingCapacity restored");
    }

    static void testDescendingIteratorAndNull() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator order");
        expectThrows(() -> d.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> d.offerFirst(null), NullPointerException.class, "offerFirst(null)");
    }

    static void testBlackBox() throws Exception {
        // constructor(Collection) + getFirst/getLast + element + offer/peek/poll
        LinkedBlockingDeque<String> d = new LinkedBlockingDeque<>(Arrays.asList("a", "b", "c"));
        check(d.size() == 3, "constructor(Collection) copies");
        check("a".equals(d.getFirst()) && "c".equals(d.getLast()), "getFirst/getLast");
        check("a".equals(d.element()), "element returns head");
        check(d.offer("d"), "offer appends tail");
        check("a".equals(d.peek()), "peek head");
        check("a".equals(d.poll()), "poll head");
        check("b".equals(d.peek()), "peek after poll");

        // offer(timeout) / poll(timeout) on a bounded deque
        LinkedBlockingDeque<String> bounded = new LinkedBlockingDeque<>(1);
        check(bounded.offer("x"), "offer on bounded");
        check(!bounded.offer("y", 1, java.util.concurrent.TimeUnit.NANOSECONDS), "offer(timeout) false when full");
        check("x".equals(bounded.poll(1, java.util.concurrent.TimeUnit.NANOSECONDS)), "poll(timeout) returns existing");
        check(bounded.poll(1, java.util.concurrent.TimeUnit.NANOSECONDS) == null, "poll(timeout) null when empty");

        // put / take (blocking handoff)
        LinkedBlockingDeque<Integer> bd = new LinkedBlockingDeque<>(1);
        bd.put(42);
        AtomicInteger taken = new AtomicInteger(-1);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try { taken.set(bd.take()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { done.countDown(); }
        });
        done.await();
        check(taken.get() == 42, "take receives put value");
        t.join();

        // remove()/remove(Object)
        LinkedBlockingDeque<String> rr = new LinkedBlockingDeque<>(Arrays.asList("a", "b", "c"));
        check("a".equals(rr.remove()), "remove() returns head");
        check(rr.remove("c"), "remove(Object) true");
        check(!rr.remove("zzz"), "remove(Object) missing false");

        // removeAll / retainAll / removeIf
        LinkedBlockingDeque<Integer> q = new LinkedBlockingDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(q.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // addAll
        LinkedBlockingDeque<Integer> d2 = new LinkedBlockingDeque<>();
        check(d2.addAll(Arrays.asList(1, 2)), "addAll true");
        check(d2.size() == 2, "addAll size");

        // forEach / iterator
        LinkedBlockingDeque<Integer> f = new LinkedBlockingDeque<>(Arrays.asList(1, 2, 3));
        List<Integer> seen = new ArrayList<>();
        f.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach order");
        Iterator<Integer> it = f.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3, "iterator order");

        // toArray / toArray(T[])
        Object[] arr = f.toArray();
        check(arr.length == 3 && arr[0].equals(1), "toArray()");
        Integer[] typed = f.toArray(new Integer[0]);
        check(typed.length == 3 && typed[2] == 3, "toArray(T[])");

        // spliterator
        java.util.Spliterator<Integer> sp = f.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        List<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator order");

        // clear
        f.clear();
        check(f.isEmpty(), "clear empties");

        // toString
        LinkedBlockingDeque<Integer> ts = new LinkedBlockingDeque<>(Arrays.asList(1, 2));
        check(ts.toString() != null && ts.toString().contains("1"), "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testDequeSemantics();
        testBlockingHandoff();
        testPushPop();
        testOccurrenceRemoval();
        testDrainToAndRemainingCapacity();
        testDescendingIteratorAndNull();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
