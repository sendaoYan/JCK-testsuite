/*
 * @test
 * @summary java.net.NetworkInterface conformance: white-box structure + black-box behavior.
 * @run main NetworkInterfaceTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class NetworkInterfaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NetworkInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getMethod("getNetworkInterfaces") != null, "getNetworkInterfaces");
        check(c.getMethod("getByName", String.class) != null, "getByName");
        check(c.getMethod("isLoopback") != null, "isLoopback");
        check(c.getMethod("getName") != null, "getName");
    }

    static void testBlackBox() throws Exception {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        int count = 0;
        NetworkInterface loopback = null;
        List<String> names = new ArrayList<>();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            count++;
            check(ni.getName() != null, "name non-null");
            check(ni.getDisplayName() != null, "displayName non-null");
            check(ni.getIndex() >= 0, "index >= 0");
            names.add(ni.getName());

            // self-consistent lookup by index and name
            NetworkInterface byIndex = NetworkInterface.getByIndex(ni.getIndex());
            check(byIndex != null && byIndex.getName().equals(ni.getName()), "getByIndex self-consistent");
            NetworkInterface byName = NetworkInterface.getByName(ni.getName());
            check(byName != null && byName.getIndex() == ni.getIndex(), "getByName self-consistent");

            // interface addresses non-null, each address non-null
            List<InterfaceAddress> addrs = ni.getInterfaceAddresses();
            check(addrs != null, "interfaceAddresses non-null");
            for (InterfaceAddress ia : addrs) check(ia.getAddress() != null, "interface address non-null");

            check(ni.inetAddresses() != null, "inetAddresses stream non-null");
            check(ni.subInterfaces() != null, "subInterfaces stream non-null");

            if (ni.isLoopback()) loopback = ni;
        }
        check(count > 0, "at least one interface");
        check(loopback != null, "loopback interface exists");
        check(loopback.isLoopback(), "loopback flag true");

        // stream form matches enumeration count
        check(NetworkInterface.networkInterfaces().count() == count, "networkInterfaces stream count");

        // every interface has a distinct name
        check(new HashSet<>(names).size() == count, "interface names unique");
    }

    // ------------------------------------------------------------------
    // Deepening: deterministic lookup negatives + loopback structural
    // properties (probed identical on JDK-8 and JDK-27)
    // ------------------------------------------------------------------

    static void testLookupNegatives() throws Exception {
        try {
            NetworkInterface.getByName(null);
            check(false, "getByName(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "getByName(null) NPE");
        }
        check(null == NetworkInterface.getByName("definitely-not-exist-xyzzy"),
                "getByName(nonexistent) null");
        check(null == NetworkInterface.getByName(""), "getByName(empty) null");
        check(null == NetworkInterface.getByIndex(0), "getByIndex(0) null");
        check(null == NetworkInterface.getByIndex(999999), "getByIndex(999999) null");
        try {
            NetworkInterface.getByIndex(-1);
            check(false, "getByIndex(-1) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "getByIndex(-1) IAE");
        }
        try {
            NetworkInterface.getByInetAddress(null);
            check(false, "getByInetAddress(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "getByInetAddress(null) NPE");
        }
    }

    static void testLoopbackProperties() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(lo != null, "loopback interface exists");
        check(lo.isLoopback(), "loopback flag true");
        check(lo.isUp(), "loopback is up (Linux lo always up)");
        check(!lo.isPointToPoint(), "loopback not point-to-point");
        check(!lo.isVirtual(), "loopback not virtual");
        check(lo.getName() != null && !lo.getName().isEmpty(), "loopback name non-empty");
        check(lo.getDisplayName() != null, "loopback displayName non-null");
        check(lo.getIndex() > 0, "loopback index positive");
        check(NetworkInterface.getByIndex(lo.getIndex()) != null, "loopback found by its index");
        check(NetworkInterface.getByIndex(lo.getIndex()).equals(lo), "getByIndex self-consistent");
        check(NetworkInterface.getByName(lo.getName()).equals(lo), "getByName self-consistent");
        check(NetworkInterface.getByName(lo.getName()).getIndex() == lo.getIndex(),
                "name/index lookup agree");
        check(lo.getMTU() > 0, "loopback MTU positive");
        byte[] hw = lo.getHardwareAddress();
        if (hw == null) {
            check(true, "loopback hardware address null");
        } else {
            boolean allZero = true;
            for (byte b : hw) if (b != 0) allZero = false;
            check(allZero || hw.length == 0, "loopback hardware address all-zero or empty");
        }
        check(lo.toString().contains(lo.getName()), "toString contains name");
        check(lo.toString().startsWith("name:"), "toString starts with name:");
        check(null == lo.getParent(), "loopback parent null");
        check(!lo.getSubInterfaces().hasMoreElements(), "loopback no sub-interfaces");
        check(lo.subInterfaces().count() == 0, "loopback subInterfaces stream empty");
        check(lo.equals(lo), "reflexive equals");
        check(!lo.equals(null), "equals null false");
        check(!lo.equals("lo"), "equals other type false");
        check(lo.equals(NetworkInterface.getByName(lo.getName())), "re-lookup equals");
        check(lo.hashCode() == NetworkInterface.getByName(lo.getName()).hashCode(),
                "re-lookup hashCode equal");
        NetworkInterface loV6 = NetworkInterface.getByInetAddress(InetAddress.getByName("::1"));
        if (loV6 != null) {
            check(loV6.equals(lo), "v6 loopback address maps to same interface");
        } else {
            check(true, "v6 loopback not present (env dependent)");
        }
    }

    static void testInetAddressesEnumeration() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        Enumeration<InetAddress> addrs = lo.getInetAddresses();
        check(addrs != null, "getInetAddresses non-null");
        boolean sawLoopback = false;
        int n = 0;
        while (addrs.hasMoreElements()) {
            InetAddress a = addrs.nextElement();
            check(a != null, "address non-null");
            check(a.getAddress().length == 4 || a.getAddress().length == 16, "address length 4/16");
            if (a.isLoopbackAddress()) sawLoopback = true;
            n++;
        }
        check(n >= 1, "loopback interface has at least one inet address");
        check(sawLoopback, "one of them is a loopback address");
        // stream view (JDK-9+) sees the same addresses
        check(lo.inetAddresses().count() == n, "inetAddresses stream count matches enumeration");
        check(lo.getInterfaceAddresses().size() == n,
                "getInterfaceAddresses size matches getInetAddresses count");
        InetAddress loop = InetAddress.getLoopbackAddress();
        check(lo.inetAddresses().anyMatch(a -> a.equals(loop)),
                "loopback address present in stream");
        NetworkInterface viaAddr = NetworkInterface.getByInetAddress(loop);
        check(viaAddr != null && viaAddr.equals(lo), "getByInetAddress(loopback) equals lo");
    }

    static void testNetworkInterfacesStream() throws Exception {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        int enumCount = 0;
        while (e.hasMoreElements()) { e.nextElement(); enumCount++; }
        check(enumCount >= 1, "enumeration non-empty");
        check(NetworkInterface.networkInterfaces().count() == enumCount,
                "networkInterfaces stream count matches enumeration");
        check(NetworkInterface.networkInterfaces().allMatch(ni -> ni.getName() != null),
                "stream names non-null");
    }

    static void testReachabilityViaInterface() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        InetAddress loop = InetAddress.getByName("127.0.0.1");
        check(loop.isReachable(lo, 0, 300), "isReachable via loopback interface");
        try {
            loop.isReachable(lo, 0, -1);
            check(false, "negative timeout must throw");
        } catch (IllegalArgumentException ex) {
            check("timeout can't be negative".equals(ex.getMessage()),
                    "exact message via netif variant");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLookupNegatives();
        testLoopbackProperties();
        testInetAddressesEnumeration();
        testNetworkInterfacesStream();
        testReachabilityViaInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
