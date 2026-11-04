/*
 * @test
 * @summary java.net.SocketOption conformance: generic interface structure
 *          (exactly name()/type(), both abstract, no defaults), custom
 *          implementations with arbitrary type parameters, the
 *          StandardSocketOptions reference matrix (name/type/identity), and
 *          the interface's role in the java.nio.channels.NetworkChannel
 *          surface.
 * @run main SocketOptionTest
 */


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.NetworkInterface;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.util.HashSet;
import java.util.Set;

public class SocketOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketOption");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getTypeParameters().length == 1, "one type parameter");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter named T");
        Method name = c.getMethod("name");
        Method type = c.getMethod("type");
        check(name != null, "name()");
        check(type != null, "type()");
        check(Modifier.isAbstract(name.getModifiers()), "name abstract");
        check(Modifier.isAbstract(type.getModifiers()), "type abstract");
        check(name.getReturnType() == String.class, "name returns String");
        check(type.getReturnType() == Class.class, "type returns Class");
        check(name.getParameterTypes().length == 0, "name no args");
        check(type.getParameterTypes().length == 0, "type no args");
        int declared = 0;
        for (Method m : c.getDeclaredMethods()) declared++;
        check(declared == 2, "exactly two declared methods, got " + declared);
    }

    static class CustomOption<T> implements SocketOption<T> {
        private final String n;
        private final Class<T> t;
        CustomOption(String n, Class<T> t) { this.n = n; this.t = t; }
        public String name() { return n; }
        public Class<T> type() { return t; }
    }

    static void testCustomImplementation() throws Exception {
        SocketOption<String> s = new CustomOption<String>("CUSTOM", String.class);
        check("CUSTOM".equals(s.name()), "custom name");
        check(String.class == s.type(), "custom type");
        check(s instanceof SocketOption, "custom is SocketOption");
        // Two customs with equal name/type are still distinct objects.
        SocketOption<String> s2 = new CustomOption<String>("CUSTOM", String.class);
        check(s.name().equals(s2.name()) && s.type() == s2.type(), "equal metadata");
        check(s != s2, "distinct identities");
        // Arbitrary type parameters work.
        SocketOption<int[]> arr = new CustomOption<int[]>("ARR", int[].class);
        check(int[].class == arr.type(), "array type parameter");
        SocketOption<Object> obj = new CustomOption<Object>("OBJ", Object.class);
        check(Object.class == obj.type(), "object type parameter");
        // null returns are type-legal but contractually invalid; callers
        // distinguish via identity, and the interface imposes no defaults.
        SocketOption<String> nullName = new SocketOption<String>() {
            public String name() { return null; }
            public Class<String> type() { return null; }
        };
        check(nullName.name() == null, "null name passes through");
        check(nullName.type() == null, "null type passes through");
    }

    static void testStandardImplementations() throws Exception {
        SocketOption<?>[] opts = {
            StandardSocketOptions.SO_KEEPALIVE, StandardSocketOptions.SO_REUSEADDR,
            StandardSocketOptions.SO_BROADCAST, StandardSocketOptions.SO_SNDBUF,
            StandardSocketOptions.SO_RCVBUF, StandardSocketOptions.SO_LINGER,
            StandardSocketOptions.TCP_NODELAY, StandardSocketOptions.IP_TOS,
            StandardSocketOptions.IP_MULTICAST_IF, StandardSocketOptions.IP_MULTICAST_TTL,
            StandardSocketOptions.IP_MULTICAST_LOOP
        };
        String[] names = {"SO_KEEPALIVE","SO_REUSEADDR","SO_BROADCAST","SO_SNDBUF","SO_RCVBUF",
                "SO_LINGER","TCP_NODELAY","IP_TOS","IP_MULTICAST_IF","IP_MULTICAST_TTL",
                "IP_MULTICAST_LOOP"};
        for (int i = 0; i < opts.length; i++) {
            check(opts[i] != null, names[i] + " non-null");
            check(names[i].equals(opts[i].name()), names[i] + " name exact");
            check(opts[i].type() != null, names[i] + " type non-null");
        }
        check(StandardSocketOptions.SO_KEEPALIVE.type() == Boolean.class, "SO_KEEPALIVE Boolean");
        check(StandardSocketOptions.SO_SNDBUF.type() == Integer.class, "SO_SNDBUF Integer");
        check(StandardSocketOptions.SO_RCVBUF.type() == Integer.class, "SO_RCVBUF Integer");
        check(StandardSocketOptions.SO_LINGER.type() == Integer.class, "SO_LINGER Integer");
        check(StandardSocketOptions.IP_TOS.type() == Integer.class, "IP_TOS Integer");
        check(StandardSocketOptions.IP_MULTICAST_TTL.type() == Integer.class, "IP_MULTICAST_TTL Integer");
        check(StandardSocketOptions.IP_MULTICAST_IF.type() == NetworkInterface.class,
                "IP_MULTICAST_IF NetworkInterface");
        check(StandardSocketOptions.TCP_NODELAY.type() == Boolean.class, "TCP_NODELAY Boolean");
        check(StandardSocketOptions.SO_REUSEADDR.type() == Boolean.class, "SO_REUSEADDR Boolean");
        check(StandardSocketOptions.SO_BROADCAST.type() == Boolean.class, "SO_BROADCAST Boolean");
        check(StandardSocketOptions.IP_MULTICAST_LOOP.type() == Boolean.class,
                "IP_MULTICAST_LOOP Boolean");
        // All standard option names are unique.
        Set<String> seen = new HashSet<String>();
        for (SocketOption<?> o : opts) {
            check(!seen.contains(o.name()), "unique name " + o.name());
            seen.add(o.name());
        }
        check(seen.size() == 11, "11 standard options on JDK-8");
    }

    static void testUsageSurface() throws Exception {
        // The interface is consumed by java.nio.channels.NetworkChannel.
        Class<?> nc = Class.forName("java.nio.channels.NetworkChannel");
        boolean hasSet = false, hasGet = false, hasSupported = false;
        for (Method m : nc.getMethods()) {
            if (m.getName().equals("setOption")
                    && m.getParameterTypes().length == 2
                    && m.getParameterTypes()[0].getName().equals("java.net.SocketOption")) hasSet = true;
            if (m.getName().equals("getOption")
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0].getName().equals("java.net.SocketOption")) hasGet = true;
            if (m.getName().equals("supportedOptions")) hasSupported = true;
        }
        check(hasSet, "NetworkChannel.setOption(SocketOption,T)");
        check(hasGet, "NetworkChannel.getOption(SocketOption)");
        check(hasSupported, "NetworkChannel.supportedOptions()");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketOption");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("type"), "has method type");
        testWhiteBox();
        testCustomImplementation();
        testStandardImplementations();
        testUsageSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
