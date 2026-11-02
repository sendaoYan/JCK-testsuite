/*
 * @test
 * @summary java.net.InetSocketAddress conformance: API type verification.
 * @run main InetSocketAddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InetSocketAddressTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.InetSocketAddress sa = new java.net.InetSocketAddress("localhost", 8080);
check(sa.getPort() == 8080, "port");
check(sa.getHostString().equals("localhost"), "hostString");
java.net.InetSocketAddress un = java.net.InetSocketAddress.createUnresolved("example.com", 80);
check(un.isUnresolved(), "unresolved");
check(un.getAddress() == null, "unresolved address null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
