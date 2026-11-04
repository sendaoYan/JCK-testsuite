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
    // ---- Part 3: contract deepening (real handshake over loopback) ----
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
            final int port = ss.getLocalPort();
            final java.util.List<String> log = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
            Thread srv = new Thread(() -> {
                try (SSLSocket s = (SSLSocket) ss.accept()) {
                    s.addHandshakeCompletedListener(e -> log.add("HCE:" + e.getCipherSuite() + ":" + e.getSession().getProtocol()));
                    s.startHandshake();
                    java.io.InputStream in = s.getInputStream();
                    java.io.OutputStream out = s.getOutputStream();
                    byte[] buf = new byte[64];
                    int n = in.read(buf);
                    out.write(buf, 0, n); out.flush();
                } catch (Exception e) { log.add("SRV-ERR:" + e); }
            });
            srv.setDaemon(true); srv.start();
            SSLSocket c = (SSLSocket) ctx.getSocketFactory().createSocket(java.net.InetAddress.getLoopbackAddress(), port);
            // register the client handshake-completed listener BEFORE the first handshake
            final int[] clientHce = new int[1];
            c.addHandshakeCompletedListener(e -> clientHce[0]++);
            c.startHandshake();
            check(java.util.Arrays.asList(c.getEnabledProtocols()).contains("TLSv1.3"),
                    "JDK-8u372 enables TLSv1.3 by default");
            c.startHandshake(); // renegotiation-style restart triggers the event again
            c.getOutputStream().write("hello".getBytes("UTF-8")); c.getOutputStream().flush();
            byte[] back = new byte[5];
            int r = c.getInputStream().read(back);
            check(r == 5 && new String(back, "UTF-8").equals("hello"), "TLS echo roundtrip");
            check(clientHce[0] >= 1, "client handshake-completed event fired");
            // negatives
            check(expect(() -> { c.addHandshakeCompletedListener(null); return "ok"; }).equals("IllegalArgumentException"),
                    "addHandshakeCompletedListener(null) -> IllegalArgumentException");
            check(expect(() -> { c.setUseClientMode(false); return "ok"; }).equals("IllegalArgumentException"),
                    "setUseClientMode after handshake -> IllegalArgumentException");
            check(expect(() -> { c.setEnabledCipherSuites(new String[]{"BOGUS_CIPHER"}); return "ok"; }).equals("IllegalArgumentException"),
                    "invalid cipher suite rejected");
            check(expect(() -> { c.setEnabledProtocols(new String[]{"BOGUS_PROTO"}); return "ok"; }).equals("IllegalArgumentException"),
                    "invalid protocol rejected");
            // SNI parameters survive a round trip through the socket
            SSLParameters sp = c.getSSLParameters();
            sp.setServerNames(java.util.Arrays.asList(new SNIHostName("localhost")));
            c.setSSLParameters(sp);
            check(c.getSSLParameters().getServerNames().size() == 1, "socket SSLParameters SNI roundtrip");
            c.close();
            srv.join(8000);
            check(log.stream().anyMatch(s -> s.startsWith("HCE:")), "server handshake-completed event fired");
        }
        // second pair pinned to TLSv1.2 only (deterministic protocol selection)
        SSLContext ctx2 = newContext();
        try (SSLServerSocket ss = (SSLServerSocket) ctx2.getServerSocketFactory().createServerSocket(0, 5, java.net.InetAddress.getLoopbackAddress())) {
            ss.setEnabledProtocols(new String[]{"TLSv1.2"});
            final int port = ss.getLocalPort();
            Thread srv = new Thread(() -> {
                try (SSLSocket s = (SSLSocket) ss.accept()) {
                    s.startHandshake();
                    s.getOutputStream().write(s.getInputStream().read()); s.getOutputStream().flush();
                } catch (Exception e) {}
            });
            srv.setDaemon(true); srv.start();
            try (SSLSocket c = (SSLSocket) ctx2.getSocketFactory().createSocket(java.net.InetAddress.getLoopbackAddress(), port)) {
                c.setEnabledProtocols(new String[]{"TLSv1.2"});
                c.startHandshake();
                check(c.getSession().getProtocol().equals("TLSv1.2"), "pinned TLSv1.2 negotiated");
                c.getOutputStream().write('P'); c.getOutputStream().flush();
                check(c.getInputStream().read() == 'P', "pinned-protocol echo roundtrip");
            }
            srv.join(8000);
        }
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
