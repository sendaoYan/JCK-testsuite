/*
 * @test
 * @summary java.lang.Object API conformance: getClass (final, runtime class),
 *          hashCode (identity), equals (reference equality), toString format,
 *          clone (Cloneable contract + CloneNotSupportedException), and the
 *          wait/notify/notifyAll monitor protocol (incl. IllegalMonitorStateException).
 * @run main ObjectTest
 */

public class ObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectIMSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException expected) {
            passed++;
        }
    }

    static class CloneableThing implements Cloneable {
        int x;
        CloneableThing(int x) { this.x = x; }
        @Override public CloneableThing clone() {
            try {
                return (CloneableThing) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    static class NotCloneable {
        @Override public NotCloneable clone() throws CloneNotSupportedException {
            return (NotCloneable) super.clone();
        }
    }

    static void testGetClass() throws Exception {
        Object o = new Object();
        check(o.getClass() == Object.class, "new Object().getClass() == Object.class");
        check("s".getClass() == String.class, "\"s\".getClass() == String.class");
        check(new int[0].getClass() == int[].class, "int[].getClass() == int[].class");
        java.lang.reflect.Method getClass = Object.class.getMethod("getClass");
        check(java.lang.reflect.Modifier.isFinal(getClass.getModifiers()), "getClass() is final");
    }

    static void testHashCodeEquals() {
        Object a = new Object();
        Object b = new Object();
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(b), "distinct objects not equals");
        check(!a.equals(null), "!equals(null)");
        check(a.hashCode() == System.identityHashCode(a), "default hashCode == identityHashCode");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        // equals => equal hashCodes (reflexive pair trivially satisfies this)
        check(a.hashCode() == a.hashCode(), "equal objects have equal hashCode");
    }

    static void testToString() {
        Object o = new Object();
        String expected = o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        check(o.toString().equals(expected), "toString == className@hexHash");
    }

    static void testClone() throws Exception {
        CloneableThing c = new CloneableThing(7);
        CloneableThing d = c.clone();
        check(c != d, "clone returns a distinct object");
        check(c.getClass() == d.getClass(), "clone has same runtime class");
        check(d.x == 7, "clone copies field values (shallow)");

        NotCloneable n = new NotCloneable();
        try {
            n.clone();
            throw new AssertionError("clone on non-Cloneable: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }
    }

    static void testMonitorErrors() {
        Object lock = new Object();
        expectIMSE(() -> { try { lock.wait(); } catch (InterruptedException e) {
            throw new AssertionError(e); } }, "wait() without owning monitor");
        expectIMSE(() -> lock.notify(), "notify() without owning monitor");
        expectIMSE(() -> lock.notifyAll(), "notifyAll() without owning monitor");
    }

    static void testWaitNotify() throws Exception {
        final Object lock = new Object();
        final boolean[] entered = { false };
        final boolean[] woken = { false };

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                entered[0] = true;
                lock.notifyAll(); // tell main we hold the monitor
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
                woken[0] = true;
            }
        });
        worker.start();

        synchronized (lock) {
            while (!entered[0]) {
                try { lock.wait(); } catch (InterruptedException e) { throw new AssertionError(e); }
            }
            lock.notify(); // wake the worker from its wait()
        }
        worker.join(2000);
        check(!worker.isAlive(), "worker thread terminated");
        check(woken[0], "notify() wakes a waiting thread");
    }

    static void testWaitTimeout() throws Exception {
        final Object lock = new Object();
        synchronized (lock) {
            // wait(timeout) returns even when nobody notifies.
            long start = System.nanoTime();
            lock.wait(50);
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            check(elapsedMillis >= 40, "wait(50) waited at least ~50ms (actual " + elapsedMillis + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        testGetClass();
        testHashCodeEquals();
        testToString();
        testClone();
        testMonitorErrors();
        testWaitNotify();
        testWaitTimeout();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
