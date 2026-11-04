/*
 * @test
 * @summary java.util.concurrent.ForkJoinWorkerThread conformance: white-box structure + black-box behavior.
 * @run main ForkJoinWorkerThreadTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ForkJoinWorkerThreadTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
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
        Class<?> c = cls("java.util.concurrent.ForkJoinWorkerThread");
        check(!c.isInterface(), "class");
        check(Thread.class.isAssignableFrom(c), "extends Thread");
        hasMethod(c, "getPool");
        hasMethod(c, "getPoolIndex");
        hasMethod(c, "getQueuedTaskCount");
        hasMethod(c, "run");
        try { c.getDeclaredMethod("onStart"); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("onStart expected", e); }
        try { c.getDeclaredMethod("onTermination", Throwable.class); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("onTermination expected", e); }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinWorkerThread wt = pool.getFactory().newThread(pool);
            check(wt instanceof Thread, "extends Thread");
            check(wt.getPool() == pool, "getPool returns the pool");
            check(wt.getPoolIndex() >= -1, "getPoolIndex callable");
            check(wt.getQueuedTaskCount() == 0, "fresh worker has no queued tasks");
            check(wt.isDaemon(), "default worker is a daemon thread");
            check(wt.getName().startsWith("ForkJoinPool-"), "default name prefix: " + wt.getName());
        } finally {
            pool.shutdown();
        }
    }

    // =============== contract additions ===============

    // ---- custom subclass instantiation ----
    static class ProbeWorker extends ForkJoinWorkerThread {
        ProbeWorker(ForkJoinPool p) { super(p); }
    }

    static void testFactoryCreatedWorker() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            ForkJoinWorkerThread mine = new ProbeWorker(pool);
            check(mine.getPool() == pool, "custom subclass bound to the pool");
            check(mine.getPoolIndex() >= 0, "pool index assigned at construction");
            check(mine.isDaemon(), "custom worker created as daemon");
        } finally { pool.shutdownNow(); }
    }

    // ---- identity while running inside a pool task ----
    static void testRunningWorkerIdentity() throws Exception {
        final ForkJoinPool pool = new ForkJoinPool(2);
        try {
            final ForkJoinPool seenPool = pool;
            String info = pool.invoke(new RecursiveTask<String>() {
                protected String compute() {
                    Thread t = Thread.currentThread();
                    if (!(t instanceof ForkJoinWorkerThread)) return "NOT-WORKER";
                    ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
                    return (w.getPool() == seenPool) + "," + t.isDaemon() + ","
                            + t.getName().startsWith("ForkJoinPool-") + "," + (w.getPoolIndex() >= 0)
                            + "," + (w.getQueuedTaskCount() >= 0);
                }
            });
            check(info.startsWith("true,true,"), "running worker: getPool()==pool and daemon");
            check(info.contains(",true,true,"), "default name prefix and poolIndex >= 0 while running");
            check(info.endsWith("true"), "getQueuedTaskCount() >= 0 while running (JDK-27 API)");
            check(!info.equals("NOT-WORKER"), "pool.invoke always executes on a worker thread");
        } finally { pool.shutdownNow(); }
    }

    // ---- custom-named workers via factory ----
    static void testCustomNamedWorkers() throws Exception {
        final AtomicInteger n = new AtomicInteger();
        ForkJoinPool.ForkJoinWorkerThreadFactory fac = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                ForkJoinWorkerThread w = new ForkJoinWorkerThread(p) { };
                w.setName("Cw-" + n.incrementAndGet());
                return w;
            }
        };
        ForkJoinPool pool = new ForkJoinPool(2, fac, null, false);
        try {
            String name = pool.invoke(new RecursiveTask<String>() {
                protected String compute() { return Thread.currentThread().getName(); }
            });
            check(name.startsWith("Cw-"), "task ran on custom-named worker: " + name);
            check(n.get() >= 1, "factory created at least one worker");
        } finally { pool.shutdownNow(); }
    }

    // ---- lifecycle hooks onStart/onTermination ----
    static void testLifecycleHooks() throws Exception {
        final AtomicInteger onStartCalls = new AtomicInteger();
        final AtomicInteger onTermCalls = new AtomicInteger();
        ForkJoinPool.ForkJoinWorkerThreadFactory fac = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                return new ForkJoinWorkerThread(p) {
                    protected void onStart() { onStartCalls.incrementAndGet(); }
                    protected void onTermination(Throwable t) { onTermCalls.incrementAndGet(); }
                };
            }
        };
        ForkJoinPool pool = new ForkJoinPool(2, fac, null, false);
        try {
            pool.invoke(new RecursiveTask<Integer>() {
                protected Integer compute() { return 1; }
            });
            check(onStartCalls.get() >= 1, "onStart invoked when a worker starts running tasks");
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool with hooked workers terminates");
        }
        check(onTermCalls.get() >= 1, "onTermination invoked when workers terminate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryCreatedWorker();
        testRunningWorkerIdentity();
        testCustomNamedWorkers();
        testLifecycleHooks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
