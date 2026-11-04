/*
 * @test
 * @summary javax.net.ssl.SSLEngine conformance: white-box structure + black-box behavior (unconnected semantics).
 * @run main SSLEngineTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
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
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
