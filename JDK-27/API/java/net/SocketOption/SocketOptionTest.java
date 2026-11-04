/*
 * @test
 * @summary java.net.SocketOption conformance: generic interface structure
 *          (exactly name()/type(), both abstract, no defaults), custom
 *          implementations with arbitrary type parameters, the
 *          StandardSocketOptions reference matrix including the JDK 9
 *          SO_REUSEPORT option, Socket.supportedOptions consumption, and
 *          the interface's role in java.nio.channels.NetworkChannel.
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
        check(c.getSuperclass() == null, "interface has no superclass");
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
        SocketOption<String> s2 = new CustomOption<String>("CUSTOM", String.class);
        check(s.name().equals(s2.name()) && s.type() == s2.type(), "equal metadata");
        check(s != s2, "distinct identities");
        SocketOption<int[]> arr = new CustomOption<int[]>("ARR", int[].class);
        check(int[].class == arr.type(), "array type parameter");
        SocketOption<Object> obj = new CustomOption<Object>("OBJ", Object.class);
        check(Object.class == obj.type(), "object type parameter");
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
            StandardSocketOptions.SO_REUSEPORT, StandardSocketOptions.SO_BROADCAST,
            StandardSocketOptions.SO_SNDBUF, StandardSocketOptions.SO_RCVBUF,
            StandardSocketOptions.SO_LINGER, StandardSocketOptions.TCP_NODELAY,
            StandardSocketOptions.IP_TOS, StandardSocketOptions.IP_MULTICAST_IF,
            StandardSocketOptions.IP_MULTICAST_TTL, StandardSocketOptions.IP_MULTICAST_LOOP
        };
        String[] names = {"SO_KEEPALIVE","SO_REUSEADDR","SO_REUSEPORT","SO_BROADCAST","SO_SNDBUF",
                "SO_RCVBUF","SO_LINGER","TCP_NODELAY","IP_TOS","IP_MULTICAST_IF",
                "IP_MULTICAST_TTL","IP_MULTICAST_LOOP"};
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
        check(StandardSocketOptions.SO_REUSEPORT.type() == Boolean.class, "SO_REUSEPORT Boolean");
        check(StandardSocketOptions.SO_BROADCAST.type() == Boolean.class, "SO_BROADCAST Boolean");
        check(StandardSocketOptions.IP_MULTICAST_LOOP.type() == Boolean.class,
                "IP_MULTICAST_LOOP Boolean");
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (SocketOption<?> o : opts) {
            check(!seen.contains(o.name()), "unique name " + o.name());
            seen.add(o.name());
        }
        check(seen.size() == 12, "12 standard options on JDK-27");
    }

    static void testSocketConsumption() throws Exception {
        // Socket exposes the JDK 9+ typed option API.
        Socket s = new Socket();
        try {
            java.util.Set<SocketOption<?>> supported = s.supportedOptions();
            check(supported != null && !supported.isEmpty(), "supportedOptions non-empty");
            check(supported.contains(StandardSocketOptions.SO_KEEPALIVE), "SO_KEEPALIVE supported");
            check(supported.contains(StandardSocketOptions.TCP_NODELAY), "TCP_NODELAY supported");
            check(!supported.contains(new CustomOption<String>("CUSTOM", String.class)),
                    "custom option not supported");
            s.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
            check(Boolean.TRUE.equals(s.getOption(StandardSocketOptions.SO_KEEPALIVE)),
                    "setOption/getOption round trip");
        } finally {
            s.close();
        }
    }

    static void testUsageSurface() throws Exception {
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
        testWhiteBox();
        testCustomImplementation();
        testStandardImplementations();
        testSocketConsumption();
        testUsageSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
