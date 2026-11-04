/*
 * @test
 * @summary javax.net.ssl.SSLEngine conformance: white-box structure + black-box behavior (unconnected semantics).
 * @run main SSLEngineTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import java.io.*;
import javax.net.ssl.*;
public class SSLEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngine");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] names = {"wrap", "unwrap", "beginHandshake", "closeOutbound", "closeInbound",
                "getHandshakeStatus", "getSession", "getHandshakeSession", "getUseClientMode",
                "setUseClientMode", "getSupportedCipherSuites", "getEnabledCipherSuites",
                "getSupportedProtocols", "getEnabledProtocols", "getSSLParameters", "setSSLParameters"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        javax.net.ssl.SSLEngine cli = ctx.createSSLEngine("client-host", 1234);
        javax.net.ssl.SSLEngine srv = ctx.createSSLEngine("server-host", 4321);
        cli.setUseClientMode(true);
        srv.setUseClientMode(false);
        check(cli.getUseClientMode() && !srv.getUseClientMode(), "use client mode");
        check("client-host".equals(cli.getPeerHost()) && cli.getPeerPort() == 1234, "client peer");
        check("server-host".equals(srv.getPeerHost()) && srv.getPeerPort() == 4321, "server peer");

        // enabled/supported cipher suites + protocols (standard algorithms only, no handshake)
        check(cli.getSupportedCipherSuites().length > 0, "supported ciphers non-empty");
        check(srv.getSupportedProtocols().length > 0, "supported protocols non-empty");
        String[] enabledCiphers = cli.getEnabledCipherSuites();
        check(enabledCiphers.length > 0, "enabled ciphers non-empty");
        cli.setEnabledCipherSuites(enabledCiphers);
        check(java.util.Arrays.equals(cli.getEnabledCipherSuites(), enabledCiphers), "enabled ciphers roundtrip");
        String[] enabledProtos = cli.getEnabledProtocols();
        check(enabledProtos.length > 0, "enabled protocols non-empty");
        cli.setEnabledProtocols(enabledProtos);
        check(java.util.Arrays.equals(cli.getEnabledProtocols(), enabledProtos), "enabled protocols roundtrip");
        check(expect(() -> { cli.setEnabledCipherSuites(new String[]{"BOGUS_CIPHER"}); return "ok"; })
                .equals("IllegalArgumentException"), "invalid cipher rejected");
        check(expect(() -> { cli.setEnabledProtocols(new String[]{"BOGUS_PROTO"}); return "ok"; })
                .equals("IllegalArgumentException"), "invalid protocol rejected");

        // SSLParameters roundtrip (SNI only; no ALPN on the JDK-8 engine path)
        javax.net.ssl.SSLParameters cp = cli.getSSLParameters();
        cp.setServerNames(java.util.Arrays.asList(new javax.net.ssl.SNIHostName("localhost")));
        cli.setSSLParameters(cp);
        check(cli.getSSLParameters().getServerNames().size() == 1, "SSLParameters SNI roundtrip");

        // handshake state machine start (no completed handshake, no real network)
        check(cli.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, "initial NOT_HANDSHAKING");
        cli.beginHandshake();
        check(cli.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_WRAP, "client begins NEED_WRAP");
        srv.beginHandshake();
        check(srv.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP, "server begins NEED_UNWRAP");

        // unconnected session queries are safe (may return a not-yet-valid session)
        check(cli.getSession() != null, "getSession non-null before handshake completes");
        check(cli.getHandshakeSession() == null, "getHandshakeSession null before handshake");

        // closing state
        check(!cli.isInboundDone(), "inbound not done initially");
        cli.closeOutbound();
        check(cli.isInboundDone() == false, "inbound still not done after closeOutbound");
        srv.closeOutbound();
        check(!srv.isInboundDone(), "server inbound not done after closeOutbound");
    }
    // ---- Part 3: contract deepening (full dual-engine handshake + data pump) ----
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
        SSLEngine cli = ctx.createSSLEngine("client-host", 1234);
        SSLEngine srv = ctx.createSSLEngine();
        cli.setUseClientMode(true);
        srv.setUseClientMode(false);
        SSLParameters cp = cli.getSSLParameters();
        cp.setServerNames(java.util.Arrays.asList(new SNIHostName("localhost")));
        cp.setApplicationProtocols(new String[]{"proto-a"});
        cli.setSSLParameters(cp);
        SSLParameters sp = srv.getSSLParameters();
        sp.setApplicationProtocols(new String[]{"proto-a", "proto-b"});
        srv.setSSLParameters(sp);
        cli.beginHandshake();
        srv.beginHandshake();
        java.nio.ByteBuffer inC = java.nio.ByteBuffer.allocate(32768), inS = java.nio.ByteBuffer.allocate(32768);
        java.nio.ByteBuffer appC = java.nio.ByteBuffer.allocate(32768), appS = java.nio.ByteBuffer.allocate(32768);
        java.nio.ByteBuffer tmp = java.nio.ByteBuffer.allocate(32768);
        boolean finC = false, finS = false; int taskRuns = 0;
        outer:
        for (int step = 0; step < 500; step++) {
            for (SSLEngine e : new SSLEngine[]{cli, srv}) {
                boolean myFin = false;
                javax.net.ssl.SSLEngineResult r;
                switch (e.getHandshakeStatus()) {
                    case NEED_WRAP:
                        tmp.clear();
                        r = e.wrap(java.nio.ByteBuffer.allocate(0), tmp);
                        if (r.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED) myFin = true;
                        tmp.flip();
                        java.nio.ByteBuffer peerIn = (e == cli) ? inS : inC;
                        while (tmp.hasRemaining()) peerIn.put(tmp.get());
                        break;
                    case NEED_UNWRAP:
                        java.nio.ByteBuffer in = (e == cli) ? inC : inS;
                        in.flip();
                        r = e.unwrap(in, (e == cli) ? appC : appS);
                        in.compact();
                        if (r.getStatus() != javax.net.ssl.SSLEngineResult.Status.BUFFER_UNDERFLOW
                                && r.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED) myFin = true;
                        break;
                    case NEED_TASK:
                        Runnable t; while ((t = e.getDelegatedTask()) != null) { t.run(); taskRuns++; }
                        break;
                    default: break;
                }
                if (e == cli && myFin) finC = true;
                if (e == srv && myFin) finS = true;
            }
            if (finC && finS) break outer;
        }
        check(finC && finS, "engine handshake finished on both sides");
        check(taskRuns > 0, "delegated tasks executed during handshake");
        check(cli.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                "client NOT_HANDSHAKING after handshake");
        check(cli.getSession().isValid(), "engine session valid");
        check(cli.getSession().getProtocol().startsWith("TLSv1"), "engine session protocol TLSv1.x");
        SSLSession ssSess = srv.getSession();
        check(ssSess instanceof ExtendedSSLSession
                && ((ExtendedSSLSession) ssSess).getRequestedServerNames().size() == 1,
                "SNI host name delivered to the server engine");
        check(cli.getHandshakeSession() == null, "handshake session null after handshake");
        // application data flows through wrap/unwrap
        tmp.clear();
        javax.net.ssl.SSLEngineResult r = cli.wrap(java.nio.ByteBuffer.wrap("hello-engine".getBytes("UTF-8")), tmp);
        check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.OK && r.bytesConsumed() == 12,
                "application wrap OK, 12 bytes consumed");
        tmp.flip(); inS.put(tmp); inS.flip();
        appS.clear();
        r = srv.unwrap(inS, appS); appS.flip();
        check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.OK
                && java.nio.charset.StandardCharsets.UTF_8.decode(appS).toString().equals("hello-engine"),
                "application data delivered through unwrap");
        // orderly close
        cli.closeOutbound();
        check(!cli.isInboundDone(), "inbound still open after closeOutbound");
        tmp.clear();
        r = cli.wrap(java.nio.ByteBuffer.allocate(0), tmp);
        check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.CLOSED, "wrap after closeOutbound -> CLOSED");
        check(cli.isOutboundDone(), "outbound done after close_notify");
        check(expect(() -> { cli.setUseClientMode(false); return "ok"; }).equals("IllegalArgumentException"),
                "setUseClientMode after initial handshake -> IllegalArgumentException");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
