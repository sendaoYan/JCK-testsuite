/*
 * @test
 * @summary javax.net.ssl.SSLSocket conformance: white-box structure + black-box behavior (unconnected semantics).
 * @run main SSLSocketTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == java.net.Socket.class, "superclass java.net.Socket");
        String[] names = {"startHandshake", "addHandshakeCompletedListener", "removeHandshakeCompletedListener",
                "getSession", "getUseClientMode", "setUseClientMode", "getEnableSessionCreation",
                "setEnableSessionCreation", "getSupportedCipherSuites", "getEnabledCipherSuites",
                "setEnabledCipherSuites", "getSupportedProtocols", "getEnabledProtocols",
                "setEnabledProtocols", "getSSLParameters", "setSSLParameters"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // unconnected socket from the default factory: no network, no handshake
        javax.net.ssl.SSLSocket s = (javax.net.ssl.SSLSocket) javax.net.ssl.SSLSocketFactory.getDefault().createSocket();
        check(!s.isConnected(), "unconnected socket not connected");

        // mode defaults + roundtrip
        check(s.getUseClientMode(), "client mode default true");
        s.setUseClientMode(true);
        check(s.getUseClientMode(), "client mode roundtrip");
        check(s.getEnableSessionCreation(), "session creation default true");
        s.setEnableSessionCreation(false);
        check(!s.getEnableSessionCreation(), "session creation toggled");
        s.setEnableSessionCreation(true);

        // cipher suite + protocol configuration (valid values only, no handshake)
        check(s.getSupportedCipherSuites().length > 0, "supported ciphers non-empty");
        String[] enabled = s.getEnabledCipherSuites();
        check(enabled.length > 0, "enabled ciphers non-empty");
        s.setEnabledCipherSuites(enabled);
        check(java.util.Arrays.equals(s.getEnabledCipherSuites(), enabled), "enabled ciphers roundtrip");
        check(s.getSupportedProtocols().length > 0, "supported protocols non-empty");
        String[] protos = s.getEnabledProtocols();
        check(protos.length > 0, "enabled protocols non-empty");
        s.setEnabledProtocols(protos);
        check(java.util.Arrays.equals(s.getEnabledProtocols(), protos), "enabled protocols roundtrip");

        // SSLParameters roundtrip
        javax.net.ssl.SSLParameters p = s.getSSLParameters();
        p.setNeedClientAuth(false);
        s.setSSLParameters(p);
        check(s.getSSLParameters() != null, "SSLParameters roundtrip non-null");

        // handshake-completed listener add/remove without a handshake
        javax.net.ssl.HandshakeCompletedListener l = e -> {};
        s.addHandshakeCompletedListener(l);
        s.removeHandshakeCompletedListener(l);
        check(true, "add/remove handshake completed listener");

        // unconnected semantics: getSession yields a session, but startHandshake cannot proceed
        check(s.getSession() != null, "getSession non-null on unconnected socket");
        check(expect(() -> { s.startHandshake(); return "ok"; }).equals("SocketException"),
                "startHandshake on unconnected socket -> SocketException");

        s.close();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
