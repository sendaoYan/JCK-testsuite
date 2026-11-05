/*
 * @test
 * @summary javax.rmi.ssl.SslRMIServerSocketFactory conformance: white-box structure + black-box behavior.
 * @run main SslRMIServerSocketFactoryTest
 */

import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class SslRMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = SslRMIServerSocketFactory.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(RMIServerSocketFactory.class.isAssignableFrom(c), "implements RMIServerSocketFactory");
        check(c.getMethod("createServerSocket", int.class) != null, "createServerSocket(int) declared");
    }

    static void testBlackBox() throws Exception {
        // default configuration
        SslRMIServerSocketFactory f = new SslRMIServerSocketFactory();
        check(f instanceof RMIServerSocketFactory, "instanceof RMIServerSocketFactory");
        check(f.getEnabledCipherSuites() == null, "default cipher suites null");
        check(f.getEnabledProtocols() == null, "default protocols null");
        check(!f.getNeedClientAuth(), "default needClientAuth false");

        // explicit configuration
        SslRMIServerSocketFactory f2 = new SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true);
        check(f2.getEnabledCipherSuites().length == 1, "explicit cipher length");
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f2.getEnabledCipherSuites()[0]), "explicit cipher value");
        check(f2.getEnabledProtocols().length == 1, "explicit protocols length");
        check("TLSv1.2".equals(f2.getEnabledProtocols()[0]), "explicit protocol value");
        check(f2.getNeedClientAuth(), "explicit needClientAuth true");

        // getters return defensive copies
        String[] ciphers = f2.getEnabledCipherSuites();
        ciphers[0] = "MUTATED";
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f2.getEnabledCipherSuites()[0]), "getter defensive copy");
        check(f2.getEnabledCipherSuites() != f2.getEnabledCipherSuites(), "getters return fresh copies");

        // constructor takes a defensive copy of the input arrays
        String[] ciphersArg = new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"};
        String[] protosArg = new String[]{"TLSv1.2"};
        SslRMIServerSocketFactory f3 = new SslRMIServerSocketFactory(ciphersArg, protosArg, false);
        ciphersArg[0] = "MUTATED";
        protosArg[0] = "MUTATED";
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f3.getEnabledCipherSuites()[0]),
                "constructor defensive copy of ciphers");
        check("TLSv1.2".equals(f3.getEnabledProtocols()[0]),
                "constructor defensive copy of protocols");

        // null arrays are accepted and mean "use SSL defaults"
        SslRMIServerSocketFactory fNull = new SslRMIServerSocketFactory(null, null, false);
        check(fNull.getEnabledCipherSuites() == null, "null ciphers ctor -> null getter");
        check(fNull.getEnabledProtocols() == null, "null protocols ctor -> null getter");
        check(!fNull.getNeedClientAuth(), "null arrays ctor needClientAuth false");

        // SSLContext 4-arg constructor
        SslRMIServerSocketFactory f4 = new SslRMIServerSocketFactory(
                javax.net.ssl.SSLContext.getDefault(), new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, null, true);
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f4.getEnabledCipherSuites()[0]), "sslcontext ctor cipher");
        check(f4.getEnabledProtocols() == null, "sslcontext ctor null protocols");
        check(f4.getNeedClientAuth(), "sslcontext ctor needClientAuth true");

        // equality
        check(f2.equals(new SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true)), "equal config equals");
        check(!f2.equals(f), "different config not equal");
        check(f2.hashCode() == new SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true).hashCode(), "equal hashCode");
        check(!f2.equals(null), "not equals null");
        check(f.equals(new SslRMIServerSocketFactory(null, null, false)),
                "default ctor equals null-arrays ctor");

        // null/empty cipher elements rejected with a precise message
        try {
            new SslRMIServerSocketFactory(new String[]{null}, null, false);
            check(false, "expected IAE for null cipher element");
        } catch (IllegalArgumentException e) {
            check("The specified CipherSuites array contains invalid null or empty string elements".equals(e.getMessage()),
                    "null cipher element IAE message");
        }

        // unsupported protocol -> IllegalArgumentException
        boolean iae2 = false;
        try { new SslRMIServerSocketFactory(null, new String[]{"NOT_A_PROTOCOL"}, false); }
        catch (IllegalArgumentException expected) { iae2 = true; }
        check(iae2, "unsupported protocol throws IllegalArgumentException");

        // unsupported cipher -> IllegalArgumentException
        boolean iae = false;
        try { new SslRMIServerSocketFactory(new String[]{"NOT_A_CIPHER_SUITE"}, null, false); }
        catch (IllegalArgumentException expected) { iae = true; }
        check(iae, "unsupported cipher throws IllegalArgumentException");

        // createServerSocket binds an ephemeral port (local, closed immediately)
        ServerSocket ss = f.createServerSocket(0);
        check(ss != null, "createServerSocket non-null");
        check(ss.getLocalPort() > 0, "createServerSocket bound ephemeral port");
        ss.close();
        check(ss.isClosed(), "server socket closed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
