/*
 * @test
 * @summary java.net.InterfaceAddress conformance: address/prefix via loopback.
 * @run main InterfaceAddressTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class InterfaceAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testOriginal() throws Exception {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(ni != null, "loopback interface present");
        boolean found = false;
        for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
            check(ia.getAddress() != null, "interface has address");
            check(ia.getNetworkPrefixLength() >= 0, "prefix length");
            found = true;
        }
        check(found, "at least one interface address");
    }

    // ------------------------------------------------------------------
    // Deepening: structural contract on the loopback interface (probed on
    // JDK-8 and JDK-27: broadcast null on lo, prefix 8 for v4 / 128 for v6)
    // ------------------------------------------------------------------

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.InterfaceAddress");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int publicCtors = 0;
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ctor.getModifiers())) publicCtors++;
        }
        check(publicCtors == 0, "no public constructors (obtained from NetworkInterface only)");
        check(!Modifier.isFinal(c.getModifiers()), "not final on JDK-8 (JDK-27: final)");
    }

    static void testLoopbackInterfaceAddresses() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        check(lo != null, "loopback interface present");
        List<InterfaceAddress> ias = lo.getInterfaceAddresses();
        check(ias != null, "interfaceAddresses list non-null");
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
            // a loopback interface has no broadcast on Linux but other platforms
            // may report one; when present it is always IPv4.
            InetAddress bc = ia.getBroadcast();
            if (bc != null) {
                check(bc instanceof Inet4Address,
                        "loopback broadcast (when present) is IPv4: " + a.getHostAddress());
            }
            // toString contains the textual address
            check(ia.toString().contains(a.getHostAddress()), "toString contains address text");
            check(ia.toString().contains("/"), "toString slash-separated");
        }
        check(v4 >= 1, "at least one IPv4 interface address on lo");
        check(v6 >= 1, "at least one IPv6 interface address on lo");
    }

    static void testEqualsHashCode() throws Exception {
        NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        List<InterfaceAddress> first = lo.getInterfaceAddresses();
        // re-lookup produces distinct but equal InterfaceAddress objects (probed)
        NetworkInterface loAgain = NetworkInterface.getByName(lo.getName());
        check(loAgain != null, "re-lookup interface");
        List<InterfaceAddress> second = loAgain.getInterfaceAddresses();
        check(first.size() == second.size(), "same address count on re-lookup");
        for (int i = 0; i < first.size(); i++) {
            InterfaceAddress a = first.get(i);
            InterfaceAddress b = second.get(i);
            check(a.equals(b), "re-lookup InterfaceAddress equal: " + a);
            check(a.hashCode() == b.hashCode(), "re-lookup hashCode equal");
            check(a.equals(a), "reflexive equals");
            check(!a.equals(null), "equals null false");
            check(!a.equals("x"), "equals other type false");
            check(a.toString() != null && !a.toString().isEmpty(), "toString non-empty");
            check(a.getAddress().equals(b.getAddress()), "address equal on re-lookup");
            check(a.getNetworkPrefixLength() == b.getNetworkPrefixLength(),
                    "prefix equal on re-lookup");
        }
    }

    static void testAllInterfacesStructural() throws Exception {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        int total = 0;
        while (e.hasMoreElements()) {
            for (InterfaceAddress ia : e.nextElement().getInterfaceAddresses()) {
                InetAddress a = ia.getAddress();
                check(a != null, "address non-null across all interfaces");
                short plen = ia.getNetworkPrefixLength();
                check(plen >= 0, "prefix >= 0");
                if (a instanceof Inet4Address) check(plen <= 32, "v4 prefix <= 32");
                else check(plen <= 128, "v6 prefix <= 128");
                InetAddress bc = ia.getBroadcast();
                if (bc != null) check(bc instanceof Inet4Address, "broadcast is IPv4 when present");
                total++;
            }
        }
        check(total >= 1, "at least one InterfaceAddress system-wide");
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testWhiteBox();
        testLoopbackInterfaceAddresses();
        testEqualsHashCode();
        testAllInterfacesStructural();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
