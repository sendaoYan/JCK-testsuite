/*
 * @test
 * @summary java.net.NetworkInterface conformance: API type verification.
 * @run main NetworkInterfaceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NetworkInterfaceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.util.Enumeration<java.net.NetworkInterface> nis = java.net.NetworkInterface.getNetworkInterfaces();
check(nis != null, "getNetworkInterfaces non-null");
java.net.NetworkInterface lo = java.net.NetworkInterface.getByInetAddress(java.net.InetAddress.getLoopbackAddress());
check(lo != null, "getByInetAddress loopback non-null");
check(lo.isLoopback(), "loopback interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
