/*
 * @test
 * @summary java.net.URLPermission conformance: API type verification.
 * @run main URLPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URLPermission up = new java.net.URLPermission("http://example.com/path", "GET");
check(up.getName().equals("http://example.com/path"), "name");
check(up.getActions() != null, "actions");
check(up instanceof java.security.Permission, "Permission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
