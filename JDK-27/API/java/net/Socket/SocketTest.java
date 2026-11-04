/*
 * @test
 * @summary java.net.Socket conformance: white-box structure + black-box behavior.
 * @run main SocketTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SocketTest {
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
        Class<?> c = Class.forName("java.net.Socket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(java.net.Proxy.class) != null, "ctor(Proxy)");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getConstructor(InetAddress.class, int.class) != null, "ctor(InetAddress,int)");
        check(c.getMethod("connect", SocketAddress.class) != null, "connect(SocketAddress)");
        check(c.getMethod("connect", SocketAddress.class, int.class) != null, "connect(SocketAddress,int)");
        check(c.getMethod("bind", SocketAddress.class) != null, "bind(SocketAddress)");
        check(c.getMethod("getInputStream") != null, "getInputStream");
        check(c.getMethod("getOutputStream") != null, "getOutputStream");
        check(c.getMethod("close") != null, "close");
        check(c.getMethod("shutdownInput") != null, "shutdownInput");
        check(c.getMethod("shutdownOutput") != null, "shutdownOutput");
        check(c.getMethod("setOption", SocketOption.class, Object.class) != null, "setOption");
        check(c.getMethod("getOption", SocketOption.class) != null, "getOption");
        check(c.getMethod("supportedOptions") != null, "supportedOptions");
        check(c.getMethod("sendUrgentData", int.class) != null, "sendUrgentData");
    }

    static void testBlackBox() throws Exception {
        // unconnected socket defaults
        Socket s = new Socket();
        check(!s.isConnected(), "not connected");
        check(!s.isBound(), "not bound");
        check(!s.isClosed(), "not closed");
        check(!s.isInputShutdown(), "not input shutdown");
        check(!s.isOutputShutdown(), "not output shutdown");
        check(s.getInetAddress() == null, "inetAddress null");
        check(s.getRemoteSocketAddress() == null, "remote address null");
        check(s.getPort() == 0, "port 0");
        check(s.getChannel() == null, "channel null");

        // localhost + ephemeral port handshake
        ServerSocket ss = new ServerSocket(0);
        int serverPort = ss.getLocalPort();
        Thread client = new Thread(() -> {
            try (Socket c = new Socket("127.0.0.1", serverPort)) {
                c.getOutputStream().write(42);
                c.getOutputStream().flush();
                check(c.getInputStream().read() == 99, "client echo");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        client.start();
        Socket server = ss.accept();
        try {
            check(server.isConnected(), "accepted connected");
            check(server.isBound(), "accepted bound");
            check(server.getInputStream().read() == 42, "server read 42");
            server.getOutputStream().write(99);
            server.getOutputStream().flush();

            // options on connected socket
            server.setTcpNoDelay(true);
            check(server.getTcpNoDelay(), "tcpNoDelay");
            server.setSoTimeout(1000);
            check(server.getSoTimeout() == 1000, "soTimeout");
            server.setKeepAlive(true);
            check(server.getKeepAlive(), "keepAlive");
            server.setReuseAddress(true);
            check(server.getReuseAddress(), "reuseAddress");
            server.setSendBufferSize(8192);
            check(server.getSendBufferSize() > 0, "sendBufferSize");
            server.setReceiveBufferSize(8192);
            check(server.getReceiveBufferSize() > 0, "receiveBufferSize");
            server.setSoLinger(true, 5);
            check(server.getSoLinger() >= 0, "soLinger");
            server.setOOBInline(true);
            check(server.getOOBInline(), "oobInline");
            server.sendUrgentData(1);

            check(server.getPort() > 0, "getPort > 0 (client ephemeral)");
            check(server.getLocalPort() == serverPort, "getLocalPort equals server port");
            check(((InetSocketAddress) server.getRemoteSocketAddress()).getAddress().isLoopbackAddress(), "remote address loopback");
            check(server.getLocalSocketAddress() != null, "local address");
            check(server.getInetAddress() != null, "inetAddress");
            check(server.getLocalAddress() != null, "localAddress");

            // shutdownOutput / shutdownInput
            server.shutdownOutput();
            check(server.isOutputShutdown(), "output shutdown");
            check(!server.isInputShutdown(), "input not shutdown");

            check(server.toString() != null, "toString");
            Set<SocketOption<?>> opts = server.supportedOptions();
            check(opts != null && !opts.isEmpty(), "supportedOptions non-empty");
        } finally {
            server.close();
        }
        client.join();
        ss.close();

        // unconnected socket binding + close
        Socket u = new Socket();
        u.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        check(u.isBound(), "bound after bind(null)");
        check(u.getLocalPort() > 0, "localPort after bind");
        u.close();
        check(u.isClosed(), "closed");

        // close() before connect is fine, but operations fail
        Socket closed = new Socket();
        closed.close();
        check(closed.isClosed(), "closed flag");
        fails(SocketException.class, "getInputStream on closed", () -> closed.getInputStream());

        // connect to a refused port on loopback
        try (ServerSocket tmp = new ServerSocket(0)) {
            int p = tmp.getLocalPort();
            tmp.close();
            fails(IOException.class, "connect refused", () -> {
                try (Socket c2 = new Socket()) {
                    c2.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), p));
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
