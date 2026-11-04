/*
 * @test
 * @summary java.util.concurrent.TransferQueue API conformance: white-box structure
 *          + black-box behavior. Contract level: tryTransfer without a consumer
 *          returns false and never enqueues; tryTransfer/timed tryTransfer with a
 *          waiting consumer succeeds and hands off directly; transfer blocks (no
 *          consumer) observed via producer thread state, then hands off on take;
 *          hasWaitingConsumer / getWaitingConsumerCount transitions (0 / 1 / 2 and
 *          back) at deterministic synchronization points; FIFO queue operations
 *          through the BlockingQueue face; LinkedTransferQueue vs SynchronousQueue
 *          interface lattice.
 * @run main TransferQueueTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class TransferQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    // ---- original smoke test (kept) ----
    static void testTransferQueue() throws Exception {
        TransferQueue<String> q = new LinkedTransferQueue<String>();
        check(q.getWaitingConsumerCount() == 0, "no waiting consumers initially");
        q.put("a");
        check(q.tryTransfer("b", 100, TimeUnit.MILLISECONDS) || q.size() > 0, "tryTransfer without blocking");
        check("a".equals(q.take()), "take");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.TransferQueue");
        check(c.isInterface(), "TransferQueue is an interface");
        check(BlockingQueue.class.isAssignableFrom(c), "extends BlockingQueue");
        check(Queue.class.isAssignableFrom(c), "is a Queue");
        check(Collection.class.isAssignableFrom(c), "is a Collection");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == BlockingQueue.class,
              "direct superinterface is exactly BlockingQueue");
        check(c.getTypeParameters().length == 1, "one type parameter E");
        // method faces
        check(c.getMethod("tryTransfer", Object.class).getReturnType() == boolean.class,
              "tryTransfer(E) returns boolean");
        check(c.getMethod("transfer", Object.class).getReturnType() == void.class,
              "transfer(E) returns void");
        check(c.getMethod("tryTransfer", Object.class, long.class, TimeUnit.class).getReturnType() == boolean.class,
              "timed tryTransfer returns boolean");
        check(c.getMethod("hasWaitingConsumer").getReturnType() == boolean.class,
              "hasWaitingConsumer returns boolean");
        check(c.getMethod("getWaitingConsumerCount").getReturnType() == int.class,
              "getWaitingConsumerCount returns int");
        // transfer family declares InterruptedException
        for (java.lang.reflect.Method m : new java.lang.reflect.Method[] {
                c.getMethod("transfer", Object.class),
                c.getMethod("tryTransfer", Object.class, long.class, TimeUnit.class)}) {
            boolean ie = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == InterruptedException.class) ie = true;
            check(ie, m.getName() + " declares InterruptedException");
        }
        // tryTransfer(E) does NOT declare checked exceptions
        java.lang.reflect.Method tt = c.getMethod("tryTransfer", Object.class);
        check(tt.getExceptionTypes().length == 0, "immediate tryTransfer declares no checked exceptions");
    }

    // ---- Part 2: tryTransfer without a consumer ----
    static void testTryTransferNoConsumer() throws Exception {
        TransferQueue<String> q = new LinkedTransferQueue<String>();
        check(q.getWaitingConsumerCount() == 0, "no waiting consumers initially");
        check(!q.hasWaitingConsumer(), "hasWaitingConsumer false with no consumers");
        check(!q.tryTransfer("x"), "tryTransfer returns false with no consumer");
        check(q.size() == 0 && q.isEmpty(), "failed tryTransfer enqueues nothing");
        check(q.poll() == null, "nothing became available after failed tryTransfer");
        check(!q.tryTransfer("y", 100, TimeUnit.MILLISECONDS),
              "timed tryTransfer times out with no consumer");
        check(q.size() == 0 && q.isEmpty(), "timed-out tryTransfer enqueues nothing");
        // a queued element from put is taken FIFO even after failed transfers
        q.put("a");
        check(q.size() == 1, "put queues an element");
        check("a".equals(q.take()), "take returns queued element");
        check(q.isEmpty(), "queue empty again");
    }

    // ---- Part 3: tryTransfer with a waiting consumer ----
    static void testTryTransferWithConsumer() throws Exception {
        final LinkedTransferQueue<String> q = new LinkedTransferQueue<String>();
        final CountDownLatch consumed = new CountDownLatch(1);
        final String[] got = new String[1];
        Thread consumer = daemon(new Runnable() { public void run() {
            try { got[0] = q.take(); consumed.countDown(); } catch (InterruptedException e) { }
        }});
        consumer.start();
        // bounded spin until the consumer parks inside take()
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (q.getWaitingConsumerCount() == 0 && System.nanoTime() < deadline) Thread.yield();
        check(q.getWaitingConsumerCount() == 1, "exactly one waiting consumer observed");
        check(q.hasWaitingConsumer(), "hasWaitingConsumer true with a parked taker");
        check(q.tryTransfer("z"), "tryTransfer hands off to the waiting consumer");
        check(q.size() == 0, "successful tryTransfer enqueues nothing");
        check(consumed.await(5, TimeUnit.SECONDS), "consumer finished after handoff");
        consumer.join(5000);
        check("z".equals(got[0]), "consumer received the exact transferred element");
        check(q.getWaitingConsumerCount() == 0, "waiting count back to 0");
        check(!q.hasWaitingConsumer(), "hasWaitingConsumer false after consumption");

        // timed tryTransfer with an already-waiting consumer succeeds immediately
        final CountDownLatch consumed2 = new CountDownLatch(1);
        final String[] got2 = new String[1];
        Thread consumer2 = daemon(new Runnable() { public void run() {
            try { got2[0] = q.take(); consumed2.countDown(); } catch (InterruptedException e) { }
        }});
        consumer2.start();
        deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (q.getWaitingConsumerCount() == 0 && System.nanoTime() < deadline) Thread.yield();
        check(q.tryTransfer("w", 5, TimeUnit.SECONDS), "timed tryTransfer succeeds with waiting consumer");
        check(consumed2.await(5, TimeUnit.SECONDS), "second consumer finished");
        consumer2.join(5000);
        check("w".equals(got2[0]), "second consumer received its element");
    }

    // ---- Part 4: transfer blocks until a consumer arrives ----
    static void testTransferBlocks() throws Exception {
        final LinkedTransferQueue<String> q = new LinkedTransferQueue<String>();
        final CountDownLatch transferred = new CountDownLatch(1);
        Thread producer = daemon(new Runnable() { public void run() {
            try { q.transfer("T"); transferred.countDown(); } catch (InterruptedException e) { }
        }});
        producer.start();
        // bounded spin until the producer parks inside transfer()
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (producer.getState() == Thread.State.RUNNABLE
               && System.nanoTime() < deadline) Thread.yield();
        check(producer.getState() == Thread.State.WAITING
              || producer.getState() == Thread.State.BLOCKED,
              "transfer blocks with no consumer (state=" + producer.getState() + ")");
        check(transferred.getCount() == 1, "blocked transfer has not returned");
        check(q.size() == 1 && !q.isEmpty(), "element is held by the blocked transfer");
        // a consumer taking releases the blocked transfer
        final CountDownLatch consumed = new CountDownLatch(1);
        final String[] got = new String[1];
        Thread consumer = daemon(new Runnable() { public void run() {
            try { got[0] = q.take(); consumed.countDown(); } catch (InterruptedException e) { }
        }});
        consumer.start();
        check(transferred.await(5, TimeUnit.SECONDS), "transfer returned after handoff");
        check(consumed.await(5, TimeUnit.SECONDS), "consumer finished");
        producer.join(5000);
        consumer.join(5000);
        check("T".equals(got[0]), "consumer received the transferred element");
        check(q.isEmpty(), "queue empty after handoff");
    }

    // ---- Part 5: two waiting consumers ----
    static void testTwoWaitingConsumers() throws Exception {
        final LinkedTransferQueue<String> q = new LinkedTransferQueue<String>();
        final CountDownLatch c1done = new CountDownLatch(1);
        final CountDownLatch c2done = new CountDownLatch(1);
        final List<String> received = java.util.Collections.synchronizedList(new ArrayList<String>());
        Thread c1 = daemon(new Runnable() { public void run() {
            try { received.add(q.take()); c1done.countDown(); } catch (InterruptedException e) { }
        }});
        Thread c2 = daemon(new Runnable() { public void run() {
            try { received.add(q.take()); c2done.countDown(); } catch (InterruptedException e) { }
        }});
        c1.start();
        c2.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (q.getWaitingConsumerCount() < 2 && System.nanoTime() < deadline) Thread.yield();
        check(q.getWaitingConsumerCount() == 2, "both consumers observed waiting");
        check(q.hasWaitingConsumer(), "hasWaitingConsumer true with two takers");
        // each tryTransfer hands off to exactly one waiting consumer
        check(q.tryTransfer("A"), "first tryTransfer handed off");
        check(q.tryTransfer("B"), "second tryTransfer handed off");
        check(c1done.await(5, TimeUnit.SECONDS) && c2done.await(5, TimeUnit.SECONDS),
              "both consumers served");
        c1.join(5000);
        c2.join(5000);
        // handoff order between the two consumers is unspecified: compare as a set
        List<String> sorted = new ArrayList<String>(received);
        java.util.Collections.sort(sorted);
        check(sorted.equals(Arrays.asList("A", "B")), "both transferred elements were received");
        check(q.getWaitingConsumerCount() == 0, "no waiting consumers remain");
    }

    // ---- Part 6: BlockingQueue face on an unbounded TransferQueue ----
    static void testQueueOperations() throws Exception {
        TransferQueue<String> q = new LinkedTransferQueue<String>();
        check(q.isEmpty() && q.size() == 0, "new queue empty");
        check(q.peek() == null && q.poll() == null, "empty probes");
        check(q.offer("a"), "offer succeeds (unbounded)");
        q.put("b");
        check(q.add("c"), "add succeeds (unbounded)");
        check(q.size() == 3, "three elements queued");
        check("a".equals(q.peek()), "peek head");
        check("a".equals(q.element()), "element head");
        check("a".equals(q.take()), "take removes head");
        check("b".equals(q.poll(1, TimeUnit.SECONDS)), "timed poll removes head");
        check("c".equals(q.poll()), "poll removes head");
        check(q.isEmpty(), "queue drained");
        // FIFO iteration
        q.add("x");
        q.add("y");
        List<String> seen = new ArrayList<String>();
        for (Iterator<String> it = q.iterator(); it.hasNext(); ) seen.add(it.next());
        check(seen.equals(Arrays.asList("x", "y")), "iteration order FIFO");
        // drainTo through the queue face
        List<String> drained = new ArrayList<String>();
        check(q.drainTo(drained) == 2, "drainTo transfers all");
        check(drained.equals(Arrays.asList("x", "y")), "drainTo FIFO order");
        check(q.isEmpty(), "drained empty");
        // remainingCapacity of the unbounded queue is Integer.MAX_VALUE
        check(q.remainingCapacity() == Integer.MAX_VALUE, "unbounded remainingCapacity");
    }

    // ---- Part 7: interface lattice ----
    static void testHierarchy() {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<String>();
        check(ltq instanceof TransferQueue, "LinkedTransferQueue implements TransferQueue");
        check(ltq instanceof BlockingQueue, "LinkedTransferQueue implements BlockingQueue");
        check(ltq instanceof Queue, "LinkedTransferQueue implements Queue");
        check(TransferQueue.class.isAssignableFrom(LinkedTransferQueue.class),
              "TransferQueue.isAssignableFrom(LinkedTransferQueue)");
        check(BlockingQueue.class.isAssignableFrom(TransferQueue.class),
              "TransferQueue extends BlockingQueue");
        // SynchronousQueue is a BlockingQueue but NOT a TransferQueue
        SynchronousQueue<String> sq = new SynchronousQueue<String>();
        check(sq instanceof BlockingQueue, "SynchronousQueue implements BlockingQueue");
        check(!(sq instanceof TransferQueue), "SynchronousQueue is NOT a TransferQueue");
        // ArrayBlockingQueue is not a TransferQueue either
        check(!(new ArrayBlockingQueue<String>(1) instanceof TransferQueue),
              "ArrayBlockingQueue is NOT a TransferQueue");
        // usable through the supertype reference
        TransferQueue<String> tq = ltq;
        BlockingQueue<String> bq = tq;
        check(bq.offer("k") && "k".equals(bq.peek()), "operations through BlockingQueue reference");
        check("k".equals(bq.poll()), "poll through BlockingQueue reference");
        check(tq.getWaitingConsumerCount() == 0, "transfer queries through interface reference");
        check(!tq.tryTransfer("z"), "tryTransfer through interface reference with no consumer");
        check(bq.isEmpty(), "failed transfer left queue empty");
    }

    public static void main(String[] args) throws Exception {
        testTransferQueue();
        testWhiteBox();
        testTryTransferNoConsumer();
        testTryTransferWithConsumer();
        testTransferBlocks();
        testTwoWaitingConsumers();
        testQueueOperations();
        testHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
