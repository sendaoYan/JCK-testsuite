/*
 * @test
 * @summary java.lang.Thread conformance: white-box structure + black-box behavior.
 * @run main ThreadTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadTest {
    static int passed;

    // Exposes the protected 5-arg constructor and the protected final clone().
    static class T extends Thread {
        T(ThreadGroup g, Runnable r, String name, long stackSize, boolean inherit) {
            super(g, r, name, stackSize, inherit);
        }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectITSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // Exact-message variants (messages locked by probing both JDKs).
    static void expectNPEMsg(Runnable r, String expectedMsg, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException e) {
            if (!expectedMsg.equals(e.getMessage())) {
                throw new AssertionError(msg + ": NPE message <" + e.getMessage()
                      + "> != expected <" + expectedMsg + ">");
            }
            passed++;
        }
    }

    static void expectIAEMsg(Runnable r, String expectedMsg, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            if (!expectedMsg.equals(e.getMessage())) {
                throw new AssertionError(msg + ": IAE message <" + e.getMessage()
                      + "> != expected <" + expectedMsg + ">");
            }
            passed++;
        }
    }

    // ===================== 契约深化（2026-11）=====================

    // Bounded (<= 10s) poll until the thread reaches the expected state; the
    // returned state is then compared precisely by the caller.
    static Thread.State awaitState(Thread t, Thread.State expected) {
        long deadline = System.nanoTime() + 10_000_000_000L;
        Thread.State s = t.getState();
        while (System.nanoTime() < deadline && (s = t.getState()) != expected) {
            Thread.yield();
        }
        return s;
    }

    // Bounded (<= 10s) poll until the group reports the expected active count.
    static int awaitActiveCount(ThreadGroup g, int expected) {
        long deadline = System.nanoTime() + 10_000_000_000L;
        int c = g.activeCount();
        while (System.nanoTime() < deadline && (c = g.activeCount()) != expected) {
            Thread.yield();
        }
        return c;
    }

    // Records (thread, throwable) pairs for uncaught-exception routing.
    static class RecordingHandler implements Thread.UncaughtExceptionHandler {
        volatile Thread seenThread;
        volatile Throwable seenThrowable;
        volatile int calls;
        public void uncaughtException(Thread t, Throwable e) {
            seenThread = t;
            seenThrowable = e;
            calls++;
        }
    }

    static class RecordingGroup extends ThreadGroup {
        volatile Thread seenThread;
        volatile Throwable seenThrowable;
        volatile int calls;
        RecordingGroup(String name) { super(name); }
        @Override public void uncaughtException(Thread t, Throwable e) {
            seenThread = t;
            seenThrowable = e;
            calls++;
        }
    }

    static void testStateEnumContract() throws Exception {
        Class<?> sc = Class.forName("java.lang.Thread$State");
        check(sc.isEnum(), "Thread.State is an enum");
        check(Enum.class.isAssignableFrom(sc), "Thread.State is an Enum");
        Thread.State[] vals = Thread.State.values();
        check(vals.length == 6, "Thread.State has exactly 6 constants");
        check(vals[0] == Thread.State.NEW && vals[1] == Thread.State.RUNNABLE
              && vals[2] == Thread.State.BLOCKED && vals[3] == Thread.State.WAITING
              && vals[4] == Thread.State.TIMED_WAITING && vals[5] == Thread.State.TERMINATED,
              "State declaration order NEW,RUNNABLE,BLOCKED,WAITING,TIMED_WAITING,TERMINATED");
        check(Thread.State.valueOf("NEW") == Thread.State.NEW, "valueOf(\"NEW\")");
        check(Thread.State.valueOf("RUNNABLE") == Thread.State.RUNNABLE, "valueOf(\"RUNNABLE\")");
        check(Thread.State.valueOf("BLOCKED") == Thread.State.BLOCKED, "valueOf(\"BLOCKED\")");
        check(Thread.State.valueOf("WAITING") == Thread.State.WAITING, "valueOf(\"WAITING\")");
        check(Thread.State.valueOf("TIMED_WAITING") == Thread.State.TIMED_WAITING, "valueOf(\"TIMED_WAITING\")");
        check(Thread.State.valueOf("TERMINATED") == Thread.State.TERMINATED, "valueOf(\"TERMINATED\")");
        check("NEW".equals(Thread.State.NEW.name()), "State.NEW name()");
        check("RUNNABLE".equals(Thread.State.RUNNABLE.name()), "State.RUNNABLE name()");
        check("BLOCKED".equals(Thread.State.BLOCKED.name()), "State.BLOCKED name()");
        check("WAITING".equals(Thread.State.WAITING.name()), "State.WAITING name()");
        check("TIMED_WAITING".equals(Thread.State.TIMED_WAITING.name()), "State.TIMED_WAITING name()");
        check("TERMINATED".equals(Thread.State.TERMINATED.name()), "State.TERMINATED name()");
        check(Thread.State.NEW.equals(Thread.State.NEW), "State equals self");
        check(Thread.State.NEW.toString().equals("NEW"), "State toString is the name");
        check(Thread.State.NEW.ordinal() == 0 && Thread.State.TERMINATED.ordinal() == 5,
              "NEW ordinal 0 and TERMINATED ordinal 5");
        check(Thread.State.TERMINATED.compareTo(Thread.State.NEW) > 0, "State compareTo by ordinal");
        check(Thread.State.WAITING.getDeclaringClass() == Thread.State.class, "getDeclaringClass");
        check(Thread.State.BLOCKED.getDeclaringClass() == Thread.State.class, "getDeclaringClass (BLOCKED)");
        check(Thread.State.values() != vals, "values() returns a fresh array each call");
    }

    static void testStateMachineDeterministic() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final boolean[] selfStateRunnable = {false};
        Thread t = new Thread(() -> {
            selfStateRunnable[0] = Thread.currentThread().getState() == Thread.State.RUNNABLE;
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "sm-worker");
        check(t.getState() == Thread.State.NEW, "unstarted getState() NEW");
        check(!t.isAlive(), "unstarted isAlive() false");
        t.setDaemon(true);
        t.start();
        check(entered.await(10, TimeUnit.SECONDS), "worker entered run");
        check(selfStateRunnable[0], "running thread sees its own state as RUNNABLE");
        check(t.isAlive(), "worker alive while parked");
        // Precise: a thread parked in CountDownLatch.await() is WAITING.
        check(awaitState(t, Thread.State.WAITING) == Thread.State.WAITING,
              "thread blocked in latch.await() is WAITING");
        // While still parked the thread must not leave the {WAITING,RUNNABLE}
        // family: a spurious park wakeup may transiently show RUNNABLE before the
        // worker re-parks, so a single instant read must accept both.
        Thread.State parkedState = t.getState();
        check(parkedState == Thread.State.WAITING || parkedState == Thread.State.RUNNABLE,
              "parked thread stays WAITING (spurious wakeup may transiently show RUNNABLE)");
        release.countDown();
        t.join(10_000);
        check(!t.isAlive(), "worker dead after join");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED after join");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED stable across calls");
        check(t.getState() != Thread.State.RUNNABLE, "terminated thread is not RUNNABLE");
    }

    static void testTimedWaitingState() throws Exception {
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "tw-worker");
        t.setDaemon(true);
        t.start();
        try {
            // A thread parked in a timed await is precisely TIMED_WAITING.
            check(awaitState(t, Thread.State.TIMED_WAITING) == Thread.State.TIMED_WAITING,
                  "thread in latch.await(10s) is TIMED_WAITING");
        } finally {
            release.countDown();
            t.join(10_000);
        }
        check(!t.isAlive(), "timed-wait worker terminated");
    }

    static void testBlockedState() throws Exception {
        final Object monitor = new Object();
        final CountDownLatch entered = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            entered.countDown();
            synchronized (monitor) { /* main releases the monitor and joins */ }
        }, "blocked-worker");
        t.setDaemon(true);
        synchronized (monitor) {
            t.start();
            check(entered.await(10, TimeUnit.SECONDS), "blocked worker started");
            // Worker is trying to enter the monitor held by this thread.
            check(awaitState(t, Thread.State.BLOCKED) == Thread.State.BLOCKED,
                  "thread waiting for a monitor held by another thread is BLOCKED");
        }
        t.join(10_000);
        check(!t.isAlive(), "blocked worker terminated after monitor release");
    }

    static void testWaitingViaObjectWait() throws Exception {
        final Object obj = new Object();
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            synchronized (obj) {
                try {
                    release.countDown();
                    obj.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "wait-worker");
        t.setDaemon(true);
        t.start();
        check(release.await(10, TimeUnit.SECONDS), "wait worker started");
        try {
            // A thread in Object.wait() (no timeout) is precisely WAITING.
            check(awaitState(t, Thread.State.WAITING) == Thread.State.WAITING,
                  "thread in obj.wait() is WAITING");
        } finally {
            synchronized (obj) {
                obj.notify();
            }
            t.join(10_000);
        }
        check(!t.isAlive(), "wait worker terminated after notify");
    }

    static void testDoubleStartLive() throws Exception {
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "live-worker");
        t.setDaemon(true);
        t.start();
        try {
            check(t.isAlive(), "worker alive before second start");
            expectITSE(() -> t.start(), "second start() on a live thread");
            check(t.isAlive(), "worker still alive after failed second start");
        } finally {
            release.countDown();
            t.join(10_000);
        }
        check(!t.isAlive(), "live worker terminated");
        expectITSE(() -> t.start(), "start() on a terminated thread");
    }

    static void testStartAfterDirectRun() throws Exception {
        final boolean[] ranDirect = {false};
        final boolean[] ranThreaded = {false};
        final Thread[] holder = {null};
        Thread t = new Thread(() -> {
            if (Thread.currentThread() == holder[0]) {
                ranThreaded[0] = true;
            } else {
                ranDirect[0] = true;
            }
        });
        holder[0] = t;
        t.run();
        check(ranDirect[0], "direct run() executed on caller thread");
        check(t.getState() == Thread.State.NEW, "still NEW after direct run()");
        t.start();
        t.join(10_000);
        check(ranThreaded[0], "start() after direct run() executes body on the thread");
        check(t.getState() == Thread.State.TERMINATED, "terminated after start following direct run");
    }

    static void testDaemonLifecycle() throws Exception {
        Thread fresh = new Thread(() -> {});
        check(!fresh.isDaemon(), "fresh thread non-daemon by default");
        fresh.setDaemon(true);
        check(fresh.isDaemon(), "setDaemon(true) pre-start");
        fresh.setDaemon(false);
        check(!fresh.isDaemon(), "setDaemon(false) roundtrip pre-start");

        // setDaemon after start -> ITSE, in both directions.
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            started.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();
        try {
            check(started.await(10, TimeUnit.SECONDS), "daemon worker started");
            expectITSE(() -> t.setDaemon(false), "setDaemon(false) after start");
            expectITSE(() -> t.setDaemon(true), "setDaemon(true) after start");
            check(t.isDaemon(), "daemon flag unchanged after failed setDaemon");
        } finally {
            release.countDown();
            t.join(10_000);
        }
        check(!t.isAlive(), "daemon worker terminated");
        // setDaemon only throws while the thread is alive; on a terminated
        // thread it succeeds (and is observable).
        t.setDaemon(false);
        check(!t.isDaemon(), "setDaemon on terminated thread succeeds");
        t.setDaemon(true);
        check(t.isDaemon(), "setDaemon(true) on terminated thread observable");
    }

    static void testNamePrecise() throws Exception {
        Thread a = new Thread((Runnable) null, "r-name");
        check("r-name".equals(a.getName()), "Thread(Runnable,String) name");
        ThreadGroup g = new ThreadGroup("np-group");
        Thread b = new Thread(g, "g-name");
        check("g-name".equals(b.getName()) && b.getThreadGroup() == g, "Thread(ThreadGroup,String) name+group");
        Thread c = new Thread("s-name");
        check("s-name".equals(c.getName()), "Thread(String) name");
        Thread d = new Thread(g, (Runnable) null, "three-arg");
        check("three-arg".equals(d.getName()), "Thread(ThreadGroup,Runnable,String) name");
        Thread e = new Thread(g, (Runnable) null, "four-arg", 0L);
        check("four-arg".equals(e.getName()), "4-arg constructor name");
        d.setName("renamed");
        check("renamed".equals(d.getName()), "setName/getName roundtrip");
        d.setName("");
        check("".equals(d.getName()), "empty name allowed");
        // Exact NPE messages (locked by probe on JDK-27; note the JDK-27
        // constructor message differs from setName's).
        expectNPEMsg(() -> new Thread((String) null), "'name' is null",
                     "Thread((String)null) NPE message (JDK-27)");
        expectNPEMsg(() -> d.setName(null), "name cannot be null", "setName(null) NPE message");
        // Name survives start and termination.
        final CountDownLatch release = new CountDownLatch(1);
        final boolean[] workerSeesName = {false};
        final boolean[] workerSeesDaemon = {false};
        Thread w = new Thread(() -> {
            workerSeesName[0] = "named-worker".equals(Thread.currentThread().getName());
            workerSeesDaemon[0] = Thread.currentThread().isDaemon();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "named-worker");
        w.setDaemon(true);
        w.start();
        release.countDown();
        w.join(10_000);
        check(workerSeesName[0], "worker sees its own name");
        check(workerSeesDaemon[0], "worker observes its own daemon flag");
        check("named-worker".equals(w.getName()), "name preserved after termination");
        check(w.toString().contains("named-worker"), "toString contains name");
        check(new Thread((Runnable) null, "dflt-grp").getThreadGroup()
              == Thread.currentThread().getThreadGroup(),
              "default group is the creator's group");
    }

    static void testPriorityFull() throws Exception {
        Thread fresh = new Thread(() -> {});
        check(fresh.getPriority() == Thread.NORM_PRIORITY, "fresh thread priority NORM(5)");
        fresh.setPriority(Thread.MIN_PRIORITY);
        check(fresh.getPriority() == Thread.MIN_PRIORITY, "setPriority(MIN) roundtrip");
        fresh.setPriority(Thread.MAX_PRIORITY);
        check(fresh.getPriority() == Thread.MAX_PRIORITY, "setPriority(MAX) roundtrip");
        fresh.setPriority(Thread.NORM_PRIORITY);
        check(fresh.getPriority() == Thread.NORM_PRIORITY, "setPriority(NORM) roundtrip");
        expectIAE(() -> fresh.setPriority(-5), "setPriority(-5)");
        expectIAE(() -> fresh.setPriority(0), "setPriority(0)");
        expectIAE(() -> fresh.setPriority(Thread.MAX_PRIORITY + 1), "setPriority(MAX+1)");
        expectIAE(() -> fresh.setPriority(Integer.MAX_VALUE), "setPriority(Integer.MAX_VALUE)");

        // Group max clamps both the initial and a later priority.
        ThreadGroup low = new ThreadGroup("low-pri-group");
        low.setMaxPriority(4);
        Thread inLow = new Thread(low, () -> {});
        check(inLow.getPriority() == 4, "initial priority clamped to group max");
        inLow.setPriority(9);
        check(inLow.getPriority() == 4, "setPriority clamped to group max");
        inLow.setPriority(2);
        check(inLow.getPriority() == 2, "lowering below group max allowed");

        // New threads inherit the creator's priority.
        Thread me = Thread.currentThread();
        int saved = me.getPriority();
        try {
            me.setPriority(7);
            check(me.getPriority() == 7, "creator priority set to 7");
            Thread child = new Thread(() -> {});
            check(child.getPriority() == 7, "child inherits creator priority");
        } finally {
            me.setPriority(saved);
        }
        check(me.getPriority() == saved, "creator priority restored");

        // Priority set before start is visible inside the thread.
        final CountDownLatch release = new CountDownLatch(1);
        final int[] workerPriority = {0};
        Thread w = new Thread(() -> {
            workerPriority[0] = Thread.currentThread().getPriority();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        w.setPriority(6);
        w.setDaemon(true);
        w.start();
        release.countDown();
        w.join(10_000);
        check(workerPriority[0] == 6, "worker observes priority set before start");
        check(w.getPriority() == 6, "priority preserved after termination");
    }

    static void testPriorityDeadThread() throws Exception {
        Thread t = new Thread(() -> {});
        t.setDaemon(true);
        t.start();
        t.join(10_000);
        check(!t.isAlive(), "worker terminated");
        // JDK-27: setPriority on a terminated thread mutates the stored value.
        t.setPriority(3);
        check(t.getPriority() == 3, "setPriority on dead thread observable (JDK-27)");
    }

    static void testIdContract() throws Exception {
        Thread a = new Thread(() -> {});
        Thread b = new Thread(() -> {});
        check(a.getId() > 0, "id positive");
        check(a.getId() == a.getId(), "id stable across calls");
        check(a.getId() != b.getId(), "distinct threads have distinct ids");
        // JDK-27: threadId() is the replacement for the deprecated getId().
        check(a.threadId() > 0, "threadId positive");
        check(a.threadId() == a.getId(), "threadId() == getId()");
        check(a.threadId() == a.threadId(), "threadId stable");
        final CountDownLatch release = new CountDownLatch(1);
        final long[] workerId = {0};
        Thread w = new Thread(() -> {
            workerId[0] = Thread.currentThread().getId();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long before = w.getId();
        check(w.threadId() == before, "threadId matches before start");
        w.setDaemon(true);
        w.start();
        release.countDown();
        w.join(10_000);
        check(workerId[0] > 0, "worker id positive");
        check(workerId[0] == before, "worker sees the same id as the creator");
        check(w.getId() == before, "id preserved after termination");
    }

    static void testHoldsLockFull() {
        Object a = new Object();
        Object b = new Object();
        check(!Thread.holdsLock(a), "holdsLock false when unheld");
        synchronized (a) {
            check(Thread.holdsLock(a), "holdsLock true when held");
            check(!Thread.holdsLock(b), "holdsLock false for a different object");
            synchronized (a) {
                check(Thread.holdsLock(a), "holdsLock true for reentrant acquisition");
            }
        }
        check(!Thread.holdsLock(a), "holdsLock false after block exit");
        expectNPE(() -> Thread.holdsLock(null), "holdsLock(null) NPE (message is null)");
    }

    static void testHoldsLockCrossThread() throws Exception {
        final Object lock = new Object();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final boolean[] workerView = {true};
        Thread w = new Thread(() -> {
            entered.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
                workerView[0] = Thread.holdsLock(lock);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        w.setDaemon(true);
        synchronized (lock) {
            w.start();
            check(entered.await(10, TimeUnit.SECONDS), "worker started");
            release.countDown();
        }
        w.join(10_000);
        check(!workerView[0], "lock held by main is not held by the worker");
    }

    static void testInterruptNewThread() throws Exception {
        final boolean[] sawFlagInRun = {false};
        Thread t = new Thread(() -> sawFlagInRun[0] = Thread.currentThread().isInterrupted());
        t.setDaemon(true);
        t.interrupt(); // no exception
        // JDK-27: the flag is set on a NEW thread and observable.
        check(t.isInterrupted(), "interrupt() on NEW thread sets the flag (JDK-27)");
        t.start();
        t.join(10_000);
        check(sawFlagInRun[0], "NEW-thread interrupt visible in run() (JDK-27)");
    }

    static void testInterruptTerminated() throws Exception {
        Thread t = new Thread(() -> {});
        t.setDaemon(true);
        t.start();
        t.join(10_000);
        t.interrupt(); // no exception
        // JDK-27: the flag remains observable on a terminated thread.
        check(t.isInterrupted(), "interrupt() on terminated thread sets the flag (JDK-27)");
    }

    static void testInterruptLatchedWorker() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch caught = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final boolean[] gotIE = {false};
        final boolean[] flagClearedInCatch = {false};
        final boolean[] reinterrupted = {false};
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                gotIE[0] = true;
                flagClearedInCatch[0] = !Thread.currentThread().isInterrupted();
                Thread.currentThread().interrupt(); // re-set the flag
                reinterrupted[0] = Thread.currentThread().isInterrupted();
                // Signal only AFTER recording the re-set flag, so the creator's
                // caught.await() return happens-after this write.
                caught.countDown();
            }
        }, "ie-worker");
        t.setDaemon(true);
        t.start();
        try {
            check(entered.await(10, TimeUnit.SECONDS), "worker entered await");
            check(awaitState(t, Thread.State.WAITING) == Thread.State.WAITING,
                  "worker WAITING before interrupt");
            t.interrupt();
            check(caught.await(10, TimeUnit.SECONDS), "worker caught InterruptedException");
            check(gotIE[0], "latch.await() threw InterruptedException inside the worker");
            check(flagClearedInCatch[0], "interrupt flag is cleared when IE is thrown");
            check(reinterrupted[0], "worker can re-set the interrupt flag");
        } finally {
            release.countDown();
            t.join(10_000);
        }
        // JDK-27 keeps the re-set flag observable after termination.
        check(t.isInterrupted(), "re-set interrupt flag observable from creator after join (JDK-27)");
        check(t.getState() == Thread.State.TERMINATED, "interrupted worker terminated");
    }

    static void testInterruptedVsIsInterrupted() {
        Thread self = Thread.currentThread();
        self.interrupt();
        check(self.isInterrupted(), "isInterrupted true after self-interrupt");
        check(self.isInterrupted(), "isInterrupted is non-destructive");
        check(self.isInterrupted(), "isInterrupted still true (third call)");
        check(Thread.interrupted(), "static interrupted() returns true");
        check(!self.isInterrupted(), "interrupted() cleared the flag");
        check(!Thread.interrupted(), "second interrupted() returns false");
    }

    static void testInterruptRunningWorker() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch observed = new CountDownLatch(1);
        final boolean[] sawFlag = {false};
        Thread t = new Thread(() -> {
            entered.countDown();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            // Record the flag inside the worker before it terminates, so the
            // creator reads a value captured while the worker was alive (the
            // interrupt flag's post-exit observability differs across JDKs).
            sawFlag[0] = Thread.currentThread().isInterrupted();
            observed.countDown();
        }, "spin-worker");
        t.setDaemon(true);
        t.start();
        check(entered.await(10, TimeUnit.SECONDS), "spin worker started");
        t.interrupt(); // no lock is held by the caller: must not throw
        check(observed.await(10, TimeUnit.SECONDS), "worker observed the interrupt flag");
        check(sawFlag[0], "running worker's flag set");
        t.join(10_000);
        check(!t.isAlive(), "spin worker exited on interrupt");
    }

    static void testSelfInterruptWait() throws Exception {
        Object lock = new Object();
        Thread.currentThread().interrupt();
        boolean gotIE = false;
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                gotIE = true;
            }
        }
        check(gotIE, "pending interrupt makes wait() throw immediately");
        check(!Thread.currentThread().isInterrupted(), "IE from wait() clears the flag");
        synchronized (lock) {
            check(Thread.holdsLock(lock), "lock re-acquirable after IE from wait()");
        }
    }

    static void testSelfInterruptJoin() throws Exception {
        final CountDownLatch release = new CountDownLatch(1);
        Thread live = new Thread(() -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        live.setDaemon(true);
        live.start();
        Thread.currentThread().interrupt();
        boolean gotIE = false;
        try {
            live.join();
        } catch (InterruptedException e) {
            gotIE = true;
        }
        check(gotIE, "pending interrupt makes join() throw immediately");
        check(!Thread.currentThread().isInterrupted(), "IE from join() clears the flag");
        release.countDown();
        live.join(10_000);
        check(!live.isAlive(), "joined worker terminated");
    }

    static void testSelfInterruptSleep() throws Exception {
        Thread.currentThread().interrupt();
        boolean gotIE = false;
        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            gotIE = true;
        }
        check(gotIE, "pending interrupt makes sleep() throw immediately");
        check(!Thread.currentThread().isInterrupted(), "IE from sleep() clears the flag");
    }

    static void testUncaughtPerThreadHandler() throws Exception {
        final CountDownLatch done = new CountDownLatch(1);
        final RecordingHandler h = new RecordingHandler();
        final RuntimeException boom = new RuntimeException("boom-per-thread");
        Thread t = new Thread(() -> {
            throw boom;
        }, "ue-per-thread");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((th, ex) -> {
            h.uncaughtException(th, ex);
            done.countDown();
        });
        t.start();
        check(done.await(10, TimeUnit.SECONDS), "handler was invoked");
        check(h.calls == 1, "per-thread handler called exactly once");
        check(h.seenThread == t, "handler receives the dying thread itself");
        check(h.seenThrowable == boom, "handler receives the same exception instance");
        t.join(10_000);
        check(!t.isAlive(), "thread died despite handled exception");
        check(t.getState() == Thread.State.TERMINATED, "uncaught-exception thread TERMINATED");
    }

    // The JVM consults the default handler only when a dying thread's group
    // chain reaches the unmodified root group. Test harnesses (jtreg) wrap the
    // test main thread in their own ThreadGroup whose uncaughtException override
    // would intercept the dispatch before the default handler, so anchor the
    // worker directly in the real root group for a harness-independent route.
    static ThreadGroup rootThreadGroup() {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while (g.getParent() != null) {
            g = g.getParent();
        }
        return g;
    }

    static void testUncaughtDefaultRouting() throws Exception {
        Thread.UncaughtExceptionHandler savedDefault = Thread.getDefaultUncaughtExceptionHandler();
        try {
            check(savedDefault == null, "no default handler installed by the harness");
            final RecordingHandler h = new RecordingHandler();
            final RuntimeException boom = new RuntimeException("boom-default");
            Thread.setDefaultUncaughtExceptionHandler(h);
            check(Thread.getDefaultUncaughtExceptionHandler() == h, "setDefaultUncaughtExceptionHandler roundtrip");
            final CountDownLatch done = new CountDownLatch(1);
            Thread.UncaughtExceptionHandler counting = (th, ex) -> { h.uncaughtException(th, ex); done.countDown(); };
            Thread.setDefaultUncaughtExceptionHandler(counting);
            Thread t = new Thread(rootThreadGroup(), () -> {
                throw boom;
            }, "ue-default");
            t.setDaemon(true);
            t.start();
            check(done.await(10, TimeUnit.SECONDS), "default handler was invoked");
            check(h.seenThread == t, "default handler receives the dying thread");
            check(h.seenThrowable == boom, "default handler receives the same exception instance");
            t.join(10_000);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(savedDefault);
        }
        check(Thread.getDefaultUncaughtExceptionHandler() == null, "default handler restored");
    }

    static void testUncaughtGroupRoute() throws Exception {
        final RecordingGroup rg = new RecordingGroup("ue-group");
        final RuntimeException boom = new RuntimeException("boom-group");
        Thread t = new Thread(rg, () -> {
            throw boom;
        }, "ue-group-worker");
        t.setDaemon(true);
        t.start();
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (rg.calls == 0 && System.nanoTime() < deadline) {
            Thread.yield();
        }
        check(rg.calls == 1, "group.uncaughtException called exactly once");
        check(rg.seenThread == t, "group handler receives the dying thread");
        check(rg.seenThrowable == boom, "group handler receives the same exception instance");
        t.join(10_000);
    }

    static void testUncaughtPrecedence() throws Exception {
        final RecordingGroup rg = new RecordingGroup("ue-prec-group");
        final RecordingHandler h = new RecordingHandler();
        final RuntimeException boom = new RuntimeException("boom-prec");
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = new Thread(rg, () -> {
            throw boom;
        }, "ue-prec");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((th, ex) -> { h.uncaughtException(th, ex); done.countDown(); });
        t.start();
        check(done.await(10, TimeUnit.SECONDS), "per-thread handler invoked");
        check(h.calls == 1 && h.seenThrowable == boom, "per-thread handler takes precedence");
        t.join(10_000);
        check(rg.calls == 0, "group handler not consulted when per-thread handler set");
    }

    static void testUncaughtErrorRoute() throws Exception {
        final RecordingHandler h = new RecordingHandler();
        final Error err = new AssertionError("err-route");
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            throw err;
        }, "ue-error");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((th, ex) -> { h.uncaughtException(th, ex); done.countDown(); });
        t.start();
        check(done.await(10, TimeUnit.SECONDS), "handler invoked for an Error");
        check(h.seenThrowable == err, "handler receives the same Error instance");
        check(h.seenThrowable instanceof Error, "routed throwable is an Error");
        t.join(10_000);
    }

    static void testUncaughtNormalExit() throws Exception {
        final RecordingHandler h = new RecordingHandler();
        Thread t = new Thread(() -> {}, "ue-normal");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(h);
        t.start();
        t.join(10_000);
        check(!t.isAlive(), "normal worker terminated");
        check(h.calls == 0, "handler not invoked on normal exit");
    }

    static void testUncaughtHandlerDefaults() {
        ThreadGroup g = new ThreadGroup("ue-defaults-group");
        Thread t = new Thread(g, () -> {});
        check(t.getUncaughtExceptionHandler() == g, "default handler is the thread's group");
        Thread.UncaughtExceptionHandler h = (th, ex) -> {};
        t.setUncaughtExceptionHandler(h);
        check(t.getUncaughtExceptionHandler() == h, "setUncaughtExceptionHandler roundtrip");
        t.setUncaughtExceptionHandler(null);
        check(t.getUncaughtExceptionHandler() == g, "setUncaughtExceptionHandler(null) reverts to group");
        check(g.toString().contains("ue-defaults-group"), "group toString contains name");
    }

    static void testJoinForms() throws Exception {
        // join(0) and join(ms) on a terminated thread return immediately.
        Thread dead = new Thread(() -> {});
        dead.setDaemon(true);
        dead.start();
        dead.join(10_000);
        dead.join(0);
        check(!dead.isAlive(), "join(0) on terminated thread returns");
        long t0 = System.nanoTime();
        dead.join(100);
        check(System.nanoTime() - t0 < 5_000_000_000L, "join(100) on terminated thread returns promptly");
        dead.join(0, 0);
        check(!dead.isAlive(), "join(0,0) on terminated thread returns");
        dead.join(1, 500_000);
        check(!dead.isAlive(), "join(1,500000) on terminated thread returns");

        // join on a never-started thread returns immediately (not alive).
        Thread never = new Thread(() -> {});
        long t1 = System.nanoTime();
        never.join();
        check(System.nanoTime() - t1 < 5_000_000_000L, "join() on NEW thread returns immediately");

        // Timed join on a live thread waits out its timeout and returns.
        final CountDownLatch release = new CountDownLatch(1);
        Thread live = new Thread(() -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        live.setDaemon(true);
        live.start();
        long t2 = System.currentTimeMillis();
        live.join(80);
        long waited = System.currentTimeMillis() - t2;
        check(waited >= 80, "join(80) on live thread waited at least 80ms");
        check(live.isAlive(), "thread still alive after timed-out join");
        // Negative/out-of-range timeout arguments are rejected, even on a dead thread.
        expectIAEMsg(() -> { try { dead.join(-1); } catch (InterruptedException e) { throw new RuntimeException(e); } },
                     "timeout value is negative", "join(-1) IAE message");
        expectIAEMsg(() -> { try { dead.join(0, -1); } catch (InterruptedException e) { throw new RuntimeException(e); } },
                     "nanosecond timeout value out of range", "join(0,-1) IAE message");
        expectIAEMsg(() -> { try { dead.join(0, 1_000_000); } catch (InterruptedException e) { throw new RuntimeException(e); } },
                     "nanosecond timeout value out of range", "join(0,1000000) IAE message");
        release.countDown();
        live.join(10_000);
        live.join(0);
        check(!live.isAlive(), "join(0) returns once the worker exits");
    }

    static void testSleepForms() throws Exception {
        Thread.sleep(0);
        passed++; // sleep(0) is legal and returns immediately
        Thread.sleep(1);
        check(Thread.currentThread().isAlive(), "current thread alive after sleep(1)");
        long t0 = System.currentTimeMillis();
        Thread.sleep(5, 500_000);
        check(System.currentTimeMillis() - t0 >= 5, "sleep(5,500000) waits at least 5ms");
        Thread.sleep(0, 0);
        passed++; // sleep(0,0) is legal
        expectIAEMsg(() -> doSleep(-1), "timeout value is negative", "sleep(-1) IAE message");
        expectIAEMsg(() -> doSleep(0, -1), "nanosecond timeout value out of range", "sleep(0,-1) IAE message");
        expectIAEMsg(() -> doSleep(0, 1_000_000), "nanosecond timeout value out of range",
                     "sleep(0,1000000) IAE message");
    }

    static void doSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void doSleep(long ms, int nanos) {
        try {
            Thread.sleep(ms, nanos);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void testYieldContract() {
        Thread.yield();
        passed++; // yield() never throws
        // interrupt() requires no monitor: contrast with Object.notify(), which
        // would throw IllegalMonitorStateException here.
        Thread self = Thread.currentThread();
        self.interrupt();
        check(Thread.interrupted(), "interrupt() without holding a lock does not throw IMSE");
        Thread.yield();
        passed++; // yield() callable while the interrupt flag is set
    }

    static void testToStringPrecise() throws Exception {
        ThreadGroup g = new ThreadGroup("ts-group");
        Thread t = new Thread(g, () -> {}, "ts-name");
        // JDK-27 format: Thread[#id,name,priority,group]
        String expected = "Thread[#" + t.threadId() + ",ts-name,5,ts-group]";
        check(expected.equals(t.toString()), "toString exact format (JDK-27)");
        t.setPriority(7);
        check(t.toString().equals("Thread[#" + t.threadId() + ",ts-name,7,ts-group]"),
              "toString reflects priority");
        t.setName("ts-renamed");
        check(t.toString().equals("Thread[#" + t.threadId() + ",ts-renamed,7,ts-group]"),
              "toString reflects rename");
        t.setDaemon(true);
        t.start();
        t.join(10_000);
        // Dead thread: group renders as the empty string.
        check(t.toString().equals("Thread[#" + t.threadId() + ",ts-renamed,7,]"),
              "toString of terminated thread (JDK-27)");
    }

    static void testGetStackTraceStates() throws Exception {
        Thread fresh = new Thread(() -> {});
        check(fresh.getStackTrace().length == 0, "getStackTrace on NEW thread is empty");
        final CountDownLatch release = new CountDownLatch(1);
        Thread w = new Thread(() -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "st-worker");
        w.setDaemon(true);
        w.start();
        check(awaitState(w, Thread.State.WAITING) == Thread.State.WAITING, "worker WAITING");
        check(w.getStackTrace().length > 0, "getStackTrace of live worker non-empty");
        release.countDown();
        w.join(10_000);
        check(w.getStackTrace().length == 0, "getStackTrace on terminated thread is empty");
        StackTraceElement[] own = Thread.currentThread().getStackTrace();
        check(own.length > 0, "own stack trace non-empty");
        boolean sawSelf = false;
        for (StackTraceElement e : own) {
            if (e.getClassName().equals(ThreadTest.class.getName())) {
                sawSelf = true;
                break;
            }
        }
        check(sawSelf, "own stack trace contains this class");
    }

    static void testGroupMembershipLifecycle() throws Exception {
        ThreadGroup g = new ThreadGroup("gm-group");
        check(g.activeCount() == 0, "fresh group has no active threads");
        final CountDownLatch release = new CountDownLatch(1);
        Thread w = new Thread(g, () -> {
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "gm-worker");
        w.setDaemon(true);
        check(w.getThreadGroup() == g, "worker group before start");
        w.start();
        check(awaitActiveCount(g, 1) == 1, "group activeCount == 1 while worker alive");
        Thread[] found = new Thread[2];
        int n = g.enumerate(found, false);
        check(n == 1, "enumerate returns exactly the live worker");
        check(found[0] == w, "enumerate contains the worker by identity");
        release.countDown();
        w.join(10_000);
        check(awaitActiveCount(g, 0) == 0, "group activeCount back to 0 after join");
        check(g.enumerate(new Thread[2], false) == 0, "enumerate empty after worker exit");
        check(w.getThreadGroup() == null, "getThreadGroup() null after termination");
    }

    static class RunOverride extends Thread {
        final boolean[] ranBody;
        RunOverride(Runnable r, boolean[] ranBody) { super(r); this.ranBody = ranBody; }
        @Override public void run() { ranBody[0] = true; }
    }

    static void testRunOverrideSubclass() throws Exception {
        final boolean[] runnableRan = {false};
        final boolean[] bodyRan = {false};
        RunOverride t = new RunOverride(() -> runnableRan[0] = true, bodyRan);
        t.setDaemon(true);
        t.start();
        t.join(10_000);
        check(bodyRan[0], "overridden run() wins");
        check(!runnableRan[0], "constructor Runnable not executed when run() is overridden");
        check(t.getState() == Thread.State.TERMINATED, "subclass thread terminated");
        check(t instanceof Thread, "subclass instance is a Thread");
    }

    static void testStaticEnumerateForms() {
        check(Thread.enumerate(new Thread[0]) == 0, "static enumerate with zero-length array returns 0");
        Thread[] arr = new Thread[Thread.activeCount() + 4];
        int n = Thread.enumerate(arr);
        check(n >= 1, "static enumerate(Thread[]) one-arg returns >= 1");
        boolean sawCurrent = false;
        for (int i = 0; i < n; i++) {
            if (arr[i] == Thread.currentThread()) {
                sawCurrent = true;
                break;
            }
        }
        check(sawCurrent, "static enumerate includes the current thread");
    }

    static void testContextClassLoaderNull() {
        Thread me = Thread.currentThread();
        ClassLoader saved = me.getContextClassLoader();
        try {
            me.setContextClassLoader(null);
            check(me.getContextClassLoader() == null, "setContextClassLoader(null) accepted");
        } finally {
            me.setContextClassLoader(saved);
        }
        check(me.getContextClassLoader() == saved, "context class loader restored");
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> c = Class.forName("java.lang.Thread");
        check(Modifier.isStatic(c.getMethod("enumerate", Thread[].class).getModifiers()),
              "enumerate(Thread[]) static");
        check(Modifier.isStatic(c.getMethod("interrupted").getModifiers()), "interrupted() static");
        check(Modifier.isStatic(c.getMethod("holdsLock", Object.class).getModifiers()), "holdsLock static");
        check(Modifier.isNative(c.getMethod("holdsLock", Object.class).getModifiers()), "holdsLock native");
        check(Modifier.isStatic(c.getMethod("yield").getModifiers()), "yield static");
        check(Modifier.isStatic(c.getMethod("onSpinWait").getModifiers()), "onSpinWait static (JDK-27)");
        check(Modifier.isPublic(Class.forName("java.lang.Thread$UncaughtExceptionHandler").getModifiers()),
              "UncaughtExceptionHandler is a public interface");
        check(Class.forName("java.lang.Thread$UncaughtExceptionHandler").isInterface(),
              "UncaughtExceptionHandler is an interface");
        check(Modifier.isStatic(c.getMethod("ofVirtual").getModifiers()), "ofVirtual static (JDK-27)");
    }

    // ---- virtual threads (JDK-27 only; absent from JDK-8) ----
    static void testVirtualThreads() throws Exception {
        // Main runs on a platform thread.
        check(!Thread.currentThread().isVirtual(), "main thread is not virtual");
        check(!new Thread(() -> {}).isVirtual(), "platform thread isVirtual false");

        // ofVirtual().start: started immediately, daemon by definition.
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final boolean[] selfVirtual = {false};
        Thread vt = Thread.ofVirtual().start(() -> {
            selfVirtual[0] = Thread.currentThread().isVirtual();
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        check(entered.await(10, TimeUnit.SECONDS), "virtual worker entered run");
        check(vt.isVirtual(), "ofVirtual().start() thread isVirtual true");
        check(selfVirtual[0], "virtual worker sees itself as virtual");
        check(vt.isDaemon(), "virtual threads are always daemon");
        check(vt.getPriority() == Thread.NORM_PRIORITY, "virtual thread priority is NORM");
        vt.setPriority(9);
        check(vt.getPriority() == Thread.NORM_PRIORITY, "setPriority on virtual thread is a no-op");
        vt.setName("vt-name");
        check("vt-name".equals(vt.getName()), "setName on virtual thread");
        check(vt.toString().startsWith("VirtualThread["), "virtual toString starts with VirtualThread[");
        check(vt.threadId() > 0 && vt.threadId() == vt.getId(), "virtual threadId == getId");
        check(awaitState(vt, Thread.State.WAITING) == Thread.State.WAITING,
              "virtual worker parked on latch is WAITING");
        try {
            vt.setDaemon(false);
            check(false, "setDaemon(false) on virtual thread should throw");
        } catch (IllegalArgumentException e) {
            check("'false' not legal for virtual threads".equals(e.getMessage()),
                  "setDaemon(false) IAE message (JDK-27)");
        }
        try {
            vt.setDaemon(true);
            check(false, "setDaemon(true) on started virtual thread should throw ITSE");
        } catch (IllegalThreadStateException e) {
            passed++;
        }
        check(vt.isDaemon(), "virtual thread still daemon after failed setDaemon");
        release.countDown();
        vt.join(10_000);
        check(!vt.isAlive(), "virtual worker terminated");
        check(vt.getState() == Thread.State.TERMINATED, "virtual thread TERMINATED");
        expectITSE(() -> vt.start(), "double start on terminated virtual thread");

        // unstarted: NEW until start().
        Thread un = Thread.ofVirtual().unstarted(() -> {});
        check(un.getState() == Thread.State.NEW, "ofVirtual().unstarted() is NEW");
        check(un.isVirtual(), "unstarted virtual thread isVirtual true");
        check(un.isDaemon(), "unstarted virtual thread daemon");
        un.start();
        un.join(10_000);
        check(un.getState() == Thread.State.TERMINATED, "unstarted virtual terminated after start+join");
        expectITSE(() -> un.start(), "second start on terminated virtual thread");

        // Thread.onSpinWait (JDK 9+): callable, never throws.
        Thread.onSpinWait();
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Thread");
        check(!c.isInterface(), "Thread is a class");
        check(!Modifier.isFinal(c.getModifiers()), "Thread is subclassable (not final)");
        check(Runnable.class.isAssignableFrom(c), "Thread implements Runnable");
        check(Modifier.isNative(c.getMethod("currentThread").getModifiers()), "currentThread native");
        check(Modifier.isStatic(c.getMethod("sleep", long.class).getModifiers()), "sleep static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCurrentAndFields() {
        Thread t = Thread.currentThread();
        check(t == Thread.currentThread(), "currentThread returns the calling thread");
        check(t.isAlive(), "current thread alive");
        check(t.getName() != null && !t.getName().isEmpty(), "name non-null/non-empty");
        t.setName("thread-test-main");
        check(t.getName().equals("thread-test-main"), "setName/getName roundtrip");
        check(t.getId() > 0, "id positive");
        check(t.getPriority() >= Thread.MIN_PRIORITY && t.getPriority() <= Thread.MAX_PRIORITY,
              "priority within [MIN,MAX]");
        check(Thread.MIN_PRIORITY == 1 && Thread.NORM_PRIORITY == 5 && Thread.MAX_PRIORITY == 10,
              "priority constants 1/5/10");
        check(!t.isDaemon(), "main thread non-daemon");
        check(t.getThreadGroup() != null, "thread group non-null");
        check(t.getState() == Thread.State.RUNNABLE, "current thread RUNNABLE");
        check(t.getContextClassLoader() != null, "context class loader non-null");
    }

    static void testPrioritySetter() {
        Thread t = Thread.currentThread();
        int orig = t.getPriority();
        t.setPriority(Thread.MAX_PRIORITY);
        check(t.getPriority() == Thread.MAX_PRIORITY, "setPriority(MAX)");
        t.setPriority(Thread.MIN_PRIORITY);
        check(t.getPriority() == Thread.MIN_PRIORITY, "setPriority(MIN)");
        t.setPriority(orig);
        check(t.getPriority() == orig, "restore priority");
        try {
            t.setPriority(Thread.MAX_PRIORITY + 1);
            throw new AssertionError("setPriority(MAX+1): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        t.setPriority(orig);
    }

    static void testNames() {
        Thread a = new Thread();
        Thread b = new Thread();
        check(a.getName().startsWith("Thread-"), "default name prefix");
        check(!a.getName().equals(b.getName()), "default names distinct");
        Thread named = new Thread(() -> {}, "named-worker");
        check(named.getName().equals("named-worker"), "Thread(Runnable,String) name");
        Thread nameOnly = new Thread("name-only");
        check(nameOnly.getName().equals("name-only"), "Thread(String) name");
    }

    static void testStartRunJoin() throws Exception {
        final boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true, "worker");
        t.setDaemon(true);
        check(t.isDaemon(), "daemon flag set before start");
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "NEW before start");
        t.start();
        t.join();
        check(ran[0], "run executed");
        check(!t.isAlive(), "not alive after join");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED after join");
        // join on an already-terminated thread returns immediately.
        long start = System.nanoTime();
        t.join();
        check(System.nanoTime() - start < 1_000_000_000L, "join on terminated thread returns promptly");
    }

    static void testRunDirect() {
        final boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true);
        t.run();
        check(ran[0], "run() executes runnable on calling thread");
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "direct run() leaves thread NEW");
    }

    static void testSleep() throws Exception {
        long start = System.nanoTime();
        Thread.sleep(20);
        long ms = (System.nanoTime() - start) / 1_000_000L;
        // javadoc: sleep is "subject to the precision and accuracy of system
        // timers"; Windows timer granularity is ~15.6ms, so a 20ms request may
        // return slightly early. Allow 5ms tolerance while still requiring
        // approximately the requested delay.
        check(ms >= 20 - 5, "sleep blocks at least close to requested ms");
    }

    static void testInterruptFlag() {
        Thread t = Thread.currentThread();
        t.interrupt();
        check(t.isInterrupted(), "interrupt sets flag");
        check(Thread.interrupted(), "interrupted() returns and clears flag");
        check(!t.isInterrupted(), "flag cleared");
    }

    static void testSleepInterrupted() throws Exception {
        final boolean[] interrupted = {false};
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                interrupted[0] = true;
            }
        });
        t.setDaemon(true);
        t.start();
        for (int i = 0; i < 10_000 && t.getState() != Thread.State.TIMED_WAITING; i++) {
            Thread.yield();
        }
        t.interrupt();
        t.join();
        check(interrupted[0], "sleep throws InterruptedException on interrupt");
    }

    static void testHoldsLock() {
        Object lock = new Object();
        check(!Thread.holdsLock(lock), "holdsLock false when not held");
        synchronized (lock) {
            check(Thread.holdsLock(lock), "holdsLock true when held");
        }
    }

    // ---- Part 2b: black-box for the previously uncovered Thread methods ----
    static void testBlackBox() throws Exception {
        Thread current = Thread.currentThread();

        // activeCount / enumerate (static)
        check(Thread.activeCount() >= 1, "activeCount >= 1");
        Thread[] arr = new Thread[Thread.activeCount() + 8];
        int n = Thread.enumerate(arr);
        check(n >= 1, "enumerate returns >= 1 thread");

        // getStackTrace
        StackTraceElement[] st = current.getStackTrace();
        check(st != null && st.length > 0, "getStackTrace non-empty");

        // dumpStack (prints to stderr, must not throw) and checkAccess (no SM -> no-op)
        Thread.dumpStack();
        passed++;
        current.checkAccess();
        passed++;

        // threadId / toString
        check(current.threadId() > 0, "threadId positive");
        String s = current.toString();
        check(s.startsWith("Thread[") && s.contains(current.getName()), "toString format");

        // setContextClassLoader roundtrip (restored in finally)
        ClassLoader origCl = current.getContextClassLoader();
        try {
            ClassLoader custom = new ClassLoader(ClassLoader.getSystemClassLoader()) { };
            current.setContextClassLoader(custom);
            check(current.getContextClassLoader() == custom, "setContextClassLoader sets");
            current.setContextClassLoader(origCl);
            check(current.getContextClassLoader() == origCl, "setContextClassLoader restored");
        } finally {
            current.setContextClassLoader(origCl);
        }

        // (ThreadGroup, Runnable) and (ThreadGroup, String) constructors
        ThreadGroup g = new ThreadGroup("bb-group");
        Thread t1 = new Thread(g, () -> {});
        check(t1.getThreadGroup() == g, "Thread(ThreadGroup,Runnable) group");
        Thread t2 = new Thread(g, "named-in-group");
        check(t2.getThreadGroup() == g && "named-in-group".equals(t2.getName()),
              "Thread(ThreadGroup,String)");

        // Protected 5-arg constructor via subclass.
        T t3 = new T(g, () -> {}, "five-arg", 0L, true);
        check("five-arg".equals(t3.getName()) && t3.getThreadGroup() == g,
              "5-arg constructor sets name and group");

        // clone() is protected final and always throws CloneNotSupportedException.
        try {
            t3.doClone();
            throw new AssertionError("Thread.clone: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }

        // startVirtualThread: returns a started virtual thread.
        final boolean[] ran = {false};
        Thread vt = Thread.startVirtualThread(() -> ran[0] = true);
        vt.join();
        check(ran[0], "virtual thread ran");
        check(vt.isVirtual(), "isVirtual true");
        check(vt.getState() == Thread.State.TERMINATED, "virtual thread terminated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentAndFields();
        testPrioritySetter();
        testNames();
        testStartRunJoin();
        testRunDirect();
        testSleep();
        testInterruptFlag();
        testSleepInterrupted();
        testHoldsLock();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testStateEnumContract();
        testStateMachineDeterministic();
        testTimedWaitingState();
        testBlockedState();
        testWaitingViaObjectWait();
        testDoubleStartLive();
        testStartAfterDirectRun();
        testDaemonLifecycle();
        testNamePrecise();
        testPriorityFull();
        testPriorityDeadThread();
        testIdContract();
        testHoldsLockFull();
        testHoldsLockCrossThread();
        testInterruptNewThread();
        testInterruptTerminated();
        testInterruptLatchedWorker();
        testInterruptedVsIsInterrupted();
        testInterruptRunningWorker();
        testSelfInterruptWait();
        testSelfInterruptJoin();
        testSelfInterruptSleep();
        testUncaughtPerThreadHandler();
        testUncaughtDefaultRouting();
        testUncaughtGroupRoute();
        testUncaughtPrecedence();
        testUncaughtErrorRoute();
        testUncaughtNormalExit();
        testUncaughtHandlerDefaults();
        testJoinForms();
        testSleepForms();
        testYieldContract();
        testToStringPrecise();
        testGetStackTraceStates();
        testGroupMembershipLifecycle();
        testRunOverrideSubclass();
        testStaticEnumerateForms();
        testContextClassLoaderNull();
        testWhiteBoxMore();
        testVirtualThreads();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
