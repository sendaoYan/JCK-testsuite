/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor conformance: white-box structure + black-box
 *          behavior (core/max/queue sizing, prestart, submit, rejection, termination).
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

    /** 断言异常类型精确 + 消息精确（JDK-27 实测携带描述性消息）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": expected message \"" + expectMsg + "\" but got: " + m);
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

    static class HookingTPE extends ThreadPoolExecutor {
        int beforeCount, afterCount, terminatedCount;
        HookingTPE(int core, int max, long keepAlive, TimeUnit unit,
                   java.util.concurrent.BlockingQueue<Runnable> workQueue) {
            super(core, max, keepAlive, unit, workQueue);
        }
        protected void beforeExecute(Thread t, Runnable r) { beforeCount++; }
        protected void afterExecute(Runnable r, Throwable t) { afterCount++; }
        protected void terminated() { terminatedCount++; }
    }

    static void testBlackBox() throws Exception {
        // constructor variants with ThreadFactory / RejectedExecutionHandler
        java.util.concurrent.ThreadFactory tf = r -> { Thread t = new Thread(r); t.setDaemon(true); return t; };
        java.util.concurrent.RejectedExecutionHandler reh =
                (r, e) -> { throw new RejectedExecutionException("rejected"); };
        ThreadPoolExecutor t1 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), tf);
        check(t1.getThreadFactory() == tf, "constructor(...,ThreadFactory) getThreadFactory");
        t1.shutdown();
        t1.awaitTermination(10, TimeUnit.SECONDS);

        ThreadPoolExecutor t2 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), reh);
        check(t2.getRejectedExecutionHandler() == reh, "constructor(...,RejectedExecutionHandler)");
        t2.shutdown();
        t2.awaitTermination(10, TimeUnit.SECONDS);

        ThreadPoolExecutor t3 = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1), tf, reh);
        check(t3.getThreadFactory() == tf && t3.getRejectedExecutionHandler() == reh,
              "constructor(...,ThreadFactory,RejectedExecutionHandler)");

        // setThreadFactory / setRejectedExecutionHandler
        java.util.concurrent.ThreadFactory tf2 = r -> { Thread t = new Thread(r); t.setDaemon(true); return t; };
        t3.setThreadFactory(tf2);
        check(t3.getThreadFactory() == tf2, "setThreadFactory");
        java.util.concurrent.RejectedExecutionHandler reh2 = (r, e) -> { };
        t3.setRejectedExecutionHandler(reh2);
        check(t3.getRejectedExecutionHandler() == reh2, "setRejectedExecutionHandler");

        // allowCoreThreadTimeOut / allowsCoreThreadTimeOut
        check(!t3.allowsCoreThreadTimeOut(), "allowsCoreThreadTimeOut default false");
        t3.allowCoreThreadTimeOut(true);
        check(t3.allowsCoreThreadTimeOut(), "allowCoreThreadTimeOut(true) sets true");

        // isShutdown / isTerminating / purge / remove
        check(!t3.isShutdown(), "not shutdown initially");
        check(!t3.isTerminating(), "not terminating initially");
        t3.purge();
        check(true, "purge callable");
        check(!t3.remove(() -> { }), "remove(non-queued) false");
        t3.shutdown();
        t3.awaitTermination(10, TimeUnit.SECONDS);

        // beforeExecute / afterExecute / terminated hooks
        HookingTPE h = new HookingTPE(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        h.execute(() -> { });
        h.shutdown();
        h.awaitTermination(10, TimeUnit.SECONDS);
        check(h.beforeCount == 1, "beforeExecute invoked once");
        check(h.afterCount == 1, "afterExecute invoked once");
        check(h.terminatedCount == 1, "terminated invoked once");
        check(!h.isTerminating(), "isTerminating false after termination");

        // toString
        check(h.toString() != null && h.toString().length() > 0, "toString non-null");
    }

    // ---- 追加：契约深化 ----

    // 构造器与 setter 负例矩阵（JDK-27 实测消息精确断言；JDK-8 为 null 消息，版本差异）
    static void testConstructorMatrix() throws Exception {
        java.util.concurrent.BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
        ThreadFactory tf = r -> { Thread t = new Thread(r); t.setDaemon(true); return t; };
        java.util.concurrent.RejectedExecutionHandler h = new ThreadPoolExecutor.AbortPolicy();

        try { new ThreadPoolExecutor(-1, 1, 0L, TimeUnit.MILLISECONDS, q); check(false, "core<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "corePoolSize must be non-negative", "corePoolSize<0 IAE"); }
        try { new ThreadPoolExecutor(1, 0, 0L, TimeUnit.MILLISECONDS, q); check(false, "max<=0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "maximumPoolSize must be positive", "maximumPoolSize<=0 IAE"); }
        try { new ThreadPoolExecutor(2, 1, 0L, TimeUnit.MILLISECONDS, q); check(false, "max<core"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "maximumPoolSize must be greater than or equal to corePoolSize", "maximumPoolSize<corePoolSize IAE"); }
        try { new ThreadPoolExecutor(1, 1, -1L, TimeUnit.MILLISECONDS, q); check(false, "keepAlive<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "keepAliveTime must be non-negative", "keepAliveTime<0 IAE"); }
        try { new ThreadPoolExecutor(0, 0, 0L, TimeUnit.MILLISECONDS, q); check(false, "0,0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "maximumPoolSize must be positive", "core=max=0 IAE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, null, q); check(false, "unit null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "unit", "unit null NPE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, null); check(false, "queue null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "workQueue", "workQueue null NPE"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q, (ThreadFactory) null); check(false, "tf null 6"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "threadFactory", "threadFactory null NPE(6参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q, (java.util.concurrent.RejectedExecutionHandler) null); check(false, "handler null 6"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "handler", "handler null NPE(6参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q, null, h); check(false, "tf null 7"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "threadFactory", "threadFactory null NPE(7参)"); }
        try { new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q, tf, null); check(false, "handler null 7"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "handler", "handler null NPE(7参)"); }

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q);
        try { tpe.setCorePoolSize(-1); check(false, "setCore<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "corePoolSize must be non-negative", "setCorePoolSize(-1) IAE"); }
        // JDK 9+ 版本差异：setCorePoolSize 高于 maximumPoolSize 抛 IAE（JDK-8 静默接受）
        try {
            tpe.setCorePoolSize(3);
            check(false, "setCore>max");
        } catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, "corePoolSize must be less than or equal to maximumPoolSize",
                     "setCorePoolSize(3)>max=1 IAE（JDK-8 静默接受，版本差异）");
        }
        try { tpe.setMaximumPoolSize(0); check(false, "setMax=0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "maximumPoolSize must be positive", "setMaximumPoolSize(0) IAE"); }
        try { tpe.setKeepAliveTime(-1L, TimeUnit.MILLISECONDS); check(false, "setKeepAlive<0"); }
        catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, "time must be non-negative", "setKeepAliveTime(-1) IAE"); }
        try { tpe.setKeepAliveTime(1L, null); check(false, "setKeepAlive unit null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "unit", "setKeepAliveTime unit null NPE"); }
        try { tpe.setThreadFactory(null); check(false, "setTF null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "threadFactory", "setThreadFactory(null) NPE"); }
        try { tpe.setRejectedExecutionHandler(null); check(false, "setHandler null"); }
        catch (NullPointerException e) { checkExc(e, NullPointerException.class, "handler", "setRejectedExecutionHandler(null) NPE"); }
        try { tpe.allowCoreThreadTimeOut(true); check(false, "allowCore keepAlive=0"); }
        catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, "Core threads must have nonzero keep alive times",
                     "allowCoreThreadTimeOut(true) 且 keepAlive=0 IAE（两侧消息一致）");
        }
        // core=0 合法
        tpe.setCorePoolSize(0);
        check(tpe.getCorePoolSize() == 0, "setCorePoolSize(0) 合法");
        tpe.setCorePoolSize(1);
        tpe.shutdown();
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "matrix executor terminates");
    }

    static void await5s(CountDownLatch l) {
        try { if (!l.await(5, TimeUnit.SECONDS)) throw new AssertionError("latch 等待超时"); }
        catch (InterruptedException e) { /* shutdownNow 中断属预期 */ }
    }

    // 核心流程状态机：core → queue → 非core → 拒绝（latch 逐步精确观察）
    static void testTaskFlowStateMachine() throws Exception {
        CountDownLatch t1Started = new CountDownLatch(1);
        CountDownLatch t3Started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger ran = new AtomicInteger();
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
                                                        new ArrayBlockingQueue<>(1));
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
            check(tpe.getActiveCount() == 2, "getActiveCount==2");
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

    // shutdown 生命周期与拒绝（isTerminating 中间态确定性观察）
    static void testShutdownLifecycle() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        check(!tpe.isShutdown() && !tpe.isTerminated() && !tpe.isTerminating(), "新建执行器三态均 false");
        check(!tpe.awaitTermination(200, TimeUnit.MILLISECONDS), "未 shutdown 时 awaitTermination 返回 false");

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

    // prestart：core=1 时首次 true 再次 false；prestartAllCoreThreads 补足差额
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
        ProbingTPE() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                // 抑制 execute() 原始异常走线程未捕获处理器时的 stderr 噪声
                t.setUncaughtExceptionHandler((thr, ex) -> { });
                return t;
            });
        }
        protected void beforeExecute(Thread t, Runnable r) {
            synchronized (events) { events.add("before"); }
            beforeThread = t; beforeTask = r;
        }
        protected void afterExecute(Runnable r, Throwable t) {
            synchronized (events) { events.add("after"); }
            synchronized (afterPairs) { afterPairs.add(new Object[]{r, t}); }
            afterTwo.countDown();
        }
        protected void terminated() { }
    }

    static void testHookDetails() throws Exception {
        ProbingTPE tpe = new ProbingTPE();
        final RuntimeException boom = new RuntimeException("hook-boom");
        final Thread[] taskThread = new Thread[1];
        CountDownLatch normalDone = new CountDownLatch(1);
        CountDownLatch throwDone = new CountDownLatch(1);
        Runnable normal = () -> { taskThread[0] = Thread.currentThread(); normalDone.countDown(); };
        Runnable throwing = () -> { throwDone.countDown(); throw boom; };
        try {
            tpe.execute(normal);
            check(normalDone.await(5, TimeUnit.SECONDS), "正常任务完成");
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
        testWhiteBox();
        testSubmitAndTerminate();
        testCoreMaxQueueSemantics();
        testPrestartAndConfig();
        testRejection();
        testBlackBox();
        testConstructorMatrix();
        testTaskFlowStateMachine();
        testShutdownLifecycle();
        testShutdownNowSemantics();
        testAwaitTerminationInterrupt();
        testPrestartDetails();
        testRemoveAndPurge();
        testFutureAfterShutdown();
        testKeepAliveAndCoreTimeout();
        testHookDetails();
        testToStringStates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
