/*
 * @test
 * @summary javax.net.ssl.SSLServerSocket conformance: white-box structure + black-box behavior (unbound semantics).
 * @run main SSLServerSocketTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLServerSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLServerSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == java.net.ServerSocket.class, "superclass java.net.ServerSocket");
        String[] names = {"getUseClientMode", "setUseClientMode", "getEnableSessionCreation",
                "setEnableSessionCreation", "getNeedClientAuth", "setNeedClientAuth",
                "getWantClientAuth", "setWantClientAuth", "getSupportedCipherSuites",
                "getEnabledCipherSuites", "setEnabledCipherSuites", "getSupportedProtocols",
                "getEnabledProtocols", "setEnabledProtocols", "getSSLParameters", "setSSLParameters"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // unbound server socket from the default factory: no port binding, no accept
        javax.net.ssl.SSLServerSocket ss = (javax.net.ssl.SSLServerSocket)
                javax.net.ssl.SSLServerSocketFactory.getDefault().createServerSocket();
        check(!ss.isBound(), "unbound server socket not bound");

        // mode defaults
        check(!ss.getUseClientMode(), "server mode default false");
        ss.setUseClientMode(true);
        check(ss.getUseClientMode(), "use client mode settable");
        ss.setUseClientMode(false);
        check(!ss.getUseClientMode(), "use client mode reset");

        check(ss.getEnableSessionCreation(), "session creation default true");
        ss.setEnableSessionCreation(false);
        check(!ss.getEnableSessionCreation(), "session creation toggled");
        ss.setEnableSessionCreation(true);

        // client authentication flags (need/want are mutually exclusive)
        check(!ss.getNeedClientAuth() && !ss.getWantClientAuth(), "client auth default false");
        ss.setWantClientAuth(true);
        check(ss.getWantClientAuth() && !ss.getNeedClientAuth(), "want client auth sets want only");
        ss.setNeedClientAuth(true);
        check(ss.getNeedClientAuth() && !ss.getWantClientAuth(), "need client auth resets want");
        ss.setNeedClientAuth(false);
        check(!ss.getNeedClientAuth() && !ss.getWantClientAuth(), "client auth cleared");

        // cipher + protocol configuration (valid values only)
        check(ss.getSupportedCipherSuites().length > 0, "supported ciphers non-empty");
        String[] enabled = ss.getEnabledCipherSuites();
        check(enabled.length > 0, "enabled ciphers non-empty");
        ss.setEnabledCipherSuites(enabled);
        check(java.util.Arrays.equals(ss.getEnabledCipherSuites(), enabled), "enabled ciphers roundtrip");
        check(ss.getSupportedProtocols().length > 0, "supported protocols non-empty");
        String[] protos = ss.getEnabledProtocols();
        check(protos.length > 0, "enabled protocols non-empty");
        ss.setEnabledProtocols(protos);
        check(java.util.Arrays.equals(ss.getEnabledProtocols(), protos), "enabled protocols roundtrip");

        // SSLParameters roundtrip
        javax.net.ssl.SSLParameters p = ss.getSSLParameters();
        check(p != null, "SSLParameters non-null");
        ss.setSSLParameters(p);
        check(ss.getSSLParameters() != null, "SSLParameters roundtrip non-null");

        // unbound accept fails cleanly (SocketException), never binds a port
        check(expect(() -> { ss.accept(); return "ok"; }).equals("SocketException"),
                "accept before bind -> SocketException");

        ss.close();
        check(ss.isClosed(), "closed");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
