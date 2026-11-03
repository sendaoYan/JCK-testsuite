/*
 * @test
 * @summary java.net.SocketImpl conformance: white-box structure + black-box behavior.
 * @run main SocketImplTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class SocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // minimal concrete subclass exposing protected accessors
    static class Impl extends SocketImpl {
        protected void create(boolean stream) { }
        protected void connect(String host, int port) { }
        protected void connect(InetAddress address, int port) { }
        protected void connect(SocketAddress address, int timeout) { }
        protected void bind(InetAddress host, int port) { }
        protected void listen(int backlog) { }
        protected void accept(SocketImpl s) { }
        protected InputStream getInputStream() { return null; }
        protected OutputStream getOutputStream() { return null; }
        protected int available() { return 0; }
        protected void close() { }
        protected void sendUrgentData(int data) { }
        public void setOption(int optID, Object value) { }
        public Object getOption(int optID) { return null; }
        FileDescriptor fd() { return getFileDescriptor(); }
        InetAddress addr() { return getInetAddress(); }
        int port() { return getPort(); }
        int localPort() { return getLocalPort(); }
        boolean urgent() { return supportsUrgentData(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImpl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");
        check(c.getConstructor() != null, "no-arg ctor");
        for (String m : new String[]{"create","connect","bind","listen","accept","getInputStream","getOutputStream","available","close","sendUrgentData"}) {
            check(hasMethod(c, m), "method " + m);
        }
        check(c.getDeclaredField("fd") != null, "fd field");
        check(c.getDeclaredField("address") != null, "address field");
        check(c.getDeclaredField("port") != null, "port field");
        check(c.getDeclaredField("localport") != null, "localport field");
    }

    static void testBlackBox() throws Exception {
        check(Modifier.isAbstract(SocketImpl.class.getModifiers()), "still abstract");
        Impl impl = new Impl();
        check(impl.fd() == null, "fd null by default");
        check(impl.addr() == null, "address null by default");
        check(impl.port() == 0, "port 0 by default");
        check(impl.localPort() == 0, "localport 0 by default");
        check(impl.toString() != null, "toString non-null");
        check(!impl.urgent(), "supportsUrgentData false by default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
