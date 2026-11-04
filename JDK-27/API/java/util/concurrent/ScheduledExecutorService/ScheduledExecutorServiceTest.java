/*
 * @test
 * @summary java.util.concurrent.ScheduledExecutorService conformance: white-box structure +
 *          black-box behavior (schedule/scheduleAtFixedRate/scheduleWithFixedDelay).
 * @run main ScheduledExecutorServiceTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceTest {
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
        Class<?> c = cls("java.util.concurrent.ScheduledExecutorService");
        check(c.isInterface(), "ScheduledExecutorService is an interface");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1 && ifs[0] == ExecutorService.class, "extends ExecutorService");
        hasMethod(c, "schedule", Callable.class, long.class, TimeUnit.class);
        hasMethod(c, "schedule", Runnable.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleAtFixedRate", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleWithFixedDelay", Runnable.class, long.class, long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        try {
            // schedule(Callable, 0 delay) -> result
            ScheduledFuture<Integer> f = ses.schedule(new Callable<Integer>() {
                public Integer call() { return 99; }
            }, 0, TimeUnit.MILLISECONDS);
            check(f.get() == 99, "scheduled Callable result");
            check(f.isDone(), "scheduled future is done");

            // schedule(Runnable, 0 delay) -> null result once done
            final boolean[] ran = new boolean[1];
            ScheduledFuture<?> rf = ses.schedule((Runnable) () -> ran[0] = true, 0, TimeUnit.MILLISECONDS);
            check(rf.get() == null, "scheduled Runnable returns null");
            check(ran[0], "scheduled Runnable executed");

            // scheduleAtFixedRate: runs at least once, then cancel immediately
            CountDownLatch firstRun = new CountDownLatch(1);
            ScheduledFuture<?> fr = ses.scheduleAtFixedRate(firstRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(firstRun.await(10, TimeUnit.SECONDS), "scheduleAtFixedRate first run");
            check(fr.cancel(true), "scheduleAtFixedRate cancelled");

            // scheduleWithFixedDelay: runs at least once, then cancel immediately
            CountDownLatch delayedRun = new CountDownLatch(1);
            ScheduledFuture<?> fd = ses.scheduleWithFixedDelay(delayedRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(delayedRun.await(10, TimeUnit.SECONDS), "scheduleWithFixedDelay first run");
            check(fd.cancel(true), "scheduleWithFixedDelay cancelled");
        } finally {
            ses.shutdownNow();
        }
        check(ses.awaitTermination(10, TimeUnit.SECONDS), "terminates after shutdownNow");
    }

    // ---- 追加：契约深化（接口层） ----

    // 延迟任务到期执行（latch 有界，不断言精确时间）；未到期状态用远期任务确定性观察
    static void testDelayedExecution() throws Exception {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        try {
            ScheduledFuture<?> far = ses.schedule(() -> { }, 10, TimeUnit.MINUTES);
            check(!far.isDone() && !far.isCancelled(), "10 分钟延迟任务在观察点必未完成");
            check(far.getDelay(TimeUnit.SECONDS) > 0, "未到期任务 getDelay > 0");
            check(far.cancel(false), "取消远期任务");
            CountDownLatch ran = new CountDownLatch(1);
            ScheduledFuture<?> f = ses.schedule(ran::countDown, 200, TimeUnit.MILLISECONDS);
            check(ran.await(5, TimeUnit.SECONDS), "延迟任务到期执行");
            check(f.isDone(), "执行后 isDone");
        } finally {
            ses.shutdownNow();
        }
        check(ses.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 周期任务跑 N 次后取消
    static void testPeriodicCountN() throws Exception {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        try {
            CountDownLatch three = new CountDownLatch(3);
            ScheduledFuture<?> fr = ses.scheduleAtFixedRate(three::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(three.await(5, TimeUnit.SECONDS), "scheduleAtFixedRate 跑满 3 次");
            check(fr.cancel(true), "取消 fixedRate 任务");
            CountDownLatch two = new CountDownLatch(2);
            ScheduledFuture<?> fd = ses.scheduleWithFixedDelay(two::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(two.await(5, TimeUnit.SECONDS), "scheduleWithFixedDelay 跑满 2 次");
            check(fd.cancel(true), "取消 fixedDelay 任务");
        } finally {
            ses.shutdownNow();
        }
        check(ses.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 负延迟立即执行；shutdown 后 schedule 被拒
    static void testEdgeSemantics() throws Exception {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            ses.schedule(ran::countDown, -5, TimeUnit.MILLISECONDS);
            check(ran.await(5, TimeUnit.SECONDS), "负延迟任务立即执行");
        } finally {
            ses.shutdown();
        }
        check(ses.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        boolean ree = false;
        try {
            ses.schedule(() -> { }, 1, TimeUnit.MILLISECONDS);
            check(false, "shutdown 后 schedule 应被拒");
        } catch (java.util.concurrent.RejectedExecutionException e) {
            ree = e.getMessage() != null && e.getMessage().contains("rejected from");
        }
        check(ree, "shutdown 后 schedule 抛 REE（消息含 \"rejected from\"）");
    }

    // 接口继承方法经引用可用（submit/execute/invokeAll）
    static void testInheritedMembers() throws Exception {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        try {
            check(ses.submit(() -> 3).get() == 3, "submit 经接口引用");
            CountDownLatch ran = new CountDownLatch(1);
            ses.execute(ran::countDown);
            check(ran.await(5, TimeUnit.SECONDS), "execute 经接口引用（即刻任务语义）");
        } finally {
            ses.shutdownNow();
        }
        check(ses.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDelayedExecution();
        testPeriodicCountN();
        testEdgeSemantics();
        testInheritedMembers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
