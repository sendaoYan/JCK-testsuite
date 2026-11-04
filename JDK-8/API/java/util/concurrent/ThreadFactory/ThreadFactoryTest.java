/*
 * @test
 * @summary java.util.concurrent.ThreadFactory API conformance: newThread.
 * @run main ThreadFactoryTest
 */

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadFactory() {
        final AtomicInteger count = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("worker-" + count.incrementAndGet());
            return t;
        };
        Runnable task = () -> { };
        Thread t1 = tf.newThread(task);
        Thread t2 = tf.newThread(task);
        check("worker-1".equals(t1.getName()) && "worker-2".equals(t2.getName()), "custom naming");
        check(!t1.isAlive() && !t2.isAlive(), "newThread does not start the thread");
    }

    public static void main(String[] args) {
        testThreadFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
