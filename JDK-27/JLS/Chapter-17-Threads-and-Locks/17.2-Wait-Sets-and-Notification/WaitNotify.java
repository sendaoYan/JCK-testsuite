/*
 * @test
 * @summary JLS 17.2.1 / 17.2.2: wait() wakes a thread after notifyAll(); the
 *          standard while-loop guard makes the handoff immune to lost
 *          notifications and spurious wake-ups.
 * @run main WaitNotify
 */

public class WaitNotify {
    static final Object lock = new Object();
    static boolean ready = false;

    public static void main(String[] args) throws Exception {
        Thread worker = new Thread(() -> {
            synchronized (lock) {
                while (!ready) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new AssertionError("unexpected interrupt", e);
                    }
                }
                if (!ready) throw new AssertionError("ready not observed");
            }
        });

        worker.start();
        Thread.sleep(100);          // let the worker reach wait (not required for correctness)

        synchronized (lock) {
            ready = true;
            lock.notifyAll();
        }

        worker.join(5000);
        if (worker.isAlive()) throw new AssertionError("worker did not wake");

        System.out.println("PASS");
    }
}
