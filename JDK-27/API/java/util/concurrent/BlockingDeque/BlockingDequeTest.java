/*
 * @test
 * @summary java.util.concurrent.BlockingDeque conformance: white-box structure + black-box behavior.
 * @run main BlockingDequeTest
 */

import java.util.concurrent.*;

public class BlockingDequeTest {
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
        Class<?> c = cls("java.util.concurrent.BlockingDeque");
        check(c.isInterface(), "BlockingDeque is an interface");
        check(BlockingQueue.class.isAssignableFrom(c), "extends BlockingQueue");
        check(java.util.Deque.class.isAssignableFrom(c), "extends Deque");
        hasMethod(c, "addFirst", Object.class);
        hasMethod(c, "addLast", Object.class);
        hasMethod(c, "putFirst", Object.class);
        hasMethod(c, "putLast", Object.class);
        hasMethod(c, "takeFirst");
        hasMethod(c, "takeLast");
        hasMethod(c, "pollFirst", long.class, TimeUnit.class);
        hasMethod(c, "pollLast", long.class, TimeUnit.class);
        hasMethod(c, "offerFirst", Object.class);
        hasMethod(c, "offerLast", Object.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BlockingDeque<String> d = new LinkedBlockingDeque<>(2);
        d.addFirst("a");
        d.addLast("b");
        check("b".equals(d.peekLast()), "peekLast");
        check("a".equals(d.takeFirst()), "takeFirst");
        d.addFirst("c");
        check("c".equals(d.pollFirst()), "pollFirst");
        check("b".equals(d.pollLast(1, TimeUnit.SECONDS)), "pollLast(timeout)");
        check(d.isEmpty(), "empty after draining");
        d.putFirst("x");
        d.putLast("y");
        check("y".equals(d.takeLast()), "putLast/takeLast pair");
        check("x".equals(d.takeFirst()), "putFirst/takeFirst pair");
        check(d.remainingCapacity() == 2, "remainingCapacity restored");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
