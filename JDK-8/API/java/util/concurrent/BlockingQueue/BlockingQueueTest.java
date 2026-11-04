/*
 * @test
 * @summary java.util.concurrent.BlockingQueue API conformance: white-box structure
 *          + black-box behavior. Contract level: the javadoc four-forms matrix
 *          (throw / special value / block / timed-out x insert / remove / examine)
 *          driven via ArrayBlockingQueue in both empty and full states with exact
 *          IllegalStateException("Queue full") / NoSuchElementException messages,
 *          put/take producer-consumer pairing with bounded latches, put-blocks-
 *          when-full observed via thread state, remainingCapacity transitions,
 *          full drainTo matrix (order, max=0/negative, self, null), null-element
 *          rejection, contains/remove(Object), cross-implementation instanceofs.
 * @run main BlockingQueueTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class BlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectMsg(Runnable r, Class<? extends Throwable> type, String expected, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            if (expected != null && !expected.equals(t.getMessage()))
                throw new AssertionError(msg + ": expected message <" + expected
                                         + "> but got <" + t.getMessage() + ">", t);
            passed++;
        }
    }

    static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    // ---- original smoke test (kept) ----
    static void testBlockingQueue() throws Exception {
        BlockingQueue<String> q = new LinkedBlockingQueue<String>(2);
        check(q.offer("a") && q.offer("b"), "offer");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check(q.remainingCapacity() == 0, "remainingCapacity 0 when full");
        check("a".equals(q.poll()), "poll");
        check("b".equals(q.poll(1, TimeUnit.SECONDS)), "poll with timeout");
        check(q.poll() == null, "poll on empty returns null");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.BlockingQueue");
        check(c.isInterface(), "BlockingQueue is an interface");
        check(Queue.class.isAssignableFrom(c), "extends Queue");
        check(Collection.class.isAssignableFrom(c), "is a Collection");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == Queue.class,
              "direct superinterface is exactly Queue");
        check(c.getTypeParameters().length == 1, "one type parameter E");
        check("E".equals(c.getTypeParameters()[0].getName()), "type parameter named E");
        // method faces of the interface itself
        check(c.getMethod("put", Object.class).getReturnType() == void.class, "put(E) returns void");
        String[] names = {"put", "take", "offer", "poll", "remainingCapacity", "drainTo", "add", "remove", "contains"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods())
                if (m.getName().equals(n) && m.getDeclaringClass() == c) { found = true; break; }
            check(found, "BlockingQueue declares " + n);
        }
        check(c.getMethod("offer", Object.class, long.class, TimeUnit.class) != null, "offer(E,long,TimeUnit)");
        check(c.getMethod("poll", long.class, TimeUnit.class) != null, "poll(long,TimeUnit)");
        check(c.getMethod("drainTo", Collection.class).getReturnType() == int.class, "drainTo returns int");
        check(c.getMethod("drainTo", Collection.class, int.class).getReturnType() == int.class,
              "drainTo(c,max) returns int");
        check(c.getMethod("remainingCapacity").getReturnType() == int.class, "remainingCapacity returns int");
        // put/take/poll(timeout)/offer(timeout) declare InterruptedException
        for (java.lang.reflect.Method m : new java.lang.reflect.Method[] {
                c.getMethod("put", Object.class),
                c.getMethod("take"),
                c.getMethod("offer", Object.class, long.class, TimeUnit.class),
                c.getMethod("poll", long.class, TimeUnit.class)}) {
            boolean ie = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == InterruptedException.class) ie = true;
            check(ie, m.getName() + " declares InterruptedException");
        }
    }

    // ---- Part 2: javadoc four-forms matrix ----
    static void testFourFormsMatrix() {
        // INSERT row, capacity-1 queue: add/offer states
        final BlockingQueue<String> q = new ArrayBlockingQueue<String>(1);
        check(q.add("a"), "add succeeds with space");
        expectMsg(new Runnable() { public void run() { q.add("b"); } },
                  IllegalStateException.class, "Queue full", "add on full queue");
        check(q.size() == 1 && "a".equals(q.peek()), "failed add left queue unchanged");
        check(!q.offer("b"), "offer on full queue returns false");
        check(q.size() == 1, "failed offer left queue unchanged");
        try {
            check(!q.offer("b", 1, TimeUnit.MILLISECONDS), "timed offer on full queue returns false after timeout");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        check(q.size() == 1, "timed-out offer left queue unchanged");
        // timed offer with zero timeout also fails fast on full queue
        try {
            check(!q.offer("b", 0, TimeUnit.MILLISECONDS), "zero-timeout offer on full returns false");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }

        // REMOVE + EXAMINE rows, non-empty then empty
        check("a".equals(q.peek()), "peek returns head");
        check("a".equals(q.element()), "element returns head");
        check(q.size() == 1, "peek/element do not remove");
        check("a".equals(q.poll()), "poll returns head");
        check(q.poll() == null, "poll on empty returns null");
        check(q.peek() == null, "peek on empty returns null");
        expectMsg(new Runnable() { public void run() { q.element(); } },
                  NoSuchElementException.class, null, "element on empty queue");
        expectMsg(new Runnable() { public void run() { q.remove(); } },
                  NoSuchElementException.class, null, "remove on empty queue");
        try {
            check(q.poll(1, TimeUnit.MILLISECONDS) == null, "timed poll on empty returns null after timeout");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }

        // FIFO order with several elements
        BlockingQueue<String> fifo = new ArrayBlockingQueue<String>(3);
        check(fifo.add("a") && fifo.add("b") && fifo.add("c"), "fill to capacity");
        check("a".equals(fifo.peek()), "head is first inserted");
        check("a".equals(fifo.poll()) && "b".equals(fifo.poll()) && "c".equals(fifo.poll()),
              "FIFO removal order");
        Iterator<String> it = fifo.iterator();
        check(!it.hasNext(), "iterator of empty queue has no elements");
        fifo.add("x");
        fifo.add("y");
        List<String> seen = new ArrayList<String>();
        for (String s : fifo) seen.add(s);
        check(seen.equals(Arrays.asList("x", "y")), "iteration order is FIFO");
    }

    // ---- Part 3: put/take producer-consumer pairing ----
    static void testPutTakePairing() throws Exception {
        // producer put() -> main take()
        final BlockingQueue<String> q = new ArrayBlockingQueue<String>(1);
        final CountDownLatch produced = new CountDownLatch(1);
        Thread producer = daemon(new Runnable() { public void run() {
            try { q.put("v"); produced.countDown(); } catch (InterruptedException e) { }
        }});
        producer.start();
        check("v".equals(q.take()), "take receives the put element");
        check(produced.await(5, TimeUnit.SECONDS), "producer finished");
        producer.join(5000);

        // main put() -> consumer take()
        final CountDownLatch consumed = new CountDownLatch(1);
        final String[] got = new String[1];
        Thread consumer = daemon(new Runnable() { public void run() {
            try { got[0] = q.take(); consumed.countDown(); } catch (InterruptedException e) { }
        }});
        consumer.start();
        q.put("w");
        check(consumed.await(5, TimeUnit.SECONDS), "consumer received the element");
        consumer.join(5000);
        check("w".equals(got[0]), "take returned the exact transferred element");
        check(q.isEmpty(), "queue empty after handoff");
    }

    // ---- Part 4: put blocks while the queue is full ----
    static void testPutBlocksWhenFull() throws Exception {
        final BlockingQueue<String> q = new ArrayBlockingQueue<String>(1);
        q.add("occupied");
        final CountDownLatch putDone = new CountDownLatch(1);
        Thread producer = daemon(new Runnable() { public void run() {
            try { q.put("second"); putDone.countDown(); } catch (InterruptedException e) { }
        }});
        producer.start();
        // bounded spin until the producer parks inside put()
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (producer.getState() == Thread.State.RUNNABLE
               && System.nanoTime() < deadline) Thread.yield();
        check(producer.getState() == Thread.State.WAITING
              || producer.getState() == Thread.State.BLOCKED,
              "put is blocked while queue is full (state=" + producer.getState() + ")");
        check(putDone.getCount() == 1, "blocked put has not completed");
        check(q.size() == 1 && "occupied".equals(q.peek()), "element did not enter the full queue");
        // freeing space releases the blocked put
        check("occupied".equals(q.poll()), "head removed to free space");
        check(putDone.await(5, TimeUnit.SECONDS), "blocked put completed after space freed");
        producer.join(5000);
        check(q.size() == 1 && "second".equals(q.peek()), "blocked put's element now queued");
    }

    // ---- Part 5: remainingCapacity ----
    static void testRemainingCapacity() {
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(3);
        check(q.remainingCapacity() == 3, "remainingCapacity equals capacity when empty");
        q.offer("a");
        check(q.remainingCapacity() == 2 && q.size() == 1, "remaining+size=capacity after insert");
        q.offer("b");
        check(q.remainingCapacity() == 1, "remainingCapacity decrements");
        q.offer("c");
        check(q.remainingCapacity() == 0 && q.size() == 3, "remainingCapacity 0 when full");
        q.poll();
        check(q.remainingCapacity() == 1, "removal restores remainingCapacity");
        q.clear();
        check(q.remainingCapacity() == 3 && q.isEmpty(), "clear restores remainingCapacity");
        // unbounded queue reports Integer.MAX_VALUE (LinkedBlockingQueue default ctor)
        BlockingQueue<String> unbounded = new LinkedBlockingQueue<String>();
        check(unbounded.remainingCapacity() == Integer.MAX_VALUE,
              "unsized LinkedBlockingQueue remainingCapacity is Integer.MAX_VALUE");
    }

    // ---- Part 6: drainTo matrix ----
    static void testDrainTo() throws Exception {
        BlockingQueue<String> q = new LinkedBlockingQueue<String>();
        q.add("a"); q.add("b"); q.add("c");
        List<String> dest = new ArrayList<String>();
        check(q.drainTo(dest) == 3, "drainTo returns number transferred");
        check(dest.equals(Arrays.asList("a", "b", "c")), "drainTo preserves FIFO order");
        check(q.isEmpty() && q.size() == 0, "drainTo emptied the source");

        // partial drain with max
        q.add("a"); q.add("b"); q.add("c");
        List<String> part = new ArrayList<String>();
        check(q.drainTo(part, 2) == 2, "drainTo(max) returns at most max");
        check(part.equals(Arrays.asList("a", "b")), "drainTo(max) moved the first max elements");
        check(q.size() == 1 && "c".equals(q.peek()), "drainTo(max) left the remainder");

        // max greater than size drains everything
        check(q.drainTo(part, 10) == 1, "drainTo(max>size) drains all remaining");
        check(part.equals(Arrays.asList("a", "b", "c")), "destination appended in order");
        check(q.isEmpty(), "source emptied by oversized max");

        // max 0 and negative transfer nothing (measured behavior: no exception)
        q.add("a");
        check(q.drainTo(part, 0) == 0, "drainTo(max=0) transfers nothing");
        check(q.drainTo(part, -1) == 0, "drainTo(max<0) transfers nothing");
        check(q.size() == 1, "queue untouched by max=0/negative drains");

        // drain from empty queue returns 0, destination untouched
        q.clear();
        check(q.drainTo(part) == 0, "drainTo on empty queue returns 0");
        check(part.size() == 3, "destination unchanged by empty drain");

        // drain appends to a non-empty destination
        q.add("z");
        part.clear();
        part.add("pre");
        check(q.drainTo(part) == 1, "drain into non-empty destination");
        check(part.equals(Arrays.asList("pre", "z")), "drainTo appends after existing elements");

        // null destination and self-drain are rejected
        expectMsg(new Runnable() { public void run() { q.drainTo(null); } },
                  NullPointerException.class, null, "drainTo(null)");
        expectMsg(new Runnable() { public void run() { q.drainTo(q); } },
                  IllegalArgumentException.class, null, "drainTo into itself");
    }

    // ---- Part 7: null-element rejection ----
    static void testNullRejection() {
        final BlockingQueue<String> q = new ArrayBlockingQueue<String>(2);
        expectMsg(new Runnable() { public void run() { q.add(null); } },
                  NullPointerException.class, null, "add(null)");
        expectMsg(new Runnable() { public void run() { q.offer(null); } },
                  NullPointerException.class, null, "offer(null)");
        expectMsg(new Runnable() { public void run() {
            try { q.put(null); } catch (InterruptedException e) { throw new AssertionError(e); }
        } }, NullPointerException.class, null, "put(null)");
        try {
            q.offer(null, 1, TimeUnit.MILLISECONDS);
            check(false, "timed offer(null) must throw NPE");
        } catch (NullPointerException e) {
            check(true, "timed offer(null) throws NullPointerException");
        } catch (InterruptedException e) {
            throw new AssertionError("unexpected interrupt", e);
        }
        check(q.isEmpty(), "queue still empty after null rejections");
        // contains(null)/remove(null) report false rather than throwing (measured)
        check(!q.contains(null), "contains(null) false on empty");
        check(!q.remove((Object) null), "remove(null) false on empty");
        q.add("a");
        check(!q.contains(null), "contains(null) false with content");
    }

    // ---- Part 8: search operations ----
    static void testSearchOps() {
        BlockingQueue<String> q = new LinkedBlockingQueue<String>();
        q.add("a"); q.add("b"); q.add("c");
        check(q.contains("b"), "contains present element");
        check(!q.contains("z"), "contains absent element false");
        check(q.remove("b"), "remove(Object) removes middle element");
        check(q.size() == 2 && !q.contains("b"), "element gone after remove");
        check("a".equals(q.poll()) && "c".equals(q.poll()), "order preserved around removed element");
        check(!q.remove("b"), "remove(Object) absent returns false");
        check(q.isEmpty(), "queue drained");
    }

    // ---- Part 9: cross-implementation instanceofs ----
    static void testCrossImplementations() {
        check(new ArrayBlockingQueue<String>(1) instanceof BlockingQueue, "ArrayBlockingQueue is a BlockingQueue");
        check(new LinkedBlockingQueue<String>() instanceof BlockingQueue, "LinkedBlockingQueue is a BlockingQueue");
        check(new LinkedBlockingDeque<String>() instanceof BlockingQueue, "LinkedBlockingDeque is a BlockingQueue");
        check(new PriorityBlockingQueue<String>() instanceof BlockingQueue, "PriorityBlockingQueue is a BlockingQueue");
        check(new DelayQueue() instanceof BlockingQueue, "DelayQueue is a BlockingQueue");
        check(new SynchronousQueue<String>() instanceof BlockingQueue, "SynchronousQueue is a BlockingQueue");
        check(new LinkedTransferQueue<String>() instanceof BlockingQueue, "LinkedTransferQueue is a BlockingQueue");
        check(new LinkedTransferQueue<String>() instanceof TransferQueue, "LinkedTransferQueue is a TransferQueue");
        check(new java.util.concurrent.ConcurrentLinkedQueue<String>() instanceof BlockingQueue == false,
              "ConcurrentLinkedQueue is NOT a BlockingQueue");
        check(BlockingQueue.class.isAssignableFrom(TransferQueue.class), "TransferQueue extends BlockingQueue");

        // SynchronousQueue: zero capacity semantics through the BlockingQueue face
        BlockingQueue<String> sq = new SynchronousQueue<String>();
        check(sq.remainingCapacity() == 0, "SynchronousQueue remainingCapacity is 0");
        check(sq.isEmpty(), "SynchronousQueue is always empty");
        check(!sq.offer("x"), "offer without a waiting taker returns false");
        check(sq.poll() == null, "poll without a waiting giver returns null");
        check(!sq.remove("x"), "remove on empty SynchronousQueue false");
        expectMsg(new Runnable() { public void run() { sq.add("x"); } },
                  IllegalStateException.class, "Queue full", "add on SynchronousQueue reports full");
    }

    public static void main(String[] args) throws Exception {
        testBlockingQueue();
        testWhiteBox();
        testFourFormsMatrix();
        testPutTakePairing();
        testPutBlocksWhenFull();
        testRemainingCapacity();
        testDrainTo();
        testNullRejection();
        testSearchOps();
        testCrossImplementations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
