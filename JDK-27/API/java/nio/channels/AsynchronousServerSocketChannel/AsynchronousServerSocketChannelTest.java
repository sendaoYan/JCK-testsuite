/*
 * @test
 * @summary java.nio.channels.AsynchronousServerSocketChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousServerSocketChannelTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutionException;

public class AsynchronousServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /**
     * A wildcard local address (0.0.0.0 or ::) is not a connectable target on
     * Windows (WSAEADDRNOTAVAIL), so derive the matching loopback address and
     * keep the bound port.
     */
    static InetSocketAddress connectTarget(InetSocketAddress local)
            throws java.net.UnknownHostException {
        InetAddress a = local.getAddress();
        if (a != null && a.isAnyLocalAddress()) {
            InetAddress target = (a instanceof java.net.Inet6Address)
                    ? InetAddress.getByName("::1") : InetAddress.getByName("127.0.0.1");
            return new InetSocketAddress(target, local.getPort());
        }
        return local;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousServerSocketChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousServerSocketChannel is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(java.nio.channels.AsynchronousChannel.class.isAssignableFrom(c),
              "implements AsynchronousChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "implements NetworkChannel");
        for (String n : new String[] {"open", "bind", "setOption", "accept", "getLocalAddress", "provider"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        var group = AsynchronousChannelGroup.withFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "assc"); t.setDaemon(true); return t;
        });
        try {
            try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group)) {
                check(server.provider() == group.provider(), "provider from group");
                check(!server.isOpen() == false, "server open");

                // accept before bind
                boolean threw = false;
                try { server.accept(); }
                catch (java.nio.channels.NotYetBoundException e) { threw = true; }
                check(threw, "accept before bind -> NotYetBoundException");

                // bind(null) + local address
                AsynchronousServerSocketChannel ret = server.bind(null, 16);
                check(ret == server, "bind returns this");
                InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
                check(addr.getPort() > 0, "ephemeral port");
                check(!server.supportedOptions().isEmpty(), "supported options");

                threw = false;
                try { server.bind(null); }
                catch (java.nio.channels.AlreadyBoundException e) { threw = true; }
                check(threw, "double bind -> AlreadyBoundException");

                // Future accept: client connects first
                try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                    Future<Void> cf = client.connect(connectTarget(addr));
                    check(cf.get(10, TimeUnit.SECONDS) == null, "client connected");
                    Future<AsynchronousSocketChannel> af = server.accept();
                    AsynchronousSocketChannel accepted = af.get(10, TimeUnit.SECONDS);
                    check(accepted != null, "accepted via Future");
                    check(accepted.getRemoteAddress() != null, "accepted remote address");
                    // echo one byte: client writes, accepted echoes back
                    client.write(ByteBuffer.wrap(new byte[] {7})).get(10, TimeUnit.SECONDS);
                    ByteBuffer buf = ByteBuffer.allocate(1);
                    check(accepted.read(buf).get(10, TimeUnit.SECONDS) == 1, "accepted read 1");
                    accepted.write((ByteBuffer) buf.flip()).get(10, TimeUnit.SECONDS);
                    ByteBuffer back = ByteBuffer.allocate(1);
                    check(client.read(back).get(10, TimeUnit.SECONDS) == 1, "client got echo");
                    check(back.get(0) == 7, "echo content");
                    accepted.close();
                }

                // CompletionHandler accept
                try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                    client.connect(connectTarget(addr)).get(10, TimeUnit.SECONDS);
                    CompletableFuture<AsynchronousSocketChannel> got = new CompletableFuture<>();
                    server.accept("attach", new CompletionHandler<AsynchronousSocketChannel, String>() {
                        public void completed(AsynchronousSocketChannel ch, String a) {
                            check("attach".equals(a), "attachment delivered");
                            got.complete(ch);
                        }
                        public void failed(Throwable t, String a) { got.completeExceptionally(t); }
                    });
                    AsynchronousSocketChannel accepted = got.get(10, TimeUnit.SECONDS);
                    check(accepted != null && accepted.isOpen(), "accepted via handler");
                    accepted.close();
                }

                // second concurrent accept -> AcceptPendingException
                Future<AsynchronousSocketChannel> pending = server.accept();
                try {
                    server.accept();
                    check(false, "second accept should throw AcceptPendingException");
                } catch (java.nio.channels.AcceptPendingException e) {
                    passed++;
                }
                pending.cancel(false);
            }
        } finally {
            group.shutdownNow();
        }

        // default-group open variant + option chaining
        try (AsynchronousServerSocketChannel s2 = AsynchronousServerSocketChannel.open()) {
            s2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 10);
            s2.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
            check(s2.getOption(StandardSocketOptions.SO_RCVBUF) >= 16 * 1024, "SO_RCVBUF");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnboundAndBindContract();
        testAcceptContract();
        testGroupAndSurfaceContract();
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

    static void testUnboundAndBindContract() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) {
            check(s.isOpen(), "fresh server open");
            check(s.getLocalAddress() == null, "local address null before bind");
            check(s.provider() == AsynchronousChannelProvider.provider(),
                  "open() wired to default AsynchronousChannelProvider");
            checkThrows(NotYetBoundException.class, new Runnable() {
                public void run() { s.accept(); }
            }, "Future accept before bind");
            checkThrows(NotYetBoundException.class, new Runnable() {
                public void run() { s.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    public void completed(AsynchronousSocketChannel r, Object a) { }
                    public void failed(Throwable t, Object a) { }
                }); }
            }, "handler accept before bind");
            // options work independently of binding
            AsynchronousServerSocketChannel ret = s.setOption(StandardSocketOptions.SO_RCVBUF, 32 * 1024);
            check(ret == s, "setOption returns this");
            check(s.getOption(StandardSocketOptions.SO_RCVBUF) >= 32 * 1024, "SO_RCVBUF round trip");
            check(!s.supportedOptions().isEmpty(), "supportedOptions non-empty");
            check(s.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF),
                  "SO_RCVBUF among supported options");
            // bind contract
            check(s.bind(null) == s, "bind(null) returns this");
            InetSocketAddress la = (InetSocketAddress) s.getLocalAddress();
            check(la != null && la.getPort() > 0, "ephemeral port assigned");
            check(la.getAddress().isAnyLocalAddress(), "bind(null) binds the wildcard address");
            checkThrows(AlreadyBoundException.class, new Runnable() {
                public void run() {
                    try { s.bind(null); }
                    catch (java.io.IOException e) { throw new RuntimeException(e); }
                }
            }, "double bind (no-backlog overload)");
            checkThrows(AlreadyBoundException.class, new Runnable() {
                public void run() {
                    try { s.bind(null, 16); }
                    catch (java.io.IOException e) { throw new RuntimeException(e); }
                }
            }, "double bind (backlog overload)");
        }
        // explicit loopback bind and backlog overload
        try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) {
            check(s.bind(new InetSocketAddress(lo, 0), 7) == s, "bind(addr, backlog) returns this");
            InetSocketAddress la = (InetSocketAddress) s.getLocalAddress();
            check(la.getAddress().equals(lo), "bound to the requested loopback address");
            check(la.getPort() > 0, "port assigned");
        }
        // unsupported address type
        try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) {
            checkThrows(UnsupportedAddressTypeException.class, new Runnable() {
                public void run() {
                    try { s.bind(new java.net.SocketAddress() { }); }
                    catch (java.io.IOException e) { throw new RuntimeException(e); }
                }
            }, "bind with a non-internet address type");
        }
    }

    static void testAcceptContract() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            final InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            // accept with nothing pending: Future stays undone, second accept is rejected
            Future<AsynchronousSocketChannel> pending = server.accept();
            check(!pending.isDone(), "accept with no connection stays pending");
            check(!pending.isCancelled(), "pending accept not cancelled");
            checkThrows(AcceptPendingException.class, new Runnable() {
                public void run() { server.accept(); }
            }, "second accept while one is pending");
            checkThrows(AcceptPendingException.class, new Runnable() {
                public void run() { server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    public void completed(AsynchronousSocketChannel r, Object a) { }
                    public void failed(Throwable t, Object a) { }
                }); }
            }, "handler accept while one is pending");
            pending.cancel(false);
            check(pending.isCancelled(), "cancel marks the pending accept cancelled");
        }
        // a fresh server: connect then accept hands back a usable channel
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, TimeUnit.SECONDS);
                Future<AsynchronousSocketChannel> af = server.accept();
                AsynchronousSocketChannel accepted = af.get(10, TimeUnit.SECONDS);
                check(accepted != null && accepted.isOpen(), "accepted channel open");
                check(((InetSocketAddress) accepted.getLocalAddress()).getPort() == addr.getPort(),
                      "accepted local port equals server port");
                check(accepted.getRemoteAddress() != null, "accepted remote address known");
                // Windows 双栈客户端把自己的本地地址报成通配 [::]:port，而服务端看到的
                // 仍是具体的 127.0.0.1:port：端口必须一致，本地地址为通配时只比端口。
                InetSocketAddress acceptedRemote = (InetSocketAddress) accepted.getRemoteAddress();
                InetSocketAddress clientLocal = (InetSocketAddress) client.getLocalAddress();
                check(acceptedRemote.getPort() == clientLocal.getPort(),
                      "accepted remote port equals client local port");
                check(clientLocal.getAddress().isAnyLocalAddress() || acceptedRemote.equals(clientLocal),
                      "accepted remote is the client local address (wildcard local on Windows)");
                check(accepted.provider() == server.provider(),
                      "accepted channel inherits the server provider");
                // one byte round trip proves the accepted pair works
                client.write(ByteBuffer.wrap(new byte[] {5})).get(10, TimeUnit.SECONDS);
                ByteBuffer b = ByteBuffer.allocate(1);
                check(accepted.read(b).get(10, TimeUnit.SECONDS) == 1 && b.get(0) == 5,
                      "accepted channel transfers data");
                accepted.close();
            }
            // handler-form accept: attachment passthrough
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, TimeUnit.SECONDS);
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final java.net.SocketAddress[] remote = new java.net.SocketAddress[1];
                final String[] att = new String[1];
                server.accept("srv-att", new CompletionHandler<AsynchronousSocketChannel, String>() {
                    public void completed(AsynchronousSocketChannel r, String a) {
                        try { remote[0] = r.getRemoteAddress(); } catch (Exception e) { }
                        att[0] = a;
                        done.countDown();
                    }
                    public void failed(Throwable t, String a) { done.countDown(); }
                });
                check(done.await(10, TimeUnit.SECONDS), "handler accept completed");
                check("srv-att".equals(att[0]), "accept attachment passed through");
                check(remote[0] != null, "handler-accepted channel has a remote address");
            }
        }
        // closing while an accept is pending fails it with AsynchronousCloseException
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(lo, 0));
            Future<AsynchronousSocketChannel> pending = server.accept();
            check(!pending.isDone(), "accept pending before close");
            server.close();
            try {
                pending.get(10, TimeUnit.SECONDS);
                check(false, "pending accept must fail on close");
            } catch (ExecutionException e) {
                check(e.getCause() instanceof AsynchronousCloseException,
                      "pending accept + close -> AsynchronousCloseException, got " + e.getCause());
            }
            check(!server.isOpen(), "server closed");
        }
        // accept on a closed channel fails through the Future with ClosedChannelException
        AsynchronousServerSocketChannel dead = AsynchronousServerSocketChannel.open();
        dead.bind(null);
        dead.close();
        try {
            dead.accept().get(10, TimeUnit.SECONDS);
            check(false, "accept on closed server must fail");
        } catch (ExecutionException e) {
            check(e.getCause() instanceof ClosedChannelException,
                  "accept after close -> ClosedChannelException, got " + e.getCause());
        }
    }

    static void testGroupAndSurfaceContract() throws Exception {
        // channels opened on a group are wired to the group's provider
        java.util.concurrent.ThreadFactory daemon = new java.util.concurrent.ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "a7-assc");
                t.setDaemon(true);
                return t;
            }
        };
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(1, daemon);
        try {
            try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open(group)) {
                check(s.provider() == group.provider(), "open(group) uses the group provider");
                check(group.provider() == AsynchronousChannelProvider.provider(),
                      "fixed pool group uses the default provider");
                s.bind(null);
                check(((InetSocketAddress) s.getLocalAddress()).getPort() > 0, "group channel binds");
            }
        } finally {
            group.shutdownNow();
            check(group.awaitTermination(10, TimeUnit.SECONDS), "group terminated");
        }
        // close is idempotent
        AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open();
        s.bind(null);
        s.close();
        s.close();
        check(!s.isOpen(), "close idempotent, channel closed");
        // structural surface: exactly two accept overloads, no socket(), no timeout accept
        int acceptOverloads = 0;
        for (java.lang.reflect.Method m : AsynchronousServerSocketChannel.class.getMethods())
            if (m.getName().equals("accept")) acceptOverloads++;
        check(acceptOverloads == 2, "exactly Future and CompletionHandler accept overloads, got "
              + acceptOverloads);
        boolean noSocket = false;
        try { AsynchronousServerSocketChannel.class.getMethod("socket"); }
        catch (NoSuchMethodException e) { noSocket = true; }
        check(noSocket, "AsynchronousServerSocketChannel has no socket() method");
        boolean noAcceptTimeout = true;
        for (java.lang.reflect.Method m : AsynchronousServerSocketChannel.class.getMethods())
            for (Class<?> p : m.getParameterTypes())
                if (p == TimeUnit.class) noAcceptTimeout = false;
        check(noAcceptTimeout, "accept has no timed overload (unlike socket read/write)");
    }


}
