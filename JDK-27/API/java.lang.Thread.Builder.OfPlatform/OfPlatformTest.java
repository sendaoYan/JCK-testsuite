/*
 * @test
 * @summary java.lang.Thread.Builder.OfPlatform conformance: interface and platform thread options.
 * @run main OfPlatformTest
 */
public class OfPlatformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(Thread.Builder.OfPlatform.class.isInterface(), "is interface");
        Thread t = Thread.ofPlatform().name("plat").daemon(true).priority(Thread.NORM_PRIORITY).start(() -> {});
        check("plat".equals(t.getName()), "name");
        check(t.isDaemon(), "daemon");
        check(!t.isVirtual(), "not virtual");
        check(t.getPriority() == Thread.NORM_PRIORITY, "priority");
        t.join();
        Thread u = Thread.ofPlatform().daemon().unstarted(() -> {});
        check(u.getState() == Thread.State.NEW, "unstarted NEW");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
