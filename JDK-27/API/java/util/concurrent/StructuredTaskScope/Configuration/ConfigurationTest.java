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

    // ---- Part 3: contract deep-dive ----
    static void testWitherSemantics() throws Exception {
        final StructuredTaskScope.Configuration[] cfgs = new StructuredTaskScope.Configuration[3];
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> {
                     cfgs[0] = cfg;
                     cfgs[1] = cfg.withName("n1");
                     cfgs[2] = cfg.withTimeout(Duration.ofSeconds(5));
                     return cfg.withName("n2").withTimeout(Duration.ofSeconds(5))
                               .withThreadFactory(r -> new Thread(r, "cfg-t"));
                 })) {
            scope.fork(() -> "x");
            scope.join();
        }
        check(cfgs[0] != null, "open passes the default Configuration to the function");
        check(cfgs[1] != cfgs[0], "withName returns a distinct Configuration (immutable wither)");
        check(cfgs[2] != cfgs[0], "withTimeout returns a distinct Configuration");
        check(cfgs[1] != cfgs[2], "different withers return different instances");
    }

    static void testThreadFactoryConfiguration() throws Exception {
        final java.util.List<String> names =
                java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        final boolean[] platform = new boolean[1];
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> cfg.withThreadFactory(r -> {
                     Thread t = new Thread(r, "cfg-factory");
                     t.setDaemon(true);
                     return t;
                 }))) {
            scope.fork(() -> {
                names.add(Thread.currentThread().getName());
                platform[0] = !Thread.currentThread().isVirtual();
                return "x";
            });
            scope.join();
        }
        check(names.size() == 1 && "cfg-factory".equals(names.get(0)),
                "subtask ran on the factory thread: " + names);
        check(platform[0], "custom factory produces platform threads (vs default virtual threads)");
    }

    static void testTimeoutConfiguration() throws Exception {
        // a configured timeout actually fires: join throws with CancelledByTimeoutException cause
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> cfg.withTimeout(Duration.ofMillis(250)))) {
            scope.fork(() -> { Thread.sleep(30_000); return "slow"; });
            try {
                scope.join();
                check(false, "timeout-configured join should throw");
            } catch (ExecutionException ee) {
                check(ee.getCause() instanceof StructuredTaskScope.CancelledByTimeoutException,
                        "withTimeout join fails with CancelledByTimeoutException cause");
            }
            check(scope.isCancelled(), "scope cancelled after configured timeout");
        }
        // a generous timeout never fires
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> cfg.withTimeout(Duration.ofSeconds(30)))) {
            scope.fork(() -> "fast");
            check(scope.join() == null, "generous timeout does not disturb a fast scope");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWitherSemantics();
        testThreadFactoryConfiguration();
        testTimeoutConfiguration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
