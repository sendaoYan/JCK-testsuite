/*
 * @test
 * @summary java.net.ServerSocket conformance: white-box structure + black-box behavior.
 * @run main ServerSocketTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ServerSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ServerSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(int.class, int.class) != null, "ctor(int,int)");
        check(c.getConstructor(int.class, int.class, InetAddress.class) != null, "ctor(int,int,InetAddress)");
        check(c.getMethod("bind", SocketAddress.class) != null, "bind(SocketAddress)");
        check(c.getMethod("bind", SocketAddress.class, int.class) != null, "bind(SocketAddress,int)");
        check(c.getMethod("accept") != null, "accept");
        check(c.getMethod("close") != null, "close");
        check(c.getMethod("setOption", SocketOption.class, Object.class) != null, "setOption");
        check(c.getMethod("getOption", SocketOption.class) != null, "getOption");
        check(c.getMethod("supportedOptions") != null, "supportedOptions");
    }

    static void testBlackBox() throws Exception {
        // unbound ServerSocket
        ServerSocket unbound = new ServerSocket();
        check(!unbound.isBound(), "not bound");
        check(!unbound.isClosed(), "not closed");
        check(unbound.getLocalPort() == -1, "localPort -1 unbound");
        check(unbound.getInetAddress() == null, "inetAddress null unbound");
        check(unbound.getLocalSocketAddress() == null, "local address null unbound");
        check(unbound.getChannel() == null, "channel null");

        unbound.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        check(unbound.isBound(), "bound after bind");
        check(unbound.getLocalPort() > 0, "ephemeral port after bind");
        unbound.close();
        check(unbound.isClosed(), "closed after close");

        // bound on ephemeral port, accept a connection
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        check(ss.isBound(), "bound");
        check(!ss.isClosed(), "not closed");
        check(ss.getLocalPort() == port, "getLocalPort stable");
        check(ss.getLocalSocketAddress().equals(new InetSocketAddress(port)), "local address");

        ss.setSoTimeout(2000);
        check(ss.getSoTimeout() == 2000, "soTimeout");
        ss.setReuseAddress(true);
        check(ss.getReuseAddress(), "reuseAddress");
        ss.setReceiveBufferSize(8192);
        check(ss.getReceiveBufferSize() > 0, "receiveBufferSize");
        check(ss.toString() != null, "toString");
        check(ss.supportedOptions() != null && !ss.supportedOptions().isEmpty(), "supportedOptions");

        // setOption / getOption roundtrip
        ss.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        check(Boolean.TRUE.equals(ss.getOption(StandardSocketOptions.SO_REUSEADDR)), "setOption/getOption");

        Thread client = new Thread(() -> {
            try (Socket c = new Socket("127.0.0.1", port)) {
                c.getOutputStream().write(7);
                c.getOutputStream().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        client.start();
        Socket accepted = ss.accept();
        check(accepted != null, "accept non-null");
        check(accepted.isConnected(), "accepted connected");
        check(accepted.getInputStream().read() == 7, "read from accepted");
        accepted.close();
        client.join();
        ss.close();
        check(ss.isClosed(), "closed");

        // accept on closed socket throws
        fails(SocketException.class, "accept on closed", () -> {
            ServerSocket s2 = new ServerSocket(0);
            s2.close();
            s2.accept();
        });
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
