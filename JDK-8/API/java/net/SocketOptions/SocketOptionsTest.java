/*
 * @test
 * @summary java.net.SocketOptions conformance: interface structure with the
 *          exact two abstract methods (setOption(int,Object)/getOption(int)
 *          declaring SocketException), the full constant matrix with exact
 *          probed values (TCP_NODELAY=1 ... SO_OOBINLINE=4099; JDK-8 has no
 *          SO_REUSEPORT), public static final modifiers, distinctness,
 *          known implementors, and behavior of a custom implementation
 *          called through the interface.
 * @run main SocketOptionsTest
 */


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.SocketException;
import java.net.SocketOptions;
import java.util.HashSet;
import java.util.Set;

public class SocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketOptions");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        // Both methods abstract, no defaults, declaring SocketException.
        Method set = c.getMethod("setOption", int.class, Object.class);
        check(set != null, "setOption");
        check(Modifier.isAbstract(set.getModifiers()), "setOption abstract");
        check(set.getExceptionTypes().length == 1
                && set.getExceptionTypes()[0].getName().equals("java.net.SocketException"),
                "setOption throws SocketException");
        Method get = c.getMethod("getOption", int.class);
        check(get != null, "getOption");
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
        Class<?> c = SocketOptions.class;
        // Exact values (stable across platforms, probed on JDK-8 and JDK-27).
        check(SocketOptions.TCP_NODELAY == 0x0001, "TCP_NODELAY == 1");
        check(SocketOptions.SO_BINDADDR == 15, "SO_BINDADDR == 15");
        check(SocketOptions.SO_REUSEADDR == 0x0004, "SO_REUSEADDR == 4");
        check(SocketOptions.SO_BROADCAST == 0x0020, "SO_BROADCAST == 32");
        check(SocketOptions.IP_MULTICAST_IF == 16, "IP_MULTICAST_IF == 16");
        check(SocketOptions.IP_MULTICAST_IF2 == 31, "IP_MULTICAST_IF2 == 31");
        check(SocketOptions.IP_MULTICAST_LOOP == 18, "IP_MULTICAST_LOOP == 18");
        check(SocketOptions.IP_TOS == 3, "IP_TOS == 3");
        check(SocketOptions.SO_LINGER == 0x0080, "SO_LINGER == 128");
        check(SocketOptions.SO_TIMEOUT == 4102, "SO_TIMEOUT == 4102");
        check(SocketOptions.SO_SNDBUF == 4097, "SO_SNDBUF == 4097");
        check(SocketOptions.SO_RCVBUF == 4098, "SO_RCVBUF == 4098");
        check(SocketOptions.SO_KEEPALIVE == 0x0008, "SO_KEEPALIVE == 8");
        check(SocketOptions.SO_OOBINLINE == 4099, "SO_OOBINLINE == 4099");
        // JDK-8 does not define SO_REUSEPORT (added in JDK 9).
        try {
            Field f = c.getField("SO_REUSEPORT");
            check(f == null, "SO_REUSEPORT absent on JDK-8");
        } catch (NoSuchFieldException e) {
            passed++;
        }
        // Every constant is public static final int.
        String[] names = {"TCP_NODELAY","SO_BINDADDR","SO_REUSEADDR","SO_BROADCAST",
                "IP_MULTICAST_IF","IP_MULTICAST_IF2","IP_MULTICAST_LOOP","IP_TOS",
                "SO_LINGER","SO_TIMEOUT","SO_SNDBUF","SO_RCVBUF","SO_KEEPALIVE","SO_OOBINLINE"};
        for (String n : names) {
            Field f = c.getField(n);
            check(Modifier.isPublic(f.getModifiers()), n + " public");
            check(Modifier.isStatic(f.getModifiers()), n + " static");
            check(Modifier.isFinal(f.getModifiers()), n + " final");
            check(f.getType() == int.class, n + " int");
        }
        // Pairwise distinctness of the whole matrix.
        int[] vals = {SocketOptions.TCP_NODELAY, SocketOptions.SO_BINDADDR, SocketOptions.SO_REUSEADDR,
                SocketOptions.SO_BROADCAST, SocketOptions.IP_MULTICAST_IF, SocketOptions.IP_MULTICAST_IF2,
                SocketOptions.IP_MULTICAST_LOOP, SocketOptions.IP_TOS, SocketOptions.SO_LINGER,
                SocketOptions.SO_TIMEOUT, SocketOptions.SO_SNDBUF, SocketOptions.SO_RCVBUF,
                SocketOptions.SO_KEEPALIVE, SocketOptions.SO_OOBINLINE};
        Set<Integer> seen = new HashSet<Integer>();
        for (int v : vals) {
            check(!seen.contains(v), "constant " + v + " distinct");
            seen.add(v);
        }
        check(seen.size() == 14, "14 distinct constants on JDK-8, got " + seen.size());
        // The modern getsockopt-style options share the 0x1000 prefix.
        check((SocketOptions.SO_SNDBUF & 0x1000) != 0, "SO_SNDBUF has 0x1000 bit");
        check((SocketOptions.SO_TIMEOUT & 0x1000) != 0, "SO_TIMEOUT has 0x1000 bit");
        check((SocketOptions.SO_LINGER & 0x1000) == 0, "SO_LINGER is a legacy value");
    }

    static void testImplementors() throws Exception {
        // The two public implementors of the interface.
        check(java.net.SocketImpl.class.isAssignableFrom(java.net.SocketImpl.class)
                && SocketOptions.class.isAssignableFrom(java.net.SocketImpl.class),
                "SocketImpl implements SocketOptions");
        check(SocketOptions.class.isAssignableFrom(java.net.DatagramSocketImpl.class),
                "DatagramSocketImpl implements SocketOptions");
        // Socket and ServerSocket do NOT implement it directly.
        check(!SocketOptions.class.isAssignableFrom(java.net.Socket.class),
                "Socket does not implement SocketOptions");
        check(!SocketOptions.class.isAssignableFrom(java.net.ServerSocket.class),
                "ServerSocket does not implement SocketOptions");
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

    static void testCustomImplementation() throws Exception {
        RecordingOptions ro = new RecordingOptions();
        SocketOptions iface = ro;
        // setOption passes through id and value untouched.
        iface.setOption(SocketOptions.TCP_NODELAY, Boolean.TRUE);
        check(ro.lastSetId == SocketOptions.TCP_NODELAY, "setOption id passthrough");
        check(Boolean.TRUE.equals(ro.lastSetValue), "setOption value passthrough");
        // getOption result flows back.
        Object got = iface.getOption(SocketOptions.SO_TIMEOUT);
        check(got instanceof Integer && ((Integer) got).intValue() == SocketOptions.SO_TIMEOUT,
                "getOption result passthrough");
        check(ro.lastGetId == SocketOptions.SO_TIMEOUT, "getOption id passthrough");
        // A SocketException from the implementation propagates unchanged.
        try {
            iface.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(-1));
            check(false, "impl exception must propagate");
        } catch (SocketException e) {
            check("bad value".equals(e.getMessage()), "exception passthrough, got: " + e.getMessage());
        }
        // null values pass through without interface-level validation.
        iface.setOption(SocketOptions.SO_LINGER, null);
        check(ro.lastSetValue == null, "null value passthrough");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketOptions");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOption"), "has method getOption");
        check(names.contains("setOption"), "has method setOption");
        testWhiteBox();
        testConstantMatrix();
        testImplementors();
        testCustomImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
