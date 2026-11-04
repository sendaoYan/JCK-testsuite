/*
 * @test
 * @summary java.util.concurrent.PriorityBlockingQueue conformance: white-box + black-box
 *          (natural/comparator priority ordering, poll/peek/take, unbounded
 *          remainingCapacity, blocking take handoff, drainTo, null rejection).
 * @run main PriorityBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityBlockingQueueTest {
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

    // non-serializable comparator (serialization must fail with NotSerializableException)
    static final class NonSerComp implements Comparator<Integer> {
        public int compare(Integer a, Integer b) { return Integer.compare(b, a); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.PriorityBlockingQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(int.class) != null
              && c.getConstructor(int.class, Comparator.class) != null, "constructors");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "offer", Object.class), "add/offer");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "poll") && hasMethod(c, "peek"), "poll/peek");
        check(hasMethod(c, "comparator"), "comparator");
        check(hasMethod(c, "remainingCapacity"), "remainingCapacity");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testNaturalOrdering() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        q.offer(3);
        q.offer(1);
        q.offer(4);
        q.offer(1);
        q.offer(5);
        check(q.size() == 5, "size");
        check(q.peek() == 1, "peek smallest");
        check(q.poll() == 1, "poll 1");
        check(q.poll() == 1, "poll duplicate 1");
        check(q.poll() == 3, "poll 3");
        check(q.poll() == 4, "poll 4");
        check(q.poll() == 5, "poll 5");
        check(q.poll() == null && q.isEmpty(), "empty after polls");
    }

    static void testComparatorOrdering() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(11, Comparator.reverseOrder());
        q.offer(1);
        q.offer(2);
        q.offer(3);
        check(q.comparator() != null, "comparator non-null");
        check(q.poll() == 3 && q.poll() == 2 && q.poll() == 1, "reverse comparator ordering");
    }

    static void testTakeBlocksWhenEmpty() throws Exception {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                result.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        q.put(10);
        done.await();
        check(result.get() == 10, "take blocks until element available");
        t.join();
    }

    static void testDrainToAndRemainingCapacity() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        check(q.remainingCapacity() == Integer.MAX_VALUE, "unbounded remainingCapacity");
        q.add(2);
        q.add(1);
        q.add(3);
        List<Integer> list = new ArrayList<>();
        check(q.drainTo(list) == 3, "drainTo count");
        // PriorityBlockingQueue.drainTo drains in priority (sorted) order.
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo priority order");
    }

    static void testNullRejection() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
    }

    static void testBlackBox() {
        // constructors (int)/(Collection)
        PriorityBlockingQueue<Integer> c1 = new PriorityBlockingQueue<>(16);
        check(c1.isEmpty(), "constructor(int) empty");
        PriorityBlockingQueue<Integer> c2 = new PriorityBlockingQueue<>(Arrays.asList(3, 1, 2));
        check(c2.size() == 3 && c2.peek() == 1, "constructor(Collection) heapified");

        // contains
        check(c2.contains(2) && !c2.contains(99), "contains");

        // toArray / toArray(T[])
        Object[] arr = c2.toArray();
        Arrays.sort(arr);
        check(arr.length == 3 && arr[0].equals(1) && arr[2].equals(3), "toArray() contents");
        Integer[] typed = c2.toArray(new Integer[0]);
        check(typed.length == 3, "toArray(T[]) length");

        // forEach
        List<Integer> seen = new ArrayList<>();
        c2.forEach(seen::add);
        seen.sort(Integer::compareTo);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach contents");

        // iterator enumerates all (heap order, not sorted)
        java.util.Iterator<Integer> it = c2.iterator();
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n == 3, "iterator enumerates all");

        // remove(Object)
        check(c2.remove(2), "remove(Object) true");
        check(!c2.remove(99), "remove(Object) missing false");

        // removeAll / retainAll / removeIf
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(Arrays.asList(1, 2, 3, 4, 5));
        check(q.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // spliterator
        PriorityBlockingQueue<Integer> spq = new PriorityBlockingQueue<>(Arrays.asList(3, 1, 2));
        java.util.Spliterator<Integer> sp = spq.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        List<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        viaSp.sort(Integer::compareTo);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator contents");

        // clear
        spq.clear();
        check(spq.isEmpty(), "clear empties");

        // toString
        PriorityBlockingQueue<Integer> ts = new PriorityBlockingQueue<>(Arrays.asList(1, 2));
        check(ts.toString() != null && ts.toString().contains("1"), "toString non-null");
    }

    // ---- Part 3: blocking-queue contract deepening ----
    static void testFixedSequencePollOrder() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        for (int v : new int[]{5, 3, 1, 4, 2}) q.offer(v);
        check(q.size() == 5, "fixed-sequence: size");
        boolean ascending = true;
        for (int i = 1; i <= 5; i++) ascending &= (q.poll() == i);
        check(ascending, "fixed-sequence 5,3,1,4,2 polls 1,2,3,4,5");
        check(q.isEmpty(), "fixed-sequence: empty after");
    }

    static void testCaseInsensitiveComparator() {
        PriorityBlockingQueue<String> q =
                new PriorityBlockingQueue<>(11, String.CASE_INSENSITIVE_ORDER);
        check(q.comparator() == String.CASE_INSENSITIVE_ORDER, "comparator() returns same instance");
        for (String s : new String[]{"b", "A", "c", "a"}) q.offer(s);
        check(q.size() == 4, "case-insensitive: size");
        check("A".equals(q.poll()), "case-insensitive: poll A first");
        check("a".equals(q.poll()), "case-insensitive: poll a second");
        check("b".equals(q.poll()) && "c".equals(q.poll()), "case-insensitive: poll b,c");
        check(q.isEmpty(), "case-insensitive: empty after");
    }

    static void testInitialCapacityIae() {
        IllegalArgumentException e0 = expectThrowsMsg(() -> new PriorityBlockingQueue<Integer>(0),
                IllegalArgumentException.class, "initialCapacity 0 IAE");
        check(e0.getMessage() == null, "initialCapacity 0 IAE has null message");
        IllegalArgumentException en = expectThrowsMsg(() -> new PriorityBlockingQueue<Integer>(-1),
                IllegalArgumentException.class, "initialCapacity negative IAE");
        check(en.getMessage() == null, "initialCapacity negative IAE has null message");
        IllegalArgumentException ec = expectThrowsMsg(
                () -> new PriorityBlockingQueue<Integer>(0, Comparator.reverseOrder()),
                IllegalArgumentException.class, "initialCapacity 0 with comparator IAE");
        check(ec.getMessage() == null, "initialCapacity 0+cmp IAE has null message");
        PriorityBlockingQueue<Integer> one = new PriorityBlockingQueue<>(1);
        check(one.offer(2) && one.offer(1) && one.size() == 2 && one.poll() == 1,
                "initialCapacity 1 usable and grows");
        new PriorityBlockingQueue<Integer>(11, null); // null comparator = natural order, allowed
        check(true, "null comparator constructor allowed");
    }

    static void testUnboundedGrowthHundred() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        boolean all = true;
        for (int i = 0; i < 100; i++) all &= q.offer((i * 37) % 100 + 1);
        check(all, "100 offers succeed (unbounded growth)");
        check(q.size() == 100, "size 100");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "remainingCapacity stays MAX_VALUE");
        boolean ascending = true;
        for (int i = 1; i <= 100; i++) ascending &= (q.poll() == i);
        check(ascending, "100 elements poll in exact ascending order");
        check(q.isEmpty(), "empty after draining 100");
    }

    static void testPutNeverBlocks() throws Exception {
        // PriorityBlockingQueue.put does not declare InterruptedException (never blocks).
        check(PriorityBlockingQueue.class.getMethod("put", Object.class).getExceptionTypes().length == 0,
                "put(Object) declares no checked exceptions");
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(1);
        q.put(1);
        q.put(2); // exceeds initial capacity, still immediate
        check(q.size() == 2 && q.poll() == 1 && q.poll() == 2, "put adds immediately, unbounded");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "remainingCapacity unbounded");
    }

    static void testIteratorMultisetAndWeakConsistency() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        for (int v : new int[]{5, 3, 1, 4, 2}) q.offer(v);
        List<Integer> iterated = new ArrayList<>();
        for (Integer v : q) iterated.add(v);
        List<Integer> sorted = new ArrayList<>(iterated);
        sorted.sort(Integer::compareTo);
        check(sorted.equals(Arrays.asList(1, 2, 3, 4, 5)),
                "iterator yields all elements as multiset (order not guaranteed)");
        // weak consistency: no CME on concurrent structural modification
        java.util.Iterator<Integer> it = q.iterator();
        check(it.next() != null, "iterator first element");
        q.poll();
        q.offer(6);
        List<Integer> rest = new ArrayList<>();
        while (it.hasNext()) rest.add(it.next()); // must not throw CME
        check(!rest.isEmpty(), "iterator continues after concurrent modifications, no CME");
    }

    static void testPeekAlwaysMin() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        int[] seq = {4, 2, 7, 1, 5};
        int min = Integer.MAX_VALUE;
        boolean invariant = true;
        for (int v : seq) {
            q.offer(v);
            min = Math.min(min, v);
            invariant &= (q.peek() == min);
        }
        check(invariant, "peek always returns the minimum across insert sequence");
        check(q.poll() == 1, "poll removes the minimum");
        check(q.peek() == 2, "peek returns new minimum after poll");
    }

    static void testDrainToEdges() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        for (int v : new int[]{5, 3, 1, 4, 2}) q.offer(v);
        List<Integer> sink = new ArrayList<>();
        check(q.drainTo(sink, 0) == 0, "drainTo(max=0) returns 0");
        check(sink.isEmpty() && q.size() == 5, "drainTo(max=0) mutates nothing");
        IllegalArgumentException iae = expectThrowsMsg(() -> q.drainTo(q),
                IllegalArgumentException.class, "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        check(q.size() == 5, "drainTo(self) leaves queue unchanged");
        List<Integer> part = new ArrayList<>();
        check(q.drainTo(part, 2) == 2 && part.equals(Arrays.asList(1, 2)),
                "drainTo(max) removes in ascending priority order, truncated");
        check(q.size() == 3 && q.peek() == 3, "drainTo(max) leaves larger elements");
        List<Integer> rest = new ArrayList<>();
        check(q.drainTo(rest) == 3 && rest.equals(Arrays.asList(3, 4, 5)),
                "drainTo remainder drains ascending with source cleared");
    }

    static void testTimedPollEmptyWaits() throws Exception {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        long t0 = System.nanoTime();
        check(q.poll(200, TimeUnit.MILLISECONDS) == null, "timed poll on empty waits then returns null");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed poll waited ~200ms");
    }

    static void testEmptyStateTable() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        check(q.peek() == null, "empty-state: peek null");
        expectThrows(() -> q.element(), NoSuchElementException.class, "empty-state: element NSEE");
        expectThrows(() -> q.remove(), NoSuchElementException.class, "empty-state: remove() NSEE");
        check(q.isEmpty() && q.size() == 0, "empty-state: size 0");
    }

    static void testSerializationComparators() throws Exception {
        // natural ordering round-trip
        PriorityBlockingQueue<Integer> natural = new PriorityBlockingQueue<>();
        for (int v : new int[]{5, 3, 1, 4, 2}) natural.offer(v);
        @SuppressWarnings("unchecked")
        PriorityBlockingQueue<Integer> rtNat = (PriorityBlockingQueue<Integer>) roundTrip(natural);
        check(rtNat.size() == 5, "natural round-trip preserves size");
        check(rtNat.comparator() == null, "natural round-trip keeps comparator null");
        boolean asc = true;
        for (int i = 1; i <= 5; i++) asc &= (rtNat.poll() == i);
        check(asc, "natural round-trip preserves ascending poll order");
        // comparator round-trip
        PriorityBlockingQueue<Integer> rev = new PriorityBlockingQueue<>(11, Comparator.reverseOrder());
        rev.offer(1);
        rev.offer(3);
        rev.offer(2);
        @SuppressWarnings("unchecked")
        PriorityBlockingQueue<Integer> rtRev = (PriorityBlockingQueue<Integer>) roundTrip(rev);
        check(rtRev.comparator() != null, "comparator round-trip preserves comparator");
        check(rtRev.poll() == 3 && rtRev.poll() == 2 && rtRev.poll() == 1,
                "comparator round-trip preserves descending poll order");
        // non-serializable comparator -> serialization fails
        final PriorityBlockingQueue<Integer> bad = new PriorityBlockingQueue<>(11, new NonSerComp());
        bad.offer(1);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(bad);
            check(false, "non-serializable comparator must fail serialization");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    static void testNullRejectionExtended() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> q.put(null), NullPointerException.class, "put(null)");
        expectThrows(() -> { q.offer(null, 1, TimeUnit.SECONDS); }, NullPointerException.class,
                "offer(null,timeout)");
        check(!q.contains(null), "contains(null) false");
        check(!q.remove(null), "remove(null) false");
        check(q.isEmpty(), "queue unchanged after null rejections");
    }

    static void testEqualsIdentitySemantics() {
        PriorityBlockingQueue<Integer> a = new PriorityBlockingQueue<>();
        PriorityBlockingQueue<Integer> b = new PriorityBlockingQueue<>();
        check(a.add(1) && a.add(2) && b.add(1) && b.add(2), "setup: equal contents, distinct instances");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "no element-based equals override (identity semantics)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(Arrays.asList(1, 2)), "queue not equal to List with same elements");
    }

    static void testContainsRemoveMiddle() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.contains(2), "contains middle element");
        check(q.remove(Integer.valueOf(2)), "remove middle element");
        check(!q.contains(2) && q.size() == 2, "middle element gone");
        check(q.poll() == 1 && q.poll() == 3, "remaining elements still poll in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrdering();
        testComparatorOrdering();
        testTakeBlocksWhenEmpty();
        testDrainToAndRemainingCapacity();
        testNullRejection();
        testBlackBox();
        testFixedSequencePollOrder();
        testCaseInsensitiveComparator();
        testInitialCapacityIae();
        testUnboundedGrowthHundred();
        testPutNeverBlocks();
        testIteratorMultisetAndWeakConsistency();
        testPeekAlwaysMin();
        testDrainToEdges();
        testTimedPollEmptyWaits();
        testEmptyStateTable();
        testSerializationComparators();
        testNullRejectionExtended();
        testEqualsIdentitySemantics();
        testContainsRemoveMiddle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
