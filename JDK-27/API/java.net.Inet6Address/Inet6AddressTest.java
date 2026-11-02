/*
 * @test
 * @summary java.net.Inet6Address conformance: API type verification.
 * @run main Inet6AddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class Inet6AddressTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.Inet6Address a6 = java.net.Inet6Address.getByAddress(null, new byte[16], 0);
check(a6.getAddress().length == 16, "16 bytes");
check(a6 instanceof java.net.InetAddress, "InetAddress");
check(java.net.InetAddress.ofLiteral("::1") instanceof java.net.Inet6Address, "ofLiteral ::1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
