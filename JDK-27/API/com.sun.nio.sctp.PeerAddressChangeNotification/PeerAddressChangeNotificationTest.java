/*
 * @test
 * @summary com.sun.nio.sctp.PeerAddressChangeNotification conformance: white-box structure + black-box behavior.
 * @run main PeerAddressChangeNotificationTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class PeerAddressChangeNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.PeerAddressChangeNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        check(m.contains("address"), "address");
        check(m.contains("association"), "association");
        check(m.contains("event"), "event");
        Method ev = c.getDeclaredMethod("event");
        check(ev.getReturnType().getName().equals(
                "com.sun.nio.sctp.PeerAddressChangeNotification$AddressChangeEvent"),
              "event returns AddressChangeEvent");
        check(c.getDeclaredClasses().length >= 1, "nested AddressChangeEvent declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SocketAddress addr = new InetSocketAddress("127.0.0.1", 12345);
        com.sun.nio.sctp.Association assoc = new com.sun.nio.sctp.Association(1, 2, 3) {};
        com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent ev =
                com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.ADDR_ADDED;

        com.sun.nio.sctp.PeerAddressChangeNotification n =
                new com.sun.nio.sctp.PeerAddressChangeNotification() {
                    public SocketAddress address() { return addr; }
                    public com.sun.nio.sctp.Association association() { return assoc; }
                    public com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent event() { return ev; }
                };

        check(n.address() == addr, "address returns");
        check(n.association() == assoc, "association returns");
        check(n.event() == ev, "event returns");
        check(n instanceof com.sun.nio.sctp.Notification, "instanceof Notification");

        // AddressChangeEvent enum constants and round-trips
        com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent[] vals =
                com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.values();
        check(vals.length == 6, "AddressChangeEvent has 6 constants");
        Set<String> names = new HashSet<>(Arrays.asList(
                "ADDR_AVAILABLE", "ADDR_UNREACHABLE", "ADDR_REMOVED",
                "ADDR_ADDED", "ADDR_MADE_PRIMARY", "ADDR_CONFIRMED"));
        for (com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent e : vals) {
            check(names.contains(e.name()), "constant " + e.name());
        }
        for (String nm : names) {
            check(com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.valueOf(nm).name().equals(nm),
                  "valueOf " + nm + " round-trip");
        }

        // null address / association are permitted
        com.sun.nio.sctp.PeerAddressChangeNotification n2 =
                new com.sun.nio.sctp.PeerAddressChangeNotification() {
                    public SocketAddress address() { return null; }
                    public com.sun.nio.sctp.Association association() { return null; }
                    public com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent event() {
                        return com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.ADDR_REMOVED;
                    }
                };
        check(n2.address() == null, "null address allowed");
        check(n2.association() == null, "null association allowed");
        check(n2.event() == com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.ADDR_REMOVED,
              "ADDR_REMOVED event allowed");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
