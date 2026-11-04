/*
 * @test
 * @summary java.net.SocketImpl conformance: white-box structure + black-box
 *          behavior - abstract method surface with modifiers, protected
 *          fields fd/address/port/localport, concrete protected accessors
 *          and their defaults, supportsUrgentData false, base
 *          shutdownInput/shutdownOutput throwing "Method not implemented!",
 *          toString "Socket[addr=...]" form, JDK 13+ protected
 *          setOption/getOption/supportedOptions overloads, SocketOptions
 *          conformance, and InstantiationException on direct instantiation.
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

        String shutdownIn() throws IOException {
            try { shutdownInput(); return "OK"; } catch (IOException e) { return e.getMessage(); }
        }
        String shutdownOut() throws IOException {
            try { shutdownOutput(); return "OK"; } catch (IOException e) { return e.getMessage(); }
        }
    }

    /** Second subclass with real streams for I/O contract checks. */
    static class Mini2 extends SocketImpl {
        public void create(boolean stream) throws IOException { }
        public void connect(String host, int port) throws IOException { throw new IOException("mini"); }
        public void connect(InetAddress addr, int port) throws IOException { throw new IOException("mini"); }
        public void connect(SocketAddress addr, int timeout) throws IOException { throw new IOException("mini"); }
        public void bind(InetAddress host, int port) throws IOException { }
        public void listen(int backlog) throws IOException { }
        public void accept(SocketImpl s) throws IOException { }
        protected InputStream getInputStream() throws IOException { return new ByteArrayInputStream(new byte[0]); }
        protected OutputStream getOutputStream() throws IOException { return new ByteArrayOutputStream(); }
        protected int available() throws IOException { return 0; }
        public void close() throws IOException { }
        public void sendUrgentData(int data) throws IOException { }
        public void setOption(int optID, Object value) throws SocketException { }
        public Object getOption(int optID) throws SocketException { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImpl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");
        check(c.getConstructor() != null, "no-arg ctor");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        for (String m : new String[]{"create","connect","bind","listen","accept","getInputStream","getOutputStream","available","close","sendUrgentData"}) {
            check(hasMethod(c, m), "method " + m);
        }
        check(c.getDeclaredField("fd") != null, "fd field");
        check(c.getDeclaredField("address") != null, "address field");
        check(c.getDeclaredField("port") != null, "port field");
        check(c.getDeclaredField("localport") != null, "localport field");
        // Field types and protection.
        check(Modifier.isProtected(c.getDeclaredField("fd").getModifiers()), "fd protected");
        check(Modifier.isProtected(c.getDeclaredField("address").getModifiers()), "address protected");
        check(Modifier.isProtected(c.getDeclaredField("port").getModifiers()), "port protected");
        check(Modifier.isProtected(c.getDeclaredField("localport").getModifiers()), "localport protected");
        check(c.getDeclaredField("fd").getType().getName().equals("java.io.FileDescriptor"), "fd type");
        check(c.getDeclaredField("address").getType().getName().equals("java.net.InetAddress"), "address type");
        check(c.getDeclaredField("port").getType() == int.class, "port type int");
        check(c.getDeclaredField("localport").getType() == int.class, "localport type int");

        // Every abstract member of the surface is protected.
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                check(Modifier.isProtected(m.getModifiers()), "abstract " + m.getName() + " protected");
            }
        }
        // Concrete protected surface.
        for (String mn : new String[]{"shutdownInput", "shutdownOutput", "getFileDescriptor",
                "getInetAddress", "getPort", "supportsUrgentData", "getLocalPort",
                "setPerformancePreferences"}) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(mn) && !Modifier.isAbstract(m.getModifiers())) found = true;
            }
            check(found, "concrete method " + mn);
        }
        // JDK 13+ protected SocketOption-based overloads (absent on JDK-8).
        check(hasMethod(c, "supportedOptions"), "supportedOptions method (JDK 13+)");
        boolean hasGetOpt = false, hasSetOpt = false;
        for (Method m : c.getDeclaredMethods()) {
            Class<?>[] ps = m.getParameterTypes();
            if (m.getName().equals("getOption") && ps.length == 1
                    && ps[0].getName().equals("java.net.SocketOption")
                    && !Modifier.isAbstract(m.getModifiers())) hasGetOpt = true;
            if (m.getName().equals("setOption") && ps.length == 2
                    && ps[0].getName().equals("java.net.SocketOption")
                    && !Modifier.isAbstract(m.getModifiers())) hasSetOpt = true;
        }
        check(hasGetOpt, "protected getOption(SocketOption) present");
        check(hasSetOpt, "protected setOption(SocketOption,T) present");
        check(Modifier.isPublic(c.getMethod("toString").getModifiers()), "toString public");
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
        // Exact toString form and base shutdown behavior.
        check("Socket[addr=null,port=0,localport=0]".equals(impl.toString()),
                "toString exact form, got: " + impl.toString());
        check("Method not implemented!".equals(impl.shutdownIn()),
                "shutdownInput base throws Method not implemented!");
        check("Method not implemented!".equals(impl.shutdownOut()),
                "shutdownOutput base throws Method not implemented!");
        // Abstract class cannot be instantiated directly.
        try {
            SocketImpl.class.getDeclaredConstructor().newInstance();
            check(false, "abstract class must not instantiate");
        } catch (InstantiationException e) {
            passed++;
        }
    }

    static void testSubclassIO() throws Exception {
        Mini2 m = new Mini2();
        check(m instanceof SocketOptions, "Mini2 is a SocketOptions");
        m.create(false);
        passed++;
        m.bind(InetAddress.getByName("127.0.0.1"), 0);
        passed++;
        m.listen(5);
        passed++;
        InputStream in = m.getInputStream();
        OutputStream out = m.getOutputStream();
        check(in != null, "getInputStream non-null");
        check(out != null, "getOutputStream non-null");
        check(in.read() == -1, "empty stream EOF");
        out.write(42);
        passed++;
        check(m.available() == 0, "available 0");
        try {
            m.connect(InetAddress.getByName("127.0.0.1"), 1234);
            check(false, "connect must throw per subclass");
        } catch (IOException e) {
            check("mini".equals(e.getMessage()), "subclass exception propagates");
        }
        m.close();
        passed++;
    }

    static void testSocketOptionsConformance() throws Exception {
        SocketOptions opts = new Mini2();
        opts.setOption(SocketOptions.TCP_NODELAY, Boolean.TRUE);
        passed++;
        check(opts.getOption(SocketOptions.TCP_NODELAY) == null, "getOption returns subclass value");
        check(SocketOptions.TCP_NODELAY == 1, "TCP_NODELAY value");
        check(SocketOptions.SO_TIMEOUT == 4102, "SO_TIMEOUT value");
        check(SocketOptions.SO_LINGER == 128, "SO_LINGER value");
        check(SocketOptions.SO_REUSEPORT == 14, "SO_REUSEPORT value (JDK-9+)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSubclassIO();
        testSocketOptionsConformance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
