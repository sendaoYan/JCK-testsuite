/*
 * @test
 * @summary java.util.concurrent.AbstractExecutorService conformance: white-box structure + black-box behavior.
 * @run main AbstractExecutorServiceTest
 */

import java.util.*;
import java.util.concurrent.*;

public class AbstractExecutorServiceTest {
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

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.AbstractExecutorService");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(ExecutorService.class.isAssignableFrom(c), "implements ExecutorService");
        hasMethod(c, "submit", Runnable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "invokeAll", Collection.class);
        hasMethod(c, "invokeAll", Collection.class, long.class, TimeUnit.class);
        hasMethod(c, "invokeAny", Collection.class);
        hasMethod(c, "invokeAny", Collection.class, long.class, TimeUnit.class);
        // protected 可覆写钩子 newTaskFor
        check(hasDeclaredProtected(c, "newTaskFor", Runnable.class, Object.class),
              "protected newTaskFor(Runnable, T) 可覆写");
        check(hasDeclaredProtected(c, "newTaskFor", Callable.class),
              "protected newTaskFor(Callable) 可覆写");
    }

    static boolean hasDeclaredProtected(Class<?> c, String name, Class<?>... p) {
        try {
            int m = c.getDeclaredMethod(name, p).getModifiers();
            return java.lang.reflect.Modifier.isProtected(m);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DirectExecutor ses = new DirectExecutor();
        check(ses.submit(() -> 3).get() == 3, "submit(Callable) returns result");
        check(ses.submit(() -> { }).get() == null, "submit(Runnable) returns null");
        check(ses.submit(() -> { }, "v").get().equals("v"), "submit(Runnable, T) returns T");

        List<Callable<Integer>> tasks = Arrays.asList(() -> 1, () -> 2, () -> 3);
        List<Future<Integer>> futures = ses.invokeAll(tasks);
        check(futures.size() == 3, "invokeAll returns all futures");
        check(futures.get(0).get() == 1 && futures.get(1).get() == 2 && futures.get(2).get() == 3,
              "invokeAll preserves order");
        check(futures.stream().allMatch(Future::isDone), "invokeAll tasks all done");

        List<Future<Integer>> timed = ses.invokeAll(tasks, 1, TimeUnit.SECONDS);
        check(timed.size() == 3 && timed.stream().allMatch(Future::isDone), "invokeAll(timeout) all done");

        Integer any = ses.invokeAny(Arrays.asList(() -> 7));
        check(any == 7, "invokeAny returns a result");
    }

    // ---- 追加：契约深化 ----

    // 跟踪 newTaskFor 调用的子类（委托真实 TPE 执行）
    static class TrackingExecutor extends AbstractExecutorService {
        final ThreadPoolExecutor delegate;
        int newTaskForRunnable;
        int newTaskForCallable;
        TrackingExecutor() {
            delegate = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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

    static void testNewTaskForRouting() throws Exception {
        TrackingExecutor ex = new TrackingExecutor();
        try {
            check(ex.submit((Runnable) () -> { }).get() == null, "submit(Runnable) get null");
            check(ex.newTaskForRunnable == 1, "submit(Runnable) 调用 newTaskFor(Runnable, null)");
            check(ex.submit(() -> { }, "v").get().equals("v"), "submit(Runnable, T) 值精确");
            check(ex.newTaskForRunnable == 2, "submit(Runnable, T) 再次经 newTaskFor(Runnable, T)");
            check(ex.submit(() -> 5).get() == 5, "submit(Callable) 值精确");
            check(ex.newTaskForCallable == 1, "submit(Callable) 调用 newTaskFor(Callable)");
            ex.invokeAll(Arrays.asList(() -> 1, () -> 2));
            check(ex.newTaskForCallable == 3, "invokeAll(2 任务) 再产生 2 次 newTaskFor(Callable)");
        } finally {
            ex.shutdownNow();
        }
        check(ex.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    static void testInvokeAllContract() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            List<Callable<Integer>> tasks = Arrays.asList(() -> 10, () -> 20);
            List<Future<Integer>> futures = tpe.invokeAll(tasks);
            check(futures.size() == 2, "invokeAll 返回与任务数相同的 Future 列表");
            check(futures.get(0).isDone() && futures.get(1).isDone(), "invokeAll 返回时全部 done");
            check(futures.get(0).get() == 10 && futures.get(1).get() == 20, "invokeAll 结果按提交序对应");
            check(tpe.invokeAll(new ArrayList<Callable<Integer>>()).isEmpty(), "invokeAll(空集合) 返回空列表");
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAll(timeout)：超时 → 未完成任务被取消
    static void testInvokeAllTimeout() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch release = new CountDownLatch(1);
        try {
            List<Callable<Integer>> tasks = Arrays.asList(
                () -> { try { release.await(); } catch (InterruptedException e) { } return 1; },
                () -> { try { release.await(); } catch (InterruptedException e) { } return 2; });
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

    // invokeAny：一个成功即返回
    static void testInvokeAnyFirstSuccess() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch blocker = new CountDownLatch(1);
        try {
            List<Callable<Integer>> tasks = Arrays.asList(
                () -> { try { blocker.await(); } catch (InterruptedException e) { } return 1; },
                () -> 99);
            Integer got = tpe.invokeAny(tasks);
            check(got == 99, "invokeAny 返回首个成功任务的值（不等待阻塞任务）");
        } finally {
            blocker.countDown();
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // invokeAny 全部失败 → ExecutionException（单线程 FIFO → cause 为最后一个异常实例）
    static void testInvokeAnyAllFail() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            final RuntimeException ex1 = new RuntimeException("one");
            final RuntimeException ex2 = new RuntimeException("two");
            final RuntimeException ex3 = new RuntimeException("three");
            boolean gotEE = false;
            try {
                tpe.invokeAny(Arrays.asList(() -> { throw ex1; }, () -> { throw ex2; }, () -> { throw ex3; }));
                check(false, "invokeAny 全失败应抛 ExecutionException");
            } catch (ExecutionException e) {
                gotEE = e.getCause() == ex3;
            }
            check(gotEE, "单线程 FIFO 下 invokeAny 全失败的 cause 为最后一个异常实例");

            boolean gotEETimed = false;
            try {
                tpe.invokeAny(Arrays.asList(() -> { throw ex1; }, () -> { throw ex2; }), 5, TimeUnit.SECONDS);
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

    // 负例：空集合/​null（JDK-27 消息精确）+ submit(null)
    static void testNegativeCases() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            try {
                tpe.invokeAny(new ArrayList<Callable<Integer>>());
                check(false, "invokeAny(空) 应抛 IAE");
            } catch (IllegalArgumentException e) {
                checkExc(e, IllegalArgumentException.class, "tasks is empty", "invokeAny 空集合 IAE（JDK-27 消息）");
            }
            try {
                tpe.invokeAny(null);
                check(false, "invokeAny(null) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, "tasks", "invokeAny(null) NPE");
            }
            try {
                tpe.invokeAll(null);
                check(false, "invokeAll(null) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, "tasks", "invokeAll(null) NPE");
            }
            List<Callable<Integer>> withNull = new ArrayList<>();
            withNull.add(() -> 1);
            withNull.add(null);
            try {
                tpe.invokeAll(withNull);
                check(false, "invokeAll 含 null 元素应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, null, "invokeAll null 元素 NPE（实测消息 null）");
            }
            try {
                tpe.submit((Runnable) null);
                check(false, "submit(null Runnable) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, "task", "submit(null Runnable) NPE");
            }
            try {
                tpe.submit((Callable<?>) null);
                check(false, "submit(null Callable) 应抛 NPE");
            } catch (NullPointerException e) {
                checkExc(e, NullPointerException.class, "task", "submit(null Callable) NPE");
            }
        } finally {
            tpe.shutdownNow();
        }
        check(tpe.awaitTermination(5, TimeUnit.SECONDS), "terminates");
    }

    // DirectExecutor 下任务异常被 Future 捕获
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
        testWhiteBox();
        testBlackBox();
        testNewTaskForRouting();
        testInvokeAllContract();
        testInvokeAllTimeout();
        testInvokeAnyFirstSuccess();
        testInvokeAnyAllFail();
        testNegativeCases();
        testDirectExecutorException();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static class DirectExecutor extends AbstractExecutorService {
        public void shutdown() {}
        public List<Runnable> shutdownNow() { return Collections.emptyList(); }
        public boolean isShutdown() { return false; }
        public boolean isTerminated() { return false; }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        public void execute(Runnable command) { command.run(); }
    }}
