/*
 * @test
 * @summary java.net.SecureCacheResponse conformance: white-box structure + black-box behavior.
 * @run main SecureCacheResponseTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

public class SecureCacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SecureCacheResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(CacheResponse.class.isAssignableFrom(c), "extends CacheResponse");
        check(c.getMethod("getCipherSuite") != null, "getCipherSuite");
        check(c.getMethod("getLocalCertificateChain") != null, "getLocalCertificateChain");
        check(c.getMethod("getServerCertificateChain") != null, "getServerCertificateChain");
        check(c.getMethod("getPeerPrincipal") != null, "getPeerPrincipal");
        check(c.getMethod("getLocalPrincipal") != null, "getLocalPrincipal");
        check(c.getMethod("getSSLSession") != null, "getSSLSession");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
