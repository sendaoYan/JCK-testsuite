/*
 * @test
 * @summary java.net.HttpCookie conformance: API type verification.
 * @run main HttpCookieTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HttpCookieTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.HttpCookie c = new java.net.HttpCookie("name", "value");
check(c.getName().equals("name"), "name");
check(c.getValue().equals("value"), "value");
c.setMaxAge(100);
check(c.getMaxAge() == 100, "maxAge");
check(java.net.HttpCookie.parse("a=1").size() == 1, "parse");
check("a".equals(java.net.HttpCookie.parse("a=1").get(0).getName()), "parse name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
