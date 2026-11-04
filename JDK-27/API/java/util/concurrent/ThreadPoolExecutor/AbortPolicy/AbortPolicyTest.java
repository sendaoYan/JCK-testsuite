/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$AbortPolicy conformance: white-box structure + black-box behavior.
 * @run main AbortPolicyTest
 */

import java.util.concurrent.*;

public class AbortPolicyTest {
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
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$AbortPolicy");
        check(!c.isInterface(), "class");
        check(RejectedExecutionHandler.class.isAssignableFrom(c), "implements RejectedExecutionHandler");
        check(c.getConstructor() != null, "no-arg ctor");
        hasMethod(c, "rejectedExecution", Runnable.class, ThreadPoolExecutor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();
            boolean threw = false;
            try { policy.rejectedExecution(() -> { }, exec); }
            catch (RejectedExecutionException expected) { threw = true; }
            check(threw, "AbortPolicy throws RejectedExecutionException");
        } finally {
            exec.shutdown();
        }
    }

    // ---- 追加：契约深化 ----

    // 构造饱和执行器：1 工作线程阻塞 + 容量 1 的队列被占满
    static ThreadPoolExecutor saturatedPool(CountDownLatch started, CountDownLatch release) {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new ThreadPoolExecutor.AbortPolicy());
        tpe.execute(() -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { }
        });
        return tpe;
    }

    // 饱和执行器上 execute 被拒：REE 消息含 "rejected from" 且带执行器 toString 信息
    static void testSaturatedRejection() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ThreadPoolExecutor tpe = saturatedPool(started, release);
        try {
            check(started.await(5, TimeUnit.SECONDS), "工作线程已占满");
            tpe.execute(() -> { }); // 占满队列
            check(tpe.getQueue().size() == 1, "队列已满（容量 1）");
            boolean rejected = false;
            try {
                tpe.execute(() -> { });
                check(false, "饱和后 execute 应被 AbortPolicy 拒绝");
            } catch (RejectedExecutionException e) {
                rejected = e.getMessage() != null && e.getMessage().contains("rejected from")
                        && e.getMessage().contains("ThreadPoolExecutor");
            }
            check(rejected, "AbortPolicy 的 REE 消息含 \"rejected from\" 与执行器信息");
        } finally {
            release.countDown();
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 对已终止执行器直接调用 rejectedExecution 同样抛 REE
    static void testRejectedOnTerminated() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        boolean threw = false;
        try {
            new ThreadPoolExecutor.AbortPolicy().rejectedExecution(() -> { }, tpe);
        } catch (RejectedExecutionException expected) {
            threw = expected.getMessage() != null && expected.getMessage().contains("rejected from");
        }
        check(threw, "对已终止执行器调用 rejectedExecution 抛 REE 且消息含 \"rejected from\"");
    }

    static void testMisc() {
        ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();
        check(policy.toString() != null && policy.toString().length() > 0, "toString 非空");
        check(policy instanceof RejectedExecutionHandler,
              "AbortPolicy 实现 RejectedExecutionHandler（可独立 new 并作用于任意 TPE）");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSaturatedRejection();
        testRejectedOnTerminated();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
