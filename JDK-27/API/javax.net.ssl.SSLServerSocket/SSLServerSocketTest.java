/*
 * @test
 * @summary javax.net.ssl.SSLServerSocket conformance: API type verification.
 * @run main SSLServerSocketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLServerSocketTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SSLServerSocket");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("java.net.ServerSocket"), "extends java.net.ServerSocket");
check(hasMethod(c, "getEnabledCipherSuites"), "getEnabledCipherSuites present");
check(hasMethod(c, "setEnabledCipherSuites"), "setEnabledCipherSuites present");
check(hasMethod(c, "getSupportedCipherSuites"), "getSupportedCipherSuites present");
check(hasMethod(c, "getSupportedProtocols"), "getSupportedProtocols present");
check(hasMethod(c, "getEnabledProtocols"), "getEnabledProtocols present");
check(hasMethod(c, "setEnabledProtocols"), "setEnabledProtocols present");
check(hasMethod(c, "setNeedClientAuth"), "setNeedClientAuth present");
check(hasMethod(c, "getNeedClientAuth"), "getNeedClientAuth present");
check(hasMethod(c, "setWantClientAuth"), "setWantClientAuth present");
check(hasMethod(c, "getWantClientAuth"), "getWantClientAuth present");
check(hasMethod(c, "setUseClientMode"), "setUseClientMode present");
check(hasMethod(c, "getUseClientMode"), "getUseClientMode present");
check(hasMethod(c, "setEnableSessionCreation"), "setEnableSessionCreation present");
check(hasMethod(c, "getEnableSessionCreation"), "getEnableSessionCreation present");
check(hasMethod(c, "getSSLParameters"), "getSSLParameters present");
check(hasMethod(c, "setSSLParameters"), "setSSLParameters present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
