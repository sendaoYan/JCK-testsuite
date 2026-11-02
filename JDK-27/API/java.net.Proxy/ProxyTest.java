/*
 * @test
 * @summary java.net.Proxy conformance: API type verification.
 * @run main ProxyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ProxyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("example.com", 8080));
check(p.type() == java.net.Proxy.Type.HTTP, "type");
check(p.address() instanceof java.net.InetSocketAddress, "address");
check(java.net.Proxy.NO_PROXY.type() == java.net.Proxy.Type.DIRECT, "NO_PROXY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
