/*
 * @test
 * @summary sun.security.krb5.KrbCryptoException conformance: type shape + behavioral (headless-safe).
 * @run main KrbCryptoExceptionTest
 */

public class KrbCryptoExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.KrbCryptoException", false, KrbCryptoExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.KrbException"), "superclass");
        sun.security.krb5.KrbCryptoException kce = new sun.security.krb5.KrbCryptoException("crypto");
        check(kce.getMessage() != null, "KrbCryptoException message");
        check(kce instanceof sun.security.krb5.KrbException, "KrbCryptoException instanceof KrbException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
