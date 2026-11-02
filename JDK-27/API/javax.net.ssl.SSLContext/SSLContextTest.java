/*
 * @test
 * @summary javax.net.ssl.SSLContext conformance: API type verification.
 * @run main SSLContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getDefault();
check(ctx.getProtocol() != null, "protocol");
check(ctx.getSocketFactory() != null, "socket factory");
check(ctx.getServerSocketFactory() != null, "server socket factory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
