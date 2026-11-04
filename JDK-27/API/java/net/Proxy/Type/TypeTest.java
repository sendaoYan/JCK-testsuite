/*
 * @test
 * @summary java.net.Proxy.Type conformance: API type verification.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.Proxy.Type.valueOf("SOCKS") == java.net.Proxy.Type.SOCKS, "valueOf");
check(java.net.Proxy.Type.values().length == 3, "3 values");
check(java.net.Proxy.Type.DIRECT.name().equals("DIRECT"), "DIRECT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
