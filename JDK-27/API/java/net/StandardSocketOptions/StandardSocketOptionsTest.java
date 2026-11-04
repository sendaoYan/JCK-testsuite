/*
 * @test
 * @summary java.net.StandardSocketOptions conformance: white-box structure +
 *          black-box behavior - utility class (final, private constructor),
 *          the complete 12-field option matrix (JDK 9 adds SO_REUSEPORT)
 *          with exact name() and type() per option, identity distinctness,
 *          reflective generic types, and conformance through the
 *          SocketOption interface.
 * @run main StandardSocketOptionsTest
 */

import java.lang.reflect.*;
import java.net.*;

public class StandardSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkNameType(SocketOption<?> o, String name, Class<?> type) {
        check(o != null, name + " non-null");
        check(name.equals(o.name()), name + " name exact, got " + o.name());
        check(type == o.type(), name + " type exact, got " + o.type());
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = StandardSocketOptions.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "private ctor");
        for (String f : new String[]{"SO_BROADCAST","SO_KEEPALIVE","SO_SNDBUF","SO_RCVBUF","SO_REUSEADDR","SO_REUSEPORT","SO_LINGER","IP_TOS","IP_MULTICAST_IF","IP_MULTICAST_TTL","IP_MULTICAST_LOOP","TCP_NODELAY"}) {
            check(c.getField(f) != null, "field " + f);
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()), f + " public");
            check(Modifier.isStatic(fd.getModifiers()), f + " static");
            check(Modifier.isFinal(fd.getModifiers()), f + " final");
        }
        int publicStatic = 0;
        for (Field f : c.getFields()) {
            if (f.getDeclaringClass() == c && Modifier.isStatic(f.getModifiers())) publicStatic++;
        }
        check(publicStatic == 12, "exactly 12 static fields, got " + publicStatic);
    }

    static void testOptionMatrix() throws Exception {
        checkNameType(StandardSocketOptions.SO_BROADCAST, "SO_BROADCAST", Boolean.class);
        checkNameType(StandardSocketOptions.SO_KEEPALIVE, "SO_KEEPALIVE", Boolean.class);
        checkNameType(StandardSocketOptions.SO_SNDBUF, "SO_SNDBUF", Integer.class);
        checkNameType(StandardSocketOptions.SO_RCVBUF, "SO_RCVBUF", Integer.class);
        checkNameType(StandardSocketOptions.SO_REUSEADDR, "SO_REUSEADDR", Boolean.class);
        checkNameType(StandardSocketOptions.SO_REUSEPORT, "SO_REUSEPORT", Boolean.class);
        checkNameType(StandardSocketOptions.SO_LINGER, "SO_LINGER", Integer.class);
        checkNameType(StandardSocketOptions.IP_TOS, "IP_TOS", Integer.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_IF, "IP_MULTICAST_IF", NetworkInterface.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_TTL, "IP_MULTICAST_TTL", Integer.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_LOOP, "IP_MULTICAST_LOOP", Boolean.class);
        checkNameType(StandardSocketOptions.TCP_NODELAY, "TCP_NODELAY", Boolean.class);
        SocketOption<?>[] all = {StandardSocketOptions.SO_BROADCAST, StandardSocketOptions.SO_KEEPALIVE,
                StandardSocketOptions.SO_SNDBUF, StandardSocketOptions.SO_RCVBUF,
                StandardSocketOptions.SO_REUSEADDR, StandardSocketOptions.SO_REUSEPORT,
                StandardSocketOptions.SO_LINGER, StandardSocketOptions.IP_TOS,
                StandardSocketOptions.IP_MULTICAST_IF, StandardSocketOptions.IP_MULTICAST_TTL,
                StandardSocketOptions.IP_MULTICAST_LOOP, StandardSocketOptions.TCP_NODELAY};
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (SocketOption<?> o : all) {
            check(o instanceof SocketOption, o.name() + " is SocketOption");
            check(!names.contains(o.name()), "name " + o.name() + " unique");
            names.add(o.name());
        }
        check(names.size() == 12, "12 distinct option names");
        check(StandardSocketOptions.SO_KEEPALIVE != StandardSocketOptions.SO_BROADCAST, "distinct options");
        check(StandardSocketOptions.SO_SNDBUF != StandardSocketOptions.SO_RCVBUF, "distinct buffers");
        check(StandardSocketOptions.SO_REUSEADDR != StandardSocketOptions.SO_REUSEPORT,
                "distinct reuse options");
        // Distinct names carry distinct identities (singleton semantics).
        check(StandardSocketOptions.SO_REUSEPORT != StandardSocketOptions.SO_REUSEADDR,
                "SO_REUSEPORT distinct identity");
    }

    static void testGenericTypes() throws Exception {
        Class<?> c = StandardSocketOptions.class;
        check(c.getField("SO_SNDBUF").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.lang.Integer>"), "SO_SNDBUF generic type");
        check(c.getField("SO_KEEPALIVE").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.lang.Boolean>"), "SO_KEEPALIVE generic type");
        check(c.getField("IP_MULTICAST_IF").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.net.NetworkInterface>"), "IP_MULTICAST_IF generic type");
        check(c.getField("SO_REUSEPORT").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.lang.Boolean>"), "SO_REUSEPORT generic type");
    }

    static void testInterfaceConformance() throws Exception {
        SocketOption<Integer> snd = StandardSocketOptions.SO_SNDBUF;
        check("SO_SNDBUF".equals(snd.name()), "via interface name");
        check(Integer.class == snd.type(), "via interface type");
        java.util.List<String> all = new java.util.ArrayList<String>();
        for (SocketOption<?> o : new SocketOption<?>[]{StandardSocketOptions.SO_BROADCAST,
                StandardSocketOptions.SO_KEEPALIVE, StandardSocketOptions.SO_SNDBUF,
                StandardSocketOptions.SO_RCVBUF, StandardSocketOptions.SO_REUSEADDR,
                StandardSocketOptions.SO_REUSEPORT, StandardSocketOptions.SO_LINGER,
                StandardSocketOptions.IP_TOS, StandardSocketOptions.IP_MULTICAST_IF,
                StandardSocketOptions.IP_MULTICAST_TTL, StandardSocketOptions.IP_MULTICAST_LOOP,
                StandardSocketOptions.TCP_NODELAY}) {
            all.add(o.name() + ":" + o.type().getSimpleName());
        }
        check(all.size() == 12, "12 rendered options");
        check(all.get(0).equals("SO_BROADCAST:Boolean"), "first rendering, got " + all.get(0));
        check(all.get(5).equals("SO_REUSEPORT:Boolean"), "SO_REUSEPORT rendering, got " + all.get(5));
        check(all.get(7).equals("IP_TOS:Integer"), "IP_TOS rendering, got " + all.get(7));
        check(all.get(11).equals("TCP_NODELAY:Boolean"), "TCP_NODELAY rendering");
    }

    static void testSocketSupport() throws Exception {
        // Options reported as supported by a plain Socket (JDK 9+ API).
        java.net.Socket s = new java.net.Socket();
        try {
            java.util.Set<java.net.SocketOption<?>> supported = s.supportedOptions();
            check(supported.contains(StandardSocketOptions.SO_SNDBUF), "Socket supports SO_SNDBUF");
            check(supported.contains(StandardSocketOptions.SO_RCVBUF), "Socket supports SO_RCVBUF");
            check(supported.contains(StandardSocketOptions.SO_REUSEADDR), "Socket supports SO_REUSEADDR");
            check(supported.contains(StandardSocketOptions.TCP_NODELAY), "Socket supports TCP_NODELAY");
            check(!supported.contains(StandardSocketOptions.SO_BROADCAST), "no SO_BROADCAST on Socket");
            check(!supported.contains(StandardSocketOptions.IP_MULTICAST_TTL), "no IP_MULTICAST_TTL on Socket");
        } finally {
            s.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOptionMatrix();
        testGenericTypes();
        testInterfaceConformance();
        testSocketSupport();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
