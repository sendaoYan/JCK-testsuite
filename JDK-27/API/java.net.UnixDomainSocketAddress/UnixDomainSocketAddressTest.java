/*
 * @test
 * @summary java.net.UnixDomainSocketAddress conformance: API type verification.
 * @run main UnixDomainSocketAddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class UnixDomainSocketAddressTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.UnixDomainSocketAddress uds = java.net.UnixDomainSocketAddress.of("/tmp/example.sock");
check(uds.getPath().toString().equals("/tmp/example.sock"), "path");
check(uds instanceof java.net.SocketAddress, "SocketAddress");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
