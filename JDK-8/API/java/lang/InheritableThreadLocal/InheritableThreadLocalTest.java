/*
 * @test
 * @summary java.lang.InheritableThreadLocal API conformance: extends ThreadLocal, child
 *          threads inherit the parent's value at creation time, and childValue override
 *          transforms the inherited value.
 * @run main InheritableThreadLocalTest
 */

public class InheritableThreadLocalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInheritance() throws Exception {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("parent");
        final String[] childVal = {null};
        Thread t = new Thread(() -> childVal[0] = itl.get());
        t.start();
        t.join();
        check("parent".equals(childVal[0]), "child inherits parent's value");
        check("parent".equals(itl.get()), "parent value unchanged");
    }

    static void testChildValueOverride() throws Exception {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            protected String childValue(String parentValue) { return parentValue + "-child"; }
        };
        itl.set("p");
        final String[] childVal = {null};
        Thread t = new Thread(() -> childVal[0] = itl.get());
        t.start();
        t.join();
        check("p-child".equals(childVal[0]), "childValue transforms parent value");
    }

    static void testIsThreadLocal() {
        check(new InheritableThreadLocal<>() instanceof ThreadLocal,
              "InheritableThreadLocal extends ThreadLocal");
    }

    public static void main(String[] args) throws Exception {
        testIsThreadLocal();
        testInheritance();
        testChildValueOverride();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
