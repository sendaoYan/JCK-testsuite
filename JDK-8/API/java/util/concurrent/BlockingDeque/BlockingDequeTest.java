/*
 * @test
 * @summary java.util.concurrent.BlockingDeque API conformance: white-box structure
 *          + black-box behavior. Contract level: the full head/tail 12-method matrix
 *          (addFirst/addLast, offerFirst/offerLast, putFirst/putLast, pollFirst/
 *          pollLast, takeFirst/takeLast, peekFirst/peekLast + timed variants) driven
 *          via LinkedBlockingDeque(2) in both empty and full states with exact
 *          IllegalStateException("Deque full") / NoSuchElementException results,
 *          removeFirstOccurrence/removeLastOccurrence, push/pop stack semantics,
 *          BlockingQueue-inherited method equivalence (add~addLast, remove~
 *          removeFirst, peek~peekFirst, ...), null rejection, take pairing with
 *          producer threads via bounded latches, interface hierarchy.
 * @run main BlockingDequeTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class BlockingDequeTest {
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
    static void testBlockingDeque() throws Exception {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>(2);
        d.addFirst("a");
        d.addLast("b");
        check("b".equals(d.peekLast()), "peekLast");
        check("a".equals(d.takeFirst()), "takeFirst");
        check("b".equals(d.pollFirst(1, TimeUnit.SECONDS)), "pollFirst with timeout");
        check(d.peek() == null, "empty after draining");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.BlockingDeque");
        check(c.isInterface(), "BlockingDeque is an interface");
        check(BlockingQueue.class.isAssignableFrom(c), "extends BlockingQueue");
        check(Deque.class.isAssignableFrom(c), "extends Deque");
        check(Queue.class.isAssignableFrom(c), "is a Queue");
        check(Collection.class.isAssignableFrom(c), "is a Collection");
        Class<?>[] supers = c.getInterfaces();
        List<Class<?>> sl = Arrays.asList(supers);
        check(supers.length == 2 && sl.contains(BlockingQueue.class) && sl.contains(Deque.class),
              "direct superinterfaces are BlockingQueue and Deque");
        check(c.getTypeParameters().length == 1, "one type parameter E");
        // head/tail blocking faces
        check(c.getMethod("addFirst", Object.class).getReturnType() == void.class, "addFirst returns void");
        check(c.getMethod("addLast", Object.class).getReturnType() == void.class, "addLast returns void");
        check(c.getMethod("putFirst", Object.class).getReturnType() == void.class, "putFirst returns void");
        check(c.getMethod("putLast", Object.class).getReturnType() == void.class, "putLast returns void");
        check(c.getMethod("takeFirst") != null && c.getMethod("takeLast") != null, "takeFirst/takeLast faces");
        check(c.getMethod("offerFirst", Object.class, long.class, TimeUnit.class) != null, "offerFirst timed");
        check(c.getMethod("offerLast", Object.class, long.class, TimeUnit.class) != null, "offerLast timed");
        check(c.getMethod("pollFirst", long.class, TimeUnit.class) != null, "pollFirst timed");
        check(c.getMethod("pollLast", long.class, TimeUnit.class) != null, "pollLast timed");
        check(c.getMethod("removeFirstOccurrence", Object.class).getReturnType() == boolean.class,
              "removeFirstOccurrence returns boolean");
        check(c.getMethod("removeLastOccurrence", Object.class).getReturnType() == boolean.class,
              "removeLastOccurrence returns boolean");
        check(c.getMethod("push", Object.class).getReturnType() == void.class, "push returns void");
        // put-family declares InterruptedException
        for (java.lang.reflect.Method m : new java.lang.reflect.Method[] {
                c.getMethod("putFirst", Object.class),
                c.getMethod("putLast", Object.class),
                c.getMethod("takeFirst"),
                c.getMethod("takeLast")}) {
            boolean ie = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == InterruptedException.class) ie = true;
            check(ie, m.getName() + " declares InterruptedException");
        }
    }

    // ---- Part 2: head/tail matrix, empty state ----
    static void testEmptyState() throws Exception {
        final BlockingDeque<String> d = new LinkedBlockingDeque<String>(2);
        check(d.isEmpty() && d.size() == 0, "new deque is empty");
        check(d.pollFirst() == null, "pollFirst on empty returns null");
        check(d.pollLast() == null, "pollLast on empty returns null");
        check(d.peekFirst() == null, "peekFirst on empty returns null");
        check(d.peekLast() == null, "peekLast on empty returns null");
        check(d.pollFirst(1, TimeUnit.MILLISECONDS) == null, "timed pollFirst on empty returns null");
        check(d.pollLast(1, TimeUnit.MILLISECONDS) == null, "timed pollLast on empty returns null");
        expectMsg(new Runnable() { public void run() { d.getFirst(); } },
                  NoSuchElementException.class, null, "getFirst on empty");
        expectMsg(new Runnable() { public void run() { d.getLast(); } },
                  NoSuchElementException.class, null, "getLast on empty");
        expectMsg(new Runnable() { public void run() { d.removeFirst(); } },
                  NoSuchElementException.class, null, "removeFirst on empty");
        expectMsg(new Runnable() { public void run() { d.removeLast(); } },
                  NoSuchElementException.class, null, "removeLast on empty");
        expectMsg(new Runnable() { public void run() { d.pop(); } },
                  NoSuchElementException.class, null, "pop on empty");
        check(d.isEmpty(), "empty-state probes left no elements");
    }

    // ---- Part 3: head/tail matrix, full state ----
    static void testFullState() throws Exception {
        final BlockingDeque<String> d = new LinkedBlockingDeque<String>(2);
        d.addFirst("a");
        d.addLast("b");
        check(d.size() == 2, "deque full at capacity 2");
        expectMsg(new Runnable() { public void run() { d.addFirst("c"); } },
                  IllegalStateException.class, "Deque full", "addFirst on full");
        expectMsg(new Runnable() { public void run() { d.addLast("c"); } },
                  IllegalStateException.class, "Deque full", "addLast on full");
        expectMsg(new Runnable() { public void run() { d.push("c"); } },
                  IllegalStateException.class, "Deque full", "push on full");
        check(!d.offerFirst("c"), "offerFirst on full returns false");
        check(!d.offerLast("c"), "offerLast on full returns false");
        check(!d.offerFirst("c", 1, TimeUnit.MILLISECONDS), "timed offerFirst on full returns false");
        check(!d.offerLast("c", 1, TimeUnit.MILLISECONDS), "timed offerLast on full returns false");
        check(d.size() == 2 && "a".equals(d.peekFirst()) && "b".equals(d.peekLast()),
              "failed inserts left deque unchanged");
    }

    // ---- Part 4: head/tail operations, non-empty state ----
    static void testHeadTailOperations() throws Exception {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>(4);
        d.addFirst("m");
        d.addFirst("l");          // l m
        d.addLast("n");           // l m n
        d.addLast("o");           // l m n o
        check(d.size() == 4, "four elements inserted");
        check("l".equals(d.peekFirst()), "peekFirst is the head");
        check("o".equals(d.peekLast()), "peekLast is the tail");
        check(d.size() == 4, "peek does not remove");
        check("l".equals(d.pollFirst()), "pollFirst removes the head");
        check("o".equals(d.pollLast()), "pollLast removes the tail");
        check(d.size() == 2, "size after end removals");
        check("m".equals(d.pollFirst(1, TimeUnit.SECONDS)), "timed pollFirst removes head");
        check("n".equals(d.pollLast(1, TimeUnit.SECONDS)), "timed pollLast removes tail");
        check(d.isEmpty(), "deque drained from both ends");
        // putFirst/putLast deliver to the matching ends
        d.putFirst("h1");
        d.putLast("t1");
        d.putFirst("h2");         // h2 h1 t1
        check("h2".equals(d.peekFirst()) && "t1".equals(d.peekLast()), "putFirst/putLast order");
        check("h2".equals(d.takeFirst()), "takeFirst removes most recent putFirst");
        check("h1".equals(d.takeFirst()), "takeFirst FIFO within head-inserts");
        check("t1".equals(d.takeLast()), "takeLast removes the tail");
        check(d.isEmpty(), "deque empty after takes");
    }

    // ---- Part 5: takeFirst/takeLast pairing with producers ----
    static void testTakePairing() throws Exception {
        // producer putLast -> main takeFirst (FIFO delivery)
        final BlockingDeque<String> d = new LinkedBlockingDeque<String>();
        final CountDownLatch produced = new CountDownLatch(1);
        Thread producer = daemon(new Runnable() { public void run() {
            try { d.putLast("v"); produced.countDown(); } catch (InterruptedException e) { }
        }});
        producer.start();
        check("v".equals(d.takeFirst()), "takeFirst receives putLast element");
        check(produced.await(5, TimeUnit.SECONDS), "producer finished");
        producer.join(5000);

        // producer putFirst -> main takeLast (cross-end delivery)
        final CountDownLatch produced2 = new CountDownLatch(1);
        Thread producer2 = daemon(new Runnable() { public void run() {
            try { d.putFirst("w"); produced2.countDown(); } catch (InterruptedException e) { }
        }});
        producer2.start();
        check("w".equals(d.takeLast()), "takeLast receives putFirst element");
        check(produced2.await(5, TimeUnit.SECONDS), "second producer finished");
        producer2.join(5000);
        check(d.isEmpty(), "deque empty after both pairings");

        // main putLast -> consumer takeLast
        final CountDownLatch consumed = new CountDownLatch(1);
        final String[] got = new String[1];
        Thread consumer = daemon(new Runnable() { public void run() {
            try { got[0] = d.takeLast(); consumed.countDown(); } catch (InterruptedException e) { }
        }});
        consumer.start();
        d.putLast("x");
        check(consumed.await(5, TimeUnit.SECONDS), "consumer received element");
        consumer.join(5000);
        check("x".equals(got[0]), "takeLast returned the exact element");
    }

    // ---- Part 6: removeFirstOccurrence / removeLastOccurrence ----
    static void testOccurrenceRemoval() {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>();
        d.addLast("a"); d.addLast("b"); d.addLast("a"); d.addLast("c");   // a b a c
        check(d.removeFirstOccurrence("a"), "removeFirstOccurrence finds first match");
        check("b".equals(d.peekFirst()), "head occurrence removed first");
        check(d.size() == 3, "only one element removed");
        check(d.removeLastOccurrence("a"), "removeLastOccurrence finds last match");
        check(!d.contains("a"), "all a-occurrences gone");
        check(d.peekFirst().equals("b") && d.peekLast().equals("c"), "survivors in order");
        check(!d.removeFirstOccurrence("z"), "removeFirstOccurrence absent false");
        check(!d.removeLastOccurrence("z"), "removeLastOccurrence absent false");
        check(!d.removeFirstOccurrence(null), "removeFirstOccurrence(null) false (measured)");
        check(!d.removeLastOccurrence(null), "removeLastOccurrence(null) false (measured)");
        check(d.size() == 2, "failed removals left size unchanged");
        // occurrence removal reaches both ends from the correct side
        BlockingDeque<String> e = new LinkedBlockingDeque<String>();
        e.addLast("k"); e.addLast("k");
        check(e.removeLastOccurrence("k"), "single distinct pair: last removed");
        check("k".equals(e.peekFirst()) && "k".equals(e.peekLast()), "one k remains");
    }

    // ---- Part 7: push/pop stack semantics ----
    static void testStackSemantics() {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>();
        d.push("a");
        d.push("b");
        d.push("c");
        check(d.size() == 3, "three pushes");
        check("c".equals(d.peek()), "peek shows last pushed (LIFO)");
        check("c".equals(d.pop()), "pop returns last pushed");
        check("b".equals(d.pop()), "pop returns second-to-last");
        check("c".equals(d.peek()) == false, "popped elements gone");
        check("a".equals(d.pop()), "pop returns first pushed");
        check(d.isEmpty(), "stack drained LIFO");
        expectMsg(new Runnable() { public void run() { d.pop(); } },
                  NoSuchElementException.class, null, "pop on empty stack");
    }

    // ---- Part 8: BlockingQueue-inherited method equivalence ----
    static void testBlockingQueueBridge() throws Exception {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>(4);
        // add(e) behaves as addLast(e): inserts at the tail
        check(d.add("1"), "add returns true");
        d.add("2");
        check("1".equals(d.peekFirst()) && "2".equals(d.peekLast()), "add appends at tail");
        // offer(e) behaves as offerLast(e)
        check(d.offer("3"), "offer appends at tail");
        check("3".equals(d.peekLast()), "offer result visible at tail");
        // put(e) behaves as putLast(e)
        d.put("4");
        check(d.size() == 4 && "4".equals(d.peekLast()), "put appends at tail");
        // remove() behaves as removeFirst()
        check("1".equals(d.remove()), "remove takes from the head");
        // poll() behaves as pollFirst()
        check("2".equals(d.poll()), "poll takes from the head");
        // element() behaves as getFirst()
        check("3".equals(d.element()), "element examines the head");
        // peek() behaves as peekFirst()
        check("3".equals(d.peek()) && "3".equals(d.peekFirst()), "peek equals peekFirst");
        // timed poll behaves as pollFirst(t,u)
        check("3".equals(d.poll(1, TimeUnit.MILLISECONDS)), "timed poll takes from the head");
        // timed offer behaves as offerLast
        check(d.offer("5", 1, TimeUnit.MILLISECONDS), "timed offer appends at tail");
        check("5".equals(d.peekLast()), "timed offer element at tail");
        // take() behaves as takeFirst()
        check("4".equals(d.take()), "take takes from the head");
        check(d.size() == 1 && "5".equals(d.take()), "sole remaining element taken from head");
        check(d.isEmpty(), "bridge drained");
        // remainingCapacity/contains shared with the queue face
        check(d.remainingCapacity() == 4, "remainingCapacity restored");
        d.add("z");
        check(d.contains("z"), "contains via queue face");
    }

    // ---- Part 9: null-element rejection ----
    static void testNullRejection() {
        final BlockingDeque<String> d = new LinkedBlockingDeque<String>(2);
        expectMsg(new Runnable() { public void run() { d.addFirst(null); } },
                  NullPointerException.class, null, "addFirst(null)");
        expectMsg(new Runnable() { public void run() { d.addLast(null); } },
                  NullPointerException.class, null, "addLast(null)");
        expectMsg(new Runnable() { public void run() { d.offerFirst(null); } },
                  NullPointerException.class, null, "offerFirst(null)");
        expectMsg(new Runnable() { public void run() { d.offerLast(null); } },
                  NullPointerException.class, null, "offerLast(null)");
        expectMsg(new Runnable() { public void run() { d.push(null); } },
                  NullPointerException.class, null, "push(null)");
        expectMsg(new Runnable() { public void run() {
            try { d.putFirst(null); } catch (InterruptedException e) { throw new AssertionError(e); }
        } }, NullPointerException.class, null, "putFirst(null)");
        expectMsg(new Runnable() { public void run() {
            try { d.putLast(null); } catch (InterruptedException e) { throw new AssertionError(e); }
        } }, NullPointerException.class, null, "putLast(null)");
        check(d.isEmpty(), "null rejections left deque empty");
    }

    // ---- Part 10: hierarchy, iteration order ----
    static void testHierarchyAndIteration() {
        LinkedBlockingDeque<String> d = new LinkedBlockingDeque<String>();
        check(d instanceof BlockingDeque, "LinkedBlockingDeque implements BlockingDeque");
        check(d instanceof BlockingQueue, "LinkedBlockingDeque implements BlockingQueue");
        check(d instanceof Deque, "LinkedBlockingDeque implements Deque");
        check(d instanceof Queue, "LinkedBlockingDeque implements Queue");
        check(d instanceof Collection, "LinkedBlockingDeque implements Collection");
        check(BlockingDeque.class.isAssignableFrom(LinkedBlockingDeque.class),
              "BlockingDeque.isAssignableFrom(LinkedBlockingDeque)");
        check(BlockingQueue.class.isAssignableFrom(BlockingDeque.class),
              "BlockingDeque is a BlockingQueue");
        // iterator runs first (head) to last (tail)
        d.addLast("1"); d.addLast("2"); d.addLast("3");
        List<String> fwd = new ArrayList<String>();
        for (String s : d) fwd.add(s);
        check(fwd.equals(Arrays.asList("1", "2", "3")), "iterator is head-to-tail");
        // descending iterator runs tail-to-head
        List<String> rev = new ArrayList<String>();
        for (Iterator<String> it = d.descendingIterator(); it.hasNext(); )
            rev.add(it.next());
        check(rev.equals(Arrays.asList("3", "2", "1")), "descendingIterator is tail-to-head");
        // usable through every supertype reference
        BlockingDeque<String> bd = d;
        BlockingQueue<String> bq = bd;
        Deque<String> dq = bd;
        Queue<String> qq = bd;
        check("1".equals(bq.peek()) && "1".equals(dq.peekFirst()) && "1".equals(qq.peek()),
              "peek consistent across all supertype references");
    }

    public static void main(String[] args) throws Exception {
        testBlockingDeque();
        testWhiteBox();
        testEmptyState();
        testFullState();
        testHeadTailOperations();
        testTakePairing();
        testOccurrenceRemoval();
        testStackSemantics();
        testBlockingQueueBridge();
        testNullRejection();
        testHierarchyAndIteration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
