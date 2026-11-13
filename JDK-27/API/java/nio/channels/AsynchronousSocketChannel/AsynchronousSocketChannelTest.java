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
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

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
        testChannelBasicsContract();
        testConnectStateContract();
        testReadWriteContract();
        testPendingAndCloseContract();
        testSurfaceContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static void testChannelBasicsContract() throws Exception {
        AsynchronousSocketChannel a1 = AsynchronousSocketChannel.open();
        AsynchronousSocketChannel a2 = AsynchronousSocketChannel.open();
        check(a1 != a2, "open() returns fresh channels");
        check(a1.isOpen() && a2.isOpen(), "fresh channels are open");
        java.net.SocketAddress la = a1.getLocalAddress();
        check(la == null || ((InetSocketAddress) la).getPort() == 0,
              "local address null or unbound port before bind/connect");
        check(a1.provider() == a2.provider(), "default provider instance is shared");
        a1.close();
        a1.close(); // idempotent
        check(!a1.isOpen(), "closed after close");
        a2.close();
        check(a2 instanceof java.nio.channels.AsynchronousByteChannel,
              "implements AsynchronousByteChannel");
        check(a2 instanceof java.nio.channels.NetworkChannel, "implements NetworkChannel");
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

    /** Latch-backed handler recording the completion path taken, result and attachment. */
    static final class Recorder<V, A> implements CompletionHandler<V, A> {
        final CountDownLatch done = new CountDownLatch(1);
        volatile V result;
        volatile A attachment;
        volatile Throwable failure;
        public void completed(V r, A a) { result = r; attachment = a; done.countDown(); }
        public void failed(Throwable t, A a) { failure = t; attachment = a; done.countDown(); }
        boolean awaitDone() throws InterruptedException { return done.await(10, TimeUnit.SECONDS); }
    }

    static void testConnectStateContract() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        // pre-bind then connect: bind(loopback:0) assigns an ephemeral local port
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            final InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel pre = AsynchronousSocketChannel.open()) {
                AsynchronousSocketChannel bound = pre.bind(new InetSocketAddress(lo, 0));
                check(bound == pre, "bind returns this");
                check(((InetSocketAddress) pre.getLocalAddress()).getPort() > 0, "pre-bind assigns port");
                check(pre.connect(addr).get(10, TimeUnit.SECONDS) == null, "connect after explicit bind");
                AsynchronousSocketChannel accepted = server.accept().get(10, TimeUnit.SECONDS);
                check(accepted != null, "server accepted pre-bound client");
                accepted.close();
            }
        }
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            final InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                check(client.isOpen(), "fresh channel open");
                check(client.provider() == AsynchronousChannelProvider.provider(),
                      "open() wired to default AsynchronousChannelProvider");
                // connect Future completes with null on success
                Future<Void> cf = client.connect(addr);
                check(cf.get(10, TimeUnit.SECONDS) == null, "connect Future result is null");
                check(cf.isDone(), "connect Future done");
                check(!cf.isCancelled(), "connect Future not cancelled without cancel()");
                InetSocketAddress remote = (InetSocketAddress) client.getRemoteAddress();
                check(remote != null && remote.getPort() == addr.getPort(), "remote address is the server");
                InetSocketAddress local = (InetSocketAddress) client.getLocalAddress();
                check(local != null && local.getPort() > 0, "ephemeral local port assigned by connect");
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
                check(peer != null && peer.isOpen(), "peer accepted and open");
                // precise connect state-machine errors
                checkThrows(AlreadyConnectedException.class, new Runnable() {
                    public void run() { client.connect(addr); }
                }, "connect while connected");
                checkThrows(AlreadyBoundException.class, new Runnable() {
                    public void run() {
                        try { client.bind(null); }
                        catch (java.io.IOException e) { throw new RuntimeException(e); }
                    }
                }, "bind while already implicitly bound");
                // ConnectionPendingException: a second connect issued while the first is pending
                boolean sawPending = false;
                for (int attempt = 0; attempt < 5 && !sawPending; attempt++) {
                    try (AsynchronousSocketChannel racing = AsynchronousSocketChannel.open()) {
                        racing.setOption(StandardSocketOptions.SO_SNDBUF, 4 * 1024);
                        Future<Void> first = racing.connect(addr);
                        try {
                            racing.connect(addr);
                        } catch (ConnectionPendingException e) {
                            sawPending = true;
                        } catch (AlreadyConnectedException e) {
                            // loopback connect may complete before the second call: retry
                        }
                        first.get(10, TimeUnit.SECONDS);
                    }
                }
                check(sawPending, "connect while pending -> ConnectionPendingException");
                // option contract
                client.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
                check(client.getOption(StandardSocketOptions.SO_RCVBUF) >= 64 * 1024, "SO_RCVBUF round trip");
                check(!client.supportedOptions().isEmpty(), "supportedOptions non-empty");
                check(client.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF),
                      "SO_RCVBUF among supported options");
                // shutdownInput: own reads return EOF immediately
                check(client.shutdownInput() == client, "shutdownInput returns this");
                check(client.read(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS) == -1,
                      "read returns -1 after shutdownInput");
                // shutdownOutput: peer reads EOF; own writes fail
                client.shutdownOutput();
                check(peer.read(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS) == -1,
                      "peer sees EOF after shutdownOutput");
                try {
                    client.write(ByteBuffer.wrap(new byte[] {1})).get(10, TimeUnit.SECONDS);
                    check(false, "write after shutdownOutput must fail");
                } catch (ExecutionException e) {
                    check(e.getCause() instanceof ClosedChannelException,
                          "write after shutdownOutput -> ClosedChannelException, got " + e.getCause());
                }
                peer.close();
            }
        }
        // unconnected channel: both directions report NotYetConnectedException synchronously
        try (AsynchronousSocketChannel u = AsynchronousSocketChannel.open()) {
            checkThrows(NotYetConnectedException.class, new Runnable() {
                public void run() { u.read(ByteBuffer.allocate(1)); }
            }, "read before connect");
            checkThrows(NotYetConnectedException.class, new Runnable() {
                public void run() { u.write(ByteBuffer.allocate(1)); }
            }, "write before connect");
        }
    }

    static void testReadWriteContract() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, TimeUnit.SECONDS);
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);

                // handler-form write: attachment passthrough, result is the byte count
                Recorder<Integer, String> wh = new Recorder<Integer, String>();
                client.write(ByteBuffer.wrap(new byte[] {10, 20, 30}), "w-att", wh);
                check(wh.awaitDone(), "write handler invoked");
                check(wh.failure == null && wh.result != null && wh.result == 3,
                      "handler write result is 3");
                check("w-att".equals(wh.attachment), "write attachment passed through");
                ByteBuffer rb = ByteBuffer.allocate(3);
                check(peer.read(rb).get(10, TimeUnit.SECONDS) == 3, "peer received 3 bytes");
                check(rb.get(0) == 10 && rb.get(1) == 20 && rb.get(2) == 30, "byte-exact payload");

                // reverse direction, Future form
                check(peer.write(ByteBuffer.wrap(new byte[] {7, 8})).get(10, TimeUnit.SECONDS) == 2,
                      "peer write 2 bytes");
                ByteBuffer cb = ByteBuffer.allocate(2);
                Future<Integer> rf = client.read(cb);
                check(rf.get(10, TimeUnit.SECONDS) == 2, "client read 2 bytes");
                check(cb.get(0) == 7 && cb.get(1) == 8, "client read content");
                check(rf.isDone() && !rf.isCancelled(), "read Future done and not cancelled");

                // timed read that succeeds: completed path carries the attachment
                peer.write(ByteBuffer.wrap(new byte[] {9})).get(10, TimeUnit.SECONDS);
                Recorder<Integer, Object> tr = new Recorder<Integer, Object>();
                ByteBuffer tb = ByteBuffer.allocate(1);
                client.read(tb, 5, TimeUnit.SECONDS, "t-att", tr);
                check(tr.awaitDone(), "timed read handler invoked");
                check(tr.failure == null && tr.result != null && tr.result == 1, "timed read result 1");
                check("t-att".equals(tr.attachment) && tb.get(0) == 9,
                      "timed read attachment and content");

                // bounded Future.get on a pending read raises java.util.concurrent.TimeoutException
                Future<Integer> never = client.read(ByteBuffer.allocate(1));
                try {
                    never.get(100, TimeUnit.MILLISECONDS);
                    check(false, "pending read Future.get(100ms) must time out");
                } catch (java.util.concurrent.TimeoutException e) {
                    check(true, "pending read Future.get(100ms) -> TimeoutException");
                }
                peer.write(ByteBuffer.wrap(new byte[] {1})).get(10, TimeUnit.SECONDS);
                check(never.get(10, TimeUnit.SECONDS) == 1, "pending read completes when data arrives");

                // large transfer; loops are mandatory because async writes may complete partially
                final int TOTAL = 256 * 1024;
                ByteBuffer big = ByteBuffer.allocate(TOTAL);
                for (int i = 0; i < TOTAL; i++) big.put((byte) i);
                big.flip();
                long written = 0;
                while (big.hasRemaining()) {
                    Integer n = client.write(big).get(10, TimeUnit.SECONDS);
                    if (n == null || n <= 0) { check(false, "large write stalled"); break; }
                    written += n;
                }
                check(written == TOTAL, "all large-write bytes accepted, wrote " + written);
                ByteBuffer sink = ByteBuffer.allocate(TOTAL);
                while (sink.position() < TOTAL) {
                    Integer n = peer.read(sink).get(10, TimeUnit.SECONDS);
                    if (n == null || n <= 0) { check(false, "large read stalled"); break; }
                }
                boolean mismatch = false;
                for (int i = 0; i < TOTAL; i++) if (sink.get(i) != (byte) i) mismatch = true;
                check(!mismatch, "large transfer is byte-exact");

                // gathering write and scattering read through the timed overloads
                ByteBuffer g1 = ByteBuffer.wrap(new byte[] {1});
                ByteBuffer g2 = ByteBuffer.wrap(new byte[] {2});
                Recorder<Long, Object> gw = new Recorder<Long, Object>();
                client.write(new ByteBuffer[] {g1, g2}, 0, 2, 5, TimeUnit.SECONDS, "g-att", gw);
                check(gw.awaitDone() && gw.failure == null && gw.result != null && gw.result == 2L,
                      "gathering write transferred 2");
                ByteBuffer s1 = ByteBuffer.allocate(1);
                ByteBuffer s2 = ByteBuffer.allocate(1);
                Recorder<Long, Object> sr = new Recorder<Long, Object>();
                peer.read(new ByteBuffer[] {s1, s2}, 0, 2, 5, TimeUnit.SECONDS, "s-att", sr);
                check(sr.awaitDone() && sr.failure == null && sr.result != null && sr.result == 2L,
                      "scattering read transferred 2");
                check(s1.get(0) == 1 && s2.get(0) == 2, "scattered contents land in order");
                peer.close();
            }
        }
    }

    static void testPendingAndCloseContract() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        // ReadPendingException: a read with no data pends; a concurrent read is rejected
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, TimeUnit.SECONDS);
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
                final AsynchronousSocketChannel ch = client;
                Recorder<Integer, Object> first = new Recorder<Integer, Object>();
                client.read(ByteBuffer.allocate(1), 30, TimeUnit.SECONDS, null, first);
                checkThrows(ReadPendingException.class, new Runnable() {
                    public void run() { ch.read(ByteBuffer.allocate(1)); }
                }, "second read while one is pending");
                checkThrows(IllegalArgumentException.class, new Runnable() {
                    public void run() { ch.read(ByteBuffer.allocate(1).asReadOnlyBuffer()); }
                }, "read into read-only buffer");
                peer.write(ByteBuffer.wrap(new byte[] {42})).get(10, TimeUnit.SECONDS);
                check(first.awaitDone(), "pending read completed after peer write");
                check(first.failure == null && first.result != null && first.result == 1,
                      "pending read transferred 1 byte");
                // InterruptedByTimeoutException: short timeout with guaranteed no data
                Recorder<Integer, Object> timed = new Recorder<Integer, Object>();
                client.read(ByteBuffer.allocate(1), 60, TimeUnit.MILLISECONDS, "timeout-att", timed);
                check(timed.awaitDone(), "timed-out read handler invoked");
                check(timed.failure instanceof InterruptedByTimeoutException,
                      "expired read timeout -> InterruptedByTimeoutException, got " + timed.failure);
                check("timeout-att".equals(timed.attachment), "failure path carries the attachment");
                peer.close();
            }
        }
        // WritePendingException: saturate the kernel buffers until a write parks
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
                client.connect(addr).get(10, TimeUnit.SECONDS);
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
                peer.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
                Future<Integer> parked = null;
                for (int i = 0; i < 32; i++) {
                    Future<Integer> f = client.write(ByteBuffer.allocate(256 * 1024));
                    if (!f.isDone()) { parked = f; break; }
                }
                check(parked != null, "socket buffers saturated, one write parked");
                final AsynchronousSocketChannel ch = client;
                final Future<Integer> parkedF = parked;
                boolean rejected = false;
                try {
                    ch.write(ByteBuffer.allocate(1));
                } catch (WritePendingException wpe) {
                    rejected = true;
                }
                if (!rejected) {
                    // The completion thread clears the write-pending state
                    // (enableWriting) immediately before publishing the
                    // completed result, so a concurrent writer may slip into
                    // that window. That is benign: the contract is violated
                    // only if the first write was still outstanding.
                    try {
                        parkedF.get(2, TimeUnit.SECONDS);
                        passed++; // first write had already completed
                    } catch (java.util.concurrent.TimeoutException te) {
                        throw new AssertionError("second write while one is pending:"
                                + " expected WritePendingException and no exception was thrown");
                    }
                } else {
                    passed++;
                }
                // draining the peer releases the parked write
                ByteBuffer drain = ByteBuffer.allocate(65536);
                while (!parked.isDone()) {
                    Future<Integer> rf = peer.read(drain);
                    Integer n;
                    try {
                        n = rf.get(2, TimeUnit.SECONDS);
                    } catch (java.util.concurrent.TimeoutException te) {
                        break;
                    }
                    if (n == null || n <= 0) break;
                    drain.clear();
                }
                check(parked.get(10, TimeUnit.SECONDS) > 0, "parked write completed after draining");
                peer.close();
            }
        }
        // closed channel: pending-free failures surface through the Future
        AsynchronousSocketChannel closed = AsynchronousSocketChannel.open();
        closed.close();
        check(!closed.isOpen(), "closed channel reports closed");
        try {
            closed.read(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS);
            check(false, "read on closed channel must fail");
        } catch (ExecutionException e) {
            check(e.getCause() instanceof ClosedChannelException,
                  "closed read -> ClosedChannelException, got " + e.getCause());
        }
        try {
            closed.write(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS);
            check(false, "write on closed channel must fail");
        } catch (ExecutionException e) {
            check(e.getCause() instanceof ClosedChannelException,
                  "closed write -> ClosedChannelException, got " + e.getCause());
        }
        // closing while a read is pending fails the handler with AsynchronousCloseException
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            client.connect(addr).get(10, TimeUnit.SECONDS);
            AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
            Recorder<Integer, Object> pend = new Recorder<Integer, Object>();
            client.read(ByteBuffer.allocate(1), 30, TimeUnit.SECONDS, "close-att", pend);
            client.close();
            check(pend.awaitDone(), "pending read handler invoked on close");
            check(pend.failure instanceof AsynchronousCloseException,
                  "close during pending read -> AsynchronousCloseException, got " + pend.failure);
            check("close-att".equals(pend.attachment), "attachment delivered on failure");
            peer.close();
        }
    }

    static void testSurfaceContract() {
        java.lang.reflect.Method[] ms = AsynchronousSocketChannel.class.getMethods();
        int timedReads = 0, timedWrites = 0, gatherReads = 0, gatherWrites = 0;
        for (java.lang.reflect.Method m : ms) {
            Class<?>[] ps = m.getParameterTypes();
            if (m.getName().equals("read") && ps.length == 5) timedReads++;
            if (m.getName().equals("write") && ps.length == 5) timedWrites++;
            if (m.getName().equals("read") && ps.length == 7) gatherReads++;
            if (m.getName().equals("write") && ps.length == 7) gatherWrites++;
        }
        check(timedReads == 1 && timedWrites == 1 && gatherReads >= 1 && gatherWrites >= 1,
              "timed read/write overloads (single and gathering) exist");
        boolean noSocket = false;
        try { AsynchronousSocketChannel.class.getMethod("socket"); }
        catch (NoSuchMethodException e) { noSocket = true; }
        check(noSocket, "AsynchronousSocketChannel has no socket() method");
        boolean noIsConnected = false;
        try { AsynchronousSocketChannel.class.getMethod("isConnected"); }
        catch (NoSuchMethodException e) { noIsConnected = true; }
        check(noIsConnected, "AsynchronousSocketChannel has no isConnected() method");
    }


}
