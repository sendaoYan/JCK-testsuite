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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
