/*
 * @test
 * @summary javax.net.ServerSocketFactory conformance: white-box structure + black-box behavior (bind/accept matrix).
 * @run main ServerSocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.*;
public class ServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ServerSocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0 && Modifier.isProtected(k.getModifiers())) protCtor = true;
        check(protCtor, "protected no-arg constructor");
        String[] names = {"getDefault", "createServerSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        int abstractCreate = 0;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("createServerSocket") && Modifier.isAbstract(m.getModifiers())) abstractCreate++;
        check(abstractCreate == 3, "3 abstract createServerSocket overloads, found " + abstractCreate);
        boolean concreteNoArg = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("createServerSocket") && m.getParameterCount() == 0
                    && !Modifier.isAbstract(m.getModifiers())) concreteNoArg = true;
        check(concreteNoArg, "base class declares concrete no-arg createServerSocket()");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ServerSocketFactory def = ServerSocketFactory.getDefault();
        check(def != null, "getDefault non-null");

        try (ServerSocket unbound = def.createServerSocket()) {
            check(!unbound.isBound(), "createServerSocket() unbound");
        }
        try (ServerSocket ss = def.createServerSocket(0, 10, InetAddress.getLoopbackAddress())) {
            check(ss.getLocalPort() > 0, "ephemeral port bound");
            check(ss.getInetAddress().isLoopbackAddress(), "loopback bound");
            check(ss.isBound() && !ss.isClosed(), "open server socket");
        }
        try (ServerSocket p1 = def.createServerSocket(0)) {
            check(p1.isBound() && p1.getLocalPort() > 0, "createServerSocket(0) bound to ephemeral port");
            check(!p1.getInetAddress().isLoopbackAddress() || p1.getInetAddress().isLoopbackAddress(),
                    "createServerSocket(0) bound (wildcard or loopback accepted)");
        }
        try (ServerSocket p2 = def.createServerSocket(0, 4)) {
            check(p2.isBound(), "createServerSocket(0, backlog) bound");
        }

        // invalid ports are rejected before any binding happens
        check(expect(() -> def.createServerSocket(-1)).equals("IllegalArgumentException"),
                "createServerSocket(-1) -> IllegalArgumentException");
        check(expect(() -> def.createServerSocket(65536)).equals("IllegalArgumentException"),
                "createServerSocket(65536) -> IllegalArgumentException");

        // accept with a bounded SO_TIMEOUT raises SocketTimeoutException deterministically
        try (ServerSocket ss = def.createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ss.setSoTimeout(300);
            long t0 = System.currentTimeMillis();
            Object res = expect(() -> ss.accept());
            long dt = System.currentTimeMillis() - t0;
            check(res.equals("SocketTimeoutException"), "accept with SO_TIMEOUT -> SocketTimeoutException, got " + res);
            check(dt >= 200 && dt < 5000, "timeout honored within bounds (" + dt + "ms)");
            // Reset to blocking before the echo pair: the 300ms SO_TIMEOUT above
            // would otherwise make accept() below time out under -Xcomp load.
            ss.setSoTimeout(0);
            // factory server + factory client echo pair (full round trip)
            final int port = ss.getLocalPort();
            Thread t = new Thread(() -> {
                try (Socket c = SocketFactory.getDefault().createSocket(InetAddress.getLoopbackAddress(), port)) {
                    c.getOutputStream().write(c.getInputStream().read());
                    c.getOutputStream().flush();
                } catch (Exception e) { }
            });
            t.setDaemon(true); t.start();
            try (Socket accepted = ss.accept()) {
                accepted.getOutputStream().write('Z'); accepted.getOutputStream().flush();
                check(accepted.getInputStream().read() == 'Z', "factory server accepts factory client echo");
            }
            t.join(5000);
        }
        // a closed server socket refuses further accepts
        ServerSocket closed = def.createServerSocket(0);
        closed.close();
        check(closed.isClosed(), "close() marks server socket closed");
        check(expect(() -> closed.accept()).equals("SocketException"),
                "accept on closed server socket -> SocketException");

        // subclass factory: the no-arg createServerSocket() is inherited from the base class,
        // which does not implement it (throws SocketException).
        class F extends ServerSocketFactory {
            public ServerSocket createServerSocket(int p) throws IOException { return new ServerSocket(); }
            public ServerSocket createServerSocket(int p, int b) throws IOException { return new ServerSocket(); }
            public ServerSocket createServerSocket(int p, int b, InetAddress a) throws IOException { return new ServerSocket(); }
        }
        F f = new F();
        check(expect(() -> f.createServerSocket()).equals("SocketException"),
                "base no-arg createServerSocket() throws SocketException for a subclass that does not override it");
        final ServerSocket s1 = new ServerSocket(), s2 = new ServerSocket(), s3 = new ServerSocket();
        final int[] calls = new int[3];
        class G extends ServerSocketFactory {
            public ServerSocket createServerSocket(int p) { calls[0]++; return s1; }
            public ServerSocket createServerSocket(int p, int b) { calls[1]++; return s2; }
            public ServerSocket createServerSocket(int p, int b, InetAddress a) { calls[2]++; return s3; }
        }
        G g = new G();
        check(g.createServerSocket(1) == s1 && g.createServerSocket(1, 2) == s2
                && g.createServerSocket(1, 2, null) == s3, "full-override factory returns its own sockets");
        check(calls[0] == 1 && calls[1] == 1 && calls[2] == 1, "each overload dispatches exactly once");
        s1.close(); s2.close(); s3.close();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
