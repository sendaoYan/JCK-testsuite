/*
 * @test
 * @summary java.util.concurrent.SynchronousQueue API conformance: blocking handoff and no-capacity semantics.
 * @run main SynchronousQueueTest
 */

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSynchronousQueue() throws Exception {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        Thread producer = new Thread(() -> {
            try {
                q.put("hello");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        producer.start();
        check("hello".equals(q.take()), "take receives the put value");
        producer.join();
        check(!q.offer("x"), "offer returns false with no waiting taker");
        check(q.poll() == null, "poll returns null with no waiting producer");
    }

    public static void main(String[] args) throws Exception {
        testSynchronousQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
