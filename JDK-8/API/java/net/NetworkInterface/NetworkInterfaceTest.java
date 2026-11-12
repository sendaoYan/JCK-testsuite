/*
 * @test
 * @summary java.net.NetworkInterface conformance: deterministic contract subset.
 *          Lookup negatives (getByName/getByIndex/getByInetAddress), loopback
 *          interface structural properties (name/displayName/index/MTU/
 *          hardware address), address enumeration, re-lookup equality, and
 *          isReachable via an explicit interface. Environment-dependent values
 *          (interface names, MTU, hardware addresses) are only asserted
 *          structurally. Probed identical on JDK-8 and JDK-27.
 * @run main NetworkInterfaceTest
 */

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NetworkInterfaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testOriginal() throws Exception {
        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        check(ifs != null, "getNetworkInterfaces non-null");
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(ni != null, "loopback interface");
        check(ni.isLoopback(), "isLoopback");
        check(ni.getName() != null && ni.getName().length() > 0, "getName");
    }

    static void testLookupNegatives() throws Exception {
        // getByName(null) -> NPE (probed; no message on JDK-8)
        try {
            NetworkInterface.getByName(null);
            check(false, "getByName(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "getByName(null) NPE");
        }
        check(null == NetworkInterface.getByName("definitely-not-exist-xyzzy"),
                "getByName(nonexistent) null");
        check(null == NetworkInterface.getByName(""), "getByName(empty) null");
        // getByIndex: 0 and huge indices have no interface; negative -> IAE (probed)
        check(null == NetworkInterface.getByIndex(0), "getByIndex(0) null");
        check(null == NetworkInterface.getByIndex(999999), "getByIndex(999999) null");
        try {
            NetworkInterface.getByIndex(-1);
            check(false, "getByIndex(-1) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "getByIndex(-1) IAE");
        }
        // getByInetAddress(null) -> NPE (probed)
        try {
            NetworkInterface.getByInetAddress(null);
            check(false, "getByInetAddress(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "getByInetAddress(null) NPE");
        }
        // an unbound address returns null, not an exception (1.2.3.4 not on any local iface
        // in a standard container; tolerate null only when 1.2.3.4 is genuinely absent)
        NetworkInterface byAddr = NetworkInterface.getByInetAddress(InetAddress.getByName("1.2.3.4"));
        check(byAddr == null || byAddr.getName() != null,
                "getByInetAddress(1.2.3.4) null or valid (env dependent)");
    }

    static NetworkInterface loopbackInterface() throws Exception {
        // loopback address is always bound to a loopback interface (probed)
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(lo != null, "loopback interface exists");
        return lo;
    }

    static void testLoopbackProperties() throws Exception {
        NetworkInterface lo = loopbackInterface();
        check(lo.isLoopback(), "loopback flag true");
        check(lo.isUp(), "loopback is up (Linux lo always up)");
        check(!lo.isPointToPoint(), "loopback not point-to-point");
        check(!lo.isVirtual(), "loopback not virtual");
        // name: non-null, non-empty; displayName: non-null (equals name on Linux)
        check(lo.getName() != null && !lo.getName().isEmpty(), "loopback name non-empty");
        check(lo.getDisplayName() != null, "loopback displayName non-null");
        // index: positive (index 0 is never assigned - probed via getByIndex(0)==null)
        check(lo.getIndex() > 0, "loopback index positive");
        check(NetworkInterface.getByIndex(lo.getIndex()) != null, "loopback found by its index");
        check(NetworkInterface.getByIndex(lo.getIndex()).equals(lo), "getByIndex self-consistent");
        check(NetworkInterface.getByName(lo.getName()) != null, "loopback found by name");
        check(NetworkInterface.getByName(lo.getName()).equals(lo), "getByName self-consistent");
        check(NetworkInterface.getByName(lo.getName()).getIndex() == lo.getIndex(),
                "name/index lookup agree");
        // MTU is environment specific: Linux lo reports 65536 here, Windows may
        // report 0 or -1 (0xFFFFFFFF placeholder). Assert only a sane value.
        int mtu = lo.getMTU();
        check(mtu >= -1, "loopback MTU non-negative or -1 placeholder (got " + mtu + ")");
        // hardware address of a loopback: null, empty or all-zero (probed: null here)
        byte[] hw = lo.getHardwareAddress();
        if (hw == null) {
            check(true, "loopback hardware address null");
        } else {
            boolean allZero = true;
            for (byte b : hw) if (b != 0) allZero = false;
            check(allZero || hw.length == 0, "loopback hardware address all-zero or empty");
        }
        // toString mentions the name (probed format "name:lo (lo)")
        check(lo.toString().contains(lo.getName()), "toString contains name");
        check(lo.toString().startsWith("name:"), "toString starts with name:");
        // top-level loopback has no parent and no sub-interfaces (probed)
        check(null == lo.getParent(), "loopback parent null");
        check(!lo.getSubInterfaces().hasMoreElements(), "loopback no sub-interfaces");
        // equals/hashCode semantics
        check(lo.equals(lo), "reflexive equals");
        check(!lo.equals(null), "equals null false");
        check(!lo.equals("lo"), "equals other type false");
        check(lo.equals(NetworkInterface.getByName(lo.getName())), "re-lookup equals");
        check(lo.hashCode() == NetworkInterface.getByName(lo.getName()).hashCode(),
                "re-lookup hashCode equal");
        // both loopback address families map to the same interface object (probed)
        NetworkInterface loV6 = NetworkInterface.getByInetAddress(InetAddress.getByName("::1"));
        if (loV6 != null) {
            check(loV6.equals(lo), "v6 loopback address maps to same interface");
        } else {
            check(true, "v6 loopback not present (env dependent)");
        }
    }

    static void testEnumerationStructure() throws Exception {
        Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
        check(e1 != null, "enumeration non-null");
        check(e1.hasMoreElements(), "at least one interface (loopback always present)");
        Set<String> names = new HashSet<String>();
        int count = 0;
        int loopbackCount = 0;
        while (e1.hasMoreElements()) {
            NetworkInterface ni = e1.nextElement();
            check(ni != null, "element non-null");
            check(ni.getName() != null && !ni.getName().isEmpty(), "name non-empty for " + ni.getName());
            check(ni.getDisplayName() != null, "displayName non-null");
            check(ni.getIndex() > 0, "index positive for " + ni.getName());
            check(names.add(ni.getName()), "interface names unique: " + ni.getName());
            check(ni.equals(NetworkInterface.getByName(ni.getName())), "re-lookup equals " + ni.getName());
            check(ni.getInterfaceAddresses() != null, "interfaceAddresses list non-null");
            // flags callable (values are platform specific)
            boolean up = ni.isUp();
            boolean loop = ni.isLoopback();
            boolean p2p = ni.isPointToPoint();
            boolean virt = ni.isVirtual();
            boolean mc = ni.supportsMulticast();
            check(up || !up, "isUp callable");
            check(loop || !loop, "isLoopback callable");
            check(p2p || !p2p, "isPointToPoint callable");
            check(virt || !virt, "isVirtual callable");
            check(mc || !mc, "supportsMulticast callable");
            if (loop) loopbackCount++;
            count++;
        }
        check(count >= 1, "counted at least one interface");
        check(loopbackCount >= 1, "a loopback interface is present");
        // a second enumeration yields equal objects for the same names
        Enumeration<NetworkInterface> e2 = NetworkInterface.getNetworkInterfaces();
        int eqCount = 0;
        int total = 0;
        while (e2.hasMoreElements()) {
            NetworkInterface ni = e2.nextElement();
            NetworkInterface first = NetworkInterface.getByName(ni.getName());
            total++;
            if (ni.equals(first) && ni.hashCode() == first.hashCode()) eqCount++;
        }
        check(eqCount == total, "re-enumeration all equal (" + eqCount + "/" + total + ")");
    }

    static void testInetAddressesEnumeration() throws Exception {
        NetworkInterface lo = loopbackInterface();
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
        // getByInetAddress agrees with the enumeration for the loopback address
        InetAddress loop = InetAddress.getLoopbackAddress();
        NetworkInterface viaAddr = NetworkInterface.getByInetAddress(loop);
        check(viaAddr != null && viaAddr.equals(lo), "getByInetAddress(loopback) equals lo");
        boolean found = false;
        Enumeration<InetAddress> again = lo.getInetAddresses();
        while (again.hasMoreElements()) {
            if (again.nextElement().equals(loop)) found = true;
        }
        check(found, "loopback address present in interface enumeration");
        // interface addresses list mirrors the enumeration size (probed)
        check(lo.getInterfaceAddresses().size() == n,
                "getInterfaceAddresses size matches getInetAddresses count");
    }

    static void testReachabilityViaInterface() throws Exception {
        NetworkInterface lo = loopbackInterface();
        InetAddress loop = InetAddress.getByName("127.0.0.1");
        check(loop.isReachable(lo, 0, 300), "isReachable via loopback interface");
        try {
            loop.isReachable(lo, 0, -1);
            check(false, "negative timeout must throw");
        } catch (IllegalArgumentException e) {
            check("timeout can't be negative".equals(e.getMessage()),
                    "exact message via netif variant");
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testLookupNegatives();
        testLoopbackProperties();
        testEnumerationStructure();
        testInetAddressesEnumeration();
        testReachabilityViaInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
