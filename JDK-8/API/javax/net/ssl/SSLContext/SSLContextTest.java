/*
 * @test
 * @summary javax.net.ssl.SSLContext conformance: white-box structure + black-box behavior.
 * @run main SSLContextTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        String[] names = {"getInstance", "getDefault", "getProtocol", "getProvider", "init",
                "getSocketFactory", "getServerSocketFactory", "createSSLEngine",
                "getDefaultSSLParameters", "getSupportedSSLParameters",
                "getClientSessionContext", "getServerSessionContext"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default context is always available
        javax.net.ssl.SSLContext def = javax.net.ssl.SSLContext.getDefault();
        check(def != null, "default non-null");
        check(def.getProtocol() != null && !def.getProtocol().isEmpty(), "default protocol");
        check(def.getProvider() != null, "default provider");

        // factory + protocol aliases (JDK-8 has no TLSv1.3 context)
        javax.net.ssl.SSLContext tls = javax.net.ssl.SSLContext.getInstance("TLS");
        javax.net.ssl.SSLContext tls12 = javax.net.ssl.SSLContext.getInstance("TLSv1.2");
        javax.net.ssl.SSLContext ssl = javax.net.ssl.SSLContext.getInstance("SSL");
        check("TLS".equals(tls.getProtocol()), "TLS protocol");
        check("TLSv1.2".equals(tls12.getProtocol()), "TLSv1.2 protocol");
        check("SSL".equals(ssl.getProtocol()), "SSL protocol");
        check(tls.getProvider() != null, "provider non-null");
        check(expect(() -> { javax.net.ssl.SSLContext.getInstance("BOGUS"); return "ok"; })
                .equals("NoSuchAlgorithmException"), "unknown algorithm -> NoSuchAlgorithmException");
        check(expect(() -> { javax.net.ssl.SSLContext.getInstance("TLS", "NoSuchProviderXYZ"); return "ok"; })
                .equals("NoSuchProviderException"), "unknown provider -> NoSuchProviderException");

        // init(null,null,null) then factories + engines (no key material needed, no handshake)
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        check(ctx.getSocketFactory() != null, "socket factory non-null");
        check(ctx.getServerSocketFactory() != null, "server socket factory non-null");
        check(ctx.getSocketFactory() instanceof javax.net.ssl.SSLSocketFactory, "factory is SSLSocketFactory");
        check(ctx.getServerSocketFactory() instanceof javax.net.ssl.SSLServerSocketFactory, "server factory type");

        javax.net.ssl.SSLEngine e1 = ctx.createSSLEngine();
        javax.net.ssl.SSLEngine e2 = ctx.createSSLEngine("peer", 8443);
        check(e1 != null && e2 != null, "engines non-null");
        check(e1.getPeerHost() == null && e1.getPeerPort() == -1, "default engine no peer");
        check("peer".equals(e2.getPeerHost()) && e2.getPeerPort() == 8443, "engine peer set");

        check(ctx.getDefaultSSLParameters() != null, "default SSLParameters non-null");
        check(ctx.getSupportedSSLParameters() != null, "supported SSLParameters non-null");
        javax.net.ssl.SSLParameters sup = ctx.getSupportedSSLParameters();
        check(sup.getProtocols() != null && sup.getProtocols().length > 0, "supported protocols non-empty");
        check(sup.getCipherSuites() != null && sup.getCipherSuites().length > 0, "supported ciphers non-empty");
        check(ctx.getClientSessionContext() != null && ctx.getServerSessionContext() != null, "session contexts non-null");
    }
    // ---- Part 3: contract deepening (factory matrix + default context lifecycle) ----
    static void testContract() throws Exception {
        // algorithm matrix (fixed alias set, present on this JDK)
        for (String alg : new String[]{"TLS", "Default", "TLSv1.2", "TLSv1.3", "TLSv1", "TLSv1.1", "SSLv3"}) {
            SSLContext c = SSLContext.getInstance(alg);
            check(c.getProtocol().equals(alg), "getInstance(" + alg + ").getProtocol() echoes the name");
            check(c.getProvider() != null && "SunJSSE".equals(c.getProvider().getName()),
                    "getInstance(" + alg + ") resolved by SunJSSE");
        }
        check(expect(() -> { SSLContext.getInstance(null); return "ok"; }).equals("NullPointerException"),
                "getInstance(null) -> NullPointerException");
        check(expect(() -> { SSLContext.getInstance("NoSuchTLS"); return "ok"; }).equals("NoSuchAlgorithmException"),
                "unknown protocol -> NoSuchAlgorithmException");
        // DTLS support differs by JDK release
                check(expect(() -> { SSLContext.getInstance("DTLS"); return "ok"; }).equals("NoSuchAlgorithmException"),
                "JDK-8 has no DTLS context -> NoSuchAlgorithmException");
        // the default context is a lazily created "Default" protocol context
        check("Default".equals(SSLContext.getDefault().getProtocol()),
                "default context protocol is Default");
        SSLContext prev = SSLContext.getDefault();
        SSLContext mine = SSLContext.getInstance("TLS");
        mine.init(null, null, null);
        SSLContext.setDefault(mine);
        check(SSLContext.getDefault() == mine, "setDefault installs the new default");
        SSLContext.setDefault(prev);
        check(SSLContext.getDefault() == prev, "default restored");
        check(expect(() -> { SSLContext.setDefault(null); return "ok"; }).equals("NullPointerException"),
                "setDefault(null) -> NullPointerException");
        // init semantics: null arrays are legal (defaults), re-init is legal
        SSLContext u = SSLContext.getInstance("TLS");
        check(expect(() -> { u.createSSLEngine(); return "ok"; }).equals("IllegalStateException"),
                "createSSLEngine before init -> IllegalStateException");
        check(expect(() -> { u.getSocketFactory(); return "ok"; }).equals("IllegalStateException"),
                "getSocketFactory before init -> IllegalStateException");
        u.init(null, null, null);
        check(expect(() -> { u.init(null, null, null); return "ok"; }).equals("ok"),
                "re-init after init is allowed");
        // factory accessors mint a fresh instance per call
        SSLContext d = SSLContext.getInstance("TLS"); d.init(null, null, null);
        check(d.getSocketFactory() != d.getSocketFactory(),
                "getSocketFactory returns a fresh instance per call");
        check(d.getClientSessionContext() == d.getClientSessionContext(),
                "getClientSessionContext returns the same context object");
        check(d.getClientSessionContext() != d.getServerSessionContext(),
                "client and server session contexts are distinct");
        // parameter views: supported is a superset of default, both non-empty
        SSLParameters dp = d.getDefaultSSLParameters(), sp = d.getSupportedSSLParameters();
        check(dp.getProtocols() != null && dp.getProtocols().length > 0, "default parameters have protocols");
        java.util.Set<String> sup = new java.util.HashSet<String>(java.util.Arrays.asList(sp.getProtocols()));
        for (String p : dp.getProtocols()) check(sup.contains(p), "default protocol " + p + " is supported");
        check(sp.getCipherSuites() != null && sp.getCipherSuites().length > 0, "supported cipher suites non-empty");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
