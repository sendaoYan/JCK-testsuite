/*
 * @test
 * @summary java.lang.Thread.Builder.OfPlatform conformance: white-box structure + black-box behavior.
 * @run main OfPlatformTest
 */
import java.lang.reflect.Method;

public class OfPlatformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Thread.Builder.OfPlatform.class;
        check(c.isInterface(), "OfPlatform is an interface");
        check(Thread.Builder.class.isAssignableFrom(c), "extends Thread.Builder");
        check(c.getMethod("name", String.class).getReturnType() == Thread.Builder.OfPlatform.class,
              "covariant name(String) returns OfPlatform");
        check(c.getMethod("group", ThreadGroup.class).getReturnType() == Thread.Builder.OfPlatform.class,
              "group(ThreadGroup) returns OfPlatform");
        check(c.getMethod("daemon", boolean.class).getReturnType() == Thread.Builder.OfPlatform.class,
              "daemon(boolean) returns OfPlatform");
        check(c.getMethod("daemon").getReturnType() == Thread.Builder.OfPlatform.class,
              "daemon() returns OfPlatform (default method)");
        check(c.getMethod("daemon").isDefault(), "daemon() is a default method");
        check(c.getMethod("priority", int.class).getReturnType() == Thread.Builder.OfPlatform.class,
              "priority(int) returns OfPlatform");
        check(c.getMethod("stackSize", long.class).getReturnType() == Thread.Builder.OfPlatform.class,
              "stackSize(long) returns OfPlatform");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPlatformOptions() throws Exception {
        ThreadGroup g = new ThreadGroup("ofplatform-group");
        Thread t = Thread.ofPlatform().group(g).name("plat").daemon(true)
                .priority(Thread.NORM_PRIORITY).stackSize(0L).start(() -> { });
        check("plat".equals(t.getName()), "name applied");
        check(t.getThreadGroup() == g, "group applied");
        check(t.isDaemon(), "daemon(true) applied");
        check(!t.isVirtual(), "platform thread is not virtual");
        check(t.getPriority() == Thread.NORM_PRIORITY, "priority applied");
        t.join();
        check(t.getThreadGroup() == null || !t.isAlive(), "group cleared after termination");
    }

    static void testDaemonDefault() throws Exception {
        Thread u = Thread.ofPlatform().daemon().unstarted(() -> { });
        check(u.isDaemon(), "daemon() defaults to true");
        Thread v = Thread.ofPlatform().daemon(false).unstarted(() -> { });
        check(!v.isDaemon(), "daemon(false) explicit");
    }

    static void testFactory() throws Exception {
        java.util.concurrent.ThreadFactory f = Thread.ofPlatform().name("pf-", 100).factory();
        Thread t = f.newThread(() -> { });
        check(t != null && t.getState() == Thread.State.NEW, "factory yields unstarted platform threads");
        check("pf-100".equals(t.getName()), "factory threads use the name prefix and counter");
        t.start(); t.join();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPlatformOptions();
        testDaemonDefault();
        testFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
