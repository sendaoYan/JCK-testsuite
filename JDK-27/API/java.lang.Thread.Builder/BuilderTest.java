/*
 * @test
 * @summary java.lang.Thread.Builder conformance: interface and platform/virtual builders.
 * @run main BuilderTest
 */
import java.util.concurrent.ThreadFactory;

public class BuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(Thread.Builder.class.isInterface(), "is interface");
        Thread.Builder b = Thread.ofPlatform();
        check(b != null, "ofPlatform returns Builder");
        Thread t = b.name("builder-main").start(() -> {});
        check("builder-main".equals(t.getName()), "name(String) start");
        t.join();
        Thread u = Thread.ofPlatform().unstarted(() -> {});
        check(u.getState() == Thread.State.NEW, "unstarted NEW");
        check(Thread.ofPlatform().factory() instanceof ThreadFactory, "factory");
        Thread v = Thread.ofVirtual().name("v").start(() -> {});
        check(v.isVirtual(), "ofVirtual start");
        v.join();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
