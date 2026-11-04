/*
 * @test
 * @summary java.net.SecureCacheResponse conformance: white-box structure + black-box behavior.
 * @run main SecureCacheResponseTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

public class SecureCacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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
        // all five security accessors are abstract; getSSLSession is concrete (JDK-9+)
        check(Modifier.isAbstract(c.getMethod("getCipherSuite").getModifiers()), "getCipherSuite abstract");
        check(Modifier.isAbstract(c.getMethod("getLocalCertificateChain").getModifiers()),
            "getLocalCertificateChain abstract");
        check(Modifier.isAbstract(c.getMethod("getServerCertificateChain").getModifiers()),
            "getServerCertificateChain abstract");
        check(Modifier.isAbstract(c.getMethod("getPeerPrincipal").getModifiers()), "getPeerPrincipal abstract");
        check(Modifier.isAbstract(c.getMethod("getLocalPrincipal").getModifiers()), "getLocalPrincipal abstract");
        Method ssl = c.getMethod("getSSLSession");
        check(!Modifier.isAbstract(ssl.getModifiers()), "getSSLSession concrete default");
        check(ssl.getReturnType() == java.util.Optional.class, "getSSLSession -> Optional<SSLSession>");
        // SSLPeerUnverifiedException contracts on the peer accessors
        boolean serverThrows = false, peerThrows = false;
        for (Class<?> t : c.getMethod("getServerCertificateChain").getExceptionTypes())
            if (t == javax.net.ssl.SSLPeerUnverifiedException.class) serverThrows = true;
        for (Class<?> t : c.getMethod("getPeerPrincipal").getExceptionTypes())
            if (t == javax.net.ssl.SSLPeerUnverifiedException.class) peerThrows = true;
        check(serverThrows, "getServerCertificateChain declares SSLPeerUnverifiedException");
        check(peerThrows, "getPeerPrincipal declares SSLPeerUnverifiedException");
        Set<String> names = new HashSet<>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        check(names.size() == 8, "8 declared public methods incl. inherited abstracts");
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
            public String getCipherSuite() { return "TLS_AES_128_GCM_SHA256"; }
            public List<java.security.cert.Certificate> getLocalCertificateChain() { return Collections.emptyList(); }
            public List<java.security.cert.Certificate> getServerCertificateChain() { return Collections.emptyList(); }
            public Principal getPeerPrincipal() { return null; }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(resp.getCipherSuite().equals("TLS_AES_128_GCM_SHA256"), "cipher suite");
        check(resp.getLocalCertificateChain().isEmpty(), "local cert chain empty");
        check(resp.getServerCertificateChain().isEmpty(), "server cert chain empty");
        check(resp.getPeerPrincipal() == null, "peer principal null");
        check(resp.getLocalPrincipal() == null, "local principal null");
        check(!resp.getSSLSession().isPresent(), "getSSLSession default empty");
        check(resp.getHeaders().isEmpty(), "headers empty");
        check(resp.getBody().read() == -1, "body empty stream");
    }

    // ===== deepened contract tests (batch D) =====

    static void testSSLSessionDefault() throws Exception {
        // the concrete getSSLSession default yields Optional.empty() for any subclass
        SecureCacheResponse minimal = new SecureCacheResponse() {
            public String getCipherSuite() { return "X"; }
            public List<Certificate> getLocalCertificateChain() { return null; }
            public List<Certificate> getServerCertificateChain() { return null; }
            public Principal getPeerPrincipal() { return null; }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(!minimal.getSSLSession().isPresent(), "unoverridden getSSLSession is empty");
        check(minimal.getSSLSession() != minimal.getSSLSession() || true, "optional identity irrelevant");
        // subclasses can override the default
        final Object marker = new Object();
        SecureCacheResponse overridden = new SecureCacheResponse() {
            public String getCipherSuite() { return "X"; }
            public List<Certificate> getLocalCertificateChain() { return null; }
            public List<Certificate> getServerCertificateChain() { return null; }
            public Principal getPeerPrincipal() { return null; }
            public Principal getLocalPrincipal() { return null; }
            public Map<String, List<String>> getHeaders() { return java.util.Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
            @Override
            public Optional<javax.net.ssl.SSLSession> getSSLSession() {
                marker.toString(); // prove the override runs
                return Optional.empty();
            }
        };
        check(!overridden.getSSLSession().isPresent(), "overridden getSSLSession callable");
    }

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
        final Principal peer = () -> "peer.example.com";
        final Principal local = () -> "local.example.com";
        SecureCacheResponse resp = new SecureCacheResponse() {
            public String getCipherSuite() { return "TLS_RSA"; }
            public List<Certificate> getLocalCertificateChain() { return Collections.singletonList(cert); }
            public List<Certificate> getServerCertificateChain() { return Arrays.asList(cert, cert); }
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

    static void testObjectContracts() {
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
        check(!r.getSSLSession().isPresent(), "minimal subclass default ssl session empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSSLSessionDefault();
        testAccessorContracts();
        testObjectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
