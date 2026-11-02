/*
 * @test
 * @summary javax.net.ssl.SSLPermission conformance: API type verification.
 * @run main SSLPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLPermission p = new javax.net.ssl.SSLPermission("getSSLSessionContext");
check(p.getName().equals("getSSLSessionContext"), "name");
check(p instanceof java.security.BasicPermission, "BasicPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
