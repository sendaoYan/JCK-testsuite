/*
 * @test
 * @summary javax.rmi.ssl.SslRMIServerSocketFactory conformance: ssl rmi server socket factory.
 * @run main SslRMIServerSocketFactoryTest
 */

public class SslRMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.rmi.ssl.SslRMIServerSocketFactory f = new javax.rmi.ssl.SslRMIServerSocketFactory();
        check(f.getEnabledCipherSuites() == null, "default cipher suites null");
        check(f.getEnabledProtocols() == null, "default protocols null");
        check(!f.getNeedClientAuth(), "default needClientAuth false");
        javax.rmi.ssl.SslRMIServerSocketFactory f2 = new javax.rmi.ssl.SslRMIServerSocketFactory(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true);
        check(f2.getEnabledCipherSuites().length == 1, "cipher length");
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f2.getEnabledCipherSuites()[0]), "cipher value");
        check(f2.getEnabledProtocols().length == 1, "protocols length");
        check("TLSv1.2".equals(f2.getEnabledProtocols()[0]), "protocol value");
        check(f2.getNeedClientAuth(), "needClientAuth true");
        java.net.ServerSocket ss = f.createServerSocket(0);
        check(ss != null, "createServerSocket non-null");
        check(ss.getLocalPort() > 0, "bound port");
        ss.close();
        check(f2.equals(new javax.rmi.ssl.SslRMIServerSocketFactory(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
