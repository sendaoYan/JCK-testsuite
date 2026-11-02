/*
 * @test
 * @summary java.net.CookieManager conformance: API type verification.
 * @run main CookieManagerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CookieManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.CookieManager cm = new java.net.CookieManager();
check(cm.getCookieStore() != null, "cookie store");
check(cm instanceof java.net.CookieHandler, "CookieHandler");
cm.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
