/*
 * @test
 * @summary java.lang.Object conformance: white-box structure (no superclass,
 *          concrete, final getClass/notify/notifyAll/wait, native hashCode) +
 *          black-box behavior (getClass, identity hashCode/equals, toString
 *          format, clone Cloneable contract + shallow copy + CNSE, wait/notify
 *          monitor protocol incl. IllegalMonitorStateException and timeout, and
 *          the reachable no-op protected finalize()).
 * @run main ObjectTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
        int[] ref;
        CloneableThing(int x, int[] ref) { this.x = x; this.ref = ref; }
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

    // Exposes Object.finalize() (protected) to prove the no-op super.finalize() is reachable.
    static class Finalizable {
        void invokeFinalize() {
            try {
                finalize();
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
        @Override protected void finalize() throws Throwable { super.finalize(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Object");
        check(c.getSuperclass() == null, "Object has no superclass");
        check(!c.isInterface(), "Object is a class, not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "Object is concrete");
        Method getClass = c.getMethod("getClass");
        check(Modifier.isFinal(getClass.getModifiers()), "getClass() is final");
        check(Modifier.isFinal(c.getMethod("notify").getModifiers()), "notify() is final");
        check(Modifier.isFinal(c.getMethod("notifyAll").getModifiers()), "notifyAll() is final");
        check(Modifier.isFinal(c.getMethod("wait").getModifiers()), "wait() is final");
        check(Modifier.isFinal(c.getMethod("wait", long.class).getModifiers()), "wait(long) is final");
        check(Modifier.isFinal(c.getMethod("wait", long.class, int.class).getModifiers()),
              "wait(long,int) is final");
        check(Modifier.isNative(c.getMethod("hashCode").getModifiers()), "hashCode() is native");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testGetClass() {
        Object o = new Object();
        check(o.getClass() == Object.class, "new Object().getClass() == Object.class");
        check("s".getClass() == String.class, "\"s\".getClass() == String.class");
        check(new int[0].getClass() == int[].class, "int[].getClass() == int[].class");
    }

    static void testHashCodeEquals() {
        Object a = new Object();
        Object b = new Object();
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(b), "distinct objects not equals");
        check(!a.equals(null), "!equals(null)");
        check(a.equals(b) == b.equals(a), "equals is symmetric");
        check(a.hashCode() == System.identityHashCode(a), "default hashCode == identityHashCode");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
    }

    static void testToString() {
        Object o = new Object();
        String expected = o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        check(o.toString().equals(expected), "toString == className@hexHash");
    }

    static void testClone() {
        int[] shared = new int[] { 7 };
        CloneableThing c = new CloneableThing(7, shared);
        CloneableThing d = c.clone();
        check(c != d, "clone returns a distinct object");
        check(c.getClass() == d.getClass(), "clone has same runtime class");
        check(d.x == 7, "clone copies field values (shallow)");
        check(d.ref == c.ref, "clone is a shallow copy (reference fields shared)");

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
        worker.setDaemon(true);
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

    // ---- Part 2b: protected finalize() is reachable and a no-op ----
    static void testBlackBox() {
        new Finalizable().invokeFinalize();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetClass();
        testHashCodeEquals();
        testToString();
        testClone();
        testMonitorErrors();
        testWaitNotify();
        testWaitTimeout();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
