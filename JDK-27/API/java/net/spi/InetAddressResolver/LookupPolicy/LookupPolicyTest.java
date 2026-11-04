/*
 * @test
 * @summary java.net.spi.InetAddressResolver.LookupPolicy conformance: API type verification.
 * @run main LookupPolicyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LookupPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.spi.InetAddressResolver.LookupPolicy lp = java.net.spi.InetAddressResolver.LookupPolicy.of(java.net.spi.InetAddressResolver.LookupPolicy.IPV4);
check(lp.characteristics() == java.net.spi.InetAddressResolver.LookupPolicy.IPV4, "characteristics");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
