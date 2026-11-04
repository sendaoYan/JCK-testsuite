/*
 * @test
 * @summary java.net.StandardSocketOptions conformance: white-box structure + black-box behavior.
 * @run main StandardSocketOptionsTest
 */

import java.lang.reflect.*;
import java.net.*;

public class StandardSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.StandardSocketOptions");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "private ctor");
        for (String f : new String[]{"SO_BROADCAST","SO_KEEPALIVE","SO_SNDBUF","SO_RCVBUF","SO_REUSEADDR","SO_REUSEPORT","SO_LINGER","IP_TOS","IP_MULTICAST_IF","IP_MULTICAST_TTL","IP_MULTICAST_LOOP","TCP_NODELAY"}) {
            check(c.getField(f) != null, "field " + f);
        }
    }

    static void testBlackBox() throws Exception {
        // SocketOption contract: name() and type()
        check(StandardSocketOptions.SO_REUSEADDR.name().equals("SO_REUSEADDR"), "SO_REUSEADDR name");
        check(StandardSocketOptions.SO_REUSEADDR.type() == Boolean.class, "SO_REUSEADDR Boolean");
        check(StandardSocketOptions.SO_KEEPALIVE.type() == Boolean.class, "SO_KEEPALIVE Boolean");
        check(StandardSocketOptions.TCP_NODELAY.type() == Boolean.class, "TCP_NODELAY Boolean");
        check(StandardSocketOptions.IP_MULTICAST_LOOP.type() == Boolean.class, "IP_MULTICAST_LOOP Boolean");
        check(StandardSocketOptions.SO_SNDBUF.type() == Integer.class, "SO_SNDBUF Integer");
        check(StandardSocketOptions.SO_RCVBUF.type() == Integer.class, "SO_RCVBUF Integer");
        check(StandardSocketOptions.SO_LINGER.type() == Integer.class, "SO_LINGER Integer");
        check(StandardSocketOptions.IP_TOS.type() == Integer.class, "IP_TOS Integer");
        check(StandardSocketOptions.IP_MULTICAST_TTL.type() == Integer.class, "IP_MULTICAST_TTL Integer");
        check(StandardSocketOptions.IP_MULTICAST_IF.type() == NetworkInterface.class, "IP_MULTICAST_IF NetworkInterface");
        check(StandardSocketOptions.SO_REUSEPORT.name().equals("SO_REUSEPORT"), "SO_REUSEPORT name");

        // each option object is a distinct identity
        check(StandardSocketOptions.SO_KEEPALIVE != StandardSocketOptions.SO_BROADCAST, "distinct options");
        check(StandardSocketOptions.SO_SNDBUF.name().equals("SO_SNDBUF"), "SO_SNDBUF name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
