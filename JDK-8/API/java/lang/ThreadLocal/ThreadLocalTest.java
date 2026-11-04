/*
 * @test
 * @summary java.lang.ThreadLocal API conformance: get/set/remove, default null initialValue,
 *          initialValue override, ThreadLocal.withInitial, and per-thread isolation.
 * @run main ThreadLocalTest
 */

public class ThreadLocalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasic() {
        ThreadLocal<String> tl = new ThreadLocal<>();
        check(tl.get() == null, "initial value null by default");
        tl.set("x");
        check("x".equals(tl.get()), "get returns set value");
        tl.remove();
        check(tl.get() == null, "remove clears value");
    }

    static void testInitialValueOverride() {
        ThreadLocal<String> tl = new ThreadLocal<String>() {
            protected String initialValue() { return "init"; }
        };
        check("init".equals(tl.get()), "initialValue override");
    }

    static void testWithInitial() {
        ThreadLocal<Integer> tl = ThreadLocal.withInitial(() -> 42);
        check(tl.get() == 42, "withInitial supplier");
        tl.set(1);
        check(tl.get() == 1, "set overrides initial");
        tl.remove();
        check(tl.get() == 42, "remove re-initializes from supplier");
    }

    static void testThreadIsolation() throws Exception {
        final ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("main");
        final String[] other = new String[1];
        Thread t = new Thread(() -> other[0] = tl.get());
        t.start();
        t.join();
        check("main".equals(tl.get()), "main thread value unchanged");
        check(other[0] == null, "other thread sees null (isolated)");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testInitialValueOverride();
        testWithInitial();
        testThreadIsolation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
