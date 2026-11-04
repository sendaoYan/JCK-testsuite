/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor API conformance: submit a Callable and
 *          terminate.
 * @run main ThreadPoolExecutorTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** 断言异常类型精确 + 消息精确（JDK-8 实测 TPE 参数负例消息均为 null）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": unexpected message: " + m);
        passed++;
    }

    static void testThreadPoolExecutor() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        Future<Integer> f = tpe.submit(new Callable<Integer>() {
            public Integer call() { return 7; }
        });
        check(f.get() == 7, "submit result");
        tpe.shutdown();
        check(tpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    // 构造器与 setter 负例矩阵（类型+消息实测后精确断言）
    static void testConstructorMatrix() throws Exception {
        BlockingQueueHolder q = new BlockingQueueHolder();
        ThreadFactory tf = r -> { Thread t = new Thread(r); t.setDaemon(true); return t; };
        java.util.concurrent.RejectedExecutionHandler h = new ThreadPoolExecutor.AbortPolicy();

        try { new ThreadPoolExecutor(-1, 1, 0L, TimeUnit.MILLISECONDS, q.q); check(false, "core<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "corePoolSize<0 IAE"); }
        try { new ThreadPoolExecutor(1, 0, 0L, TimeUnit.MILLISECONDS, q.q); check(false, "max<=0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "maximumPoolSize<=0 IAE"); }
        try { new ThreadPoolExecutor(2, 1, 0L, TimeUnit.MILLISECONDS, q.q); check(false, "max<core"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "maximumPoolSize<corePoolSize IAE"); }
        try { new ThreadPoolExecutor(1, 1, -1L, TimeUnit.MILLISECONDS, q.q); check(false, "keepAlive<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "keepAliveTime<0 IAE"); }
        try { new ThreadPoolExecutor(0, 0, 0L, TimeUnit.MILLISECONDS, q.q); check(false, "0,0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "core=max=0 IAE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, null, q.q); check(false, "unit null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "unit null NPE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, null); check(false, "queue null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "workQueue null NPE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q.q, (ThreadFactory) null); check(false, "tf null 6"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "threadFactory null NPE(6参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q.q, (java.util.concurrent.RejectedExecutionHandler) null); check(false, "handler null 6"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "handler null NPE(6参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q.q, null, h); check(false, "tf null 7"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "threadFactory null NPE(7参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q.q, tf, null); check(false, "handler null 7"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "handler null NPE(7参)"); }

        // setter 负例
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q.q);
        try { tpe.setCorePoolSize(-1); check(false, "setCore<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "setCorePoolSize(-1) IAE"); }
        try { tpe.setMaximumPoolSize(0); check(false, "setMax=0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "setMaximumPoolSize(0) IAE"); }
        try { tpe.setKeepAliveTime(-1L, TimeUnit.MILLISECONDS); check(false, "setKeepAlive<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "setKeepAliveTime(-1) IAE"); }
        try { tpe.setKeepAliveTime(1L, null); check(false, "setKeepAlive unit null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "setKeepAliveTime unit null NPE"); }
        try { tpe.setThreadFactory(null); check(false, "setTF null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "setThreadFactory(null) NPE"); }
        try { tpe.setRejectedExecutionHandler(null); check(false, "setHandler null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "setRejectedExecutionHandler(null) NPE"); }
        // keepAlive==0 时 allowCoreThreadTimeOut(true) → IAE（两侧消息一致的罕见负例）
        try { tpe.allowCoreThreadTimeOut(true); check(false, "allowCore keeAlive=0"); }
        catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, "Core threads must have nonzero keep alive times",
                     "allowCoreThreadTimeOut(true) 且 keepAlive=0 IAE");
        }
        // JDK-8 版本差异：setCorePoolSize 高于 maximumPoolSize 被静默接受（JDK 9+ 抛 IAE）
        tpe.setCorePoolSize(3);
        check(tpe.getCorePoolSize() == 3, "JDK-8 接受 setCorePoolSize>maximumPoolSize（版本差异记录）");
        tpe.setCorePoolSize(1);
        check(tpe.getCorePoolSize() == 1, "setCorePoolSize 恢复");
        // core=0 合法
        tpe.setCorePoolSize(0);
        check(tpe.getCorePoolSize() == 0, "setCorePoolSize(0) 合法");
        tpe.setCorePoolSize(1);
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "matrix executor terminates");
    }

    private static class BlockingQueueHolder {
        final java.util.concurrent.BlockingQueue<Runnable> q = new LinkedBlockingQueue<Runnable>();
    }

    // 核心流程状态机：core → queue → 非core → 拒绝（latch 精确观察）
    static void testTaskFlowStateMachine() throws Exception {
        CountDownLatch t1Started = new CountDownLatch(1);
        CountDownLatch t3Started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger ran = new AtomicInteger();
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
                                                        new ArrayBlockingQueue<Runnable>(1));
        try {
            Runnable t1 = () -> { t1Started.countDown(); ran.incrementAndGet(); await5s(release); };
            Runnable t2 = () -> ran.incrementAndGet();
            Runnable t3 = () -> { t3Started.countDown(); ran.incrementAndGet(); await5s(release); };
            Runnable t4 = () -> ran.incrementAndGet();

            tpe.execute(t1);
            check(t1Started.await(5, TimeUnit.SECONDS), "任务1 占据 core 工作线程");
            tpe.execute(t2);
            check(tpe.getQueue().size() == 1 && tpe.getQueue().contains(t2), "任务2 入队（队列容量 1 占满）");
            tpe.execute(t3);
            check(t3Started.await(5, TimeUnit.SECONDS), "任务3 触发第二个（非 core）工作线程");
            check(tpe.getPoolSize() == 2, "getPoolSize==2（达 maximumPoolSize）");
            check(tpe.getActiveCount() == 2, "getActiveCount==2（两任务均在运行）");
            check(tpe.getLargestPoolSize() == 2 && tpe.getLargestPoolSize() <= tpe.getMaximumPoolSize(),
                  "getLargestPoolSize==2 且不超过 max");
            check(tpe.getCorePoolSize() == 1 && tpe.getMaximumPoolSize() == 2, "core/max 配置不变");
            check(tpe.getQueue().size() == 1, "队列中恰有任务2");
            check(tpe.getTaskCount() == 3, "getTaskCount 计入 3 个被接受任务");
            boolean rejected = false;
            try {
                tpe.execute(t4);
            } catch (RejectedExecutionException e) {
                rejected = e.getMessage() != null && e.getMessage().contains("rejected from");
            }
            check(rejected, "core+队列+max 耗尽后第 4 任务被拒（消息含 \"rejected from\"）");
            release.countDown();
        } finally {
            release.countDown();
            tpe.shutdown();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "flow executor terminates");
        check(ran.get() == 3, "任务 1/2/3 执行、被拒任务 4 从未执行");
        check(tpe.getCompletedTaskCount() == 3, "终止后 getCompletedTaskCount==3");
    }

    static void await5s(CountDownLatch l) {
        try { if (!l.await(5, TimeUnit.SECONDS)) throw new AssertionError("latch 等待超时"); }
        catch (InterruptedException e) { /* shutdownNow 中断属预期 */ }
    }

    // shutdown 生命周期与拒绝（含 isTerminating 中间态的确定性观察）
    static void testShutdownLifecycle() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        check(!tpe.isShutdown() && !tpe.isTerminated() && !tpe.isTerminating(), "新建执行器三态均 false");
        check(!tpe.awaitTermination(200, TimeUnit.MILLISECONDS), "未 shutdown 时 awaitTermination 返回 false");

        // 阻塞任务使 shutdown→termination 之间可确定观察 isTerminating
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch rel = new CountDownLatch(1);
        tpe.execute(() -> { started.countDown(); await5s(rel); });
        check(started.await(5, TimeUnit.SECONDS), "任务已启动");
        tpe.shutdown();
        check(tpe.isShutdown(), "shutdown 后 isShutdown");
        check(!tpe.isTerminated(), "任务未完时 isTerminated false");
        check(tpe.isTerminating(), "shutdown 后、终止前 isTerminating true");
        try {
            tpe.execute(() -> { });
            check(false, "execute after shutdown");
        } catch (RejectedExecutionException e) {
            check(e.getMessage() != null && e.getMessage().contains("rejected from"),
                  "shutdown 后 execute 抛 REE（消息含 \"rejected from\"）");
        }
        try {
            tpe.submit(() -> { });
            check(false, "submit after shutdown");
        } catch (RejectedExecutionException e) {
            check(e.getMessage() != null && e.getMessage().contains("rejected from"),
                  "shutdown 后 submit 抛 REE（消息含 \"rejected from\"）");
        }
        tpe.shutdown();
        check(tpe.isShutdown(), "shutdown 幂等");
        rel.countDown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "awaitTermination true");
        check(tpe.isTerminated(), "终止后 isTerminated true");
        check(!tpe.isTerminating(), "终止后 isTerminating false");
        check(tpe.awaitTermination(0, TimeUnit.MILLISECONDS), "已终止时 awaitTermination(0) 立即 true");
        check(tpe.shutdownNow().isEmpty(), "已终止后 shutdownNow 返回空列表");
    }

    // shutdownNow：返回滞留队列任务 + 中断运行中任务
    static void testShutdownNowSemantics() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        final AtomicInteger ranQueued = new AtomicInteger();
        Runnable running = () -> {
            started.countDown();
            try { Thread.sleep(10000); }
            catch (InterruptedException e) { interrupted.countDown(); }
        };
        Runnable markerA = () -> ranQueued.incrementAndGet();
        Runnable markerB = () -> ranQueued.incrementAndGet();
        tpe.execute(running);
        check(started.await(5, TimeUnit.SECONDS), "运行中任务已启动");
        tpe.execute(markerA);
        tpe.execute(markerB);
        check(tpe.getQueue().size() == 2, "两个任务滞留队列");
        List<Runnable> pending = tpe.shutdownNow();
        try {
            check(pending.size() == 2, "shutdownNow 返回 2 个未执行任务");
            check(pending.contains(markerA) && pending.contains(markerB), "返回的恰是滞留队列中的标记任务");
            check(interrupted.await(5, TimeUnit.SECONDS), "运行中任务被 shutdownNow 中断");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "shutdownNow 后终止");
        check(ranQueued.get() == 0, "滞留任务从不再执行");
        check(tpe.shutdownNow().isEmpty(), "再次 shutdownNow 返回空");
    }

    // awaitTermination 被中断 → InterruptedException
    static void testAwaitTerminationInterrupt() throws Exception {
        final ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                              new LinkedBlockingQueue<Runnable>());
        final boolean[] gotIE = new boolean[1];
        Thread waiter = new Thread(() -> {
            try { tpe.awaitTermination(30, TimeUnit.SECONDS); }
            catch (InterruptedException e) { gotIE[0] = true; }
        }, "tpe-await-interrupt");
        waiter.setDaemon(true);
        waiter.start();
        waiter.interrupt();
        waiter.join(5000);
        check(gotIE[0], "awaitTermination 响应中断抛 InterruptedException");
        tpe.shutdownNow();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // prestart：core=1 时首次 true 再次 false；core=3 时 prestartAllCoreThreads 补足差额
    static void testPrestartDetails() throws Exception {
        ThreadPoolExecutor one = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            check(one.prestartCoreThread(), "core=1 首次 prestartCoreThread 返回 true");
            check(one.getPoolSize() == 1, "预启动 1 线程");
            check(!one.prestartCoreThread(), "workerCount==core 后 prestartCoreThread 返回 false");
            // 注：刚预启动的 Worker 在进入取任务循环前 getActiveCount 可能短暂为 1，非确定契约，不断言
        } finally {
            one.shutdownNow();
        }
        check(one.awaitTermination(5, TimeUnit.SECONDS), "terminates");

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            check(tpe.prestartCoreThread(), "core=3 首次 prestartCoreThread 返回 true（1<3）");
            check(tpe.prestartAllCoreThreads() == 2, "prestartAllCoreThreads 补足剩余 2");
            check(tpe.getPoolSize() == 3, "池内共 3 线程");
            check(tpe.prestartAllCoreThreads() == 0, "再次 prestartAllCoreThreads 返回 0");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // remove/purge：从队列移除已提交任务
    static void testRemoveAndPurge() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch rel = new CountDownLatch(1);
        final AtomicInteger ranA = new AtomicInteger(), ranB = new AtomicInteger();
        Runnable running = () -> { started.countDown(); await5s(rel); };
        Runnable queuedA = () -> ranA.incrementAndGet();
        Runnable queuedB = () -> ranB.incrementAndGet();
        tpe.execute(running);
        check(started.await(5, TimeUnit.SECONDS), "运行中任务已启动");
        tpe.execute(queuedA);
        tpe.execute(queuedB);
        check(tpe.getQueue().size() == 2, "两任务入队");
        check(tpe.remove(queuedA), "remove 移除在队任务返回 true");
        check(!tpe.getQueue().contains(queuedA), "队列不再含被移除任务");
        check(tpe.getQueue().size() == 1, "队列剩余 1");
        check(!tpe.remove(queuedA), "再次 remove 同一任务返回 false");
        check(!tpe.remove(() -> { }), "remove 从未提交任务返回 false");
        tpe.purge();
        check(true, "purge 可调用");
        rel.countDown();
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check(ranA.get() == 0, "被移除任务从未执行");
        check(ranB.get() == 1, "仍在队任务照常执行");
    }

    // shutdown 前提交的任务其 Future 仍可 get
    static void testFutureAfterShutdown() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        CountDownLatch done = new CountDownLatch(1);
        Future<String> f = tpe.submit(() -> { done.countDown(); return "kept"; });
        check(done.await(5, TimeUnit.SECONDS), "任务完成");
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check("kept".equals(f.get()), "shutdown 前提交的 Future.get 仍返回值");
        check(f.isDone() && !f.isCancelled(), "Future 状态 done 且未取消");
    }

    // keepAliveTime 换算 + allowCoreThreadTimeOut 开关
    static void testKeepAliveAndCoreTimeout() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            check(tpe.getKeepAliveTime(TimeUnit.MILLISECONDS) == 0, "默认 keepAlive 0");
            tpe.setKeepAliveTime(250, TimeUnit.MILLISECONDS);
            check(tpe.getKeepAliveTime(TimeUnit.MILLISECONDS) == 250, "set/get keepAlive 同单位");
            check(tpe.getKeepAliveTime(TimeUnit.MICROSECONDS) == 250000, "keepAlive 换算到 µs");
            check(tpe.getKeepAliveTime(TimeUnit.SECONDS) == 0, "keepAlive 换算截断（250ms→0s）");
            check(!tpe.allowsCoreThreadTimeOut(), "allowsCoreThreadTimeOut 默认 false");
            tpe.allowCoreThreadTimeOut(true);
            check(tpe.allowsCoreThreadTimeOut(), "allowCoreThreadTimeOut(true) 生效");
            tpe.allowCoreThreadTimeOut(false);
            check(!tpe.allowsCoreThreadTimeOut(), "allowCoreThreadTimeOut(false) 复位");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // beforeExecute/afterExecute/terminated 钩子：调用次序、线程同一性、异常透传
    static class ProbingTPE extends ThreadPoolExecutor {
        final List<String> events = new ArrayList<>();
        final List<Object[]> afterPairs = new ArrayList<>(); // {runnable, throwable}
        final CountDownLatch afterTwo = new CountDownLatch(2);
        volatile Thread beforeThread;
        volatile Runnable beforeTask;
        AtomicInteger beforeCount = new AtomicInteger();
        AtomicInteger afterCount = new AtomicInteger();
        AtomicInteger terminatedCount = new AtomicInteger();
        ProbingTPE() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                // 抑制 execute() 原始异常走线程未捕获处理器时的 stderr 噪声（契约本身由 afterExecute 断言）
                t.setUncaughtExceptionHandler((thr, ex) -> { });
                return t;
            });
        }
        protected void beforeExecute(Thread t, Runnable r) {
            synchronized (events) { events.add("before"); }
            beforeThread = t; beforeTask = r; beforeCount.incrementAndGet();
        }
        protected void afterExecute(Runnable r, Throwable t) {
            synchronized (events) { events.add("after"); }
            synchronized (afterPairs) { afterPairs.add(new Object[]{r, t}); }
            afterCount.incrementAndGet();
            afterTwo.countDown();
        }
        protected void terminated() { terminatedCount.incrementAndGet(); }
    }

    static void testHooks() throws Exception {
        ProbingTPE tpe = new ProbingTPE();
        final RuntimeException boom = new RuntimeException("hook-boom");
        final Thread[] taskThread = new Thread[1];
        CountDownLatch normalDone = new CountDownLatch(1);
        CountDownLatch throwDone = new CountDownLatch(1);
        Runnable normal = () -> {
            taskThread[0] = Thread.currentThread();
            normalDone.countDown();
        };
        Runnable throwing = () -> { throwDone.countDown(); throw boom; };
        try {
            tpe.execute(normal);
            check(normalDone.await(5, TimeUnit.SECONDS), "正常任务完成");
            // beforeExecute 先于任务体运行，此时钩子必然已回调
            check(tpe.beforeThread == taskThread[0], "beforeExecute 收到的线程即执行任务的线程");
            check(tpe.beforeTask == normal, "beforeExecute 收到原始 Runnable 对象");
            tpe.execute(throwing);
            check(throwDone.await(5, TimeUnit.SECONDS), "抛异常任务执行");
            check(tpe.afterTwo.await(5, TimeUnit.SECONDS), "两个 afterExecute 均已回调");
        } finally {
            tpe.shutdown();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        synchronized (tpe.events) {
            check(tpe.events.size() == 4, "两任务产生 4 个钩子事件");
            check("before".equals(tpe.events.get(0)) && "after".equals(tpe.events.get(1))
                  && "before".equals(tpe.events.get(2)) && "after".equals(tpe.events.get(3)),
                  "单线程池内钩子严格 before→after 成对交替");
        }
        synchronized (tpe.afterPairs) {
            check(tpe.afterPairs.size() == 2, "afterExecute 记录 2 对");
            check(tpe.afterPairs.get(0)[0] == normal && tpe.afterPairs.get(0)[1] == null,
                  "正常完成任务 afterExecute(r, null)");
            check(tpe.afterPairs.get(1)[0] == throwing && tpe.afterPairs.get(1)[1] == boom,
                  "execute 抛出的异常实例原样传给 afterExecute");
        }
        check(tpe.beforeCount.get() == 2 && tpe.afterCount.get() == 2, "before/after 各 2 次");
        check(tpe.terminatedCount.get() == 1, "terminated 恰调用 1 次");
        check(tpe.toString().contains("Terminated"), "toString 终止后含 [Terminated");
    }

    static void testToStringStates() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        check(tpe.toString().contains("Running"), "toString 运行态含 [Running");
        check(tpe.toString().contains("pool size"), "toString 含 pool size 字段");
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check(tpe.toString().contains("Terminated"), "toString 终止态含 [Terminated");
    }

    public static void main(String[] args) throws Exception {
        testThreadPoolExecutor();
        testConstructorMatrix();
        testTaskFlowStateMachine();
        testShutdownLifecycle();
        testShutdownNowSemantics();
        testAwaitTerminationInterrupt();
        testPrestartDetails();
        testRemoveAndPurge();
        testFutureAfterShutdown();
        testKeepAliveAndCoreTimeout();
        testHooks();
        testToStringStates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
