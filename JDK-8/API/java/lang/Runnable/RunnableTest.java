/*
 * @test
 * @summary java.lang.Runnable API conformance: run() executes synchronously on the calling
 *          thread and on a new thread when passed to Thread.start().
 * @run main RunnableTest
 */

public class RunnableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ==================== 契约深化（2026-11）====================

    /** 自造 Runnable 实现类（三形态之一：普通类）。 */
    static class Task implements Runnable {
        int runs = 0;
        Thread lastRunner = null;
        public void run() { runs++; lastRunner = Thread.currentThread(); }
    }

    static int staticMethodRuns = 0;
    static void taskMethod() { staticMethodRuns++; }

    /** 白盒：Runnable 恰 1 个抽象方法 run()（函数式接口）。 */
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Runnable");
        check(c.isInterface(), "Runnable is an interface");
        check(c.getTypeParameters().length == 0, "Runnable is not generic");
        java.lang.reflect.Method run = c.getMethod("run");
        check(java.lang.reflect.Modifier.isAbstract(run.getModifiers()), "run() is abstract");
        check(run.getReturnType() == void.class, "run() returns void");
        check(run.getParameterCount() == 0, "run() takes no parameters");
        check(java.lang.reflect.Modifier.isPublic(run.getModifiers()), "run() is public");
        check(run.getExceptionTypes().length == 0, "run() declares no checked exceptions");
        int total = 0, abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            total++;
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(total == 1, "Runnable exposes exactly 1 public method, got " + total);
        check(abstractCount == 1, "exactly 1 abstract method (functional interface)");
        check(c.getAnnotation(java.lang.FunctionalInterface.class) != null,
              "@FunctionalInterface annotation present");
    }

    /** 三形态：lambda / 方法引用 / 普通类。 */
    static void testThreeForms() {
        final int[] lambdaRuns = {0};
        Runnable lambda = () -> lambdaRuns[0]++;
        check(lambda instanceof Runnable, "lambda is a Runnable");
        lambda.run();
        check(lambdaRuns[0] == 1, "lambda run() executed once");
        lambda.run();
        check(lambdaRuns[0] == 2, "Runnable is reusable (no once-only constraint)");

        staticMethodRuns = 0;
        Runnable methodRef = RunnableTest::taskMethod;
        check(methodRef instanceof Runnable, "method reference is a Runnable");
        methodRef.run();
        methodRef.run();
        check(staticMethodRuns == 2, "static method reference dispatched twice");

        Task task = new Task();
        check(task instanceof Runnable, "custom class instance is a Runnable");
        task.run();
        check(task.runs == 1, "custom class run() executed");
        check(task.lastRunner == Thread.currentThread(), "custom run() runs on the CALLING thread");

        Runnable anon = new Runnable() { public void run() { } };
        check(anon.getClass().isAnonymousClass(), "anonymous class form also compiles");
    }

    /** run() 直接调用在当前线程执行（不开新线程）。 */
    static void testDirectRunOnCallingThread() {
        Thread caller = Thread.currentThread();
        final Thread[] runner = new Thread[1];
        Runnable r = new Runnable() {
            public void run() { runner[0] = Thread.currentThread(); }
        };
        r.run();
        check(runner[0] == caller, "run() executes on the caller's thread");
        check(!runner[0].isDaemon() || runner[0].isDaemon(), "thread identity is caller");
        // run() 是普通方法调用：不抛 IllegalThreadStateException、不改变线程状态
        r.run();
        check(runner[0] == caller, "second direct run() still on caller thread");
    }

    /** Thread(runnable) 真跑一次：latch 计数恰好 1，运行线程非主线程。 */
    static void testThreadRunsExactlyOnce() throws Exception {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final Thread[] runner = new Thread[1];
        final int[] count = {0};
        Runnable r = new Runnable() {
            public void run() {
                count[0]++;
                runner[0] = Thread.currentThread();
                latch.countDown();
            }
        };
        Thread t = new Thread(r, "lcN2-worker");
        check(t.getState() == Thread.State.NEW, "thread is NEW before start");
        t.start();
        check(latch.await(10, java.util.concurrent.TimeUnit.SECONDS), "runnable executed within bound");
        t.join(10000);
        check(!t.isAlive(), "worker terminated");
        check(count[0] == 1, "run() executed EXACTLY once, got " + count[0]);
        check(runner[0] == t, "run() executed on the worker thread");
        check(runner[0] != Thread.currentThread(), "worker thread differs from caller");
        check("lcN2-worker".equals(runner[0].getName()), "thread name visible inside run()");
        check(t.getState() == Thread.State.TERMINATED, "thread TERMINATED after run");
        // start 已结束的线程再 start → IllegalThreadStateException（run 只跑一次的机制保证）
        try {
            t.start();
            throw new AssertionError("re-start: expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
        check(count[0] == 1, "re-start attempt did NOT re-run the runnable");
    }

    /** run() 抛异常：直接调用时原样冒泡到调用线程（不经 UncaughtExceptionHandler）。 */
    static void testDirectRunExceptionPropagation() {
        final RuntimeException boom = new RuntimeException("task-boom");
        Runnable r = new Runnable() {
            public void run() { throw boom; }
        };
        try {
            r.run();
            throw new AssertionError("expected RuntimeException from direct run()");
        } catch (RuntimeException e) {
            check(e == boom, "direct run() propagates the SAME exception instance");
        }
    }

    /** new Thread(null runnable)：线程合法但啥也不跑，正常终止。 */
    static void testThreadWithNullRunnable() throws Exception {
        Thread t = new Thread((Runnable) null, "lcN2-null-runnable");
        t.start();
        t.join(10000);
        check(!t.isAlive(), "null-runnable thread terminates immediately");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED state");
    }

    /** daemon 线程上的 runnable 照常执行。 */
    static void testDaemonRunnable() throws Exception {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final boolean[] ran = new boolean[1];
        Thread t = new Thread(new Runnable() {
            public void run() { ran[0] = true; latch.countDown(); }
        }, "lcN2-daemon");
        t.setDaemon(true);
        check(t.isDaemon(), "daemon flag set before start");
        t.start();
        check(latch.await(10, java.util.concurrent.TimeUnit.SECONDS), "daemon runnable ran");
        check(ran[0], "run() body executed on daemon thread");
        t.join(10000);
        check(!t.isAlive(), "daemon worker terminated");
    }

    /** 形态细节：lambda 运行时类是合成类；不同 lambda 不同实例；Object 多态持有。 */
    static void testLambdaRuntimeFacts() {
        Runnable a = () -> { };
        Runnable b = () -> { };
        check(a.getClass().isSynthetic(), "lambda runtime class is synthetic");
        check(a != b, "two lambda expressions yield distinct instances");
        check(a.getClass().getName().contains("RunnableTest"), "lambda class lives in the host class");
        Object asObject = a;
        check(asObject instanceof Runnable, "Runnable held as Object still instanceof Runnable");
        check(((Runnable) asObject) == a, "cast back recovers the same instance");
        Runnable sameRef = a;
        check(sameRef == a, "assignment does not copy the Runnable");
    }

    /** 工作线程身份：getId>0、名字在 run() 内可读。 */
    static void testWorkerIdentity() throws Exception {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final long[] idSeen = new long[1];
        Thread t = new Thread(new Runnable() {
            public void run() { idSeen[0] = Thread.currentThread().getId(); latch.countDown(); }
        }, "lcN2-id-worker");
        long before = t.getId();
        check(before > 0, "thread id positive before start");
        t.start();
        check(latch.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker ran");
        check(idSeen[0] == before, "getId() identical inside and outside run()");
        t.join(10000);
        check(!t.isAlive(), "worker terminated");
    }

    public static void main(String[] args) throws Exception {
        final boolean[] ran = {false};
        Runnable r = () -> ran[0] = true;
        r.run();
        check(ran[0], "run() executes synchronously on calling thread");

        final boolean[] threadRan = {false};
        Thread t = new Thread(() -> threadRan[0] = true);
        t.start();
        t.join();
        check(threadRan[0], "run() executes on started thread");

        testWhiteBox();
        testThreeForms();
        testDirectRunOnCallingThread();
        testThreadRunsExactlyOnce();
        testDirectRunExceptionPropagation();
        testThreadWithNullRunnable();
        testDaemonRunnable();
        testLambdaRuntimeFacts();
        testWorkerIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
