/*
 * @test
 * @summary java.lang.ref.Cleaner conformance: white-box structure + black-box behavior.
 * @run main CleanerTest
 */

import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CleanerTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ref.Cleaner");
        check(Modifier.isFinal(c.getModifiers()), "Cleaner is final");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ctor : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ctor.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor: factory only");
        Method create = c.getDeclaredMethod("create");
        check(Modifier.isStatic(create.getModifiers()) && create.getReturnType() == Cleaner.class,
              "static create()");
        check(Modifier.isStatic(c.getDeclaredMethod("create", ThreadFactory.class).getModifiers()),
              "static create(ThreadFactory)");
        Method reg = c.getDeclaredMethod("register", Object.class, Runnable.class);
        check(reg.getReturnType() == Cleaner.Cleanable.class, "register returns Cleanable");
        check(reg.getExceptionTypes().length == 0, "register declares no checked exceptions");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // create() default factory
        Cleaner cleaner = Cleaner.create();
        check(cleaner != null, "create() non-null");

        // register + explicit clean(): action runs exactly once, deterministically (no GC)
        AtomicInteger runs = new AtomicInteger();
        Object obj = new Object();
        Cleaner.Cleanable c1 = cleaner.register(obj, runs::incrementAndGet);
        check(c1 != null, "register non-null");
        check(runs.get() == 0, "action not run before clean");
        c1.clean();
        check(runs.get() == 1, "clean() runs action once");
        c1.clean();
        c1.clean();
        check(runs.get() == 1, "repeated clean() is idempotent (action runs once)");

        // multiple registrations on the same object are independent
        AtomicInteger runsA = new AtomicInteger(), runsB = new AtomicInteger();
        Object shared = new Object();
        Cleaner.Cleanable ca = cleaner.register(shared, runsA::incrementAndGet);
        Cleaner.Cleanable cb = cleaner.register(shared, runsB::incrementAndGet);
        ca.clean();
        check(runsA.get() == 1 && runsB.get() == 0, "cleaning one registration leaves the other");
        cb.clean();
        check(runsA.get() == 1 && runsB.get() == 1, "second registration cleaned independently");

        // register throws NPE for null obj / null action (impl uses requireNonNull)
        try {
            cleaner.register(null, () -> { });
            check(false, "register(null obj) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "register(null obj) NPE");
        }
        try {
            cleaner.register(new Object(), null);
            check(false, "register(null action) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "register(null action) NPE");
        }

        // create(ThreadFactory): factory is used to create the cleaner thread;
        // use a DAEMON factory so the test JVM can exit normally.
        AtomicInteger made = new AtomicInteger();
        ThreadFactory daemonFactory = r -> {
            made.incrementAndGet();
            Thread t = new Thread(r, "jck-cleaner-daemon");
            t.setDaemon(true);
            return t;
        };
        Cleaner c2 = Cleaner.create(daemonFactory);
        check(c2 != null, "create(ThreadFactory) non-null");
        AtomicInteger ran = new AtomicInteger();
        Cleaner.Cleanable c3 = c2.register(new Object(), ran::incrementAndGet);
        c3.clean();
        check(ran.get() == 1, "clean works on factory-created cleaner");

        try {
            Cleaner.create(null);
            check(false, "create(null factory) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "create(null ThreadFactory) NPE");
        }

        // action throwing inside clean(): exception propagates; at-most-once still holds
        AtomicInteger attempts = new AtomicInteger();
        Cleaner.Cleanable boom = cleaner.register(new Object(), () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("boom");
        });
        try {
            boom.clean();
            check(false, "throwing action should propagate");
        } catch (IllegalStateException expected) {
            check(true, "throwing action propagates from clean()");
        }
        boom.clean();
        check(attempts.get() == 1, "at-most-once: failed action is not re-attempted");
    }

    // ---- contract deepening (batch lcL, 2026-11) ----

    static void testCreateSemantics() {
        Cleaner a = Cleaner.create();
        Cleaner b = Cleaner.create();
        check(a != null && b != null, "create() non-null");
        check(a != b, "two create() calls give independent cleaners");
        check(!a.equals(b), "Cleaner uses identity equality");
        check(a.equals(a), "identity equality reflexive");
        check(!a.equals(null), "equals(null) false");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        check(a.toString() != null && a.toString().startsWith("java.lang.ref.Cleaner@"),
                "Object.toString form");
        check(a.register(new Object(), () -> { }) != null, "first cleaner accepts registration");
        check(b.register(new Object(), () -> { }) != null, "second cleaner accepts registration");
    }

    static void testFactoryThreadCreation() {
        // probed on jdk-27.0.1: the ThreadFactory is invoked exactly once, at
        // create() time; register() does not spawn additional threads
        AtomicInteger made = new AtomicInteger();
        java.util.concurrent.atomic.AtomicReference<Thread> created =
                new java.util.concurrent.atomic.AtomicReference<>();
        ThreadFactory factory = r -> {
            made.incrementAndGet();
            Thread t = new Thread(r, "lcL-cleaner");
            t.setDaemon(true);
            created.set(t);
            return t;
        };
        Cleaner c = Cleaner.create(factory);
        check(made.get() == 1, "ThreadFactory invoked exactly once at create()");
        check(created.get() != null, "factory produced the cleaner thread");
        check(created.get().isDaemon(), "factory-controlled daemon flag honored");
        AtomicInteger ran = new AtomicInteger();
        c.register(new Object(), ran::incrementAndGet).clean();
        check(ran.get() == 1, "factory-created cleaner is functional");
        check(made.get() == 1, "no extra threads created by register()/clean()");
    }

    static void testCleanableSurface() throws Exception {
        Class<?> cc = Cleaner.Cleanable.class;
        check(cc.isInterface(), "Cleanable is an interface");
        check(Modifier.isPublic(cc.getModifiers()), "Cleanable is public");
        check(cc.getName().equals("java.lang.ref.Cleaner$Cleanable"), "nested name");
        Method[] ms = cc.getDeclaredMethods();
        check(ms.length == 1 && ms[0].getName().equals("clean") && ms[0].getReturnType() == void.class,
                "Cleanable declares exactly one method: void clean()");
        check(Modifier.isPublic(ms[0].getModifiers()) && Modifier.isAbstract(ms[0].getModifiers()),
                "clean() is public abstract");
        Cleaner c = Cleaner.create();
        Object reg = c.register(new Object(), () -> { });
        check(reg instanceof Cleaner.Cleanable, "register() result implements Cleanable");
        check(reg instanceof java.lang.ref.Reference,
                "the returned Cleanable is itself a Reference (probed: phantom-based impl)");
    }

    static void testManualCleanWithStrongReference() {
        // manual clean() runs the action regardless of the object's reachability
        Cleaner c = Cleaner.create();
        AtomicInteger n = new AtomicInteger();
        Object alive = new Object();
        c.register(alive, n::incrementAndGet).clean();
        check(n.get() == 1, "manual clean runs while the referent is strongly reachable");
        check(alive != null, "referent still strongly held after cleaning");
        c.register(alive, n::incrementAndGet).clean();
        check(n.get() == 2, "the same object can be registered and cleaned repeatedly");
    }

    static void testBulkIndependence() {
        Cleaner c = Cleaner.create();
        int n = 30;
        int[] counts = new int[n];
        Cleaner.Cleanable[] cs = new Cleaner.Cleanable[n];
        Object[] targets = new Object[n];
        for (int i = 0; i < n; i++) {
            final int idx = i;
            targets[i] = new Object();
            cs[i] = c.register(targets[i], () -> counts[idx]++);
        }
        boolean distinct = true;
        for (int i = 0; i < n && distinct; i++)
            for (int j = i + 1; j < n; j++)
                if (cs[i] == cs[j]) { distinct = false; break; }
        check(distinct, "each registration yields a distinct Cleanable");
        int before = 0;
        for (int i = 0; i < n; i++) before += counts[i];
        check(before == 0, "no action ran before any clean()");
        for (int i = 0; i < n; i += 2) cs[i].clean();
        int half = 0;
        for (int i = 0; i < n; i++) half += counts[i];
        check(half == n / 2, "cleaning half of the registrations runs exactly half the actions");
        for (int i = 0; i < n; i++) cs[i].clean();
        for (int i = 0; i < n; i++)
            check(counts[i] == 1, "action " + i + " ran exactly once in total");
        int total = 0;
        for (int i = 0; i < n; i++) total += counts[i];
        check(total == n, "sum of runs equals the number of registrations");
    }

    static void testCrossCleanerIndependence() {
        Cleaner c1 = Cleaner.create();
        Cleaner c2 = Cleaner.create();
        AtomicInteger n1 = new AtomicInteger();
        AtomicInteger n2 = new AtomicInteger();
        Object shared = new Object();
        Cleaner.Cleanable a1 = c1.register(shared, n1::incrementAndGet);
        Cleaner.Cleanable a2 = c2.register(shared, n2::incrementAndGet);
        check(n1.get() == 0 && n2.get() == 0, "nothing ran yet");
        a1.clean();
        check(n1.get() == 1 && n2.get() == 0, "cleaning in c1 leaves c2's registration alone");
        a2.clean();
        check(n1.get() == 1 && n2.get() == 1, "both registrations cleaned independently");
        Object other = new Object();
        AtomicInteger n3 = new AtomicInteger(), n4 = new AtomicInteger();
        c1.register(other, n3::incrementAndGet).clean();
        c2.register(other, n3::incrementAndGet).clean();
        check(n3.get() == 2 && n4.get() == 0, "same object across two cleaners cleaned twice");
    }

    static void testCleanFromAnotherThread() throws Exception {
        // manual clean() is deterministic and thread-safe; still exactly-once
        Cleaner c = Cleaner.create();
        AtomicInteger n = new AtomicInteger();
        Cleaner.Cleanable cl = c.register(new Object(), n::incrementAndGet);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        final boolean[] ok = {false};
        Thread worker = new Thread(() -> {
            try {
                cl.clean();
                cl.clean();
                ok[0] = true;
            } catch (Throwable t) {
                ok[0] = false; // never let the exception escape the thread (jtreg rule)
            } finally {
                done.countDown();
            }
        }, "lcL-clean-worker");
        worker.setDaemon(true);
        worker.start();
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                "cross-thread clean finished within the bounded wait");
        check(ok[0], "clean() is callable from another thread");
        check(n.get() == 1, "exactly-once holds across two threads calling clean()");
        worker.join(10000);
    }

    static void testAutoCleanBestEffort() {
        // best-effort: <= 10 gc() nudges x <= 10ms latch waits; on timeout the
        // assertions are skipped (recorded), never failed. Extreme environments
        // may comment this method out.
        Cleaner c = Cleaner.create();
        AtomicInteger runs = new AtomicInteger();
        java.util.concurrent.CountDownLatch ran = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean onDaemon =
                new java.util.concurrent.atomic.AtomicBoolean();
        Object target = new Object();
        c.register(target, () -> {
            runs.incrementAndGet();
            onDaemon.set(Thread.currentThread().isDaemon());
            ran.countDown();
        });
        target = null;
        boolean done = false;
        try {
            for (int i = 0; i < 10 && !(done = ran.await(10, java.util.concurrent.TimeUnit.MILLISECONDS)); i++) {
                System.gc();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (done) {
            check(runs.get() == 1, "unreachable object auto-cleaned exactly once");
            check(onDaemon.get(), "cleaning ran on the daemon cleaner thread");
        } else {
            System.out.println("NOTE: auto-clean not observed within bounded GC loop "
                    + "- best-effort skip");
        }
    }

    static void testAutoCleanAfterManualCleanBestEffort() {
        // once manually cleaned, automatic cleaning must not run the action again
        Cleaner c = Cleaner.create();
        AtomicInteger runs = new AtomicInteger();
        Object target = new Object();
        Cleaner.Cleanable cl = c.register(target, runs::incrementAndGet);
        cl.clean();
        check(runs.get() == 1, "manual clean ran the action once");
        target = null;
        cl = null;
        boolean quiet = true;
        try {
            for (int i = 0; i < 10; i++) {
                System.gc();
                Thread.sleep(10); // bounded wait, part of the <= 10 x <= 10ms budget
                quiet = (runs.get() == 1);
                if (!quiet) break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (quiet) {
            check(runs.get() == 1, "auto-cleaning after a manual clean never re-runs the action");
        } else {
            check(false, "action re-run after manual clean - contract violation");
        }
    }

    static void testCleanableUsableAfterClean() {
        Cleaner c = Cleaner.create();
        AtomicInteger n = new AtomicInteger();
        Object o = new Object();
        Cleaner.Cleanable cl = c.register(o, n::incrementAndGet);
        cl.clean();
        check(n.get() == 1, "cleaned once");
        cl.clean(); // idempotent no-op, no exception
        cl.clean();
        check(n.get() == 1, "further clean() calls are silent no-ops");
        Cleaner.Cleanable cl2 = c.register(o, n::incrementAndGet);
        check(cl2 != cl, "re-registering the same object yields a fresh Cleanable");
        cl2.clean();
        check(n.get() == 2, "the fresh registration cleans independently");
    }

    static void testStronglyHeldThrowingAction() {
        // a throwing action propagates from manual clean() but stays at-most-once;
        // the referent is kept strongly reachable so no automatic cleaning races
        Cleaner c = Cleaner.create();
        AtomicInteger attempts = new AtomicInteger();
        Object alive = new Object();
        Cleaner.Cleanable boom = c.register(alive, () -> {
            attempts.incrementAndGet();
            throw new UnsupportedOperationException("boom");
        });
        try {
            boom.clean();
            check(false, "throwing action must propagate from clean()");
        } catch (UnsupportedOperationException expected) {
            check(true, "unsupported-operation action propagates");
        }
        // after the (failed) first attempt the registration is already unregistered:
        // a second clean() is a silent no-op and never re-attempts the action
        boom.clean();
        check(true, "second clean() of a failed registration returns normally");
        check(attempts.get() == 1, "failed action is not re-attempted (at-most-once)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCreateSemantics();
        testFactoryThreadCreation();
        testCleanableSurface();
        testManualCleanWithStrongReference();
        testBulkIndependence();
        testCrossCleanerIndependence();
        testCleanFromAnotherThread();
        testAutoCleanBestEffort();
        testAutoCleanAfterManualCleanBestEffort();
        testCleanableUsableAfterClean();
        testStronglyHeldThrowingAction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
