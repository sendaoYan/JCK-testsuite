/*
 * @test
 * @summary java.net.Inet4Address conformance: API type verification.
 * @run main Inet4AddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class Inet4AddressTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.Inet4Address a4 = java.net.Inet4Address.ofLiteral("127.0.0.1");
check(a4 != null, "ofLiteral non-null");
check(a4.getHostAddress().equals("127.0.0.1"), "host address");
check(a4.isLoopbackAddress(), "loopback");
check(a4.getAddress().length == 4, "4 bytes");
check(a4 instanceof java.net.InetAddress, "InetAddress");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
