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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
