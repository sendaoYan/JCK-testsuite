/*
 * @test
 * @summary java.util.concurrent.ExecutorService conformance: white-box structure + black-box
 *          behavior (submit/get, invokeAll/invokeAny, shutdown/awaitTermination/close).
 * @run main ExecutorServiceTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTest {
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
        Class<?> c = cls("java.util.concurrent.ExecutorService");
        check(c.isInterface(), "ExecutorService is an interface");
        Class<?>[] ifs = c.getInterfaces();
        boolean extendsExecutor = false, extendsAutoCloseable = false;
        for (Class<?> i : ifs) {
            if (i == Executor.class) extendsExecutor = true;
            if (i == AutoCloseable.class) extendsAutoCloseable = true;
        }
        check(extendsExecutor, "extends Executor");
        check(extendsAutoCloseable, "extends AutoCloseable");
        hasMethod(c, "shutdown");
        hasMethod(c, "shutdownNow");
        hasMethod(c, "isShutdown");
        hasMethod(c, "isTerminated");
        hasMethod(c, "awaitTermination", long.class, TimeUnit.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "invokeAll", java.util.Collection.class);
        hasMethod(c, "invokeAny", java.util.Collection.class);
        hasMethod(c, "close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(3);
        try {
            // submit variants
            Future<Integer> c = exec.submit((Callable<Integer>) () -> 42);
            check(c.get() == 42, "submit(Callable).get");
            Future<String> r = exec.submit(() -> { }, "ok");
            check("ok".equals(r.get()), "submit(Runnable, result).get");
            Future<?> v = exec.submit(() -> { });
            check(v.get() == null, "submit(Runnable).get returns null");

            // invokeAll returns in-order, all-completed futures
            List<Callable<Integer>> tasks = Arrays.asList(() -> 1, () -> 2, () -> 3);
            List<Future<Integer>> results = exec.invokeAll(tasks);
            check(results.size() == 3, "invokeAll size");
            for (Future<Integer> f : results) check(f.isDone(), "invokeAll futures are done");
            check(results.get(0).get() == 1 && results.get(1).get() == 2 && results.get(2).get() == 3,
                  "invokeAll preserves order and results");

            // invokeAny returns one of the results
            Integer any = exec.invokeAny(tasks);
            check(any != null && any >= 1 && any <= 3, "invokeAny returns a valid result");
        } finally {
            exec.shutdown();
        }
        check(exec.isShutdown(), "isShutdown after shutdown");
        check(exec.awaitTermination(10, TimeUnit.SECONDS), "awaitTermination returns true");
        check(exec.isTerminated(), "isTerminated after termination");
        check(exec.shutdownNow().isEmpty(), "shutdownNow returns empty list when already terminated");

        // close() (AutoCloseable default) initiates shutdown
        ExecutorService c2 = Executors.newSingleThreadExecutor();
        c2.submit(() -> 7).get();
        c2.close();
        check(c2.isShutdown(), "close() shuts down the service");
        check(c2.awaitTermination(10, TimeUnit.SECONDS), "closed service terminates");
    }

    // ---- 追加：契约深化 ----

    // Future 契约（接口返回的 Future）：超时/取消/取消后 get
    static void testFutureContract() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try {
            Future<Integer> slow = exec.submit(() -> { try { release.await(); } catch (InterruptedException e) { } return 1; });
            boolean timedOut = false;
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
                check(false, "未完成任务 get(100ms) 应超时");
            } catch (java.util.concurrent.TimeoutException e) {
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
            } catch (java.util.concurrent.CancellationException e) {
                gotCE = true;
            }
            check(gotCE, "取消后 get 抛 CancellationException");
        } finally {
            release.countDown();
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // shutdown 后拒绝（接口层，消息含 "rejected from"）
    static void testRejectionAfterShutdown() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
        exec.shutdown();
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
        boolean ree = false;
        try {
            exec.execute(() -> { });
            check(false, "shutdown 后 execute 应被拒");
        } catch (java.util.concurrent.RejectedExecutionException e) {
            ree = e.getMessage() != null && e.getMessage().contains("rejected from");
        }
        check(ree, "shutdown 后 execute 抛 REE（消息含 \"rejected from\"）");
    }

    // shutdownNow：返回滞留任务 + 中断运行任务（接口引用）
    static void testShutdownNow() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
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

    // awaitTermination 未 shutdown 时（超时后）返回 false
    static void testAwaitTerminationFalse() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
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
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            final IllegalStateException boom = new IllegalStateException("es-boom");
            Future<Integer> f = exec.submit(() -> { throw boom; });
            boolean gotEE = false;
            try {
                f.get();
                check(false, "任务异常应经 get 抛 ExecutionException");
            } catch (java.util.concurrent.ExecutionException e) {
                gotEE = e.getCause() == boom;
            }
            check(gotEE, "ExecutionException 的 cause 为原异常实例");
            check(f.isDone() && !f.isCancelled(), "失败任务 done 且未取消");
        } finally {
            exec.shutdownNow();
        }
        check(exec.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // close() 深化：幂等、等待在途任务、close 后 submit → REE
    static void testCloseSemantics() throws Exception {
        // close 幂等：重复 close 无异常
        ExecutorService a = java.util.concurrent.Executors.newSingleThreadExecutor();
        a.submit(() -> 1).get();
        a.close();
        a.close();
        check(a.isShutdown() && a.isTerminated(), "close 幂等且已终止");
        boolean ree = false;
        try {
            a.submit(() -> { });
            check(false, "close 后 submit 应被拒");
        } catch (java.util.concurrent.RejectedExecutionException e) {
            ree = true;
        }
        check(ree, "close 后 submit 抛 RejectedExecutionException");

        // close 等待在途任务完成而非直接取消
        ExecutorService b = java.util.concurrent.Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        final boolean[] taskFinished = new boolean[1];
        b.execute(() -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { }
            taskFinished[0] = true;
        });
        check(started.await(5, TimeUnit.SECONDS), "任务已启动");
        Thread closer = new Thread(b::close, "es-closer");
        closer.setDaemon(true);
        closer.start();
        // closer 阻塞在 close 中等待任务完成；释放后 join 成功即证明等待语义
        release.countDown();
        closer.join(5000);
        check(!closer.isAlive(), "close() 等待在途任务完成后返回");
        check(taskFinished[0], "在途任务正常完成（未被 close 中断）");
        check(b.isTerminated(), "close 后执行器终止");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFutureContract();
        testRejectionAfterShutdown();
        testShutdownNow();
        testAwaitTerminationFalse();
        testExecutionException();
        testCloseSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
