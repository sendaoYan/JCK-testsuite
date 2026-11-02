/*
 * @test
 * @summary java.net.StandardSocketOptions conformance: API type verification.
 * @run main StandardSocketOptionsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StandardSocketOptionsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.StandardSocketOptions.SO_REUSEADDR != null, "SO_REUSEADDR");
check(java.net.StandardSocketOptions.SO_KEEPALIVE != null, "SO_KEEPALIVE");
check(java.net.StandardSocketOptions.TCP_NODELAY != null, "TCP_NODELAY");
check(java.net.StandardSocketOptions.IP_TOS != null, "IP_TOS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
