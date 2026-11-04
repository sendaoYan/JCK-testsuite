/*
 * @test
 * @summary java.net.SocketAddress conformance: white-box structure + black-box behavior.
 * @run main SocketAddressTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(InetSocketAddress.class.getSuperclass() == c, "InetSocketAddress extends");
        check(UnixDomainSocketAddress.class.getSuperclass() == c, "UnixDomainSocketAddress extends");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
