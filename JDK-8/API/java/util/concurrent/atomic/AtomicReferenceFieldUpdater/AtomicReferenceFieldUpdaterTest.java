/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceFieldUpdater API conformance.
 * @run main AtomicReferenceFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AtomicReferenceFieldUpdaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Holder {
        volatile String value;
    }

    static void testAtomicReferenceFieldUpdater() {
        AtomicReferenceFieldUpdater<Holder, String> updater =
            AtomicReferenceFieldUpdater.newUpdater(Holder.class, String.class, "value");
        Holder h = new Holder();
        updater.set(h, "a");
        check("a".equals(updater.get(h)), "get after set");
        check(updater.compareAndSet(h, "a", "b"), "compareAndSet succeeds");
        check("b".equals(updater.get(h)), "value updated");
        check(!updater.compareAndSet(h, "a", "c"), "compareAndSet fails with stale expected");
    }

    public static void main(String[] args) {
        testAtomicReferenceFieldUpdater();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
