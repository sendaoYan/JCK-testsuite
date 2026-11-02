/*
 * @test
 * @summary com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent conformance: enum constants.
 * @run main AddressChangeEventTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AddressChangeEventTest {
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

    public static void main(String[] a) throws Exception {
        com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent[] v = com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.values();
        check(v.length == 6, "6 constants");
        check(com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.ADDR_AVAILABLE.name().equals("ADDR_AVAILABLE"), "ADDR_AVAILABLE");
        check(com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.valueOf("ADDR_REMOVED") == com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent.ADDR_REMOVED, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
