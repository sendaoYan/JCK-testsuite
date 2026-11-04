/*
 * @test
 * @summary java.lang.AutoCloseable conformance: white-box structure + black-box behavior
 *          (try-with-resources closes in reverse order, close runs once, close() exception
 *          propagates).
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.AutoCloseable");
        check(c.isInterface(), "AutoCloseable is an interface");
        java.lang.reflect.Method close = c.getMethod("close");
        check(close.getReturnType() == void.class, "close() returns void");
        check(close.getExceptionTypes().length == 1
              && close.getExceptionTypes()[0] == Exception.class,
              "close() throws Exception");
    }

    // ---- Part 2: black-box (behavioral) ----
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

    
    static void testBlackBox() throws Exception {
        testCloseOrderAndOnce();
        testCloseThrows();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
