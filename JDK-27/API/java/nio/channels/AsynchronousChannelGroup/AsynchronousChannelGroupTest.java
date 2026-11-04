/*
 * @test
 * @summary java.nio.channels.AsynchronousChannelGroup conformance: white-box structure + black-box behavior.
 * @run main AsynchronousChannelGroupTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousChannelGroupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static ThreadFactory daemonFactory(String name) {
        AtomicInteger seq = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, name + "-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousChannelGroup");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousChannelGroup is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"provider", "withFixedThreadPool", "withCachedThreadPool",
                "withThreadPool", "isShutdown", "isTerminated", "shutdown", "shutdownNow",
                "awaitTermination"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fixed thread pool group
        AsynchronousChannelGroup fixed = AsynchronousChannelGroup.withFixedThreadPool(2, daemonFactory("fixed"));
        try {
            check(!fixed.isShutdown(), "group active");
            check(!fixed.isTerminated(), "group not terminated");
            check(fixed.provider() != null, "group provider non-null");
            check(fixed.provider() == java.nio.channels.spi.AsynchronousChannelProvider.provider(),
                  "group provider is the default provider");

            // channels can be created from the group (behavioral smoke)
            try (var server = java.nio.channels.AsynchronousServerSocketChannel.open(fixed)) {
                check(server.provider() == fixed.provider(), "server channel provider");
            }

            // graceful shutdown marks shutdown; termination follows
            fixed.shutdown();
            check(fixed.isShutdown(), "shutdown after shutdown()");
            check(fixed.awaitTermination(10, TimeUnit.SECONDS), "terminates after shutdown");
            check(fixed.isTerminated(), "terminated");
        } finally {
            fixed.shutdownNow();
        }

        // shutdownNow: immediate shutdown, channels can no longer be created
        AsynchronousChannelGroup sn = AsynchronousChannelGroup.withFixedThreadPool(1, daemonFactory("sn"));
        sn.shutdownNow();
        check(sn.isShutdown(), "isShutdown after shutdownNow");
        boolean threw = false;
        try { java.nio.channels.AsynchronousServerSocketChannel.open(sn); }
        catch (java.nio.channels.ShutdownChannelGroupException e) { threw = true; }
        check(threw, "open channel on shutdown group -> ShutdownChannelGroupException");
        check(sn.awaitTermination(10, TimeUnit.SECONDS), "terminated after shutdownNow");
        check(sn.isTerminated(), "isTerminated");

        // cached thread pool group built from an ExecutorService
        var pool = Executors.newCachedThreadPool(daemonFactory("cached"));
        try {
            AsynchronousChannelGroup cached = AsynchronousChannelGroup.withCachedThreadPool(pool, 2);
            try {
                check(!cached.isShutdown(), "cached group active");
                try (var client = java.nio.channels.AsynchronousSocketChannel.open(cached)) {
                    check(client.provider() == cached.provider(), "client provider");
                }
            } finally {
                cached.shutdown();
                check(cached.awaitTermination(10, TimeUnit.SECONDS), "cached group terminates");
            }
        } finally {
            pool.shutdownNow();
        }

        // withThreadPool: group owned by caller-supplied executor; termination after tasks done
        var pool2 = Executors.newSingleThreadExecutor(daemonFactory("wtp"));
        try {
            CountDownLatch ran = new CountDownLatch(1);
            pool2.execute(ran::countDown);
            AsynchronousChannelGroup own = AsynchronousChannelGroup.withThreadPool(pool2);
            check(!own.isShutdown(), "owned group active");
            own.shutdown();
            check(own.awaitTermination(10, TimeUnit.SECONDS), "owned group terminates");
            check(own.isTerminated(), "owned group terminated");
        } finally {
            pool2.shutdownNow();
        }

        // fixed pool size must be positive
        threw = false;
        try { AsynchronousChannelGroup.withFixedThreadPool(0, daemonFactory("bad")); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "withFixedThreadPool(0) -> IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
