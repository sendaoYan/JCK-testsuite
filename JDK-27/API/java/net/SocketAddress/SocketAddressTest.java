/*
 * @test
 * @summary java.net.SocketAddress conformance: white-box structure +
 *          black-box behavior - abstract base (public no-arg ctor,
 *          Serializable), InetSocketAddress and JDK 16+
 *          UnixDomainSocketAddress as implementations, polymorphic
 *          equals/hashCode, serialization round-trip through the base type,
 *          port-range validation, and exact "Unsupported address type"
 *          rejection of foreign implementations by Socket.bind.
 * @run main SocketAddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.net.*;

public class SocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Minimal concrete subclass: proves the base constructor is callable. */
    static class PlainAddress extends SocketAddress {
        final String id;
        PlainAddress(String id) { this.id = id; }
        @Override public boolean equals(Object o) {
            return o instanceof PlainAddress && ((PlainAddress) o).id.equals(id);
        }
        @Override public int hashCode() { return id.hashCode(); }
        @Override public String toString() { return "PlainAddress[" + id + "]"; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getDeclaredConstructor().getModifiers() != 0, "ctor accessible");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass is Object");
        check(InetSocketAddress.class.getSuperclass() == c, "InetSocketAddress extends");
        check(UnixDomainSocketAddress.class.getSuperclass() == c, "UnixDomainSocketAddress extends");
        int abstractOwn = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractOwn++;
        }
        check(abstractOwn == 0, "no abstract methods declared");
    }

    static void testBlackBox() throws Exception {
        // Abstract class: instantiate through an anonymous minimal concrete subclass, directly invoking the public no-arg constructor.
        check(Modifier.isAbstract(SocketAddress.class.getModifiers()), "abstract instance check");
        SocketAddress anon = new SocketAddress() { };
        check(anon != null, "anonymous concrete subclass invokes public no-arg constructor");
        check(anon instanceof SocketAddress, "anonymous subclass instanceof SocketAddress");
        SocketAddress sa = new InetSocketAddress(1234);
        check(sa instanceof SocketAddress, "InetSocketAddress instanceof SocketAddress");
        check(new InetSocketAddress(1234).equals(new InetSocketAddress(1234)), "subtype equals");
        check(new InetSocketAddress(1234).hashCode() == new InetSocketAddress(1234).hashCode(), "subtype hashCode");
    }

    static void testInstantiation() throws Exception {
        try {
            SocketAddress.class.getDeclaredConstructor().newInstance();
            check(false, "abstract class must not instantiate");
        } catch (InstantiationException e) {
            passed++;
        }
        SocketAddress b1 = new SocketAddress() { };
        SocketAddress b2 = new SocketAddress() { };
        check(b1.equals(b1), "anonymous self equals");
        check(!b1.equals(b2), "distinct anonymous instances unequal");
        check(b1.hashCode() == b1.hashCode(), "hashCode stable");
        check(b1.toString() != null, "toString non-null");
    }

    static void testPolymorphicEquals() throws Exception {
        SocketAddress isa1 = new InetSocketAddress("127.0.0.1", 80);
        SocketAddress isa2 = new InetSocketAddress("127.0.0.1", 80);
        SocketAddress isa3 = new InetSocketAddress("127.0.0.1", 81);
        check(isa1.equals(isa2), "equal InetSocketAddress wrap");
        check(isa1.hashCode() == isa2.hashCode(), "hashCode consistent with equals");
        check(!isa1.equals(isa3), "different port unequal");
        SocketAddress plain = new PlainAddress("127.0.0.1:80");
        check(!isa1.equals(plain), "cross-implementation not equal");
        check(!plain.equals(isa1), "cross-implementation not equal (symmetric)");
        check(!isa1.equals(null), "not equal to null");
        check(!isa1.equals(new Object()), "not equal to unrelated type");
        PlainAddress p1 = new PlainAddress("k");
        PlainAddress p2 = new PlainAddress("k");
        check(p1.equals(p2) && p2.equals(p1), "custom subclass equals symmetric");
        check(p1.hashCode() == p2.hashCode(), "custom subclass hashCode agrees");
        // Unix-domain addresses equal by path string.
        SocketAddress u1 = UnixDomainSocketAddress.of("/tmp/a.sock");
        SocketAddress u2 = UnixDomainSocketAddress.of("/tmp/a.sock");
        SocketAddress u3 = UnixDomainSocketAddress.of("/tmp/b.sock");
        check(u1.equals(u2), "unix equal by path");
        check(u1.hashCode() == u2.hashCode(), "unix hashCode agrees");
        check(!u1.equals(u3), "unix different path unequal");
        check(!u1.equals(isa1), "unix vs inet unequal");
        check(u1.toString().contains("/tmp/a.sock"), "unix toString contains path");
    }

    static void testSerialization() throws Exception {
        SocketAddress original = new InetSocketAddress("127.0.0.1", 7777);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        SocketAddress back = (SocketAddress) ois.readObject();
        check(back != null, "deserialized non-null");
        check(back instanceof InetSocketAddress, "deserialized concrete type");
        check(back.getClass() == InetSocketAddress.class, "deserialized exact class");
        check(back.equals(original), "deserialized equals original");
        check(back.hashCode() == original.hashCode(), "deserialized hashCode");
        check(((InetSocketAddress) back).getPort() == 7777, "deserialized port");
        check(((InetSocketAddress) back).getAddress().isLoopbackAddress(), "deserialized loopback address");
        SocketAddress unr = InetSocketAddress.createUnresolved("host.invalid", 42);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(unr);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        SocketAddress back2 = (SocketAddress) ois2.readObject();
        check(back2.equals(unr), "unresolved round trip");
        check(((InetSocketAddress) back2).isUnresolved(), "unresolved stays unresolved");
        check(java.io.Serializable.class.isAssignableFrom(SocketAddress.class),
                "SocketAddress implements Serializable");
    }

    static void testNullSemantics() throws Exception {
        SocketAddress a = new InetSocketAddress(1234);
        check(!a.equals(null), "equals(null) false");
        check(a.toString().contains("1234"), "toString mentions port");
        try {
            new InetSocketAddress("127.0.0.1", 65536);
            check(false, "port 65536 must fail");
        } catch (IllegalArgumentException e) {
            check("port out of range:65536".equals(e.getMessage()), "port 65536 message");
        }
        try {
            new InetSocketAddress("127.0.0.1", -1);
            check(false, "port -1 must fail");
        } catch (IllegalArgumentException e) {
            check("port out of range:-1".equals(e.getMessage()), "port -1 message");
        }
    }

    static void testUsageInSocketAPI() throws Exception {
        // Socket.bind accepts any SocketAddress implementation; a foreign
        // implementation is rejected with IllegalArgumentException.
        java.net.Socket s = new java.net.Socket();
        try {
            try {
                s.bind(new PlainAddress("nowhere"));
                check(false, "foreign SocketAddress must be rejected");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains("Unsupported address type"),
                        "unsupported address message, got: " + e.getMessage());
            }
        } finally {
            s.close();
        }
        java.net.Socket s2 = new java.net.Socket();
        try {
            s2.bind(null);
            check(s2.isBound(), "bind(null) binds");
            check(s2.getLocalPort() > 0, "bind(null) ephemeral port");
        } finally {
            s2.close();
        }
        // ServerSocket.bind rejects foreign implementations identically.
        java.net.ServerSocket ss = new java.net.ServerSocket();
        try {
            try {
                ss.bind(new PlainAddress("nowhere"));
                check(false, "server foreign SocketAddress must be rejected");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains("Unsupported address type"),
                        "server unsupported address message");
            }
        } finally {
            ss.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInstantiation();
        testPolymorphicEquals();
        testSerialization();
        testNullSemantics();
        testUsageInSocketAPI();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
