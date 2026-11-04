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
        testContract();
    }
    // ---- Part 3: contract deepening (null semantics / defensive copies / JDK-8 surface) ----
    static void testContract() throws Exception {
        SSLParameters p = new SSLParameters();
        // constructor surface: exactly 3 public constructors
        int pubCtors = 0;
        boolean oneArg = false, twoArg = false;
        for (Constructor<?> k : SSLParameters.class.getConstructors()) {
            pubCtors++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String[].class) oneArg = true;
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == String[].class
                    && k.getParameterTypes()[1] == String[].class) twoArg = true;
        }
        check(pubCtors == 3, "exactly 3 public constructors, found " + pubCtors);
        check(oneArg && twoArg, "declares (String[]) and (String[],String[]) constructors");
        SSLParameters two = new SSLParameters(new String[]{"A"}, new String[]{"TLSv1.2"});
        check(Arrays.equals(two.getCipherSuites(), new String[]{"A"})
                && Arrays.equals(two.getProtocols(), new String[]{"TLSv1.2"}),
                "two-arg ctor (cipherSuites, protocols) round trip");
        // fresh-instance defaults beyond the ones above
        check(p.getApplicationProtocols() != null && p.getApplicationProtocols().length == 0,
                "fresh getApplicationProtocols is an empty array (never null)");
        check(p.getServerNames() == null && p.getSNIMatchers() == null,
                "fresh getServerNames/getSNIMatchers null");
        check(p.getUseCipherSuitesOrder() == false, "fresh useCipherSuitesOrder false");
        check(p.getEndpointIdentificationAlgorithm() == null, "fresh endpoint id null");
        // null-argument semantics: setters that accept null and reset to null ...
        p.setCipherSuites(null);
        check(p.getCipherSuites() == null, "setCipherSuites(null) accepted, getter back to null");
        p.setProtocols(null);
        check(p.getProtocols() == null, "setProtocols(null) accepted, getter back to null");
        p.setServerNames(null);
        check(p.getServerNames() == null, "setServerNames(null) accepted, getter back to null");
        p.setSNIMatchers(null);
        check(p.getSNIMatchers() == null, "setSNIMatchers(null) accepted, getter back to null");
        p.setEndpointIdentificationAlgorithm(null);
        check(p.getEndpointIdentificationAlgorithm() == null, "setEndpointIdentificationAlgorithm(null) accepted");
        // ... while setApplicationProtocols(null) is the documented IAE exception
        final SSLParameters fp = p;
        check(expect(() -> { fp.setApplicationProtocols(null); return "ok"; }).equals("IllegalArgumentException"),
                "setApplicationProtocols(null) -> IllegalArgumentException");
        // empty SNI list is legal and observable (returns empty list, not null)
        p.setServerNames(new ArrayList<SNIServerName>());
        check(p.getServerNames() != null && p.getServerNames().isEmpty(),
                "setServerNames(empty) preserved as empty list");
        // setter copies the source list: later mutation of the source is not observed
        List<SNIServerName> src = new ArrayList<>();
        src.add(new SNIHostName("e.com"));
        p.setServerNames(src);
        src.clear();
        check(p.getServerNames().size() == 1, "setServerNames copies the list (source mutation invisible)");
        // setter also copies matchers
        List<SNIMatcher> ms = new ArrayList<>();
        ms.add(SNIHostName.createSNIMatcher("e.com"));
        p.setSNIMatchers(ms);
        ms.clear();
        check(p.getSNIMatchers().size() == 1, "setSNIMatchers copies the list (source mutation invisible)");
        // need=false / want=false clear their own flag without touching the other
        p = new SSLParameters();
        p.setNeedClientAuth(true);
        p.setNeedClientAuth(false);
        check(!p.getNeedClientAuth() && !p.getWantClientAuth(), "setNeedClientAuth(false) clears need only");
        p.setWantClientAuth(true);
        p.setWantClientAuth(false);
        check(!p.getWantClientAuth() && !p.getNeedClientAuth(), "setWantClientAuth(false) clears want only");
        // protocols array is defensively copied on the getter side too
        p.setProtocols(new String[]{"TLSv1.2"});
        String[] gotProto = p.getProtocols();
        gotProto[0] = "MUT";
        check(p.getProtocols()[0].equals("TLSv1.2"), "getProtocols returns a fresh copy each call");
        // ALPN: same empty-array default after a round trip to empty
        p.setApplicationProtocols(new String[0]);
        check(p.getApplicationProtocols().length == 0, "setApplicationProtocols(empty) preserved");
        // JDK-8 surface: the DTLS-era setters added in JDK 9 are NOT present here
        String[] jdk9Plus = {"setNamedGroups", "getNamedGroups", "setSignatureSchemes", "getSignatureSchemes",
                "setMaximumPacketSize", "getMaximumPacketSize", "setEnableRetransmissions", "getEnableRetransmissions"};
        for (String n : jdk9Plus) {
            boolean found = false;
            for (Method m : SSLParameters.class.getMethods()) if (m.getName().equals(n)) found = true;
            check(!found, "JDK-8 does not declare " + n + "(added in JDK 9)");
        }
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
