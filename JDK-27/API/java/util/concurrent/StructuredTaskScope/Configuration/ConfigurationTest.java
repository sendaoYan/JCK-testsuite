/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$Configuration conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main ConfigurationTest
 */

import java.time.*;
import java.util.concurrent.*;

public class ConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$Configuration");
        check(c.isInterface(), "Configuration is an interface");
        hasMethod(c, "withThreadFactory", ThreadFactory.class);
        hasMethod(c, "withName", String.class);
        hasMethod(c, "withTimeout", Duration.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final StructuredTaskScope.Configuration[] seen = new StructuredTaskScope.Configuration[1];
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> {
                     seen[0] = cfg.withName("test")
                                  .withTimeout(Duration.ofSeconds(5))
                                  .withThreadFactory(r -> new Thread(r));
                     return seen[0];
                 })) {
            scope.fork(() -> "x");
            scope.join();
        }
        check(seen[0] != null, "withName/withTimeout/withThreadFactory fluent chain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
