/*
 * @test
 * @summary java.net.SocketOption conformance: white-box structure + black-box behavior.
 * @run main SocketOptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketOption");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("name") != null, "name()");
        check(c.getMethod("type") != null, "type()");
    }

    static void testBlackBox() throws Exception {
        // StandardSocketOptions provides the canonical implementations.
        SocketOption<?>[] opts = {
            StandardSocketOptions.SO_KEEPALIVE, StandardSocketOptions.SO_REUSEADDR,
            StandardSocketOptions.SO_SNDBUF, StandardSocketOptions.SO_RCVBUF,
            StandardSocketOptions.TCP_NODELAY, StandardSocketOptions.IP_TOS,
            StandardSocketOptions.IP_MULTICAST_TTL, StandardSocketOptions.IP_MULTICAST_IF,
            StandardSocketOptions.IP_MULTICAST_LOOP, StandardSocketOptions.SO_LINGER
        };
        for (SocketOption<?> o : opts) {
            check(o.name() != null && !o.name().isEmpty(), "name non-empty");
            check(o.type() != null, "type non-null");
        }
        check(StandardSocketOptions.SO_KEEPALIVE.name().equals("SO_KEEPALIVE"), "name exact");
        check(StandardSocketOptions.SO_KEEPALIVE.type() == Boolean.class, "type Boolean");
        check(StandardSocketOptions.SO_RCVBUF.type() == Integer.class, "type Integer");
        check(StandardSocketOptions.IP_MULTICAST_IF.type() == NetworkInterface.class, "type NetworkInterface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
