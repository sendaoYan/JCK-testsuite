/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor.DiscardPolicy conformance: silently discards.
 * @run main DiscardPolicyTest
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscardPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDiscardPolicy() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            ThreadPoolExecutor.DiscardPolicy policy = new ThreadPoolExecutor.DiscardPolicy();
            policy.rejectedExecution(() -> { }, exec);
            check(true, "DiscardPolicy discards without throwing");
        } finally {
            exec.shutdown();
        }
    }

    // 构造饱和执行器（DiscardPolicy）
    static ThreadPoolExecutor saturatedPool(CountDownLatch started, CountDownLatch release) {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());
        tpe.execute(() -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { }
        });
        return tpe;
    }

    // 饱和执行器上 execute：正常返回、不抛异常，且被拒任务从未执行（终止后计数仍为 0）
    static void testSaturatedDiscard() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ThreadPoolExecutor tpe = saturatedPool(started, release);
        final AtomicInteger discardedCount = new AtomicInteger();
        try {
            check(started.await(5, TimeUnit.SECONDS), "工作线程已占满");
            tpe.execute(() -> { }); // 占满队列
            check(tpe.getQueue().size() == 1, "队列已满（容量 1）");
            tpe.execute(() -> discardedCount.incrementAndGet()); // 被拒 → 静默丢弃
            check(true, "DiscardPolicy 被拒时 execute 正常返回（无异常）");
            check(tpe.getQueue().size() == 1, "被拒任务未入队（队列长度不变）");
            release.countDown();
        } finally {
            release.countDown();
            tpe.shutdown();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check(discardedCount.get() == 0, "被丢弃任务从未执行（计数 0）");
    }

    // 对已 shutdown 的执行器：同样静默丢弃
    static void testSilentlySkipsOnShutdown() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        final boolean[] ran = new boolean[1];
        new ThreadPoolExecutor.DiscardPolicy().rejectedExecution(() -> ran[0] = true, tpe);
        check(!ran[0], "对已 shutdown 执行器任务不被运行（静默丢弃）");
    }

    static void testMisc() {
        ThreadPoolExecutor.DiscardPolicy policy = new ThreadPoolExecutor.DiscardPolicy();
        check(policy.toString() != null && policy.toString().length() > 0, "toString 非空");
        check(policy instanceof java.util.concurrent.RejectedExecutionHandler,
              "DiscardPolicy 实现 RejectedExecutionHandler（可独立 new 并作用于任意 TPE）");
    }

    public static void main(String[] args) throws Exception {
        testDiscardPolicy();
        testSaturatedDiscard();
        testSilentlySkipsOnShutdown();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
