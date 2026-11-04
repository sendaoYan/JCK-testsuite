/*
 * @test
 * @summary javax.net.SocketFactory conformance: white-box structure + black-box behavior (loopback connect matrix).
 * @run main SocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.*;
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
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0 && Modifier.isProtected(k.getModifiers())) protCtor = true;
        check(protCtor, "protected no-arg constructor");
        String[] names = {"getDefault", "createSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        // exactly 4 abstract createSocket overloads (host/addr x with/without local binding)
        int abstractCreate = 0;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("createSocket") && Modifier.isAbstract(m.getModifiers())) abstractCreate++;
        check(abstractCreate == 4, "4 abstract createSocket overloads, found " + abstractCreate);
        // the no-arg createSocket() is concrete in the base class (throws SocketException)
        boolean concreteNoArg = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("createSocket") && m.getParameterCount() == 0
                    && !Modifier.isAbstract(m.getModifiers())) concreteNoArg = true;
        check(concreteNoArg, "base class declares concrete no-arg createSocket()");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SocketFactory def = SocketFactory.getDefault();
        check(def != null, "getDefault non-null");
        check(def == SocketFactory.getDefault(), "getDefault idempotent");
        Socket s = def.createSocket();
        check(s != null && !s.isConnected() && !s.isBound(), "default createSocket() unconnected, unbound");
        check(!s.isClosed(), "fresh socket not closed");
        s.close();
        check(s.isClosed(), "close() marks socket closed");

        // a minimal subclass inherits the base no-arg createSocket(), which is not implemented
        class F extends SocketFactory {
            public Socket createSocket(String h, int p) { return new Socket(); }
            public Socket createSocket(String h, int p, InetAddress a, int lp) { return new Socket(); }
            public Socket createSocket(InetAddress a, int p) { return new Socket(); }
            public Socket createSocket(InetAddress a, int p, InetAddress l, int lp) { return new Socket(); }
        }
        F f = new F();
        check(expect(() -> { f.createSocket(); return "ok"; }).equals("SocketException"),
                "base no-arg createSocket() throws SocketException for a subclass that does not override it");

        // subclass overriding all four abstract methods returns its own sockets (delegation pattern)
        final Socket sentinel1 = new Socket(), sentinel2 = new Socket(),
                     sentinel3 = new Socket(), sentinel4 = new Socket();
        final int[] calls = new int[4];
        class G extends SocketFactory {
            public Socket createSocket(String h, int p) { calls[0]++; return sentinel1; }
            public Socket createSocket(String h, int p, InetAddress a, int lp) { calls[1]++; return sentinel2; }
            public Socket createSocket(InetAddress a, int p) { calls[2]++; return sentinel3; }
            public Socket createSocket(InetAddress a, int p, InetAddress l, int lp) { calls[3]++; return sentinel4; }
        }
        G g = new G();
        check(g.createSocket("h", 1) == sentinel1 && g.createSocket("h", 1, null, 0) == sentinel2
                && g.createSocket((InetAddress) null, 1) == sentinel3
                && g.createSocket((InetAddress) null, 1, null, 0) == sentinel4,
                "full-override factory returns its own sockets");
        check(calls[0] == 1 && calls[1] == 1 && calls[2] == 1 && calls[3] == 1,
                "each overload dispatches exactly once");

        // loopback connect matrix against a plain echo server
        try (ServerSocket srv = new ServerSocket(0, 8, InetAddress.getLoopbackAddress())) {
            final int port = srv.getLocalPort();
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < 4; i++) {
                        try (Socket cs = srv.accept()) {
                            cs.getOutputStream().write(cs.getInputStream().read());
                            cs.getOutputStream().flush();
                        }
                    }
                } catch (Exception e) { }
            });
            t.setDaemon(true); t.start();

            Socket c1 = def.createSocket("localhost", port);
            check(c1.isConnected() && c1.isBound(), "createSocket(host,port) connected");
            check(c1.getPort() == port, "remote port recorded");
            check(((InetSocketAddress) c1.getRemoteSocketAddress()).getAddress().isLoopbackAddress(),
                    "remote address is loopback");
            c1.getOutputStream().write('A'); c1.getOutputStream().flush();
            check(c1.getInputStream().read() == 'A', "createSocket(String,int) echo round trip");
            c1.close();

            Socket c2 = def.createSocket(InetAddress.getLoopbackAddress(), port);
            c2.getOutputStream().write('B'); c2.getOutputStream().flush();
            check(c2.getInputStream().read() == 'B', "createSocket(InetAddress,int) echo round trip");
            c2.close();

            Socket c3 = def.createSocket("127.0.0.1", port, InetAddress.getLoopbackAddress(), 0);
            check(c3.isBound() && c3.getLocalPort() > 0
                    && c3.getLocalAddress().isLoopbackAddress(), "4-arg (String) binds the given local address");
            c3.getOutputStream().write('C'); c3.getOutputStream().flush();
            check(c3.getInputStream().read() == 'C', "createSocket(String,int,InetAddress,int) echo round trip");
            c3.close();

            Socket c4 = def.createSocket(InetAddress.getLoopbackAddress(), port,
                    InetAddress.getLoopbackAddress(), 0);
            c4.getOutputStream().write('D'); c4.getOutputStream().flush();
            check(c4.getInputStream().read() == 'D', "createSocket(InetAddress,int,InetAddress,int) echo round trip");
            check(c4.getLocalAddress().isLoopbackAddress(), "4-arg (InetAddress) local address is loopback");
            c4.close();
            t.join(5000);
        }

        // connecting to a closed port on loopback fails with an IOException (ConnectException)
        int deadPort;
        try (ServerSocket dead = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            deadPort = dead.getLocalPort();
        }
        Object res = expect(() -> def.createSocket(InetAddress.getLoopbackAddress(), deadPort));
        check(res.equals("ConnectException") || res.equals("IOException"),
                "connect to closed port -> IOException family, got " + res);
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
