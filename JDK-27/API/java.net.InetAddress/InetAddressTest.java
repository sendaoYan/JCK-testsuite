/*
 * @test
 * @summary java.net.InetAddress conformance: API type verification.
 * @run main InetAddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InetAddressTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.InetAddress lb = java.net.InetAddress.getLoopbackAddress();
check(lb.isLoopbackAddress(), "loopback");
check(lb.getAddress().length == 4 || lb.getAddress().length == 16, "address bytes");
java.net.InetAddress byAddr = java.net.InetAddress.getByAddress(new byte[]{127,0,0,1});
check(byAddr.getHostAddress().equals("127.0.0.1"), "getByAddress");
check(byAddr instanceof java.net.Inet4Address, "Inet4Address");
check(java.net.InetAddress.getByName("127.0.0.1").getHostAddress().equals("127.0.0.1"), "getByName literal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
