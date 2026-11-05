/*
 * @test
 * @summary java.lang.ThreadLocal API conformance: get/set/remove, default null initialValue,
 *          initialValue override, ThreadLocal.withInitial, and per-thread isolation.
 * @run main ThreadLocalTest
 */

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

public class ThreadLocalTest {
    static int passed;

    // Exposes the protected initialValue() so we can assert the default null return.
    static class DefaultTL extends ThreadLocal<String> {
        String init() { return initialValue(); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadLocal");
        check(!c.isInterface(), "ThreadLocal is a class");
        check(c.getSuperclass() == Object.class, "ThreadLocal extends Object");
        Method[] ms = c.getDeclaredMethods();
        check(has(ms, "get") && has(ms, "set") && has(ms, "remove"), "get/set/remove present");
    }

    static boolean has(Method[] ms, String name) {
        for (Method m : ms) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testBasic() {
        ThreadLocal<String> tl = new ThreadLocal<>();
        check(tl.get() == null, "initial value null by default");
        tl.set("x");
        check("x".equals(tl.get()), "get returns set value");
        tl.remove();
        check(tl.get() == null, "remove clears value");
    }

    static void testInitialValueOverride() {
        ThreadLocal<String> tl = new ThreadLocal<String>() {
            protected String initialValue() { return "init"; }
        };
        check("init".equals(tl.get()), "initialValue override");
    }

    static void testWithInitial() {
        ThreadLocal<Integer> tl = ThreadLocal.withInitial(() -> 42);
        check(tl.get() == 42, "withInitial supplier");
        tl.set(1);
        check(tl.get() == 1, "set overrides initial");
        tl.remove();
        check(tl.get() == 42, "remove re-initializes from supplier");
    }

    static void testThreadIsolation() throws Exception {
        final ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("main");
        final String[] other = new String[1];
        Thread t = new Thread(() -> other[0] = tl.get());
        t.setDaemon(true);
        t.start();
        t.join();
        check("main".equals(tl.get()), "main thread value unchanged");
        check(other[0] == null, "other thread sees null (isolated)");
    }


    // ===================== 契约深化（2026-11）=====================

    static final class Box {
        final int id;
        Box(int id) { this.id = id; }
    }

    // Counts how many times the supplier/initialValue ran in this thread.
    static class CountingLocal extends ThreadLocal<Box> {
        volatile int calls;
        @Override protected Box initialValue() {
            calls++;
            return new Box(calls);
        }
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testLifecycleFull() {
        ThreadLocal<Object> tl = new ThreadLocal<>();
        check(tl.get() == null, "get returns null before any set");
        Object v1 = new Object();
        tl.set(v1);
        check(tl.get() == v1, "get returns the exact set instance");
        Object v2 = new Object();
        tl.set(v2);
        check(tl.get() == v2, "second set replaces the first");
        check(tl.get() != v1, "replaced value no longer returned");
        tl.set(v2);
        check(tl.get() == v2, "re-setting the same reference is idempotent");
        tl.remove();
        check(tl.get() == null, "remove clears the value");
        tl.remove();
        check(tl.get() == null, "second remove is a no-op");
        tl.set(v1);
        check(tl.get() == v1, "set works again after remove");
        tl.set(null);
        check(tl.get() == null, "set(null) stores an explicit null");
        tl.remove();
        check(tl.get() == null, "remove after set(null) still null");
    }

    static void testWithInitialContract() {
        final int[] calls = {0};
        ThreadLocal<Box> tl = ThreadLocal.withInitial(() -> {
            calls[0]++;
            return new Box(calls[0]);
        });
        Box first = tl.get();
        check(first != null && first.id == 1, "withInitial get returns supplier value");
        check(calls[0] == 1, "supplier invoked exactly once");
        check(tl.get() == first, "second get returns the same instance");
        check(calls[0] == 1, "supplier not re-invoked on second get");
        Box override = new Box(99);
        tl.set(override);
        check(tl.get() == override, "set overrides the supplied value");
        check(calls[0] == 1, "set does not invoke the supplier");
        tl.set(null);
        check(tl.get() == null, "set(null) over the supplied value");
        tl.remove();
        Box after = tl.get();
        check(calls[0] == 2, "remove re-arms the supplier (invoked again)");
        check(after != null && after.id == 2, "re-initialized value is a fresh instance");
        check(after != first, "re-initialized instance differs from the first");
        // Static factory null check.
        expectNPE(() -> ThreadLocal.withInitial(null), "withInitial(null) NPE");
        // Supplier exceptions propagate from get().
        ThreadLocal<Integer> boom = ThreadLocal.withInitial(() -> {
            throw new IllegalStateException("supply-boom");
        });
        try {
            boom.get();
            check(false, "supplier exception should propagate from get()");
        } catch (IllegalStateException e) {
            check("supply-boom".equals(e.getMessage()), "propagated exception message preserved");
        }
        // Two independent withInitial locals.
        ThreadLocal<Integer> a = ThreadLocal.withInitial(() -> 1);
        ThreadLocal<Integer> b = ThreadLocal.withInitial(() -> 2);
        check(a.get() == 1 && b.get() == 2, "independent withInitial locals");
        a.set(10);
        check(a.get() == 10 && b.get() == 2, "set on one leaves the other supplied");
        // Integer boxing edge: identity of a small int is not asserted, only value.
        ThreadLocal<String> s = ThreadLocal.withInitial(() -> "abc");
        check("abc".equals(s.get()), "string supplier value");
        s.remove();
        check("abc".equals(s.get()), "string supplier re-armed after remove");
    }

    static void testInitialValueSubclass() {
        CountingLocal cl = new CountingLocal();
        check(cl.calls == 0, "initialValue not yet invoked before first get");
        Box v1 = cl.get();
        check(v1 != null && v1.id == 1, "initialValue supplies the first get");
        check(cl.calls == 1, "initialValue invoked once");
        check(cl.get() == v1, "subsequent gets return the cached instance");
        check(cl.calls == 1, "initialValue not re-invoked while cached");
        cl.remove();
        Box v2 = cl.get();
        check(cl.calls == 2, "remove re-arms initialValue");
        check(v2 != v1, "second initialization is a fresh instance");
        cl.set(new Box(42));
        check(cl.get().id == 42, "set overrides initialValue subclass");
        check(cl.calls == 2, "set does not re-invoke initialValue");

        // Anonymous subclass returning a fixed value.
        ThreadLocal<String> fixed = new ThreadLocal<String>() {
            @Override protected String initialValue() { return "fixed"; }
        };
        check("fixed".equals(fixed.get()), "anonymous subclass initialValue");
        check("fixed".equals(fixed.get()), "fixed value stable across gets");

        // Subclass explicitly returning null behaves like the default.
        ThreadLocal<String> nullInit = new ThreadLocal<String>() {
            @Override protected String initialValue() { return null; }
        };
        check(nullInit.get() == null, "initialValue null subclass get null");

        // Protected access: initialValue is protected, callable from subclass.
        DefaultTL d = new DefaultTL();
        check(d.init() == null, "default initialValue returns null (reprise)");
    }

    static void testWorkerIsolationLatched() throws Exception {
        final ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("main-value");
        final CountDownLatch bothSet = new CountDownLatch(2);
        final CountDownLatch release = new CountDownLatch(1);
        final String[] readA = {null};
        final String[] readB = {null};
        final String[] readAAfterRemove = {null};
        final boolean[] workerSeesMain = {false};
        Thread ta = new Thread(() -> {
            tl.set("worker-A");
            workerSeesMain[0] = "main-value".equals(tl.get()) == false;
            bothSet.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            readA[0] = tl.get();
            tl.remove();
            readAAfterRemove[0] = tl.get();
        }, "tl-a");
        Thread tb = new Thread(() -> {
            tl.set("worker-B");
            bothSet.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            readB[0] = tl.get();
        }, "tl-b");
        ta.setDaemon(true);
        tb.setDaemon(true);
        ta.start();
        tb.start();
        check(bothSet.await(10, java.util.concurrent.TimeUnit.SECONDS), "both workers set their values");
        check(tl.get().equals("main-value"), "main value unaffected by worker sets");
        release.countDown();
        ta.join(10_000);
        tb.join(10_000);
        check(readA[0] != null && readA[0].equals("worker-A"), "worker A reads its own value");
        check(readB[0] != null && readB[0].equals("worker-B"), "worker B reads its own value");
        check(readAAfterRemove[0] == null, "worker A remove clears only its own value");
        check(tl.get().equals("main-value"), "main value intact after workers exit");
        check(workerSeesMain[0], "worker never observes the main thread's value");
    }

    static void testInitialValuePerThread() throws Exception {
        // NOTE: CountingLocal.calls is shared across threads (initialValue runs
        // in the thread that first calls get), so the numbering below is
        // deterministic: main is call #1, the worker is call #2.
        final CountingLocal cl = new CountingLocal();
        final Box mainBox = cl.get();
        final Box[] workerBox = {null};
        Thread w = new Thread(() -> workerBox[0] = cl.get());
        w.setDaemon(true);
        w.start();
        w.join(10_000);
        check(workerBox[0] != null, "worker got a value");
        check(workerBox[0] != mainBox, "worker value is a distinct instance");
        check(workerBox[0].id == 2, "worker's initialValue was the second invocation");
        check(cl.calls == 2, "counter shows exactly two initializations");
        check(cl.get() == mainBox, "main's cached instance unchanged by worker");
    }

    static void testIdentitySemantics() {
        ThreadLocal<String> a = new ThreadLocal<>();
        ThreadLocal<String> b = new ThreadLocal<>();
        check(a.equals(a), "ThreadLocal equals itself");
        check(!a.equals(b), "distinct ThreadLocals not equal");
        check(a.toString().startsWith("java.lang.ThreadLocal@"), "toString format");
        int h1 = a.hashCode();
        check(a.hashCode() == h1, "hashCode stable");
        a.set("x");
        check(a.hashCode() == h1, "hashCode unaffected by set");
        a.remove();
        check(a.hashCode() == h1, "hashCode unaffected by remove");
        check(b.hashCode() == b.hashCode(), "second local hashCode stable");
        check(a.hashCode() != b.hashCode(), "distinct locals (almost surely) distinct hashCodes");
    }

    static void testMultipleLocals() {
        ThreadLocal<String> one = new ThreadLocal<>();
        ThreadLocal<String> two = new ThreadLocal<>();
        check(one.get() == null && two.get() == null, "both locals start null");
        one.set("1");
        check(two.get() == null, "set on one leaves the other null");
        two.set("2");
        check("1".equals(one.get()) && "2".equals(two.get()), "independent values in the same thread");
        one.remove();
        check(one.get() == null && "2".equals(two.get()), "remove one keeps the other");
        two.set("2b");
        check("2b".equals(two.get()), "second local overwrites independently");
        Box boxed = new Box(7);
        ThreadLocal<Box> three = new ThreadLocal<>();
        three.set(boxed);
        check(three.get() == boxed && three.get().id == 7, "reference type local");
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadLocal");
        check(java.lang.reflect.Modifier.isStatic(c.getMethod("withInitial",
              java.util.function.Supplier.class).getModifiers()),
              "withInitial is static");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredMethod("initialValue").getModifiers()),
              "initialValue is protected");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("get").getModifiers()), "get is public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("set", Object.class).getModifiers()),
              "set is public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("remove").getModifiers()), "remove is public");
        check(!c.isInterface() && c.getSuperclass() == Object.class, "plain class extending Object");
    }


    static void testMutableValueSharedPerThread() {
        ThreadLocal<StringBuilder> tl = new ThreadLocal<>();
        tl.set(new StringBuilder("ab"));
        StringBuilder sb = tl.get();
        check(sb.length() == 2, "mutable stored value");
        sb.append("c");
        check(tl.get().length() == 3, "same mutable instance returned");
        check("abc".equals(tl.get().toString()), "mutation visible through get");
        tl.set(new StringBuilder("reset"));
        check("reset".equals(tl.get().toString()), "set replaces the mutable instance");
    }

    static void testSetGetRemoveCycles() {
        ThreadLocal<Integer> tl = new ThreadLocal<>();
        for (int i = 1; i <= 3; i++) {
            tl.set(i * 11);
            check(tl.get() == i * 11, "cycle " + i + " set/get");
            tl.remove();
            check(tl.get() == null, "cycle " + i + " remove");
        }
        check(tl.get() == null, "final state null after cycles");
    }

    static void testNullSupplierValue() {
        final int[] calls = {0};
        ThreadLocal<Object> tl = ThreadLocal.withInitial(() -> {
            calls[0]++;
            return null;
        });
        check(tl.get() == null, "null-supplying withInitial returns null");
        check(calls[0] == 1, "null supplier still invoked once");
        check(tl.get() == null, "second get still null");
        check(calls[0] == 1, "null result is cached like any other");
        tl.remove();
        check(tl.get() == null, "after remove still null");
        check(calls[0] == 2, "supplier re-invoked after remove");
    }

    static void testSequentialWorkersIsolated() throws Exception {
        final ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("main");
        final String[] first = {null};
        Thread w1 = new Thread(() -> {
            tl.set("first-worker");
            first[0] = tl.get();
        });
        w1.setDaemon(true);
        w1.start();
        w1.join(10_000);
        final String[] second = {null};
        Thread w2 = new Thread(() -> second[0] = tl.get());
        w2.setDaemon(true);
        w2.start();
        w2.join(10_000);
        check("first-worker".equals(first[0]), "first worker set and read its value");
        check(second[0] == null, "second worker starts fresh (no leakage from first)");
        check("main".equals(tl.get()), "main value unaffected across workers");
    }

    static void testSupplierPerThread() throws Exception {
        final int[] calls = {0};
        final ThreadLocal<Object> tl = ThreadLocal.withInitial(() -> {
            calls[0]++;
            return new Object();
        });
        Object mainV = tl.get();
        final Object[] workerV = {null};
        Thread w = new Thread(() -> workerV[0] = tl.get());
        w.setDaemon(true);
        w.start();
        w.join(10_000);
        check(calls[0] == 2, "supplier ran once per thread");
        check(workerV[0] != null && workerV[0] != mainV, "each thread gets its own supplied instance");
        check(tl.get() == mainV, "main instance cached");
    }

    static void testManyLocals() {
        ThreadLocal<Integer>[] locals = new ThreadLocal[5];
        for (int i = 0; i < locals.length; i++) {
            final int idx = i;
            locals[i] = ThreadLocal.withInitial(() -> idx * 10);
        }
        for (int i = 0; i < locals.length; i++) {
            check(locals[i].get() == i * 10, "local " + i + " supplied value");
        }
        locals[2].set(999);
        check(locals[2].get() == 999, "mid local set");
        check(locals[1].get() == 10 && locals[3].get() == 30, "neighbors unaffected");
        for (int i = 0; i < locals.length; i++) {
            if (i != 2) {
                check(locals[i].get() == i * 10, "local " + i + " still supplied");
            }
        }
    }

    static void testSubclassWithField() {
        class PrefixedLocal extends ThreadLocal<String> {
            final String prefix;
            PrefixedLocal(String prefix) { this.prefix = prefix; }
            @Override protected String initialValue() { return prefix + "-init"; }
        }
        PrefixedLocal a = new PrefixedLocal("A");
        PrefixedLocal b = new PrefixedLocal("B");
        check("A-init".equals(a.get()), "prefixed local A");
        check("B-init".equals(b.get()), "prefixed local B");
        a.set("A-set");
        check("A-set".equals(a.get()) && "B-init".equals(b.get()), "set on A leaves B");
        a.remove();
        check("A-init".equals(a.get()), "remove restores A's initialValue");
    }
    // ---- Part 2b: default initialValue() ----
    static void testBlackBox() {
        check(new DefaultTL().init() == null, "default initialValue returns null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasic();
        testInitialValueOverride();
        testWithInitial();
        testThreadIsolation();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testLifecycleFull();
        testWithInitialContract();
        testInitialValueSubclass();
        testWorkerIsolationLatched();
        testInitialValuePerThread();
        testIdentitySemantics();
        testMultipleLocals();
        testWhiteBoxMore();
        testMutableValueSharedPerThread();
        testSetGetRemoveCycles();
        testNullSupplierValue();
        testSequentialWorkersIsolated();
        testSupplierPerThread();
        testManyLocals();
        testSubclassWithField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
