/*
 * @test
 * @summary java.net.HttpURLConnection conformance: API type verification.
 * @run main HttpURLConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HttpURLConnectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.HttpURLConnection");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("java.net.URLConnection"), "extends java.net.URLConnection");
check(hasMethod(c, "setAuthenticator"), "setAuthenticator present");
check(hasMethod(c, "getHeaderFieldKey"), "getHeaderFieldKey present");
check(hasMethod(c, "setFixedLengthStreamingMode"), "setFixedLengthStreamingMode present");
check(hasMethod(c, "setChunkedStreamingMode"), "setChunkedStreamingMode present");
check(hasMethod(c, "getHeaderField"), "getHeaderField present");
check(hasMethod(c, "setFollowRedirects"), "setFollowRedirects present");
check(hasMethod(c, "getFollowRedirects"), "getFollowRedirects present");
check(hasMethod(c, "setInstanceFollowRedirects"), "setInstanceFollowRedirects present");
check(hasMethod(c, "getInstanceFollowRedirects"), "getInstanceFollowRedirects present");
check(hasMethod(c, "setRequestMethod"), "setRequestMethod present");
check(hasMethod(c, "getRequestMethod"), "getRequestMethod present");
check(hasMethod(c, "getResponseCode"), "getResponseCode present");
check(hasMethod(c, "getResponseMessage"), "getResponseMessage present");
check(hasMethod(c, "getHeaderFieldDate"), "getHeaderFieldDate present");
check(hasMethod(c, "disconnect"), "disconnect present");
check(hasMethod(c, "usingProxy"), "usingProxy present");
check(hasMethod(c, "getPermission"), "getPermission present");
check(hasMethod(c, "getErrorStream"), "getErrorStream present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
