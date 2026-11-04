/*
 * @test
 * @summary java.lang.ThreadLocal conformance: white-box structure + black-box behavior.
 * @run main ThreadLocalTest
 */

import java.lang.reflect.Method;

public class ThreadLocalTest {
    static int passed;

    // Exposes the protected initialValue() so we can assert the default null return.
    static class DefaultTL extends ThreadLocal<String> {
        String init() { return initialValue(); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadLocal");
        check(!c.isInterface(), "ThreadLocal is a class");
        check(c.getSuperclass() == Object.class, "ThreadLocal extends Object");
        Method[] ms = c.getDeclaredMethods();
        check(has(ms, "get") && has(ms, "set") && has(ms, "remove"), "get/set/remove present");
    }

    static boolean has(Method[] ms, String name) {
        for (Method m : ms) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
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
        t.setDaemon(true);
        t.start();
        t.join();
        check("main".equals(tl.get()), "main thread value unchanged");
        check(other[0] == null, "other thread sees null (isolated)");
    }

    // ---- Part 2b: black-box for the default initialValue() ----
    static void testBlackBox() {
        check(new DefaultTL().init() == null, "default initialValue returns null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasic();
        testInitialValueOverride();
        testWithInitial();
        testThreadIsolation();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
