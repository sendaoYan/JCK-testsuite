/*
 * @test
 * @summary sun.security.krb5.KrbCryptoException conformance: safe value/exception behavior.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main KrbCryptoExceptionTest
 */
public class KrbCryptoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        sun.security.krb5.KrbCryptoException e = new sun.security.krb5.KrbCryptoException("boom");
        check(e.getMessage().equals("boom"), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
