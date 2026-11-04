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
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    interface ThrowingRunnable { void run() throws Throwable; }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T expectThrowsMsg(Runnable r, Class<T> type, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                     + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static <T extends Throwable> T expectThrowsThrowing(Class<T> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void awaitBounded(CountDownLatch latch, String what) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout (5s) waiting for: " + what);
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

    // ---- Part 3: blocking-queue contract deepening ----
    static void testCapacityBoundary() {
        IllegalArgumentException e0 = expectThrowsMsg(() -> new LinkedBlockingDeque<Integer>(0),
                IllegalArgumentException.class, "ctor capacity 0 IAE");
        check(e0.getMessage() == null, "ctor capacity 0 IAE has null message");
        IllegalArgumentException en = expectThrowsMsg(() -> new LinkedBlockingDeque<Integer>(-1),
                IllegalArgumentException.class, "ctor capacity negative IAE");
        check(en.getMessage() == null, "ctor negative IAE has null message");
        LinkedBlockingDeque<Integer> unbounded = new LinkedBlockingDeque<>();
        check(unbounded.remainingCapacity() == Integer.MAX_VALUE, "default capacity is MAX_VALUE");
        unbounded.add(1);
        unbounded.add(2);
        check(unbounded.remainingCapacity() == Integer.MAX_VALUE - 2,
                "unbounded remainingCapacity = MAX - size");
        LinkedBlockingDeque<Integer> one = new LinkedBlockingDeque<>(1);
        check(one.remainingCapacity() == 1 && one.offer(1) && !one.offer(2), "capacity 1 constructor usable");
    }

    static void testTwelveMethodEmptyState() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        check(d.isEmpty() && d.size() == 0, "empty-state: initially empty");
        expectThrows(() -> d.getFirst(), NoSuchElementException.class, "empty-state: getFirst NSEE");
        expectThrows(() -> d.getLast(), NoSuchElementException.class, "empty-state: getLast NSEE");
        expectThrows(() -> d.removeFirst(), NoSuchElementException.class, "empty-state: removeFirst NSEE");
        expectThrows(() -> d.removeLast(), NoSuchElementException.class, "empty-state: removeLast NSEE");
        expectThrows(() -> d.pop(), NoSuchElementException.class, "empty-state: pop NSEE");
        expectThrows(() -> d.element(), NoSuchElementException.class, "empty-state: element NSEE");
        expectThrows(() -> d.remove(), NoSuchElementException.class, "empty-state: remove() NSEE");
        check(d.pollFirst() == null, "empty-state: pollFirst null");
        check(d.pollLast() == null, "empty-state: pollLast null");
        check(d.peekFirst() == null && d.peekLast() == null, "empty-state: peekFirst/peekLast null");
        expectThrows(() -> d.iterator().next(), NoSuchElementException.class, "empty-state: iterator next NSEE");
        expectThrows(() -> d.descendingIterator().next(), NoSuchElementException.class,
                "empty-state: descendingIterator next NSEE");
    }

    static void testTwelveMethodFullState() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(2);
        d.addFirst(10);
        d.addLast(20);
        check(d.size() == 2 && d.remainingCapacity() == 0, "full-state: capacity 2 filled");
        IllegalStateException iseF = expectThrowsMsg(() -> d.addFirst(30),
                IllegalStateException.class, "full-state: addFirst ISE");
        check("Deque full".equals(iseF.getMessage()), "full-state: addFirst message exactly 'Deque full'");
        IllegalStateException iseL = expectThrowsMsg(() -> d.addLast(30),
                IllegalStateException.class, "full-state: addLast ISE");
        check("Deque full".equals(iseL.getMessage()), "full-state: addLast message exactly 'Deque full'");
        IllegalStateException iseA = expectThrowsMsg(() -> d.add(30),
                IllegalStateException.class, "full-state: add (bridge) ISE");
        check("Deque full".equals(iseA.getMessage()), "full-state: add bridge message 'Deque full'");
        IllegalStateException iseP = expectThrowsMsg(() -> d.push(30),
                IllegalStateException.class, "full-state: push ISE");
        check("Deque full".equals(iseP.getMessage()), "full-state: push message 'Deque full'");
        check(!d.offerFirst(30), "full-state: offerFirst false");
        check(!d.offerLast(30), "full-state: offerLast false");
        check(!d.offer(30), "full-state: offer (bridge) false");
        check(d.getFirst() == 10 && d.getLast() == 20, "full-state: getFirst/getLast return ends");
        check(d.peekFirst() == 10 && d.peekLast() == 20, "full-state: peekFirst/peekLast return ends");
        check(d.element() == 10 && d.peek() == 10, "full-state: bridge element/peek return head");
        check(d.pollFirst() == 10 && d.pollLast() == 20 && d.isEmpty(), "full-state: drain from both ends");
    }

    static void testBridgeMethods() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>();
        check(d.add(1) && d.add(2) && d.add(3), "bridge: add appends at tail");
        check(d.peekLast() == 3, "bridge: add behaves like addLast");
        check(d.offer(4) && d.peekLast() == 4, "bridge: offer appends at tail");
        check(d.peek() == 1 && d.peekFirst() == 1, "bridge: peek == peekFirst");
        check(d.poll() == 1 && d.peekFirst() == 2, "bridge: poll removes from first end");
        check(d.element() == 2 && d.getFirst() == 2, "bridge: element == getFirst");
        check(d.remove() == 2 && d.peekFirst() == 3, "bridge: remove() == removeFirst");
        d.push(9);
        check(d.peekFirst() == 9, "bridge: push == addFirst");
        check(d.pop() == 9, "bridge: pop == removeFirst");
        check(d.contains(3) && d.contains(4), "bridge: contents as expected");
    }

    static void testTimedDeque() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(1);
        d.add(1);
        long t0 = System.nanoTime();
        check(!d.offerFirst(2, 200, TimeUnit.MILLISECONDS), "timed offerFirst on full waits then false");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed offerFirst waited ~200ms");
        check(!d.offerLast(2, 200, TimeUnit.MILLISECONDS), "timed offerLast on full false");
        check(!d.offer(2, 200, TimeUnit.MILLISECONDS), "timed offer (bridge) on full false");
        d.clear();
        t0 = System.nanoTime();
        check(d.pollLast(200, TimeUnit.MILLISECONDS) == null, "timed pollLast on empty waits then null");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed pollLast waited ~200ms");
        check(d.pollFirst(200, TimeUnit.MILLISECONDS) == null, "timed pollFirst on empty null");
        check(d.poll(200, TimeUnit.MILLISECONDS) == null, "timed poll (bridge) on empty null");
        check(d.isEmpty(), "still empty after timed empty-deque polls");
    }

    static void testTakeLastPairingAndPutLast() throws Exception {
        // consumer blocks in takeLast on empty deque; putLast hands off
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(2);
        AtomicInteger got = new AtomicInteger(-1);
        CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        Thread c = startDaemon(() -> {
            entered.countDown();
            try {
                got.set(d.takeLast());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "takeLast consumer entry");
        d.putLast(42);
        awaitBounded(done, "takeLast completion");
        c.join(5000);
        check(got.get() == 42, "takeLast receives putLast value");
        check(d.isEmpty(), "deque empty after handoff");
        // producer blocks in putLast on full deque; pollLast frees the slot
        LinkedBlockingDeque<Integer> f = new LinkedBlockingDeque<>(1);
        f.putLast(1);
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch pEntered = new CountDownLatch(1), pDone = new CountDownLatch(1);
        Thread p = startDaemon(() -> {
            pEntered.countDown();
            try {
                f.putLast(2);
                completed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                pDone.countDown();
            }
        });
        awaitBounded(pEntered, "putLast producer entry");
        check(f.pollLast() == 1, "pollLast frees slot for blocked putLast");
        awaitBounded(pDone, "putLast completion");
        p.join(5000);
        check(completed.get() && f.peekLast() == 2, "putLast completed after slot freed");
    }

    static void testInterruptedTakeFirst() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(1);
        CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                d.takeFirst();
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "takeFirst thread entry");
        t.interrupt();
        awaitBounded(done, "interrupted takeFirst completion");
        t.join(5000);
        check(gotIE.get(), "blocked takeFirst interrupted -> InterruptedException");
        check(!t.isAlive(), "interrupted takeFirst thread terminated");
    }

    static void testInterruptStatusAtEntry() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt();
            try {
                d.takeLast();
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(done, "pre-interrupted takeLast completion");
        t.join(5000);
        check(gotIE.get(), "takeLast with pre-set interrupt status throws InterruptedException");
    }

    static void testInterruptedPutFirst() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(1);
        d.add(1);
        CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                d.putFirst(2);
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "putFirst thread entry");
        t.interrupt();
        awaitBounded(done, "interrupted putFirst completion");
        t.join(5000);
        check(gotIE.get(), "blocked putFirst interrupted -> InterruptedException");
        check(d.size() == 1 && d.peekFirst() == 1, "deque unchanged after interrupted putFirst");
    }

    static void testIteratorSemantics() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(4);
        d.add(1);
        d.add(2);
        d.add(3);
        Iterator<Integer> it = d.iterator();
        check(it.next() == 1, "ascending iterator first");
        it.remove();
        check(d.size() == 2 && d.peekFirst() == 2, "ascending iterator.remove works");
        LinkedBlockingDeque<Integer> d2 = new LinkedBlockingDeque<>(4);
        d2.add(1);
        d2.add(2);
        d2.add(3);
        Iterator<Integer> dit = d2.descendingIterator();
        check(dit.next() == 3, "descending iterator first");
        dit.remove();
        check(d2.peekFirst() == 1 && d2.peekLast() == 2 && d2.size() == 2,
                "descending iterator.remove removes visited tail element");
    }

    static void testSerializationRoundTrip() throws Exception {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(3);
        d.add(1);
        d.add(2);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(d);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        @SuppressWarnings("unchecked")
        LinkedBlockingDeque<Integer> rt = (LinkedBlockingDeque<Integer>) back;
        check(rt.size() == 2, "serialization round-trip preserves element count");
        check(rt.pollFirst() == 1 && rt.pollLast() == 2 && rt.isEmpty(),
                "serialization round-trip preserves both-end order");
        check(rt.remainingCapacity() == 3, "serialization round-trip preserves capacity");
        check(rt.offer(1) && rt.offer(2) && rt.offer(3), "round-tripped deque accepts capacity elements");
        check(!rt.offer(4), "round-tripped deque is still bounded by original capacity");
    }

    static void testNullRejectionExtended() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(2);
        expectThrows(() -> d.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> d.addFirst(null), NullPointerException.class, "addFirst(null)");
        expectThrows(() -> d.addLast(null), NullPointerException.class, "addLast(null)");
        expectThrows(() -> d.offerFirst(null), NullPointerException.class, "offerFirst(null)");
        expectThrows(() -> d.offerLast(null), NullPointerException.class, "offerLast(null)");
        expectThrowsThrowing(NullPointerException.class, () -> d.putFirst(null), "putFirst(null)");
        expectThrowsThrowing(NullPointerException.class, () -> d.putLast(null), "putLast(null)");
        expectThrowsThrowing(NullPointerException.class,
                () -> d.offerFirst(null, 1, TimeUnit.SECONDS), "offerFirst(null,timeout)");
        check(!d.contains(null), "contains(null) false");
        check(!d.remove(null), "remove(null) false");
        check(!d.removeFirstOccurrence(null), "removeFirstOccurrence(null) false");
        check(!d.removeLastOccurrence(null), "removeLastOccurrence(null) false");
        check(d.isEmpty(), "deque unchanged after null rejections");
    }

    static void testRemoveOccurrenceMissing() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(2);
        d.add(1);
        check(!d.removeFirstOccurrence(99), "removeFirstOccurrence(missing) false");
        check(!d.removeLastOccurrence(99), "removeLastOccurrence(missing) false");
        check(d.size() == 1, "deque unchanged after missing occurrence removals");
    }

    static void testDrainToEdges() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(4);
        d.add(1);
        d.add(2);
        List<Integer> sink = new ArrayList<>();
        check(d.drainTo(sink, 0) == 0, "drainTo(max=0) returns 0");
        check(sink.isEmpty() && d.size() == 2, "drainTo(max=0) mutates nothing");
        IllegalArgumentException iae = expectThrowsMsg(() -> d.drainTo(d),
                IllegalArgumentException.class, "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        check(d.size() == 2, "drainTo(self) leaves deque unchanged");
        d.add(3);
        List<Integer> part = new ArrayList<>();
        check(d.drainTo(part, 2) == 2 && part.equals(Arrays.asList(1, 2)),
                "drainTo(max) truncates at max in FIFO order");
        check(d.size() == 1 && d.peek() == 3, "drainTo(max) leaves remainder");
    }

    static void testEqualsIdentitySemantics() {
        LinkedBlockingDeque<Integer> a = new LinkedBlockingDeque<>();
        LinkedBlockingDeque<Integer> b = new LinkedBlockingDeque<>();
        check(a.add(1) && a.add(2) && b.add(1) && b.add(2), "setup: equal contents, distinct instances");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "no element-based equals override (identity semantics)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(Arrays.asList(1, 2)), "deque not equal to List with same elements");
    }

    static void testToArrayExactContract() {
        LinkedBlockingDeque<String> d = new LinkedBlockingDeque<>(4);
        d.add("a");
        d.add("b");
        d.add("c");
        Object[] arr = d.toArray();
        check(arr.getClass() == Object[].class && arr.length == 3, "toArray() exact length");
        check(Arrays.equals(arr, new Object[]{"a", "b", "c"}), "toArray() first-to-last order");
        String[] small = d.toArray(new String[0]);
        check(small.getClass() == String[].class, "toArray(T[0]) exact runtime type");
        check(Arrays.equals(small, new String[]{"a", "b", "c"}), "toArray(T[0]) contents in order");
        String[] exact = d.toArray(new String[3]);
        check(exact.getClass() == String[].class && "c".equals(exact[2]), "toArray(T[exact size]) filled");
    }

    static void testContainsRemoveMiddle() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(4);
        d.add(1);
        d.add(2);
        d.add(3);
        check(d.contains(2), "contains middle element");
        check(d.remove(Integer.valueOf(2)), "remove middle element");
        check(!d.contains(2) && d.size() == 2, "middle element gone");
        check(Arrays.equals(d.toArray(), new Object[]{1, 3}), "order preserved after middle removal");
    }

    static void testAddAllOverflowPartial() {
        LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(2);
        IllegalStateException ise = expectThrowsMsg(
                () -> d.addAll(Arrays.asList(1, 2, 3)), IllegalStateException.class,
                "addAll overflow ISE");
        check("Deque full".equals(ise.getMessage()), "addAll overflow message 'Deque full'");
        check(d.size() == 2, "addAll overflow retains already-inserted prefix");
        check(d.poll() == 1 && d.poll() == 2 && d.isEmpty(), "retained prefix in order");
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
        testCapacityBoundary();
        testTwelveMethodEmptyState();
        testTwelveMethodFullState();
        testBridgeMethods();
        testTimedDeque();
        testTakeLastPairingAndPutLast();
        testInterruptedTakeFirst();
        testInterruptStatusAtEntry();
        testInterruptedPutFirst();
        testIteratorSemantics();
        testSerializationRoundTrip();
        testNullRejectionExtended();
        testRemoveOccurrenceMissing();
        testDrainToEdges();
        testEqualsIdentitySemantics();
        testToArrayExactContract();
        testContainsRemoveMiddle();
        testAddAllOverflowPartial();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
