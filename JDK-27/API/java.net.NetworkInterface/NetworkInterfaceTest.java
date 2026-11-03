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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
