/*
 * @test
 * @summary java.util.concurrent.ForkJoinWorkerThread API conformance: getPool/thread identity.
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

    static void testForkJoinWorkerThread() {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinWorkerThread wt = pool.getFactory().newThread(pool);
            check(wt instanceof Thread, "extends Thread");
            check(wt.getPool() == pool, "getPool returns the pool");
        } finally {
            pool.shutdown();
        }
    }

    // =============== contract additions ===============

    // ---- Part A: white-box structure ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ForkJoinWorkerThread");
        check(!c.isInterface(), "ForkJoinWorkerThread is a class");
        check(c.getSuperclass() == Thread.class, "extends Thread");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(ForkJoinPool.class);
        check(!java.lang.reflect.Modifier.isPrivate(ctor.getModifiers()),
              "protected (ForkJoinPool) constructor available to subclasses");
        try { c.getMethod("getPool"); passed++; } catch (NoSuchMethodException e) { throw new AssertionError(e); }
        try { c.getMethod("getPoolIndex"); passed++; } catch (NoSuchMethodException e) { throw new AssertionError(e); }
        try { c.getMethod("run"); passed++; } catch (NoSuchMethodException e) { throw new AssertionError(e); }
        // JDK-8 surface: onStart/onTermination were backported to 8u and are invoked;
        // getQueuedTaskCount remains absent (JDK-9+ only)
        boolean hasQtc = true;
        try { c.getMethod("getQueuedTaskCount"); } catch (NoSuchMethodException e) { hasQtc = false; }
        check(!hasQtc, "JDK-8 ForkJoinWorkerThread has no getQueuedTaskCount (JDK-9+ only)");
        try { c.getDeclaredMethod("onStart"); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("onStart expected on 8u372", e); }
        try { c.getDeclaredMethod("onTermination", Throwable.class); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("onTermination expected on 8u372", e); }
    }

    // ---- Part E: lifecycle hooks (backported to 8u, invoked by the pool) ----
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

    // ---- Part B: factory-created (not yet started) worker ----
    static void testFactoryCreatedWorker() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            ForkJoinWorkerThread wt = pool.getFactory().newThread(pool);
            check(wt.getPool() == pool, "bound to the pool at construction");
            check(wt.getName().startsWith("ForkJoinPool-"), "default name prefix: " + wt.getName());
            check(wt.isDaemon(), "worker thread created as daemon");
            check(wt.getPoolIndex() >= 0, "pool index assigned at construction");
            // a custom subclass can also be instantiated directly
            ForkJoinWorkerThread mine = new ProbeWorker(pool);
            check(mine.getPool() == pool, "custom subclass bound to the pool");
        } finally { pool.shutdownNow(); }
    }

    static class ProbeWorker extends ForkJoinWorkerThread {
        ProbeWorker(ForkJoinPool p) { super(p); }
    }

    // ---- Part C: identity while running inside a pool task ----
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
                            + t.getName().startsWith("ForkJoinPool-") + "," + (w.getPoolIndex() >= 0);
                }
            });
            check(info.startsWith("true,true,"), "running worker: getPool()==pool and daemon");
            check(info.endsWith("true,true"), "running worker: default name prefix and poolIndex >= 0");
            check(!info.equals("NOT-WORKER"), "pool.invoke always executes on a worker thread");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part D: custom-named workers via factory ----
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

    public static void main(String[] args) throws Exception {
        testForkJoinWorkerThread();
        testWhiteBoxStructure();
        testFactoryCreatedWorker();
        testRunningWorkerIdentity();
        testCustomNamedWorkers();
        testLifecycleHooks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
