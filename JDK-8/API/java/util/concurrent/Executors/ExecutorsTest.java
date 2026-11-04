/*
 * @test
 * @summary java.util.concurrent.Executors API conformance: factory-created pools execute
 *          a Callable and terminate.
 * @run main ExecutorsTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** 断言异常类型精确 + 消息精确（JDK-8 侧实测 j.u.c 工厂负例消息为 null）。 */
    static void checkExc(Throwable t, Class<? extends Throwable> type, String expectMsg, String label) {
        if (!type.isInstance(t))
            throw new AssertionError(label + ": expected " + type.getName() + " but got " + t);
        passed++;
        String m = t.getMessage();
        if (expectMsg == null ? m != null : !expectMsg.equals(m))
            throw new AssertionError(label + ": unexpected message: " + m);
        passed++;
    }

    static void testExecutors() throws Exception {
        ExecutorService single = Executors.newSingleThreadExecutor();
        ExecutorService fixed = Executors.newFixedThreadPool(2);
        ExecutorService cached = Executors.newCachedThreadPool();
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        try {
            Future<Integer> f = single.submit(new Callable<Integer>() {
                public Integer call() { return 42; }
            });
            check(f.get() == 42, "submit result");
        } finally {
            single.shutdown();
            fixed.shutdown();
            cached.shutdown();
            sched.shutdown();
        }
        check(single.awaitTermination(10, TimeUnit.SECONDS), "single terminated");
    }

    // newFixedThreadPool(2)：屏障证明恰好 2 并行 + 三方屏障证明上界 2
    static void testFixedThreadPoolParallelism() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CyclicBarrier pair = new CyclicBarrier(2);
            CountDownLatch bothDone = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);
            for (int i = 0; i < 2; i++) {
                pool.execute(() -> {
                    try {
                        pair.await(5, TimeUnit.SECONDS);
                        bothDone.countDown();
                        release.await();
                    } catch (Exception e) { /* 由 bothDone 记录失败 */ }
                });
            }
            check(bothDone.await(5, TimeUnit.SECONDS),
                  "newFixedThreadPool(2): 两任务经 CyclicBarrier(2) 证明同时运行");
            release.countDown();

            CyclicBarrier trio = new CyclicBarrier(3);
            final AtomicInteger passedBarrier = new AtomicInteger();
            CountDownLatch allReturned = new CountDownLatch(3);
            for (int i = 0; i < 3; i++) {
                pool.execute(() -> {
                    try { trio.await(1500, TimeUnit.MILLISECONDS); passedBarrier.incrementAndGet(); }
                    catch (Exception e) { /* 超时/破损即并行度受限于 2 的证据 */ }
                    allReturned.countDown();
                });
            }
            check(allReturned.await(5, TimeUnit.SECONDS), "有界并行度探测完成");
            check(passedBarrier.get() == 0,
                  "newFixedThreadPool(2) 的第三方屏障永不成立：并行度上界为 2");
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "fixed pool terminates");
    }

    // newSingleThreadExecutor：提交序执行 + 全部任务共用一个线程
    static void testSingleThreadSerialization() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            final List<Integer> order = new ArrayList<>();
            Set<String> names = ConcurrentHashMap.newKeySet();
            CountDownLatch done = new CountDownLatch(3);
            for (int i = 1; i <= 3; i++) {
                final int id = i;
                pool.execute(() -> {
                    synchronized (order) { order.add(id); }
                    names.add(Thread.currentThread().getName());
                    done.countDown();
                });
            }
            check(done.await(5, TimeUnit.SECONDS), "串行任务全部完成");
            synchronized (order) {
                check(order.size() == 3 && order.get(0) == 1 && order.get(1) == 2 && order.get(2) == 3,
                      "newSingleThreadExecutor 按提交序 1,2,3 执行");
            }
            check(names.size() == 1, "三个任务共用同一个工作线程");
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "single-thread pool terminates");
    }

    // newCachedThreadPool：4 个并发阻塞任务恰好占用 4 个线程；线程名 pool-N-thread-M；第二轮仅断言线程总数上界
    static void testCachedThreadPoolThreads() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            Set<String> names = ConcurrentHashMap.newKeySet();
            CountDownLatch entered4 = new CountDownLatch(4);
            CountDownLatch release = new CountDownLatch(1);
            for (int i = 0; i < 4; i++) {
                pool.execute(() -> {
                    names.add(Thread.currentThread().getName());
                    entered4.countDown();
                    try { release.await(); } catch (InterruptedException e) { }
                });
            }
            check(entered4.await(5, TimeUnit.SECONDS), "4 个任务同时运行");
            check(names.size() == 4, "4 个同时阻塞的任务恰好占用 4 个不同线程");
            for (String n : names)
                check(n.matches("pool-\\d+-thread-\\d+"), "cached 线程名符合 pool-N-thread-M: " + n);
            release.countDown();

            // 第二轮：空闲线程可能被复用（60s keepAlive 内），但 SynchronousQueue 的
            // offer/worker 回 park 存在竞态，故只断言确定的上界（每任务至多 1 新线程）
            CountDownLatch entered4b = new CountDownLatch(4);
            CountDownLatch release2 = new CountDownLatch(1);
            for (int i = 0; i < 4; i++) {
                pool.execute(() -> {
                    names.add(Thread.currentThread().getName());
                    entered4b.countDown();
                    try { release2.await(); } catch (InterruptedException e) { }
                });
            }
            check(entered4b.await(5, TimeUnit.SECONDS), "第二轮 4 个任务同时运行");
            check(names.size() >= 4 && names.size() <= 8,
                  "两轮共 8 任务的不同线程名在 [4,8] 内: " + names.size());
            release2.countDown();
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "cached pool terminates");
    }

    // newWorkStealingPool(2)：屏障证明 2 并行
    static void testWorkStealingParallelism() throws Exception {
        ExecutorService pool = Executors.newWorkStealingPool(2);
        try {
            CyclicBarrier pair = new CyclicBarrier(2);
            CountDownLatch bothDone = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);
            for (int i = 0; i < 2; i++) {
                pool.execute(() -> {
                    try {
                        pair.await(5, TimeUnit.SECONDS);
                        bothDone.countDown();
                        release.await();
                    } catch (Exception e) { }
                });
            }
            check(bothDone.await(5, TimeUnit.SECONDS),
                  "newWorkStealingPool(2): 两任务经屏障证明同时运行");
            release.countDown();
        } finally {
            pool.shutdownNow();
        }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "work-stealing pool terminates");
    }

    // defaultThreadFactory：命名/daemon/优先级/未启动/null 负例
    static void testDefaultThreadFactory() {
        ThreadFactory tf = Executors.defaultThreadFactory();
        Thread t1 = tf.newThread(() -> { });
        Thread t2 = tf.newThread(() -> { });
        check(t1 != null && t2 != null, "defaultThreadFactory 创建线程");
        check(t1.getName().matches("pool-\\d+-thread-\\d+"),
              "default 线程名符合 pool-N-thread-M: " + t1.getName());
        check(!t1.getName().equals(t2.getName()), "同工厂连续线程名递增（M 不同）");
        check(!t1.isDaemon(), "default 工厂线程为非 daemon");
        check(t1.getPriority() == Thread.NORM_PRIORITY, "default 工厂优先级 NORM_PRIORITY");
        check(!t1.isAlive(), "newThread 不启动线程");
        check(t1.getThreadGroup() != null, "线程组非 null");
        // 实测：null runnable 合法（Thread 构造器接受 null），返回未启动线程
        Thread t3 = tf.newThread(null);
        check(t3 != null && !t3.isAlive(), "newThread(null) 返回未启动线程（null runnable 合法）");
    }

    static ThreadFactory customFactory(final AtomicInteger seq) {
        return r -> {
            Thread t = new Thread(r, "jck-custom-" + seq.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY + 1);
            return t;
        };
    }

    // 自定义 ThreadFactory 透传：名字/daemon/优先级在任务内观察
    static void testCustomThreadFactoryPassthrough() throws Exception {
        AtomicInteger seq = new AtomicInteger();
        ThreadFactory tf = customFactory(seq);
        ExecutorService[] pools = {
            Executors.newFixedThreadPool(1, tf),
            Executors.newSingleThreadExecutor(tf),
            Executors.newCachedThreadPool(tf),
        };
        try {
            for (ExecutorService p : pools) {
                CountDownLatch done = new CountDownLatch(1);
                final String[] seenName = new String[1];
                final boolean[] seenDaemon = new boolean[1];
                final int[] seenPrio = new int[1];
                p.execute(() -> {
                    Thread cur = Thread.currentThread();
                    seenName[0] = cur.getName();
                    seenDaemon[0] = cur.isDaemon();
                    seenPrio[0] = cur.getPriority();
                    done.countDown();
                });
                check(done.await(5, TimeUnit.SECONDS), "custom-factory 任务完成");
                check(seenName[0] != null && seenName[0].startsWith("jck-custom-"),
                      "自定义线程名透传: " + seenName[0]);
                check(seenDaemon[0], "自定义 daemon 标志透传");
                check(seenPrio[0] == Thread.MIN_PRIORITY + 1, "自定义优先级透传");
            }
        } finally {
            for (ExecutorService p : pools) p.shutdownNow();
        }
    }

    // Callable 适配器族
    static void testCallableAdapters() throws Exception {
        final boolean[] ran = new boolean[1];
        Callable<Object> c1 = Executors.callable((Runnable) () -> ran[0] = true);
        check(!ran[0], "callable(Runnable) 不急切执行");
        check(c1.call() == null, "callable(Runnable).call() 返回 null");
        check(ran[0], "callable(Runnable) 在 call() 时运行 runnable");

        Callable<String> c2 = Executors.callable(() -> { }, "result-T");
        check("result-T".equals(c2.call()), "callable(Runnable, T) 返回固定值 T");

        java.security.PrivilegedAction<String> pa = () -> "pa-value";
        check("pa-value".equals(Executors.callable(pa).call()),
              "callable(PrivilegedAction) 返回 action 结果");

        java.security.PrivilegedExceptionAction<String> pea = () -> "pea-value";
        check("pea-value".equals(Executors.callable(pea).call()),
              "callable(PrivilegedExceptionAction) 返回 action 结果");

        final RuntimeException boom = new RuntimeException("pea-boom");
        java.security.PrivilegedExceptionAction<String> peaThrow =
                () -> { throw new java.security.PrivilegedActionException(boom); };
        boolean propagated = false;
        try {
            Executors.callable(peaThrow).call();
        } catch (java.security.PrivilegedActionException expected) {
            propagated = expected.getCause() == boom;
        }
        check(propagated, "callable(PrivilegedExceptionAction) 透传 PrivilegedActionException 且 cause 为原异常");

        boolean npe1 = false;
        try { Executors.callable((Runnable) null); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "callable(null Runnable) 抛 NPE");
        boolean npe2 = false;
        try { Executors.callable(null, "x"); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "callable(null, T) 抛 NPE");
        boolean npe3 = false;
        try { Executors.callable((java.security.PrivilegedAction<?>) null); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "callable(null PrivilegedAction) 抛 NPE");
    }

    // privileged 系工厂（无 SecurityManager 环境下仍可运行；JDK-8 未废弃）
    static void testPrivilegedFactories() throws Exception {
        check("pv".equals(Executors.privilegedCallable(() -> "pv").call()),
              "privilegedCallable 返回原 Callable 结果");
        check("pccl".equals(Executors.privilegedCallableUsingCurrentClassLoader(() -> "pccl").call()),
              "privilegedCallableUsingCurrentClassLoader 返回原 Callable 结果");
        Thread t = Executors.privilegedThreadFactory().newThread(() -> { });
        check(t != null && t.getName().matches("pool-\\d+-thread-\\d+"),
              "privilegedThreadFactory 线程名 pool-N-thread-M: " + t.getName());
        check(!t.isDaemon(), "privilegedThreadFactory 非 daemon");
    }

    // Scheduled 工厂（详细行为归 ScheduledThreadPoolExecutor 批次，此处做工厂存在性+透传）
    static void testScheduledThreadPoolFactories() throws Exception {
        ScheduledExecutorService a = Executors.newSingleThreadScheduledExecutor();
        try {
            CountDownLatch ran = new CountDownLatch(1);
            a.schedule(ran::countDown, 0, TimeUnit.MILLISECONDS);
            check(ran.await(5, TimeUnit.SECONDS), "newSingleThreadScheduledExecutor 执行 0 延迟任务");
        } finally {
            a.shutdownNow();
        }
        check(a.awaitTermination(5, TimeUnit.SECONDS), "single-scheduled terminates");

        ScheduledExecutorService b = Executors.newScheduledThreadPool(2);
        try {
            check(b.schedule(() -> 8, 0, TimeUnit.MILLISECONDS).get() == 8,
                  "newScheduledThreadPool schedule(Callable) 值精确");
        } finally {
            b.shutdownNow();
        }
        check(b.awaitTermination(5, TimeUnit.SECONDS), "scheduled(2) terminates");

        AtomicInteger seq = new AtomicInteger();
        ScheduledExecutorService c = Executors.newScheduledThreadPool(1, customFactory(seq));
        try {
            CountDownLatch done = new CountDownLatch(1);
            final String[] seen = new String[1];
            c.execute(() -> { seen[0] = Thread.currentThread().getName(); done.countDown(); });
            check(done.await(5, TimeUnit.SECONDS), "scheduled tf 任务完成");
            check(seen[0] != null && seen[0].startsWith("jck-custom-"),
                  "newScheduledThreadPool(int, ThreadFactory) 线程名透传");
        } finally {
            c.shutdownNow();
        }
    }

    // unconfigurable 包装：委托执行 + shutdown 传播
    static void testUnconfigurable() throws Exception {
        ExecutorService inner = Executors.newSingleThreadExecutor();
        ExecutorService wrapped = Executors.unconfigurableExecutorService(inner);
        try {
            check(wrapped.submit(() -> 11).get() == 11, "unconfigurableExecutorService 委托 submit");
        } finally {
            wrapped.shutdown();
        }
        check(inner.isShutdown(), "包装器 shutdown 传播到委托池");
        check(wrapped.awaitTermination(5, TimeUnit.SECONDS), "unconfigurable terminates");

        ScheduledExecutorService sInner = Executors.newScheduledThreadPool(1);
        ScheduledExecutorService sWrapped = Executors.unconfigurableScheduledExecutorService(sInner);
        try {
            check(sWrapped.schedule(() -> 12, 0, TimeUnit.MILLISECONDS).get() == 12,
                  "unconfigurableScheduledExecutorService 委托 schedule");
        } finally {
            sWrapped.shutdownNow();
        }
        check(sWrapped.awaitTermination(5, TimeUnit.SECONDS), "unconfigurable scheduled terminates");
    }

    // 工厂负例（JDK-8 实测消息均为 null；JDK 9+ 携带描述性消息，见 JDK-27 侧）
    static void testFactoryNegatives() throws InterruptedException, java.util.concurrent.ExecutionException {
        try {
            Executors.newFixedThreadPool(0);
            check(false, "newFixedThreadPool(0) 应抛 IAE");
        } catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, null, "newFixedThreadPool(0)");
        }
        try {
            Executors.newFixedThreadPool(-1);
            check(false, "newFixedThreadPool(-1) 应抛 IAE");
        } catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, null, "newFixedThreadPool(-1)");
        }
        try {
            Executors.newScheduledThreadPool(-1);
            check(false, "newScheduledThreadPool(-1) 应抛 IAE");
        } catch (IllegalArgumentException e) {
            checkExc(e, IllegalArgumentException.class, null, "newScheduledThreadPool(-1)");
        }
        // newScheduledThreadPool(0) 合法（core=0 的 STPE 可用）
        ScheduledExecutorService zero = Executors.newScheduledThreadPool(0);
        try {
            check(zero.schedule(() -> 1, 0, TimeUnit.MILLISECONDS).get() == 1,
                  "newScheduledThreadPool(0) 合法可用");
        } finally {
            zero.shutdownNow();
        }
        try {
            Executors.unconfigurableExecutorService(null);
            check(false, "unconfigurableExecutorService(null) 应抛 NPE");
        } catch (NullPointerException e) {
            checkExc(e, NullPointerException.class, null, "unconfigurableExecutorService(null)");
        }
    }

    public static void main(String[] args) throws Exception {
        testExecutors();
        testFixedThreadPoolParallelism();
        testSingleThreadSerialization();
        testCachedThreadPoolThreads();
        testWorkStealingParallelism();
        testDefaultThreadFactory();
        testCustomThreadFactoryPassthrough();
        testCallableAdapters();
        testPrivilegedFactories();
        testScheduledThreadPoolFactories();
        testUnconfigurable();
        testFactoryNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
