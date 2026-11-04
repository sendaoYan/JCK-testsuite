/*
 * @test
 * @summary java.net.SocketOptions conformance: white-box structure + black-box behavior.
 * @run main SocketOptionsTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketOptions");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("setOption", int.class, Object.class) != null, "setOption");
        check(c.getMethod("getOption", int.class) != null, "getOption");
        for (String f : new String[]{"TCP_NODELAY","SO_BINDADDR","SO_REUSEADDR","SO_REUSEPORT","SO_BROADCAST","IP_MULTICAST_IF","IP_MULTICAST_IF2","IP_MULTICAST_LOOP","IP_TOS","SO_LINGER","SO_TIMEOUT","SO_SNDBUF","SO_RCVBUF","SO_KEEPALIVE","SO_OOBINLINE"}) {
            check(c.getField(f) != null, "field " + f);
        }
    }

    static void testBlackBox() throws Exception {
        // Documented constant values (stable across platforms).
        check(SocketOptions.TCP_NODELAY == 0x0001, "TCP_NODELAY");
        check(SocketOptions.SO_REUSEADDR == 0x04, "SO_REUSEADDR");
        check(SocketOptions.SO_REUSEPORT == 0x0E, "SO_REUSEPORT");
        check(SocketOptions.SO_KEEPALIVE == 0x0008, "SO_KEEPALIVE");
        check(SocketOptions.SO_LINGER == 0x0080, "SO_LINGER");
        check(SocketOptions.SO_BROADCAST == 0x0020, "SO_BROADCAST");
        check(SocketOptions.SO_REUSEADDR != SocketOptions.SO_REUSEPORT, "distinct reuse constants");
        check(SocketOptions.SO_SNDBUF != SocketOptions.SO_RCVBUF, "distinct buffer constants");
        // all constants positive
        int[] cs = {SocketOptions.TCP_NODELAY, SocketOptions.SO_BINDADDR, SocketOptions.SO_REUSEADDR,
                    SocketOptions.SO_REUSEPORT, SocketOptions.SO_BROADCAST, SocketOptions.IP_MULTICAST_IF,
                    SocketOptions.IP_MULTICAST_IF2, SocketOptions.IP_MULTICAST_LOOP, SocketOptions.IP_TOS,
                    SocketOptions.SO_LINGER, SocketOptions.SO_TIMEOUT, SocketOptions.SO_SNDBUF,
                    SocketOptions.SO_RCVBUF, SocketOptions.SO_KEEPALIVE, SocketOptions.SO_OOBINLINE};
        for (int v : cs) check(v >= 0, "constant non-negative " + v);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
