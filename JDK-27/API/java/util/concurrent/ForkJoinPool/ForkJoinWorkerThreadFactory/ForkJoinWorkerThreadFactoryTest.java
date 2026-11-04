/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory conformance: white-box structure + black-box behavior.
 * @run main ForkJoinWorkerThreadFactoryTest
 */

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ForkJoinWorkerThreadFactoryTest {
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
        Class<?> c = cls("java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory");
        check(c.isInterface(), "ForkJoinWorkerThreadFactory is an interface");
        hasMethod(c, "newThread", ForkJoinPool.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool.getFactory();
            ForkJoinWorkerThread t = factory.newThread(pool);
            check(t instanceof Thread, "newThread returns a Thread");
            check(t.getPool() == pool, "getPool returns the pool");
            check(t.getUncaughtExceptionHandler() != null, "has an uncaught exception handler");
        } finally {
            pool.shutdown();
        }
    }

    // =============== contract additions ===============

    // ---- Part A: full white-box surface ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory");
        java.lang.reflect.Method[] ms = c.getMethods();
        int abstractCount = 0;
        for (java.lang.reflect.Method m : ms) {
            if (!m.isDefault() && java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 1, "exactly one abstract method (functional interface)");
        java.lang.reflect.Method nt = c.getMethod("newThread", ForkJoinPool.class);
        check(nt.getReturnType() == ForkJoinWorkerThread.class, "newThread returns ForkJoinWorkerThread");
        check(!nt.isDefault(), "newThread is not a default method");
        Object def = ForkJoinPool.class.getField("defaultForkJoinWorkerThreadFactory").get(null);
        check(def != null, "static defaultForkJoinWorkerThreadFactory non-null");
        check(c.isInstance(def), "default factory implements the interface");
        check(ForkJoinPool.defaultForkJoinWorkerThreadFactory == def,
              "field value identical through direct constant access");
    }

    // ---- Part B: default factory behavior ----
    static void testDefaultFactory() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            check(t != null, "default factory creates a thread");
            check(t.getPool() == pool, "created thread bound to the pool");
            check(t.isDaemon(), "factory-created worker is a daemon thread");
            check(t.getName().startsWith("ForkJoinPool-"), "default worker name prefix: " + t.getName());
            check(t.getPoolIndex() >= 0, "pool index assigned at creation");
            // documented: implementations throw NPE for a null pool
            try {
                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(null);
                check(false, "default factory must reject a null pool");
            } catch (NullPointerException e) {
                check(true, "default factory newThread(null) -> NullPointerException");
            }
        } finally { pool.shutdownNow(); }
    }

    // ---- Part C: custom recording factory drives a real pool ----
    static class RecordingWorker extends ForkJoinWorkerThread {
        RecordingWorker(ForkJoinPool p) { super(p); }
    }

    static void testCustomFactory() throws Exception {
        final AtomicInteger created = new AtomicInteger();
        final CopyOnWriteArrayList<Thread> made = new CopyOnWriteArrayList<Thread>();
        ForkJoinPool.ForkJoinWorkerThreadFactory fac = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                RecordingWorker w = new RecordingWorker(p);
                w.setName("RecWorker-" + created.incrementAndGet());
                made.add(w);
                return w;
            }
        };
        Thread.UncaughtExceptionHandler ueh = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) { }
        };
        ForkJoinPool pool = new ForkJoinPool(2, fac, ueh, false);
        try {
            check(pool.getFactory() == fac, "getFactory returns the custom factory");
            final ForkJoinPool seen = pool;
            String info = pool.invoke(new RecursiveTask<String>() {
                protected String compute() {
                    Thread t = Thread.currentThread();
                    RecordingWorker w = (RecordingWorker) t;
                    return t.getName() + "|" + (w.getPool() == seen) + "|" + t.isDaemon();
                }
            });
            check(info.startsWith("RecWorker-"), "task ran on a factory-made worker: " + info);
            check(info.endsWith("|true|true"), "worker sees its pool and is daemon");
            check(created.get() >= 1, "factory invoked at least once");
            check(!made.isEmpty() && made.get(0) instanceof RecordingWorker,
                  "factory-produced threads are of the custom subclass");
            check(!made.isEmpty() && made.get(0).getUncaughtExceptionHandler() == ueh,
                  "pool handler installed on the created thread");
            check(!made.isEmpty() && made.get(0).isDaemon(), "created worker is daemon");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part D: factory returning null cannot run tasks ----
    static void testNullReturningFactory() throws Exception {
        ForkJoinPool.ForkJoinWorkerThreadFactory nullFac = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            public ForkJoinWorkerThread newThread(ForkJoinPool p) { return null; }
        };
        ForkJoinPool pool = new ForkJoinPool(1, nullFac, null, false);
        final AtomicBoolean ran = new AtomicBoolean(false);
        pool.execute(new java.util.concurrent.RecursiveAction() {
            protected void compute() { ran.set(true); }
        });
        pool.shutdownNow();
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool with null factory still terminates");
        check(!ran.get(), "task never ran because no worker could be created");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxStructure();
        testDefaultFactory();
        testCustomFactory();
        testNullReturningFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
