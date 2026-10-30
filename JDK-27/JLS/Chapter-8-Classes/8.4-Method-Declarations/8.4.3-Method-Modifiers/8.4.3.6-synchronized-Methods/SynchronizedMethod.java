/*
 * @test
 * @summary JLS 8.4.3.6: a synchronized instance method acquires the monitor of
 *          the object for which it is invoked, providing mutual exclusion.
 * @run main SynchronizedMethod
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class Counter {
    int count;
    synchronized void bump() { count++; }
}

public class SynchronizedMethod {
    public static void main(String[] args) throws Exception {
        Method bump = Counter.class.getDeclaredMethod("bump");
        if (!Modifier.isSynchronized(bump.getModifiers()))
            throw new AssertionError("bump must be synchronized");

        // Concurrent increments must be serialized by the monitor.
        Counter c = new Counter();
        int N = 1000;
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> { for (int j = 0; j < 100; j++) c.bump(); });
            ts[i].start();
        }
        for (Thread t : ts) {
            try { t.join(); } catch (InterruptedException e) { throw new AssertionError(e); }
        }
        if (c.count != N * 100) throw new AssertionError("lost updates: " + c.count);

        System.out.println("PASS");
    }
}
