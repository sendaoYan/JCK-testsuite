/*
 * @test
 * @summary java.net.SocketOptions conformance: white-box structure +
 *          black-box behavior - exact two abstract methods
 *          (setOption(int,Object)/getOption(int) declaring SocketException),
 *          full constant matrix with exact probed values (TCP_NODELAY=1 ...
 *          SO_OOBINLINE=4099, SO_REUSEPORT=14 present since JDK 9),
 *          public static final modifiers, pairwise distinctness, known
 *          implementors, and behavior of a custom implementation called
 *          through the interface.
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
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getMethod("setOption", int.class, Object.class) != null, "setOption");
        check(c.getMethod("getOption", int.class) != null, "getOption");
        for (String f : new String[]{"TCP_NODELAY","SO_BINDADDR","SO_REUSEADDR","SO_REUSEPORT","SO_BROADCAST","IP_MULTICAST_IF","IP_MULTICAST_IF2","IP_MULTICAST_LOOP","IP_TOS","SO_LINGER","SO_TIMEOUT","SO_SNDBUF","SO_RCVBUF","SO_KEEPALIVE","SO_OOBINLINE"}) {
            check(c.getField(f) != null, "field " + f);
        }
        Method set = c.getMethod("setOption", int.class, Object.class);
        check(Modifier.isAbstract(set.getModifiers()), "setOption abstract");
        check(set.getExceptionTypes().length == 1
                && set.getExceptionTypes()[0].getName().equals("java.net.SocketException"),
                "setOption throws SocketException");
        Method get = c.getMethod("getOption", int.class);
        check(Modifier.isAbstract(get.getModifiers()), "getOption abstract");
        check(get.getExceptionTypes().length == 1
                && get.getExceptionTypes()[0].getName().equals("java.net.SocketException"),
                "getOption throws SocketException");
        check(get.getReturnType() == Object.class, "getOption returns Object");
        int declared = 0;
        for (Method m : c.getDeclaredMethods()) declared++;
        check(declared == 2, "exactly two declared methods, got " + declared);
    }

    static void testConstantMatrix() throws Exception {
        // Documented constant values (stable across platforms, probed on
        // JDK-8 and JDK-27; identical except SO_REUSEPORT is JDK-9+).
        check(SocketOptions.TCP_NODELAY == 0x0001, "TCP_NODELAY");
        check(SocketOptions.SO_BINDADDR == 15, "SO_BINDADDR == 15");
        check(SocketOptions.SO_REUSEADDR == 0x04, "SO_REUSEADDR");
        check(SocketOptions.SO_REUSEPORT == 0x0E, "SO_REUSEPORT == 14 (JDK-9+)");
        check(SocketOptions.SO_BROADCAST == 0x0020, "SO_BROADCAST");
        check(SocketOptions.IP_MULTICAST_IF == 16, "IP_MULTICAST_IF == 16");
        check(SocketOptions.IP_MULTICAST_IF2 == 31, "IP_MULTICAST_IF2 == 31");
        check(SocketOptions.IP_MULTICAST_LOOP == 18, "IP_MULTICAST_LOOP == 18");
        check(SocketOptions.IP_TOS == 3, "IP_TOS == 3");
        check(SocketOptions.SO_LINGER == 0x0080, "SO_LINGER");
        check(SocketOptions.SO_TIMEOUT == 4102, "SO_TIMEOUT == 4102");
        check(SocketOptions.SO_SNDBUF == 4097, "SO_SNDBUF == 4097");
        check(SocketOptions.SO_RCVBUF == 4098, "SO_RCVBUF == 4098");
        check(SocketOptions.SO_KEEPALIVE == 0x0008, "SO_KEEPALIVE");
        check(SocketOptions.SO_OOBINLINE == 4099, "SO_OOBINLINE == 4099");
        check(SocketOptions.SO_REUSEADDR != SocketOptions.SO_REUSEPORT, "distinct reuse constants");
        check(SocketOptions.SO_SNDBUF != SocketOptions.SO_RCVBUF, "distinct buffer constants");
        String[] names = {"TCP_NODELAY","SO_BINDADDR","SO_REUSEADDR","SO_REUSEPORT","SO_BROADCAST",
                "IP_MULTICAST_IF","IP_MULTICAST_IF2","IP_MULTICAST_LOOP","IP_TOS",
                "SO_LINGER","SO_TIMEOUT","SO_SNDBUF","SO_RCVBUF","SO_KEEPALIVE","SO_OOBINLINE"};
        Class<?> c = SocketOptions.class;
        for (String n : names) {
            Field f = c.getField(n);
            check(Modifier.isPublic(f.getModifiers()), n + " public");
            check(Modifier.isStatic(f.getModifiers()), n + " static");
            check(Modifier.isFinal(f.getModifiers()), n + " final");
            check(f.getType() == int.class, n + " int");
        }
        int[] vals = {SocketOptions.TCP_NODELAY, SocketOptions.SO_BINDADDR, SocketOptions.SO_REUSEADDR,
                SocketOptions.SO_REUSEPORT, SocketOptions.SO_BROADCAST, SocketOptions.IP_MULTICAST_IF,
                SocketOptions.IP_MULTICAST_IF2, SocketOptions.IP_MULTICAST_LOOP, SocketOptions.IP_TOS,
                SocketOptions.SO_LINGER, SocketOptions.SO_TIMEOUT, SocketOptions.SO_SNDBUF,
                SocketOptions.SO_RCVBUF, SocketOptions.SO_KEEPALIVE, SocketOptions.SO_OOBINLINE};
        java.util.Set<Integer> seen = new java.util.HashSet<Integer>();
        for (int v : vals) {
            check(!seen.contains(v), "constant " + v + " distinct");
            seen.add(v);
        }
        check(seen.size() == 15, "15 distinct constants on JDK-27, got " + seen.size());
        check((SocketOptions.SO_SNDBUF & 0x1000) != 0, "SO_SNDBUF has 0x1000 bit");
        check((SocketOptions.SO_TIMEOUT & 0x1000) != 0, "SO_TIMEOUT has 0x1000 bit");
        check((SocketOptions.SO_LINGER & 0x1000) == 0, "SO_LINGER is a legacy value");
    }

    static class RecordingOptions implements SocketOptions {
        int lastSetId = -1;
        Object lastSetValue;
        int lastGetId = -1;
        public void setOption(int optID, Object value) throws SocketException {
            lastSetId = optID;
            lastSetValue = value;
            if (value instanceof Integer && ((Integer) value).intValue() < 0) {
                throw new SocketException("bad value");
            }
        }
        public Object getOption(int optID) throws SocketException {
            lastGetId = optID;
            return Integer.valueOf(optID);
        }
    }

    static void testImplementors() throws Exception {
        check(SocketOptions.class.isAssignableFrom(java.net.SocketImpl.class),
                "SocketImpl implements SocketOptions");
        check(SocketOptions.class.isAssignableFrom(java.net.DatagramSocketImpl.class),
                "DatagramSocketImpl implements SocketOptions");
        check(!SocketOptions.class.isAssignableFrom(java.net.Socket.class),
                "Socket does not implement SocketOptions");
        check(!SocketOptions.class.isAssignableFrom(java.net.ServerSocket.class),
                "ServerSocket does not implement SocketOptions");
    }

    static void testCustomImplementation() throws Exception {
        RecordingOptions ro = new RecordingOptions();
        SocketOptions iface = ro;
        iface.setOption(SocketOptions.TCP_NODELAY, Boolean.TRUE);
        check(ro.lastSetId == SocketOptions.TCP_NODELAY, "setOption id passthrough");
        check(Boolean.TRUE.equals(ro.lastSetValue), "setOption value passthrough");
        Object got = iface.getOption(SocketOptions.SO_TIMEOUT);
        check(got instanceof Integer && ((Integer) got).intValue() == SocketOptions.SO_TIMEOUT,
                "getOption result passthrough");
        check(ro.lastGetId == SocketOptions.SO_TIMEOUT, "getOption id passthrough");
        try {
            iface.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(-1));
            check(false, "impl exception must propagate");
        } catch (SocketException e) {
            check("bad value".equals(e.getMessage()), "exception passthrough, got: " + e.getMessage());
        }
        iface.setOption(SocketOptions.SO_LINGER, null);
        check(ro.lastSetValue == null, "null value passthrough");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstantMatrix();
        testImplementors();
        testCustomImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
