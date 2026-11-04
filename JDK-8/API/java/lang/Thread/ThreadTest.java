/*
 * @test
 * @summary java.lang.Thread API conformance: currentThread, name, id, priority, daemon,
 *          ThreadGroup, state transitions (NEW/RUNNABLE/TERMINATED), start vs run, join,
 *          sleep, and interrupt/isInterrupted/interrupted.
 * @run main ThreadTest
 */

public class ThreadTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "NEW before start");
        t.start();
        t.join();
        check(ran[0], "run executed");
        check(!t.isAlive(), "not alive after join");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED after join");
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
        check(ms >= 20, "sleep blocks at least requested ms");
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
        t.start();
        for (int i = 0; i < 10_000 && t.getState() != Thread.State.TIMED_WAITING; i++) {
            Thread.yield();
        }
        t.interrupt();
        t.join();
        check(interrupted[0], "sleep throws InterruptedException on interrupt");
    }

    public static void main(String[] args) throws Exception {
        testCurrentAndFields();
        testPrioritySetter();
        testNames();
        testStartRunJoin();
        testRunDirect();
        testSleep();
        testInterruptFlag();
        testSleepInterrupted();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
