/*
 * @test
 * @summary java.net.SecureCacheResponse conformance: API type verification.
 * @run main SecureCacheResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SecureCacheResponseTest {
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
        Class<?> c = Class.forName("java.net.SecureCacheResponse");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("java.net.CacheResponse"), "extends java.net.CacheResponse");
check(hasMethod(c, "getCipherSuite"), "getCipherSuite present");
check(hasMethod(c, "getLocalCertificateChain"), "getLocalCertificateChain present");
check(hasMethod(c, "getServerCertificateChain"), "getServerCertificateChain present");
check(hasMethod(c, "getPeerPrincipal"), "getPeerPrincipal present");
check(hasMethod(c, "getLocalPrincipal"), "getLocalPrincipal present");
check(hasMethod(c, "getSSLSession"), "getSSLSession present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
