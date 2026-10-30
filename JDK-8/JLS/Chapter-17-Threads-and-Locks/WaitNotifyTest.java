/*
 * @test
 * @summary JLS 17.2 (Java SE 8): a thread blocks on wait() until another thread
 *          calls notify()/notifyAll(); wait() releases the monitor while blocked.
 * @run main WaitNotifyTest
 */

public class WaitNotifyTest {
    static final Object lock = new Object();
    static volatile boolean entered = false;
    static volatile boolean ready = false;

    public static void main(String[] args) throws Exception {
        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                entered = true;
                try {
                    while (!ready) lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        waiter.start();
        while (!entered) Thread.yield();

        synchronized (lock) {
            ready = true;
            lock.notifyAll();
        }

        waiter.join(2000);
        if (waiter.isAlive()) throw new AssertionError("waiter did not wake");
        System.out.println("PASS");
    }
}
