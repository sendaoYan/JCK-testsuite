/*
 * @test
 * @summary java.net.StandardProtocolFamily conformance: API type verification.
 * @run main StandardProtocolFamilyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StandardProtocolFamilyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.StandardProtocolFamily.INET.name().equals("INET"), "INET");
check(java.net.StandardProtocolFamily.valueOf("INET6").name().equals("INET6"), "valueOf");
check(java.net.StandardProtocolFamily.UNIX instanceof java.net.ProtocolFamily, "ProtocolFamily");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
