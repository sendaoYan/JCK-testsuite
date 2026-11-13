/*
 * @test
 * @summary java.net.InterfaceAddress conformance: white-box structure + black-box behavior.
 * @run main InterfaceAddressTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class InterfaceAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.InterfaceAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("getAddress") != null, "getAddress");
        check(c.getMethod("getBroadcast") != null, "getBroadcast");
        check(c.getMethod("getNetworkPrefixLength") != null, "getNetworkPrefixLength");
        check(c.getMethod("equals", Object.class) != null, "equals");
        check(c.getMethod("hashCode") != null, "hashCode");
        // only obtainable from NetworkInterface (probed; ctor private on JDK-27)
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        boolean anyPublic = false;
        for (java.lang.reflect.Constructor<?> ctor : ctors) {
            if (Modifier.isPublic(ctor.getModifiers())) anyPublic = true;
        }
        check(!anyPublic, "no public constructors");
    }

    static void testBlackBox() throws Exception {
        List<InterfaceAddress> all = new ArrayList<>();
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            all.addAll(nis.nextElement().getInterfaceAddresses());
        }
        check(!all.isEmpty(), "at least one interface address exists");

        for (InterfaceAddress ia : all) {
            InetAddress a = ia.getAddress();
            check(a != null, "address non-null");
            short plen = ia.getNetworkPrefixLength();
            check(plen >= 0, "prefix >= 0");
            if (a instanceof Inet4Address) check(plen <= 32, "ipv4 prefix <= 32");
            else check(plen <= 128, "ipv6 prefix <= 128");

            InetAddress bc = ia.getBroadcast();
            if (bc != null) check(bc instanceof Inet4Address, "broadcast is ipv4 when present");

            check(ia.equals(ia), "reflexive equals");
            check(ia.hashCode() == ia.hashCode(), "stable hashCode");
            check(ia.toString() != null && !ia.toString().isEmpty(), "toString non-empty");
        }

        // equals/hashCode consistent across identical addresses within a single interface
        if (all.size() >= 1) {
            InterfaceAddress first = all.get(0);
            check(first.equals(first), "first equals itself");
            check(!first.equals(new Object()), "not equals arbitrary object");
        }
    }

    // ------------------------------------------------------------------
    // Deepening: deterministic loopback contract (probed on both JDKs:
    // no broadcast on POSIX lo; the Windows IPv4 lo reports 127.255.255.255.
    // Prefix 8/128 and re-lookup equality hold everywhere.)
    // ------------------------------------------------------------------

    static void testLoopbackInterfaceAddresses() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(lo != null, "loopback interface present");
        List<InterfaceAddress> ias = lo.getInterfaceAddresses();
        check(!ias.isEmpty(), "loopback has interface addresses");
        int v4 = 0;
        int v6 = 0;
        for (InterfaceAddress ia : ias) {
            InetAddress a = ia.getAddress();
            check(a != null, "address non-null");
            check(a.isLoopbackAddress() || a.isAnyLocalAddress(),
                    "lo carries loopback/any-local addresses only");
            short plen = ia.getNetworkPrefixLength();
            check(plen >= 0 && plen <= 128, "prefix length within 0..128");
            if (a instanceof Inet4Address) {
                check(plen <= 32, "IPv4 prefix <= 32");
                v4++;
            } else {
                check(plen <= 128, "IPv6 prefix <= 128");
                v6++;
            }
            // The broadcast address is a platform representation detail: the
            // POSIX loopback has none, while the Windows IPv4 loopback
            // (127.0.0.1/8) reports 127.255.255.255. IPv6 never has one.
            InetAddress bc = ia.getBroadcast();
            if (a instanceof Inet4Address) {
                if (java.io.File.separatorChar == '\\') {
                    check(bc == null || bc instanceof Inet4Address,
                            "Windows IPv4 loopback broadcast is IPv4 or absent: " + bc);
                } else {
                    check(bc == null, "no broadcast on loopback: " + a.getHostAddress());
                }
            } else {
                check(bc == null, "no broadcast for an IPv6 loopback address: " + a.getHostAddress());
            }
            check(ia.toString().contains(a.getHostAddress()), "toString contains address text");
        }
        check(v4 >= 1, "at least one IPv4 interface address on lo");
        check(v6 >= 1, "at least one IPv6 interface address on lo");
    }

    static void testReLookupEquality() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        List<InterfaceAddress> first = lo.getInterfaceAddresses();
        NetworkInterface loAgain = NetworkInterface.getByName(lo.getName());
        List<InterfaceAddress> second = loAgain.getInterfaceAddresses();
        check(first.size() == second.size(), "same address count on re-lookup");
        for (int i = 0; i < first.size(); i++) {
            InterfaceAddress a = first.get(i);
            InterfaceAddress b = second.get(i);
            check(a.equals(b), "re-lookup InterfaceAddress equal: " + a);
            check(a.hashCode() == b.hashCode(), "re-lookup hashCode equal");
            check(a.getAddress().equals(b.getAddress()), "address equal on re-lookup");
            check(a.getNetworkPrefixLength() == b.getNetworkPrefixLength(),
                    "prefix equal on re-lookup");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLoopbackInterfaceAddresses();
        testReLookupEquality();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
