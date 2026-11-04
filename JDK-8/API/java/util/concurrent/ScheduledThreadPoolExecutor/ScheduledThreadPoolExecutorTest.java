/*
 * @test
 * @summary java.util.concurrent.ScheduledThreadPoolExecutor API conformance: submit a
 *          Callable and terminate.
 * @run main ScheduledThreadPoolExecutorTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** 断言异常类型精确 + 消息精确（实测为准）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": unexpected message: " + m);
        passed++;
    }

    static void testScheduledThreadPoolExecutor() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            Future<Integer> f = stpe.submit(new Callable<Integer>() {
                public Integer call() { return 5; }
            });
            check(f.get() == 5, "submit result");
        } finally {
            stpe.shutdown();
        }
        check(stpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    // ---- 追加：契约深化 ----

    // schedule(Runnable, delay) 到期执行（latch 有界，不断言精确时距）
    static void testScheduleRunnableDelay() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            ScheduledFuture<?> f = stpe.schedule(ran::countDown, 200, TimeUnit.MILLISECONDS);
            check(ran.await(5, TimeUnit.SECONDS), "schedule(Runnable, 200ms) 到期执行");
            check(f.get() == null, "Runnable 周期任务 get 返回 null");
            check(f.getDelay(TimeUnit.NANOSECONDS) <= 0, "已完成任务 getDelay <= 0");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // schedule(Callable, delay) 返回值 get 精确
    static void testScheduleCallableDelay() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            ScheduledFuture<Integer> f = stpe.schedule(() -> 77, 200, TimeUnit.MILLISECONDS);
            check(f.get() == 77, "schedule(Callable, 200ms) 值精确");
            check(f.isDone() && !f.isCancelled(), "完成后 done 且未取消");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // ScheduledFuture：未到期 getDelay>=0、cancel(false) 后 get → CancellationException、
    // getQueue 含未到期任务；setRemoveOnCancelPolicy 两态行为
    static void testScheduledFutureContract() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            ScheduledFuture<?> far = stpe.schedule(() -> "x", 10, TimeUnit.MINUTES);
            check(stpe.getQueue().contains(far), "getQueue 含未到期任务");
            check(far.getDelay(TimeUnit.MINUTES) >= 0, "未到期任务 getDelay >= 0");
            check(stpe.getQueue().size() >= 1, "getQueue 至少含 1 个任务");
            check(far.cancel(false), "cancel(false) 返回 true");
            check(far.isCancelled(), "cancel 后 isCancelled");
            boolean gotCE = false;
            try {
                far.get();
                check(false, "取消后 get 应抛 CancellationException");
            } catch (CancellationException e) {
                gotCE = true;
            }
            check(gotCE, "取消后 get 抛 CancellationException");
            // 默认 removeOnCancel=false：取消后仍滞留队列（到期时被丢弃）
            check(!stpe.getRemoveOnCancelPolicy(), "默认 removeOnCancelPolicy=false");
            check(stpe.getQueue().contains(far), "policy=false 下取消任务仍留在队列（实测记录）");
            // 打开 policy 后取消立即出队
            stpe.setRemoveOnCancelPolicy(true);
            check(stpe.getRemoveOnCancelPolicy(), "setRemoveOnCancelPolicy(true) 生效");
            ScheduledFuture<?> far2 = stpe.schedule(() -> "y", 10, TimeUnit.MINUTES);
            far2.cancel(false);
            check(!stpe.getQueue().contains(far2), "policy=true 下取消任务立即移出队列");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // scheduleAtFixedRate 计数 N 次后 cancel；再次 cancel 返回 false
    static void testFixedRateCountN() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            CountDownLatch three = new CountDownLatch(3);
            ScheduledFuture<?> f = stpe.scheduleAtFixedRate(three::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(three.await(5, TimeUnit.SECONDS), "fixedRate 跑满 3 次");
            check(f.cancel(true), "cancel(true) 成功");
            check(!f.cancel(true), "二次 cancel 返回 false");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 周期任务抛 RuntimeException → 后续执行静默停止（用仍在跑的对照任务证明调度器存活）
    static void testExceptionStopsPeriodic() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            final AtomicInteger bad = new AtomicInteger();
            CountDownLatch badOnce = new CountDownLatch(1);
            stpe.scheduleAtFixedRate(() -> {
                bad.incrementAndGet();
                badOnce.countDown();
                throw new IllegalStateException("periodic-boom");
            }, 0, 50, TimeUnit.MILLISECONDS);
            check(badOnce.await(5, TimeUnit.SECONDS), "异常周期任务已执行第一次");
            // 对照任务：证明调度器仍在工作
            CountDownLatch ctrl4 = new CountDownLatch(4);
            stpe.scheduleAtFixedRate(ctrl4::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(ctrl4.await(5, TimeUnit.SECONDS), "对照周期任务继续跑 4 次（调度器存活）");
            check(bad.get() == 1, "抛异常后周期任务静默停止（计数定格 1，对照任务已推进）");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // scheduleWithFixedDelay：执行次数可达 N 且任务串行（无重叠）
    static void testFixedDelaySerial() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            final AtomicBoolean inTask = new AtomicBoolean();
            final AtomicInteger overlaps = new AtomicInteger();
            CountDownLatch three = new CountDownLatch(3);
            ScheduledFuture<?> f = stpe.scheduleWithFixedDelay(() -> {
                if (!inTask.compareAndSet(false, true)) overlaps.incrementAndGet();
                try { /* 短任务体 */ } finally { inTask.set(false); }
                three.countDown();
            }, 0, 50, TimeUnit.MILLISECONDS);
            check(three.await(5, TimeUnit.SECONDS), "fixedDelay 跑满 3 次");
            check(overlaps.get() == 0, "fixedDelay 周期任务不重叠（串行）");
            check(f.cancel(true), "cancel fixedDelay 任务");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 负例：period/delay<=0 → IAE；unit/task null → NPE（消息实测两侧均为 null）
    static void testScheduleNegatives() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            try { stpe.scheduleAtFixedRate(() -> { }, 1, 0, TimeUnit.MILLISECONDS); check(false, "rate period=0"); }
            catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "scheduleAtFixedRate period=0 IAE"); }
            try { stpe.scheduleAtFixedRate(() -> { }, 1, -1, TimeUnit.MILLISECONDS); check(false, "rate period<0"); }
            catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "scheduleAtFixedRate period<0 IAE"); }
            try { stpe.scheduleWithFixedDelay(() -> { }, 1, 0, TimeUnit.MILLISECONDS); check(false, "delay=0"); }
            catch (IllegalArgumentException e) { checkExc(e, IllegalArgumentException.class, null, "scheduleWithFixedDelay delay<=0 IAE"); }
            try { stpe.schedule(() -> { }, 1, null); check(false, "unit null"); }
            catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "schedule unit null NPE"); }
            try { stpe.schedule((Runnable) null, 1, TimeUnit.MILLISECONDS); check(false, "task null"); }
            catch (NullPointerException e) { checkExc(e, NullPointerException.class, null, "schedule task null NPE"); }
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        // shutdown 后 schedule → REE（消息含 "rejected from"）
        boolean ree = false;
        try {
            stpe.schedule(() -> { }, 1, TimeUnit.MILLISECONDS);
            check(false, "shutdown 后 schedule");
        } catch (RejectedExecutionException e) {
            ree = e.getMessage() != null && e.getMessage().contains("rejected from");
        }
        check(ree, "shutdown 后 schedule 抛 REE（消息含 \"rejected from\"）");
    }

    // 负延迟立即执行
    static void testNegativeDelay() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            stpe.schedule(ran::countDown, -5, TimeUnit.MILLISECONDS);
            check(ran.await(5, TimeUnit.SECONDS), "负延迟任务立即执行");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // continueExistingPeriodicTasksAfterShutdownPolicy 默认 false：shutdown 后周期任务停止
    static void testPeriodicStopsAfterShutdown() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        check(!stpe.getContinueExistingPeriodicTasksAfterShutdownPolicy(),
              "默认 shutdown 后不继续周期任务");
        CountDownLatch two = new CountDownLatch(2);
        final AtomicInteger n = new AtomicInteger();
        stpe.scheduleAtFixedRate(() -> { n.incrementAndGet(); two.countDown(); }, 0, 50, TimeUnit.MILLISECONDS);
        check(two.await(5, TimeUnit.SECONDS), "周期任务先跑 2 次");
        stpe.shutdown();
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "shutdown 后终止");
        int frozen = n.get();
        check(true, "终止时刻计数 " + frozen);
        // 终止后线程全亡，任务永不可能再跑（用第二个执行器的延迟任务证明墙钟推进）
        ScheduledThreadPoolExecutor control = new ScheduledThreadPoolExecutor(1);
        try {
            control.schedule(() -> { }, 200, TimeUnit.MILLISECONDS).get(5, TimeUnit.SECONDS);
        } finally {
            control.shutdownNow();
        }
        check(control.awaitTermination(5, TimeUnit.SECONDS), "control terminates");
        check(n.get() == frozen, "终止后周期任务计数不再增长（默认策略 false）");
    }

    // executeExistingDelayedTasksAfterShutdownPolicy：默认 true（延迟任务保留）；false（取消并移除）
    static void testDelayedTasksAfterShutdownPolicy() throws Exception {
        ScheduledThreadPoolExecutor a = new ScheduledThreadPoolExecutor(1);
        check(a.getExecuteExistingDelayedTasksAfterShutdownPolicy(), "默认 executeExistingDelayedTasksAfterShutdownPolicy=true");
        ScheduledFuture<?> far = a.schedule(() -> "x", 10, TimeUnit.MINUTES);
        a.shutdown();
        check(!far.isCancelled(), "policy=true：shutdown 不取消已排程延迟任务");
        check(a.getQueue().contains(far), "policy=true：延迟任务仍留在队列");
        a.shutdownNow();
        check(a.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        check(!a.getQueue().contains(far), "shutdownNow 清空队列");

        ScheduledThreadPoolExecutor b = new ScheduledThreadPoolExecutor(1);
        b.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        check(!b.getExecuteExistingDelayedTasksAfterShutdownPolicy(), "set false 生效");
        ScheduledFuture<?> far2 = b.schedule(() -> "x", 10, TimeUnit.MINUTES);
        b.shutdown();
        check(b.awaitTermination(5, TimeUnit.SECONDS), "policy=false 下 shutdown 立即终止");
        check(far2.isCancelled(), "policy=false：shutdown 取消延迟任务");
        check(!b.getQueue().contains(far2), "policy=false：延迟任务被移出队列");
    }

    // execute/submit 即刻任务语义（0 延迟等价）
    static void testImmediateExecution() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            CountDownLatch e1 = new CountDownLatch(1);
            stpe.execute(e1::countDown);
            check(e1.await(5, TimeUnit.SECONDS), "execute 立即执行");
            CountDownLatch s1 = new CountDownLatch(1);
            stpe.submit(s1::countDown);
            check(s1.await(5, TimeUnit.SECONDS), "submit 立即执行");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 构造器族 + STPE(0) 合法 + STPE(-1) IAE（JDK-8 消息 null）
    static void testConstructors() throws Exception {
        check(new ScheduledThreadPoolExecutor(0) != null, "STPE(0) 合法");
        try {
            new ScheduledThreadPoolExecutor(-1);
            check(false, "STPE(-1) 应抛 IAE");
        } catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, null, "STPE(-1) IAE（JDK-8 消息 null）");
        }
        final AtomicInteger seq = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "stpe-jck-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        ThreadPoolExecutor.AbortPolicy handler = new ThreadPoolExecutor.AbortPolicy();
        try {
            ScheduledThreadPoolExecutor a = new ScheduledThreadPoolExecutor(1, tf);
            check(a.getThreadFactory() == tf, "ctor(int, ThreadFactory) getThreadFactory");
            ScheduledThreadPoolExecutor b = new ScheduledThreadPoolExecutor(1, handler);
            check(b.getRejectedExecutionHandler() == handler, "ctor(int, RejectedExecutionHandler)");
            ScheduledThreadPoolExecutor c = new ScheduledThreadPoolExecutor(1, tf, handler);
            check(c.getThreadFactory() == tf && c.getRejectedExecutionHandler() == handler,
                  "ctor(int, ThreadFactory, RejectedExecutionHandler)");
            // ThreadFactory 名字透传
            CountDownLatch done = new CountDownLatch(1);
            final String[] seen = new String[1];
            c.execute(() -> { seen[0] = Thread.currentThread().getName(); done.countDown(); });
            check(done.await(5, TimeUnit.SECONDS), "ctor tf 任务完成");
            check(seen[0] != null && seen[0].startsWith("stpe-jck-"), "自定义线程名透传: " + seen[0]);
            a.shutdownNow(); b.shutdownNow(); c.shutdownNow();
            a.awaitTermination(5, TimeUnit.SECONDS);
            b.awaitTermination(5, TimeUnit.SECONDS);
            c.awaitTermination(5, TimeUnit.SECONDS);
        } finally {
            // 已在 try 内关闭
        }
    }

    public static void main(String[] args) throws Exception {
        testScheduledThreadPoolExecutor();
        testScheduleRunnableDelay();
        testScheduleCallableDelay();
        testScheduledFutureContract();
        testFixedRateCountN();
        testExceptionStopsPeriodic();
        testFixedDelaySerial();
        testScheduleNegatives();
        testNegativeDelay();
        testPeriodicStopsAfterShutdown();
        testDelayedTasksAfterShutdownPolicy();
        testImmediateExecution();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
