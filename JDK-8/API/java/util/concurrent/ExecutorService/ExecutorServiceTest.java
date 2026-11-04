/*
 * @test
 * @summary java.util.concurrent.ExecutorService API conformance: submit/get a Callable and
 *          shutdown/isShutdown.
 * @run main ExecutorServiceTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutorService() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> f = exec.submit(() -> 42);
        check(f.get() == 42, "submit/get");
        exec.shutdown();
        check(exec.isShutdown(), "isShutdown");
    }

    // ---- 追加：契约深化 ----

    // 接口方法面探针（JDK-8：仅扩展 Executor，无 close()——JDK 19+ 才有 AutoCloseable）
    static void testInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ExecutorService");
        check(c.isInterface(), "ExecutorService 是接口");
        boolean extendsExecutor = false, extendsAutoCloseable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == Executor.class) extendsExecutor = true;
            if (i == AutoCloseable.class) extendsAutoCloseable = true;
        }
        check(extendsExecutor, "extends Executor");
        // JDK-8 版本差异：尚未扩展 AutoCloseable（close() 于 JDK 19 加入）
        check(!extendsAutoCloseable, "JDK-8 的 ExecutorService 不扩展 AutoCloseable（版本差异）");
        String[] names = {
            "shutdown", "shutdownNow", "isShutdown", "isTerminated", "awaitTermination",
            "submit", "invokeAll", "invokeAny",
        };
        Class<?>[][] params = {
            {}, {}, {}, {}, {long.class, TimeUnit.class},
            {Callable.class}, {java.util.Collection.class}, {java.util.Collection.class},
        };
        for (int i = 0; i < names.length; i++) {
            c.getMethod(names[i], params[i]);
            check(true, "方法面: " + names[i]);
        }
        boolean hasClose = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("close")) hasClose = true;
        }
        check(!hasClose, "JDK-8 的 ExecutorService 无 close()（版本差异）");
    }

    // 行为经 ThreadPoolExecutor 驱动：实例关系 + submit 三变体 + execute
    static void testBehaviorViaTPE() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        check(exec instanceof Executor, "ThreadPoolExecutor 是 Executor（经接口引用可用 execute）");
        try {
            check(exec.submit((Callable<Integer>) () -> 7).get() == 7, "submit(Callable).get 值精确");
            check("ok".equals(exec.submit(() -> { }, "ok").get()), "submit(Runnable, T).get 值精确");
            check(exec.submit(() -> { }).get() == null, "submit(Runnable).get 为 null");
            CountDownLatch ran = new CountDownLatch(1);
            exec.execute(ran::countDown);
            check(ran.await(5, TimeUnit.SECONDS), "execute(Runnable) 经接口引用执行任务");
        } finally {
            exec.shutdown();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // Future 契约（接口返回的 Future）：超时/取消/取消后 get
    static void testFutureContract() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        CountDownLatch release = new CountDownLatch(1);
        try {
            Future<Integer> slow = exec.submit(() -> { try { release.await(); } catch (InterruptedException e) { } return 1; });
            boolean timedOut = false;
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
                check(false, "未完成任务 get(100ms) 应超时");
            } catch (TimeoutException e) {
                timedOut = true;
            }
            check(timedOut, "get(timeout) 未完成时抛 TimeoutException");
            check(!slow.isDone(), "超时返回不影响 isDone");
            check(slow.cancel(true), "cancel(true) 返回 true");
            check(slow.isCancelled() && slow.isDone(), "取消后 isCancelled 且 isDone");
            boolean gotCE = false;
            try {
                slow.get();
                check(false, "取消后 get 应抛 CancellationException");
            } catch (CancellationException e) {
                gotCE = true;
            }
            check(gotCE, "取消后 get 抛 CancellationException");
        } finally {
            release.countDown();
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAll/invokeAny 经接口引用
    static void testInvokeViaInterface() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            tasks.add(() -> 1);
            tasks.add(() -> 2);
            List<Future<Integer>> all = exec.invokeAll(tasks);
            check(all.size() == 2 && all.get(0).get() == 1 && all.get(1).get() == 2,
                  "invokeAll 结果按提交序");
            CountDownLatch blocker = new CountDownLatch(1);
            try {
                Integer any = exec.invokeAny(new ArrayList<>(java.util.Arrays.asList(
                        () -> { try { blocker.await(); } catch (InterruptedException e) { } return 1; },
                        () -> 5)));
                check(any == 5, "invokeAny 返回成功任务值");
            } finally {
                blocker.countDown();
            }
        } finally {
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // shutdown 后拒绝：接口层 REE（消息含 "rejected from"，实测）
    static void testRejectionAfterShutdown() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        exec.shutdown();
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        boolean ree = false;
        try {
            exec.execute(() -> { });
            check(false, "shutdown 后 execute 应被拒");
        } catch (RejectedExecutionException e) {
            ree = e.getMessage() != null && e.getMessage().contains("rejected from");
        }
        check(ree, "shutdown 后 execute 抛 REE（消息含 \"rejected from\"）");
    }

    // shutdownNow：返回滞留任务 + 中断运行任务
    static void testShutdownNow() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        Runnable marker = () -> { };
        exec.execute(() -> {
            started.countDown();
            try { Thread.sleep(10000); }
            catch (InterruptedException e) { interrupted.countDown(); }
        });
        check(started.await(5, TimeUnit.SECONDS), "任务已启动");
        exec.execute(marker);
        List<Runnable> pending = exec.shutdownNow();
        try {
            check(pending.size() == 1 && pending.contains(marker), "shutdownNow 返回滞留任务列表");
            check(interrupted.await(5, TimeUnit.SECONDS), "shutdownNow 中断运行中任务");
        } finally {
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // awaitTermination 未 shutdown 时立即（超时后）返回 false
    static void testAwaitTerminationFalse() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        try {
            check(!exec.awaitTermination(200, TimeUnit.MILLISECONDS),
                  "未 shutdown 时 awaitTermination 返回 false");
            check(!exec.isShutdown() && !exec.isTerminated(), "超时等待不改变状态");
        } finally {
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // 任务异常经 Future.get 抛 ExecutionException（cause 原样）
    static void testExecutionException() throws Exception {
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<Runnable>());
        try {
            final IllegalStateException boom = new IllegalStateException("es-boom");
            Future<Integer> f = exec.submit(() -> { throw boom; });
            boolean gotEE = false;
            try {
                f.get();
                check(false, "任务异常应经 get 抛 ExecutionException");
            } catch (ExecutionException e) {
                gotEE = e.getCause() == boom;
            }
            check(gotEE, "ExecutionException 的 cause 为原异常实例");
            check(f.isDone() && !f.isCancelled(), "失败任务 done 且未取消");
        } finally {
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    public static void main(String[] args) throws Exception {
        testExecutorService();
        testInterfaceSurface();
        testBehaviorViaTPE();
        testFutureContract();
        testInvokeViaInterface();
        testRejectionAfterShutdown();
        testShutdownNow();
        testAwaitTerminationFalse();
        testExecutionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
