/*
 * @test
 * @summary java.net.NetPermission conformance: API type verification.
 * @run main NetPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NetPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.NetPermission np = new java.net.NetPermission("setDefaultAuthenticator");
check(np.getName().equals("setDefaultAuthenticator"), "name");
check(np instanceof java.security.BasicPermission, "BasicPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
