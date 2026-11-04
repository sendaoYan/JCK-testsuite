/*
 * @test
 * @summary java.util.concurrent.ScheduledExecutorService API conformance: schedule a Callable.
 * @run main ScheduledExecutorServiceTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScheduledExecutorService() throws Exception {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        try {
            Future<Integer> f = ses.schedule(new Callable<Integer>() {
                public Integer call() { return 99; }
            }, 0, TimeUnit.MILLISECONDS);
            check(f.get() == 99, "scheduled result");
        } finally {
            ses.shutdown();
        }
    }

    // ---- 追加：契约深化（接口层；行为经 ScheduledThreadPoolExecutor 驱动） ----

    // 接口方法面探针
    static void testInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ScheduledExecutorService");
        check(c.isInterface(), "ScheduledExecutorService 是接口");
        boolean extendsExecutorService = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == Class.forName("java.util.concurrent.ExecutorService")) extendsExecutorService = true;
        }
        check(extendsExecutorService, "extends ExecutorService");
        String[] names = { "schedule", "schedule", "scheduleAtFixedRate", "scheduleWithFixedDelay" };
        Class<?>[][] params = {
            { Callable.class, long.class, TimeUnit.class },
            { Runnable.class, long.class, TimeUnit.class },
            { Runnable.class, long.class, long.class, TimeUnit.class },
            { Runnable.class, long.class, long.class, TimeUnit.class },
        };
        for (int i = 0; i < names.length; i++) {
            c.getMethod(names[i], params[i]);
            check(true, "方法面: " + names[i] + "/" + i);
        }
    }

    // ScheduledThreadPoolExecutor 可赋给接口；经接口引用完成 schedule 两变体
    static void testInterfaceReference() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        check(stpe instanceof ScheduledExecutorService, "ScheduledThreadPoolExecutor 实现 ScheduledExecutorService");
        ScheduledExecutorService ses = stpe;
        try {
            check(ses.schedule((Callable<Integer>) () -> 5, 0, TimeUnit.MILLISECONDS).get() == 5,
                  "经接口引用 schedule(Callable) 值精确");
            final boolean[] ran = new boolean[1];
            ScheduledFuture<?> rf = ses.schedule((Runnable) () -> ran[0] = true, 0, TimeUnit.MILLISECONDS);
            check(rf.get() == null && ran[0], "经接口引用 schedule(Runnable) 执行且 get null");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

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

    // 周期任务（fixedRate/fixedDelay）跑 N 次后取消
    static void testPeriodicViaInterface() throws Exception {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        try {
            CountDownLatch three = new CountDownLatch(3);
            ScheduledFuture<?> fr = ses.scheduleAtFixedRate(three::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(three.await(5, TimeUnit.SECONDS), "scheduleAtFixedRate 经接口引用跑满 3 次");
            check(fr.cancel(true), "取消 fixedRate 任务");
            CountDownLatch two = new CountDownLatch(2);
            ScheduledFuture<?> fd = ses.scheduleWithFixedDelay(two::countDown, 0, 50, TimeUnit.MILLISECONDS);
            check(two.await(5, TimeUnit.SECONDS), "scheduleWithFixedDelay 经接口引用跑满 2 次");
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
        } catch (RejectedExecutionException e) {
            ree = e.getMessage() != null && e.getMessage().contains("rejected from");
        }
        check(ree, "shutdown 后 schedule 抛 REE（消息含 \"rejected from\"）");
    }

    // 接口继承的方法经 ScheduledExecutorService 引用可用（submit/execute/invokeAll）
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
        testScheduledExecutorService();
        testInterfaceSurface();
        testInterfaceReference();
        testDelayedExecution();
        testPeriodicViaInterface();
        testEdgeSemantics();
        testInheritedMembers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
