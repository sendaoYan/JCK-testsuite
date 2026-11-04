/*
 * @test
 * @summary javax.swing.text.LayoutQueue conformance: white-box structure + black-box behavior.
 * @run main LayoutQueueTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import javax.swing.text.LayoutQueue;

public class LayoutQueueTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Interrupt any text-layout worker threads so the JVM can exit after addTask. */
    static void interruptLayoutThreads() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("text-layout".equals(t.getName())) {
                t.interrupt();
            }
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.LayoutQueue");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default queue is stable within the current AppContext.
        LayoutQueue def = LayoutQueue.getDefaultQueue();
        check(def != null, "getDefaultQueue non-null");
        check(LayoutQueue.getDefaultQueue() == def, "getDefaultQueue stable");

        // setDefaultQueue takes effect and can be restored.
        LayoutQueue original = def;
        LayoutQueue custom = new LayoutQueue();
        LayoutQueue.setDefaultQueue(custom);
        check(LayoutQueue.getDefaultQueue() == custom, "setDefaultQueue takes effect");
        LayoutQueue.setDefaultQueue(original);
        check(LayoutQueue.getDefaultQueue() == original, "setDefaultQueue restored");

        // addTask runs the task on the layout worker thread (bounded by a latch).
        final boolean[] flag = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        LayoutQueue q = new LayoutQueue();
        q.addTask(() -> {
            flag[0] = true;
            latch.countDown();
        });
        latch.await();
        check(flag[0], "addTask task executed");

        // A second task on the same queue is also drained in FIFO order.
        final int[] order = {0};
        final CountDownLatch latch2 = new CountDownLatch(2);
        LayoutQueue q2 = new LayoutQueue();
        q2.addTask(() -> {
            if (order[0] == 0) order[0] = 1;
            latch2.countDown();
        });
        q2.addTask(() -> {
            if (order[0] == 1) order[0] = 2;
            latch2.countDown();
        });
        latch2.await();
        check(order[0] == 2, "tasks drained in FIFO order");

        // Clean up worker threads so the test JVM terminates.
        interruptLayoutThreads();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
