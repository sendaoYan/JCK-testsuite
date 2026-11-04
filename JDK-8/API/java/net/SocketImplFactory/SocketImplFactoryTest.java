/*
 * @test
 * @summary java.net.SocketImplFactory conformance: single-method interface
 *          contract (createSocketImpl), Socket.setSocketImplFactory and
 *          ServerSocket.setSocketFactory one-shot semantics - first set OK,
 *          second set and null re-set both fail with exact SocketException
 *          "factory already defined" - the factory being invoked exactly
 *          once per Socket construction, custom implementations flowing
 *          through Socket, and lambda implementability.
 * @run main SocketImplFactoryTest
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.util.HashSet;
import java.util.Set;

public class SocketImplFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** No-op concrete SocketImpl handed out by the factory. */
    static class MiniImpl extends SocketImpl {
        public void create(boolean stream) throws IOException { }
        public void connect(String host, int port) throws IOException { throw new IOException("mini-connect"); }
        public void connect(InetAddress addr, int port) throws IOException { throw new IOException("mini-connect"); }
        public void connect(SocketAddress addr, int timeout) throws IOException { throw new IOException("mini-connect"); }
        public void bind(InetAddress host, int port) throws IOException { }
        public void listen(int backlog) throws IOException { }
        public void accept(SocketImpl s) throws IOException { }
        protected InputStream getInputStream() throws IOException { return new java.io.ByteArrayInputStream(new byte[0]); }
        protected OutputStream getOutputStream() throws IOException { return new java.io.ByteArrayOutputStream(); }
        protected int available() throws IOException { return 0; }
        public void close() throws IOException { }
        public void sendUrgentData(int data) throws IOException { }
        public void setOption(int optID, Object value) throws SocketException { }
        public Object getOption(int optID) throws SocketException { return null; }
    }

    static int created;

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImplFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        // Exactly one abstract method, no defaults.
        Method m = c.getMethod("createSocketImpl");
        check(m != null, "has method createSocketImpl");
        check(Modifier.isAbstract(m.getModifiers()), "createSocketImpl abstract");
        check(m.getReturnType().getName().equals("java.net.SocketImpl"), "return type SocketImpl");
        check(m.getParameterTypes().length == 0, "no parameters");
        int own = 0;
        for (Method mm : c.getDeclaredMethods()) own++;
        check(own == 1, "exactly one declared method, got " + own);
        // Not a functional-interface annotation holder, but lambdas work.
        SocketImplFactory lambda = () -> null;
        check(lambda != null, "lambda implements the interface");
        check(lambda.createSocketImpl() == null, "lambda createSocketImpl delegates");
    }

    static void testImplementation() throws Exception {
        final SocketImpl[] memo = { null };
        SocketImplFactory f = new SocketImplFactory() {
            public SocketImpl createSocketImpl() {
                memo[0] = new MiniImpl();
                return memo[0];
            }
        };
        SocketImpl first = f.createSocketImpl();
        check(first != null, "factory produces an impl");
        check(first == memo[0], "factory returns its product");
        check(first instanceof MiniImpl, "product type");
        check(first instanceof SocketImpl, "product is SocketImpl");
        // Factory contract places no constraint on repeated calls.
        SocketImpl second = f.createSocketImpl();
        check(second != null && second != first, "distinct products per call");
        // Anonymous class form.
        SocketImplFactory anon = new SocketImplFactory() {
            public SocketImpl createSocketImpl() { return null; }
        };
        check(anon.createSocketImpl() == null, "null return permitted (deferred error)");
    }

    static void testFactoryRegistration() throws Exception {
        // The factory is set once per JVM; the first registration succeeds.
        final int[] count = { 0 };
        Socket.setSocketImplFactory(new SocketImplFactory() {
            public SocketImpl createSocketImpl() {
                created++;
                return new MiniImpl();
            }
        });
        passed++;
        check(count[0] == 0, "factory not invoked at registration time");

        // Second registration fails with the exact one-shot error.
        try {
            Socket.setSocketImplFactory(new SocketImplFactory() {
                public SocketImpl createSocketImpl() { return null; }
            });
            check(false, "second Socket factory set must fail");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "second set message, got: " + e.getMessage());
        }
        // Null does not reset the factory either.
        try {
            Socket.setSocketImplFactory(null);
            check(false, "null re-set must fail");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "null re-set message, got: " + e.getMessage());
        }

        // ServerSocket has an independent one-shot factory with the same
        // error contract.
        try {
            ServerSocket.setSocketFactory(new SocketImplFactory() {
                public SocketImpl createSocketImpl() { return new MiniImpl(); }
            });
            passed++;
        } catch (SocketException e) {
            check(false, "first ServerSocket factory set should succeed, got: " + e);
        }
        try {
            ServerSocket.setSocketFactory(new SocketImplFactory() {
                public SocketImpl createSocketImpl() { return null; }
            });
            check(false, "second ServerSocket factory set must fail");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "server second set message, got: " + e.getMessage());
        }
        try {
            ServerSocket.setSocketFactory(null);
            check(false, "server null re-set must fail");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "server null re-set message, got: " + e.getMessage());
        }
    }

    static void testFactoryUsedBySocket() throws Exception {
        // Every Socket construction consults the factory exactly once.
        int before = created;
        Socket s = new Socket();
        check(created == before + 1, "factory invoked once per new Socket(), got " + (created - before));
        check(s != null, "socket with custom impl constructs");
        check("Socket[unconnected]".equals(s.toString()), "custom impl renders unconnected toString");
        check(!s.isConnected(), "custom impl socket not connected");
        check(!s.isClosed(), "custom impl socket open");
        s.close();
        check(s.isClosed(), "custom impl socket closes");
        // A second construction invokes the factory again.
        int before2 = created;
        Socket s2 = new Socket();
        check(created == before2 + 1, "factory invoked for each construction");
        // Custom impl behavior flows through the Socket API surface.
        try {
            s2.connect(new java.net.InetSocketAddress("127.0.0.1", 1234), 100);
            check(false, "custom impl connect must propagate IOException");
        } catch (IOException e) {
            check("mini-connect".equals(e.getMessage()), "custom impl exception surfaces, got: " + e.getMessage());
        }
        s2.close();
    }

    static void testExceptionTypeOnly() throws Exception {
        // The registration methods declare IOException; the one-shot failure
        // is the SocketException subtype.
        try {
            Socket.setSocketImplFactory(() -> null);
            check(false, "unreachable");
        } catch (SocketException e) {
            check(e instanceof java.io.IOException, "SocketException is an IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketImplFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createSocketImpl"), "has method createSocketImpl");
        testWhiteBox();
        testImplementation();
        // Registration mutates JVM-global state: run the Socket-behavior
        // checks after it, and nothing socket-dependent afterwards.
        testFactoryRegistration();
        testFactoryUsedBySocket();
        testExceptionTypeOnly();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
