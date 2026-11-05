/*
 * @test
 * @summary javax.rmi.ssl.SslRMIServerSocketFactory conformance: white-box structure + black-box behavior.
 * @run main SslRMIServerSocketFactoryTest
 */

import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import javax.net.ssl.SSLContext;

public class SslRMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.ssl.SslRMIServerSocketFactory");
        check(c != null, "loads");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(!c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.rmi.server.RMIServerSocketFactory.class.isAssignableFrom(c), "implements/extends java.rmi.server.RMIServerSocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getMethod("createServerSocket", int.class) != null, "createServerSocket(int) declared");
        check(c.getMethod("getEnabledCipherSuites") != null, "getEnabledCipherSuites declared");
        check(c.getMethod("getEnabledProtocols") != null, "getEnabledProtocols declared");
        check(c.getMethod("getNeedClientAuth") != null, "getNeedClientAuth declared");
        check(c.getMethod("equals", Object.class) != null, "equals declared");
        check(c.getMethod("hashCode") != null, "hashCode declared");
    }

    static void testBlackBox() throws Exception {
        javax.rmi.ssl.SslRMIServerSocketFactory f = new javax.rmi.ssl.SslRMIServerSocketFactory();
        check(f instanceof RMIServerSocketFactory, "instanceof RMIServerSocketFactory");
        check(f.getEnabledCipherSuites() == null, "default cipher suites null");
        check(f.getEnabledProtocols() == null, "default protocols null");
        check(!f.getNeedClientAuth(), "default needClientAuth false");

        // explicit configuration: constructor round trip
        javax.rmi.ssl.SslRMIServerSocketFactory f2 = new javax.rmi.ssl.SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true);
        check(f2.getEnabledCipherSuites().length == 1, "explicit cipher length");
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f2.getEnabledCipherSuites()[0]), "explicit cipher value");
        check(f2.getEnabledProtocols().length == 1, "explicit protocols length");
        check("TLSv1.2".equals(f2.getEnabledProtocols()[0]), "explicit protocol value");
        check(f2.getNeedClientAuth(), "explicit needClientAuth true");

        // constructor takes a defensive copy of the input arrays
        String[] ciphersArg = new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"};
        String[] protosArg = new String[]{"TLSv1.2"};
        javax.rmi.ssl.SslRMIServerSocketFactory f3 = new javax.rmi.ssl.SslRMIServerSocketFactory(ciphersArg, protosArg, false);
        ciphersArg[0] = "MUTATED";
        protosArg[0] = "MUTATED";
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f3.getEnabledCipherSuites()[0]),
                "constructor defensive copy of ciphers");
        check("TLSv1.2".equals(f3.getEnabledProtocols()[0]),
                "constructor defensive copy of protocols");

        // getters return fresh defensive copies (two calls give distinct arrays)
        String[] got1 = f3.getEnabledCipherSuites();
        String[] got2 = f3.getEnabledCipherSuites();
        check(got1 != got2, "getters return fresh copies");
        got1[0] = "MUTATED";
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f3.getEnabledCipherSuites()[0]), "getter defensive copy");

        // null arrays are accepted and mean "use SSL defaults"
        javax.rmi.ssl.SslRMIServerSocketFactory fNull = new javax.rmi.ssl.SslRMIServerSocketFactory(null, null, false);
        check(fNull.getEnabledCipherSuites() == null, "null ciphers ctor -> null getter");
        check(fNull.getEnabledProtocols() == null, "null protocols ctor -> null getter");
        check(!fNull.getNeedClientAuth(), "null arrays ctor needClientAuth false");

        // SSLContext 4-arg constructor (JDK-8+)
        javax.rmi.ssl.SslRMIServerSocketFactory f4 = new javax.rmi.ssl.SslRMIServerSocketFactory(
                SSLContext.getDefault(), new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, null, true);
        check("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equals(f4.getEnabledCipherSuites()[0]), "sslcontext ctor cipher");
        check(f4.getEnabledProtocols() == null, "sslcontext ctor null protocols");
        check(f4.getNeedClientAuth(), "sslcontext ctor needClientAuth true");

        // equality: value semantics over the configuration triple
        check(f2.equals(new javax.rmi.ssl.SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true)),
                "equal config equals");
        check(!f2.equals(f), "different config not equal");
        check(f2.hashCode() == new javax.rmi.ssl.SslRMIServerSocketFactory(
                new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}, new String[]{"TLSv1.2"}, true).hashCode(),
                "equal hashCode");
        check(!f2.equals(null), "not equals null");
        check(!f2.equals(new Object()), "not equals other type");
        check(f.equals(new javax.rmi.ssl.SslRMIServerSocketFactory(null, null, false)),
                "default ctor equals null-arrays ctor");

        // null/empty cipher elements rejected with a precise message
        try {
            new javax.rmi.ssl.SslRMIServerSocketFactory(new String[]{null}, null, false);
            check(false, "expected IAE for null cipher element");
        } catch (IllegalArgumentException e) {
            check("The specified CipherSuites array contains invalid null or empty string elements".equals(e.getMessage()),
                    "null cipher element IAE message");
        }

        // unsupported cipher -> IllegalArgumentException
        boolean iae = false;
        try { new javax.rmi.ssl.SslRMIServerSocketFactory(new String[]{"NOT_A_CIPHER_SUITE"}, null, false); }
        catch (IllegalArgumentException expected) { iae = true; }
        check(iae, "unsupported cipher throws IllegalArgumentException");

        // unsupported protocol -> IllegalArgumentException
        boolean iae2 = false;
        try { new javax.rmi.ssl.SslRMIServerSocketFactory(null, new String[]{"NOT_A_PROTOCOL"}, false); }
        catch (IllegalArgumentException expected) { iae2 = true; }
        check(iae2, "unsupported protocol throws IllegalArgumentException");

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
