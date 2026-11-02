/*
 * @test
 * @summary javax.rmi.ssl.SslRMIClientSocketFactory conformance: ssl rmi client socket factory.
 * @run main SslRMIClientSocketFactoryTest
 */

public class SslRMIClientSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.rmi.ssl.SslRMIClientSocketFactory f1 = new javax.rmi.ssl.SslRMIClientSocketFactory();
        javax.rmi.ssl.SslRMIClientSocketFactory f2 = new javax.rmi.ssl.SslRMIClientSocketFactory();
        check(f1.equals(f2), "equals");
        check(f1.hashCode() == f2.hashCode(), "hashCode");
        check(!f1.equals(null), "not equals null");
        boolean threw = false;
        try { f1.createSocket("127.0.0.1", 1); } catch (java.io.IOException e) { threw = true; }
        check(threw, "createSocket to closed port throws IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
