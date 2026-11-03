/*
 * @test
 * @summary java.util.concurrent.BlockingQueue conformance: white-box structure + black-box behavior.
 * @run main BlockingQueueTest
 */

import java.util.*;
import java.util.concurrent.*;

public class BlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.BlockingQueue");
        check(c.isInterface(), "BlockingQueue is an interface");
        check(Queue.class.isAssignableFrom(c), "extends Queue");
        hasMethod(c, "put", Object.class);
        hasMethod(c, "take");
        hasMethod(c, "offer", Object.class, long.class, TimeUnit.class);
        hasMethod(c, "poll", long.class, TimeUnit.class);
        hasMethod(c, "remainingCapacity");
        hasMethod(c, "drainTo", Collection.class);
        hasMethod(c, "drainTo", Collection.class, int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BlockingQueue<String> q = new LinkedBlockingQueue<>(2);
        check(q.remainingCapacity() == 2, "remainingCapacity initial");
        check(q.offer("a") && q.offer("b"), "offer within capacity");
        check(q.remainingCapacity() == 0, "remainingCapacity 0 when full");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check("a".equals(q.peek()), "peek");
        check("a".equals(q.poll()), "poll");
        check("b".equals(q.poll(1, TimeUnit.SECONDS)), "poll(timeout)");
        check(q.poll() == null, "poll empty returns null");
        q.put("x"); q.put("y");
        List<String> drained = new ArrayList<>();
        check(q.drainTo(drained) == 2, "drainTo drains all");
        check(drained.equals(Arrays.asList("x", "y")), "drainTo order FIFO");
        BlockingQueue<String> q2 = new LinkedBlockingQueue<>();
        q2.add("1"); q2.add("2"); q2.add("3");
        List<String> partial = new ArrayList<>();
        check(q2.drainTo(partial, 2) == 2, "drainTo(max) drains at most max");
        check(partial.equals(Arrays.asList("1", "2")) && q2.size() == 1, "drainTo(max) leaves remainder");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
