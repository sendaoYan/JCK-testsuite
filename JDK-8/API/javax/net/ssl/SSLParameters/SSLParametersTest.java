/*
 * @test
 * @summary javax.net.ssl.SSLParameters conformance: white-box structure + black-box behavior.
 * @run main SSLParametersTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Arrays.stream(Class.forName("javax.net.ssl.SSLParameters").getMethods()).anyMatch(m -> m.getName().equals("setServerNames")), "method setServerNames");
        check(Arrays.stream(Class.forName("javax.net.ssl.SSLParameters").getMethods()).anyMatch(m -> m.getName().equals("setApplicationProtocols")), "method setApplicationProtocols");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLParameters p = new javax.net.ssl.SSLParameters();
        check(p.getCipherSuites() == null, "default cipherSuites null");
        check(p.getProtocols() == null, "default protocols null");
        check(!p.getNeedClientAuth() && !p.getWantClientAuth(), "default auth false");
        p.setCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        p.setProtocols(new String[]{"TLSv1.3"});
        check(Arrays.equals(p.getCipherSuites(), new String[]{"TLS_AES_128_GCM_SHA256"}), "cipher roundtrip");
        check(Arrays.equals(p.getProtocols(), new String[]{"TLSv1.3"}), "protocol roundtrip");
        String[] cs = {"TLS_CHACHA20_POLY1305_SHA256"};
        p.setCipherSuites(cs); cs[0] = "MUTATED";
        check(p.getCipherSuites()[0].equals("TLS_CHACHA20_POLY1305_SHA256"), "cipher defensive copy");
        p.setNeedClientAuth(true);
        check(p.getNeedClientAuth() && !p.getWantClientAuth(), "needClientAuth sets need");
        p.setWantClientAuth(true);
        check(p.getWantClientAuth() && !p.getNeedClientAuth(), "wantClientAuth resets need");
        p.setApplicationProtocols(new String[]{"h2", "http/1.1"});
        check(Arrays.equals(p.getApplicationProtocols(), new String[]{"h2", "http/1.1"}), "ALPN roundtrip");
        String[] ap = {"h2"};
        p.setApplicationProtocols(ap); ap[0] = "X";
        check(p.getApplicationProtocols()[0].equals("h2"), "ALPN defensive copy");
        java.util.List<javax.net.ssl.SNIServerName> sni = new java.util.ArrayList<>();
        sni.add(new javax.net.ssl.SNIHostName("example.com"));
        p.setServerNames(sni);
        java.util.List<javax.net.ssl.SNIServerName> got = p.getServerNames();
        check(got.size() == 1 && got.get(0) instanceof javax.net.ssl.SNIHostName, "SNI roundtrip");
        check(expect(() -> { got.add(new javax.net.ssl.SNIHostName("x.com")); return "ok"; }).equals("UnsupportedOperationException"), "serverNames unmodifiable");
        check(expect(() -> { p.setServerNames(java.util.Arrays.asList(new javax.net.ssl.SNIHostName("a"), new javax.net.ssl.SNIHostName("b"))); return "ok"; }).equals("IllegalArgumentException"), "duplicate SNI type rejected");
        p.setSNIMatchers(java.util.Arrays.asList(javax.net.ssl.SNIHostName.createSNIMatcher("example.com")));
        check(p.getSNIMatchers().size() == 1, "SNIMatchers roundtrip");
        p.setUseCipherSuitesOrder(true);
        check(p.getUseCipherSuitesOrder(), "useCipherSuitesOrder roundtrip");
        p.setEndpointIdentificationAlgorithm("HTTPS");
        check(p.getEndpointIdentificationAlgorithm().equals("HTTPS"), "endpoint id algo");
        javax.net.ssl.SSLParameters q = new javax.net.ssl.SSLParameters(new String[]{"TLSv1.2"});
        check(Arrays.equals(q.getCipherSuites(), new String[]{"TLSv1.2"}), "single-arg ctor is cipherSuites");
        check(q.getProtocols() == null, "single-arg ctor protocols null");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
