/*
 * @test
 * @summary java.util.concurrent.AbstractExecutorService API conformance: submit returns a Future.
 * @run main AbstractExecutorServiceTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AbstractExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** 断言异常类型精确 + 消息精确（JDK-8 实测消息为 null）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": unexpected message: " + m);
        passed++;
    }

    static void testAbstractExecutorService() throws Exception {
        AbstractExecutorService ses = new DirectExecutor();
        Future<Integer> f = ses.submit(new Callable<Integer>() {
            public Integer call() {
                return 3;
            }
        });
        check(f.get() == 3, "submit returns the task result");
    }

    static class DirectExecutor extends AbstractExecutorService {
        public void shutdown() {}

        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        public boolean isShutdown() {
            return false;
        }

        public boolean isTerminated() {
            return false;
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }

        public void execute(Runnable command) {
            command.run();
        }
    }

    // 跟踪 newTaskFor 调用的子类（委托真实 TPE 执行）
    static class TrackingExecutor extends AbstractExecutorService {
        final ThreadPoolExecutor delegate;
        int newTaskForRunnable;
        int newTaskForCallable;
        TrackingExecutor() {
            delegate = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                              new LinkedBlockingQueue<Runnable>());
        }
        protected <T> RunnableFuture<T> newTaskFor(Runnable r, T value) {
            newTaskForRunnable++;
            return super.newTaskFor(r, value);
        }
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> c) {
            newTaskForCallable++;
            return super.newTaskFor(c);
        }
        public void shutdown() { delegate.shutdown(); }
        public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
        public boolean isShutdown() { return delegate.isShutdown(); }
        public boolean isTerminated() { return delegate.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }
        public void execute(Runnable command) { delegate.execute(command); }
    }

    // submit 三变体都经由 newTaskFor 构造 Future
    static void testNewTaskForRouting() throws Exception {
        TrackingExecutor ex = new TrackingExecutor();
        try {
            check(ex.submit((Runnable) () -> { }).get() == null, "submit(Runnable) get null");
            check(ex.newTaskForRunnable == 1, "submit(Runnable) 调用 newTaskFor(Runnable, null)");
            check(ex.submit(() -> { }, "v").get().equals("v"), "submit(Runnable, T) 值精确");
            check(ex.newTaskForRunnable == 2, "submit(Runnable, T) 再次经 newTaskFor(Runnable, T)");
            check(ex.submit(() -> 5).get() == 5, "submit(Callable) 值精确");
            check(ex.newTaskForCallable == 1, "submit(Callable) 调用 newTaskFor(Callable)");
            List<Callable<Integer>> two = new ArrayList<>();
            two.add(() -> 1);
            two.add(() -> 2);
            ex.invokeAll(two);
            check(ex.newTaskForCallable == 3, "invokeAll(2 任务) 再产生 2 次 newTaskFor(Callable)");
        } finally {
            ex.shutdownNow();
        }
        check(ex.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAll：全 done、结果按提交序
    static void testInvokeAllContract() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            tasks.add(() -> 10);
            tasks.add(() -> 20);
            List<Future<Integer>> futures = tpe.invokeAll(tasks);
            check(futures.size() == 2, "invokeAll 返回与任务数相同的 Future 列表");
            check(futures.get(0).isDone() && futures.get(1).isDone(), "invokeAll 返回时全部 done");
            check(futures.get(0).get() == 10 && futures.get(1).get() == 20, "invokeAll 结果按提交序对应");
            // 空集合：合法，返回空列表
            check(tpe.invokeAll(new ArrayList<Callable<Integer>>()).isEmpty(), "invokeAll(空集合) 返回空列表");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAll(timeout)：超时 → 未完成任务被取消（isCancelled、get 抛 CancellationException）
    static void testInvokeAllTimeout() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        CountDownLatch release = new CountDownLatch(1);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            tasks.add(() -> { try { release.await(); } catch (InterruptedException e) { } return 1; });
            tasks.add(() -> { try { release.await(); } catch (InterruptedException e) { } return 2; });
            List<Future<Integer>> futures = tpe.invokeAll(tasks, 300, TimeUnit.MILLISECONDS);
            check(futures.get(0).isDone() && futures.get(1).isDone(), "invokeAll(timeout) 返回时全部 done（完成或被取消）");
            check(futures.get(0).isCancelled() && futures.get(1).isCancelled(), "超时未完成任务均被取消");
            boolean ce0 = false, ce1 = false;
            try { futures.get(0).get(); } catch (CancellationException e) { ce0 = true; }
            try { futures.get(1).get(); } catch (CancellationException e) { ce1 = true; }
            check(ce0 && ce1, "被取消 Future 的 get 抛 CancellationException");
        } finally {
            release.countDown();
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAny：一个成功即返回（阻塞任务 + 立即成功任务 → 返回成功者）
    static void testInvokeAnyFirstSuccess() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        CountDownLatch blocker = new CountDownLatch(1);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            tasks.add(() -> { try { blocker.await(); } catch (InterruptedException e) { } return 1; });
            tasks.add(() -> 99);
            Integer got = tpe.invokeAny(tasks);
            check(got == 99, "invokeAny 返回首个成功任务的值（不等待阻塞任务）");
        } finally {
            blocker.countDown();
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAny 全部失败 → ExecutionException，cause 为异常实例（单线程 FIFO → 最后一个异常）
    static void testInvokeAnyAllFail() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            final RuntimeException ex1 = new RuntimeException("one");
            final RuntimeException ex2 = new RuntimeException("two");
            final RuntimeException ex3 = new RuntimeException("three");
            List<Callable<Integer>> tasks = new ArrayList<>();
            tasks.add(() -> { throw ex1; });
            tasks.add(() -> { throw ex2; });
            tasks.add(() -> { throw ex3; });
            boolean gotEE = false;
            try {
                tpe.invokeAny(tasks);
                check(false, "invokeAny 全失败应抛 ExecutionException");
            } catch (ExecutionException e) {
                gotEE = e.getCause() == ex3;
            }
            check(gotEE, "单线程 FIFO 下 invokeAny 全失败的 cause 为最后一个异常实例");

            // 带超时版本同样语义
            List<Callable<Integer>> two = new ArrayList<>();
            two.add(() -> { throw ex1; });
            two.add(() -> { throw ex2; });
            boolean gotEETimed = false;
            try {
                tpe.invokeAny(two, 5, TimeUnit.SECONDS);
                check(false, "timed invokeAny 全失败应抛 ExecutionException");
            } catch (ExecutionException e) {
                gotEETimed = e.getCause() == ex2;
            }
            check(gotEETimed, "timed invokeAny 全失败的 cause 为最后一个异常实例");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAny/invokeAll 空集合与 null 负例；submit(null) NPE
    static void testNegativeCases() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        try {
            try {
                tpe.invokeAny(new ArrayList<Callable<Integer>>());
                check(false, "invokeAny(空) 应抛 IAE");
            } catch (IllegalArgumentException e) {
                checkExc(e, IllegalArgumentException.class, null, "invokeAny 空集合 IAE（JDK-8 消息 null）");
            }
            try {
                tpe.invokeAny(null);
                check(false, "invokeAny(null) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "invokeAny(null) NPE");
            }
            try {
                tpe.invokeAll(null);
                check(false, "invokeAll(null) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "invokeAll(null) NPE");
            }
            List<Callable<Integer>> withNull = new ArrayList<>();
            withNull.add(() -> 1);
            withNull.add(null);
            try {
                tpe.invokeAll(withNull);
                check(false, "invokeAll 含 null 元素应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "invokeAll null 元素 NPE");
            }
            try {
                tpe.submit((Runnable) null);
                check(false, "submit(null Runnable) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "submit(null Runnable) NPE");
            }
            try {
                tpe.submit((Callable<?>) null);
                check(false, "submit(null Callable) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "submit(null Callable) NPE");
            }
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // DirectExecutor（调用者线程执行）下任务异常被 Future 捕获
    static void testDirectExecutorException() throws Exception {
        DirectExecutor ses = new DirectExecutor();
        final RuntimeException boom = new RuntimeException("direct-boom");
        Future<Integer> f = ses.submit(() -> { throw boom; });
        boolean gotEE = false;
        try {
            f.get();
            check(false, "任务异常应经 get 抛 ExecutionException");
        } catch (ExecutionException e) {
            gotEE = e.getCause() == boom;
        }
        check(gotEE, "DirectExecutor 下异常经 FutureTask 捕获且 cause 为原实例");
        check(f.isDone(), "失败任务同样标记 done");
    }

    public static void main(String[] args) throws Exception {
        testAbstractExecutorService();
        testNewTaskForRouting();
        testInvokeAllContract();
        testInvokeAllTimeout();
        testInvokeAnyFirstSuccess();
        testInvokeAnyAllFail();
        testNegativeCases();
        testDirectExecutorException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
