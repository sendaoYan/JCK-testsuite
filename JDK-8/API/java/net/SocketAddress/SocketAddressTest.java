/*
 * @test
 * @summary java.net.SocketAddress conformance: abstract base class structure
 *          (public no-arg constructor, Serializable), InetSocketAddress as
 *          the standard implementation, polymorphic equals/hashCode across
 *          implementations, serialization round-trip through the base type,
 *          and null/identity semantics of the inherited Object contract.
 * @run main SocketAddressTest
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

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
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        Set<String> expIfs = new HashSet<String>();
        expIfs.add("java.io.Serializable");
        Set<String> actIfs = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        // The no-arg constructor is public (inherited by subclasses).
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()), "no-arg ctor public");
        // No abstract instance methods of its own: subclass bodies define all.
        int abstractOwn = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractOwn++;
        }
        check(abstractOwn == 0, "no abstract methods declared");
        // InetSocketAddress is the platform implementation.
        check(InetSocketAddress.class.getSuperclass() == c, "InetSocketAddress extends SocketAddress");
    }

    static void testInstantiation() throws Exception {
        // Direct instantiation is impossible (abstract).
        try {
            SocketAddress.class.newInstance();
            check(false, "abstract class must not instantiate");
        } catch (InstantiationException e) {
            passed++;
        }
        // A minimal concrete subclass invokes the public no-arg constructor.
        SocketAddress a = new PlainAddress("x");
        check(a != null, "anonymous concrete subclass instance");
        check(a instanceof SocketAddress, "instanceof SocketAddress");
        check(a instanceof java.io.Serializable, "Serializable through base");
        // The default equals/hashCode (Object) apply unless overridden.
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
        // Cross-implementation inequality through the base type.
        SocketAddress plain = new PlainAddress("127.0.0.1:80");
        check(!isa1.equals(plain), "cross-implementation not equal");
        check(!plain.equals(isa1), "cross-implementation not equal (symmetric)");
        check(!isa1.equals(null), "not equal to null");
        check(!isa1.equals(new Object()), "not equal to unrelated type");
        PlainAddress p1 = new PlainAddress("k");
        PlainAddress p2 = new PlainAddress("k");
        check(p1.equals(p2) && p2.equals(p1), "custom subclass equals symmetric");
        check(p1.hashCode() == p2.hashCode(), "custom subclass hashCode agrees");
    }

    static void testSerialization() throws Exception {
        // Round-trip through the abstract base type.
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
        check(((InetSocketAddress) back).getAddress().isLoopbackAddress(),
                "deserialized loopback address");
        // Unresolved addresses also serialize through the base type.
        SocketAddress unr = InetSocketAddress.createUnresolved("host.invalid", 42);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(unr);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        SocketAddress back2 = (SocketAddress) ois2.readObject();
        check(back2.equals(unr), "unresolved round trip");
        check(((InetSocketAddress) back2).isUnresolved(), "unresolved stays unresolved");
        // Serializability is a property of the base type contract.
        check(java.io.Serializable.class.isAssignableFrom(SocketAddress.class),
                "SocketAddress implements Serializable");
    }

    static void testNullSemantics() throws Exception {
        // The base class itself defines no null-taking methods; assert the
        // Object contract is inherited unchanged for null comparisons.
        SocketAddress a = new InetSocketAddress(1234);
        check(!a.equals(null), "equals(null) false");
        check(a.toString().contains("1234"), "toString mentions port");
        // InetSocketAddress rejects out-of-range ports at construction.
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
        // bind(null) means "bind to an ephemeral wildcard port" per contract.
        java.net.Socket s2 = new java.net.Socket();
        try {
            s2.bind(null);
            check(s2.isBound(), "bind(null) binds");
            check(s2.getLocalPort() > 0, "bind(null) ephemeral port");
        } finally {
            s2.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketAddress");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        testWhiteBox();
        testInstantiation();
        testPolymorphicEquals();
        testSerialization();
        testNullSemantics();
        testUsageInSocketAPI();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
