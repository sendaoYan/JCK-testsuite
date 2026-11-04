/*
 * @test
 * @summary java.net.DatagramSocketImpl conformance: abstract class hierarchy and members.
 *          Deepened: full declared-member table (abstract vs concrete
 *          classification of every method, protected fields fd/localPort,
 *          package-private members socket/dataAvailable/setOption/getOption/
 *          setDatagramSocket/getDatagramSocket incl. the package-private
 *          SocketOption-based setOption/getOption pair backported to 8u),
 *          minimal concrete subclass instantiation proving every abstract
 *          member is implementable, protected accessor defaults
 *          (getLocalPort() == 0, getFileDescriptor() == null), subclass
 *          access to the protected localPort field, SocketOptions interface
 *          contract on a subclass, and non-instantiability of the abstract
 *          class itself.
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
import java.util.HashSet;
import java.util.Set;

public class DatagramSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- preserved original assertions, kept verbatim ----
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImpl");
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

    // Declared member table. The 16 abstract datagram methods on JDK-8 include
    // the legacy setTTL(byte)/getTTL() pair (removed in JDK-13+); connect,
    // disconnect, getLocalPort, getFileDescriptor and the package-private
    // SocketOption-based setOption/getOption are concrete.
    static void testMemberTable() throws Exception {
        Class<?> c = DatagramSocketImpl.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");
        check(c.getConstructor() != null, "implicit public no-arg ctor");

        String[][] abstracts = {
                {"create", "0"}, {"bind", "2"}, {"send", "1"}, {"peek", "1"}, {"peekData", "1"},
                {"receive", "1"}, {"setTTL", "1"}, {"getTTL", "0"},
                {"setTimeToLive", "1"}, {"getTimeToLive", "0"},
                {"join", "1"}, {"leave", "1"},
                {"joinGroup", "2"}, {"leaveGroup", "2"}, {"close", "0"},
        };
        for (String[] e : abstracts) {
            String n = e[0];
            int p = Integer.parseInt(e[1]);
            check(isDeclaredAbstract(c, n, p), n + " declared abstract");
            check(isDeclaredProtected(c, n, p), n + " declared protected");
        }
        check(isDeclaredProtected(c, "connect", 2) && !isDeclaredAbstract(c, "connect", 2),
                "connect(InetAddress,int) protected concrete");
        check(isDeclaredProtected(c, "disconnect", 0) && !isDeclaredAbstract(c, "disconnect", 0),
                "disconnect() protected concrete");
        check(isDeclaredProtected(c, "getLocalPort", 0) && !isDeclaredAbstract(c, "getLocalPort", 0),
                "getLocalPort() protected concrete");
        check(isDeclaredProtected(c, "getFileDescriptor", 0) && !isDeclaredAbstract(c, "getFileDescriptor", 0),
                "getFileDescriptor() protected concrete");

        // protected state
        check(c.getDeclaredField("localPort") != null
                && c.getDeclaredField("localPort").getType() == int.class
                && Modifier.isProtected(c.getDeclaredField("localPort").getModifiers()),
                "protected int localPort");
        check(c.getDeclaredField("fd") != null
                && c.getDeclaredField("fd").getType().getName().equals("java.io.FileDescriptor")
                && Modifier.isProtected(c.getDeclaredField("fd").getModifiers()),
                "protected FileDescriptor fd");

        // package-private members present on JDK-8 (not part of the exported
        // contract, but part of the class shape): the SocketOption-based
        // setOption/getOption pair (8u backport), dataAvailable, and the
        // DatagramSocket back-references
        check(hasDeclared(c, "dataAvailable", 0), "package-private dataAvailable");
        check(hasDeclared(c, "setDatagramSocket", 1), "package-private setDatagramSocket");
        check(hasDeclared(c, "getDatagramSocket", 0), "package-private getDatagramSocket");
        check(hasDeclared(c, "setOption", 2), "package-private setOption(SocketOption,T) on 8u");
        check(hasDeclared(c, "getOption", 1), "package-private getOption(SocketOption) on 8u");
        check(c.getDeclaredField("socket") != null, "package-private socket field");

        // the SocketOptions methods setOption(int,Object)/getOption(int) are
        // NOT re-declared by the class - they stay abstract via the interface
        boolean hasIntFlavour = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("setOption")
                    && m.getParameterTypes().length == 2
                    && m.getParameterTypes()[0] == int.class) hasIntFlavour = true;
        }
        check(!hasIntFlavour, "setOption(int,Object) not re-declared, resolved via SocketOptions");
    }

    // Minimal concrete subclass: every abstract member implementable; protected
    // defaults and state visible to the subclass.
    static class MyImpl extends DatagramSocketImpl {
        int created;
        int bound;
        int sent;
        int closed;
        int joined;
        int left;
        int joinedGroup;
        int leftGroup;
        int peeked;
        int peekedData;
        int received;
        int lastTtl = -1;
        int lastOptId = -1;

        protected void create() { created++; }
        protected void bind(int p, InetAddress addr) { bound++; localPort = p; }
        protected void send(DatagramPacket p) { sent++; }
        protected int peek(InetAddress i) { peeked++; return 1; }
        protected int peekData(DatagramPacket p) { peekedData++; return 2; }
        protected void receive(DatagramPacket p) { received++; }
        protected void setTTL(byte t) { lastTtl = t & 0xFF; }
        protected byte getTTL() { return (byte) lastTtl; }
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
        void callDisconnect() { disconnect(); }
        java.io.FileDescriptor reportFd() { return getFileDescriptor(); }
        void pokeLocalPort(int v) { localPort = v; }
    }

    static void testMinimalSubclass() throws Exception {
        check(Modifier.isAbstract(DatagramSocketImpl.class.getModifiers()),
                "class remains abstract");
        check(SocketOptions.class.isAssignableFrom(DatagramSocketImpl.class),
                "is SocketOptions");
        MyImpl impl = new MyImpl();
        check(impl != null, "minimal subclass instantiable");
        check(impl instanceof SocketOptions, "subclass is a SocketOptions");

        // protected accessor defaults on a fresh instance
        check(impl.reportLocalPort() == 0, "fresh getLocalPort() == 0");
        check(impl.reportFd() == null, "fresh getFileDescriptor() == null");

        // protected localPort field is subclass-writable and readable
        impl.pokeLocalPort(4711);
        check(impl.reportLocalPort() == 4711, "localPort write visible via getLocalPort()");

        // abstract methods are dispatchable and count calls
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
        impl.setTTL((byte) 5);
        check(impl.getTTL() == (byte) 5, "setTTL/getTTL pair");
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
        // concrete protected disconnect() default is a tolerated no-op
        impl.callDisconnect();
        check(true, "disconnect() default no-op on subclass");

        // SocketOptions contract via the subclass
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(123));
        check(impl.lastOptId == SocketOptions.SO_SNDBUF, "setOption(int,Object) dispatched");
        check(impl.getOption(SocketOptions.SO_RCVBUF)
                .equals(Integer.valueOf(SocketOptions.SO_RCVBUF)), "getOption(int,Object) dispatched");
    }

    static class InetSocketAddressStub extends SocketAddress { }

    // the abstract class itself cannot be instantiated
    static void testNonInstantiable() throws Exception {
        try {
            DatagramSocketImpl.class.newInstance();
            throw new AssertionError("abstract class instantiated");
        } catch (InstantiationException expected) {
            passed++;
        }
        // and a real socket still gets its port via the platform impl
        java.net.DatagramSocket ds = null;
        try {
            ds = new java.net.DatagramSocket(0);
            check(ds.getLocalPort() > 0, "ephemeral port assigned via impl");
        } finally {
            if (ds != null) ds.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testMemberTable();
        testMinimalSubclass();
        testNonInstantiable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
