/*
 * @test
 * @summary java.net.SecureCacheResponse conformance: white-box structure + black-box behavior.
 * @run main SecureCacheResponseTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CacheResponse;
import java.net.SecureCacheResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecureCacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===== original assertions preserved verbatim =====
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.SecureCacheResponse");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.net.CacheResponse".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("getCipherSuite"), "has method getCipherSuite");
        check(names.contains("getHeaders"), "has method getHeaders");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SecureCacheResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(CacheResponse.class.isAssignableFrom(c), "extends CacheResponse");
        check(c.getSuperclass() == CacheResponse.class, "direct subclass of CacheResponse");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(c.getMethod("getCipherSuite") != null, "getCipherSuite");
        check(c.getMethod("getLocalCertificateChain") != null, "getLocalCertificateChain");
        check(c.getMethod("getServerCertificateChain") != null, "getServerCertificateChain");
        check(c.getMethod("getPeerPrincipal") != null, "getPeerPrincipal");
        check(c.getMethod("getLocalPrincipal") != null, "getLocalPrincipal");
        // all five security accessors are abstract
        check(Modifier.isAbstract(c.getMethod("getCipherSuite").getModifiers()), "getCipherSuite abstract");
        check(Modifier.isAbstract(c.getMethod("getLocalCertificateChain").getModifiers()),
            "getLocalCertificateChain abstract");
        check(Modifier.isAbstract(c.getMethod("getServerCertificateChain").getModifiers()),
            "getServerCertificateChain abstract");
        check(Modifier.isAbstract(c.getMethod("getPeerPrincipal").getModifiers()), "getPeerPrincipal abstract");
        check(Modifier.isAbstract(c.getMethod("getLocalPrincipal").getModifiers()), "getLocalPrincipal abstract");
        // JDK-8 divergence: getSSLSession() does NOT exist yet (added in JDK-9)
        try {
            c.getMethod("getSSLSession");
            check(false, "JDK-8 must not have getSSLSession");
        } catch (NoSuchMethodException e) {
            check(true, "JDK-8 divergence: getSSLSession absent (added in JDK-9)");
        }
        // SSLPeerUnverifiedException contracts on the peer accessors
        boolean serverThrows = false, peerThrows = false;
        for (Class<?> t : c.getMethod("getServerCertificateChain").getExceptionTypes())
            if (t == javax.net.ssl.SSLPeerUnverifiedException.class) serverThrows = true;
        for (Class<?> t : c.getMethod("getPeerPrincipal").getExceptionTypes())
            if (t == javax.net.ssl.SSLPeerUnverifiedException.class) peerThrows = true;
        check(serverThrows, "getServerCertificateChain declares SSLPeerUnverifiedException");
        check(peerThrows, "getPeerPrincipal declares SSLPeerUnverifiedException");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        // JDK-8: 7 methods (no getSSLSession; JDK-27 has 8)
        check(names.size() == 7, "7 declared public methods (JDK-8: no getSSLSession)");
        try {
            c.getDeclaredConstructor().newInstance();
            check(false, "newInstance should fail");
        } catch (InstantiationException e) {
            check(true, "InstantiationException");
        } catch (InvocationTargetException e) {
            check(true, "wrapped InstantiationException");
        }
    }

    static void testBlackBox() throws Exception {
        SecureCacheResponse resp = new SecureCacheResponse() {
            public String getCipherSuite() { return "TLS_RSA_WITH_AES_128_CBC_SHA"; }
            public List<Certificate> getLocalCertificateChain() { return Collections.emptyList(); }
            public List<Certificate> getServerCertificateChain() { return Collections.emptyList(); }
            public Principal getPeerPrincipal() { return null; }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(resp.getCipherSuite().equals("TLS_RSA_WITH_AES_128_CBC_SHA"), "cipher suite");
        check(resp.getLocalCertificateChain().isEmpty(), "local cert chain empty");
        check(resp.getServerCertificateChain().isEmpty(), "server cert chain empty");
        check(resp.getPeerPrincipal() == null, "peer principal null");
        check(resp.getLocalPrincipal() == null, "local principal null");
        check(resp.getHeaders().isEmpty(), "headers empty");
        check(resp.getBody().read() == -1, "body empty stream");
    }

    // ===== deepened contract tests (batch D) =====

    static void testAccessorContracts() throws Exception {
        // non-empty chains and principals round-trip exactly
        final Certificate cert = new Certificate("X.509") {
            public byte[] getEncoded() { return new byte[0]; }
            public void verify(java.security.PublicKey k) { }
            public void verify(java.security.PublicKey k, String sigProvider) { }
            public String toString() { return "fake"; }
            public java.security.PublicKey getPublicKey() { return null; }
            public int hashCode() { return 42; }
        };
        final Principal peer = new Principal() {
            public String getName() { return "peer.example.com"; }
        };
        final Principal local = new Principal() {
            public String getName() { return "local.example.com"; }
        };
        SecureCacheResponse resp = new SecureCacheResponse() {
            public String getCipherSuite() { return "TLS_RSA"; }
            public List<Certificate> getLocalCertificateChain() { return Collections.singletonList(cert); }
            public List<Certificate> getServerCertificateChain() { return java.util.Arrays.asList(cert, cert); }
            public Principal getPeerPrincipal() { return peer; }
            public Principal getLocalPrincipal() { return local; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(resp.getCipherSuite().equals("TLS_RSA"), "cipher round-trip");
        check(resp.getLocalCertificateChain().size() == 1, "local chain size");
        check(resp.getLocalCertificateChain().get(0) == cert, "local chain exact element");
        check(resp.getServerCertificateChain().size() == 2, "server chain size");
        check(resp.getServerCertificateChain().get(1) == cert, "server chain exact element");
        check(resp.getPeerPrincipal() == peer, "peer principal exact instance");
        check(resp.getLocalPrincipal() == local, "local principal exact instance");
        check(resp.getPeerPrincipal().getName().equals("peer.example.com"), "peer principal name");
        check(resp.getLocalPrincipal().getName().equals("local.example.com"), "local principal name");
        // peer accessors may throw SSLPeerUnverifiedException per contract
        SecureCacheResponse unverified = new SecureCacheResponse() {
            public String getCipherSuite() { return "TLS_X"; }
            public List<Certificate> getLocalCertificateChain() { return null; }
            public List<Certificate> getServerCertificateChain() throws javax.net.ssl.SSLPeerUnverifiedException {
                throw new javax.net.ssl.SSLPeerUnverifiedException("no-peer-cert");
            }
            public Principal getPeerPrincipal() throws javax.net.ssl.SSLPeerUnverifiedException {
                throw new javax.net.ssl.SSLPeerUnverifiedException("no-peer");
            }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        try { unverified.getServerCertificateChain(); check(false, "expected SSLPeerUnverifiedException"); }
        catch (javax.net.ssl.SSLPeerUnverifiedException e) {
            check(e.getMessage().equals("no-peer-cert"), "server chain unverified propagated");
        }
        try { unverified.getPeerPrincipal(); check(false, "expected SSLPeerUnverifiedException"); }
        catch (javax.net.ssl.SSLPeerUnverifiedException e) {
            check(e.getMessage().equals("no-peer"), "peer principal unverified propagated");
        }
    }

    static void testObjectContracts() throws Exception {
        SecureCacheResponse r = new SecureCacheResponse() {
            public String getCipherSuite() { return null; }
            public List<Certificate> getLocalCertificateChain() { return null; }
            public List<Certificate> getServerCertificateChain() { return null; }
            public Principal getPeerPrincipal() { return null; }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(r.equals(r), "identity equals");
        check(r.hashCode() == r.hashCode(), "hashCode stable");
        check(r.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testBlackBox();
        testAccessorContracts();
        testObjectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
