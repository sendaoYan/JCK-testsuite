/*
 * @test
 * @summary java.lang.Thread.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ThreadFactory;

public class BuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Thread.Builder.class;
        check(c.isInterface(), "is interface");
        check(c.getMethod("name", String.class).getReturnType() == Thread.Builder.class,
              "name(String) returns Builder");
        check(c.getMethod("name", String.class, long.class).getReturnType() == Thread.Builder.class,
              "name(String, long) returns Builder");
        check(c.getMethod("inheritInheritableThreadLocals", boolean.class).getReturnType() == Thread.Builder.class,
              "inheritInheritableThreadLocals(boolean) returns Builder");
        check(c.getMethod("uncaughtExceptionHandler", Thread.UncaughtExceptionHandler.class)
              .getReturnType() == Thread.Builder.class, "uncaughtExceptionHandler returns Builder");
        check(c.getMethod("unstarted", Runnable.class).getReturnType() == Thread.class,
              "unstarted(Runnable) returns Thread");
        check(c.getMethod("start", Runnable.class).getReturnType() == Thread.class,
              "start(Runnable) returns Thread");
        check(c.getMethod("factory").getReturnType() == ThreadFactory.class,
              "factory() returns ThreadFactory");
        check(c.getMethod("start", Runnable.class).getParameterTypes()[0] == Runnable.class,
              "start takes a Runnable task");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testStartAndUnstarted() throws Exception {
        Thread t = Thread.ofPlatform().name("builder-main").start(() -> { });
        check("builder-main".equals(t.getName()), "name(String) applies to started thread");
        check(t.isAlive() || t.getState() == Thread.State.TERMINATED,
              "start(Runnable) actually started the thread");
        t.join();
        check(t.getState() == Thread.State.TERMINATED, "terminated after join");
        Thread u = Thread.ofPlatform().unstarted(() -> { });
        check(u.getState() == Thread.State.NEW, "unstarted leaves the thread in NEW");
        u.start();
        u.join();
    }

    static void testNamePrefixCounter() throws Exception {
        ThreadFactory f = Thread.ofPlatform().name("worker-", 7L).factory();
        Thread a = f.newThread(() -> { });
        Thread b = f.newThread(() -> { });
        check("worker-7".equals(a.getName()), "counter starts at the given value");
        check("worker-8".equals(b.getName()), "counter increments per factory thread");
        a.start(); b.start(); a.join(); b.join();
        check("worker-9".equals(f.newThread(() -> { }).getName()), "counter keeps advancing");
    }

    static void testUncaughtExceptionHandler() throws Exception {
        final Thread[] seenThread = {null};
        final Throwable[] seenThrowable = {null};
        Thread t = Thread.ofPlatform().name("boom").daemon(true)
                .uncaughtExceptionHandler((th, ex) -> { seenThread[0] = th; seenThrowable[0] = ex; })
                .start(() -> { throw new IllegalStateException("died"); });
        t.join();
        check(seenThread[0] == t, "handler observed the dying thread");
        check(seenThrowable[0] instanceof IllegalStateException
              && "died".equals(seenThrowable[0].getMessage()), "handler observed the throwable");
    }

    static void testInheritInheritableThreadLocals() throws Exception {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        final String[] inherited = {null};
        try {
            itl.set("parent-value");
            Thread no = Thread.ofPlatform().inheritInheritableThreadLocals(false)
                    .start(() -> inherited[0] = "child:" + itl.get());
            no.join();
            check("child:null".equals(inherited[0]), "inherit(false) drops inheritable values");
            Thread yes = Thread.ofPlatform()
                    .start(() -> inherited[0] = "child:" + itl.get());
            yes.join();
            check("child:parent-value".equals(inherited[0]), "default inherits inheritable values");
        } finally {
            itl.remove();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testStartAndUnstarted();
        testNamePrefixCounter();
        testUncaughtExceptionHandler();
        testInheritInheritableThreadLocals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
