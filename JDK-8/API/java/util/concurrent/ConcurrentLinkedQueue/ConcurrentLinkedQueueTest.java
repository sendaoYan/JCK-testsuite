/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedQueue conformance: white-box + black-box
 *          (FIFO offer/poll/peek/element two-state semantics, null rejection, toArray
 *          null-terminator, iterator weak consistency + remove support, size
 *          consistency, spliterator, serialization, bounded concurrent offer/poll).
 * @run main ConcurrentLinkedQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentLinkedQueueTest {
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
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentLinkedQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(Queue.class.isAssignableFrom(c), "implements Queue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "offer", Object.class) && hasMethod(c, "add", Object.class),
              "offer/add");
        check(hasMethod(c, "poll") && hasMethod(c, "peek"), "poll/peek");
        check(hasMethod(c, "size") && hasMethod(c, "isEmpty"), "size/isEmpty");
        check(hasMethod(c, "contains", Object.class) && hasMethod(c, "remove", Object.class),
              "contains/remove");
        check(hasMethod(c, "iterator") && hasMethod(c, "toArray"), "iterator/toArray");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testQueue() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        check(q.offer("a") && q.offer("b"), "offer");
        check("a".equals(q.peek()), "peek");
        check("a".equals(q.poll()), "poll");
        check("b".equals(q.poll()) && q.poll() == null, "poll to empty");
        check(q.peek() == null && q.size() == 0, "empty");
    }

    static void testNullRejection() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        try {
            q.offer(null);
            throw new AssertionError("offer(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testEmptyState() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        check(q.isEmpty() && q.size() == 0, "fresh queue empty");
        check(q.peek() == null, "peek empty returns null");
        check(q.poll() == null, "poll empty returns null");
        expectThrows(() -> q.element(), java.util.NoSuchElementException.class,
                     "element() empty throws NoSuchElementException");
        check(!q.remove("x"), "remove(Object) empty false");
        check(!q.contains("x"), "contains empty false");
        check(q.toString().equals("[]"), "empty toString []");
        check(!q.iterator().hasNext(), "empty iterator hasNext false");
        expectThrows(() -> q.iterator().next(), java.util.NoSuchElementException.class,
                     "empty iterator next throws NoSuchElementException");
        check(q.toArray().length == 0, "empty toArray length 0");
    }

    static void testFifoOrderExact() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        boolean offered = true;
        for (int i = 1; i <= 10; i++) offered &= q.offer(i);
        check(offered && q.size() == 10, "offered 10 elements");
        boolean fifo = true;
        for (int i = 1; i <= 10; i++) fifo &= (q.poll() == i);
        check(fifo, "poll sequence is exactly FIFO 1..10");
        check(q.isEmpty(), "queue drained");
    }

    static void testQueueSemantics() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        check(q.isEmpty() && q.size() == 0, "empty initially");
        check(q.offer("a") && q.offer("b") && q.offer("c"), "offer");
        check(q.add("d"), "add");
        check(q.size() == 4, "size");
        check("a".equals(q.peek()), "peek head");
        check("a".equals(q.poll()), "poll head");
        check("b".equals(q.poll()), "poll next");
        check(q.peek().equals("c"), "peek after poll");
        check(q.contains("c") && !q.contains("a"), "contains");
        check(q.remove("c"), "remove returns true");
        check(!q.remove("zzz"), "remove missing false");
        check(q.size() == 1 && "d".equals(q.peek()), "remaining element");
        q.clear();
        check(q.isEmpty() && q.peek() == null && q.poll() == null, "clear empties");
    }

    static void testNullMatrix() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> new ConcurrentLinkedQueue<String>(null), NullPointerException.class,
                     "ctor(null Collection)");
        expectThrows(() -> new ConcurrentLinkedQueue<String>(Arrays.asList("a", null)),
                     NullPointerException.class, "ctor(Collection with null element)");
        expectThrows(() -> q.addAll(null), NullPointerException.class, "addAll(null)");
        expectThrows(() -> q.addAll(Arrays.asList("ok", null)), NullPointerException.class,
                     "addAll(Collection with null element)");
        // read-side nulls are tolerated and simply report absence
        check(!q.contains(null), "contains(null) returns false");
        check(!q.remove(null), "remove(null) returns false");
    }

    static void testIteratorRemove() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = q.iterator();
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "iterator.remove before next throws IllegalStateException");
        check(it.next() == 1, "iterator next");
        it.remove();
        check(q.size() == 2 && !q.contains(1), "iterator.remove removes the last returned");
        expectThrows(() -> it.remove(), IllegalStateException.class,
                     "second consecutive iterator.remove throws IllegalStateException");
        check(it.hasNext() && it.next() == 2, "iteration continues after remove");
    }

    static void testToArrayAndAddAll() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        q.add(1);
        q.add(2);
        Object[] arr = q.toArray();
        check(arr.length == 2 && arr[0].equals(1) && arr[1].equals(2), "toArray order");
        Integer[] typed = q.toArray(new Integer[0]);
        check(typed.length == 2 && typed[0] == 1 && typed[1] == 2, "toArray(T[])");
        Integer[] bigger = q.toArray(new Integer[4]);
        check(bigger.length == 4 && bigger[0] == 1 && bigger[1] == 2 && bigger[2] == null,
              "toArray(bigger) null-terminates at size");
        Integer[] smaller = q.toArray(new Integer[1]);
        check(smaller.length == 2 && smaller[0] == 1 && smaller[1] == 2,
              "toArray(smaller) returns exact new array");
        ConcurrentLinkedQueue<Integer> q2 = new ConcurrentLinkedQueue<>(Arrays.asList(3, 4, 5));
        q.addAll(q2);
        check(q.size() == 5, "addAll size");
        check(q.toString().equals("[1, 2, 3, 4, 5]"), "toString preserves order");
    }

    static void testSizeConsistency() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 100; i++) q.offer(i);
        check(q.size() == 100, "size is exact after 100 sequential offers");
        check(!q.isEmpty(), "not empty");
        int polled = 0;
        while (q.poll() != null) polled++;
        check(polled == 100 && q.size() == 0, "drained exactly 100");
    }

    static void testWeaklyConsistentIterator() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        Iterator<Integer> it = q.iterator();
        int seen = 0;
        boolean modified = false;
        while (it.hasNext()) {
            seen++;
            it.next();
            if (!modified) {
                modified = true;
                q.add(99); // concurrent modification (bounded) does not throw
            }
        }
        check(seen >= 3, "iterator completes without ConcurrentModificationException");
        check(q.contains(99) && q.size() == 4, "modification applied");
    }

    static void testSpliteratorAndStream() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = q.spliterator();
        int ch = sp.characteristics();
        check((ch & java.util.Spliterator.ORDERED) != 0, "spliterator ORDERED");
        check((ch & java.util.Spliterator.NONNULL) != 0, "spliterator NONNULL");
        check((ch & java.util.Spliterator.CONCURRENT) != 0, "spliterator CONCURRENT");
        check((ch & java.util.Spliterator.SIZED) == 0, "spliterator not SIZED (weakly consistent)");
        check(sp.estimateSize() == Long.MAX_VALUE, "estimateSize Long.MAX_VALUE");
        final int[] first = {-1};
        check(sp.tryAdvance(x -> first[0] = x) && first[0] == 1, "tryAdvance first element");
        java.util.List<Integer> rest = new java.util.ArrayList<>();
        sp.forEachRemaining(rest::add);
        check(rest.equals(Arrays.asList(2, 3)), "forEachRemaining after tryAdvance");
        check(q.stream().count() == 3, "stream count");
        check(q.parallelStream().count() == 3, "parallelStream count");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentLinkedQueue.class).getSerialVersionUID()
              == 196745693267521676L, "serialVersionUID locked");
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>(Arrays.asList(1, 2, 3));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(q);
        }
        @SuppressWarnings("unchecked")
        ConcurrentLinkedQueue<Integer> copy = (ConcurrentLinkedQueue<Integer>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.size() == 3, "round-trip size");
        check(copy.poll() == 1 && copy.poll() == 2 && copy.poll() == 3,
              "round-trip preserves FIFO order");
        check(copy.offer(4) && copy.poll() == 4, "resurrected queue usable");
    }

    static void testBlackBox() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>(Arrays.asList(1, 2, 3, 4, 5));

        // forEach order
        java.util.ArrayList<Integer> seen = new java.util.ArrayList<>();
        q.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3, 4, 5)), "forEach order");

        // removeAll / retainAll / removeIf
        check(q.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // spliterator
        ConcurrentLinkedQueue<Integer> spq = new ConcurrentLinkedQueue<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = spq.spliterator();
        check(sp.estimateSize() == Long.MAX_VALUE, "spliterator estimateSize Long.MAX_VALUE");
        java.util.ArrayList<Integer> viaSp = new java.util.ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator order");

        // toString
        check(spq.toString() != null && spq.toString().contains("1"), "toString non-null");
    }

    static void testConcurrentOfferPoll() throws Exception {
        // bounded producer/consumer proof of FIFO handoff (daemon threads, 5s cap)
        final ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        final int n = 1000;
        final java.util.concurrent.CountDownLatch produced = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch consumed = new java.util.concurrent.CountDownLatch(1);
        final int[] consumedCount = {0};
        final boolean[] ordered = {true};
        Thread producer = new Thread(() -> {
            for (int i = 0; i < n; i++) q.offer(i);
            produced.countDown();
        }, "clq-producer");
        Thread consumer = new Thread(() -> {
            int expected = 0;
            try {
                produced.await(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            Integer v;
            while (expected < n && (v = q.poll()) != null) {
                if (v != expected) ordered[0] = false;
                expected++;
            }
            consumedCount[0] = expected;
            consumed.countDown();
        }, "clq-consumer");
        producer.setDaemon(true);
        consumer.setDaemon(true);
        producer.start();
        consumer.start();
        check(consumed.await(5, java.util.concurrent.TimeUnit.SECONDS), "consumer finished within 5s");
        producer.join(5000);
        check(ordered[0], "consumed values arrive in FIFO order");
        // consumer may finish before producer: total consumed + remaining must equal n
        check(consumedCount[0] + q.size() == n, "no element lost (consumed + remaining == n)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testQueue();
        testNullRejection();
        testEmptyState();
        testFifoOrderExact();
        testQueueSemantics();
        testNullMatrix();
        testIteratorRemove();
        testToArrayAndAddAll();
        testSizeConsistency();
        testWeaklyConsistentIterator();
        testSpliteratorAndStream();
        testSerialization();
        testBlackBox();
        testConcurrentOfferPoll();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
