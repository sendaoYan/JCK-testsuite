/*
 * @test
 * @summary java.nio.channels.spi.AsynchronousChannelProvider conformance: white-box structure + black-box behavior.
 * @run main AsynchronousChannelProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousChannelProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /**
     * A wildcard bind address (0.0.0.0 or ::) cannot be used as a connect
     * target on Windows; map it to the matching loopback address, keeping the
     * bound port.
     */
    static java.net.InetSocketAddress connectTarget(java.net.InetSocketAddress local)
            throws java.net.UnknownHostException {
        java.net.InetAddress a = local.getAddress();
        if (a != null && a.isAnyLocalAddress()) {
            java.net.InetAddress target = (a instanceof java.net.Inet6Address)
                    ? java.net.InetAddress.getByName("::1")
                    : java.net.InetAddress.getByName("127.0.0.1");
            return new java.net.InetSocketAddress(target, local.getPort());
        }
        return local;
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
        Class<?> c = Class.forName("java.nio.channels.spi.AsynchronousChannelProvider");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousChannelProvider is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"provider", "openAsynchronousChannelGroup",
                "openAsynchronousServerSocketChannel", "openAsynchronousSocketChannel"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AsynchronousChannelProvider p = AsynchronousChannelProvider.provider();
        check(p != null, "provider() returns the system provider");
        check(AsynchronousChannelProvider.provider() == p, "provider() is stable");

        // group factories wired back to the provider
        AsynchronousChannelGroup fixed = p.openAsynchronousChannelGroup(1, daemonFactory("acp-f"));
        try {
            check(fixed.provider() == p, "fixed group provider");
            try (var server = p.openAsynchronousServerSocketChannel(fixed)) {
                check(server.provider() == p, "server provider");
                server.bind(null);
                check(((java.net.InetSocketAddress) server.getLocalAddress()).getPort() > 0,
                      "server from provider binds");
                // accept+connect through provider-created channels
                try (var client = p.openAsynchronousSocketChannel(fixed)) {
                    check(client.provider() == p, "client provider");
                    client.connect(connectTarget((java.net.InetSocketAddress) server.getLocalAddress()))
                          .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    var accepted = server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                    client.write(java.nio.ByteBuffer.wrap(new byte[] {3}))
                          .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1);
                    check(accepted.read(buf).get(10, java.util.concurrent.TimeUnit.SECONDS) == 1,
                          "round trip through provider channels");
                    accepted.close();
                }
            }
        } finally {
            fixed.shutdownNow();
        }

        // cached-pool group factory
        var pool = Executors.newCachedThreadPool(daemonFactory("acp-c"));
        try {
            AsynchronousChannelGroup cached = p.openAsynchronousChannelGroup(pool, 1);
            check(cached.provider() == p, "cached group provider");
            cached.shutdown();
            check(cached.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "cached group terminates");
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSurfaceContract();
        testProviderSingletonContract();
        testRecordingProviderContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static java.util.concurrent.ThreadFactory a7DaemonFactory(final String name) {
        return new java.util.concurrent.ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            }
        };
    }

    static final class RecordingProvider extends AsynchronousChannelProvider {
        int groupCalls, executorGroupCalls, socketCalls, serverCalls;
        public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                int n, java.util.concurrent.ThreadFactory f) throws java.io.IOException {
            groupCalls++;
            return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(n, f);
        }
        public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                java.util.concurrent.ExecutorService e, int s) throws java.io.IOException {
            executorGroupCalls++;
            return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(e, s);
        }
        public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
                java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
            serverCalls++;
            return AsynchronousChannelProvider.provider().openAsynchronousServerSocketChannel(g);
        }
        public java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(
                java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
            socketCalls++;
            return AsynchronousChannelProvider.provider().openAsynchronousSocketChannel(g);
        }
    }

    static Class<?> a7ClassFor(String name) throws ClassNotFoundException {
        if (name.equals("int")) return int.class;
        if (name.equals("long")) return long.class;
        if (name.equals("boolean")) return boolean.class;
        return Class.forName(name);
    }

    static void testSurfaceContract() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(AsynchronousChannelProvider.class.getModifiers()),
              "class is abstract");
        int acpCtors = 0;
        boolean acpProtectedCtor = false;
        for (java.lang.reflect.Constructor<?> c : AsynchronousChannelProvider.class.getDeclaredConstructors()) {
            acpCtors++;
            check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "no public constructor");
            if (java.lang.reflect.Modifier.isProtected(c.getModifiers())) acpProtectedCtor = true;
        }
        check(acpProtectedCtor, "protected constructor present");
        check(acpCtors == 1, "JDK-27 declares the expected constructors, got " + acpCtors);
        java.lang.reflect.Method p = AsynchronousChannelProvider.class.getMethod("provider");
        check(java.lang.reflect.Modifier.isStatic(p.getModifiers()), "provider() is static");
        check(p.getReturnType() == AsynchronousChannelProvider.class, "provider() return type");
        String[][] expect = {
            {"openAsynchronousChannelGroup", "int", "java.util.concurrent.ThreadFactory"},
            {"openAsynchronousChannelGroup", "java.util.concurrent.ExecutorService", "int"},
            {"openAsynchronousSocketChannel", "java.nio.channels.AsynchronousChannelGroup"},
            {"openAsynchronousServerSocketChannel", "java.nio.channels.AsynchronousChannelGroup"},
        };
        int abstractCount = 0;
        for (java.lang.reflect.Method m : AsynchronousChannelProvider.class.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 4, "exactly four abstract methods, got " + abstractCount);
        for (String[] e : expect) {
            Class<?>[] params = new Class<?>[e.length - 1];
            for (int i = 1; i < e.length; i++) params[i - 1] = a7ClassFor(e[i]);
            java.lang.reflect.Method m =
                    AsynchronousChannelProvider.class.getMethod(e[0], params);
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), e[0] + " is abstract");
            check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), e[0] + " is public");
        }
    }

    static void testProviderSingletonContract() {
        AsynchronousChannelProvider a = AsynchronousChannelProvider.provider();
        check(a != null, "provider() non-null");
        check(AsynchronousChannelProvider.provider() == a, "provider() returns the same instance");
        check(AsynchronousChannelProvider.provider() == a, "provider() cached across calls");
        check(a.getClass() != null, "provider has a concrete class");
    }

    static void testRecordingProviderContract() throws Exception {
        RecordingProvider rec = new RecordingProvider();
        check(rec instanceof AsynchronousChannelProvider, "custom provider is an AsynchronousChannelProvider");
        java.nio.channels.AsynchronousChannelGroup group =
                rec.openAsynchronousChannelGroup(1, a7DaemonFactory("a7-acp"));
        try {
            check(rec.groupCalls == 1, "group factory hook recorded");
            check(!group.isShutdown(), "delegated group is live");
            check(group.provider() == AsynchronousChannelProvider.provider(),
                  "delegated group reports the default provider");
            java.nio.channels.AsynchronousServerSocketChannel server =
                    rec.openAsynchronousServerSocketChannel(group);
            java.nio.channels.AsynchronousSocketChannel client =
                    rec.openAsynchronousSocketChannel(group);
            try {
                check(rec.serverCalls == 1 && rec.socketCalls == 1, "open hooks recorded");
                check(server.provider() == AsynchronousChannelProvider.provider(),
                      "server created through the delegation reports the default provider");
                server.bind(new java.net.InetSocketAddress(
                        java.net.InetAddress.getLoopbackAddress(), 0));
                client.connect(server.getLocalAddress())
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel accepted =
                        server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                client.write(java.nio.ByteBuffer.wrap(new byte[] {5}))
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1);
                check(accepted.read(buf).get(10, java.util.concurrent.TimeUnit.SECONDS) == 1
                      && buf.get(0) == 5, "pair created through the provider transfers data");
                accepted.close();
            } finally {
                client.close();
                server.close();
            }
        } finally {
            group.shutdownNow();
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "delegated group terminated");
        }
        // executor-backed group through the recording provider
        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newCachedThreadPool(a7DaemonFactory("a7-acp-exec"));
        try {
            java.nio.channels.AsynchronousChannelGroup g2 =
                    rec.openAsynchronousChannelGroup(pool, 1);
            check(rec.executorGroupCalls == 1, "executor overload recorded");
            check(!g2.isShutdown(), "executor-backed group live");
            g2.shutdown();
            check(g2.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "executor-backed group terminated");
        } finally {
            pool.shutdownNow();
        }
        // a group from a foreign provider is refused by the default delegation
        class ForeignProvider extends AsynchronousChannelProvider {
            public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                    int n, java.util.concurrent.ThreadFactory f) throws java.io.IOException {
                return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(n, f);
            }
            public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                    java.util.concurrent.ExecutorService e, int s) throws java.io.IOException {
                return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(e, s);
            }
            public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
                    java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
                return AsynchronousChannelProvider.provider().openAsynchronousServerSocketChannel(g);
            }
            public java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(
                    java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
                return AsynchronousChannelProvider.provider().openAsynchronousSocketChannel(g);
            }
        }
        final ForeignProvider foreign = new ForeignProvider();
        class ForeignGroup extends java.nio.channels.AsynchronousChannelGroup {
            ForeignGroup() { super(foreign); }
            public boolean isShutdown() { return false; }
            public boolean isTerminated() { return false; }
            public void shutdown() { }
            public void shutdownNow() { }
            public boolean awaitTermination(long t, java.util.concurrent.TimeUnit u) { return true; }
        }
        java.nio.channels.AsynchronousChannelGroup fg = new ForeignGroup();
        boolean rejected = false;
        try {
            rec.openAsynchronousSocketChannel(fg);
        } catch (java.nio.channels.IllegalChannelGroupException e) {
            rejected = true;
        }
        check(rejected, "foreign group rejected with IllegalChannelGroupException");
    }

}
