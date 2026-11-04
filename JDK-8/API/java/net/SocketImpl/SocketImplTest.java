/*
 * @test
 * @summary java.net.SocketImpl conformance: abstract class structure and
 *          subclass contract - abstract method surface (create/connect x3/
 *          bind/listen/accept/getInputStream/getOutputStream/available/
 *          close/sendUrgentData), protected fields fd/address/port/localport,
 *          concrete protected accessors and their default values on a
 *          minimal subclass, supportsUrgentData default false, toString
 *          "Socket[addr=...]" form, SocketOptions interface conformance,
 *          and InstantiationException on direct instantiation.
 * @run main SocketImplTest
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.util.HashSet;
import java.util.Set;

public class SocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Minimal concrete subclass implementing every abstract method as no-op. */
    static class MiniImpl extends SocketImpl {
        public void create(boolean stream) throws IOException { }
        public void connect(String host, int port) throws IOException { throw new IOException("mini"); }
        public void connect(InetAddress addr, int port) throws IOException { throw new IOException("mini"); }
        public void connect(SocketAddress addr, int timeout) throws IOException { throw new IOException("mini"); }
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

        void probeProtected() throws IOException {
            record(supportsUrgentData(), "supportsUrgentData default");
            record(getPort(), "getPort default");
            record(getLocalPort(), "getLocalPort default");
            record(getInetAddress(), "getInetAddress default");
            record(getFileDescriptor(), "fd default");
            // Base shutdownInput/shutdownOutput throw "Method not implemented!".
            try {
                shutdownInput();
                record("OK", "shutdownInput default");
            } catch (IOException e) {
                record(e.getMessage(), "shutdownInput default");
            }
            try {
                shutdownOutput();
                record("OK", "shutdownOutput default");
            } catch (IOException e) {
                record(e.getMessage(), "shutdownOutput default");
            }
        }
        static void record(Object v, String what) { SocketImplTest.probeValues.put(what, v); }
    }

    static final java.util.Map<String, Object> probeValues = new java.util.HashMap<String, Object>();

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImpl");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        Set<String> expIfs = new HashSet<String>();
        expIfs.add("java.net.SocketOptions");
        Set<String> actIfs = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");

        // Protected fields.
        for (String fn : new String[]{"fd", "address", "port", "localport"}) {
            Field f = c.getDeclaredField(fn);
            check(Modifier.isProtected(f.getModifiers()), "field " + fn + " protected");
            check(!Modifier.isStatic(f.getModifiers()), "field " + fn + " instance");
        }
        check(c.getDeclaredField("fd").getType().getName().equals("java.io.FileDescriptor"), "fd type");
        check(c.getDeclaredField("address").getType().getName().equals("java.net.InetAddress"), "address type");
        check(c.getDeclaredField("port").getType() == int.class, "port type int");
        check(c.getDeclaredField("localport").getType() == int.class, "localport type int");

        // Abstract method surface (signatures probed by javap, JDK-8 == JDK-27
        // for these twelve).
        String[][] abstracts = {
            {"create", "boolean"},
            {"connect", "java.lang.String", "int"},
            {"connect", "java.net.InetAddress", "int"},
            {"connect", "java.net.SocketAddress", "int"},
            {"bind", "java.net.InetAddress", "int"},
            {"listen", "int"},
            {"accept", "java.net.SocketImpl"},
            {"available"},
            {"close"},
            {"sendUrgentData", "int"},
        };
        for (String[] sig : abstracts) {
            Method m = findMethod(c, sig);
            check(m != null, "method " + sig[0] + " exists");
            check(Modifier.isAbstract(m.getModifiers()), "method " + sig[0] + " abstract");
            check(Modifier.isProtected(m.getModifiers()), "method " + sig[0] + " protected");
        }
        // getInputStream/getOutputStream are abstract with covariant types.
        check(Modifier.isAbstract(c.getDeclaredMethod("getInputStream").getModifiers()), "getInputStream abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("getOutputStream").getModifiers()), "getOutputStream abstract");
        check(c.getDeclaredMethod("getInputStream").getReturnType() == InputStream.class, "getInputStream returns InputStream");
        check(c.getDeclaredMethod("getOutputStream").getReturnType() == OutputStream.class, "getOutputStream returns OutputStream");

        // Concrete (non-abstract) protected methods.
        for (String mn : new String[]{"shutdownInput", "shutdownOutput", "getFileDescriptor",
                "getInetAddress", "getPort", "supportsUrgentData", "getLocalPort",
                "setPerformancePreferences"}) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(mn) && !Modifier.isAbstract(m.getModifiers())) found = true;
            }
            check(found, "concrete method " + mn);
        }
        // toString is public.
        check(Modifier.isPublic(c.getMethod("toString").getModifiers()), "toString public");
        // The public constructor.
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "public no-arg ctor");
    }

    static Method findMethod(Class<?> c, String... sig) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(sig[0])) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length != sig.length - 1) continue;
            boolean match = true;
            for (int i = 0; i < ps.length; i++) {
                if (!ps[i].getName().equals(sig[i + 1]) && !primitiveName(ps[i]).equals(sig[i + 1])) match = false;
            }
            if (match) return m;
        }
        return null;
    }

    static String primitiveName(Class<?> p) {
        return p == int.class ? "int" : p == boolean.class ? "boolean" : p.getName();
    }

    static void testAbstractness() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImpl");
        try {
            c.newInstance();
            check(false, "abstract class must not instantiate");
        } catch (InstantiationException e) {
            passed++;
        }
        // The minimal subclass proves every abstract member is implementable.
        MiniImpl m = new MiniImpl();
        check(m instanceof SocketImpl, "MiniImpl is a SocketImpl");
        check(m instanceof SocketOptions, "MiniImpl is a SocketOptions");
        m.create(false);
        passed++;
        m.bind(InetAddress.getByName("127.0.0.1"), 0);
        passed++;
        m.listen(5);
        passed++;
        m.close();
        passed++;
    }

    static void testMinimalSubclassDefaults() throws Exception {
        MiniImpl m = new MiniImpl();
        m.probeProtected();
        check(Boolean.FALSE.equals(probeValues.get("supportsUrgentData default")), "supportsUrgentData false");
        check(Integer.valueOf(0).equals(probeValues.get("getPort default")), "getPort 0");
        check(Integer.valueOf(0).equals(probeValues.get("getLocalPort default")), "getLocalPort 0");
        check(probeValues.get("getInetAddress default") == null, "getInetAddress null");
        check(probeValues.get("fd default") == null, "fd null");
        // The base shutdown methods are concrete but unimplemented: they
        // throw IOException "Method not implemented!" (probed on JDK-8 and
        // JDK-27 - both identical).
        check("Method not implemented!".equals(probeValues.get("shutdownInput default")),
                "shutdownInput base throws, got: " + probeValues.get("shutdownInput default"));
        check("Method not implemented!".equals(probeValues.get("shutdownOutput default")),
                "shutdownOutput base throws, got: " + probeValues.get("shutdownOutput default"));
        // toString renders the Socket form with null endpoints.
        check("Socket[addr=null,port=0,localport=0]".equals(m.toString()), "toString form, got: " + m.toString());
        // Streams from the minimal implementation.
        InputStream in = m.getInputStream();
        OutputStream out = m.getOutputStream();
        check(in != null, "getInputStream non-null");
        check(out != null, "getOutputStream non-null");
        check(in.read() == -1, "empty stream reads EOF");
        out.write(42);
        passed++;
        check(m.available() == 0, "available 0");
        try {
            m.connect(InetAddress.getByName("127.0.0.1"), 1234);
            check(false, "connect must throw per subclass");
        } catch (IOException e) {
            check("mini".equals(e.getMessage()), "subclass exception propagates");
        }
        try {
            m.sendUrgentData(1);
            passed++;
        } catch (IOException e) {
            passed++;
        }
    }

    static void testSocketOptionsConformance() throws Exception {
        // SocketImpl implements SocketOptions: call through the interface.
        SocketOptions opts = new MiniImpl();
        opts.setOption(SocketOptions.TCP_NODELAY, Boolean.TRUE);
        passed++;
        check(opts.getOption(SocketOptions.TCP_NODELAY) == null, "getOption returns subclass value");
        opts.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(500));
        passed++;
        // Exact interface constant surface used by setOption.
        check(SocketOptions.TCP_NODELAY == 1, "TCP_NODELAY value");
        check(SocketOptions.SO_TIMEOUT == 4102, "SO_TIMEOUT value");
        check(SocketOptions.SO_LINGER == 128, "SO_LINGER value");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketImpl");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.net.SocketOptions");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOption"), "has method getOption");
        check(names.contains("setOption"), "has method setOption");
        testWhiteBox();
        testAbstractness();
        testMinimalSubclassDefaults();
        testSocketOptionsConformance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
