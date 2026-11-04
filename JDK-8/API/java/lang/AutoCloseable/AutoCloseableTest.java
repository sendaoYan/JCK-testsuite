/*
 * @test
 * @summary java.lang.AutoCloseable API conformance: try-with-resources closes resources in
 *          reverse declaration order, close() runs exactly once, and an exception thrown
 *          from close() propagates.
 * @run main AutoCloseableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoCloseableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final List<String> log = new ArrayList<>();

    static class Resource implements AutoCloseable {
        final String name;
        Resource(String name) { this.name = name; }
        public void close() throws Exception { log.add(name); }
    }

    static void testCloseOrderAndOnce() throws Exception {
        log.clear();
        try (Resource a = new Resource("a"); Resource b = new Resource("b")) {
            check(log.isEmpty(), "nothing closed before block ends");
        }
        check(log.equals(Arrays.asList("b", "a")), "close reverse declaration order");
        check(log.size() == 2, "each closed once");
    }

    static class ThrowingResource implements AutoCloseable {
        public void close() throws java.io.IOException { throw new java.io.IOException("boom"); }
    }

    static void testCloseThrows() {
        try (ThrowingResource r = new ThrowingResource()) {
            // body runs before close()
        } catch (java.io.IOException e) {
            check("boom".equals(e.getMessage()), "close() exception propagates");
            return;
        }
        throw new AssertionError("expected IOException from close()");
    }

    public static void main(String[] args) throws Exception {
        testCloseOrderAndOnce();
        testCloseThrows();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
