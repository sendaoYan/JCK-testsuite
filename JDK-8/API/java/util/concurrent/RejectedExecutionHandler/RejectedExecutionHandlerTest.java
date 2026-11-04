/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionHandler API conformance: rejectedExecution via AbortPolicy.
 * @run main RejectedExecutionHandlerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RejectedExecutionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Runnable with a stable toString so the AbortPolicy message is deterministic.
    static class FixedTask implements Runnable {
        public void run() { }
        public String toString() { return "FIXED-TASK"; }
    }

    static void testRejectedExecutionHandler() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
            boolean threw = false;
            try {
                handler.rejectedExecution(() -> { }, exec);
            } catch (RejectedExecutionException expected) {
                threw = true;
            }
            check(threw, "AbortPolicy throws RejectedExecutionException");
        } finally {
            exec.shutdown();
        }
    }

    // ---- white-box: interface + the four built-in policy classes ----
    static void testWhiteBoxStructure() throws Exception {
        Class<RejectedExecutionHandler> c = RejectedExecutionHandler.class;
        check(c.isInterface(), "RejectedExecutionHandler is an interface");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        check(c.getMethod("rejectedExecution", Runnable.class, ThreadPoolExecutor.class)
                  .getReturnType() == void.class, "rejectedExecution(Runnable, ThreadPoolExecutor)");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "functional by shape only: no @FunctionalInterface annotation (实测)");

        Class<?>[] policies = {
            ThreadPoolExecutor.AbortPolicy.class,
            ThreadPoolExecutor.DiscardPolicy.class,
            ThreadPoolExecutor.DiscardOldestPolicy.class,
            ThreadPoolExecutor.CallerRunsPolicy.class,
        };
        String[] names = { "AbortPolicy", "DiscardPolicy", "DiscardOldestPolicy", "CallerRunsPolicy" };
        for (int i = 0; i < policies.length; i++) {
            Class<?> p = policies[i];
            check(Modifier.isPublic(p.getModifiers()), names[i] + " is public");
            check(RejectedExecutionHandler.class.isAssignableFrom(p),
                  names[i] + " implements RejectedExecutionHandler");
            check(p.getConstructors().length == 1, names[i] + " has exactly one public ctor");
            check(p.getConstructor() != null, names[i] + " no-arg ctor");
            check(p.getMethod("rejectedExecution", Runnable.class, ThreadPoolExecutor.class) != null,
                  names[i] + " overrides rejectedExecution");
        }
    }

    // ---- policy toString non-null, default handler is AbortPolicy ----
    static void testPolicyToStringAndDefault() {
        RejectedExecutionHandler[] ps = {
            new ThreadPoolExecutor.AbortPolicy(),
            new ThreadPoolExecutor.DiscardPolicy(),
            new ThreadPoolExecutor.DiscardOldestPolicy(),
            new ThreadPoolExecutor.CallerRunsPolicy(),
        };
        for (RejectedExecutionHandler p : ps) {
            check(p.toString() != null, p.getClass().getSimpleName() + ".toString() non-null");
        }
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            check(exec.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy,
                  "default handler of ThreadPoolExecutor is AbortPolicy");
        } finally {
            exec.shutdownNow();
        }
    }

    // Builds a saturated-but-running pool: 1 worker blocked on a latch, queue capacity 1.
    static ThreadPoolExecutor saturatedPool(CountDownLatch started, CountDownLatch hold) {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        exec.execute(() -> {
            started.countDown();
            try { hold.await(); } catch (InterruptedException ie) { }
        });
        try {
            if (!started.await(5, TimeUnit.SECONDS)) throw new AssertionError("worker did not start");
        } catch (InterruptedException e) { throw new AssertionError(e); }
        return exec;
    }

    static ThreadPoolExecutor shutdownPool() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        exec.shutdown();
        return exec;
    }

    // ---- AbortPolicy: exact exception + message format ----
    static void testAbortPolicy() {
        CountDownLatch hold = new CountDownLatch(1);
        ThreadPoolExecutor exec = saturatedPool(new CountDownLatch(1), hold);
        try {
            boolean threw = false;
            try {
                new ThreadPoolExecutor.AbortPolicy().rejectedExecution(new FixedTask(), exec);
            } catch (RejectedExecutionException expected) {
                threw = true;
                String m = expected.getMessage();
                check(m != null, "AbortPolicy exception has a message");
                check(m.startsWith("Task FIXED-TASK rejected from "),
                      "message starts 'Task <toString> rejected from '");
                check(m.contains("ThreadPoolExecutor@") && m.contains("["),
                      "message embeds the executor toString");
                check(expected.getCause() == null, "rejection carries no cause");
            }
            check(threw, "AbortPolicy rejects with RejectedExecutionException (saturated)");
        } finally {
            hold.countDown();
            exec.shutdownNow();
        }

        ThreadPoolExecutor dead = shutdownPool();
        try {
            boolean threw = false;
            try {
                new ThreadPoolExecutor.AbortPolicy().rejectedExecution(new FixedTask(), dead);
            } catch (RejectedExecutionException expected) {
                threw = true;
                check(expected.getMessage() != null
                          && expected.getMessage().startsWith("Task FIXED-TASK rejected from "),
                      "shutdown-rejection message has the same format");
            }
            check(threw, "AbortPolicy rejects on a shutdown executor too");
        } finally {
            dead.shutdownNow();
        }
    }

    // ---- DiscardPolicy: silent, no run, queue untouched ----
    static void testDiscardPolicy() {
        CountDownLatch hold = new CountDownLatch(1);
        ThreadPoolExecutor exec = saturatedPool(new CountDownLatch(1), hold);
        try {
            FixedTask queued = new FixedTask();
            exec.execute(queued); // fills the queue
            int sizeBefore = exec.getQueue().size();
            AtomicInteger ran = new AtomicInteger();
            new ThreadPoolExecutor.DiscardPolicy().rejectedExecution(ran::incrementAndGet, exec);
            check(ran.get() == 0, "DiscardPolicy never runs the task");
            check(exec.getQueue().size() == sizeBefore, "DiscardPolicy leaves the queue unchanged");
            check(exec.getQueue().contains(queued), "queued task still present");
        } finally {
            hold.countDown();
            exec.shutdownNow();
        }

        ThreadPoolExecutor dead = shutdownPool();
        try {
            AtomicInteger ran = new AtomicInteger();
            new ThreadPoolExecutor.DiscardPolicy().rejectedExecution(ran::incrementAndGet, dead);
            check(ran.get() == 0, "DiscardPolicy silent on shutdown executor");
            check(true, "DiscardPolicy never throws");
        } finally {
            dead.shutdownNow();
        }
    }

    // ---- DiscardOldestPolicy: full queue drops oldest; empty queue re-offers; shutdown drops ----
    static void testDiscardOldestPolicy() {
        // (a) full queue + running: oldest queued element is discarded, new task offered
        CountDownLatch hold = new CountDownLatch(1);
        ThreadPoolExecutor exec = saturatedPool(new CountDownLatch(1), hold);
        try {
            FixedTask oldest = new FixedTask();
            exec.execute(oldest);
            FixedTask newest = new FixedTask();
            new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(newest, exec);
            check(!exec.getQueue().contains(oldest), "oldest queued task was discarded");
            check(exec.getQueue().contains(newest), "new task was re-offered into the queue");
            check(exec.getQueue().size() == 1, "queue still holds exactly one task");
            check(exec.getQueue().peek() == newest, "head of queue is the new task");
        } finally {
            hold.countDown();
            exec.shutdownNow();
        }

        // (b) empty queue + running: poll() returns null, then the task is offered normally
        CountDownLatch hold2 = new CountDownLatch(1);
        ThreadPoolExecutor exec2 = saturatedPool(new CountDownLatch(1), hold2);
        try {
            check(exec2.getQueue().isEmpty(), "queue empty before DiscardOldest probe");
            FixedTask t = new FixedTask();
            new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(t, exec2);
            check(exec2.getQueue().peek() == t,
                  "empty-queue case: task offered after poll() returned null");
            check(exec2.getQueue().size() == 1, "queue now holds the task");
        } finally {
            hold2.countDown();
            exec2.shutdownNow();
        }

        // (c) shutdown: poll() drains nothing, executor is down => task silently dropped
        ThreadPoolExecutor dead = shutdownPool();
        try {
            AtomicInteger ran = new AtomicInteger();
            new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(ran::incrementAndGet, dead);
            check(ran.get() == 0, "DiscardOldest drops the task when executor is shutdown");
            check(dead.getQueue().isEmpty(), "queue still empty");
        } finally {
            dead.shutdownNow();
        }
    }

    // ---- CallerRunsPolicy: runs in the calling thread unless the executor is shutdown ----
    static void testCallerRunsPolicy() {
        CountDownLatch hold = new CountDownLatch(1);
        ThreadPoolExecutor exec = saturatedPool(new CountDownLatch(1), hold);
        try {
            FixedTask queued = new FixedTask();
            exec.execute(queued);
            final String[] runner = new String[1];
            final AtomicInteger ran = new AtomicInteger();
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(() -> {
                ran.incrementAndGet();
                runner[0] = Thread.currentThread().getName();
            }, exec);
            check(ran.get() == 1, "CallerRunsPolicy executed the task synchronously");
            check(runner[0] != null && runner[0].equals(Thread.currentThread().getName()),
                  "CallerRunsPolicy runs the task in the caller thread");
            check(exec.getQueue().size() == 1, "queue untouched by CallerRuns");
        } finally {
            hold.countDown();
            exec.shutdownNow();
        }

        ThreadPoolExecutor dead = shutdownPool();
        try {
            AtomicInteger ran = new AtomicInteger();
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(ran::incrementAndGet, dead);
            check(ran.get() == 0, "CallerRunsPolicy drops the task when executor is shutdown");
        } finally {
            dead.shutdownNow();
        }
    }

    // ---- custom handler: callback identity + installation via setRejectedExecutionHandler ----
    static void testCustomHandler() {
        final Runnable[] got = new Runnable[1];
        final ThreadPoolExecutor[] from = new ThreadPoolExecutor[1];
        final AtomicInteger calls = new AtomicInteger();
        RejectedExecutionHandler custom = (r, e) -> {
            calls.incrementAndGet();
            got[0] = r;
            from[0] = e;
        };

        CountDownLatch hold = new CountDownLatch(1);
        ThreadPoolExecutor exec = saturatedPool(new CountDownLatch(1), hold);
        try {
            FixedTask queued = new FixedTask();
            exec.execute(queued);
            FixedTask rejected = new FixedTask();
            custom.rejectedExecution(rejected, exec);
            check(calls.get() == 1, "custom handler invoked exactly once");
            check(got[0] == rejected, "custom handler receives the exact rejected Runnable");
            check(from[0] == exec, "custom handler receives the exact executor");
            check(!exec.getQueue().contains(rejected), "custom handler did not queue the task");
        } finally {
            hold.countDown();
            exec.shutdownNow();
        }

        // installed handler intercepts execute() rejections instead of AbortPolicy
        CountDownLatch hold2 = new CountDownLatch(1);
        ThreadPoolExecutor exec2 = saturatedPool(new CountDownLatch(1), hold2);
        try {
            exec2.setRejectedExecutionHandler(custom);
            FixedTask queued = new FixedTask();
            exec2.execute(queued);
            int before = calls.get();
            exec2.execute(new FixedTask()); // saturated -> custom handler, no throw
            check(calls.get() == before + 1, "installed handler intercepts execute() rejection");
            check(exec2.getRejectedExecutionHandler() == custom, "getRejectedExecutionHandler returns it");
        } finally {
            hold2.countDown();
            exec2.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testRejectedExecutionHandler();
        testWhiteBoxStructure();
        testPolicyToStringAndDefault();
        testAbortPolicy();
        testDiscardPolicy();
        testDiscardOldestPolicy();
        testCallerRunsPolicy();
        testCustomHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
