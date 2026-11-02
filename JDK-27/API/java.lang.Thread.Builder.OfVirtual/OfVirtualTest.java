/*
 * @test
 * @summary java.lang.Thread.Builder.OfVirtual conformance: interface and virtual thread options.
 * @run main OfVirtualTest
 */
public class OfVirtualTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(Thread.Builder.OfVirtual.class.isInterface(), "is interface");
        Thread t = Thread.ofVirtual().name("virt").start(() -> {});
        check(t.isVirtual(), "isVirtual");
        check("virt".equals(t.getName()), "name");
        t.join();
        Thread u = Thread.ofVirtual().name("v").unstarted(() -> {});
        check(u.getState() == Thread.State.NEW, "unstarted virtual NEW");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
