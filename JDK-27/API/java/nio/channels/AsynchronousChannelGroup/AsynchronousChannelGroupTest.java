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
        testFixedPoolStateContract();
        testShutdownNowContract();
        testExecutorBackedGroupsContract();
        testSurfaceContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== A7 batch: deepened contract coverage =====================

    static void checkThrows(Class<? extends Throwable> exc, Runnable action, String msg) {
        try {
            action.run();
            check(false, msg + ": expected " + exc.getSimpleName());
        } catch (Throwable t) {
            check(exc.isInstance(t), msg + ": expected " + exc.getSimpleName() + " got " + t);
        }
    }

    static java.util.concurrent.ThreadFactory a7DaemonFactory(final String name) {
        return new java.util.concurrent.ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            }
        };
    }

    static void testFixedPoolStateContract() throws Exception {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(2,
                a7DaemonFactory("a7-fixed"));
        try {
            check(!group.isShutdown(), "active group not shutdown");
            check(!group.isTerminated(), "active group not terminated");
            check(group.provider() != null, "group provider non-null");
            check(group.provider() == java.nio.channels.spi.AsynchronousChannelProvider.provider(),
                  "group provider is the default provider");
            // bounded awaitTermination on a live group returns false
            check(!group.awaitTermination(150, java.util.concurrent.TimeUnit.MILLISECONDS),
                  "live group does not terminate within 150ms");
            // channels created through the group work end to end
            java.nio.channels.AsynchronousServerSocketChannel server =
                    java.nio.channels.AsynchronousServerSocketChannel.open(group);
            try {
                server.bind(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 0));
                java.nio.channels.AsynchronousSocketChannel client =
                        java.nio.channels.AsynchronousSocketChannel.open(group);
                try {
                    check(server.provider() == group.provider(), "server wired to group provider");
                    check(client.provider() == group.provider(), "client wired to group provider");
                    client.connect(server.getLocalAddress())
                            .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    java.nio.channels.AsynchronousSocketChannel accepted =
                            server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                    client.write(java.nio.ByteBuffer.wrap(new byte[] {33}))
                            .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    java.nio.ByteBuffer b = java.nio.ByteBuffer.allocate(1);
                    check(accepted.read(b).get(10, java.util.concurrent.TimeUnit.SECONDS) == 1
                          && b.get(0) == 33, "group channels transfer data");
                    accepted.close();
                    // graceful shutdown: existing channels stay usable, group stays alive
                    group.shutdown();
                    check(group.isShutdown(), "shutdown() marks the group");
                    check(!group.isTerminated(), "group alive while channels are open");
                    client.write(java.nio.ByteBuffer.wrap(new byte[] {34}))
                            .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    check(true, "channel usable after graceful group shutdown");
                } finally {
                    client.close();
                }
            } finally {
                server.close();
            }
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "group terminates once its channels are closed");
            check(group.isTerminated(), "isTerminated after awaitTermination");
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.MILLISECONDS),
                  "awaitTermination on terminated group returns true immediately");
            group.shutdown(); // idempotent
            check(true, "shutdown on a shutdown group is allowed");
        } finally {
            group.shutdownNow();
        }
        // fixed pool size must be positive
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() {
                try { AsynchronousChannelGroup.withFixedThreadPool(0, a7DaemonFactory("bad0")); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "withFixedThreadPool(0, factory)");
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() {
                try { AsynchronousChannelGroup.withFixedThreadPool(-1, a7DaemonFactory("badneg")); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "withFixedThreadPool(-1, factory)");
    }

    static void testShutdownNowContract() throws Exception {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(1,
                a7DaemonFactory("a7-now"));
        java.nio.channels.AsynchronousSocketChannel ch = java.nio.channels.AsynchronousSocketChannel.open(group);
        check(ch.isOpen(), "group channel open before shutdownNow");
        group.shutdownNow();
        check(group.isShutdown(), "isShutdown after shutdownNow");
        // shutdownNow forcibly closes the group's channels
        check(!ch.isOpen(), "shutdownNow closes the group's channels");
        checkThrows(java.nio.channels.ShutdownChannelGroupException.class, new Runnable() {
            public void run() {
                try { java.nio.channels.AsynchronousServerSocketChannel.open(group); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "open server on shutdown group");
        checkThrows(java.nio.channels.ShutdownChannelGroupException.class, new Runnable() {
            public void run() {
                try { java.nio.channels.AsynchronousSocketChannel.open(group); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "open socket on shutdown group");
        check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS), "terminates after shutdownNow");
        check(group.isTerminated(), "isTerminated after shutdownNow");
        group.shutdownNow(); // idempotent
        check(true, "shutdownNow twice is allowed");
        // opening a channel with a group from a foreign provider is rejected
        final java.nio.channels.spi.AsynchronousChannelProvider def =
                java.nio.channels.spi.AsynchronousChannelProvider.provider();
        class ForeignProvider extends java.nio.channels.spi.AsynchronousChannelProvider {
            public AsynchronousChannelGroup openAsynchronousChannelGroup(int n,
                    java.util.concurrent.ThreadFactory f) throws java.io.IOException {
                return def.openAsynchronousChannelGroup(n, f);
            }
            public AsynchronousChannelGroup openAsynchronousChannelGroup(
                    java.util.concurrent.ExecutorService e, int s) throws java.io.IOException {
                return def.openAsynchronousChannelGroup(e, s);
            }
            public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
                    AsynchronousChannelGroup g) throws java.io.IOException {
                return def.openAsynchronousServerSocketChannel(g);
            }
            public java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(
                    AsynchronousChannelGroup g) throws java.io.IOException {
                return def.openAsynchronousSocketChannel(g);
            }
        }
        final ForeignProvider foreign = new ForeignProvider();
        class ForeignGroup extends AsynchronousChannelGroup {
            ForeignGroup() { super(foreign); }
            public boolean isShutdown() { return false; }
            public boolean isTerminated() { return false; }
            public void shutdown() { }
            public void shutdownNow() { }
            public boolean awaitTermination(long t, java.util.concurrent.TimeUnit u) { return true; }
        }
        AsynchronousChannelGroup fg = new ForeignGroup();
        check(fg.provider() == foreign, "custom group exposes its provider");
        checkThrows(java.nio.channels.IllegalChannelGroupException.class, new Runnable() {
            public void run() {
                try { java.nio.channels.AsynchronousSocketChannel.open(fg); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "open socket on a group from a foreign provider");
        checkThrows(java.nio.channels.IllegalChannelGroupException.class, new Runnable() {
            public void run() {
                try { java.nio.channels.AsynchronousServerSocketChannel.open(fg); }
                catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
        }, "open server on a group from a foreign provider");
    }

    static void testExecutorBackedGroupsContract() throws Exception {
        // withCachedThreadPool built on a caller-owned daemon pool
        java.util.concurrent.ExecutorService cached =
                java.util.concurrent.Executors.newCachedThreadPool(a7DaemonFactory("a7-cached"));
        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(cached, 1);
            try {
                check(!group.isShutdown(), "cached group active");
                check(group.provider() == java.nio.channels.spi.AsynchronousChannelProvider.provider(),
                      "cached group default provider");
                java.nio.channels.AsynchronousSocketChannel ch =
                        java.nio.channels.AsynchronousSocketChannel.open(group);
                check(ch.provider() == group.provider(), "cached group channel provider");
                ch.close();
            } finally {
                group.shutdown();
                check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                      "cached group terminates after shutdown");
                check(group.isTerminated(), "cached group terminated");
            }
        } finally {
            cached.shutdownNow();
        }
        // withThreadPool: the group wraps the caller's executor
        java.util.concurrent.ExecutorService single =
                java.util.concurrent.Executors.newSingleThreadExecutor(a7DaemonFactory("a7-wtp"));
        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(single);
            check(!group.isShutdown(), "wrapped group active");
            java.nio.channels.AsynchronousSocketChannel ch =
                    java.nio.channels.AsynchronousSocketChannel.open(group);
            check(ch.isOpen(), "wrapped group channel opens");
            ch.close();
            group.shutdown();
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "wrapped group terminates");
            check(group.isTerminated(), "wrapped group terminated");
        } finally {
            single.shutdownNow();
        }
    }

    static void testSurfaceContract() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = AsynchronousChannelGroup.class.getDeclaredConstructors();
        for (java.lang.reflect.Constructor<?> c : ctors)
            check(java.lang.reflect.Modifier.isProtected(c.getModifiers()),
                  "group constructor is protected");
        for (String n : new String[] {"withFixedThreadPool", "withCachedThreadPool", "withThreadPool",
                "isShutdown", "isTerminated", "shutdown", "shutdownNow", "awaitTermination", "provider"})
            check(hasMethod(AsynchronousChannelGroup.class, n), "has method " + n);
        check(java.lang.reflect.Modifier.isAbstract(AsynchronousChannelGroup.class.getModifiers()),
              "AsynchronousChannelGroup is abstract");
        boolean noWorkStealing = !hasMethod(AsynchronousChannelGroup.class, "withWorkStealing");
        check(noWorkStealing, "no withWorkStealing factory in the public API");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }


}
