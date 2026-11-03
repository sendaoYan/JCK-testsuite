/*
 * @test
 * @summary javax.net.SocketFactory conformance: white-box structure + black-box behavior.
 * @run main SocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.SocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Arrays.stream(Class.forName("javax.net.SocketFactory").getMethods()).anyMatch(m -> m.getName().equals("createSocket")), "method createSocket");
        check(Arrays.stream(Class.forName("javax.net.SocketFactory").getMethods()).anyMatch(m -> m.getName().equals("getDefault")), "method getDefault");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.SocketFactory f = javax.net.SocketFactory.getDefault();
        check(f != null, "getDefault non-null");
        check(f == javax.net.SocketFactory.getDefault(), "getDefault idempotent");
        java.net.Socket s = f.createSocket();
        check(s != null && !s.isConnected() && !s.isBound(), "unconnected socket");
        s.close();
        class BaseSF extends javax.net.SocketFactory {
            public java.net.Socket createSocket(String h, int p) throws java.io.IOException { return null; }
            public java.net.Socket createSocket(String h, int p, java.net.InetAddress a, int ap) throws java.io.IOException { return null; }
            public java.net.Socket createSocket(java.net.InetAddress a, int p) throws java.io.IOException { return null; }
            public java.net.Socket createSocket(java.net.InetAddress a, int p, java.net.InetAddress la, int lp) throws java.io.IOException { return null; }
        }
        check(expect(() -> { new BaseSF().createSocket(); return "ok"; }).equals("SocketException"), "base createSocket() unsupported");
        try (java.net.ServerSocket srv = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            final int port = srv.getLocalPort();
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < 2; i++) {
                        try (java.net.Socket cs = srv.accept()) {
                            cs.getOutputStream().write(cs.getInputStream().read()); cs.getOutputStream().flush();
                        }
                    }
                } catch (Exception e) {}
            });
            t.setDaemon(true); t.start();
            java.net.Socket c1 = f.createSocket("localhost", port);
            c1.getOutputStream().write('A'); c1.getOutputStream().flush();
            check(c1.getInputStream().read() == 'A', "createSocket(host,port) echo");
            c1.close();
            java.net.Socket c2 = f.createSocket(java.net.InetAddress.getLoopbackAddress(), port);
            c2.getOutputStream().write('B'); c2.getOutputStream().flush();
            check(c2.getInputStream().read() == 'B', "createSocket(addr,port) echo");
            c2.close();
            t.join(5000);
        }
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
