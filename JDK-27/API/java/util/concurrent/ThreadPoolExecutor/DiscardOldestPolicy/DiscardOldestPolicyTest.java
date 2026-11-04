/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy conformance: white-box structure + black-box behavior.
 * @run main DiscardOldestPolicyTest
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscardOldestPolicyTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy");
        check(!c.isInterface(), "class");
        check(RejectedExecutionHandler.class.isAssignableFrom(c), "implements RejectedExecutionHandler");
        check(c.getConstructor() != null, "no-arg ctor");
        hasMethod(c, "rejectedExecution", Runnable.class, ThreadPoolExecutor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2));
        try {
            Runnable oldTask = () -> { };
            exec.getQueue().offer(oldTask);
            ThreadPoolExecutor.DiscardOldestPolicy policy = new ThreadPoolExecutor.DiscardOldestPolicy();
            policy.rejectedExecution(() -> { }, exec);
            check(!exec.getQueue().contains(oldTask), "oldest queued task discarded");
        } finally {
            exec.shutdown();
        }
    }

    // ---- 追加：契约深化 ----

    // 满队列场景：最老任务被丢弃、被拒任务（最新）入队并最终执行
    static void testSaturatedDiscardOldest() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger oldRan = new AtomicInteger();
        final AtomicInteger newestRan = new AtomicInteger();
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardOldestPolicy());
        try {
            tpe.execute(() -> {          // 占据唯一工作线程
                started.countDown();
                try { release.await(); } catch (InterruptedException e) { }
            });
            check(started.await(5, TimeUnit.SECONDS), "工作线程已占满");
            Runnable oldest = () -> oldRan.incrementAndGet();
            tpe.execute(oldest);          // 入队（最老）
            check(tpe.getQueue().size() == 1 && tpe.getQueue().contains(oldest), "最老任务已入队");
            Runnable newest = () -> newestRan.incrementAndGet();
            tpe.execute(newest);          // 被拒 → 丢弃 oldest → 重试 newest 入队
            check(!tpe.getQueue().contains(oldest), "最老任务已从队列移除");
            check(tpe.getQueue().contains(newest), "被拒任务（最新）已入队");
            check(tpe.getQueue().size() == 1, "队列长度仍为 1");
            release.countDown();
        } finally {
            release.countDown();
            tpe.shutdown();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check(oldRan.get() == 0, "被丢弃的最老任务从未执行");
        check(newestRan.get() == 1, "重试入队的最新任务恰执行一次");
    }

    // 空队列场景（预启动空闲工作线程后直接调用）：poll 到 null 后重试 execute → 任务实际执行
    static void testEmptyQueueRetry() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1));
        try {
            check(tpe.prestartCoreThread(), "预启动 core 工作线程");
            check(tpe.getQueue().isEmpty(), "队列为空");
            CountDownLatch ran = new CountDownLatch(1);
            new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(ran::countDown, tpe);
            check(ran.await(5, TimeUnit.SECONDS),
                  "空队列时被拒任务经重试 execute 后实际执行（poll 到 null oldest，实测记录行为）");
            check(tpe.getQueue().isEmpty(), "执行后队列为空");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 对已 shutdown 的执行器：isShutdown 检查 → 不丢弃、不重试、不抛异常
    static void testSilentlySkipsOnShutdown() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        final boolean[] ran = new boolean[1];
        new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(() -> ran[0] = true, tpe);
        check(!ran[0], "对已 shutdown 执行器任务不被运行（静默丢弃）");
    }

    static void testMisc() {
        ThreadPoolExecutor.DiscardOldestPolicy policy = new ThreadPoolExecutor.DiscardOldestPolicy();
        check(policy.toString() != null && policy.toString().length() > 0, "toString 非空");
        check(policy instanceof RejectedExecutionHandler,
              "DiscardOldestPolicy 实现 RejectedExecutionHandler（可独立 new 并作用于任意 TPE）");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSaturatedDiscardOldest();
        testEmptyQueueRetry();
        testSilentlySkipsOnShutdown();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
