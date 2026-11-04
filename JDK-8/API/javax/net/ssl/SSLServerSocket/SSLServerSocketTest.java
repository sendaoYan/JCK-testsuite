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
    // ---- Part 3: contract deepening (real accept/handshake over loopback) ----
    static final String B64 = "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";
    static final char[] PASS = "passphrase".toCharArray();
    static KeyStore keyStore() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(B64)), PASS);
        return ks;
    }
    static SSLContext newContext() throws Exception {
        KeyStore ks = keyStore();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, PASS);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ts = KeyStore.getInstance("PKCS12"); ts.load(null, null);
        ts.setCertificateEntry("server", ks.getCertificate("server"));
        tmf.init(ts);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
        return ctx;
    }
    static void testContract() throws Exception {
        SSLContext ctx = newContext();
        try (SSLServerSocket ss = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0, 5, java.net.InetAddress.getLoopbackAddress())) {
            check(ss.getLocalPort() > 0, "ephemeral port bound");
            check(ss.getInetAddress().isLoopbackAddress(), "loopback address bound");
            check(ss.getEnabledProtocols().length > 0, "enabled protocols non-empty");
            check(expect(() -> { ss.setEnabledCipherSuites(new String[]{"BOGUS"}); return "ok"; }).equals("IllegalArgumentException"),
                    "invalid cipher suite rejected");
            check(expect(() -> { ss.setEnabledProtocols(new String[]{"BOGUS"}); return "ok"; }).equals("IllegalArgumentException"),
                    "invalid protocol rejected");
            // a bounded accept timeout is honored deterministically
            ss.setSoTimeout(300);
            long t0 = System.currentTimeMillis();
            Object res = expect(() -> ss.accept());
            long dt = System.currentTimeMillis() - t0;
            check(res.equals("SocketTimeoutException"), "accept with SO_TIMEOUT -> SocketTimeoutException, got " + res);
            check(dt >= 200 && dt < 5000, "accept timeout honored (" + dt + "ms)");
            ss.setSoTimeout(0);
            // full accept + handshake + echo round trip
            final int port = ss.getLocalPort();
            Thread srv = new Thread(() -> {
                try (SSLSocket s = (SSLSocket) ss.accept()) {
                    s.startHandshake();
                    s.getOutputStream().write(s.getInputStream().read()); s.getOutputStream().flush();
                } catch (Exception e) {}
            });
            srv.setDaemon(true); srv.start();
            try (SSLSocket c = (SSLSocket) ctx.getSocketFactory().createSocket(java.net.InetAddress.getLoopbackAddress(), port)) {
                c.startHandshake();
                c.getOutputStream().write('Q'); c.getOutputStream().flush();
                check(c.getInputStream().read() == 'Q', "accept handshake echo roundtrip");
            }
            srv.join(8000);
        }
        // server-socket side pinned to TLSv1.2, client pinned too -> negotiated TLSv1.2
        SSLContext ctx2 = newContext();
        try (SSLServerSocket ss = (SSLServerSocket) ctx2.getServerSocketFactory().createServerSocket(0, 5, java.net.InetAddress.getLoopbackAddress())) {
            ss.setEnabledProtocols(new String[]{"TLSv1.2"});
            final int port = ss.getLocalPort();
            Thread srv = new Thread(() -> {
                try (SSLSocket s = (SSLSocket) ss.accept()) {
                    s.startHandshake();
                    check2(s.getSession().getProtocol().equals("TLSv1.2"));
                } catch (Exception e) {}
            });
            srv.setDaemon(true); srv.start();
            try (SSLSocket c = (SSLSocket) ctx2.getSocketFactory().createSocket(java.net.InetAddress.getLoopbackAddress(), port)) {
                c.setEnabledProtocols(new String[]{"TLSv1.2"});
                c.startHandshake();
                check(c.getSession().getProtocol().equals("TLSv1.2"), "pinned server negotiated TLSv1.2");
            }
            srv.join(8000);
            check(pinnedOk, "server-side pinned protocol observed");
        }
    }
    static volatile boolean pinnedOk = true;
    static void check2(boolean b) { if (!b) pinnedOk = false; }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
