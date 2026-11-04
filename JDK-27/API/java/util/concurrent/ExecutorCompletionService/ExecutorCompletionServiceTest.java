/*
 * @test
 * @summary java.util.concurrent.ExecutorCompletionService conformance: white-box structure + black-box behavior.
 * @run main ExecutorCompletionServiceTest
 */

import java.util.*;
import java.util.concurrent.*;

public class ExecutorCompletionServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    /** 断言异常类型精确 + 消息精确（JDK-27 实测携带参数名消息）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": expected message \"" + expectMsg + "\" but got: " + m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ExecutorCompletionService");
        check(!c.isInterface(), "class");
        check(CompletionService.class.isAssignableFrom(c), "implements CompletionService");
        check(c.getConstructor(Executor.class) != null, "ctor (Executor)");
        check(c.getConstructor(Executor.class, BlockingQueue.class) != null, "ctor (Executor, BlockingQueue)");
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "take");
        hasMethod(c, "poll");
        hasMethod(c, "poll", long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        try {
            cs.submit(() -> 9);
            check(cs.take().get() == 9, "take returns completed task");
            check(cs.poll() == null, "poll empty returns null");
            cs.submit(() -> { Thread.sleep(150); return 1; });
            cs.submit(() -> 2);
            check(cs.take().get() == 2, "take returns first completed (order by completion)");
            check(cs.take().get() == 1, "take returns remaining completed");
            cs.submit(() -> { }, 42);
            check(cs.take().get() == 42, "submit(Runnable, result) returns result");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "executor terminated");
    }

    // ---- 追加：契约深化 ----

    // take 返回完成序而非提交序（latch 精确控制完成顺序，替代 sleep 竞态）
    static void testTakeCompletionOrder() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        CountDownLatch t1Started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            cs.submit(() -> {
                t1Started.countDown();
                try { release.await(); } catch (InterruptedException e) { }
                return 1;
            });
            cs.submit(() -> {
                try { t1Started.await(); } catch (InterruptedException e) { }
                return 2;
            });
            Future<Integer> first = cs.take();
            check(first.get() == 2, "take 返回最先完成的任务（值 2），而非提交序首个");
            release.countDown();
            Future<Integer> second = cs.take();
            check(second.get() == 1, "第二个 take 返回后完成任务（值 1）");
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // poll：空 null；超时未命中 null；命中返回已完成 Future
    static void testPollSemantics() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        try {
            check(cs.poll() == null, "无完成结果时 poll() 返回 null");
            long t0 = System.nanoTime();
            check(cs.poll(200, TimeUnit.MILLISECONDS) == null, "超时未命中 poll(timeout) 返回 null");
            check(System.nanoTime() - t0 >= 150000000L, "poll(timeout) 确实等待了超时时长");
            cs.submit(() -> 42);
            Future<Integer> hit = cs.poll(5, TimeUnit.SECONDS);
            check(hit != null && hit.get() == 42, "poll(timeout) 命中已完成任务");
            check(cs.poll() == null, "取走后再 poll 为 null");
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // submit(Callable/Runnable,T)；失败任务经 take().get() 抛 ExecutionException
    static void testSubmitVariantsAndFailure() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        try {
            cs.submit(() -> { }, 42);
            check(cs.take().get() == 42, "submit(Runnable, T) 的 take 返回 T");
            final IllegalStateException boom = new IllegalStateException("ecs-boom");
            cs.submit(() -> { throw boom; });
            boolean gotEE = false;
            try {
                cs.take().get();
                check(false, "失败任务 get 应抛 ExecutionException");
            } catch (ExecutionException e) {
                gotEE = e.getCause() == boom;
            }
            check(gotEE, "失败任务经 take().get() 抛 ExecutionException 且 cause 原样");
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // take 被中断 → InterruptedException
    static void testTakeInterrupt() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        final ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        final boolean[] gotIE = new boolean[1];
        Thread taker = new Thread(() -> {
            try { cs.take(); }
            catch (InterruptedException e) { gotIE[0] = true; }
        }, "ecs-taker");
        taker.setDaemon(true);
        taker.start();
        taker.interrupt();
        taker.join(5000);
        check(gotIE[0], "take 响应中断抛 InterruptedException");
        pool.shutdownNow();
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 自定义完成队列注入：完成项进入我们提供的队列，可被直接消费
    static void testCustomQueueInjection() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        LinkedBlockingQueue<Future<Integer>> custom = new LinkedBlockingQueue<>();
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool, custom);
        try {
            cs.submit(() -> 77);
            Future<Integer> fromQueue = custom.poll(5, TimeUnit.SECONDS);
            check(fromQueue != null && fromQueue.get() == 77, "完成项进入注入的自定义队列且值精确");
            check(cs.poll() == null, "自定义队列被取走后 ECS.poll 为 null（同一队列）");
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 构造器负例：JDK-27 消息精确（executor/completionQueue/task）
    static void testNegativeCases() {
        try {
            new ExecutorCompletionService<Integer>(null);
            check(false, "ECS(null) 应抛 NPE");
        } catch (NullPointerException e) {
            checkExc(e, NullPointerException.class, "executor", "ECS(null executor)");
        }
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            new ExecutorCompletionService<Integer>(tpe, null);
            check(false, "ECS(ex, null) 应抛 NPE");
        } catch (NullPointerException e) {
            checkExc(e, NullPointerException.class, "completionQueue", "ECS(executor, null queue)");
        }
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(tpe);
        try {
            cs.submit((Callable<Integer>) null);
            check(false, "submit(null) 应抛 NPE");
        } catch (NullPointerException e) {
            checkExc(e, NullPointerException.class, "task", "ECS submit(null Callable)");
        }
        tpe.shutdown();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTakeCompletionOrder();
        testPollSemantics();
        testSubmitVariantsAndFailure();
        testTakeInterrupt();
        testCustomQueueInjection();
        testNegativeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
