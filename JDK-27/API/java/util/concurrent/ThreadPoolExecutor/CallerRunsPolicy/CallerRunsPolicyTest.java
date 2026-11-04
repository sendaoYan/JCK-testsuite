/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy conformance: white-box structure + black-box behavior.
 * @run main CallerRunsPolicyTest
 */

import java.util.concurrent.*;

public class CallerRunsPolicyTest {
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
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy");
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
            final boolean[] ran = new boolean[1];
            ThreadPoolExecutor.CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();
            policy.rejectedExecution(() -> { ran[0] = true; }, exec);
            check(ran[0], "CallerRunsPolicy runs the task in the caller thread");
        } finally {
            exec.shutdown();
        }
    }

    // ---- 追加：契约深化 ----

    // 构造饱和执行器（CallerRunsPolicy）
    static ThreadPoolExecutor saturatedPool(CountDownLatch started, CountDownLatch release) {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new ThreadPoolExecutor.CallerRunsPolicy());
        tpe.execute(() -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { }
        });
        return tpe;
    }

    // 饱和执行器上 execute：任务同步运行在调用线程（execute 返回前即完成，无需等待）
    static void testSaturatedCallerRuns() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ThreadPoolExecutor tpe = saturatedPool(started, release);
        try {
            check(started.await(5, TimeUnit.SECONDS), "工作线程已占满");
            tpe.execute(() -> { }); // 占满队列
            check(tpe.getQueue().size() == 1, "队列已满（容量 1）");
            final String mainName = Thread.currentThread().getName();
            final boolean[] ran = new boolean[1];
            final String[] runnerName = new String[1];
            tpe.execute(() -> {           // 被拒 → CallerRuns 在调用线程同步执行
                ran[0] = true;
                runnerName[0] = Thread.currentThread().getName();
            });
            check(ran[0], "被拒任务在 execute() 返回前已运行");
            check(mainName.equals(runnerName[0]), "被拒任务运行在调用线程（线程名 == main）: " + runnerName[0]);
        } finally {
            release.countDown();
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 对已 shutdown 的执行器：isShutdown 检查使任务被静默丢弃（不运行、不抛异常）
    static void testSilentlySkipsOnShutdown() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        final boolean[] ran = new boolean[1];
        new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(() -> ran[0] = true, tpe);
        check(!ran[0], "对已 shutdown 执行器任务不被运行（静默丢弃）");
    }

    static void testMisc() {
        ThreadPoolExecutor.CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();
        check(policy.toString() != null && policy.toString().length() > 0, "toString 非空");
        check(policy instanceof RejectedExecutionHandler,
              "CallerRunsPolicy 实现 RejectedExecutionHandler（可独立 new 并作用于任意 TPE）");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSaturatedCallerRuns();
        testSilentlySkipsOnShutdown();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
