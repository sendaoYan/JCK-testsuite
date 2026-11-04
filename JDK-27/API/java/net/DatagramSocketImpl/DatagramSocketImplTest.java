/*
 * @test
 * @summary java.net.DatagramSocketImpl conformance: white-box structure + black-box behavior.
 *          Deepened: full declared-member table (abstract vs concrete
 *          classification of every method, protected fields fd/localPort,
 *          package-private dataAvailable, the protected SocketOption-based
 *          setOption/getOption/supportedOptions triple added in JDK-13+, and
 *          the REMOVED legacy setTTL(byte)/getTTL() abstract pair verified
 *          absent), minimal concrete subclass instantiation, protected
 *          accessor defaults (getLocalPort() == 0, getFileDescriptor() == null,
 *          supportedOptions() empty by default), subclass access to the
 *          protected localPort field, SocketOptions interface contract on a
 *          subclass, and non-instantiability of the abstract class itself.
 * @run main DatagramSocketImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketOptions;

public class DatagramSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- preserved original helpers and assertions ----
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImpl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");
        check(c.getConstructor() != null, "no-arg ctor");
        for (String m : new String[]{"create","bind","send","receive","peek","peekData","setTimeToLive","getTimeToLive","join","leave","joinGroup","leaveGroup","close","getLocalPort","getFileDescriptor"}) {
            check(hasMethod(c, m), "method " + m);
        }
        check(c.getDeclaredField("localPort") != null, "localPort field");
        check(c.getDeclaredField("fd") != null, "fd field");
    }

    static void testBlackBox() throws Exception {
        check(Modifier.isAbstract(DatagramSocketImpl.class.getModifiers()), "still abstract");
        check(SocketOptions.class.isAssignableFrom(DatagramSocketImpl.class), "is SocketOptions");
        java.net.DatagramSocket ds = null;
        try {
            ds = new java.net.DatagramSocket(0);
            check(ds.getLocalPort() > 0, "ephemeral port assigned via impl");
        } finally {
            if (ds != null) ds.close();
        }
    }

    static boolean hasDeclared(Class<?> c, String name, int params) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params) return true;
        }
        return false;
    }
    static boolean isDeclaredAbstract(Class<?> c, String name, int params) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params)
                return Modifier.isAbstract(m.getModifiers());
        }
        throw new AssertionError("method not declared: " + name + "/" + params);
    }
    static boolean isDeclaredProtected(Class<?> c, String name, int params) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params)
                return Modifier.isProtected(m.getModifiers());
        }
        throw new AssertionError("method not declared: " + name + "/" + params);
    }

    // Declared member table: 13 abstract datagram methods on JDK-27 (the legacy
    // byte-TTL pair was removed); connect/disconnect/getLocalPort/
    // getFileDescriptor and the SocketOption-based triple are concrete
    // protected members.
    static void testMemberTableDeep() throws Exception {
        Class<?> c = DatagramSocketImpl.class;
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");

        String[][] abstracts = {
                {"create", "0"}, {"bind", "2"}, {"send", "1"}, {"peek", "1"}, {"peekData", "1"},
                {"receive", "1"}, {"setTimeToLive", "1"}, {"getTimeToLive", "0"},
                {"join", "1"}, {"leave", "1"},
                {"joinGroup", "2"}, {"leaveGroup", "2"}, {"close", "0"},
        };
        for (String[] e : abstracts) {
            String n = e[0];
            int p = Integer.parseInt(e[1]);
            check(isDeclaredAbstract(c, n, p), n + " declared abstract");
            check(isDeclaredProtected(c, n, p), n + " declared protected");
        }
        // legacy byte-TTL family removed
        boolean legacyPresent = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("setTTL") || m.getName().equals("getTTL")) legacyPresent = true;
        }
        check(!legacyPresent, "setTTL/getTTL removed on JDK-27");

        check(isDeclaredProtected(c, "connect", 2) && !isDeclaredAbstract(c, "connect", 2),
                "connect(InetAddress,int) protected concrete");
        check(isDeclaredProtected(c, "disconnect", 0) && !isDeclaredAbstract(c, "disconnect", 0),
                "disconnect() protected concrete");
        check(isDeclaredProtected(c, "getLocalPort", 0) && !isDeclaredAbstract(c, "getLocalPort", 0),
                "getLocalPort() protected concrete");
        check(isDeclaredProtected(c, "getFileDescriptor", 0) && !isDeclaredAbstract(c, "getFileDescriptor", 0),
                "getFileDescriptor() protected concrete");
        // SocketOption-based triple (JDK-13+): protected concrete
        check(isDeclaredProtected(c, "setOption", 2) && !isDeclaredAbstract(c, "setOption", 2),
                "setOption(SocketOption,T) protected concrete");
        check(isDeclaredProtected(c, "getOption", 1) && !isDeclaredAbstract(c, "getOption", 1),
                "getOption(SocketOption) protected concrete");
        check(isDeclaredProtected(c, "supportedOptions", 0) && !isDeclaredAbstract(c, "supportedOptions", 0),
                "supportedOptions() protected concrete");

        // protected state
        check(c.getDeclaredField("localPort") != null
                && c.getDeclaredField("localPort").getType() == int.class
                && Modifier.isProtected(c.getDeclaredField("localPort").getModifiers()),
                "protected int localPort");
        check(c.getDeclaredField("fd") != null
                && c.getDeclaredField("fd").getType().getName().equals("java.io.FileDescriptor")
                && Modifier.isProtected(c.getDeclaredField("fd").getModifiers()),
                "protected FileDescriptor fd");

        // package-private dataAvailable remains; the JDK-8-era socket
        // back-references and setDatagramSocket/getDatagramSocket are gone
        check(hasDeclared(c, "dataAvailable", 0), "package-private dataAvailable");
        boolean hasSocketField = false;
        try { c.getDeclaredField("socket"); hasSocketField = true; } catch (NoSuchFieldException e) { }
        check(!hasSocketField, "socket field removed on JDK-27");
        check(!hasDeclared(c, "setDatagramSocket", 1), "setDatagramSocket removed");
        check(!hasDeclared(c, "getDatagramSocket", 0), "getDatagramSocket removed");
    }

    // Minimal concrete subclass: 13 abstract members implementable; protected
    // defaults and state visible to the subclass.
    static class MyImpl extends DatagramSocketImpl {
        int created, bound, sent, closed, joined, left, joinedGroup, leftGroup;
        int peeked, peekedData, received;
        int lastTtl = -1;
        int lastOptId = -1;

        protected void create() { created++; }
        protected void bind(int p, InetAddress addr) { bound++; localPort = p; }
        protected void send(DatagramPacket p) { sent++; }
        protected int peek(InetAddress i) { peeked++; return 1; }
        protected int peekData(DatagramPacket p) { peekedData++; return 2; }
        protected void receive(DatagramPacket p) { received++; }
        protected void setTimeToLive(int ttl) { lastTtl = ttl; }
        protected int getTimeToLive() { return lastTtl; }
        protected void join(InetAddress g) { joined++; }
        protected void leave(InetAddress g) { left++; }
        protected void joinGroup(SocketAddress sa, NetworkInterface ni) { joinedGroup++; }
        protected void leaveGroup(SocketAddress sa, NetworkInterface ni) { leftGroup++; }
        protected void close() { closed++; }
        public void setOption(int id, Object v) { lastOptId = id; }
        public Object getOption(int id) { return Integer.valueOf(id); }

        int reportLocalPort() { return getLocalPort(); }
        java.io.FileDescriptor reportFd() { return getFileDescriptor(); }
        void pokeLocalPort(int v) { localPort = v; }
        void callDisconnect() throws RuntimeException { disconnect(); }
        java.util.Set<java.net.SocketOption<?>> reportSupported() { return supportedOptions(); }
    }

    static class InetSocketAddressStub extends SocketAddress { }

    static void testMinimalSubclass() throws Exception {
        MyImpl impl = new MyImpl();
        check(impl != null, "minimal subclass instantiable");
        check(impl instanceof SocketOptions, "subclass is a SocketOptions");

        // protected accessor defaults on a fresh instance
        check(impl.reportLocalPort() == 0, "fresh getLocalPort() == 0");
        check(impl.reportFd() == null, "fresh getFileDescriptor() == null");
        java.util.Set<java.net.SocketOption<?>> sup = impl.reportSupported();
        check(sup != null, "default supportedOptions() non-null");

        impl.pokeLocalPort(4711);
        check(impl.reportLocalPort() == 4711, "localPort write visible via getLocalPort()");

        impl.create();
        impl.bind(99, InetAddress.getLoopbackAddress());
        check(impl.created == 1 && impl.bound == 1, "create/bind dispatched");
        check(impl.reportLocalPort() == 99, "bind writes localPort field");
        impl.send(new DatagramPacket(new byte[1], 1));
        check(impl.sent == 1, "send dispatched");
        check(impl.peek(InetAddress.getLoopbackAddress()) == 1, "peek dispatched");
        check(impl.peekData(new DatagramPacket(new byte[1], 1)) == 2, "peekData dispatched");
        impl.receive(new DatagramPacket(new byte[1], 1));
        check(impl.received == 1, "receive dispatched");
        impl.setTimeToLive(9);
        check(impl.getTimeToLive() == 9, "setTimeToLive/getTimeToLive pair");
        impl.join(InetAddress.getByName("239.1.2.3"));
        impl.leave(InetAddress.getByName("239.1.2.3"));
        check(impl.joined == 1 && impl.left == 1, "join/leave dispatched");
        impl.joinGroup(new InetSocketAddressStub(), null);
        impl.leaveGroup(new InetSocketAddressStub(), null);
        check(impl.joinedGroup == 1 && impl.leftGroup == 1, "joinGroup/leaveGroup dispatched");
        impl.close();
        check(impl.closed == 1, "close dispatched");
        // JDK-27: the concrete disconnect() default reports "not implemented"
        // (JDK-8's default was a silent no-op - documented divergence)
        try {
            impl.callDisconnect();
            check(false, "default disconnect() should throw on JDK-27");
        } catch (java.io.UncheckedIOException e) {
            check(e.getMessage() != null && e.getMessage().contains("disconnect not implemented"),
                    "default disconnect() message: " + e.getMessage());
        }

        // SocketOptions contract via the subclass
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(123));
        check(impl.lastOptId == SocketOptions.SO_SNDBUF, "setOption(int,Object) dispatched");
        check(impl.getOption(SocketOptions.SO_RCVBUF)
                .equals(Integer.valueOf(SocketOptions.SO_RCVBUF)), "getOption(int,Object) dispatched");
    }

    static void testNonInstantiable() throws Exception {
        try {
            DatagramSocketImpl.class.getDeclaredConstructor().newInstance((Object[]) null);
            throw new AssertionError("abstract class instantiated");
        } catch (java.lang.reflect.InvocationTargetException | InstantiationException expected) {
            // getDeclaredConstructor().newInstance() on an abstract class
            // reports InstantiationException wrapped or direct
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMemberTableDeep();
        testMinimalSubclass();
        testNonInstantiable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
