/*
 * @test
 * @summary java.lang.Thread.Builder.OfVirtual conformance: white-box structure + black-box behavior.
 * @run main OfVirtualTest
 */
import java.lang.reflect.Method;

public class OfVirtualTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Thread.Builder.OfVirtual.class;
        check(c.isInterface(), "OfVirtual is an interface");
        check(Thread.Builder.class.isAssignableFrom(c), "extends Thread.Builder");
        check(c.getMethod("name", String.class).getReturnType() == Thread.Builder.OfVirtual.class,
              "covariant name(String) returns OfVirtual");
        check(c.getMethod("name", String.class, long.class).getReturnType() == Thread.Builder.OfVirtual.class,
              "covariant name(String, long) returns OfVirtual");
        check(c.getMethod("uncaughtExceptionHandler", Thread.UncaughtExceptionHandler.class)
              .getReturnType() == Thread.Builder.OfVirtual.class, "covariant uncaughtExceptionHandler");
        // No platform-only methods on OfVirtual.
        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() == c) {
                String n = m.getName();
                check(!"group".equals(n) && !"priority".equals(n)
                      && !"stackSize".equals(n) && !"daemon".equals(n),
                      "OfVirtual has no platform-only option: " + n);
            }
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testVirtualThread() throws Exception {
        Thread t = Thread.ofVirtual().name("virt").start(() -> { });
        check(t.isVirtual(), "created thread is virtual");
        check("virt".equals(t.getName()), "name applied");
        check(t.isDaemon(), "virtual threads are always daemons");
        check(t.getThreadGroup().getName().equals("VirtualThreads"),
              "virtual threads belong to the VirtualThreads group");
        t.join();
        check(t.getState() == Thread.State.TERMINATED, "terminated after join");
    }

    static void testUnstartedAndFactory() throws Exception {
        Thread u = Thread.ofVirtual().name("v").unstarted(() -> { });
        check(u.getState() == Thread.State.NEW, "unstarted virtual thread is NEW");
        check(u.isVirtual(), "unstarted virtual thread is virtual");
        u.start(); u.join();
        java.util.concurrent.ThreadFactory f = Thread.ofVirtual().name("vf-", 3).factory();
        Thread a = f.newThread(() -> { });
        check("vf-3".equals(a.getName()), "factory uses the prefix and counter");
        check(a.isVirtual(), "factory produces virtual threads");
        a.start(); a.join();
        check("vf-4".equals(f.newThread(() -> { }).getName()), "counter advances");
    }

    static void testUncaughtExceptionHandler() throws Exception {
        final Thread[] seenThread = {null};
        final Throwable[] seenThrowable = {null};
        Thread t = Thread.ofVirtual()
                .uncaughtExceptionHandler((th, ex) -> { seenThread[0] = th; seenThrowable[0] = ex; })
                .start(() -> { throw new UnsupportedOperationException("vboom"); });
        t.join();
        check(seenThread[0] == t, "handler observed the dying virtual thread");
        check(seenThrowable[0] instanceof UnsupportedOperationException
              && "vboom".equals(seenThrowable[0].getMessage()), "handler observed the throwable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testVirtualThread();
        testUnstartedAndFactory();
        testUncaughtExceptionHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
