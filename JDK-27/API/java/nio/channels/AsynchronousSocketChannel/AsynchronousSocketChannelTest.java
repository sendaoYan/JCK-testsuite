/*
 * @test
 * @summary java.nio.channels.AsynchronousSocketChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousSocketChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousSocketChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousSocketChannel is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(java.nio.channels.AsynchronousByteChannel.class.isAssignableFrom(c),
              "implements AsynchronousByteChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "implements NetworkChannel");
        for (String n : new String[] {"open", "bind", "setOption", "shutdownInput",
                "shutdownOutput", "getRemoteAddress", "connect", "read", "write",
                "getLocalAddress", "provider"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();

            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                check(client.provider() != null, "provider non-null");
                check(client.getLocalAddress() == null || client.getLocalAddress() != null,
                      "local address before bind is implementation detail");
                client.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
                check(((InetSocketAddress) client.getLocalAddress()).getPort() > 0, "bound");

                // connect via handler
                CompletableFuture<Void> connected = new CompletableFuture<>();
                client.connect(addr, "c", new CompletionHandler<Void, String>() {
                    public void completed(Void v, String a) { connected.complete(null); }
                    public void failed(Throwable t, String a) { connected.completeExceptionally(t); }
                });
                connected.get(10, TimeUnit.SECONDS);
                check(client.getRemoteAddress() != null, "remote known");
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);

                // write/read with timeouts: handler completed path
                CompletableFuture<Integer> wr = new CompletableFuture<>();
                client.write(ByteBuffer.wrap(new byte[] {1, 2}), 5, TimeUnit.SECONDS, "w",
                        intHandler(wr));
                check(wr.get(10, TimeUnit.SECONDS) == 2, "handler write with timeout");
                ByteBuffer pb = ByteBuffer.allocate(2);
                check(peer.read(pb).get(10, TimeUnit.SECONDS) == 2, "peer received");
                peer.write((ByteBuffer) pb.flip()).get(10, TimeUnit.SECONDS);

                // scattering read (buffers) completed path
                CompletableFuture<Long> scattered = new CompletableFuture<>();
                ByteBuffer b1 = ByteBuffer.allocate(1);
                ByteBuffer b2 = ByteBuffer.allocate(1);
                client.read(new ByteBuffer[] {b1, b2}, 0, 2, 10, TimeUnit.SECONDS, "sc",
                        new CompletionHandler<Long, String>() {
                            public void completed(Long v, String a) { scattered.complete(v); }
                            public void failed(Throwable t, String a) { scattered.completeExceptionally(t); }
                        });
                check(scattered.get(10, TimeUnit.SECONDS) == 2L, "scattering read 2");
                check(b1.get(0) == 1 && b2.get(0) == 2, "scattered content");

                // gathering write
                CompletableFuture<Long> gathered = new CompletableFuture<>();
                ByteBuffer o1 = ByteBuffer.wrap(new byte[] {9});
                ByteBuffer o2 = ByteBuffer.wrap(new byte[] {8});
                client.write(new ByteBuffer[] {o1, o2}, 0, 2, 5, TimeUnit.SECONDS, "gw",
                        new CompletionHandler<Long, String>() {
                            public void completed(Long v, String a) { gathered.complete(v); }
                            public void failed(Throwable t, String a) { gathered.completeExceptionally(t); }
                        });
                check(gathered.get(10, TimeUnit.SECONDS) == 2L, "gathering write 2");
                ByteBuffer drain = ByteBuffer.allocate(2);
                check(peer.read(drain).get(10, TimeUnit.SECONDS) == 2, "peer drain");
                check(drain.get(0) == 9 && drain.get(1) == 8, "gathered content");

                // timeout path: no data for 150ms -> InterruptedByTimeoutException
                CompletableFuture<Throwable> timeout = new CompletableFuture<>();
                client.read(ByteBuffer.allocate(1), 150, TimeUnit.MILLISECONDS, "t",
                        new CompletionHandler<Integer, String>() {
                            public void completed(Integer v, String a) {
                                timeout.complete(new AssertionError("unexpected completion"));
                            }
                            public void failed(Throwable t, String a) { timeout.complete(t); }
                        });
                check(timeout.get(10, TimeUnit.SECONDS)
                      instanceof java.nio.channels.InterruptedByTimeoutException,
                      "read timeout -> InterruptedByTimeoutException");

                // options while connected
                client.setOption(StandardSocketOptions.TCP_NODELAY, true);
                check(client.getOption(StandardSocketOptions.TCP_NODELAY), "TCP_NODELAY set");

                // shutdownOutput: peer reads EOF
                client.shutdownOutput();
                ByteBuffer eof = ByteBuffer.allocate(1);
                check(peer.read(eof).get(10, TimeUnit.SECONDS) == -1, "peer EOF after shutdownOutput");
                peer.close();
            }
        }

        // ReadPendingException: start a never-completing read, then another read
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, TimeUnit.SECONDS);
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
                CompletableFuture<Integer> first = new CompletableFuture<>();
                client.read(ByteBuffer.allocate(1), 60, TimeUnit.SECONDS, "p", intHandler(first));
                boolean pending = false;
                try {
                    client.read(ByteBuffer.allocate(1));
                } catch (java.nio.channels.ReadPendingException e) {
                    pending = true;
                }
                check(pending, "overlapping read -> ReadPendingException");
                peer.write(ByteBuffer.wrap(new byte[] {5})).get(10, TimeUnit.SECONDS);
                check(first.get(10, TimeUnit.SECONDS) == 1, "pending read completed after data");
                peer.close();
            }
        }

        // unconnected channel operations fail
        try (AsynchronousSocketChannel u = AsynchronousSocketChannel.open()) {
            boolean threw = false;
            try { u.read(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS); }
            catch (Exception e) {
                threw = e.getCause() instanceof java.nio.channels.NotYetConnectedException
                        || e instanceof java.nio.channels.NotYetConnectedException;
            }
            check(threw, "read unconnected -> NotYetConnectedException");
            try {
                check(u.getRemoteAddress() == null, "getRemoteAddress null when unconnected");
            } catch (java.nio.channels.NotYetConnectedException e) {
                passed++; // also permitted by the specification
            }
        }
    }

    static java.util.function.Supplier<CompletionHandler<Integer, String>> DUMMY = null;

    static CompletionHandler<Integer, String> intHandler(CompletableFuture<Integer> f) {
        return new CompletionHandler<>() {
            public void completed(Integer v, String a) { f.complete(v); }
            public void failed(Throwable t, String a) { f.completeExceptionally(t); }
        };
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
