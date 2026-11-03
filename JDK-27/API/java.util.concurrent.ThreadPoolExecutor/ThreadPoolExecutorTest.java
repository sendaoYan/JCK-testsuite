/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor conformance: white-box structure + black-box
 *          behavior (core/max/queue sizing, prestart, submit, rejection, termination).
 * @run main ThreadPoolExecutorTest
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor");
        check(!c.isInterface(), "ThreadPoolExecutor is a class");
        check(c.getSuperclass() == cls("java.util.concurrent.AbstractExecutorService"),
              "extends AbstractExecutorService");
        hasMethod(c, "execute", Runnable.class);
        hasMethod(c, "shutdown");
        hasMethod(c, "shutdownNow");
        hasMethod(c, "getCorePoolSize");
        hasMethod(c, "setCorePoolSize", int.class);
        hasMethod(c, "getMaximumPoolSize");
        hasMethod(c, "setMaximumPoolSize", int.class);
        hasMethod(c, "getPoolSize");
        hasMethod(c, "getActiveCount");
        hasMethod(c, "getTaskCount");
        hasMethod(c, "getCompletedTaskCount");
        hasMethod(c, "getQueue");
        hasMethod(c, "prestartCoreThread");
        hasMethod(c, "getLargestPoolSize");
        hasMethod(c, "setKeepAliveTime", long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSubmitAndTerminate() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            Future<Integer> f = tpe.submit(new Callable<Integer>() {
                public Integer call() { return 7; }
            });
            check(f.get() == 7, "submit result");
            check(tpe.getCorePoolSize() == 1 && tpe.getMaximumPoolSize() == 1, "core/max sizes");
        } finally {
            tpe.shutdown();
        }
        check(tpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
        check(tpe.isTerminated(), "isTerminated");
    }

    static void testCoreMaxQueueSemantics() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch twoStarted = new CountDownLatch(2);
        AtomicInteger started = new AtomicInteger();
        Runnable blocking = () -> {
            started.incrementAndGet();
            twoStarted.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        };

        // core=1, max=2, bounded queue capacity 1
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
                                                        new ArrayBlockingQueue<>(1));
        try {
            tpe.execute(blocking); // core worker 1 runs it
            tpe.execute(blocking); // queued (queue size 1)
            tpe.execute(blocking); // grows to max: worker 2 runs it
            check(twoStarted.await(10, TimeUnit.SECONDS), "two workers started");
            check(started.get() == 2, "two tasks running concurrently");
            check(tpe.getPoolSize() == 2, "pool grew to maximumPoolSize");
            check(tpe.getMaximumPoolSize() == 2, "getMaximumPoolSize");
            check(tpe.getQueue().size() == 1, "third task sits in the bounded queue");
            check(tpe.getTaskCount() == 3, "getTaskCount counts every submitted task");
            check(tpe.getActiveCount() == 2, "getActiveCount reflects running tasks");
            check(tpe.getLargestPoolSize() == 2, "getLargestPoolSize");
        } finally {
            release.countDown();
            tpe.shutdown();
        }
        check(tpe.awaitTermination(10, TimeUnit.SECONDS), "terminates after release");
        check(tpe.getCompletedTaskCount() == 3, "all three tasks completed");
    }

    static void testPrestartAndConfig() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            check(tpe.getCorePoolSize() == 3, "core = 3");
            check(tpe.getMaximumPoolSize() == 5, "max = 5");
            tpe.setMaximumPoolSize(6);
            check(tpe.getMaximumPoolSize() == 6, "setMaximumPoolSize");
            tpe.setKeepAliveTime(5, TimeUnit.SECONDS);
            check(tpe.getKeepAliveTime(TimeUnit.MILLISECONDS) == 5000L, "getKeepAliveTime converts units");
            check(tpe.prestartAllCoreThreads() == 3, "prestartAllCoreThreads starts 3");
            check(tpe.getPoolSize() == 3, "pool size 3 after prestart");
            tpe.setCorePoolSize(2);
            check(tpe.getCorePoolSize() == 2, "setCorePoolSize");
        } finally {
            tpe.shutdown();
        }
        check(tpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");

        // prestartCoreThread on a fresh executor with no tasks
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                       new LinkedBlockingQueue<Runnable>());
        try {
            check(p2.prestartCoreThread(), "prestartCoreThread starts one core thread");
            check(p2.getPoolSize() == 1, "one worker after prestartCoreThread");
        } finally {
            p2.shutdown();
        }
        check(p2.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    static void testRejection() {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        tpe.shutdown();
        try {
            tpe.execute(() -> { });
            check(false, "execute after shutdown should reject");
        } catch (RejectedExecutionException expected) {
            check(true, "execute after shutdown throws RejectedExecutionException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSubmitAndTerminate();
        testCoreMaxQueueSemantics();
        testPrestartAndConfig();
        testRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
