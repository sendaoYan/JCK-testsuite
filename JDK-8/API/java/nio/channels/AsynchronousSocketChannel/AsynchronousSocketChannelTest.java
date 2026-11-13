/*
 * @test
 * @summary java.nio.channels.AsynchronousSocketChannel conformance: open/provider.
 * @run main AsynchronousSocketChannelTest
 */

import java.nio.channels.AsynchronousSocketChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousSocketChannel() throws Exception {
        AsynchronousSocketChannel ch = AsynchronousSocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.provider() != null, "provider");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousSocketChannel();
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
                        try {
                            first.get(10, TimeUnit.SECONDS);
                        } catch (java.util.concurrent.ExecutionException e) {
                            // 回环 connect 失败也不影响本断言，交给后续用例
                        }
                    }
                }
                if (!sawPending) {
                    // 回环 connect 往往在第二次调用前就完成；TEST-NET-1 黑洞地址的 SYN
                    // 无人应答，connect 会持续 pending，第二次调用必然命中该异常。
                    // 黑洞不可达时（无默认路由/防火墙拒绝）该探测无判定力，保持不变。
                    try (AsynchronousSocketChannel pending = AsynchronousSocketChannel.open()) {
                        InetSocketAddress blackhole = new InetSocketAddress("192.0.2.1", 9);
                        Future<Void> first = pending.connect(blackhole);
                        try {
                            pending.connect(blackhole);
                        } catch (ConnectionPendingException e) {
                            sawPending = true;
                        } catch (AlreadyConnectedException e) {
                            // 极端情况下连接瞬间完成
                        }
                        first.cancel(true);
                    } catch (java.io.IOException notApplicable) {
                        // 打开通道失败：该探测不适用
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
                checkThrows(WritePendingException.class, new Runnable() {
                    public void run() { ch.write(ByteBuffer.allocate(1)); }
                }, "second write while one is pending");
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
