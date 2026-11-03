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
                    client.connect(server.getLocalAddress()).get(10, java.util.concurrent.TimeUnit.SECONDS);
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
