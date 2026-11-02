/*
 * @test
 * @summary javax.net.ssl.SSLEngine conformance: API type verification.
 * @run main SSLEngineTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLEngineTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SSLEngine");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getPeerHost"), "getPeerHost present");
check(hasMethod(c, "getPeerPort"), "getPeerPort present");
check(hasMethod(c, "wrap"), "wrap present");
check(hasMethod(c, "unwrap"), "unwrap present");
check(hasMethod(c, "getDelegatedTask"), "getDelegatedTask present");
check(hasMethod(c, "closeInbound"), "closeInbound present");
check(hasMethod(c, "isInboundDone"), "isInboundDone present");
check(hasMethod(c, "closeOutbound"), "closeOutbound present");
check(hasMethod(c, "isOutboundDone"), "isOutboundDone present");
check(hasMethod(c, "getSupportedCipherSuites"), "getSupportedCipherSuites present");
check(hasMethod(c, "getEnabledCipherSuites"), "getEnabledCipherSuites present");
check(hasMethod(c, "setEnabledCipherSuites"), "setEnabledCipherSuites present");
check(hasMethod(c, "getSupportedProtocols"), "getSupportedProtocols present");
check(hasMethod(c, "getEnabledProtocols"), "getEnabledProtocols present");
check(hasMethod(c, "setEnabledProtocols"), "setEnabledProtocols present");
check(hasMethod(c, "getSession"), "getSession present");
check(hasMethod(c, "getHandshakeSession"), "getHandshakeSession present");
check(hasMethod(c, "beginHandshake"), "beginHandshake present");
check(hasMethod(c, "getHandshakeStatus"), "getHandshakeStatus present");
check(hasMethod(c, "setUseClientMode"), "setUseClientMode present");
check(hasMethod(c, "getUseClientMode"), "getUseClientMode present");
check(hasMethod(c, "setNeedClientAuth"), "setNeedClientAuth present");
check(hasMethod(c, "getNeedClientAuth"), "getNeedClientAuth present");
check(hasMethod(c, "setWantClientAuth"), "setWantClientAuth present");
check(hasMethod(c, "getWantClientAuth"), "getWantClientAuth present");
check(hasMethod(c, "setEnableSessionCreation"), "setEnableSessionCreation present");
check(hasMethod(c, "getEnableSessionCreation"), "getEnableSessionCreation present");
check(hasMethod(c, "getSSLParameters"), "getSSLParameters present");
check(hasMethod(c, "setSSLParameters"), "setSSLParameters present");
check(hasMethod(c, "getApplicationProtocol"), "getApplicationProtocol present");
check(hasMethod(c, "getHandshakeApplicationProtocol"), "getHandshakeApplicationProtocol present");
check(hasMethod(c, "setHandshakeApplicationProtocolSelector"), "setHandshakeApplicationProtocolSelector present");
check(hasMethod(c, "getHandshakeApplicationProtocolSelector"), "getHandshakeApplicationProtocolSelector present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
