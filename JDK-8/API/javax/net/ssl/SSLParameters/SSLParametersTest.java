/*
 * @test
 * @summary javax.net.ssl.SSLParameters conformance: setters and getters.
 * @run main SSLParametersTest
 */

public class SSLParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLParameters p = new javax.net.ssl.SSLParameters();
        p.setCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"});
        check(p.getCipherSuites().length == 1, "set/getCipherSuites");
        p.setProtocols(new String[]{"TLSv1.2"});
        check(p.getProtocols().length == 1, "set/getProtocols");
        check(!p.getNeedClientAuth() && !p.getWantClientAuth(), "client auth defaults");
        p.setEndpointIdentificationAlgorithm("HTTPS");
        check("HTTPS".equals(p.getEndpointIdentificationAlgorithm()), "getEndpointIdentificationAlgorithm");
        javax.net.ssl.SSLParameters p2 = new javax.net.ssl.SSLParameters(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"});
        check(p2.getCipherSuites().length == 1, "String[] ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
