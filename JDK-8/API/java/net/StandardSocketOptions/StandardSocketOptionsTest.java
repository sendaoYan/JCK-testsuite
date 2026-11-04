/*
 * @test
 * @summary java.net.StandardSocketOptions conformance: utility class
 *          structure (final, private constructor), the complete 11-field
 *          JDK-8 option matrix with exact name() and type() per option
 *          (SO_SNDBUF/SO_RCVBUF/SO_LINGER/IP_TOS/IP_MULTICAST_TTL are
 *          SocketOption<Integer>, the socket booleans are
 *          SocketOption<Boolean>, IP_MULTICAST_IF is
 *          SocketOption<NetworkInterface>), identity distinctness, absence
 *          of SO_REUSEPORT on JDK-8, and conformance through the
 *          SocketOption interface.
 * @run main StandardSocketOptionsTest
 */


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.NetworkInterface;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.util.HashSet;
import java.util.Set;

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
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPrivate(ctor.getModifiers()), "private ctor");
        // Exactly the 11 JDK-8 fields, all public static final.
        String[] names = {"SO_BROADCAST","SO_KEEPALIVE","SO_SNDBUF","SO_RCVBUF","SO_REUSEADDR",
                "SO_LINGER","IP_TOS","IP_MULTICAST_IF","IP_MULTICAST_TTL","IP_MULTICAST_LOOP",
                "TCP_NODELAY"};
        for (String n : names) {
            Field f = c.getField(n);
            check(Modifier.isPublic(f.getModifiers()), n + " public");
            check(Modifier.isStatic(f.getModifiers()), n + " static");
            check(Modifier.isFinal(f.getModifiers()), n + " final");
        }
        int publicStatic = 0;
        for (Field f : c.getFields()) {
            if (f.getDeclaringClass() == c && Modifier.isStatic(f.getModifiers())) publicStatic++;
        }
        check(publicStatic == 11, "exactly 11 static fields, got " + publicStatic);
        // SO_REUSEPORT does not exist on JDK-8 (added JDK 9).
        try {
            c.getField("SO_REUSEPORT");
            check(false, "SO_REUSEPORT must be absent on JDK-8");
        } catch (NoSuchFieldException e) {
            passed++;
        }
    }

    static void testOptionMatrix() throws Exception {
        checkNameType(StandardSocketOptions.SO_BROADCAST, "SO_BROADCAST", Boolean.class);
        checkNameType(StandardSocketOptions.SO_KEEPALIVE, "SO_KEEPALIVE", Boolean.class);
        checkNameType(StandardSocketOptions.SO_SNDBUF, "SO_SNDBUF", Integer.class);
        checkNameType(StandardSocketOptions.SO_RCVBUF, "SO_RCVBUF", Integer.class);
        checkNameType(StandardSocketOptions.SO_REUSEADDR, "SO_REUSEADDR", Boolean.class);
        checkNameType(StandardSocketOptions.SO_LINGER, "SO_LINGER", Integer.class);
        checkNameType(StandardSocketOptions.IP_TOS, "IP_TOS", Integer.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_IF, "IP_MULTICAST_IF", NetworkInterface.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_TTL, "IP_MULTICAST_TTL", Integer.class);
        checkNameType(StandardSocketOptions.IP_MULTICAST_LOOP, "IP_MULTICAST_LOOP", Boolean.class);
        checkNameType(StandardSocketOptions.TCP_NODELAY, "TCP_NODELAY", Boolean.class);
        // Every option is a SocketOption and the objects are singletons.
        SocketOption<?>[] all = {StandardSocketOptions.SO_BROADCAST, StandardSocketOptions.SO_KEEPALIVE,
                StandardSocketOptions.SO_SNDBUF, StandardSocketOptions.SO_RCVBUF,
                StandardSocketOptions.SO_REUSEADDR, StandardSocketOptions.SO_LINGER,
                StandardSocketOptions.IP_TOS, StandardSocketOptions.IP_MULTICAST_IF,
                StandardSocketOptions.IP_MULTICAST_TTL, StandardSocketOptions.IP_MULTICAST_LOOP,
                StandardSocketOptions.TCP_NODELAY};
        Set<String> names = new HashSet<String>();
        for (SocketOption<?> o : all) {
            check(o instanceof SocketOption, o.name() + " is SocketOption");
            check(!names.contains(o.name()), "name " + o.name() + " unique");
            names.add(o.name());
        }
        check(names.size() == 11, "11 distinct option names");
        check(StandardSocketOptions.SO_KEEPALIVE != StandardSocketOptions.SO_BROADCAST, "distinct options");
        check(StandardSocketOptions.SO_SNDBUF != StandardSocketOptions.SO_RCVBUF, "distinct buffers");
        check(StandardSocketOptions.SO_REUSEADDR != StandardSocketOptions.IP_MULTICAST_LOOP,
                "distinct booleans");
    }

    static void testGenericTypes() throws Exception {
        // Compile-time generic typing is mirrored reflectively.
        Class<?> c = StandardSocketOptions.class;
        check(c.getField("SO_SNDBUF").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.lang.Integer>"), "SO_SNDBUF generic type");
        check(c.getField("SO_KEEPALIVE").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.lang.Boolean>"), "SO_KEEPALIVE generic type");
        check(c.getField("IP_MULTICAST_IF").getGenericType().getTypeName()
                .equals("java.net.SocketOption<java.net.NetworkInterface>"), "IP_MULTICAST_IF generic type");
    }

    static void testInterfaceConformance() throws Exception {
        // The SocketOption surface is exactly name() and type().
        SocketOption<Integer> snd = StandardSocketOptions.SO_SNDBUF;
        check("SO_SNDBUF".equals(snd.name()), "via interface name");
        check(Integer.class == snd.type(), "via interface type");
        // Polymorphic use through the interface.
        java.util.List<String> all = new java.util.ArrayList<String>();
        for (SocketOption<?> o : new SocketOption<?>[]{StandardSocketOptions.SO_BROADCAST,
                StandardSocketOptions.SO_KEEPALIVE, StandardSocketOptions.SO_SNDBUF,
                StandardSocketOptions.SO_RCVBUF, StandardSocketOptions.SO_REUSEADDR,
                StandardSocketOptions.SO_LINGER, StandardSocketOptions.IP_TOS,
                StandardSocketOptions.IP_MULTICAST_IF, StandardSocketOptions.IP_MULTICAST_TTL,
                StandardSocketOptions.IP_MULTICAST_LOOP, StandardSocketOptions.TCP_NODELAY}) {
            all.add(o.name() + ":" + o.type().getSimpleName());
        }
        check(all.size() == 11, "11 rendered options");
        check(all.get(0).equals("SO_BROADCAST:Boolean"), "first rendering, got " + all.get(0));
        check(all.get(6).equals("IP_TOS:Integer"), "IP_TOS rendering, got " + all.get(6));
        check(all.get(10).equals("TCP_NODELAY:Boolean"), "TCP_NODELAY rendering");
    }

    public static void main(String[] args) throws Exception {
        check("SO_KEEPALIVE".equals(java.net.StandardSocketOptions.SO_KEEPALIVE.name()), "SO_KEEPALIVE name");
        check("TCP_NODELAY".equals(java.net.StandardSocketOptions.TCP_NODELAY.name()), "TCP_NODELAY name");
        check(java.net.StandardSocketOptions.SO_RCVBUF != null, "SO_RCVBUF non-null");
        check(java.net.StandardSocketOptions.SO_SNDBUF.type().equals(Integer.class), "SO_SNDBUF type");
        check(java.net.StandardSocketOptions.SO_KEEPALIVE.type().equals(Boolean.class), "SO_KEEPALIVE type");
        testWhiteBox();
        testOptionMatrix();
        testGenericTypes();
        testInterfaceConformance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
