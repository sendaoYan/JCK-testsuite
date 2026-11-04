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

        // equality
        check(f2.equals(new SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true)), "equal config equals");
        check(!f2.equals(f), "different config not equal");
        check(f2.hashCode() == new SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true).hashCode(), "equal hashCode");
        check(!f2.equals(null), "not equals null");

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
